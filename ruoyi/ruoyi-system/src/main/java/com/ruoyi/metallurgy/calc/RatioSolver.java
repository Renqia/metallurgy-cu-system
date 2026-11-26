package com.ruoyi.metallurgy.calc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.metallurgy.dto.RatioMaterialItem;
import com.ruoyi.metallurgy.dto.RatioRequest;
import com.ruoyi.metallurgy.dto.RatioResult;

/**
 * 配料配比求解器（纯算法类，不依赖 Spring 容器，便于单元测试）
 *
 * <h3>数学模型</h3>
 * 设各物料配比为 x_i（%，其数值等于 100 kg 混合料中该物料的质量 kg），未知量为待求物料的配比。
 * <p>约束一（质量守恒）：</p>
 * <pre>  Σ x_i = 100                                      (1)</pre>
 * <p>约束二（渣型约束，由冰铜 Cu2S-FeS 物相模型推导）：</p>
 * <p>闪速熔炼中铜几乎全部进入冰铜，冰铜由 Cu2S 与 FeS 组成，则给定目标冰铜品位 G(%) 时：</p>
 * <pre>
 *   冰铜量      M_matte = Cu_mix × 100 / G
 *   Cu2S 质量   = Cu_mix / 0.7985574 = 1.2522582 × Cu_mix
 *   FeS 质量    = M_matte − Cu2S  = Cu_mix × (100/G − 1.2522582)
 *   冰铜中 Fe   = 0.6354221 × FeS = 0.6354221 × Cu_mix × (100/G − 1.2522582)
 *   渣中 Fe     = Fe_mix − 冰铜中 Fe
 *   渣型要求    Fe_slag / SiO2_slag = R（目标硅铁比），且 SiO2 全部进渣
 * </pre>
 * <p>整理后得到关于 x_i 的线性方程：</p>
 * <pre>
 *   Σ x_i × [ (fe_i − cG × cu_i − R × sio2_i) / 100 ] = 0 ,  其中 cG = 0.6354221 × (100/G − 1.2522582)
 * </pre>
 * 两条约束均为线性，故配料计算归结为求解 2×n 线性方程组。
 *
 * <h3>求解策略</h3>
 * <ol>
 *   <li>用户锁定的物料配比直接代入，移到方程右端；</li>
 *   <li>待求物料数 &gt; 2 时方程欠定，取<b>最小范数解</b> x = Aᵀ(AAᵀ)⁻¹b（在满足两条约束的所有解中取模最小的解，
 *       即"各物料配比尽量均衡"的方案，避免出现个别物料配比异常放大）；</li>
 *   <li>出现负配比时采用简化<b>主动集法</b>：把负值最大的物料置为 0 并移出待求集，重新求解，迭代至全非负；</li>
 *   <li>约束无法同时满足（如 SiO2 不足、物料成分雷同导致系数矩阵退化）时，<b>不抛异常中断</b>，
 *       而是返回最接近的可行解并输出偏差说明。</li>
 * </ol>
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
public final class RatioSolver
{
    /** 判定为零/退化的容差 */
    private static final double EPS = 1e-10;

    /** 允许的最大物料种类数（与《需求与范围.md》一致） */
    private static final int MAX_ITEMS = 10;

    /** 配比保留的小数位 */
    private static final int RATIO_SCALE = 2;

    private RatioSolver()
    {
    }

    /**
     * 求解配料配比
     *
     * @param request 配料计算请求
     * @return 配料计算结果（含各物料配比、混合料成分、约束偏差与提示）
     */
    public static RatioResult solve(RatioRequest request)
    {
        if (request == null || request.getItems() == null || request.getItems().isEmpty())
        {
            throw new ServiceException("请先选择参与计算的物料");
        }
        List<RatioMaterialItem> items = request.getItems();
        if (items.size() < 2)
        {
            throw new ServiceException("配料计算至少需要 2 种物料（物料不足，无法同时满足质量守恒与渣型约束）");
        }
        if (items.size() > MAX_ITEMS)
        {
            throw new ServiceException("配料计算最多支持 " + MAX_ITEMS + " 种物料，当前 " + items.size() + " 种");
        }

        BigDecimal targetGrade = request.getMatteGrade();
        BigDecimal targetRatio = request.getFeSio2Ratio();
        if (targetGrade.compareTo(BigDecimal.ZERO) <= 0 || targetGrade.compareTo(new BigDecimal("100")) >= 0)
        {
            throw new ServiceException("目标冰铜品位必须在 0 ~ 100% 之间");
        }
        if (targetRatio.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("目标硅铁比必须大于 0");
        }
        // 冰铜品位在 Cu2S-FeS 物相模型下的物理上限为纯 Cu2S 的含铜量 79.86%
        if (targetGrade.doubleValue() >= MetallurgyConstants.CU_IN_CU2S.doubleValue() * 100)
        {
            throw new ServiceException("目标冰铜品位须低于纯 Cu2S 的理论含铜量 79.86%，否则冰铜中不存在 FeS 相");
        }

        RatioResult result = new RatioResult();
        result.setMatteGradeTarget(scale(targetGrade, 2));
        result.setFeSio2Target(scale(targetRatio, 3));
        result.setItems(items);

        // 冰铜物相模型系数 cG
        double cG = MetallurgyConstants.FE_IN_FES.doubleValue()
                * (100.0 / targetGrade.doubleValue() - MetallurgyConstants.CU2S_PER_CU.doubleValue());
        double r = targetRatio.doubleValue();

        int n = items.size();
        // 约束(2)的系数：a2_i = (fe_i - cG*cu_i - R*sio2_i) / 100
        double[] a2 = new double[n];
        for (int i = 0; i < n; i++)
        {
            RatioMaterialItem it = items.get(i);
            a2[i] = (it.getFeContent().doubleValue() - cG * it.getCuContent().doubleValue()
                    - r * it.getSio2Content().doubleValue()) / 100.0;
            // 说明：a2 系数只由 Fe/Cu/SiO2 决定；硫含量在熔炼节点参与硫平衡校核
        }

        // 分离固定物料与待求物料
        boolean[] fixedFlag = new boolean[n];
        boolean[] zeroFlag = new boolean[n];
        double[] x = new double[n];
        double fixedSum = 0;
        double fixedA2Sum = 0;
        List<Integer> active = new ArrayList<>();
        for (int i = 0; i < n; i++)
        {
            RatioMaterialItem it = items.get(i);
            if (it.isFixed())
            {
                fixedFlag[i] = true;
                x[i] = it.getFixedRatio().doubleValue();
                fixedSum += x[i];
                fixedA2Sum += x[i] * a2[i];
            }
            else
            {
                active.add(i);
            }
        }

        List<String> notes = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        notes.add("约束方程(1) 质量守恒：Σx = 100%");
        notes.add("约束方程(2) 渣型约束：Σx·(Fe − cG·Cu − R·SiO2)/100 = 0，其中 cG = "
                + String.format("%.6f", cG) + "，R = " + r);

        if (fixedSum > 100 + EPS)
        {
            throw new ServiceException("锁定配比的合计为 " + String.format("%.2f", fixedSum)
                    + "%，已超过 100%，请调低锁定配比");
        }
        if (!active.isEmpty())
        {
            double b1 = 100.0 - fixedSum;
            double b2 = -fixedA2Sum;
            int round = 0;
            while (!active.isEmpty() && round <= n)
            {
                round++;
                double[] solved = solveMinNorm(active, a2, b1, b2, notes);
                int worst = -1;
                double worstVal = -EPS;
                for (int idx : active)
                {
                    if (solved[idx] < worstVal)
                    {
                        worstVal = solved[idx];
                        worst = idx;
                    }
                }
                if (worst < 0)
                {
                    for (int idx : active)
                    {
                        x[idx] = Math.max(solved[idx], 0);
                    }
                    break;
                }
                // 主动集：把负值最大的物料置零，移出待求集合后重新求解
                notes.add("主动集迭代 " + round + "：物料【" + items.get(worst).getMaterialName()
                        + "】解得配比 " + String.format("%.4f", worstVal) + "% 为负，按工艺不可行处理，置为 0 后重新求解");
                x[worst] = 0;
                zeroFlag[worst] = true;
                active.remove(Integer.valueOf(worst));
            }
        }

        // 保留 2 位小数并做质量守恒补偿，保证 Σx 精确等于 100.00
        BigDecimal[] ratios = new BigDecimal[n];
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < n; i++)
        {
            ratios[i] = BigDecimal.valueOf(x[i]).setScale(RATIO_SCALE, RoundingMode.HALF_UP);
            if (ratios[i].compareTo(BigDecimal.ZERO) < 0)
            {
                ratios[i] = BigDecimal.ZERO;
            }
            sum = sum.add(ratios[i]);
        }
        BigDecimal diff = new BigDecimal("100").subtract(sum);
        if (diff.compareTo(BigDecimal.ZERO) != 0)
        {
            int maxIdx = 0;
            for (int i = 1; i < n; i++)
            {
                if (ratios[i].compareTo(ratios[maxIdx]) > 0)
                {
                    maxIdx = i;
                }
            }
            ratios[maxIdx] = ratios[maxIdx].add(diff);
            notes.add("舍入补偿：将 " + diff.toPlainString() + "% 的舍入差补入物料【"
                    + items.get(maxIdx).getMaterialName() + "】，使配比合计精确等于 100.00%");
        }
        for (int i = 0; i < n; i++)
        {
            items.get(i).setRatio(ratios[i]);
        }

        // 混合料加权成分与成本
        BigDecimal cuMix = BigDecimal.ZERO;
        BigDecimal feMix = BigDecimal.ZERO;
        BigDecimal sMix = BigDecimal.ZERO;
        BigDecimal sio2Mix = BigDecimal.ZERO;
        BigDecimal caoMix = BigDecimal.ZERO;
        BigDecimal cost = BigDecimal.ZERO;
        BigDecimal ratioSum = BigDecimal.ZERO;
        for (RatioMaterialItem it : items)
        {
            BigDecimal w = it.getRatio().divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP);
            cuMix = cuMix.add(w.multiply(it.getCuContent()));
            feMix = feMix.add(w.multiply(it.getFeContent()));
            sMix = sMix.add(w.multiply(it.getSulfurContent()));
            sio2Mix = sio2Mix.add(w.multiply(it.getSio2Content()));
            caoMix = caoMix.add(w.multiply(it.getCaoContent()));
            cost = cost.add(w.multiply(it.getUnitPrice()));
            ratioSum = ratioSum.add(it.getRatio());
        }
        result.setMixedCu(scale(cuMix, 3));
        result.setMixedFe(scale(feMix, 3));
        result.setMixedS(scale(sMix, 3));
        result.setMixedSio2(scale(sio2Mix, 3));
        result.setMixedCao(scale(caoMix, 3));
        result.setUnitCost(scale(cost, 2));
        result.setRatioSum(scale(ratioSum, 2));
        result.setMassBalanceResidual(scale(new BigDecimal("100").subtract(ratioSum).abs(), 3));

        // 由混合料成分反算冰铜量与渣中 Fe，得到实际硅铁比
        double cuMixD = cuMix.doubleValue();
        double feMixD = feMix.doubleValue();
        double sio2MixD = sio2Mix.doubleValue();
        double feMatte = MetallurgyConstants.FE_IN_FES.doubleValue() * cuMixD
                * (100.0 / targetGrade.doubleValue() - MetallurgyConstants.CU2S_PER_CU.doubleValue());
        double feSlag = feMixD - feMatte;
        double feSio2Achieved = 0;
        if (sio2MixD > EPS)
        {
            feSio2Achieved = feSlag / sio2MixD;
        }
        else
        {
            warnings.add("混合料中 SiO2 含量为 0，无法造渣，硅铁比无法计算——请在配料中提高熔剂（石英石）比例");
        }
        if (cuMixD <= EPS)
        {
            warnings.add("混合料中 Cu 含量为 0，无法形成冰铜——请检查所选物料的成分数据");
        }
        if (feMatte < 0)
        {
            warnings.add("按目标冰铜品位反算的冰铜含铁量为负，说明目标品位与混合料铜铁比不匹配（铜过多）");
        }
        if (feSlag < 0)
        {
            warnings.add("渣中含铁量为负，说明冰铜带走了过多的铁，请降低目标冰铜品位或调整配比");
        }
        result.setMatteGradeAchieved(scale(targetGrade, 2));
        result.setFeSio2Achieved(scale(BigDecimal.valueOf(feSio2Achieved), 3));
        BigDecimal dev = BigDecimal.valueOf(feSio2Achieved).subtract(targetRatio);
        result.setFeSio2Deviation(scale(dev, 3));

        // 可行性判定
        boolean feasible = true;
        if (result.getMassBalanceResidual().compareTo(new BigDecimal("0.01")) > 0)
        {
            feasible = false;
            warnings.add("质量守恒残差为 " + result.getMassBalanceResidual().toPlainString()
                    + "%，配比合计偏离 100%，请检查锁定配比的设置");
        }
        if (result.getFeSio2Deviation().abs().compareTo(new BigDecimal("0.05")) > 0)
        {
            feasible = false;
            warnings.add("实际硅铁比 " + result.getFeSio2Achieved().toPlainString()
                    + " 与目标 " + result.getFeSio2Target().toPlainString()
                    + " 偏差 " + result.getFeSio2Deviation().toPlainString()
                    + "，超出 ±0.05。常见原因：物料种类不足（自由度不够）、熔剂量受锁定、" 
                    + "或所选铜精矿的 Fe/SiO2 与目标渣型不匹配");
        }
        for (RatioMaterialItem it : items)
        {
            if (it.getRatio().compareTo(BigDecimal.ZERO) < 0)
            {
                feasible = false;
            }
        }
        result.setFeasible(feasible);
        if (feasible)
        {
            notes.add("求解成功：两条约束同时满足，全部配比非负");
        }
        else
        {
            notes.add("求解完成但存在偏差：已返回最接近的可行解，偏差原因见提示信息");
        }
        result.setSolveNotes(notes);
        result.setWarnings(warnings);
        return result;
    }

    /**
     * 求解 2×n 欠定线性方程组的最小范数解：x = Aᵀ(AAᵀ)⁻¹b
     *
     * <p>其中 A 的第一行恒为 1（质量守恒），第二行为 a2_i。矩阵 AAᵀ 为 2×2：
     * [[n, Σa2],[Σa2, Σa2²]]；当其行列式接近 0（物料成分高度雷同）时退化为均匀分配，
     * 并记录说明。</p>
     *
     * @param active 待求物料的下标集合
     * @param a2 约束(2)系数数组
     * @param b1 质量守恒方程右端
     * @param b2 渣型约束方程右端
     * @param notes 求解过程记录（会被追加）
     * @return 待求物料的解（未参与求解的下标对应值为 0）
     */
    private static double[] solveMinNorm(List<Integer> active, double[] a2, double b1, double b2, List<String> notes)
    {
        int n = active.size();
        double[] x = new double[a2.length];
        double s1 = 0;
        double s2 = 0;
        for (int i = 0; i < a2.length; i++)
        {
            x[i] = 0;
        }
        for (int idx : active)
        {
            s1 += a2[idx];
            s2 += a2[idx] * a2[idx];
        }
        if (n == 1)
        {
            // 自由度为 1：优先满足质量守恒，渣型约束只能作为偏差报告
            int idx = active.get(0);
            x[idx] = b1;
            notes.add("待求物料仅 1 种，自由度不足，按质量守恒定解，硅铁比偏差将单独报告");
            return x;
        }
        double det = n * s2 - s1 * s1;
        if (Math.abs(det) < 1e-14)
        {
            // 矩阵退化（各物料成分雷同）：均匀分配
            double each = b1 / n;
            for (int idx : active)
            {
                x[idx] = each;
            }
            notes.add("约束矩阵接近奇异（待求物料成分高度雷同），按等配比方案给出可行解，硅铁比偏差将单独报告");
            return x;
        }
        double y1 = (s2 * b1 - s1 * b2) / det;
        double y2 = (-s1 * b1 + n * b2) / det;
        for (int idx : active)
        {
            x[idx] = y1 + y2 * a2[idx];
        }
        notes.add("最小范数解：以 " + n + " 个待求物料求解 2 条约束，取模最小解（各物料配比尽量均衡）");
        return x;
    }

    /**
     * BigDecimal 统一保留小数位（四舍五入）
     */
    private static BigDecimal scale(BigDecimal value, int scale)
    {
        return value.setScale(scale, RoundingMode.HALF_UP);
    }
}
