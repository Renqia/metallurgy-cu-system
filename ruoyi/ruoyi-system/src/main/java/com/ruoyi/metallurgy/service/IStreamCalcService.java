package com.ruoyi.metallurgy.service;

import java.util.List;
import com.ruoyi.metallurgy.domain.CalcRecord;
import com.ruoyi.metallurgy.domain.StreamFlow;
import com.ruoyi.metallurgy.dto.BalanceResult;
import com.ruoyi.metallurgy.dto.RatioRequest;
import com.ruoyi.metallurgy.dto.RatioResult;
import com.ruoyi.metallurgy.dto.SankeyData;

/**
 * 铜闪速冶炼计算 服务层
 *
 * <p>编排"配料求解 → 全流程元素守恒 → 物质流装配 → 计算留痕"的完整链路，
 * 具体算法分别由 {@link com.ruoyi.metallurgy.calc.RatioSolver}、
 * {@link com.ruoyi.metallurgy.calc.FlowCalculator} 与
 * {@link com.ruoyi.metallurgy.calc.SankeyBuilder} 承担，便于单元测试。</p>
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
public interface IStreamCalcService
{
    /**
     * 配料计算（求解各物料配比）
     *
     * @param request 请求（含物料、目标冰铜品位、目标硅铁比）
     * @return 配料结果
     */
    public RatioResult calcRatio(RatioRequest request);

    /**
     * 全流程物料平衡计算（并保存计算记录与物质流明细）
     *
     * @param request 请求
     * @return 已保存的计算记录（含主键，供前端跳转物质流分析）
     */
    public CalcRecord calcBalance(RatioRequest request);

    /**
     * 读取指定计算记录的全流程结果
     *
     * @param calcId 计算记录ID
     * @return 全流程平衡结果
     */
    public BalanceResult getBalanceResult(Long calcId);

    /**
     * 构建桑基图数据
     *
     * @param calcId 计算记录ID
     * @param element 元素口径（Cu/Fe/S/SiO2/CaO/合计）
     * @return 桑基图数据
     */
    public SankeyData buildSankey(Long calcId, String element);

    /**
     * 查询计算记录的物质流明细
     *
     * @param calcId 计算记录ID
     * @return 明细集合
     */
    public List<StreamFlow> getStreamFlows(Long calcId);
}
