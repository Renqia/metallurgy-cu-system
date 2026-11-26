package com.ruoyi.metallurgy.calc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.metallurgy.dto.BalanceResult;
import com.ruoyi.metallurgy.dto.NodeBalance;
import com.ruoyi.metallurgy.dto.SankeyData;
import com.ruoyi.metallurgy.dto.StreamItem;

/**
 * 桑基图数据装配器
 *
 * <p>把全流程平衡结果装配为 ECharts 桑基图所需的 nodes / links 结构。装配规则：</p>
 * <ol>
 *   <li>以"工序节点"与"物料流"作为图节点：原料 → 闪速熔炼 →（冰铜）→ 转炉吹炼 →（粗铜）
 *       → 阳极精炼 →（阳极铜）→ 电解精炼，各节点产物作为汇（炉渣、烟尘、烟气、渣、阳极泥等）；</li>
 *   <li>原料节点最多保留 4 个（按流量降序），其余合并为"其他原料"，以控制图规模；
 *       最终节点数不超过 20、连线数不超过 30（需求 F5.1）；</li>
 *   <li>同名的烟气按工序区分命名（闪速烟气 / 吹炼烟气 / 精炼烟气），保证节点名唯一；</li>
 *   <li>流量为 0（该元素不经过该流）的连线直接过滤，并从连线反推节点集合，避免出现孤立节点。</li>
 * </ol>
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
public final class SankeyBuilder
{
    /** 原料节点显示上限（超出合并为"其他原料"） */
    private static final int MAX_RAW_NODES = 4;

    private SankeyBuilder()
    {
    }

    /**
     * 构建桑基图数据
     *
     * @param result 全流程平衡结果
     * @param element 元素口径（Cu/Fe/S/SiO2/CaO/合计）
     * @return 桑基图数据
     */
    public static SankeyData build(BalanceResult result, String element)
    {
        if (result == null || result.getNodes() == null || result.getNodes().isEmpty())
        {
            throw new ServiceException("计算结果为空，无法生成物质流桑基图");
        }
        String elem = (element == null || element.isEmpty()) ? "Cu" : element;
        SankeyData data = new SankeyData();
        data.setElement(elem);
        data.setUnit("合计".equals(elem) ? "kg/100kg混合料（总质量口径）" : "kg/100kg混合料");

        List<SankeyData.SankeyLink> links = new ArrayList<>();
        NodeBalance prep = findNode(result, "配料");
        NodeBalance smelt = findNode(result, "闪速熔炼");
        NodeBalance convert = findNode(result, "转炉吹炼");
        NodeBalance refine = findNode(result, "阳极精炼");
        NodeBalance electro = findNode(result, "电解精炼");

        // 1) 原料 → 闪速熔炼（最多保留 4 个原料节点）
        addRawMaterialLinks(prep, smelt.getNodeName(), elem, links);

        // 2) 闪速熔炼 → 各产物
        StreamItem matte = findOutput(smelt, "冰铜");
        addLink(links, smelt.getNodeName(), "冰铜", matte.massOf(elem));
        addLink(links, smelt.getNodeName(), "炉渣", findOutput(smelt, "炉渣").massOf(elem));
        addLink(links, smelt.getNodeName(), "烟尘", findOutput(smelt, "烟尘").massOf(elem));
        addLink(links, smelt.getNodeName(), "闪速烟气", findOutput(smelt, "烟气").massOf(elem));

        // 3) 冰铜 → 转炉吹炼 → 产物
        addLink(links, "冰铜", convert.getNodeName(), matte.massOf(elem));
        StreamItem blister = findOutput(convert, "粗铜");
        addLink(links, convert.getNodeName(), "粗铜", blister.massOf(elem));
        addLink(links, convert.getNodeName(), "吹炼渣", findOutput(convert, "吹炼渣").massOf(elem));
        addLink(links, convert.getNodeName(), "吹炼烟气", findOutput(convert, "烟气").massOf(elem));

        // 4) 粗铜 → 阳极精炼 → 产物
        addLink(links, "粗铜", refine.getNodeName(), blister.massOf(elem));
        StreamItem anode = findOutput(refine, "阳极铜");
        addLink(links, refine.getNodeName(), "阳极铜", anode.massOf(elem));
        addLink(links, refine.getNodeName(), "精炼渣", findOutput(refine, "精炼渣").massOf(elem));
        addLink(links, refine.getNodeName(), "精炼烟气", findOutput(refine, "精炼烟气").massOf(elem));

        // 5) 阳极铜 → 电解精炼 → 产物
        addLink(links, "阳极铜", electro.getNodeName(), anode.massOf(elem));
        addLink(links, electro.getNodeName(), "阴极铜", findOutput(electro, "阴极铜").massOf(elem));
        addLink(links, electro.getNodeName(), "阳极泥", findOutput(electro, "阳极泥").massOf(elem));

        // 6) 过滤零流量连线，并由连线反推节点集合
        List<SankeyData.SankeyLink> valid = new ArrayList<>();
        Set<String> nodeNames = new LinkedHashSet<>();
        for (SankeyData.SankeyLink link : links)
        {
            if (link.getValue() != null && link.getValue().compareTo(BigDecimal.ZERO) > 0)
            {
                valid.add(link);
                nodeNames.add(link.getSource());
                nodeNames.add(link.getTarget());
            }
        }
        for (String name : nodeNames)
        {
            data.getNodes().add(new SankeyData.SankeyNode(name));
        }
        data.setLinks(valid);
        return data;
    }

    /**
     * 添加原料到熔炼节点的连线（原料节点数超限时合并为"其他原料"）
     */
    private static void addRawMaterialLinks(NodeBalance prep, String target, String element,
            List<SankeyData.SankeyLink> links)
    {
        List<StreamItem> positive = new ArrayList<>();
        for (StreamItem item : prep.getInputs())
        {
            if (item.massOf(element).compareTo(BigDecimal.ZERO) > 0)
            {
                positive.add(item);
            }
        }
        positive.sort(Comparator.comparing((StreamItem it) -> it.massOf(element)).reversed());
        if (positive.size() <= MAX_RAW_NODES)
        {
            for (StreamItem item : positive)
            {
                addLink(links, item.getName(), target, item.massOf(element));
            }
            return;
        }
        int keep = MAX_RAW_NODES - 1;
        BigDecimal otherSum = BigDecimal.ZERO;
        for (int i = 0; i < positive.size(); i++)
        {
            StreamItem item = positive.get(i);
            if (i < keep)
            {
                addLink(links, item.getName(), target, item.massOf(element));
            }
            else
            {
                otherSum = otherSum.add(item.massOf(element));
            }
        }
        addLink(links, "其他原料", target, otherSum);
    }

    /**
     * 添加一条连线（流量为空或非正时不添加）
     */
    private static void addLink(List<SankeyData.SankeyLink> links, String source, String target, BigDecimal value)
    {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0)
        {
            return;
        }
        links.add(new SankeyData.SankeyLink(source, target, value));
    }

    /**
     * 按节点名查找节点
     */
    private static NodeBalance findNode(BalanceResult result, String nodeName)
    {
        for (NodeBalance node : result.getNodes())
        {
            if (nodeName.equals(node.getNodeName()))
            {
                return node;
            }
        }
        throw new ServiceException("计算结果缺少节点：" + nodeName);
    }

    /**
     * 按流名查找节点输出流
     */
    private static StreamItem findOutput(NodeBalance node, String name)
    {
        for (StreamItem item : node.getOutputs())
        {
            if (name.equals(item.getName()))
            {
                return item;
            }
        }
        throw new ServiceException("节点【" + node.getNodeName() + "】缺少输出流：" + name);
    }
}
