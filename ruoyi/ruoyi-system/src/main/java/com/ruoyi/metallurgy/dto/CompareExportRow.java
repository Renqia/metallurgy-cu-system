package com.ruoyi.metallurgy.dto;

import com.ruoyi.common.annotation.Excel;

/**
 * 多方案对比导出行
 *
 * <p>对比结果的列数随勾选记录数变化（2~4），无法用固定字段的实体直接导出，
 * 故导出时把"指标名/单位/各方案取值"统一转为字符串行，
 * 前两行为"参数方案""计算时间"等列头信息，其后为各指标。
 * 这样表格、图表与导出的指标名称、单位、顺序完全一致。</p>
 *
 * @author 【成员1】
 * @date 2026-10-14
 */
public class CompareExportRow
{
    @Excel(name = "项目")
    private String item;

    @Excel(name = "单位")
    private String unit;

    @Excel(name = "方案1")
    private String scheme1;

    @Excel(name = "方案2")
    private String scheme2;

    @Excel(name = "方案3")
    private String scheme3;

    @Excel(name = "方案4")
    private String scheme4;

    public CompareExportRow()
    {
    }

    public CompareExportRow(String item, String unit, String scheme1, String scheme2, String scheme3, String scheme4)
    {
        this.item = item;
        this.unit = unit;
        this.scheme1 = scheme1;
        this.scheme2 = scheme2;
        this.scheme3 = scheme3;
        this.scheme4 = scheme4;
    }

    public String getItem()
    {
        return item;
    }

    public void setItem(String item)
    {
        this.item = item;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public String getScheme1()
    {
        return scheme1;
    }

    public void setScheme1(String scheme1)
    {
        this.scheme1 = scheme1;
    }

    public String getScheme2()
    {
        return scheme2;
    }

    public void setScheme2(String scheme2)
    {
        this.scheme2 = scheme2;
    }

    public String getScheme3()
    {
        return scheme3;
    }

    public void setScheme3(String scheme3)
    {
        this.scheme3 = scheme3;
    }

    public String getScheme4()
    {
        return scheme4;
    }

    public void setScheme4(String scheme4)
    {
        this.scheme4 = scheme4;
    }
}
