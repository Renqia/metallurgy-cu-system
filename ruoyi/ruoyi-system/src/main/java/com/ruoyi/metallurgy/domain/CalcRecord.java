package com.ruoyi.metallurgy.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 计算记录对象 t_calc_record
 *
 * <p>每次配料计算或全流程平衡计算均保存输入快照与结果快照（JSON 文本），
 * 保证任何一个计算结果都可回溯"用什么输入、什么参数算出来的"，
 * 这是工艺计算系统可追溯性的基本要求。</p>
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
public class CalcRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 参数方案ID */
    private Long planId;

    /** 参数方案名称（冗余字段，便于列表展示） */
    @Excel(name = "参数方案")
    private String planName;

    /** 计算类型（1配料计算 2全流程平衡） */
    @Excel(name = "计算类型", readConverterExp = "1=配料计算,2=全流程平衡")
    private String calcType;

    /** 输入快照（JSON 文本） */
    private String inputSnapshot;

    /** 结果快照（JSON 文本） */
    private String resultSnapshot;

    /** 混合料Cu品位(%) */
    @Excel(name = "混合料Cu品位(%)")
    private BigDecimal mixedCu;

    /** 计算冰铜品位(%) */
    @Excel(name = "冰铜品位(%)")
    private BigDecimal matteGradeCalc;

    /** 计算炉渣硅铁比 */
    @Excel(name = "炉渣硅铁比")
    private BigDecimal slagFeSio2;

    /** 铜直收率(%) */
    @Excel(name = "铜直收率(%)")
    private BigDecimal directYield;

    /** 计算时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "计算时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date calcTime;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getPlanId()
    {
        return planId;
    }

    public void setPlanId(Long planId)
    {
        this.planId = planId;
    }

    public String getPlanName()
    {
        return planName;
    }

    public void setPlanName(String planName)
    {
        this.planName = planName;
    }

    public String getCalcType()
    {
        return calcType;
    }

    public void setCalcType(String calcType)
    {
        this.calcType = calcType;
    }

    public String getInputSnapshot()
    {
        return inputSnapshot;
    }

    public void setInputSnapshot(String inputSnapshot)
    {
        this.inputSnapshot = inputSnapshot;
    }

    public String getResultSnapshot()
    {
        return resultSnapshot;
    }

    public void setResultSnapshot(String resultSnapshot)
    {
        this.resultSnapshot = resultSnapshot;
    }

    public BigDecimal getMixedCu()
    {
        return mixedCu;
    }

    public void setMixedCu(BigDecimal mixedCu)
    {
        this.mixedCu = mixedCu;
    }

    public BigDecimal getMatteGradeCalc()
    {
        return matteGradeCalc;
    }

    public void setMatteGradeCalc(BigDecimal matteGradeCalc)
    {
        this.matteGradeCalc = matteGradeCalc;
    }

    public BigDecimal getSlagFeSio2()
    {
        return slagFeSio2;
    }

    public void setSlagFeSio2(BigDecimal slagFeSio2)
    {
        this.slagFeSio2 = slagFeSio2;
    }

    public BigDecimal getDirectYield()
    {
        return directYield;
    }

    public void setDirectYield(BigDecimal directYield)
    {
        this.directYield = directYield;
    }

    public Date getCalcTime()
    {
        return calcTime;
    }

    public void setCalcTime(Date calcTime)
    {
        this.calcTime = calcTime;
    }
}
