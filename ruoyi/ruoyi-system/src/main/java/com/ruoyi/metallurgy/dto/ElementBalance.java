package com.ruoyi.metallurgy.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 单元素平衡校核结果
 *
 * <p>元素守恒是物料平衡计算正确性的核心证据：某元素在该节点的输入总质量应等于输出总质量。
 * 残差按下式计算：<code>残差% = (输入 - 输出) / 输入 × 100%</code>，
 * 正常计算下应趋近 0（工程上允许 ≤0.5% 的舍入容差）。</p>
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
public class ElementBalance
{
    /** 元素/化合物名称（Cu/Fe/S/SiO2/CaO） */
    private String element;

    /** 输入总质量(kg) */
    private BigDecimal inputMass = BigDecimal.ZERO;

    /** 输出总质量(kg) */
    private BigDecimal outputMass = BigDecimal.ZERO;

    /** 残差(%)：正数表示输出少于输入 */
    private BigDecimal residual = BigDecimal.ZERO;

    public ElementBalance()
    {
    }

    public ElementBalance(String element, BigDecimal inputMass, BigDecimal outputMass)
    {
        this.element = element;
        this.inputMass = inputMass == null ? BigDecimal.ZERO : inputMass;
        this.outputMass = outputMass == null ? BigDecimal.ZERO : outputMass;
        this.residual = calcResidual(this.inputMass, this.outputMass);
    }

    /**
     * 计算残差百分比
     *
     * @param input 输入质量
     * @param output 输出质量
     * @return 残差(%)，输入为 0 时返回 0（避免除零）
     */
    private static BigDecimal calcResidual(BigDecimal input, BigDecimal output)
    {
        if (input == null || input.compareTo(BigDecimal.ZERO) == 0)
        {
            return BigDecimal.ZERO;
        }
        return input.subtract(output).multiply(new BigDecimal("100"))
                .divide(input, 6, RoundingMode.HALF_UP);
    }

    public String getElement()
    {
        return element;
    }

    public void setElement(String element)
    {
        this.element = element;
    }

    public BigDecimal getInputMass()
    {
        return inputMass;
    }

    public void setInputMass(BigDecimal inputMass)
    {
        this.inputMass = inputMass;
    }

    public BigDecimal getOutputMass()
    {
        return outputMass;
    }

    public void setOutputMass(BigDecimal outputMass)
    {
        this.outputMass = outputMass;
    }

    public BigDecimal getResidual()
    {
        return residual;
    }

    public void setResidual(BigDecimal residual)
    {
        this.residual = residual;
    }
}
