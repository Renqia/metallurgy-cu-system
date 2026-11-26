package com.ruoyi.metallurgy.calc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.metallurgy.dto.BalanceResult;
import com.ruoyi.metallurgy.dto.CalcOptions;
import com.ruoyi.metallurgy.dto.CompareResult;
import com.ruoyi.metallurgy.dto.IndicatorSeries;
import com.ruoyi.metallurgy.dto.RatioMaterialItem;
import com.ruoyi.metallurgy.dto.RatioRequest;
import com.ruoyi.metallurgy.dto.RatioResult;
import com.ruoyi.metallurgy.dto.SensitivityPoint;
import com.ruoyi.metallurgy.dto.SensitivityResult;

/**
 * 方案对比与敏感性分析引擎单元测试
 *
 * <p>全程为纯算法测试（无 Spring 上下文、无数据库读写），
 * 是《测试与验收.md》中 TC-601~TC-610 的自动化证据：</p>
 * <ul>
 *   <li>统一指标目录：条数、顺序、单位来源、空值语义；</li>
 *   <li>敏感性扫描：闭区间计数、趋势方向、失败点保留、参数校验、基线不可污染；</li>
 *   <li>模型边界：富氧不可扫描、转炉吹炼渣硅铁比与闪速熔炼硅铁比的区分。</li>
 * </ul>
 *
 * @author 【成员1】
 * @date 2026-10-14
 */
class AnalysisEngineTest
{
    /** 基准工况：目标冰铜品位 55%、目标硅铁比 1.10 */
    private static final BigDecimal GRADE = new BigDecimal("55");

    private static final BigDecimal FE_SIO2 = new BigDecimal("1.10");

    /**
     * 构造物料项
     */
    private RatioMaterialItem material(String name, String type, String cu, String fe, String s,
            String sio2, String cao)
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
     * 基线请求：模拟计算记录保存时刻的输入快照
     *
     * <p>配比与入库示例记录一致（铜精矿A/B 各 40%、石英石 5%、石灰石 3%、返料烟尘 12%），
     * 且全流程记录保存前会被锁定为 fixed，此处如实复现该状态。</p>
     */
    private RatioRequest baselineSnapshot()
    {
        List<RatioMaterialItem> items = new ArrayList<>();
        items.add(locked(material("铜精矿A（示例）", "铜精矿", "24.00", "26.00", "31.00", "6.00", "2.00"), "40.00"));
        items.add(locked(material("铜精矿B（示例）", "铜精矿", "28.00", "24.00", "30.00", "8.00", "1.50"), "40.00"));
        items.add(locked(material("石英石（熔剂）", "熔剂", "0.00", "1.50", "0.00", "92.00", "2.00"), "5.00"));
        items.add(locked(material("石灰石（熔剂）", "熔剂", "0.00", "0.50", "0.00", "3.00", "52.00"), "3.00"));
        items.add(locked(material("返料烟尘（闪速炉）", "返料", "12.00", "20.00", "6.00", "8.00", "3.00"), "12.00"));
        RatioRequest request = new RatioRequest();
        request.setItems(items);
        request.setMatteGrade(GRADE);
        request.setFeSio2Ratio(FE_SIO2);
        request.setPlanId(1L);
        request.setRemark("单元测试基线");
        return request;
    }

    /**
     * 把物料置为"已锁定配比"状态（与快照一致）
     */
    private RatioMaterialItem locked(RatioMaterialItem item, String ratio)
    {
        item.setRatio(new BigDecimal(ratio));
        item.setFixed(true);
        item.setFixedRatio(new BigDecimal(ratio));
        return item;
    }

    /**
     * 计算基线结果
     */
    private BalanceResult baselineBalance(RatioRequest request)
    {
        RatioResult ratio = RatioSolver.solve(request);
        return FlowCalculator.calculate(ratio, request.getOptions());
    }

    // ------------------------------------------------------------------
    // 统一指标目录
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TC-601 统一指标目录：19 项指标、名称唯一、顺序固定")
    void testIndicatorCatalog()
    {
        BalanceResult balance = baselineBalance(baselineSnapshot());
        List<IndicatorSeries> series = IndicatorCatalog.extract(Collections.singletonList(balance));

        assertEquals(20, series.size(), "指标目录应为 20 项，实际=" + series.size());
        assertEquals(IndicatorCatalog.names().size(), series.size(), "names() 与 extract() 条数应一致");
        List<String> seen = new ArrayList<>();
        for (IndicatorSeries s : series)
        {
            assertFalse(seen.contains(s.getName()), "指标名不得重复：" + s.getName());
            seen.add(s.getName());
            assertEquals(1, s.getValues().size(), "单列抽取时每个指标应恰好 1 个取值");
        }
        assertEquals("混合料Cu品位", series.get(0).getName(), "首项应为配料类指标，保证表格顺序稳定");
        assertEquals(IndicatorCatalog.names(), seen, "抽取顺序应与目录声明顺序一致");
        System.out.println("[TC-601] 指标目录：" + seen);
    }

    @Test
    @DisplayName("TC-602 指标单位优先取自结果快照登记的 indicatorUnits")
    void testIndicatorUnitFromSnapshot()
    {
        BalanceResult balance = baselineBalance(baselineSnapshot());
        List<IndicatorSeries> series = IndicatorCatalog.extract(Collections.singletonList(balance));

        assertEquals("%", unitOf(series, "冰铜品位"), "节点指标单位应取自快照");
        assertEquals("kg", unitOf(series, "冰铜量"), "节点指标单位应取自快照");
        assertEquals("kg/100kg混合料", unitOf(series, "SO2 总产量"), "顶层指标单位应取自快照");
        assertEquals("%", unitOf(series, IndicatorCatalog.INDICATOR_MAX_RESIDUAL), "快照未登记时使用内置单位");
        // 单个指标的取值应与快照一致，不做二次改写
        assertTrue(valueOf(series, "冰铜品位").subtract(new BigDecimal("55")).abs()
                .compareTo(new BigDecimal("0.01")) <= 0,
                "冰铜品位应等于目标值回填值，实际=" + valueOf(series, "冰铜品位"));
    }

    @Test
    @DisplayName("TC-603 空值语义：失败点/缺失列取值为 null，不补 0")
    void testNullValueNotZero()
    {
        BalanceResult balance = baselineBalance(baselineSnapshot());
        List<BalanceResult> columns = new ArrayList<>();
        columns.add(balance);
        columns.add(null);
        List<IndicatorSeries> series = IndicatorCatalog.extract(columns);

        for (IndicatorSeries s : series)
        {
            assertEquals(2, s.getValues().size(), "两列对比时每个指标应有 2 个取值位");
            assertNotNull(s.getValues().get(0), "有结果的列不应为空：" + s.getName());
            assertNull(s.getValues().get(1), "无结果的列必须为 null（前端显示—），不得补 0：" + s.getName());
        }
        assertNotNull(IndicatorCatalog.extractOne(balance).get("冰铜量"), "单结果抽取应给出基线值");
    }

    // ------------------------------------------------------------------
    // 敏感性分析
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TC-604 目标冰铜品位扫描：闭区间计数正确、含终点")
    void testScanPointCountInclusive()
    {
        SensitivityResult result = SensitivityCalculator.scan(baselineSnapshot(),
                SensitivityCalculator.FIELD_MATTE_GRADE,
                new BigDecimal("50"), new BigDecimal("60"), new BigDecimal("5"));

        assertEquals(3, result.getPoints().size(), "50~60 步长 5 应为 3 个点（含终点）");
        assertEquals(0, result.getPoints().get(0).getValue().compareTo(new BigDecimal("50")));
        assertEquals(0, result.getPoints().get(2).getValue().compareTo(new BigDecimal("60")),
                "末点应恰好落在终点上");
        assertEquals(IndicatorCatalog.names().size(), result.getIndicators().size(), "指标曲线应齐全");
        for (IndicatorSeries s : result.getIndicators())
        {
            assertEquals(3, s.getValues().size(), "曲线取值数应与扫描点数一致：" + s.getName());
        }
        assertEquals("目标冰铜品位", result.getFieldLabel());
        assertEquals("%", result.getFieldUnit());
    }

    @Test
    @DisplayName("TC-605 品位扫描趋势：品位提高则冰铜量下降、渣量上升，与既有规律一致")
    void testScanGradeTrend()
    {
        SensitivityResult result = SensitivityCalculator.scan(baselineSnapshot(),
                SensitivityCalculator.FIELD_MATTE_GRADE,
                new BigDecimal("50"), new BigDecimal("62"), new BigDecimal("2"));

        assertEquals(7, result.getPoints().size(), "50~62 步长 2 应为 7 个点");
        assertEquals(7, result.getSuccessCount(), "该区间应全部可算");
        assertEquals(0, result.getFailedCount());
        BigDecimal matte50 = valueAt(result, "冰铜量", 0);
        BigDecimal matte62 = valueAt(result, "冰铜量", 6);
        BigDecimal slag50 = valueAt(result, "炉渣量", 0);
        BigDecimal slag62 = valueAt(result, "炉渣量", 6);
        assertTrue(matte62.compareTo(matte50) < 0, "品位提高后冰铜量应下降：50%→" + matte50 + "，62%→" + matte62);
        assertTrue(slag62.compareTo(slag50) > 0, "品位提高后渣量应上升：50%→" + slag50 + "，62%→" + slag62);
        System.out.println("[TC-605] 冰铜量 " + matte50 + " → " + matte62 + "；炉渣量 " + slag50 + " → " + slag62);
    }

    @Test
    @DisplayName("TC-606 工艺不可行点：保留该点、指标为空、原因非空，不影响其它点")
    void testScanKeepsFailedPoints()
    {
        // 80% 与 85% 超过 Cu2S 物相模型上限（79.86%），应作为不可行点保留
        SensitivityResult result = SensitivityCalculator.scan(baselineSnapshot(),
                SensitivityCalculator.FIELD_MATTE_GRADE,
                new BigDecimal("70"), new BigDecimal("85"), new BigDecimal("5"));

        assertEquals(4, result.getPoints().size(), "70~85 步长 5 应为 4 个点");
        assertEquals(2, result.getSuccessCount(), "70%、75% 两点应可算");
        assertEquals(2, result.getFailedCount(), "80%、85% 两点应不可行");
        for (SensitivityPoint point : result.getPoints())
        {
            assertNotNull(point.getValue(), "失败点仍必须保留扫描值，不得丢弃该点");
            if (!point.isSuccess())
            {
                assertNotNull(point.getMessage(), "失败点必须给出中文原因");
                assertTrue(point.getMessage().contains("79.86"), "原因应说明物相模型上限，实际=" + point.getMessage());
            }
        }
        // 失败点对应指标为空，成功点仍有值
        IndicatorSeries matteMass = seriesOf(result.getIndicators(), "冰铜量");
        assertNotNull(matteMass.getValues().get(0), "成功点应有取值");
        assertNull(matteMass.getValues().get(2), "失败点指标必须为 null（折线断开，不补 0）");
        assertNull(matteMass.getValues().get(3), "失败点指标必须为 null（折线断开，不补 0）");
    }

    @Test
    @DisplayName("TC-607 转炉吹炼渣硅铁比扫描：硅铁比提高则石英石加入量下降（真实参与计算）")
    void testScanBlisterFeSio2()
    {
        RatioRequest base = baselineSnapshot();
        SensitivityResult result = SensitivityCalculator.scan(base,
                SensitivityCalculator.FIELD_BLISTER_FE_SIO2,
                new BigDecimal("1.40"), new BigDecimal("2.20"), new BigDecimal("0.20"));

        assertEquals(5, result.getPoints().size(), "1.40~2.20 步长 0.20 应为 5 个点");
        assertEquals(5, result.getSuccessCount(), "该区间应全部可算");
        assertEquals("转炉吹炼渣硅铁比", result.getFieldLabel());
        assertEquals("", result.getFieldUnit(), "硅铁比为无量纲");

        BigDecimal quartz14 = valueAt(result, "石英石加入量", 0);
        BigDecimal quartz22 = valueAt(result, "石英石加入量", 4);
        BigDecimal slag14 = valueAt(result, "吹炼渣量", 0);
        BigDecimal slag22 = valueAt(result, "吹炼渣量", 4);
        assertTrue(quartz22.compareTo(quartz14) < 0,
                "渣硅铁比提高后外加石英石应减少：1.40→" + quartz14 + "，2.20→" + quartz22);
        assertTrue(slag22.compareTo(slag14) < 0,
                "渣硅铁比提高后吹炼渣量应减少：1.40→" + slag14 + "，2.20→" + slag22);
        assertTrue(containsNote(result, "转炉吹炼渣硅铁比"), "提示应明确是转炉吹炼渣硅铁比");
        assertTrue(containsNote(result, "富氧"), "提示应说明富氧不参与计算、不作为扫描字段");
        System.out.println("[TC-607] 石英石加入量 " + quartz14 + " → " + quartz22
                + "；吹炼渣量 " + slag14 + " → " + slag22);
    }

    @Test
    @DisplayName("TC-608 基线快照缺少模型参数：按默认参数重算并给出说明")
    void testScanWithoutOptions()
    {
        RatioRequest base = baselineSnapshot();
        assertNull(base.getOptions(), "本用例基线故意不含 CalcOptions，模拟历史快照");
        SensitivityResult result = SensitivityCalculator.scan(base,
                SensitivityCalculator.FIELD_BLISTER_FE_SIO2,
                new BigDecimal("1.60"), new BigDecimal("2.00"), new BigDecimal("0.20"));

        assertEquals(0, result.getBaseValue().compareTo(MetallurgyConstants.DEFAULT_BLISTER_FE_SIO2),
                "无模型参数时基线值应取默认吹炼渣硅铁比 1.80");
        assertTrue(containsNote(result, "未保存模型参数"), "应提示基线未保存模型参数");
        assertTrue(containsNote(result, "默认"), "应提示按默认参数参与计算");
    }

    @Test
    @DisplayName("TC-609 扫描参数非法：给出明确中文提示（步长/区间/字段/点数上限）")
    void testScanValidation()
    {
        RatioRequest base = baselineSnapshot();

        ServiceException stepEx = assertThrows(ServiceException.class,
                () -> SensitivityCalculator.scan(base, SensitivityCalculator.FIELD_MATTE_GRADE,
                        new BigDecimal("50"), new BigDecimal("60"), BigDecimal.ZERO));
        assertTrue(stepEx.getMessage().contains("步长"), "应提示步长必须大于 0");

        ServiceException orderEx = assertThrows(ServiceException.class,
                () -> SensitivityCalculator.scan(base, SensitivityCalculator.FIELD_MATTE_GRADE,
                        new BigDecimal("60"), new BigDecimal("50"), new BigDecimal("1")));
        assertTrue(orderEx.getMessage().contains("起点必须不大于终点"), "应提示区间顺序");

        ServiceException fieldEx = assertThrows(ServiceException.class,
                () -> SensitivityCalculator.scan(base, "oxygenConc",
                        new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("0.1")));
        assertTrue(fieldEx.getMessage().contains("不支持的扫描字段"), "应拒绝富氧浓度等非扫描字段");

        ServiceException countEx = assertThrows(ServiceException.class,
                () -> SensitivityCalculator.scan(base, SensitivityCalculator.FIELD_MATTE_GRADE,
                        new BigDecimal("30"), new BigDecimal("90"), new BigDecimal("1")));
        assertTrue(countEx.getMessage().contains("50"), "应提示扫描点数上限，实际=" + countEx.getMessage());

        ServiceException nullEx = assertThrows(ServiceException.class,
                () -> SensitivityCalculator.scan(base, SensitivityCalculator.FIELD_MATTE_GRADE,
                        null, new BigDecimal("60"), new BigDecimal("1")));
        assertTrue(nullEx.getMessage().contains("请完整填写"), "应提示参数缺失");
    }

    @Test
    @DisplayName("TC-610 扫描不污染基线：基线请求的配比与模型参数保持原样")
    void testScanDoesNotMutateBase()
    {
        RatioRequest base = baselineSnapshot();
        List<BigDecimal> before = new ArrayList<>();
        for (RatioMaterialItem item : base.getItems())
        {
            before.add(item.getRatio());
        }
        SensitivityCalculator.scan(base, SensitivityCalculator.FIELD_MATTE_GRADE,
                new BigDecimal("50"), new BigDecimal("60"), new BigDecimal("5"));
        SensitivityCalculator.scan(base, SensitivityCalculator.FIELD_BLISTER_FE_SIO2,
                new BigDecimal("1.60"), new BigDecimal("2.00"), new BigDecimal("0.20"));

        assertEquals(0, base.getMatteGrade().compareTo(GRADE), "基线目标品位不应被扫描改写");
        assertNull(base.getOptions(), "基线模型参数不应被扫描写入");
        for (int i = 0; i < before.size(); i++)
        {
            assertEquals(0, base.getItems().get(i).getRatio().compareTo(before.get(i)),
                    "基线配比不应被扫描改写：" + base.getItems().get(i).getMaterialName());
        }
    }

    @Test
    @DisplayName("TC-611 配比锁定规则：全正且合计 100% 时按锁定处理，不重新分配配比")
    void testLockedRatioRule()
    {
        RatioRequest request = baselineSnapshot();
        RatioRequestUtils.applyLockedRatioIfComplete(request);
        for (RatioMaterialItem item : request.getItems())
        {
            assertTrue(item.isFixed(), "配比完整时应锁定");
            assertEquals(0, item.getFixedRatio().compareTo(item.getRatio()), "锁定值应等于给定配比");
        }
        // 配比合计不足 100% 时不锁定，交由求解器分配
        RatioRequest partial = baselineSnapshot();
        partial.getItems().get(0).setRatio(new BigDecimal("10"));
        partial.getItems().get(0).setFixed(false);
        RatioRequestUtils.applyLockedRatioIfComplete(partial);
        assertFalse(partial.getItems().get(0).isFixed(), "配比不足 100% 时不应锁定");
    }

    // ------------------------------------------------------------------
    // 辅助方法
    // ------------------------------------------------------------------

    private String unitOf(List<IndicatorSeries> series, String name)
    {
        return seriesOf(series, name).getUnit();
    }

    private BigDecimal valueOf(List<IndicatorSeries> series, String name)
    {
        return seriesOf(series, name).getValues().get(0);
    }

    private IndicatorSeries seriesOf(List<IndicatorSeries> series, String name)
    {
        for (IndicatorSeries s : series)
        {
            if (name.equals(s.getName()))
            {
                return s;
            }
        }
        throw new AssertionError("指标目录中不存在：" + name);
    }

    private BigDecimal valueAt(SensitivityResult result, String name, int index)
    {
        IndicatorSeries series = seriesOf(result.getIndicators(), name);
        BigDecimal value = series.getValues().get(index);
        assertNotNull(value, "指标【" + name + "】在第 " + index + " 个扫描点应有取值");
        return value;
    }

    private boolean containsNote(SensitivityResult result, String keyword)
    {
        for (String note : result.getNotes())
        {
            if (note.contains(keyword))
            {
                return true;
            }
        }
        return false;
    }

    @Test
    @DisplayName("TC-612 对比结果组装：列信息与指标列数一致（服务层契约自检）")
    void testCompareResultShape()
    {
        BalanceResult balance = baselineBalance(baselineSnapshot());
        CompareResult result = new CompareResult();
        result.setColumns(new ArrayList<>());
        result.setIndicators(IndicatorCatalog.extract(Arrays.asList(balance, balance, balance)));

        assertEquals(IndicatorCatalog.names().size(), result.getIndicators().size(), "对比指标行数与目录一致");
        for (IndicatorSeries series : result.getIndicators())
        {
            assertEquals(3, series.getValues().size(), "每行取值数应与勾选记录数一致：" + series.getName());
        }
        // 同一份结果对比自身时，各列取值必须完全相同（保证"顺序一致"）
        for (IndicatorSeries series : result.getIndicators())
        {
            List<BigDecimal> values = series.getValues();
            if (values.get(0) != null)
            {
                assertEquals(0, values.get(0).compareTo(values.get(1)), "同源记录取值应一致：" + series.getName());
            }
        }
        // CalcOptions 类型在扫描中必须可被正常构造（防止默认值回退被破坏）
        assertNotNull(new CalcOptions().getBlisterFeSio2());
    }
}
