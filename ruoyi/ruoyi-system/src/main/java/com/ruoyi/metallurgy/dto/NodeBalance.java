package com.ruoyi.metallurgy.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 单个工序节点的物料平衡结果
 *
 * <p>一个节点由若干输入流与输出流组成，并附带该节点的工艺指标（如冰铜品位、硅铁比、
 * 直收率）与元素平衡校核表。</p>
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
public class NodeBalance
{
    /** 节点名称（配料/闪速熔炼/转炉吹炼/阳极精炼/电解精炼） */
    private String nodeName;

    /** 输入流 */
    private List<StreamItem> inputs = new ArrayList<>();

    /** 输出流 */
    private List<StreamItem> outputs = new ArrayList<>();

    /** 节点工艺指标（保持插入顺序，便于前端按工艺顺序展示） */
    private Map<String, BigDecimal> indicators = new LinkedHashMap<>();

    /** 节点指标单位/说明 */
    private Map<String, String> indicatorUnits = new LinkedHashMap<>();

    /** 元素平衡校核表 */
    private List<ElementBalance> elementBalances = new ArrayList<>();

    public String getNodeName()
    {
        return nodeName;
    }

    public void setNodeName(String nodeName)
    {
        this.nodeName = nodeName;
    }

    public List<StreamItem> getInputs()
    {
        return inputs;
    }

    public void setInputs(List<StreamItem> inputs)
    {
        this.inputs = inputs;
    }

    public List<StreamItem> getOutputs()
    {
        return outputs;
    }

    public void setOutputs(List<StreamItem> outputs)
    {
        this.outputs = outputs;
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

    public List<ElementBalance> getElementBalances()
    {
        return elementBalances;
    }

    public void setElementBalances(List<ElementBalance> elementBalances)
    {
        this.elementBalances = elementBalances;
    }

    /**
     * 添加一个指标（同时登记单位说明）
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

    /**
     * 汇总输入/输出流的某元素质量
     *
     * @param items 流集合
     * @param element 元素名
     * @return 质量合计(kg)
     */
    public static BigDecimal sumElement(List<StreamItem> items, String element)
    {
        BigDecimal sum = BigDecimal.ZERO;
        for (StreamItem item : items)
        {
            sum = sum.add(item.massOf(element));
        }
        return sum;
    }

    /**
     * 按元素列表生成本节点的元素平衡校核表
     *
     * @param elements 元素名数组
     */
    public void buildElementBalances(String[] elements)
    {
        this.elementBalances = new ArrayList<>();
        for (String element : elements)
        {
            this.elementBalances.add(new ElementBalance(element,
                    sumElement(this.inputs, element), sumElement(this.outputs, element)));
        }
    }
}
