package com.ruoyi.metallurgy.service;

import java.util.List;
import com.ruoyi.metallurgy.domain.CalcRecord;

/**
 * 计算记录 服务层
 *
 * <p>记录由计算流程自动写入，本服务提供查询与删除能力；
 * 删除记录时会级联清理其物质流明细，避免残留孤儿数据。</p>
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
public interface ICalcRecordService
{
    /**
     * 查询计算记录列表
     *
     * @param calcRecord 查询条件
     * @return 记录集合
     */
    public List<CalcRecord> selectCalcRecordList(CalcRecord calcRecord);

    /**
     * 统计计算记录条数（供首页概览使用）
     *
     * @param calcRecord 查询条件
     * @return 条数
     */
    public long countCalcRecord(CalcRecord calcRecord);

    /**
     * 查询最近 N 条全流程计算记录（供首页概览使用，数据库层 LIMIT 且遵循可见范围）
     *
     * @param limit 最大条数
     * @return 记录集合（按 id 倒序）
     */
    public List<CalcRecord> selectBalanceRecordTop(int limit);

    /**
     * 查询计算记录详情（含输入/结果快照）
     *
     * @param id 主键
     * @return 计算记录
     */
    public CalcRecord selectCalcRecordById(Long id);

    /**
     * 批量删除计算记录（并级联删除物质流明细）
     *
     * @param ids 主键集合
     * @return 结果
     */
    public int deleteCalcRecordByIds(Long[] ids);

    /**
     * 删除计算记录（并级联删除物质流明细）
     *
     * @param id 主键
     * @return 结果
     */
    public int deleteCalcRecordById(Long id);
}
