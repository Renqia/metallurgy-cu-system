package com.ruoyi.metallurgy.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 桑基图数据（对接前端 ECharts series.type = 'sankey'）
 *
 * <p>结构约定：<code>nodes</code> 为去重后的节点名列表，<code>links</code> 为
 * source → target 的流量（同一元素口径）。为保证渲染性能与可读性，
 * 服务端已把流量为 0 的连线过滤掉，并按实际连线收集节点（避免孤立节点导致渲染异常）。</p>
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
public class SankeyData
{
    /** 元素口径（Cu/Fe/S/SiO2/CaO/合计） */
    private String element;

    /** 单位说明 */
    private String unit = "kg/100kg混合料";

    /** 节点列表 */
    private List<SankeyNode> nodes = new ArrayList<>();

    /** 连线列表 */
    private List<SankeyLink> links = new ArrayList<>();

    /** 桑基图节点 */
    public static class SankeyNode
    {
        private String name;

        public SankeyNode()
        {
        }

        public SankeyNode(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }
    }

    /** 桑基图连线（source → target，value 为流量） */
    public static class SankeyLink
    {
        private String source;

        private String target;

        private BigDecimal value = BigDecimal.ZERO;

        public SankeyLink()
        {
        }

        public SankeyLink(String source, String target, BigDecimal value)
        {
            this.source = source;
            this.target = target;
            this.value = value;
        }

        public String getSource()
        {
            return source;
        }

        public void setSource(String source)
        {
            this.source = source;
        }

        public String getTarget()
        {
            return target;
        }

        public void setTarget(String target)
        {
            this.target = target;
        }

        public BigDecimal getValue()
        {
            return value;
        }

        public void setValue(BigDecimal value)
        {
            this.value = value;
        }
    }

    public String getElement()
    {
        return element;
    }

    public void setElement(String element)
    {
        this.element = element;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public List<SankeyNode> getNodes()
    {
        return nodes;
    }

    public void setNodes(List<SankeyNode> nodes)
    {
        this.nodes = nodes;
    }

    public List<SankeyLink> getLinks()
    {
        return links;
    }

    public void setLinks(List<SankeyLink> links)
    {
        this.links = links;
    }
}
