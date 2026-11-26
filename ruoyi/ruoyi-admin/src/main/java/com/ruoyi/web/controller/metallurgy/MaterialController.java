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
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.metallurgy.domain.Material;
import com.ruoyi.metallurgy.service.IMaterialService;

/**
 * 物料台账管理
 *
 * <p>维护铜闪速冶炼所用物料（铜精矿、熔剂、返料等）的化学成分与单价库存，
 * 是配料计算与全流程物料平衡计算的数据基础。</p>
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
@RestController
@RequestMapping("/metallurgy/material")
public class MaterialController extends BaseController
{
    @Autowired
    private IMaterialService materialService;

    /**
     * 查询物料台账列表
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:material:list')")
    @GetMapping("/list")
    public TableDataInfo list(Material material)
    {
        startPage();
        List<Material> list = materialService.selectMaterialList(material);
        return getDataTable(list);
    }

    /**
     * 导出物料台账列表
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:material:export')")
    @Log(title = "物料台账", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Material material)
    {
        List<Material> list = materialService.selectMaterialList(material);
        ExcelUtil<Material> util = new ExcelUtil<Material>(Material.class);
        util.exportExcel(response, list, "物料台账数据");
    }

    /**
     * 获取物料台账详细信息
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:material:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(materialService.selectMaterialById(id));
    }

    /**
     * 新增物料台账
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:material:add')")
    @Log(title = "物料台账", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Material material)
    {
        material.setCreateBy(getUsername());
        return toAjax(materialService.insertMaterial(material));
    }

    /**
     * 修改物料台账
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:material:edit')")
    @Log(title = "物料台账", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Material material)
    {
        material.setUpdateBy(getUsername());
        return toAjax(materialService.updateMaterial(material));
    }

    /**
     * 删除物料台账
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:material:remove')")
    @Log(title = "物料台账", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(materialService.deleteMaterialByIds(ids));
    }

    /**
     * 导入物料台账数据
     */
    @PreAuthorize("@ss.hasPermi('metallurgy:material:import')")
    @Log(title = "物料台账", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<Material> util = new ExcelUtil<Material>(Material.class);
        List<Material> materialList = util.importExcel(file.getInputStream());
        String message = materialService.importMaterial(materialList, updateSupport, getUsername());
        return success(message);
    }

    /**
     * 下载物料导入模板
     */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<Material> util = new ExcelUtil<Material>(Material.class);
        util.importTemplateExcel(response, "物料台账数据");
    }
}
