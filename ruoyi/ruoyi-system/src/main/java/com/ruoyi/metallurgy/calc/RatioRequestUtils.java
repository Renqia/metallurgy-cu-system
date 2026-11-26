package com.ruoyi.metallurgy.calc;

import java.math.BigDecimal;
import com.ruoyi.metallurgy.dto.RatioMaterialItem;
import com.ruoyi.metallurgy.dto.RatioRequest;

/**
 * 配料请求的配比锁定规则（纯算法工具类，不依赖 Spring 容器）
 *
 * <p>业务规则：当请求中各物料配比<b>全为正数且合计约等于 100%</b>（容差 0.5%）时，
 * 视为用户已按给定配比评估，将该配比写入 <code>fixedRatio</code> 并置 <code>fixed=true</code>，
 * 后续求解不再重新分配配比，只做成分与约束校核。</p>
 *
 * <p>该规则同时被"全流程计算保存前的解析"（{@code StreamCalcServiceImpl#resolveRatio}）
 * 与"敏感性分析逐点重算"复用，确保两者对手工配比的处理完全一致——不允许出现
 * "同一份快照在两处被解释成不同配比"的情况。</p>
 *
 * @author 【成员1】
 * @date 2026-10-14
 */
public final class RatioRequestUtils
{
    /** 配比合计目标值(%) */
    public static final BigDecimal FULL_RATIO = new BigDecimal("100");

    /** 判定"合计等于 100%"的容差(%) */
    public static final BigDecimal LOCK_TOLERANCE = new BigDecimal("0.5");

    private RatioRequestUtils()
    {
    }

    /**
     * 配比完整（全正且合计≈100%）时把配比固化为锁定值
     *
     * @param request 配料/全流程请求（原地修改 items 的 fixed 与 fixedRatio）
     */
    public static void applyLockedRatioIfComplete(RatioRequest request)
    {
        if (request == null || request.getItems() == null || request.getItems().isEmpty())
        {
            return;
        }
        BigDecimal sum = BigDecimal.ZERO;
        boolean allPositive = true;
        for (RatioMaterialItem item : request.getItems())
        {
            if (item.getRatio() == null || item.getRatio().compareTo(BigDecimal.ZERO) <= 0)
            {
                allPositive = false;
                break;
            }
            sum = sum.add(item.getRatio());
        }
        if (allPositive && sum.subtract(FULL_RATIO).abs().compareTo(LOCK_TOLERANCE) <= 0)
        {
            for (RatioMaterialItem item : request.getItems())
            {
                item.setFixed(true);
                item.setFixedRatio(item.getRatio());
            }
        }
    }
}
