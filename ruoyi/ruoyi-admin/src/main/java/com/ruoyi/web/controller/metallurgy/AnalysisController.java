package com.ruoyi.web.controller.metallurgy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.metallurgy.dto.CompareColumn;
import com.ruoyi.metallurgy.dto.CompareExportRow;
import com.ruoyi.metallurgy.dto.CompareRequest;
import com.ruoyi.metallurgy.dto.CompareResult;
import com.ruoyi.metallurgy.dto.IndicatorSeries;
import com.ruoyi.metallurgy.dto.SensitivityExportRow;
import com.ruoyi.metallurgy.dto.SensitivityPoint;
import com.ruoyi.metallurgy.dto.SensitivityRequest;
import com.ruoyi.metallurgy.dto.SensitivityResult;
import com.ruoyi.metallurgy.service.IAnalysisService;

/**
 * 方案对比与敏感性分析
 *
 * <p>基于计算记录的快照做<b>只读</b>分析，不新增数据表、不写入业务数据：</p>
 * <ul>
 *   <li>方案对比：勾选 2~4 条全流程记录，并列比较统一指标目录下的各项指标；</li>
 *   <li>敏感性分析：以一条全流程记录为基线，对目标冰铜品位或转炉吹炼渣硅铁比逐点真实重算。</li>
 * </ul>
 *
 * <p>权限：分析与导出使用独立的按钮权限（{@code metallurgy:record:compare} /
 * {@code metallurgy:record:compareExport} / {@code metallurgy:analysis:sensitivity} /
 * {@code metallurgy:analysis:export}），<b>后端独立校验，不依赖前端按钮隐藏</b>。</p>
 *
 * @author 【成员1】
 * @date 2026-10-14
 */
@RestController
@RequestMapping("/metallurgy/analysis")
public class AnalysisController extends BaseController
{
    /** 导出时为固定 4 个方案列留位（对比最多 4 条记录） */
    private static final int EXPORT_COLUMNS = 4;

    /** 无数据显示占位符（与前端一致：不补 0） */
    private static final String EMPTY_CELL = "—";

    @Autowired
    private IAnalysisService analysisService;

    /**
     * 多方案指标对比
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:record:compare')")
    @PostMapping("/compare")
    public AjaxResult compare(@RequestBody CompareRequest request)
    {
        return success(analysisService.compare(request));
    }

    /**
     * 导出多方案对比结果
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:record:compareExport')")
    @Log(title = "方案对比", businessType = BusinessType.EXPORT)
    @PostMapping("/compare/export")
    public void compareExport(HttpServletResponse response, @RequestBody CompareRequest request)
    {
        CompareResult result = analysisService.compare(request);
        ExcelUtil<CompareExportRow> util = new ExcelUtil<CompareExportRow>(CompareExportRow.class);
        util.exportExcel(response, buildCompareRows(result), "方案对比");
    }

    /**
     * 敏感性分析（逐点重算）
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:analysis:sensitivity')")
    @PostMapping("/sensitivity")
    public AjaxResult sensitivity(@RequestBody SensitivityRequest request)
    {
        return success(analysisService.sensitivity(request));
    }

    /**
     * 导出敏感性分析结果
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:analysis:export')")
    @Log(title = "敏感性分析", businessType = BusinessType.EXPORT)
    @PostMapping("/sensitivity/export")
    public void sensitivityExport(HttpServletResponse response, @RequestBody SensitivityRequest request)
    {
        SensitivityResult result = analysisService.sensitivity(request);
        ExcelUtil<SensitivityExportRow> util = new ExcelUtil<SensitivityExportRow>(SensitivityExportRow.class);
        util.exportExcel(response, buildSensitivityRows(result), "敏感性分析");
    }

    /**
     * 组装对比导出行：前几行为列头信息（方案名/时间/记录ID/约束），其后为各指标行。
     * 指标名称、单位、顺序与页面表格、图表完全同源（均来自 {@code IndicatorCatalog}）。
     */
    private List<CompareExportRow> buildCompareRows(CompareResult result)
    {
        List<CompareExportRow> rows = new ArrayList<>();
        List<CompareColumn> columns = result.getColumns();

        List<String> planNames = new ArrayList<>();
        List<String> calcTimes = new ArrayList<>();
        List<String> calcIds = new ArrayList<>();
        List<String> feasibles = new ArrayList<>();
        for (CompareColumn column : columns)
        {
            planNames.add(text(column.getPlanName()));
            calcTimes.add(text(column.getCalcTime()));
            calcIds.add(column.getCalcId() == null ? EMPTY_CELL : String.valueOf(column.getCalcId()));
            feasibles.add(column.isFeasible() ? "是" : "否");
        }
        rows.add(toCompareRow("参数方案", "", planNames));
        rows.add(toCompareRow("计算时间", "", calcTimes));
        rows.add(toCompareRow("计算记录ID", "", calcIds));
        rows.add(toCompareRow("是否满足全部约束", "", feasibles));

        for (IndicatorSeries series : result.getIndicators())
        {
            List<String> values = new ArrayList<>();
            for (BigDecimal value : series.getValues())
            {
                values.add(format(value));
            }
            rows.add(toCompareRow(series.getName(), text(series.getUnit()), values));
        }
        return rows;
    }

    /**
     * 组装敏感性分析导出行：一行 = 一个扫描点，工艺不可行点保留该行并给出原因
     */
    private List<SensitivityExportRow> buildSensitivityRows(SensitivityResult result)
    {
        List<SensitivityExportRow> rows = new ArrayList<>();
        List<SensitivityPoint> points = result.getPoints();
        for (int i = 0; i < points.size(); i++)
        {
            SensitivityPoint point = points.get(i);
            SensitivityExportRow row = new SensitivityExportRow();
            row.setScanValue(format(point.getValue()));
            row.setFeasible(point.isSuccess() ? "是" : "否");
            row.setMatteGrade(valueAt(result.getIndicators(), "冰铜品位", i));
            row.setMatteMass(valueAt(result.getIndicators(), "冰铜量", i));
            row.setSlagMass(valueAt(result.getIndicators(), "炉渣量", i));
            row.setDirectYield(valueAt(result.getIndicators(), "铜直收率", i));
            row.setBlisterMass(valueAt(result.getIndicators(), "粗铜量", i));
            row.setQuartzMass(valueAt(result.getIndicators(), "石英石加入量", i));
            row.setCathodeMass(valueAt(result.getIndicators(), "阴极铜量", i));
            row.setTotalRecovery(valueAt(result.getIndicators(), "全流程铜总回收率", i));
            row.setSo2Total(valueAt(result.getIndicators(), "SO2 总产量", i));
            row.setMaxResidual(valueAt(result.getIndicators(), "最大元素平衡残差", i));
            row.setMessage(text(point.getMessage()));
            rows.add(row);
        }
        return rows;
    }

    /**
     * 取某指标在第 index 个扫描点上的取值（无该指标或无数据时返回占位符）
     */
    private String valueAt(List<IndicatorSeries> indicators, String name, int index)
    {
        if (indicators == null)
        {
            return EMPTY_CELL;
        }
        for (IndicatorSeries series : indicators)
        {
            if (name.equals(series.getName()))
            {
                List<BigDecimal> values = series.getValues();
                if (values == null || index >= values.size())
                {
                    return EMPTY_CELL;
                }
                return format(values.get(index));
            }
        }
        return EMPTY_CELL;
    }

    /**
     * 组装一行固定 4 列的对比导出数据（不足 4 列时留空）
     */
    private CompareExportRow toCompareRow(String item, String unit, List<String> values)
    {
        String[] cells = new String[EXPORT_COLUMNS];
        for (int i = 0; i < EXPORT_COLUMNS; i++)
        {
            cells[i] = values != null && i < values.size() ? values.get(i) : "";
        }
        return new CompareExportRow(item, unit, cells[0], cells[1], cells[2], cells[3]);
    }

    /**
     * 数值格式化：保留快照原始精度（不做四舍五入改写），null 显示占位符
     */
    private String format(BigDecimal value)
    {
        return value == null ? EMPTY_CELL : value.toPlainString();
    }

    private String text(String value)
    {
        return value == null ? "" : value;
    }
}
