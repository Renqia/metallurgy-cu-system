package com.ruoyi.metallurgy.service;

import java.util.List;
import com.ruoyi.metallurgy.domain.Material;

/**
 * 物料台账 服务层
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
public interface IMaterialService
{
    /**
     * 查询物料台账列表
     *
     * @param material 物料台账
     * @return 物料台账集合
     */
    public List<Material> selectMaterialList(Material material);

    /**
     * 统计物料条数（供首页概览使用）
     *
     * @param material 查询条件
     * @return 条数
     */
    public long countMaterial(Material material);

    /**
     * 查询物料台账
     *
     * @param id 物料台账主键
     * @return 物料台账
     */
    public Material selectMaterialById(Long id);

    /**
     * 根据主键数组批量查询物料（供配料计算取值）
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
     * 批量删除物料台账
     *
     * @param ids 需要删除的物料台账主键集合
     * @return 结果
     */
    public int deleteMaterialByIds(Long[] ids);

    /**
     * 删除物料台账信息
     *
     * @param id 物料台账主键
     * @return 结果
     */
    public int deleteMaterialById(Long id);

    /**
     * 批量导入物料数据（Excel 导入）
     *
     * @param list 物料数据集合
     * @param updateSupport 是否更新已存在的物料
     * @param operName 操作人
     * @return 导入结果消息
     */
    public String importMaterial(List<Material> list, Boolean updateSupport, String operName);

    /**
     * 校验物料成分合理性
     *
     * <p>校验规则：① 各成分取值须在 0~100 之间；② Cu+Fe+S+SiO2+CaO 合计不得超过 100%
     * （超过说明数据录入有误，因为还包含氧、其他杂质等未列项，合计不可能大于 100%）。
     * 校验不通过时抛出 ServiceException，由全局异常处理器返回中文提示。</p>
     *
     * @param material 物料台账
     */
    public void checkMaterialComposition(Material material);
}
