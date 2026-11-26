package com.ruoyi.metallurgy.dto;

import java.math.BigDecimal;

/**
 * 配料计算的单个物料项
 *
 * <p>承载物料的化学成分（用于建立线性方程组）以及配比约束信息：
 * ① <code>fixed=true</code> 表示该物料配比由用户锁定，不参与求解；
 * ② 其余物料的配比由计算引擎求解。</p>
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
public class RatioMaterialItem
{
    /** 物料ID */
    private Long materialId;

    /** 物料名称 */
    private String materialName;

    /** 物料类型（铜精矿/熔剂/返料/烟尘/燃料） */
    private String materialType;

    /** Cu含量(%) */
    private BigDecimal cuContent = BigDecimal.ZERO;

    /** Fe含量(%) */
    private BigDecimal feContent = BigDecimal.ZERO;

    /** S含量(%)（字段与访问器统一用 sulfurContent，规避 Jackson 属性名推导歧义） */
    private BigDecimal sulfurContent = BigDecimal.ZERO;

    /** SiO2含量(%) */
    private BigDecimal sio2Content = BigDecimal.ZERO;

    /** CaO含量(%) */
    private BigDecimal caoContent = BigDecimal.ZERO;

    /** 单价(元/吨) */
    private BigDecimal unitPrice = BigDecimal.ZERO;

    /** 是否锁定配比（锁定后不参与求解） */
    private boolean fixed = false;

    /** 锁定的配比(%)，仅 fixed=true 时有效 */
    private BigDecimal fixedRatio = BigDecimal.ZERO;

    /** 求解得到的配比(%) */
    private BigDecimal ratio = BigDecimal.ZERO;

    public Long getMaterialId()
    {
        return materialId;
    }

    public void setMaterialId(Long materialId)
    {
        this.materialId = materialId;
    }

    public String getMaterialName()
    {
        return materialName;
    }

    public void setMaterialName(String materialName)
    {
        this.materialName = materialName;
    }

    public String getMaterialType()
    {
        return materialType;
    }

    public void setMaterialType(String materialType)
    {
        this.materialType = materialType;
    }

    public BigDecimal getCuContent()
    {
        return cuContent == null ? BigDecimal.ZERO : cuContent;
    }

    public void setCuContent(BigDecimal cuContent)
    {
        this.cuContent = cuContent;
    }

    public BigDecimal getFeContent()
    {
        return feContent == null ? BigDecimal.ZERO : feContent;
    }

    public void setFeContent(BigDecimal feContent)
    {
        this.feContent = feContent;
    }

    public BigDecimal getSulfurContent()
    {
        return sulfurContent == null ? BigDecimal.ZERO : sulfurContent;
    }

    public void setSulfurContent(BigDecimal sulfurContent)
    {
        this.sulfurContent = sulfurContent;
    }

    public BigDecimal getSio2Content()
    {
        return sio2Content == null ? BigDecimal.ZERO : sio2Content;
    }

    public void setSio2Content(BigDecimal sio2Content)
    {
        this.sio2Content = sio2Content;
    }

    public BigDecimal getCaoContent()
    {
        return caoContent == null ? BigDecimal.ZERO : caoContent;
    }

    public void setCaoContent(BigDecimal caoContent)
    {
        this.caoContent = caoContent;
    }

    public BigDecimal getUnitPrice()
    {
        return unitPrice == null ? BigDecimal.ZERO : unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice)
    {
        this.unitPrice = unitPrice;
    }

    public boolean isFixed()
    {
        return fixed;
    }

    public void setFixed(boolean fixed)
    {
        this.fixed = fixed;
    }

    public BigDecimal getFixedRatio()
    {
        return fixedRatio == null ? BigDecimal.ZERO : fixedRatio;
    }

    public void setFixedRatio(BigDecimal fixedRatio)
    {
        this.fixedRatio = fixedRatio;
    }

    public BigDecimal getRatio()
    {
        return ratio == null ? BigDecimal.ZERO : ratio;
    }

    public void setRatio(BigDecimal ratio)
    {
        this.ratio = ratio;
    }
}
