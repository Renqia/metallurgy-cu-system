package com.ruoyi.metallurgy.mapper;

import java.util.List;
import com.ruoyi.metallurgy.domain.StreamFlow;

/**
 * 物质流明细 数据层
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
public interface StreamFlowMapper
{
    /**
     * 按计算记录ID查询物质流明细
     *
     * @param calcId 计算记录ID
     * @return 明细集合
     */
    public List<StreamFlow> selectStreamFlowByCalcId(Long calcId);

    /**
     * 批量写入物质流明细
     *
     * @param list 明细集合
     * @return 写入条数
     */
    public int batchInsertStreamFlow(List<StreamFlow> list);

    /**
     * 按计算记录ID删除物质流明细
     *
     * @param calcId 计算记录ID
     * @return 结果
     */
    public int deleteStreamFlowByCalcId(Long calcId);
}
