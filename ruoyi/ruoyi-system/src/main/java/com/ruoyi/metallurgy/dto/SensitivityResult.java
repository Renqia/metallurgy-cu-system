package com.ruoyi.metallurgy.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 敏感性分析结果
 *
 * <p>横轴为扫描值序列，纵向为各指标曲线；曲线与扫描点按下标一一对应，
 * 失败点对应值为 null（前端断线，不补 0）。同时给出基线记录的原始指标值，
 * 便于在图表上对照"扫描前"的水平。</p>
 *
 * @author 【成员1】
 * @date 2026-10-14
 */
public class SensitivityResult
{
    /** 基线计算记录ID */
    private Long baseCalcId;

    /** 基线记录对应的参数方案名称 */
    private String planName;

    /** 扫描字段名（matteGrade / blisterFeSio2） */
    private String field;

    /** 扫描字段显示名（如"目标冰铜品位"） */
    private String fieldLabel;

    /** 扫描字段单位（如"%"；转炉吹炼渣硅铁比为无量纲，空字符串） */
    private String fieldUnit;

    /** 基线记录中该字段的当前取值 */
    private BigDecimal baseValue;

    /** 扫描点（含失败点，按扫描值升序） */
    private List<SensitivityPoint> points = new ArrayList<>();

    /** 指标曲线（名称/单位/取值序列与扫描点一一对应） */
    private List<IndicatorSeries> indicators = new ArrayList<>();

    /** 基线记录的原始指标值（指标名 → 取值），用于图表对照 */
    private Map<String, BigDecimal> baseIndicators;

    /** 成功重算的点数 */
    private int successCount;

    /** 重算失败的点数（工艺不可行区间） */
    private int failedCount;

    /** 提示与模型限制说明 */
    private List<String> notes = new ArrayList<>();

    public Long getBaseCalcId()
    {
        return baseCalcId;
    }

    public void setBaseCalcId(Long baseCalcId)
    {
        this.baseCalcId = baseCalcId;
    }

    public String getPlanName()
    {
        return planName;
    }

    public void setPlanName(String planName)
    {
        this.planName = planName;
    }

    public String getField()
    {
        return field;
    }

    public void setField(String field)
    {
        this.field = field;
    }

    public String getFieldLabel()
    {
        return fieldLabel;
    }

    public void setFieldLabel(String fieldLabel)
    {
        this.fieldLabel = fieldLabel;
    }

    public String getFieldUnit()
    {
        return fieldUnit;
    }

    public void setFieldUnit(String fieldUnit)
    {
        this.fieldUnit = fieldUnit;
    }

    public BigDecimal getBaseValue()
    {
        return baseValue;
    }

    public void setBaseValue(BigDecimal baseValue)
    {
        this.baseValue = baseValue;
    }

    public List<SensitivityPoint> getPoints()
    {
        return points;
    }

    public void setPoints(List<SensitivityPoint> points)
    {
        this.points = points;
    }

    public List<IndicatorSeries> getIndicators()
    {
        return indicators;
    }

    public void setIndicators(List<IndicatorSeries> indicators)
    {
        this.indicators = indicators;
    }

    public Map<String, BigDecimal> getBaseIndicators()
    {
        return baseIndicators;
    }

    public void setBaseIndicators(Map<String, BigDecimal> baseIndicators)
    {
        this.baseIndicators = baseIndicators;
    }

    public int getSuccessCount()
    {
        return successCount;
    }

    public void setSuccessCount(int successCount)
    {
        this.successCount = successCount;
    }

    public int getFailedCount()
    {
        return failedCount;
    }

    public void setFailedCount(int failedCount)
    {
        this.failedCount = failedCount;
    }

    public List<String> getNotes()
    {
        return notes;
    }

    public void setNotes(List<String> notes)
    {
        this.notes = notes;
    }
}
