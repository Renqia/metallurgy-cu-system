package com.ruoyi.metallurgy.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 配料计算结果
 *
 * <p>返回各物料配比、混合料加权成分、约束偏差与可行性结论。
 * <b>设计原则：约束冲突时不抛异常中断，而是返回最接近的可行解并给出偏差说明</b>，
 * 以便工艺人员判断是"补加熔剂"还是"放宽渣型要求"。</p>
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
public class RatioResult
{
    /** 各物料配比（含求解值） */
    private List<RatioMaterialItem> items = new ArrayList<>();

    /** 混合料 Cu 品位(%) */
    private BigDecimal mixedCu = BigDecimal.ZERO;

    /** 混合料 Fe 含量(%) */
    private BigDecimal mixedFe = BigDecimal.ZERO;

    /** 混合料 S 含量(%) */
    private BigDecimal mixedS = BigDecimal.ZERO;

    /** 混合料 SiO2 含量(%) */
    private BigDecimal mixedSio2 = BigDecimal.ZERO;

    /** 混合料 CaO 含量(%) */
    private BigDecimal mixedCao = BigDecimal.ZERO;

    /** 目标冰铜品位(%) */
    private BigDecimal matteGradeTarget = BigDecimal.ZERO;

    /** 计算得到的冰铜品位(%) */
    private BigDecimal matteGradeAchieved = BigDecimal.ZERO;

    /** 目标硅铁比 Fe/SiO2 */
    private BigDecimal feSio2Target = BigDecimal.ZERO;

    /** 实际硅铁比 Fe/SiO2（按渣中 Fe 与 SiO2 计算） */
    private BigDecimal feSio2Achieved = BigDecimal.ZERO;

    /** 配比合计(%)，正常应为 100 */
    private BigDecimal ratioSum = BigDecimal.ZERO;

    /** 质量平衡残差(%) = |Σx - 100| */
    private BigDecimal massBalanceResidual = BigDecimal.ZERO;

    /** 硅铁比偏差（实际 - 目标） */
    private BigDecimal feSio2Deviation = BigDecimal.ZERO;

    /** 是否满足全部约束 */
    private boolean feasible = true;

    /** 混合料综合成本(元/吨) */
    private BigDecimal unitCost = BigDecimal.ZERO;

    /** 提示与偏差说明 */
    private List<String> warnings = new ArrayList<>();

    /** 求解说明（用于说明书与页面展示算法过程） */
    private List<String> solveNotes = new ArrayList<>();

    public List<RatioMaterialItem> getItems()
    {
        return items;
    }

    public void setItems(List<RatioMaterialItem> items)
    {
        this.items = items;
    }

    public BigDecimal getMixedCu()
    {
        return mixedCu;
    }

    public void setMixedCu(BigDecimal mixedCu)
    {
        this.mixedCu = mixedCu;
    }

    public BigDecimal getMixedFe()
    {
        return mixedFe;
    }

    public void setMixedFe(BigDecimal mixedFe)
    {
        this.mixedFe = mixedFe;
    }

    public BigDecimal getMixedS()
    {
        return mixedS;
    }

    public void setMixedS(BigDecimal mixedS)
    {
        this.mixedS = mixedS;
    }

    public BigDecimal getMixedSio2()
    {
        return mixedSio2;
    }

    public void setMixedSio2(BigDecimal mixedSio2)
    {
        this.mixedSio2 = mixedSio2;
    }

    public BigDecimal getMixedCao()
    {
        return mixedCao;
    }

    public void setMixedCao(BigDecimal mixedCao)
    {
        this.mixedCao = mixedCao;
    }

    public BigDecimal getMatteGradeTarget()
    {
        return matteGradeTarget;
    }

    public void setMatteGradeTarget(BigDecimal matteGradeTarget)
    {
        this.matteGradeTarget = matteGradeTarget;
    }

    public BigDecimal getMatteGradeAchieved()
    {
        return matteGradeAchieved;
    }

    public void setMatteGradeAchieved(BigDecimal matteGradeAchieved)
    {
        this.matteGradeAchieved = matteGradeAchieved;
    }

    public BigDecimal getFeSio2Target()
    {
        return feSio2Target;
    }

    public void setFeSio2Target(BigDecimal feSio2Target)
    {
        this.feSio2Target = feSio2Target;
    }

    public BigDecimal getFeSio2Achieved()
    {
        return feSio2Achieved;
    }

    public void setFeSio2Achieved(BigDecimal feSio2Achieved)
    {
        this.feSio2Achieved = feSio2Achieved;
    }

    public BigDecimal getRatioSum()
    {
        return ratioSum;
    }

    public void setRatioSum(BigDecimal ratioSum)
    {
        this.ratioSum = ratioSum;
    }

    public BigDecimal getMassBalanceResidual()
    {
        return massBalanceResidual;
    }

    public void setMassBalanceResidual(BigDecimal massBalanceResidual)
    {
        this.massBalanceResidual = massBalanceResidual;
    }

    public BigDecimal getFeSio2Deviation()
    {
        return feSio2Deviation;
    }

    public void setFeSio2Deviation(BigDecimal feSio2Deviation)
    {
        this.feSio2Deviation = feSio2Deviation;
    }

    public boolean isFeasible()
    {
        return feasible;
    }

    public void setFeasible(boolean feasible)
    {
        this.feasible = feasible;
    }

    public BigDecimal getUnitCost()
    {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost)
    {
        this.unitCost = unitCost;
    }

    public List<String> getWarnings()
    {
        return warnings;
    }

    public void setWarnings(List<String> warnings)
    {
        this.warnings = warnings;
    }

    public List<String> getSolveNotes()
    {
        return solveNotes;
    }

    public void setSolveNotes(List<String> solveNotes)
    {
        this.solveNotes = solveNotes;
    }
}
