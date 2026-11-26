package com.ruoyi.metallurgy.dto;

import java.math.BigDecimal;

/**
 * 方案对比的一列（对应一条全流程计算记录）
 *
 * @author 【成员1】
 * @date 2026-10-14
 */
public class CompareColumn
{
    /** 计算记录ID */
    private Long calcId;

    /** 参数方案名称（为空时前端显示"未关联方案"） */
    private String planName;

    /** 计算时间（yyyy-MM-dd HH:mm:ss） */
    private String calcTime;

    /** 备注 */
    private String remark;

    /** 是否满足全部工艺约束（元素平衡残差在容差内） */
    private boolean feasible;

    /** 最大元素平衡残差(%) */
    private BigDecimal maxResidual;

    public Long getCalcId()
    {
        return calcId;
    }

    public void setCalcId(Long calcId)
    {
        this.calcId = calcId;
    }

    public String getPlanName()
    {
        return planName;
    }

    public void setPlanName(String planName)
    {
        this.planName = planName;
    }

    public String getCalcTime()
    {
        return calcTime;
    }

    public void setCalcTime(String calcTime)
    {
        this.calcTime = calcTime;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public boolean isFeasible()
    {
        return feasible;
    }

    public void setFeasible(boolean feasible)
    {
        this.feasible = feasible;
    }

    public BigDecimal getMaxResidual()
    {
        return maxResidual;
    }

    public void setMaxResidual(BigDecimal maxResidual)
    {
        this.maxResidual = maxResidual;
    }
}
