package com.ruoyi.metallurgy.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.metallurgy.domain.Material;
import com.ruoyi.metallurgy.mapper.MaterialMapper;
import com.ruoyi.metallurgy.service.IMaterialService;

/**
 * 物料台账 服务层实现
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
@Service
public class MaterialServiceImpl implements IMaterialService
{
    /** 成分合计上限（%） */
    private static final BigDecimal COMPOSITION_LIMIT = new BigDecimal("100");

    /** 成分最小值（%） */
    private static final BigDecimal CONTENT_MIN = BigDecimal.ZERO;

    /** 成分最大值（%） */
    private static final BigDecimal CONTENT_MAX = new BigDecimal("100");

    @Autowired
    private MaterialMapper materialMapper;

    @Override
    public List<Material> selectMaterialList(Material material)
    {
        return materialMapper.selectMaterialList(material);
    }

    @Override
    public long countMaterial(Material material)
    {
        return materialMapper.countMaterial(material);
    }

    @Override
    public Material selectMaterialById(Long id)
    {
        return materialMapper.selectMaterialById(id);
    }

    @Override
    public List<Material> selectMaterialByIds(Long[] ids)
    {
        return materialMapper.selectMaterialByIds(ids);
    }

    @Override
    public int insertMaterial(Material material)
    {
        checkMaterialComposition(material);
        return materialMapper.insertMaterial(material);
    }

    @Override
    public int updateMaterial(Material material)
    {
        checkMaterialComposition(material);
        return materialMapper.updateMaterial(material);
    }

    @Override
    public int deleteMaterialByIds(Long[] ids)
    {
        return materialMapper.deleteMaterialByIds(ids);
    }

    @Override
    public int deleteMaterialById(Long id)
    {
        return materialMapper.deleteMaterialById(id);
    }

    @Override
    @Transactional
    public String importMaterial(List<Material> list, Boolean updateSupport, String operName)
    {
        if (StringUtils.isEmpty(list))
        {
            throw new ServiceException("导入物料数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (Material material : list)
        {
            try
            {
                // 先做成分合理性校验，非法数据不落库并记录行号
                checkMaterialComposition(material);
                this.insertMaterial(material);
                successNum++;
            }
            catch (Exception e)
            {
                failureNum++;
                String name = material.getMaterialName() == null ? "未知物料" : material.getMaterialName();
                // 使用 String.format 组织提示信息，避免多次字符串拼接（规范性与性能均更优）
                failureMsg.append(String.format("<br/>%d、物料 %s 导入失败：%s", failureNum, name, e.getMessage()));
            }
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, String.format("很抱歉，导入失败！共 %d 条数据格式不正确，错误如下：", failureNum));
            throw new ServiceException(failureMsg.toString());
        }
        successMsg.insert(0, String.format("恭喜您，数据已全部导入成功！共 %d 条，数据如下：", successNum));
        return successMsg.toString();
    }

    @Override
    public void checkMaterialComposition(Material material)
    {
        if (StringUtils.isEmpty(material.getMaterialName()))
        {
            throw new ServiceException("物料名称不能为空");
        }
        if (StringUtils.isEmpty(material.getMaterialType()))
        {
            throw new ServiceException("物料类型不能为空");
        }
        BigDecimal total = BigDecimal.ZERO;
        total = total.add(checkContent("Cu含量", material.getCuContent()));
        total = total.add(checkContent("Fe含量", material.getFeContent()));
        total = total.add(checkContent("S含量", material.getSulfurContent()));
        total = total.add(checkContent("SiO2含量", material.getSio2Content()));
        total = total.add(checkContent("CaO含量", material.getCaoContent()));
        if (total.compareTo(COMPOSITION_LIMIT) > 0)
        {
            throw new ServiceException("成分合计为 " + total.stripTrailingZeros().toPlainString()
                    + "%，已超过 100%，请检查录入数据（Cu+Fe+S+SiO2+CaO 不得大于 100%）");
        }
    }

    /**
     * 校验单个成分字段的取值范围，空白值按 0 处理
     *
     * @param label 字段中文名（用于错误提示）
     * @param value 成分值
     * @return 校验通过后的成分值
     */
    private BigDecimal checkContent(String label, BigDecimal value)
    {
        if (value == null)
        {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(CONTENT_MIN) < 0 || value.compareTo(CONTENT_MAX) > 0)
        {
            throw new ServiceException(label + "取值须在 0~100 之间，当前值：" + value.toPlainString());
        }
        return value;
    }
}
