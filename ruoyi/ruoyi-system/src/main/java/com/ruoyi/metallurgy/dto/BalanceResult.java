package com.ruoyi.metallurgy.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 全流程物料平衡计算结果
 *
 * <p>按工艺顺序保存各节点（配料 → 闪速熔炼 → 转炉吹炼 → 阳极精炼 → 电解精炼）的
 * 输入输出流、工艺指标与元素平衡校核表，并给出全流程汇总指标。</p>
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
public class BalanceResult
{
    /** 计算基准说明 */
    private String basis = "以 100 kg 干燥混合料为计算基准";

    /** 混合料（配料结果） */
    private StreamItem mixedMaterial = new StreamItem("混合料", "output");

    /** 各节点结果 */
    private List<NodeBalance> nodes = new ArrayList<>();

    /** 全流程汇总指标 */
    private Map<String, BigDecimal> indicators = new LinkedHashMap<>();

    /** 指标单位说明 */
    private Map<String, String> indicatorUnits = new LinkedHashMap<>();

    /** 每条元素平衡的最大残差(%)，用于页面醒目提示 */
    private BigDecimal maxResidual = BigDecimal.ZERO;

    /** 是否满足全部工艺约束 */
    private boolean feasible = true;

    /** 提示与假设说明 */
    private List<String> warnings = new ArrayList<>();

    /** 模型假设说明（写入说明书"理论依据"） */
    private List<String> assumptions = new ArrayList<>();

    public String getBasis()
    {
        return basis;
    }

    public void setBasis(String basis)
    {
        this.basis = basis;
    }

    public StreamItem getMixedMaterial()
    {
        return mixedMaterial;
    }

    public void setMixedMaterial(StreamItem mixedMaterial)
    {
        this.mixedMaterial = mixedMaterial;
    }

    public List<NodeBalance> getNodes()
    {
        return nodes;
    }

    public void setNodes(List<NodeBalance> nodes)
    {
        this.nodes = nodes;
    }

    public Map<String, BigDecimal> getIndicators()
    {
        return indicators;
    }

    public void setIndicators(Map<String, BigDecimal> indicators)
    {
        this.indicators = indicators;
    }

    public Map<String, String> getIndicatorUnits()
    {
        return indicatorUnits;
    }

    public void setIndicatorUnits(Map<String, String> indicatorUnits)
    {
        this.indicatorUnits = indicatorUnits;
    }

    public BigDecimal getMaxResidual()
    {
        return maxResidual;
    }

    public void setMaxResidual(BigDecimal maxResidual)
    {
        this.maxResidual = maxResidual;
    }

    public boolean isFeasible()
    {
        return feasible;
    }

    public void setFeasible(boolean feasible)
    {
        this.feasible = feasible;
    }

    public List<String> getWarnings()
    {
        return warnings;
    }

    public void setWarnings(List<String> warnings)
    {
        this.warnings = warnings;
    }

    public List<String> getAssumptions()
    {
        return assumptions;
    }

    public void setAssumptions(List<String> assumptions)
    {
        this.assumptions = assumptions;
    }

    /**
     * 添加全流程指标
     *
     * @param name 指标名
     * @param value 指标值
     * @param unit 单位说明
     */
    public void addIndicator(String name, BigDecimal value, String unit)
    {
        this.indicators.put(name, value);
        if (unit != null)
        {
            this.indicatorUnits.put(name, unit);
        }
    }
}
