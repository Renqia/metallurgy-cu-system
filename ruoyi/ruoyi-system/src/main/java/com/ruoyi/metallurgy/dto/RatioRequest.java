package com.ruoyi.metallurgy.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 配料计算请求
 *
 * <p>求解目标：在满足"质量守恒 Σx = 100%"与"渣型约束 Fe/SiO2 = 目标硅铁比"的前提下，
 * 求出各物料的配比 x，使熔炼产物冰铜品位达到目标值。</p>
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
public class RatioRequest
{
    /** 参数方案ID（可空，用于记录引用） */
    private Long planId;

    /** 目标冰铜品位(%) */
    private BigDecimal matteGrade = new BigDecimal("55");

    /** 目标硅铁比 Fe/SiO2 */
    private BigDecimal feSio2Ratio = new BigDecimal("1.10");

    /** 是否保存计算记录 */
    private boolean saveRecord = false;

    /** 全流程计算的模型可选参数（配料计算不使用） */
    private CalcOptions options;

    /** 备注（写入计算记录） */
    private String remark;

    /** 参与计算的物料项 */
    private List<RatioMaterialItem> items = new ArrayList<>();

    public Long getPlanId()
    {
        return planId;
    }

    public void setPlanId(Long planId)
    {
        this.planId = planId;
    }

    public BigDecimal getMatteGrade()
    {
        return matteGrade == null ? new BigDecimal("55") : matteGrade;
    }

    public void setMatteGrade(BigDecimal matteGrade)
    {
        this.matteGrade = matteGrade;
    }

    public BigDecimal getFeSio2Ratio()
    {
        return feSio2Ratio == null ? new BigDecimal("1.10") : feSio2Ratio;
    }

    public void setFeSio2Ratio(BigDecimal feSio2Ratio)
    {
        this.feSio2Ratio = feSio2Ratio;
    }

    public boolean isSaveRecord()
    {
        return saveRecord;
    }

    public void setSaveRecord(boolean saveRecord)
    {
        this.saveRecord = saveRecord;
    }

    public List<RatioMaterialItem> getItems()
    {
        return items;
    }

    public void setItems(List<RatioMaterialItem> items)
    {
        this.items = items;
    }

    public CalcOptions getOptions()
    {
        return options;
    }

    public void setOptions(CalcOptions options)
    {
        this.options = options;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }
}
