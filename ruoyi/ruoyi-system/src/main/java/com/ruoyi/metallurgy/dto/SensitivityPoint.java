package com.ruoyi.metallurgy.dto;

import java.math.BigDecimal;

/**
 * 敏感性分析的一个扫描点
 *
 * <p>单点重算失败（如目标品位超出物相上限、硫量不足）时 <b>保留该点</b>：
 * <code>success=false</code>、<code>value</code> 仍为扫描值、指标序列对应位置为 null，
 * 前端折线在该点断开而不补 0，从而不掩盖工艺不可行的区间。</p>
 *
 * @author 【成员1】
 * @date 2026-10-14
 */
public class SensitivityPoint
{
    /** 该点的扫描值 */
    private BigDecimal value;

    /** 单点重算是否成功 */
    private boolean success = true;

    /** 失败原因（中文业务提示，成功时为空） */
    private String message;

    public BigDecimal getValue()
    {
        return value;
    }

    public void setValue(BigDecimal value)
    {
        this.value = value;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public void setSuccess(boolean success)
    {
        this.success = success;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}
