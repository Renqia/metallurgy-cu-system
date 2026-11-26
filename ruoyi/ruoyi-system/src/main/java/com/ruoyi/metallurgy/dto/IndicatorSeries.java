package com.ruoyi.metallurgy.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 指标序列
 *
 * <p>用于"多方案对比"与"敏感性分析"的统一数据结构：一个指标名 + 单位 + 各列取值。
 * 取值的列序与对比列（各计算记录）或扫描点顺序严格一一对应，
 * 因此前端表格、图表与导出的数值顺序天然一致。</p>
 *
 * <p>无法取值的列一律为 <code>null</code>（例如扫描时该点工艺失败），
 * 由前端渲染为"—"，<b>不补 0</b>，避免把"无数据"误读为"结果为 0"。</p>
 *
 * @author 【成员1】
 * @date 2026-10-14
 */
public class IndicatorSeries
{
    /** 指标名称（与结果快照 indicators 中的中文键一致） */
    private String name;

    /** 指标单位（优先取自结果快照 indicatorUnits，缺失时用内置默认说明） */
    private String unit;

    /** 各列取值，与对比列或扫描点一一对应；无数据为 null */
    private List<BigDecimal> values = new ArrayList<>();

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public List<BigDecimal> getValues()
    {
        return values;
    }

    public void setValues(List<BigDecimal> values)
    {
        this.values = values;
    }

    /**
     * 追加一个取值（null 表示该列无数据）
     *
     * @param value 取值
     */
    public void addValue(BigDecimal value)
    {
        if (this.values == null)
        {
            this.values = new ArrayList<>();
        }
        this.values.add(value);
    }
}
