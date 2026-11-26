package com.ruoyi.metallurgy.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 物料台账对象 t_material
 *
 * <p>用于维护铜闪速冶炼所用原燃料与返料的化学成分（Cu/Fe/S/SiO2/CaO）与单价、库存。
 * 成分字段统一采用质量分数（%），精度 0.01，是后续配料计算与元素守恒计算的输入基础。</p>
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
public class Material extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 物料名称 */
    @Excel(name = "物料名称")
    private String materialName;

    /** 物料类型（铜精矿/熔剂/返料/烟尘/燃料） */
    @Excel(name = "物料类型", readConverterExp = "铜精矿=铜精矿,熔剂=熔剂,返料=返料,烟尘=烟尘,燃料=燃料")
    private String materialType;

    /** Cu含量(%) */
    @Excel(name = "Cu含量(%)")
    private BigDecimal cuContent;

    /** Fe含量(%) */
    @Excel(name = "Fe含量(%)")
    private BigDecimal feContent;

    /**
     * S含量(%)
     *
     * <p>命名说明：字段与访问器统一命名为 sulfurContent。若命名为 sContent，
     * 其 getter 为 getSContent，Jackson 按"第二个字母大写则保留原样"的规则会推导出
     * 属性名 "SContent"，与前端提交的 "sContent" 不匹配，导致硫含量被静默忽略而恒为 0，
     * 序列化时还会同时输出 scontent 与 sContent 两个键。统一使用 sulfurContent 可彻底规避该问题。</p>
     */
    @Excel(name = "S含量(%)")
    private BigDecimal sulfurContent;

    /** SiO2含量(%) */
    @Excel(name = "SiO2含量(%)")
    private BigDecimal sio2Content;

    /** CaO含量(%) */
    @Excel(name = "CaO含量(%)")
    private BigDecimal caoContent;

    /** 单价(元/吨) */
    @Excel(name = "单价(元/吨)")
    private BigDecimal unitPrice;

    /** 库存量(吨) */
    @Excel(name = "库存量(吨)")
    private BigDecimal stockQty;

    /** 来源/产地 */
    @Excel(name = "来源/产地")
    private String source;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setMaterialName(String materialName)
    {
        this.materialName = materialName;
    }

    public String getMaterialName()
    {
        return materialName;
    }

    public void setMaterialType(String materialType)
    {
        this.materialType = materialType;
    }

    public String getMaterialType()
    {
        return materialType;
    }

    public void setCuContent(BigDecimal cuContent)
    {
        this.cuContent = cuContent;
    }

    public BigDecimal getCuContent()
    {
        return cuContent;
    }

    public void setFeContent(BigDecimal feContent)
    {
        this.feContent = feContent;
    }

    public BigDecimal getFeContent()
    {
        return feContent;
    }

    public void setSulfurContent(BigDecimal sulfurContent)
    {
        this.sulfurContent = sulfurContent;
    }

    public BigDecimal getSulfurContent()
    {
        return sulfurContent;
    }

    public void setSio2Content(BigDecimal sio2Content)
    {
        this.sio2Content = sio2Content;
    }

    public BigDecimal getSio2Content()
    {
        return sio2Content;
    }

    public void setCaoContent(BigDecimal caoContent)
    {
        this.caoContent = caoContent;
    }

    public BigDecimal getCaoContent()
    {
        return caoContent;
    }

    public void setUnitPrice(BigDecimal unitPrice)
    {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getUnitPrice()
    {
        return unitPrice;
    }

    public void setStockQty(BigDecimal stockQty)
    {
        this.stockQty = stockQty;
    }

    public BigDecimal getStockQty()
    {
        return stockQty;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public String getSource()
    {
        return source;
    }

    @Override
    public String toString()
    {
        return "Material{id=" + id + ", materialName=" + materialName + ", materialType=" + materialType
                + ", cu=" + cuContent + ", fe=" + feContent + ", s=" + sulfurContent + ", sio2=" + sio2Content
                + ", cao=" + caoContent + "}";
    }
}
