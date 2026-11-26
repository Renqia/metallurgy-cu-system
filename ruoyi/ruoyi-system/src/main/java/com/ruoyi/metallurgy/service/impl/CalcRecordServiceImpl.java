package com.ruoyi.metallurgy.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.metallurgy.domain.CalcRecord;
import com.ruoyi.metallurgy.mapper.CalcRecordMapper;
import com.ruoyi.metallurgy.mapper.StreamFlowMapper;
import com.ruoyi.metallurgy.service.ICalcRecordService;
import com.ruoyi.metallurgy.support.RecordScope;

/**
 * 计算记录 服务层实现
 *
 * <p>列表查询与计数统一附加业务数据可见范围过滤（非超级管理员仅可见本人创建的记录），
 * 使首页概览、记录列表与分析入口的口径保持一致，不存在"概览受限但列表可见"的绕过路径。</p>
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
@Service
public class CalcRecordServiceImpl implements ICalcRecordService
{
    /** 计算类型：全流程平衡（首页概览只关心该类型的最近记录） */
    private static final String TYPE_BALANCE = "2";

    @Autowired
    private CalcRecordMapper calcRecordMapper;

    @Autowired
    private StreamFlowMapper streamFlowMapper;

    @Override
    public List<CalcRecord> selectCalcRecordList(CalcRecord calcRecord)
    {
        CalcRecord query = calcRecord == null ? new CalcRecord() : calcRecord;
        RecordScope.applyOwnerFilter(query);
        return calcRecordMapper.selectCalcRecordList(query);
    }

    @Override
    public long countCalcRecord(CalcRecord calcRecord)
    {
        CalcRecord query = calcRecord == null ? new CalcRecord() : calcRecord;
        RecordScope.applyOwnerFilter(query);
        return calcRecordMapper.countCalcRecord(query);
    }

    @Override
    public List<CalcRecord> selectBalanceRecordTop(int limit)
    {
        CalcRecord query = new CalcRecord();
        query.setCalcType(TYPE_BALANCE);
        RecordScope.applyOwnerFilter(query);
        return calcRecordMapper.selectCalcRecordTop(query.getCalcType(), query.getCreateBy(), limit);
    }

    @Override
    public CalcRecord selectCalcRecordById(Long id)
    {
        return calcRecordMapper.selectCalcRecordById(id);
    }

    @Override
    @Transactional
    public int deleteCalcRecordByIds(Long[] ids)
    {
        for (Long id : ids)
        {
            streamFlowMapper.deleteStreamFlowByCalcId(id);
        }
        return calcRecordMapper.deleteCalcRecordByIds(ids);
    }

    @Override
    @Transactional
    public int deleteCalcRecordById(Long id)
    {
        streamFlowMapper.deleteStreamFlowByCalcId(id);
        return calcRecordMapper.deleteCalcRecordById(id);
    }
}
