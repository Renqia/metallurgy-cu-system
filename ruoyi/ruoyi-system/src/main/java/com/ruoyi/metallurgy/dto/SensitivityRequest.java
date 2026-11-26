package com.ruoyi.metallurgy.dto;

import java.math.BigDecimal;

/**
 * 敏感性分析请求
 *
 * <p>以一条已有全流程计算记录为基线，对指定模型参数在给定区间内逐点独立重算，
 * 观察各工艺指标随该参数的变化趋势。<b>扫描值真实参与计算模型，不是只改显示值。</b></p>
 *
 * <p>可扫描字段（受模型能力限制，仅两项）：</p>
 * <ul>
 *   <li><code>matteGrade</code> 目标冰铜品位(%)：经配料求解器与闪速熔炼节点真实影响结果；</li>
 *   <li><code>blisterFeSio2</code> 转炉吹炼渣硅铁比：直接作用于转炉造渣石英石加入量与外排渣量。</li>
 * </ul>
 * <p><b>富氧浓度、氧料比不参与元素平衡计算，不属于可扫描字段。</b></p>
 *
 * @author 【成员1】
 * @date 2026-10-14
 */
public class SensitivityRequest
{
    /** 基线计算记录ID（须为全流程平衡记录） */
    private Long baseCalcId;

    /** 扫描字段：matteGrade 目标冰铜品位 / blisterFeSio2 转炉吹炼渣硅铁比 */
    private String field;

    /** 扫描起点 */
    private BigDecimal start;

    /** 扫描终点（含） */
    private BigDecimal end;

    /** 扫描步长（必须大于 0） */
    private BigDecimal step;

    public Long getBaseCalcId()
    {
        return baseCalcId;
    }

    public void setBaseCalcId(Long baseCalcId)
    {
        this.baseCalcId = baseCalcId;
    }

    public String getField()
    {
        return field;
    }

    public void setField(String field)
    {
        this.field = field;
    }

    public BigDecimal getStart()
    {
        return start;
    }

    public void setStart(BigDecimal start)
    {
        this.start = start;
    }

    public BigDecimal getEnd()
    {
        return end;
    }

    public void setEnd(BigDecimal end)
    {
        this.end = end;
    }

    public BigDecimal getStep()
    {
        return step;
    }

    public void setStep(BigDecimal step)
    {
        this.step = step;
    }
}
