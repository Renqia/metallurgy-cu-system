package com.ruoyi.metallurgy.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 工艺参数方案对象 t_process_param
 *
 * <p>保存铜闪速熔炼的工艺控制参数方案：富氧浓度、氧料比、硅铁比（Fe/SiO2）、
 * 目标冰铜品位、处理量。一次配料计算或全流程平衡计算均需引用一个参数方案，
 * 以保证计算结果可追溯（"用了什么参数算出的结果"）。</p>
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
public class ProcessParam extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 方案名称 */
    @Excel(name = "方案名称")
    private String planName;

    /** 富氧浓度(vol%) */
    @Excel(name = "富氧浓度(vol%)")
    private BigDecimal oxygenConc;

    /** 氧料比(Nm3/t) */
    @Excel(name = "氧料比(Nm3/t)")
    private BigDecimal oxygenRatio;

    /** 硅铁比Fe/SiO2 */
    @Excel(name = "硅铁比Fe/SiO2")
    private BigDecimal feSio2Ratio;

    /** 目标冰铜品位(%) */
    @Excel(name = "目标冰铜品位(%)")
    private BigDecimal matteGrade;

    /** 处理量(t/h) */
    @Excel(name = "处理量(t/h)")
    private BigDecimal capacity;

    /** 状态（0草稿 1启用 2停用） */
    @Excel(name = "状态", readConverterExp = "0=草稿,1=启用,2=停用")
    private String status;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setPlanName(String planName)
    {
        this.planName = planName;
    }

    public String getPlanName()
    {
        return planName;
    }

    public void setOxygenConc(BigDecimal oxygenConc)
    {
        this.oxygenConc = oxygenConc;
    }

    public BigDecimal getOxygenConc()
    {
        return oxygenConc;
    }

    public void setOxygenRatio(BigDecimal oxygenRatio)
    {
        this.oxygenRatio = oxygenRatio;
    }

    public BigDecimal getOxygenRatio()
    {
        return oxygenRatio;
    }

    public void setFeSio2Ratio(BigDecimal feSio2Ratio)
    {
        this.feSio2Ratio = feSio2Ratio;
    }

    public BigDecimal getFeSio2Ratio()
    {
        return feSio2Ratio;
    }

    public void setMatteGrade(BigDecimal matteGrade)
    {
        this.matteGrade = matteGrade;
    }

    public BigDecimal getMatteGrade()
    {
        return matteGrade;
    }

    public void setCapacity(BigDecimal capacity)
    {
        this.capacity = capacity;
    }

    public BigDecimal getCapacity()
    {
        return capacity;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    @Override
    public String toString()
    {
        return "ProcessParam{id=" + id + ", planName=" + planName + ", oxygenConc=" + oxygenConc
                + ", oxygenRatio=" + oxygenRatio + ", feSio2Ratio=" + feSio2Ratio
                + ", matteGrade=" + matteGrade + ", capacity=" + capacity + ", status=" + status + "}";
    }
}
