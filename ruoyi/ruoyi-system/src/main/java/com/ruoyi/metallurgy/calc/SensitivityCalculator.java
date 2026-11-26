package com.ruoyi.metallurgy.calc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.metallurgy.dto.BalanceResult;
import com.ruoyi.metallurgy.dto.CalcOptions;
import com.ruoyi.metallurgy.dto.RatioRequest;
import com.ruoyi.metallurgy.dto.RatioResult;
import com.ruoyi.metallurgy.dto.SensitivityPoint;
import com.ruoyi.metallurgy.dto.SensitivityResult;

/**
 * 敏感性分析计算器（纯算法类，不依赖 Spring 容器，便于单元测试）
 *
 * <h3>计算方式</h3>
 * <p>以一条已有全流程计算记录的输入快照为基线，对指定字段在给定区间内<b>逐点独立重算</b>：
 * 每个扫描点都从基线<b>深拷贝</b>出一份全新的请求（不与其它点共享任何可变状态），
 * 改写该字段后依次执行 {@link RatioSolver#solve} 与 {@link FlowCalculator#calculate}。
 * 因此扫描值是<b>真实进入计算模型</b>的，不是只改显示值拼出的"曲线"。</p>
 *
 * <h3>可扫描字段（严格受模型能力限制）</h3>
 * <ul>
 *   <li><b>目标冰铜品位</b>（<code>matteGrade</code>）：改变闪速熔炼节点按目标品位推得的冰铜量与渣量，
 *       是熔炼环节最敏感的操作参数；</li>
 *   <li><b>转炉吹炼渣硅铁比</b>（<code>blisterFeSio2</code>）：直接决定转炉外加石英石量与吹炼渣量。</li>
 * </ul>
 * <p><b>富氧浓度、氧料比不参与元素平衡计算，不属于可扫描字段</b>——页面与文档均须如实说明。</p>
 *
 * <h3>失败点的处理</h3>
 * <p>单点重算抛出业务异常时（如目标品位超出 Cu2S 物相上限、硫量不足），<b>保留该点</b>：
 * 扫描值照常返回、<code>success=false</code> 并附中文原因，指标序列对应位置为 null，
 * 前端在折线图上断开而不补 0，避免把"工艺不可行"画成"指标下降"。</p>
 *
 * @author 【成员1】
 * @date 2026-10-14
 */
public final class SensitivityCalculator
{
    /** 扫描字段：目标冰铜品位(%) */
    public static final String FIELD_MATTE_GRADE = "matteGrade";

    /** 扫描字段：转炉吹炼渣硅铁比 Fe/SiO2 */
    public static final String FIELD_BLISTER_FE_SIO2 = "blisterFeSio2";

    /** 单次扫描的最大点数（防止误操作导致长时间计算） */
    public static final int MAX_POINTS = 50;

    /** 扫描值保留的最大小数位 */
    private static final int MAX_SCALE = 6;

    private SensitivityCalculator()
    {
    }

    /**
     * 字段显示名
     *
     * @param field 字段名
     * @return 中文显示名
     */
    public static String labelOf(String field)
    {
        if (FIELD_MATTE_GRADE.equals(field))
        {
            return "目标冰铜品位";
        }
        if (FIELD_BLISTER_FE_SIO2.equals(field))
        {
            return "转炉吹炼渣硅铁比";
        }
        return field;
    }

    /**
     * 字段单位（转炉吹炼渣硅铁比为无量纲）
     *
     * @param field 字段名
     * @return 单位说明
     */
    public static String unitOf(String field)
    {
        return FIELD_MATTE_GRADE.equals(field) ? "%" : "";
    }

    /**
     * 逐点扫描重算
     *
     * @param base 基线请求（来自计算记录的输入快照，调用方不得复用其可变状态）
     * @param field 扫描字段（matteGrade / blisterFeSio2）
     * @param start 扫描起点
     * @param end 扫描终点（含）
     * @param step 扫描步长（&gt;0）
     * @return 扫描结果（含失败点）
     */
    public static SensitivityResult scan(RatioRequest base, String field,
            BigDecimal start, BigDecimal end, BigDecimal step)
    {
        if (base == null || base.getItems() == null || base.getItems().isEmpty())
        {
            throw new ServiceException("该记录的输入快照不完整，无法作为敏感性分析的基线");
        }
        String scanField = field == null ? "" : field.trim();
        if (!FIELD_MATTE_GRADE.equals(scanField) && !FIELD_BLISTER_FE_SIO2.equals(scanField))
        {
            throw new ServiceException("不支持的扫描字段：" + scanField
                    + "（当前仅支持：目标冰铜品位、转炉吹炼渣硅铁比）");
        }
        if (start == null || end == null || step == null)
        {
            throw new ServiceException("请完整填写扫描起点、终点与步长");
        }
        if (step.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("扫描步长必须大于 0");
        }
        if (start.compareTo(end) > 0)
        {
            throw new ServiceException("扫描起点必须不大于终点");
        }
        if (FIELD_MATTE_GRADE.equals(scanField))
        {
            BigDecimal upper = MetallurgyConstants.CU_IN_CU2S.multiply(new BigDecimal("100"));
            if (end.compareTo(BigDecimal.ZERO) <= 0 || start.compareTo(upper) >= 0)
            {
                throw new ServiceException("目标冰铜品位的扫描区间必须落在 0 ~ " + upper.setScale(2, RoundingMode.HALF_UP)
                        + "% 之间（纯 Cu2S 的理论含铜量上限）");
            }
        }
        else if (end.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("转炉吹炼渣硅铁比的扫描区间必须大于 0");
        }

        int scale = displayScale(start, end, step);
        int pointCount = pointCount(start, end, step);
        if (pointCount > MAX_POINTS)
        {
            throw new ServiceException("扫描点数不得多于 " + MAX_POINTS + " 个，当前 " + pointCount
                    + " 个，请增大步长或缩小扫描区间");
        }

        SensitivityResult result = new SensitivityResult();
        result.setField(scanField);
        result.setFieldLabel(labelOf(scanField));
        result.setFieldUnit(unitOf(scanField));
        result.setBaseValue(baseValueOf(base, scanField));

        List<BalanceResult> scanResults = new ArrayList<>();
        int success = 0;
        int failed = 0;
        for (int i = 0; i < pointCount; i++)
        {
            BigDecimal value = start.add(step.multiply(BigDecimal.valueOf(i))).setScale(scale, RoundingMode.HALF_UP);
            SensitivityPoint point = new SensitivityPoint();
            point.setValue(value);
            try
            {
                BalanceResult balance = recalculate(base, scanField, value);
                scanResults.add(balance);
                success++;
            }
            catch (RuntimeException e)
            {
                // 单点工艺不可行：保留该点、记录原因，不中断整体扫描
                scanResults.add(null);
                point.setSuccess(false);
                point.setMessage(e.getMessage() == null ? "该扫描点计算失败（原因未知）" : e.getMessage());
                failed++;
            }
            result.getPoints().add(point);
        }

        result.setIndicators(IndicatorCatalog.extract(scanResults));
        result.setSuccessCount(success);
        result.setFailedCount(failed);
        result.getNotes().add("共扫描 " + pointCount + " 个点：成功 " + success + " 个，工艺不可行 " + failed
                + " 个；不可行点的指标为空值，折线在该处断开（不补 0）");
        result.getNotes().add("扫描为实时重算，不写入数据库；每个扫描点均从基线独立复制后重算，互不影响");
        result.getNotes().add("富氧浓度与氧料比不参与元素平衡计算，因此不作为扫描字段");
        if (FIELD_BLISTER_FE_SIO2.equals(scanField))
        {
            result.getNotes().add("本次扫描的是转炉吹炼渣硅铁比（作用于转炉造渣石英石加入量），"
                    + "与闪速熔炼目标硅铁比不是同一个参数");
            if (base.getOptions() == null)
            {
                result.getNotes().add("基线记录的输入快照未保存模型参数（CalcOptions），"
                        + "其中转炉吹炼渣硅铁比按模型默认值 "
                        + MetallurgyConstants.DEFAULT_BLISTER_FE_SIO2.toPlainString() + " 取值");
            }
        }
        if (base.getOptions() == null)
        {
            result.getNotes().add("基线记录未保存模型参数（CalcOptions），本次扫描除被扫描字段外的其它模型参数"
                    + "按《MetallurgyConstants》默认示例值参与计算");
        }
        return result;
    }

    /**
     * 单点重算：深拷贝基线 → 改写被扫描字段 → 配料求解 → 全流程平衡
     */
    private static BalanceResult recalculate(RatioRequest base, String field, BigDecimal value)
    {
        RatioRequest request = deepCopy(base);
        request.setSaveRecord(false);
        if (FIELD_MATTE_GRADE.equals(field))
        {
            request.setMatteGrade(value);
        }
        else
        {
            CalcOptions options = request.getOptions() == null ? new CalcOptions() : request.getOptions();
            options.setBlisterFeSio2(value);
            request.setOptions(options);
        }
        // 与全流程计算保存前的处理保持一致：手工给足 100% 的配比按锁定处理，不再重新分配配比
        RatioRequestUtils.applyLockedRatioIfComplete(request);
        RatioResult ratio = RatioSolver.solve(request);
        return FlowCalculator.calculate(ratio, request.getOptions());
    }

    /**
     * 通过 JSON 序列化做深拷贝：保证各扫描点不共享 list / 对象引用
     */
    private static RatioRequest deepCopy(RatioRequest base)
    {
        RatioRequest copy = JSON.parseObject(JSON.toJSONString(base), RatioRequest.class);
        if (copy == null)
        {
            throw new ServiceException("该记录的输入快照无法解析，无法作为敏感性分析的基线");
        }
        return copy;
    }

    /**
     * 基线中该字段的当前取值
     */
    private static BigDecimal baseValueOf(RatioRequest base, String field)
    {
        if (FIELD_MATTE_GRADE.equals(field))
        {
            return base.getMatteGrade();
        }
        if (base.getOptions() == null)
        {
            return MetallurgyConstants.DEFAULT_BLISTER_FE_SIO2;
        }
        return base.getOptions().getBlisterFeSio2();
    }

    /**
     * 扫描点数 = floor((end − start) / step) + 1
     */
    private static int pointCount(BigDecimal start, BigDecimal end, BigDecimal step)
    {
        BigDecimal span = end.subtract(start);
        BigDecimal n = span.divide(step, 0, RoundingMode.DOWN);
        BigDecimal last = start.add(step.multiply(n));
        // 步长不能整除时，末点仍需落在区间内（含端点等于终点的情况）
        while (last.compareTo(end) > 0 && n.compareTo(BigDecimal.ZERO) > 0)
        {
            n = n.subtract(BigDecimal.ONE);
            last = start.add(step.multiply(n));
        }
        return n.intValue() + 1;
    }

    /**
     * 扫描值展示小数位：取起点/终点/步长中的最大标度（上限 6 位）
     */
    private static int displayScale(BigDecimal start, BigDecimal end, BigDecimal step)
    {
        int scale = Math.max(Math.max(start.scale(), end.scale()), step.scale());
        if (scale < 0)
        {
            scale = 0;
        }
        return Math.min(scale, MAX_SCALE);
    }
}
