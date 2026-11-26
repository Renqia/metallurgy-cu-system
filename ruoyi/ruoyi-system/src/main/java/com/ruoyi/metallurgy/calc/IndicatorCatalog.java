package com.ruoyi.metallurgy.calc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import com.ruoyi.metallurgy.dto.BalanceResult;
import com.ruoyi.metallurgy.dto.IndicatorSeries;
import com.ruoyi.metallurgy.dto.NodeBalance;

/**
 * 对比 / 敏感性分析统一指标目录（纯算法类，不依赖 Spring 容器）
 *
 * <p>用一个<b>有序、固定</b>的指标清单，从全流程结果快照（{@link BalanceResult}）中抽取指标值。
 * 方案对比的表格行、敏感性分析的曲线、以及两者的导出列，全部由本目录统一产生，
 * 因此三者的指标名称、单位、顺序天然一致——这是验收标准中"表 / 图 / 导出一致"的实现基础。</p>
 *
 * <p>单位优先取自结果快照自带的 <code>indicatorUnits</code>（顶层与各节点），
 * 只有快照未登记时才回退到本目录的内置说明，避免与既有页面的单位口径打架。</p>
 *
 * <p>失败点（无法计算的扫描点）以 <code>null</code> 参与抽取，对应取值为 null，
 * 由前端渲染为"—"，不补 0。</p>
 *
 * @author 【成员1】
 * @date 2026-10-14
 */
public final class IndicatorCatalog
{
    /** 节点名常量（与 FlowCalculator 生成的节点名一致） */
    private static final String NODE_PREP = "配料";

    /** 闪速熔炼节点名 */
    private static final String NODE_SMELTING = "闪速熔炼";

    /** 转炉吹炼节点名 */
    private static final String NODE_CONVERTING = "转炉吹炼";

    /** 阳极精炼节点名 */
    private static final String NODE_REFINING = "阳极精炼";

    /** 电解精炼节点名 */
    private static final String NODE_ELECTRO = "电解精炼";

    /** 最大元素平衡残差的指标名（非快照既有键，由服务层单独计算） */
    public static final String INDICATOR_MAX_RESIDUAL = "最大元素平衡残差";

    /** 指标定义（顶层指标 / 节点指标 / 特殊指标） */
    private static final List<Spec> SPECS = buildSpecs();

    private IndicatorCatalog()
    {
    }

    /**
     * 指标定义项
     *
     * <p><code>node</code> 为空表示顶层指标或由 result 直接给出的指标；
     * 非空表示取自该节点的 indicators。</p>
     */
    private static final class Spec
    {
        /** 指标名（与快照 indicators 的中文键一致） */
        private final String name;

        /** 内置单位说明（快照未登记时的回退值） */
        private final String fallbackUnit;

        /** 所属节点名，顶层/特殊指标为 null */
        private final String node;

        /** 取值函数（入参可能为 null，表示该扫描点计算失败） */
        private final Function<BalanceResult, BigDecimal> extractor;

        private Spec(String name, String fallbackUnit, String node, Function<BalanceResult, BigDecimal> extractor)
        {
            this.name = name;
            this.fallbackUnit = fallbackUnit;
            this.node = node;
            this.extractor = extractor;
        }
    }

    private static List<Spec> buildSpecs()
    {
        List<Spec> list = new ArrayList<>();
        // ---- 配料与混合料 ----
        list.add(new Spec("混合料Cu品位", "%", null,
                b -> b == null || b.getMixedMaterial() == null ? null : b.getMixedMaterial().getCu()));
        list.add(new Spec("配比合计", "%", NODE_PREP, b -> node(b, NODE_PREP, "配比合计")));
        list.add(new Spec("混合料综合成本", "元/吨", NODE_PREP, b -> node(b, NODE_PREP, "混合料综合成本")));
        // ---- 闪速熔炼 ----
        list.add(new Spec("冰铜品位", "%", NODE_SMELTING, b -> node(b, NODE_SMELTING, "冰铜品位")));
        list.add(new Spec("冰铜量", "kg", NODE_SMELTING, b -> node(b, NODE_SMELTING, "冰铜量")));
        list.add(new Spec("炉渣量", "kg", NODE_SMELTING, b -> node(b, NODE_SMELTING, "炉渣量")));
        list.add(new Spec("炉渣硅铁比", "", NODE_SMELTING, b -> node(b, NODE_SMELTING, "炉渣硅铁比")));
        list.add(new Spec("渣含铜", "%", NODE_SMELTING, b -> node(b, NODE_SMELTING, "渣含铜")));
        list.add(new Spec("铜直收率", "%", NODE_SMELTING, b -> node(b, NODE_SMELTING, "铜直收率")));
        // ---- 转炉吹炼 ----
        list.add(new Spec("粗铜量", "kg", NODE_CONVERTING, b -> node(b, NODE_CONVERTING, "粗铜量")));
        list.add(new Spec("粗铜品位", "%", NODE_CONVERTING, b -> node(b, NODE_CONVERTING, "粗铜品位")));
        list.add(new Spec("石英石加入量", "kg", NODE_CONVERTING, b -> node(b, NODE_CONVERTING, "石英石加入量")));
        list.add(new Spec("吹炼渣量", "kg", NODE_CONVERTING, b -> node(b, NODE_CONVERTING, "吹炼渣量")));
        list.add(new Spec("吹炼渣含铜", "%", NODE_CONVERTING, b -> node(b, NODE_CONVERTING, "吹炼渣含铜")));
        // ---- 阳极精炼与电解精炼 ----
        list.add(new Spec("阳极铜量", "kg", NODE_REFINING, b -> node(b, NODE_REFINING, "阳极铜量")));
        list.add(new Spec("阴极铜量", "kg", NODE_ELECTRO, b -> node(b, NODE_ELECTRO, "阴极铜量")));
        // ---- 全流程汇总 ----
        list.add(new Spec("全流程铜总回收率", "%", null, b -> top(b, "全流程铜总回收率")));
        list.add(new Spec("单位阴极铜混合料耗", "kg混合料/kg阴极铜", null, b -> top(b, "单位阴极铜混合料耗")));
        list.add(new Spec("SO2 总产量", "kg/100kg混合料", null, b -> top(b, "SO2 总产量")));
        // ---- 质量校核 ----
        list.add(new Spec(INDICATOR_MAX_RESIDUAL, "%", null,
                b -> b == null ? null : b.getMaxResidual()));
        return list;
    }

    /**
     * 抽取指标序列（行 = 指标，列 = 各结果）
     *
     * @param results 结果快照集合；元素允许为 null（表示该列无结果）
     * @return 有序指标序列
     */
    public static List<IndicatorSeries> extract(List<BalanceResult> results)
    {
        List<BalanceResult> list = results == null ? new ArrayList<>() : results;
        List<IndicatorSeries> seriesList = new ArrayList<>();
        for (Spec spec : SPECS)
        {
            IndicatorSeries series = new IndicatorSeries();
            series.setName(spec.name);
            series.setUnit(resolveUnit(spec, list));
            for (BalanceResult result : list)
            {
                series.addValue(result == null ? null : spec.extractor.apply(result));
            }
            seriesList.add(series);
        }
        return seriesList;
    }

    /**
     * 抽取单个结果的指标（指标名 → 取值），用于展示基线值
     *
     * @param result 结果快照（可为 null）
     * @return 有序映射
     */
    public static Map<String, BigDecimal> extractOne(BalanceResult result)
    {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (IndicatorSeries series : extract(java.util.Collections.singletonList(result)))
        {
            map.put(series.getName(), series.getValues().isEmpty() ? null : series.getValues().get(0));
        }
        return map;
    }

    /**
     * 指标名称清单（保持目录顺序）
     *
     * @return 指标名列表
     */
    public static List<String> names()
    {
        List<String> names = new ArrayList<>();
        for (Spec spec : SPECS)
        {
            names.add(spec.name);
        }
        return names;
    }

    /**
     * 解析指标单位：优先取快照 indicatorUnits，全部为空时用内置回退值
     */
    private static String resolveUnit(Spec spec, List<BalanceResult> results)
    {
        for (BalanceResult result : results)
        {
            String unit = snapshotUnit(result, spec);
            if (unit != null && !unit.isEmpty())
            {
                return unit;
            }
        }
        return spec.fallbackUnit;
    }

    private static String snapshotUnit(BalanceResult result, Spec spec)
    {
        if (result == null)
        {
            return null;
        }
        if (spec.node == null)
        {
            return result.getIndicatorUnits() == null ? null : result.getIndicatorUnits().get(spec.name);
        }
        NodeBalance node = findNode(result, spec.node);
        return node == null || node.getIndicatorUnits() == null ? null : node.getIndicatorUnits().get(spec.name);
    }

    private static BigDecimal top(BalanceResult result, String name)
    {
        if (result == null || result.getIndicators() == null)
        {
            return null;
        }
        return result.getIndicators().get(name);
    }

    private static BigDecimal node(BalanceResult result, String nodeName, String name)
    {
        NodeBalance node = result == null ? null : findNode(result, nodeName);
        if (node == null || node.getIndicators() == null)
        {
            return null;
        }
        return node.getIndicators().get(name);
    }

    private static NodeBalance findNode(BalanceResult result, String nodeName)
    {
        if (result == null || result.getNodes() == null)
        {
            return null;
        }
        for (NodeBalance node : result.getNodes())
        {
            if (nodeName.equals(node.getNodeName()))
            {
                return node;
            }
        }
        return null;
    }
}
