package com.ruoyi.web.controller.metallurgy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.metallurgy.domain.CalcRecord;
import com.ruoyi.metallurgy.domain.Material;
import com.ruoyi.metallurgy.domain.ProcessParam;
import com.ruoyi.metallurgy.service.ICalcRecordService;
import com.ruoyi.metallurgy.service.IMaterialService;
import com.ruoyi.metallurgy.service.IProcessParamService;
import com.ruoyi.metallurgy.support.RecordScope;

/**
 * 系统概览接口（首页统计数据）
 *
 * <p>仅要求登录即可访问，不挂按钮权限——概览数据是所有登录用户进入系统后的第一屏内容。</p>
 *
 * <p>数据可见范围：与计算记录列表保持同一口径（非超级管理员仅统计/展示本人创建的记录），
 * 因此概览不会绕过记录可见范围。</p>
 *
 * <p>空数据与异常场景的处理约定（前端据此渲染）：</p>
 * <ul>
 *   <li>空库：各计数返回 0，<code>hasFlowRecord=false</code>，<code>last*</code> 四个键<b>不写入</b>
 *       （而非返回 0），前端显示"暂无记录"或"—"，不伪造 0；</li>
 *   <li>最近记录缺失字段：对应键为 null，前端显示"—"（例如历史记录未保存铜直收率时）；</li>
 *   <li>概览只读取计算记录的汇总列，<b>不解析快照 JSON</b>，因此损坏的快照不会影响首页渲染；
 *       快照的解析与诊断在"计算记录详情 / 方案对比 / 敏感性分析"入口进行，并有中文错误提示。</li>
 * </ul>
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/metallurgy/overview")
public class OverviewController extends BaseController
{
    /** 默认返回的最近记录条数 */
    private static final int DEFAULT_RECENT_LIMIT = 5;

    /** 最近记录条数上限 */
    private static final int MAX_RECENT_LIMIT = 20;

    @Autowired
    private IMaterialService materialService;

    @Autowired
    private IProcessParamService processParamService;

    @Autowired
    private ICalcRecordService calcRecordService;

    /**
     * 概览统计：物料数、方案数、计算记录数、全流程记录数，以及最近一次全流程计算的关键结果
     */
    @GetMapping("/summary")
    public AjaxResult summary()
    {
        Map<String, Object> data = new HashMap<>();
        // 计数一律使用数据库 COUNT，避免把整表读入内存后取 size（记录规模增大时不随数据量线性退化）
        data.put("materialCount", materialService.countMaterial(new Material()));
        data.put("paramCount", processParamService.countProcessParam(new ProcessParam()));
        data.put("recordCount", calcRecordService.countCalcRecord(new CalcRecord()));
        data.put("scope", RecordScope.isAdmin() ? "all" : "self");

        CalcRecord flowQuery = new CalcRecord();
        flowQuery.setCalcType("2"); // 全流程平衡
        data.put("flowCount", calcRecordService.countCalcRecord(flowQuery));

        // 由数据库层 LIMIT 1 取最近一条，避免全量查询后在内存中取首条
        List<CalcRecord> latest = calcRecordService.selectBalanceRecordTop(1);
        if (latest.isEmpty())
        {
            // 空库/无全流程记录：不写入 last* 键，由前端显示"暂无记录"，避免伪造 0
            data.put("hasFlowRecord", false);
            data.put("emptyHint", "暂无全流程计算记录，请先完成配料计算与全流程物料平衡计算");
        }
        else
        {
            CalcRecord last = latest.get(0);
            data.put("hasFlowRecord", true);
            data.put("lastRecordId", last.getId());
            // 可能为 null（历史记录未保存该指标）：保持 null 下发，前端显示"—"
            data.put("lastDirectYield", last.getDirectYield());
            data.put("lastMatteGrade", last.getMatteGradeCalc());
            data.put("lastSlagFeSio2", last.getSlagFeSio2());
            // Map 中的 Date 不走实体上的 @JsonFormat，此处手动格式化，避免前端显示 ISO 字符串
            data.put("lastCalcTime", last.getCalcTime() == null ? null
                    : DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, last.getCalcTime()));
            data.put("lastPlanName", last.getPlanName());
        }
        return AjaxResult.success(data);
    }

    /**
     * 最近 N 条全流程计算记录摘要（用于首页"最近计算"列表）
     */
    @GetMapping("/recent")
    public AjaxResult recent(@RequestParam(defaultValue = "5") int limit)
    {
        if (limit < 1 || limit > MAX_RECENT_LIMIT)
        {
            limit = DEFAULT_RECENT_LIMIT;
        }
        // 数据库层 LIMIT，且遵循记录可见范围
        List<CalcRecord> flows = calcRecordService.selectBalanceRecordTop(limit);

        List<Map<String, Object>> rows = new ArrayList<>();
        for (CalcRecord record : flows)
        {
            Map<String, Object> row = new HashMap<>();
            row.put("id", record.getId());
            row.put("planName", record.getPlanName());
            row.put("mixedCu", record.getMixedCu());
            row.put("matteGradeCalc", record.getMatteGradeCalc());
            row.put("slagFeSio2", record.getSlagFeSio2());
            row.put("directYield", record.getDirectYield());
            row.put("calcTime", record.getCalcTime() == null ? null
                    : DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, record.getCalcTime()));
            row.put("remark", record.getRemark());
            rows.add(row);
        }
        return AjaxResult.success(rows);
    }
}
