package com.ruoyi.metallurgy.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.metallurgy.domain.CalcRecord;

/**
 * 计算记录 数据层
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
public interface CalcRecordMapper
{
    /**
     * 查询计算记录
     *
     * @param id 主键
     * @return 计算记录
     */
    public CalcRecord selectCalcRecordById(Long id);

    /**
     * 查询计算记录列表
     *
     * @param calcRecord 计算记录
     * @return 集合
     */
    public List<CalcRecord> selectCalcRecordList(CalcRecord calcRecord);

    /**
     * 统计计算记录条数（供首页概览使用，避免全量查询后在内存里取 size）
     *
     * @param calcRecord 查询条件
     * @return 条数
     */
    public long countCalcRecord(CalcRecord calcRecord);

    /**
     * 查询最近 N 条指定类型的计算记录（数据库层 LIMIT，供首页概览使用）
     *
     * <p>避免"全量查询后在内存中截断"导致记录规模增大时性能线性退化。</p>
     *
     * @param calcType 计算类型（1 配料 / 2 全流程），为空表示不限制
     * @param createBy 归属过滤（空表示不过滤，超级管理员使用）
     * @param limit 最大条数（正数）
     * @return 记录集合（按 id 倒序）
     */
    public List<CalcRecord> selectCalcRecordTop(@Param("calcType") String calcType,
            @Param("createBy") String createBy, @Param("limit") int limit);

    /**
     * 新增计算记录
     *
     * @param calcRecord 计算记录
     * @return 结果
     */
    public int insertCalcRecord(CalcRecord calcRecord);

    /**
     * 删除计算记录
     *
     * @param id 主键
     * @return 结果
     */
    public int deleteCalcRecordById(Long id);

    /**
     * 批量删除计算记录
     *
     * @param ids 主键集合
     * @return 结果
     */
    public int deleteCalcRecordByIds(Long[] ids);
}
