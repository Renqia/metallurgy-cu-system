package com.ruoyi.web.controller.metallurgy;

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
import com.ruoyi.metallurgy.domain.CalcRecord;
import com.ruoyi.metallurgy.dto.RatioRequest;
import com.ruoyi.metallurgy.dto.RatioResult;
import com.ruoyi.metallurgy.service.IStreamCalcService;

/**
 * 铜闪速冶炼计算接口
 *
 * <p>提供两类计算入口：</p>
 * <ul>
 *   <li><b>配料计算</b>：在目标冰铜品位与目标硅铁比约束下求解各物料配比；</li>
 *   <li><b>全流程物料平衡计算</b>：按五个工序节点做元素守恒，输出产物量与工艺指标，
 *       并自动保存计算记录与物质流明细。</li>
 * </ul>
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
@RestController
@RequestMapping("/metallurgy/calc")
public class CalcController extends BaseController
{
    @Autowired
    private IStreamCalcService streamCalcService;

    /**
     * 配料计算（求解配比）
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:calc:ratio')")
    @Log(title = "配料计算", businessType = BusinessType.OTHER)
    @PostMapping("/ratio")
    public AjaxResult ratio(@RequestBody RatioRequest request)
    {
        RatioResult result = streamCalcService.calcRatio(request);
        return success(result);
    }

    /**
     * 全流程物料平衡计算
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:calc:balance')")
    @Log(title = "全流程物料平衡计算", businessType = BusinessType.OTHER)
    @PostMapping("/balance")
    public AjaxResult balance(@RequestBody RatioRequest request)
    {
        CalcRecord record = streamCalcService.calcBalance(request);
        return success(record);
    }
}
