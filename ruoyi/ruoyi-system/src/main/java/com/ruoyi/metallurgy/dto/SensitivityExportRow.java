package com.ruoyi.metallurgy.dto;

import com.ruoyi.common.annotation.Excel;

/**
 * 敏感性分析导出行（一行 = 一个扫描点）
 *
 * <p>敏感性分析最多允许 50 个扫描点，若按"指标为行、扫描点为列"导出会出现几十列，
 * 故导出采用"扫描点为行、关键指标为列"的形态：</p>
 * <ul>
 *   <li>关键指标取自 {@code IndicatorCatalog} 中的同名指标（冰铜品位、冰铜量、炉渣量、
 *       铜直收率、粗铜量、石英石加入量、阴极铜量、全流程铜总回收率、SO2 总产量、最大元素平衡残差）；</li>
 *   <li>工艺不可行的扫描点：<b>保留该行</b>，"是否可行"列为"否"、指标列留空（"—"），
 *       并在"不可行原因"列给出中文原因，便于定位可行区间边界。</li>
 * </ul>
 *
 * @author 【成员1】
 * @date 2026-10-14
 */
public class SensitivityExportRow
{
    @Excel(name = "扫描值")
    private String scanValue;

    @Excel(name = "是否可行")
    private String feasible;

    @Excel(name = "冰铜品位(%)")
    private String matteGrade;

    @Excel(name = "冰铜量(kg)")
    private String matteMass;

    @Excel(name = "炉渣量(kg)")
    private String slagMass;

    @Excel(name = "铜直收率(%)")
    private String directYield;

    @Excel(name = "粗铜量(kg)")
    private String blisterMass;

    @Excel(name = "石英石加入量(kg)")
    private String quartzMass;

    @Excel(name = "阴极铜量(kg)")
    private String cathodeMass;

    @Excel(name = "全流程铜总回收率(%)")
    private String totalRecovery;

    @Excel(name = "SO2总产量(kg/100kg混合料)")
    private String so2Total;

    @Excel(name = "最大元素平衡残差(%)")
    private String maxResidual;

    @Excel(name = "不可行原因")
    private String message;

    public String getScanValue()
    {
        return scanValue;
    }

    public void setScanValue(String scanValue)
    {
        this.scanValue = scanValue;
    }

    public String getFeasible()
    {
        return feasible;
    }

    public void setFeasible(String feasible)
    {
        this.feasible = feasible;
    }

    public String getMatteGrade()
    {
        return matteGrade;
    }

    public void setMatteGrade(String matteGrade)
    {
        this.matteGrade = matteGrade;
    }

    public String getMatteMass()
    {
        return matteMass;
    }

    public void setMatteMass(String matteMass)
    {
        this.matteMass = matteMass;
    }

    public String getSlagMass()
    {
        return slagMass;
    }

    public void setSlagMass(String slagMass)
    {
        this.slagMass = slagMass;
    }

    public String getDirectYield()
    {
        return directYield;
    }

    public void setDirectYield(String directYield)
    {
        this.directYield = directYield;
    }

    public String getBlisterMass()
    {
        return blisterMass;
    }

    public void setBlisterMass(String blisterMass)
    {
        this.blisterMass = blisterMass;
    }

    public String getQuartzMass()
    {
        return quartzMass;
    }

    public void setQuartzMass(String quartzMass)
    {
        this.quartzMass = quartzMass;
    }

    public String getCathodeMass()
    {
        return cathodeMass;
    }

    public void setCathodeMass(String cathodeMass)
    {
        this.cathodeMass = cathodeMass;
    }

    public String getTotalRecovery()
    {
        return totalRecovery;
    }

    public void setTotalRecovery(String totalRecovery)
    {
        this.totalRecovery = totalRecovery;
    }

    public String getSo2Total()
    {
        return so2Total;
    }

    public void setSo2Total(String so2Total)
    {
        this.so2Total = so2Total;
    }

    public String getMaxResidual()
    {
        return maxResidual;
    }

    public void setMaxResidual(String maxResidual)
    {
        this.maxResidual = maxResidual;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}
