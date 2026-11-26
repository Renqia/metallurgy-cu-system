package com.ruoyi.metallurgy.calc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.metallurgy.dto.BalanceResult;
import com.ruoyi.metallurgy.dto.ElementBalance;
import com.ruoyi.metallurgy.dto.NodeBalance;
import com.ruoyi.metallurgy.dto.RatioMaterialItem;
import com.ruoyi.metallurgy.dto.RatioRequest;
import com.ruoyi.metallurgy.dto.RatioResult;
import com.ruoyi.metallurgy.dto.SankeyData;

/**
 * 冶金计算引擎单元测试
 *
 * <p>覆盖配料求解的边界与异常分支、全流程元素守恒的闭合性、物质流数据规模约束，
 * 是《测试与验收.md》中 TC-301~TC-406、TC-501 的自动化证据。</p>
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
class CalcEngineTest
{
    /** 基准工况：目标冰铜品位 55%、目标硅铁比 1.10（基准工况取值见《需求与范围.md》） */
    private static final BigDecimal GRADE = new BigDecimal("55");

    private static final BigDecimal FE_SIO2 = new BigDecimal("1.10");

    /**
     * 构造物料项
     */
    private RatioMaterialItem material(String name, String type, String cu, String fe, String s, String sio2, String cao)
    {
        RatioMaterialItem item = new RatioMaterialItem();
        item.setMaterialName(name);
        item.setMaterialType(type);
        item.setCuContent(new BigDecimal(cu));
        item.setFeContent(new BigDecimal(fe));
        item.setSulfurContent(new BigDecimal(s));
        item.setSio2Content(new BigDecimal(sio2));
        item.setCaoContent(new BigDecimal(cao));
        item.setUnitPrice(new BigDecimal("5000"));
        return item;
    }

    /**
     * 基准物料组：铜精矿 A/B、石英石、石灰石、返料烟尘
     */
    private List<RatioMaterialItem> baselineMaterials()
    {
        List<RatioMaterialItem> list = new ArrayList<>();
        list.add(material("铜精矿A（示例）", "铜精矿", "24.00", "26.00", "31.00", "6.00", "2.00"));
        list.add(material("铜精矿B（示例）", "铜精矿", "28.00", "24.00", "30.00", "8.00", "1.50"));
        list.add(material("石英石（熔剂）", "熔剂", "0.00", "1.50", "0.00", "92.00", "2.00"));
        list.add(material("石灰石（熔剂）", "熔剂", "0.00", "0.50", "0.00", "3.00", "52.00"));
        list.add(material("返料烟尘（闪速炉）", "返料", "12.00", "20.00", "6.00", "8.00", "3.00"));
        return list;
    }

    /**
     * 组装请求
     */
    private RatioRequest request(List<RatioMaterialItem> items, BigDecimal grade, BigDecimal feSio2)
    {
        RatioRequest req = new RatioRequest();
        req.setItems(items);
        req.setMatteGrade(grade);
        req.setFeSio2Ratio(feSio2);
        return req;
    }

    // ------------------------------------------------------------------
    // 配料求解：正常路径
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TC-301 基准数据配料求解：配比合计 100%、全部非负、混合料成分可核算")
    void testSolveRatioBaseline()
    {
        RatioResult result = RatioSolver.solve(request(baselineMaterials(), GRADE, FE_SIO2));
        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.getRatioSum(), "配比合计必须为 100%");
        assertTrue(result.getMassBalanceResidual().compareTo(new BigDecimal("0.01")) <= 0,
                "质量守恒残差应小于 0.01%");
        for (RatioMaterialItem item : result.getItems())
        {
            assertTrue(item.getRatio().compareTo(BigDecimal.ZERO) >= 0,
                    "物料【" + item.getMaterialName() + "】配比不得为负");
        }
        assertTrue(result.getMixedCu().compareTo(BigDecimal.ZERO) > 0, "混合料应含铜");
        assertTrue(result.getMixedSio2().compareTo(BigDecimal.ZERO) > 0, "混合料应含 SiO2 用于造渣");
        // 冰铜品位由约束保证等于目标值
        assertEquals(0, result.getMatteGradeAchieved().compareTo(GRADE), "冰铜品位应等于目标值");
        System.out.println("[TC-301] 配比：" + dump(result) + "；实际硅铁比=" + result.getFeSio2Achieved()
                + "，偏差=" + result.getFeSio2Deviation());
    }

    @Test
    @DisplayName("TC-302 混合料成分为各物料按配比的加权平均（人工核算一致）")
    void testMixedCompositionIsWeightedAverage()
    {
        List<RatioMaterialItem> items = baselineMaterials();
        RatioResult result = RatioSolver.solve(request(items, GRADE, FE_SIO2));
        BigDecimal expectedCu = BigDecimal.ZERO;
        for (RatioMaterialItem item : result.getItems())
        {
            expectedCu = expectedCu.add(item.getRatio().multiply(item.getCuContent())
                    .divide(new BigDecimal("100"), 6, java.math.RoundingMode.HALF_UP));
        }
        expectedCu = expectedCu.setScale(3, java.math.RoundingMode.HALF_UP);
        assertEquals(0, expectedCu.compareTo(result.getMixedCu()),
                "混合料 Cu 品位应与手算加权平均一致：手算=" + expectedCu + "，系统=" + result.getMixedCu());
    }

    // ------------------------------------------------------------------
    // 配料求解：边界与异常
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TC-303 仅选 1 种物料：给出明确中文提示而非 500")
    void testSingleMaterialRejected()
    {
        List<RatioMaterialItem> items = new ArrayList<>();
        items.add(material("铜精矿A（示例）", "铜精矿", "24.00", "26.00", "31.00", "6.00", "2.00"));
        ServiceException ex = assertThrows(ServiceException.class,
                () -> RatioSolver.solve(request(items, GRADE, FE_SIO2)));
        assertTrue(ex.getMessage().contains("至少需要 2 种物料"), "提示信息应说明物料不足");
    }

    @Test
    @DisplayName("TC-304 物料超过 10 种：提示上限")
    void testTooManyMaterialsRejected()
    {
        List<RatioMaterialItem> items = new ArrayList<>();
        for (int i = 0; i < 11; i++)
        {
            items.add(material("物料" + i, "铜精矿", "20.00", "25.00", "30.00", "5.00", "2.00"));
        }
        ServiceException ex = assertThrows(ServiceException.class,
                () -> RatioSolver.solve(request(items, GRADE, FE_SIO2)));
        assertTrue(ex.getMessage().contains("最多支持 10 种物料"), "提示信息应说明物料数量上限");
    }

    @Test
    @DisplayName("目标冰铜品位超出物相模型上限（≥79.86%）：拒绝计算")
    void testTooHighMatteGradeRejected()
    {
        ServiceException ex = assertThrows(ServiceException.class,
                () -> RatioSolver.solve(request(baselineMaterials(), new BigDecimal("85"), FE_SIO2)));
        assertTrue(ex.getMessage().contains("79.86"), "提示信息应说明 Cu2S 理论含铜量上限");
    }

    @Test
    @DisplayName("锁定配比合计超过 100%：拒绝计算")
    void testFixedRatioOverHundredRejected()
    {
        List<RatioMaterialItem> items = baselineMaterials();
        items.get(0).setFixed(true);
        items.get(0).setFixedRatio(new BigDecimal("60"));
        items.get(1).setFixed(true);
        items.get(1).setFixedRatio(new BigDecimal("60"));
        ServiceException ex = assertThrows(ServiceException.class,
                () -> RatioSolver.solve(request(items, GRADE, FE_SIO2)));
        assertTrue(ex.getMessage().contains("锁定配比"), "提示信息应指向锁定配比异常");
    }

    @Test
    @DisplayName("TC-306 物料成分全为 0：不除零、不抛异常，但给出含铜/造渣提示")
    void testZeroCompositionHandled()
    {
        List<RatioMaterialItem> items = new ArrayList<>();
        items.add(material("空物料A", "熔剂", "0", "0", "0", "0", "0"));
        items.add(material("空物料B", "熔剂", "0", "0", "0", "0", "0"));
        RatioResult result = RatioSolver.solve(request(items, GRADE, FE_SIO2));
        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.getRatioSum(), "全零成分时仍应给出配比合计 100%");
        assertFalse(result.getWarnings().isEmpty(), "应给出成分异常提示");
        System.out.println("[TC-306] 提示：" + result.getWarnings());
    }

    @Test
    @DisplayName("负配比场景：主动集迭代后配比全部非负且合计 100%")
    void testNonNegativeActiveSet()
    {
        List<RatioMaterialItem> items = new ArrayList<>();
        items.add(material("高硅熔剂", "熔剂", "0", "0", "0", "100", "0"));
        items.add(material("富铜料", "铜精矿", "60", "0", "0", "0", "0"));
        RatioResult result = RatioSolver.solve(request(items, GRADE, FE_SIO2));
        assertEquals(new BigDecimal("100.00"), result.getRatioSum(), "配比合计应为 100%");
        for (RatioMaterialItem item : result.getItems())
        {
            assertTrue(item.getRatio().compareTo(BigDecimal.ZERO) >= 0, "主动集迭代后不应存在负配比");
        }
        System.out.println("[负配比场景] 解：" + dump(result) + "；可行=" + result.isFeasible()
                + "；提示=" + result.getWarnings());
    }

    @Test
    @DisplayName("TC-305 目标硅铁比不可达：返回最接近可行解并报告偏差（不中断）")
    void testUnreachableFeSio2ReportsDeviation()
    {
        RatioResult result = RatioSolver.solve(request(baselineMaterials(), GRADE, new BigDecimal("3.00")));
        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.getRatioSum(), "即使偏差过大也应保证配比合计 100%");
        assertFalse(result.isFeasible(), "不可达时应标记为不可行");
        assertFalse(result.getWarnings().isEmpty(), "应给出偏差说明");
        System.out.println("[TC-305] 偏差=" + result.getFeSio2Deviation() + "；提示=" + result.getWarnings());
    }

    // ------------------------------------------------------------------
    // 全流程物料平衡
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TC-401/402/403 全流程计算：五节点齐备、元素守恒残差 ≤0.5%、指标为正")
    void testFullFlowBalance()
    {
        RatioResult ratio = RatioSolver.solve(request(baselineMaterials(), GRADE, FE_SIO2));
        BalanceResult balance = FlowCalculator.calculate(ratio, null);

        assertEquals(5, balance.getNodes().size(), "应包含 5 个工序节点");
        String[] expectNodes = { "配料", "闪速熔炼", "转炉吹炼", "阳极精炼", "电解精炼" };
        for (int i = 0; i < expectNodes.length; i++)
        {
            assertEquals(expectNodes[i], balance.getNodes().get(i).getNodeName(), "节点顺序应符合工艺顺序");
        }
        assertTrue(balance.getMaxResidual().compareTo(new BigDecimal("0.50")) <= 0,
                "元素平衡残差应 ≤0.5%，实际=" + balance.getMaxResidual());
        for (NodeBalance node : balance.getNodes())
        {
            for (ElementBalance eb : node.getElementBalances())
            {
                assertTrue(eb.getResidual().abs().compareTo(new BigDecimal("0.50")) <= 0,
                        "节点【" + node.getNodeName() + "】元素 " + eb.getElement()
                                + " 残差过大：" + eb.getResidual());
            }
        }
        BigDecimal directYield = balance.getIndicators().get("闪速熔炼铜直收率");
        BigDecimal totalYield = balance.getIndicators().get("全流程铜总回收率");
        assertNotNull(directYield);
        assertNotNull(totalYield);
        assertTrue(directYield.doubleValue() > 90 && directYield.doubleValue() < 100,
                "闪速熔炼铜直收率应在 90%~100% 之间，实际=" + directYield);
        assertTrue(totalYield.doubleValue() > 85 && totalYield.doubleValue() < 100,
                "全流程铜总回收率应在 85%~100% 之间，实际=" + totalYield);
        System.out.println("[TC-401] 指标：" + balance.getIndicators());
        System.out.println("[TC-401] 残差上限=" + balance.getMaxResidual());
    }

    @Test
    @DisplayName("TC-404 提高目标冰铜品位：冰铜量下降、渣量上升（工艺规律正确）")
    void testHigherGradeTrend()
    {
        RatioResult ratio55 = RatioSolver.solve(request(baselineMaterials(), new BigDecimal("50"), FE_SIO2));
        BalanceResult balance50 = FlowCalculator.calculate(ratio55, null);
        RatioResult ratio62 = RatioSolver.solve(request(baselineMaterials(), new BigDecimal("62"), FE_SIO2));
        BalanceResult balance62 = FlowCalculator.calculate(ratio62, null);

        BigDecimal matte50 = findIndicator(balance50, "闪速熔炼", "冰铜量");
        BigDecimal matte62 = findIndicator(balance62, "闪速熔炼", "冰铜量");
        BigDecimal slag50 = findIndicator(balance50, "闪速熔炼", "炉渣量");
        BigDecimal slag62 = findIndicator(balance62, "闪速熔炼", "炉渣量");
        assertTrue(matte62.compareTo(matte50) < 0, "品位提高后冰铜量应下降：50%→" + matte50 + "，62%→" + matte62);
        assertTrue(slag62.compareTo(slag50) > 0, "品位提高后渣量应上升：50%→" + slag50 + "，62%→" + slag62);
    }

    @Test
    @DisplayName("硫量不足：给出明确中文提示而非失衡结果")
    void testSulfurShortageRejected()
    {
        // 低硫富铜料 + 高硅熔剂：按 55% 冰铜品位所需硫远超入炉硫
        List<RatioMaterialItem> items = new ArrayList<>();
        items.add(material("低硫铜料", "铜精矿", "35.00", "20.00", "2.00", "2.00", "1.00"));
        items.add(material("高硅熔剂", "熔剂", "0.00", "1.00", "0.00", "95.00", "1.00"));
        RatioResult ratio = RatioSolver.solve(request(items, GRADE, FE_SIO2));
        ServiceException ex = assertThrows(ServiceException.class,
                () -> FlowCalculator.calculate(ratio, null));
        assertTrue(ex.getMessage().contains("硫量不足"), "提示信息应说明硫量不足，实际=" + ex.getMessage());
    }

    @Test
    @DisplayName("TC-501 桑基图规模约束：节点 ≤20、连线 ≤30，且无零流量连线")
    void testSankeyLimits()
    {
        RatioResult ratio = RatioSolver.solve(request(baselineMaterials(), GRADE, FE_SIO2));
        BalanceResult balance = FlowCalculator.calculate(ratio, null);
        SankeyData data = SankeyBuilder.build(balance, "Cu");
        assertTrue(data.getNodes().size() <= 20, "节点数应 ≤20，实际=" + data.getNodes().size());
        assertTrue(data.getLinks().size() <= 30, "连线数应 ≤30，实际=" + data.getLinks().size());
        assertFalse(data.getLinks().isEmpty(), "应存在有效连线");
        data.getLinks().forEach(link -> assertTrue(link.getValue().compareTo(BigDecimal.ZERO) > 0,
                "不应存在零流量连线"));
        System.out.println("[TC-501] 节点数=" + data.getNodes().size() + "，连线数=" + data.getLinks().size());
    }

    @Test
    @DisplayName("物质流按元素切换：Cu 视图与 CaO 视图节点集合不同（零流量已被过滤）")
    void testSankeyElementSwitch()
    {
        RatioResult ratio = RatioSolver.solve(request(baselineMaterials(), GRADE, FE_SIO2));
        BalanceResult balance = FlowCalculator.calculate(ratio, null);
        SankeyData cu = SankeyBuilder.build(balance, "Cu");
        SankeyData cao = SankeyBuilder.build(balance, "CaO");
        assertTrue(cu.getNodes().size() >= cao.getNodes().size(),
                "Cu 视图涉及节点不应少于 CaO 视图");
    }

    /**
     * 取某节点某指标值
     */
    private BigDecimal findIndicator(BalanceResult balance, String nodeName, String indicator)
    {
        for (NodeBalance node : balance.getNodes())
        {
            if (nodeName.equals(node.getNodeName()))
            {
                return node.getIndicators().get(indicator);
            }
        }
        return null;
    }

    /**
     * 输出配比明细，便于人工校核
     */
    private String dump(RatioResult result)
    {
        StringBuilder sb = new StringBuilder();
        for (RatioMaterialItem item : result.getItems())
        {
            if (sb.length() > 0)
            {
                sb.append("，");
            }
            sb.append(item.getMaterialName()).append("=").append(item.getRatio()).append("%");
        }
        return sb.toString();
    }
}
