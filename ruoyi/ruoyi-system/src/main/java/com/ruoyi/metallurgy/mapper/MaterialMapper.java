package com.ruoyi.metallurgy.mapper;

import java.util.List;
import com.ruoyi.metallurgy.domain.Material;

/**
 * 物料台账 数据层
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
public interface MaterialMapper
{
    /**
     * 查询物料台账
     *
     * @param id 物料台账主键
     * @return 物料台账
     */
    public Material selectMaterialById(Long id);

    /**
     * 查询物料台账列表
     *
     * @param material 物料台账
     * @return 物料台账集合
     */
    public List<Material> selectMaterialList(Material material);

    /**
     * 统计物料条数（供首页概览使用，避免全量查询后在内存里取 size）
     *
     * @param material 查询条件
     * @return 条数
     */
    public long countMaterial(Material material);

    /**
     * 根据主键数组批量查询物料（供配料计算按勾选物料取数使用）
     *
     * @param ids 主键数组
     * @return 物料集合
     */
    public List<Material> selectMaterialByIds(Long[] ids);

    /**
     * 新增物料台账
     *
     * @param material 物料台账
     * @return 结果
     */
    public int insertMaterial(Material material);

    /**
     * 修改物料台账
     *
     * @param material 物料台账
     * @return 结果
     */
    public int updateMaterial(Material material);

    /**
     * 删除物料台账
     *
     * @param id 物料台账主键
     * @return 结果
     */
    public int deleteMaterialById(Long id);

    /**
     * 批量删除物料台账
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMaterialByIds(Long[] ids);
}
