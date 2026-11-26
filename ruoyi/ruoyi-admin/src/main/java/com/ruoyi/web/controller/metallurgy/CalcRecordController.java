package com.ruoyi.web.controller.metallurgy;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.metallurgy.domain.CalcRecord;
import com.ruoyi.metallurgy.service.ICalcRecordService;
import com.ruoyi.metallurgy.support.RecordScope;

/**
 * 计算记录管理
 *
 * <p>记录由配料计算与全流程平衡计算自动产生，本接口用于查询、查看快照与删除，
 * 保证任何工艺计算结果都可追溯与复现。</p>
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
@RestController
@RequestMapping("/metallurgy/record")
public class CalcRecordController extends BaseController
{
    @Autowired
    private ICalcRecordService calcRecordService;

    /**
     * 查询计算记录列表
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:record:list')")
    @GetMapping("/list")
    public TableDataInfo list(CalcRecord calcRecord)
    {
        startPage();
        List<CalcRecord> list = calcRecordService.selectCalcRecordList(calcRecord);
        return getDataTable(list);
    }

    /**
     * 查询计算记录详情（含输入/结果快照）
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:record:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        CalcRecord record = calcRecordService.selectCalcRecordById(id);
        // 数据可见范围：列表已按归属过滤，此处防止"用编号直接打开他人记录"绕过列表
        if (record != null && !RecordScope.canView(record))
        {
            throw new ServiceException("没有权限查看该计算记录");
        }
        return success(record);
    }

    /**
     * 删除计算记录
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:record:remove')")
    @Log(title = "计算记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(calcRecordService.deleteCalcRecordByIds(ids));
    }

    /**
     * 导出计算记录列表
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:record:export')")
    @Log(title = "计算记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CalcRecord calcRecord)
    {
        List<CalcRecord> list = calcRecordService.selectCalcRecordList(calcRecord);
        ExcelUtil<CalcRecord> util = new ExcelUtil<CalcRecord>(CalcRecord.class);
        util.exportExcel(response, list, "计算记录数据");
    }
}
