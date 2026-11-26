package com.ruoyi.metallurgy.dto;

import java.math.BigDecimal;
import com.ruoyi.metallurgy.calc.MetallurgyConstants;

/**
 * 全流程计算的模型可选参数
 *
 * <p>全部字段均为可选：未传入时取 {@link MetallurgyConstants} 中的示例默认值。
 * 这些参数决定了各节点的分配系数（烟尘率、渣含铜、粗铜品位等），
 * 属于"示例取值"而非现场标定值，在说明书与页面上均须如实标注。</p>
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
public class CalcOptions
{
    /** 烟尘率(%) */
    private BigDecimal dustRate;

    /** 闪速熔炼渣含铜(%) */
    private BigDecimal slagCu;

    /** 粗铜品位(%) */
    private BigDecimal blisterGrade;

    /** 吹炼渣含铜(%) */
    private BigDecimal blisterSlagCu;

    /** 吹炼渣硅铁比 Fe/SiO2 */
    private BigDecimal blisterFeSio2;

    /** 阳极铜品位(%) */
    private BigDecimal anodeGrade;

    /** 阳极精炼铜回收率(%) */
    private BigDecimal refineYield;

    /** 阳极精炼渣含铜(%) */
    private BigDecimal refineSlagCu;

    /** 电解铜回收率(%) */
    private BigDecimal electroYield;

    /** 阴极铜品位(%) */
    private BigDecimal cathodeGrade;

    /** 阳极泥率(%) */
    private BigDecimal slimeRate;

    public BigDecimal getDustRate()
    {
        return dustRate == null ? MetallurgyConstants.DEFAULT_DUST_RATE : dustRate;
    }

    public void setDustRate(BigDecimal dustRate)
    {
        this.dustRate = dustRate;
    }

    public BigDecimal getSlagCu()
    {
        return slagCu == null ? MetallurgyConstants.DEFAULT_SLAG_CU : slagCu;
    }

    public void setSlagCu(BigDecimal slagCu)
    {
        this.slagCu = slagCu;
    }

    public BigDecimal getBlisterGrade()
    {
        return blisterGrade == null ? MetallurgyConstants.DEFAULT_BLISTER_GRADE : blisterGrade;
    }

    public void setBlisterGrade(BigDecimal blisterGrade)
    {
        this.blisterGrade = blisterGrade;
    }

    public BigDecimal getBlisterSlagCu()
    {
        return blisterSlagCu == null ? MetallurgyConstants.DEFAULT_BLISTER_SLAG_CU : blisterSlagCu;
    }

    public void setBlisterSlagCu(BigDecimal blisterSlagCu)
    {
        this.blisterSlagCu = blisterSlagCu;
    }

    public BigDecimal getBlisterFeSio2()
    {
        return blisterFeSio2 == null ? MetallurgyConstants.DEFAULT_BLISTER_FE_SIO2 : blisterFeSio2;
    }

    public void setBlisterFeSio2(BigDecimal blisterFeSio2)
    {
        this.blisterFeSio2 = blisterFeSio2;
    }

    public BigDecimal getAnodeGrade()
    {
        return anodeGrade == null ? MetallurgyConstants.DEFAULT_ANODE_GRADE : anodeGrade;
    }

    public void setAnodeGrade(BigDecimal anodeGrade)
    {
        this.anodeGrade = anodeGrade;
    }

    public BigDecimal getRefineYield()
    {
        return refineYield == null ? MetallurgyConstants.DEFAULT_REFINE_YIELD : refineYield;
    }

    public void setRefineYield(BigDecimal refineYield)
    {
        this.refineYield = refineYield;
    }

    public BigDecimal getRefineSlagCu()
    {
        return refineSlagCu == null ? MetallurgyConstants.DEFAULT_REFINE_SLAG_CU : refineSlagCu;
    }

    public void setRefineSlagCu(BigDecimal refineSlagCu)
    {
        this.refineSlagCu = refineSlagCu;
    }

    public BigDecimal getElectroYield()
    {
        return electroYield == null ? MetallurgyConstants.DEFAULT_ELECTRO_YIELD : electroYield;
    }

    public void setElectroYield(BigDecimal electroYield)
    {
        this.electroYield = electroYield;
    }

    public BigDecimal getCathodeGrade()
    {
        return cathodeGrade == null ? MetallurgyConstants.DEFAULT_CATHODE_GRADE : cathodeGrade;
    }

    public void setCathodeGrade(BigDecimal cathodeGrade)
    {
        this.cathodeGrade = cathodeGrade;
    }

    public BigDecimal getSlimeRate()
    {
        return slimeRate == null ? MetallurgyConstants.DEFAULT_SLIME_RATE : slimeRate;
    }

    public void setSlimeRate(BigDecimal slimeRate)
    {
        this.slimeRate = slimeRate;
    }
}
