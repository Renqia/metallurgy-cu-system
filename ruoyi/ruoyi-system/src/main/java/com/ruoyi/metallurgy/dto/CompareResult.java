package com.ruoyi.metallurgy.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 多方案对比结果
 *
 * <p>行 = 指标（统一目录、统一顺序），列 = 各计算记录。
 * 指标集合由算法层 {@code IndicatorCatalog} 统一抽取，保证表格、图表与导出三者
 * 的指标名称、单位、顺序完全一致。</p>
 *
 * @author 【成员1】
 * @date 2026-10-14
 */
public class CompareResult
{
    /** 对比列（按请求的记录顺序） */
    private List<CompareColumn> columns = new ArrayList<>();

    /** 指标序列（行） */
    private List<IndicatorSeries> indicators = new ArrayList<>();

    /** 提示说明（如快照缺少模型参数） */
    private List<String> notes = new ArrayList<>();

    public List<CompareColumn> getColumns()
    {
        return columns;
    }

    public void setColumns(List<CompareColumn> columns)
    {
        this.columns = columns;
    }

    public List<IndicatorSeries> getIndicators()
    {
        return indicators;
    }

    public void setIndicators(List<IndicatorSeries> indicators)
    {
        this.indicators = indicators;
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
