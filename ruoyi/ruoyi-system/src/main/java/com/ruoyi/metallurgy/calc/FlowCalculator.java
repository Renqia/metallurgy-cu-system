package com.ruoyi.metallurgy.calc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.metallurgy.dto.BalanceResult;
import com.ruoyi.metallurgy.dto.CalcOptions;
import com.ruoyi.metallurgy.dto.NodeBalance;
import com.ruoyi.metallurgy.dto.RatioMaterialItem;
import com.ruoyi.metallurgy.dto.RatioResult;
import com.ruoyi.metallurgy.dto.StreamItem;

/**
 * 铜闪速冶炼全流程物料平衡计算器（纯算法类，不依赖 Spring 容器，便于单元测试）
 *
 * <h3>计算基准</h3>
 * 以 <b>100 kg 干燥混合料</b> 为基准，各节点按工艺顺序推进，节点内做元素守恒。
 *
 * <h3>模型假设（均写入说明书"理论依据"，并随结果下发 assumptions）</h3>
 * <ol>
 *   <li>A1 冰铜由 Cu2S 与 FeS 两相组成，忽略 Ni/Pb/Zn/As 等微量组分与溶解氧；</li>
 *   <li>A2 烟尘成分与混合料相同（简化处理，实际烟尘会富集易挥发组分），烟尘率默认 2%；</li>
 *   <li>A3 冰铜中不溶解 SiO2、CaO，二者全部进入炉渣；</li>
 *   <li>A4 炉渣含铜默认 0.8%（示例取值），与渣量互为因果，采用不动点迭代求解；</li>
 *   <li>A5 转炉吹炼中冰铜的 Fe 全部氧化造渣，粗铜品位默认 98.5%，杂质简化按硫计，
 *       并根据渣型要求加入石英石；</li>
 *   <li>A6 阳极精炼脱硫完全（阳极铜含硫忽略不计），阳极铜品位默认 99.5%，
 *       精炼渣含铜默认 25%；</li>
 *   <li>A7 电解精炼铜回收率默认 99.5%，阴极铜品位 99.99%，阳极泥率 0.8%；</li>
 *   <li>A8 硫以 SO2 形态进入烟气，SO2 中的氧来自鼓风，不计入输入质量，
 *       故"总质量"口径不守恒属正常现象，而 Cu/Fe/S/SiO2/CaO 五元素口径严格守恒。</li>
 * </ol>
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
public final class FlowCalculator
{
    /** 参与平衡校核的元素/化合物 */
    public static final String[] ELEMENTS = { "Cu", "Fe", "S", "SiO2", "CaO" };

    /** 迭代收敛容差(kg) */
    private static final double CONV = 1e-10;

    /** 最大迭代次数 */
    private static final int MAX_ITER = 200;

    private FlowCalculator()
    {
    }

    /**
     * 执行全流程物料平衡计算
     *
     * @param ratio 配料计算结果（提供配比与混合料成分）
     * @param options 模型可选参数（为空则取默认示例值）
     * @return 全流程平衡结果
     */
    public static BalanceResult calculate(RatioResult ratio, CalcOptions options)
    {
        if (ratio == null || ratio.getItems() == null || ratio.getItems().isEmpty())
        {
            throw new ServiceException("配料结果为空，无法进行全流程计算");
        }
        CalcOptions opt = options == null ? new CalcOptions() : options;
        BalanceResult result = new BalanceResult();
        result.setBasis("以 100 kg 干燥混合料为计算基准");
        result.setAssumptions(buildAssumptions(opt));

        List<String> warnings = new ArrayList<>();
        double basis = MetallurgyConstants.BASIS_MASS.doubleValue();

        // ---------- 节点1：配料 ----------
        NodeBalance prep = new NodeBalance();
        prep.setNodeName("配料");
        double[] mixElem = new double[ELEMENTS.length];
        for (RatioMaterialItem it : ratio.getItems())
        {
            double mass = it.getRatio().doubleValue();
            double[] pct = { it.getCuContent().doubleValue(), it.getFeContent().doubleValue(),
                    it.getSulfurContent().doubleValue(), it.getSio2Content().doubleValue(),
                    it.getCaoContent().doubleValue() };
            // 每一股物料登记为输入流；100 kg 基准下配比数值即物料质量，故按配比累加各元素质量
            prep.getInputs().add(StreamItem.of(it.getMaterialName(), "input", bd(mass),
                    bd(pct[0]), bd(pct[1]), bd(pct[2]), bd(pct[3]), bd(pct[4])));
            for (int i = 0; i < pct.length; i++)
            {
                mixElem[i] += mass * pct[i] / 100.0;
            }
        }
        StreamItem mixed = new StreamItem("混合料", "output");
        mixed.setTotalMass(bd(basis));
        mixed.setCu(bd(mixElem[0]));
        mixed.setFe(bd(mixElem[1]));
        mixed.setS(bd(mixElem[2]));
        mixed.setSio2(bd(mixElem[3]));
        mixed.setCao(bd(mixElem[4]));
        prep.getOutputs().add(mixed);
        prep.buildElementBalances(ELEMENTS);
        prep.addIndicator("混合料Cu品位", bd(pct(mixElem[0], basis)), "%");
        prep.addIndicator("配比合计", ratio.getRatioSum(), "%");
        prep.addIndicator("混合料综合成本", ratio.getUnitCost(), "元/吨");
        result.getNodes().add(prep);
        result.setMixedMaterial(mixed);

        // ---------- 节点2：闪速熔炼 ----------
        NodeBalance smelting = buildSmeltingNode(ratio, mixed, mixElem, opt, warnings);
        result.getNodes().add(smelting);
        StreamItem matte = findOutput(smelting, "冰铜");

        // ---------- 节点3：转炉吹炼 ----------
        NodeBalance converting = buildConvertingNode(matte, opt, warnings);
        result.getNodes().add(converting);
        StreamItem blister = findOutput(converting, "粗铜");

        // ---------- 节点4：阳极精炼 ----------
        NodeBalance refining = buildRefiningNode(blister, opt, warnings);
        result.getNodes().add(refining);
        StreamItem anode = findOutput(refining, "阳极铜");

        // ---------- 节点5：电解精炼 ----------
        NodeBalance electro = buildElectroNode(anode, opt, warnings);
        result.getNodes().add(electro);
        StreamItem cathode = findOutput(electro, "阴极铜");

        // ---------- 全流程指标 ----------
        double cuMix = mixed.getCu().doubleValue();
        double cuMatte = matte.getCu().doubleValue();
        double cuCathode = cathode.getCu().doubleValue();
        double slagMass = findOutput(smelting, "炉渣").getTotalMass().doubleValue();
        double so2Total = findOutput(smelting, "烟气").getTotalMass().doubleValue()
                + findOutput(converting, "烟气").getTotalMass().doubleValue()
                + findOutput(refining, "精炼烟气").getTotalMass().doubleValue();
        double cathodeMass = cathode.getTotalMass().doubleValue();

        // 全流程汇总指标：直收率反映熔炼环节的铜回收效率，总回收率反映累计到阴极铜的回收效率
        result.addIndicator("闪速熔炼铜直收率", bd(cuMix > 0 ? cuMatte / cuMix * 100 : 0), "%");
        result.addIndicator("全流程铜总回收率", bd(cuMix > 0 ? cuCathode / cuMix * 100 : 0), "%");
        result.addIndicator("闪速熔炼渣率", bd(slagMass / basis * 100), "%");
        result.addIndicator("每100kg混合料产阴极铜", bd(cathodeMass), "kg");
        result.addIndicator("单位阴极铜混合料耗", bd(cathodeMass > 0 ? basis / cathodeMass : 0), "kg混合料/kg阴极铜");
        result.addIndicator("SO2 总产量", bd(so2Total), "kg/100kg混合料");
        result.setWarnings(warnings);
        result.setMaxResidual(maxResidual(result.getNodes()));
        result.setFeasible(result.getMaxResidual().compareTo(MetallurgyConstants.RESIDUAL_TOLERANCE) <= 0);
        if (!result.isFeasible())
        {
            warnings.add("存在元素平衡残差超过 " + MetallurgyConstants.RESIDUAL_TOLERANCE.toPlainString()
                    + "% 的节点，请检查输入数据与模型参数");
        }
        return result;
    }

    /**
     * 节点2：闪速熔炼
     *
     * <p>混合料与富氧空气在反应塔内完成氧化熔炼：Cu 富集为冰铜（Cu2S-FeS 系），
     * Fe 部分进入冰铜、其余与 SiO2、CaO 造渣，S 部分进入冰铜、其余以 SO2 进入烟气。
     * 渣中含铜按设定值回收，与渣量互为因果，采用不动点迭代求解。</p>
     */
    private static NodeBalance buildSmeltingNode(RatioResult ratio, StreamItem mixed, double[] mixElem,
            CalcOptions opt, List<String> warnings)
    {
        NodeBalance node = new NodeBalance();
        node.setNodeName("闪速熔炼");
        node.getInputs().add(mixed);

        double dustRate = opt.getDustRate().doubleValue() / 100.0;
        double grade = ratio.getMatteGradeTarget().doubleValue();
        double slagCuPct = opt.getSlagCu().doubleValue() / 100.0;

        // 烟尘（成分同混合料，简化处理 A2）
        double dustMass = mixed.getTotalMass().doubleValue() * dustRate;
        double[] dustPct = { pct(mixElem[0], mixed.getTotalMass().doubleValue()),
                pct(mixElem[1], mixed.getTotalMass().doubleValue()),
                pct(mixElem[2], mixed.getTotalMass().doubleValue()),
                pct(mixElem[3], mixed.getTotalMass().doubleValue()),
                pct(mixElem[4], mixed.getTotalMass().doubleValue()) };
        StreamItem dust = StreamItem.of("烟尘", "output", bd(dustMass), bd(dustPct[0]), bd(dustPct[1]),
                bd(dustPct[2]), bd(dustPct[3]), bd(dustPct[4]));

        // 扣除烟尘后参与熔炼的元素量
        double cuEff = mixElem[0] - dust.getCu().doubleValue();
        double feEff = mixElem[1] - dust.getFe().doubleValue();
        double sEff = mixElem[2] - dust.getS().doubleValue();
        double sio2Eff = mixElem[3] - dust.getSio2().doubleValue();
        double caoEff = mixElem[4] - dust.getCao().doubleValue();

        // 不动点迭代：渣含铜取决于渣量，而渣量又包含渣中的铜，二者互为因果，故迭代至收敛
        double cuSlag = 0;
        double cuMatte = 0;
        double feMatte = 0;
        double sMatte = 0;
        double matteMass = 0;
        double feSlag = 0;
        double slagMass = 0;
        for (int iter = 0; iter < MAX_ITER; iter++)
        {
            cuMatte = cuEff - cuSlag;
            double matteIdeal = cuMatte * 100.0 / grade;
            double cu2s = MetallurgyConstants.CU2S_PER_CU.doubleValue() * cuMatte;
            double fes = matteIdeal - cu2s;
            if (fes < 0)
            {
                fes = 0;
                warnings.add("冰铜品位 " + grade + "% 高于物相模型上限，已按纯 Cu2S 冰铜处理");
            }
            matteMass = cu2s + fes;
            feMatte = MetallurgyConstants.FE_IN_FES.doubleValue() * fes;
            sMatte = MetallurgyConstants.S_IN_CU2S.doubleValue() * cu2s
                    + MetallurgyConstants.S_IN_FES.doubleValue() * fes;
            feSlag = feEff - feMatte;
            double sio2Slag = sio2Eff;
            double caoSlag = caoEff;
            slagMass = feSlag / MetallurgyConstants.FE_IN_FEO.doubleValue() + sio2Slag + caoSlag + cuSlag;
            double cuSlagNew = slagMass * slagCuPct;
            if (Math.abs(cuSlagNew - cuSlag) < CONV)
            {
                cuSlag = cuSlagNew;
                break;
            }
            cuSlag = cuSlagNew;
        }
        cuMatte = cuEff - cuSlag;
        if (sMatte > sEff)
        {
            throw new ServiceException("闪速熔炼硫量不足：按目标冰铜品位 " + grade + "% 形成的冰铜需要硫 "
                    + bd(sMatte).toPlainString() + " kg，而入炉有效硫仅 " + bd(sEff).toPlainString()
                    + " kg，无法形成该品位的冰铜。请降低目标冰铜品位，或提高硫化矿（铜精矿）配比、减少熔剂比例");
        }
        double sGas = sEff - sMatte;
        double so2Mass = sGas / MetallurgyConstants.S_IN_SO2.doubleValue();

        StreamItem matte = new StreamItem("冰铜", "output");
        matte.setTotalMass(bd(matteMass));
        matte.setCu(bd(cuMatte));
        matte.setFe(bd(feMatte));
        matte.setS(bd(sMatte));

        StreamItem slag = new StreamItem("炉渣", "output");
        slag.setTotalMass(bd(slagMass));
        slag.setCu(bd(cuSlag));
        slag.setSio2(bd(sio2Eff));
        slag.setCao(bd(caoEff));
        slag.setFe(bd(feSlag));

        StreamItem gas = new StreamItem("烟气", "output");
        gas.setTotalMass(bd(so2Mass));
        gas.setS(bd(sGas));

        node.getOutputs().add(matte);
        node.getOutputs().add(slag);
        node.getOutputs().add(dust);
        node.getOutputs().add(gas);
        node.buildElementBalances(ELEMENTS);

        node.addIndicator("冰铜品位", bd(matteMass > 0 ? cuMatte / matteMass * 100 : 0), "%");
        node.addIndicator("冰铜量", bd(matteMass), "kg");
        node.addIndicator("炉渣量", bd(slagMass), "kg");
        node.addIndicator("炉渣硅铁比", bd(sio2Eff > 0 ? feSlag / sio2Eff : 0), "");
        node.addIndicator("渣含铜", bd(slagMass > 0 ? cuSlag / slagMass * 100 : 0), "%");
        node.addIndicator("烟尘量", bd(dustMass), "kg");
        node.addIndicator("SO2 量", bd(so2Mass), "kg");
        node.addIndicator("铜直收率", bd(mixElem[0] > 0 ? cuMatte / mixElem[0] * 100 : 0), "%");
        return node;
    }

    /**
     * 节点3：转炉吹炼
     *
     * <p>冰铜中的 FeS 被氧化，Fe 造渣、S 以 SO2 入烟气；Cu2S 被氧化为金属铜。
     * 按设定的渣型（Fe/SiO2）加入石英石熔剂。</p>
     */
    private static NodeBalance buildConvertingNode(StreamItem matte, CalcOptions opt, List<String> warnings)
    {
        NodeBalance node = new NodeBalance();
        node.setNodeName("转炉吹炼");
        node.getInputs().add(matte);

        double cuInMatte = matte.getCu().doubleValue();
        double feInMatte = matte.getFe().doubleValue();
        double sInMatte = matte.getS().doubleValue();
        double blisterGrade = opt.getBlisterGrade().doubleValue() / 100.0;
        double slagCuPct = opt.getBlisterSlagCu().doubleValue() / 100.0;
        double targetFeSio2 = opt.getBlisterFeSio2().doubleValue();

        if (cuInMatte <= 0)
        {
            throw new ServiceException("冰铜中铜量为 0，无法进行转炉吹炼计算，请检查冰铜品位与配料");
        }

        double cuSlag = 0;
        double cuCopper = 0;
        double copperMass = 0;
        double sInCopper = 0;
        double sGas = 0;
        double quartzMass = 0;
        double feFromQuartz = 0;
        double caoFromQuartz = 0;
        double sio2Slag = 0;
        double feSlag = 0;
        double slagMass = 0;
        // 与闪速熔炼同理：吹炼渣中的铜损失与渣量互为因果，采用不动点迭代求解
        for (int iter = 0; iter < MAX_ITER; iter++)
        {
            cuCopper = cuInMatte - cuSlag;
            copperMass = cuCopper / blisterGrade;
            sInCopper = copperMass * (1 - blisterGrade);
            if (sInCopper > sInMatte)
            {
                // 硫不足以支撑设定的粗铜杂质含量时，按入炉硫上限取值，保证硫元素守恒
                sInCopper = sInMatte;
            }
            sGas = sInMatte - sInCopper;
            sio2Slag = targetFeSio2 > 0 ? feInMatte / targetFeSio2 : 0;
            quartzMass = sio2Slag / (MetallurgyConstants.QUARTZ_SIO2.doubleValue() / 100.0);
            feFromQuartz = quartzMass * MetallurgyConstants.QUARTZ_FE.doubleValue() / 100.0;
            caoFromQuartz = quartzMass * MetallurgyConstants.QUARTZ_CAO.doubleValue() / 100.0;
            feSlag = feInMatte + feFromQuartz;
            slagMass = feSlag / MetallurgyConstants.FE_IN_FEO.doubleValue() + sio2Slag + caoFromQuartz + cuSlag;
            double cuSlagNew = slagMass * slagCuPct;
            if (Math.abs(cuSlagNew - cuSlag) < CONV)
            {
                cuSlag = cuSlagNew;
                break;
            }
            cuSlag = cuSlagNew;
        }
        if (sInMatte > 0 && sInCopper >= sInMatte)
        {
            warnings.add("转炉吹炼入炉硫不足：粗铜含硫被限制为冰铜带入的全部硫量（"
                    + bd(sInMatte).toPlainString() + " kg），粗铜实际品位高于设定值");
        }
        double so2Mass = sGas / MetallurgyConstants.S_IN_SO2.doubleValue();

        StreamItem quartz = StreamItem.of("石英石熔剂", "input", bd(quartzMass), BigDecimal.ZERO,
                MetallurgyConstants.QUARTZ_FE, BigDecimal.ZERO, MetallurgyConstants.QUARTZ_SIO2,
                MetallurgyConstants.QUARTZ_CAO);
        node.getInputs().add(quartz);

        StreamItem copper = new StreamItem("粗铜", "output");
        copper.setTotalMass(bd(copperMass));
        copper.setCu(bd(cuCopper));
        copper.setS(bd(sInCopper));

        StreamItem slag = new StreamItem("吹炼渣", "output");
        slag.setTotalMass(bd(slagMass));
        slag.setCu(bd(cuSlag));
        slag.setFe(bd(feSlag));
        slag.setSio2(bd(sio2Slag));
        slag.setCao(bd(caoFromQuartz));

        StreamItem gas = new StreamItem("烟气", "output");
        gas.setTotalMass(bd(so2Mass));
        gas.setS(bd(sGas));

        node.getOutputs().add(copper);
        node.getOutputs().add(slag);
        node.getOutputs().add(gas);
        node.buildElementBalances(ELEMENTS);

        node.addIndicator("粗铜量", bd(copperMass), "kg");
        node.addIndicator("粗铜品位", bd(copperMass > 0 ? cuCopper / copperMass * 100 : 0), "%");
        node.addIndicator("吹炼渣量", bd(slagMass), "kg");
        node.addIndicator("吹炼渣含铜", bd(slagMass > 0 ? cuSlag / slagMass * 100 : 0), "%");
        node.addIndicator("石英石加入量", bd(quartzMass), "kg");
        node.addIndicator("SO2 量", bd(so2Mass), "kg");
        node.addIndicator("吹炼铜回收率", bd(cuInMatte > 0 ? cuCopper / cuInMatte * 100 : 0), "%");
        return node;
    }

    /**
     * 节点4：阳极精炼（火法精炼）
     *
     * <p>粗铜经氧化—还原脱硫、脱氧后得到阳极铜。假设脱硫完全（A6），硫全部进入精炼烟气。</p>
     */
    private static NodeBalance buildRefiningNode(StreamItem blister, CalcOptions opt, List<String> warnings)
    {
        NodeBalance node = new NodeBalance();
        node.setNodeName("阳极精炼");
        node.getInputs().add(blister);

        double cuIn = blister.getCu().doubleValue();
        double sIn = blister.getS().doubleValue();
        double anodeGrade = opt.getAnodeGrade().doubleValue() / 100.0;
        double refineSlagCu = opt.getRefineSlagCu().doubleValue() / 100.0;
        double refineYield = opt.getRefineYield().doubleValue() / 100.0;

        // 铜分配：回收率决定进阳极铜的铜量，精炼渣含铜决定渣量（两者为独立参数）
        double cuAnode = cuIn * refineYield;
        double cuSlag = cuIn - cuAnode;
        double anodeMass = cuAnode / anodeGrade;
        double slagMass = refineSlagCu > 0 ? cuSlag / refineSlagCu : 0;
        // 精炼烟气：硫全部脱除
        double sGas = sIn;
        double so2Mass = sGas / MetallurgyConstants.S_IN_SO2.doubleValue();

        StreamItem anode = new StreamItem("阳极铜", "output");
        anode.setTotalMass(bd(anodeMass));
        anode.setCu(bd(cuAnode));

        StreamItem slag = new StreamItem("精炼渣", "output");
        slag.setTotalMass(bd(slagMass));
        slag.setCu(bd(cuSlag));

        StreamItem gas = new StreamItem("精炼烟气", "output");
        gas.setTotalMass(bd(so2Mass));
        gas.setS(bd(sGas));

        node.getOutputs().add(anode);
        node.getOutputs().add(slag);
        node.getOutputs().add(gas);
        node.buildElementBalances(ELEMENTS);

        node.addIndicator("阳极铜量", bd(anodeMass), "kg");
        node.addIndicator("阳极铜品位", bd(anodeMass > 0 ? cuAnode / anodeMass * 100 : 0), "%");
        node.addIndicator("精炼渣量", bd(slagMass), "kg");
        node.addIndicator("精炼铜回收率", bd(cuIn > 0 ? cuAnode / cuIn * 100 : 0), "%");
        node.addIndicator("SO2 量", bd(so2Mass), "kg");
        return node;
    }

    /**
     * 节点5：电解精炼
     *
     * <p>阳极铜电解得到阴极铜与阳极泥，铜回收率与阳极泥率按模型参数取值。</p>
     */
    private static NodeBalance buildElectroNode(StreamItem anode, CalcOptions opt, List<String> warnings)
    {
        NodeBalance node = new NodeBalance();
        node.setNodeName("电解精炼");
        node.getInputs().add(anode);

        double cuIn = anode.getCu().doubleValue();
        double anodeMass = anode.getTotalMass().doubleValue();
        double yield = opt.getElectroYield().doubleValue() / 100.0;
        double cathodeGrade = opt.getCathodeGrade().doubleValue() / 100.0;
        double slimeRate = opt.getSlimeRate().doubleValue() / 100.0;

        double cuCathode = cuIn * yield;
        double cathodeMass = cuCathode / cathodeGrade;
        double cuLoss = cuIn - cuCathode;
        double slimeMass = anodeMass * slimeRate;

        StreamItem cathode = new StreamItem("阴极铜", "output");
        cathode.setTotalMass(bd(cathodeMass));
        cathode.setCu(bd(cuCathode));

        StreamItem slime = new StreamItem("阳极泥", "output");
        slime.setTotalMass(bd(slimeMass));
        slime.setCu(bd(cuLoss));

        node.getOutputs().add(cathode);
        node.getOutputs().add(slime);
        node.buildElementBalances(ELEMENTS);

        node.addIndicator("阴极铜量", bd(cathodeMass), "kg");
        node.addIndicator("阴极铜品位", bd(cathodeMass > 0 ? cuCathode / cathodeMass * 100 : 0), "%");
        node.addIndicator("阳极泥量", bd(slimeMass), "kg");
        node.addIndicator("电解铜回收率", bd(cuIn > 0 ? cuCathode / cuIn * 100 : 0), "%");
        return node;
    }

    /**
     * 从节点的输出流中按名称取流量（找不到时抛出业务异常，避免空指针导致 500）
     *
     * @param node 节点
     * @param name 流量名称
     * @return 流条目
     */
    private static StreamItem findOutput(NodeBalance node, String name)
    {
        for (StreamItem item : node.getOutputs())
        {
            if (name.equals(item.getName()))
            {
                return item;
            }
        }
        throw new ServiceException("计算结果缺少节点【" + node.getNodeName() + "】的输出流：" + name);
    }

    /**
     * 汇总所有节点元素平衡的最大残差绝对值
     */
    private static BigDecimal maxResidual(List<NodeBalance> nodes)
    {
        double max = 0;
        for (NodeBalance node : nodes)
        {
            for (com.ruoyi.metallurgy.dto.ElementBalance eb : node.getElementBalances())
            {
                double v = Math.abs(eb.getResidual().doubleValue());
                if (v > max)
                {
                    max = v;
                }
            }
        }
        return BigDecimal.valueOf(max).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 生成模型假设说明
     */
    private static List<String> buildAssumptions(CalcOptions opt)
    {
        List<String> list = new ArrayList<>();
        list.add("计算基准：100 kg 干燥混合料");
        list.add("冰铜按 Cu2S-FeS 两相模型处理（Cu2S 含 Cu 79.86%、FeS 含 Fe 63.54%）");
        list.add("烟尘率取 " + opt.getDustRate().toPlainString() + "%，烟尘成分与混合料相同（简化处理）");
        list.add("炉渣含铜取 " + opt.getSlagCu().toPlainString() + "%，与渣量采用不动点迭代求解");
        list.add("转炉吹炼：粗铜品位 " + opt.getBlisterGrade().toPlainString()
                + "%，吹炼渣含铜 " + opt.getBlisterSlagCu().toPlainString()
                + "%，吹炼渣 Fe/SiO2 = " + opt.getBlisterFeSio2().toPlainString() + "（据此外加石英石）");
        list.add("阳极精炼：铜回收率 " + opt.getRefineYield().toPlainString()
                + "%，阳极铜品位 " + opt.getAnodeGrade().toPlainString()
                + "%，精炼渣含铜 " + opt.getRefineSlagCu().toPlainString() + "%，假设脱硫完全");
        list.add("电解精炼：铜回收率 " + opt.getElectroYield().toPlainString()
                + "%，阴极铜品位 " + opt.getCathodeGrade().toPlainString()
                + "%，阳极泥率 " + opt.getSlimeRate().toPlainString() + "%");
        list.add("硫以 SO2 形态入烟气，SO2 中的氧来自鼓风、不计入输入质量；"
                + "因此总质量口径不守恒属正常，Cu/Fe/S/SiO2/CaO 五元素口径严格守恒");
        list.add("本模型为基于元素守恒的教学级简化模型，不等同于商业流程模拟软件，也不替代现场标定");
        return list;
    }

    /**
     * 计算成分百分比：元素质量 / 总质量 × 100
     */
    private static double pct(double elementMass, double totalMass)
    {
        return totalMass > 0 ? elementMass / totalMass * 100.0 : 0;
    }

    /**
     * double → BigDecimal（保留 4 位小数，四舍五入）
     */
    private static BigDecimal bd(double value)
    {
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP);
    }
}
