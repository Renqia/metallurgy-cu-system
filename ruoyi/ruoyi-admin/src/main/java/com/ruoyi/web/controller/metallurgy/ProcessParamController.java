package com.ruoyi.web.controller.metallurgy;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.metallurgy.domain.ProcessParam;
import com.ruoyi.metallurgy.service.IProcessParamService;

/**
 * 工艺参数方案管理
 *
 * <p>维护铜闪速熔炼的工艺控制参数（富氧浓度、氧料比、硅铁比、目标冰铜品位、处理量）。
 * 参数越界（超出文献参考区间）时**只提示不阻断保存**，因为现场工况可能超出文献范围，
 * 提示信息通过返回体的 <code>warnings</code> 字段下发，由前端以黄色提示条展示。</p>
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
@RestController
@RequestMapping("/metallurgy/param")
public class ProcessParamController extends BaseController
{
    @Autowired
    private IProcessParamService processParamService;

    /**
     * 查询工艺参数方案列表
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:param:list')")
    @GetMapping("/list")
    public TableDataInfo list(ProcessParam processParam)
    {
        startPage();
        List<ProcessParam> list = processParamService.selectProcessParamList(processParam);
        return getDataTable(list);
    }

    /**
     * 查询启用状态的参数方案（供配料计算/全流程平衡页面下拉选择）
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:param:list')")
    @GetMapping("/enabled")
    public AjaxResult enabled()
    {
        return success(processParamService.selectEnabledProcessParam());
    }

    /**
     * 查询参数参考区间（供表单实时提示）
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:param:list')")
    @GetMapping("/ranges")
    public AjaxResult ranges()
    {
        return success(processParamService.getParamRanges());
    }

    /**
     * 导出工艺参数方案列表
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:param:export')")
    @Log(title = "工艺参数方案", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ProcessParam processParam)
    {
        List<ProcessParam> list = processParamService.selectProcessParamList(processParam);
        ExcelUtil<ProcessParam> util = new ExcelUtil<ProcessParam>(ProcessParam.class);
        util.exportExcel(response, list, "工艺参数方案数据");
    }

    /**
     * 获取工艺参数方案详细信息
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:param:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(processParamService.selectProcessParamById(id));
    }

    /**
     * 新增工艺参数方案
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:param:add')")
    @Log(title = "工艺参数方案", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ProcessParam processParam)
    {
        processParam.setCreateBy(getUsername());
        int rows = processParamService.insertProcessParam(processParam);
        return toAjax(rows).put("warnings", processParamService.getParamWarnings(processParam));
    }

    /**
     * 修改工艺参数方案
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:param:edit')")
    @Log(title = "工艺参数方案", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ProcessParam processParam)
    {
        processParam.setUpdateBy(getUsername());
        int rows = processParamService.updateProcessParam(processParam);
        return toAjax(rows).put("warnings", processParamService.getParamWarnings(processParam));
    }

    /**
     * 删除工艺参数方案
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:param:remove')")
    @Log(title = "工艺参数方案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(processParamService.deleteProcessParamByIds(ids));
    }
}
