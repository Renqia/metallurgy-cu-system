package com.ruoyi.web.controller.metallurgy;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.metallurgy.domain.StreamFlow;
import com.ruoyi.metallurgy.dto.BalanceResult;
import com.ruoyi.metallurgy.dto.SankeyData;
import com.ruoyi.metallurgy.service.IStreamCalcService;

/**
 * 物质流分析接口
 *
 * <p>基于已保存的计算记录提供：全流程结果明细、桑基图数据、物质流报表与 Excel 导出。
 * 该设计使物质流分析可随时回看历史工况，而无需重算。</p>
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
@RestController
@RequestMapping("/metallurgy/stream")
public class StreamController extends BaseController
{
    @Autowired
    private IStreamCalcService streamCalcService;

    /**
     * 查询全流程物料平衡结果
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:stream:view')")
    @GetMapping("/balance/{calcId}")
    public AjaxResult balance(@PathVariable("calcId") Long calcId)
    {
        BalanceResult result = streamCalcService.getBalanceResult(calcId);
        return success(result);
    }

    /**
     * 查询桑基图数据
     *
     * @param calcId 计算记录ID
     * @param element 元素口径（Cu/Fe/S/SiO2/CaO/合计），默认 Cu
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:stream:view')")
    @GetMapping("/sankey/{calcId}")
    public AjaxResult sankey(@PathVariable("calcId") Long calcId,
            @RequestParam(value = "element", required = false) String element)
    {
        SankeyData data = streamCalcService.buildSankey(calcId, element);
        return success(data);
    }

    /**
     * 查询物质流明细（报表数据）
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:stream:view')")
    @GetMapping("/flows/{calcId}")
    public AjaxResult flows(@PathVariable("calcId") Long calcId)
    {
        List<StreamFlow> flows = streamCalcService.getStreamFlows(calcId);
        return success(flows);
    }

    /**
     * 导出物质流明细报表
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:stream:export')")
    @Log(title = "物质流分析报表", businessType = BusinessType.EXPORT)
    @PostMapping("/export/{calcId}")
    public void export(HttpServletResponse response, @PathVariable("calcId") Long calcId)
    {
        List<StreamFlow> flows = streamCalcService.getStreamFlows(calcId);
        ExcelUtil<StreamFlow> util = new ExcelUtil<StreamFlow>(StreamFlow.class);
        util.exportExcel(response, flows, "物质流明细报表");
    }
}
