package com.ruoyi.metallurgy.dto;

import java.math.BigDecimal;

/**
 * 物质流条目：表示某个工序节点的一股输入或输出流
 *
 * <p>同时记录总质量与各元素（Cu/Fe/S/SiO2/CaO）的质量，是元素守恒校核、
 * 桑基图渲染与物质流报表的最小数据单元。</p>
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
public class StreamItem
{
    /** 流名称（物料名或产物名） */
    private String name;

    /** 流向：input（输入）/ output（输出） */
    private String kind;

    /** 总质量(kg) */
    private BigDecimal totalMass = BigDecimal.ZERO;

    /** Cu 质量(kg) */
    private BigDecimal cu = BigDecimal.ZERO;

    /** Fe 质量(kg) */
    private BigDecimal fe = BigDecimal.ZERO;

    /** S 质量(kg) */
    private BigDecimal s = BigDecimal.ZERO;

    /** SiO2 质量(kg) */
    private BigDecimal sio2 = BigDecimal.ZERO;

    /** CaO 质量(kg) */
    private BigDecimal cao = BigDecimal.ZERO;

    /** 单位 */
    private String unit = "kg/100kg混合料";

    public StreamItem()
    {
    }

    public StreamItem(String name, String kind)
    {
        this.name = name;
        this.kind = kind;
    }

    /**
     * 按给定的成分百分比与总质量构造流条目
     *
     * @param name 流名称
     * @param kind 流向（input/output）
     * @param totalMass 总质量(kg)
     * @param cuPct Cu 质量分数(%)
     * @param fePct Fe 质量分数(%)
     * @param sPct S 质量分数(%)
     * @param sio2Pct SiO2 质量分数(%)
     * @param caoPct CaO 质量分数(%)
     * @return 流条目
     */
    public static StreamItem of(String name, String kind, BigDecimal totalMass,
            BigDecimal cuPct, BigDecimal fePct, BigDecimal sPct, BigDecimal sio2Pct, BigDecimal caoPct)
    {
        StreamItem item = new StreamItem(name, kind);
        item.setTotalMass(totalMass);
        item.setCu(pctOf(totalMass, cuPct));
        item.setFe(pctOf(totalMass, fePct));
        item.setS(pctOf(totalMass, sPct));
        item.setSio2(pctOf(totalMass, sio2Pct));
        item.setCao(pctOf(totalMass, caoPct));
        return item;
    }

    /**
     * 计算指定质量中某成分的质量：质量 × 百分比 / 100
     */
    private static BigDecimal pctOf(BigDecimal mass, BigDecimal pct)
    {
        if (mass == null || pct == null)
        {
            return BigDecimal.ZERO;
        }
        return mass.multiply(pct).divide(new BigDecimal("100"), 6, java.math.RoundingMode.HALF_UP);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getKind()
    {
        return kind;
    }

    public void setKind(String kind)
    {
        this.kind = kind;
    }

    public BigDecimal getTotalMass()
    {
        return totalMass == null ? BigDecimal.ZERO : totalMass;
    }

    public void setTotalMass(BigDecimal totalMass)
    {
        this.totalMass = totalMass;
    }

    public BigDecimal getCu()
    {
        return cu == null ? BigDecimal.ZERO : cu;
    }

    public void setCu(BigDecimal cu)
    {
        this.cu = cu;
    }

    public BigDecimal getFe()
    {
        return fe == null ? BigDecimal.ZERO : fe;
    }

    public void setFe(BigDecimal fe)
    {
        this.fe = fe;
    }

    public BigDecimal getS()
    {
        return s == null ? BigDecimal.ZERO : s;
    }

    public void setS(BigDecimal s)
    {
        this.s = s;
    }

    public BigDecimal getSio2()
    {
        return sio2 == null ? BigDecimal.ZERO : sio2;
    }

    public void setSio2(BigDecimal sio2)
    {
        this.sio2 = sio2;
    }

    public BigDecimal getCao()
    {
        return cao == null ? BigDecimal.ZERO : cao;
    }

    public void setCao(BigDecimal cao)
    {
        this.cao = cao;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    /**
     * 按元素名取该元素质量（供桑基图按元素切换视图使用）
     *
     * @param element 元素名（Cu/Fe/S/SiO2/CaO/合计）
     * @return 质量(kg)
     */
    public BigDecimal massOf(String element)
    {
        if (element == null)
        {
            return BigDecimal.ZERO;
        }
        switch (element)
        {
            case "Cu":
                return getCu();
            case "Fe":
                return getFe();
            case "S":
                return getS();
            case "SiO2":
                return getSio2();
            case "CaO":
                return getCao();
            case "合计":
                return getTotalMass();
            default:
                return BigDecimal.ZERO;
        }
    }
}
