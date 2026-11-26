package com.ruoyi.metallurgy.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.metallurgy.calc.IndicatorCatalog;
import com.ruoyi.metallurgy.calc.SensitivityCalculator;
import com.ruoyi.metallurgy.domain.CalcRecord;
import com.ruoyi.metallurgy.dto.BalanceResult;
import com.ruoyi.metallurgy.dto.CompareColumn;
import com.ruoyi.metallurgy.dto.CompareRequest;
import com.ruoyi.metallurgy.dto.CompareResult;
import com.ruoyi.metallurgy.dto.RatioRequest;
import com.ruoyi.metallurgy.dto.SensitivityRequest;
import com.ruoyi.metallurgy.dto.SensitivityResult;
import com.ruoyi.metallurgy.mapper.CalcRecordMapper;
import com.ruoyi.metallurgy.service.IAnalysisService;
import com.ruoyi.metallurgy.support.RecordScope;

/**
 * 方案对比与敏感性分析 服务层实现
 *
 * <p>职责边界：本服务只负责"取快照 + 校验 + 组织结果"，
 * 指标抽取与逐点重算全部委托给纯算法类（{@code IndicatorCatalog} / {@code SensitivityCalculator}），
 * 既保证与既有计算链路口径一致，也保证算法可被单元测试直接覆盖（无需数据库）。</p>
 *
 * <p>数据安全：所有方法均为只读；敏感性扫描的重算结果<b>不写库</b>，
 * 因此不会污染计算记录表与物质流明细表。</p>
 *
 * @author 【成员1】
 * @date 2026-10-14
 */
@Service
public class AnalysisServiceImpl implements IAnalysisService
{
    /** 计算类型：全流程平衡（只有该类型可用于对比与扫描） */
    private static final String TYPE_BALANCE = "2";

    /** 对比记录数量下限 */
    private static final int MIN_COMPARE_COUNT = 2;

    /** 对比记录数量上限 */
    private static final int MAX_COMPARE_COUNT = 4;

    @Autowired
    private CalcRecordMapper calcRecordMapper;

    @Override
    public CompareResult compare(CompareRequest request)
    {
        List<Long> calcIds = request == null || request.getCalcIds() == null
                ? new ArrayList<>() : request.getCalcIds();
        if (calcIds.size() < MIN_COMPARE_COUNT || calcIds.size() > MAX_COMPARE_COUNT)
        {
            throw new ServiceException("请选择 " + MIN_COMPARE_COUNT + "~" + MAX_COMPARE_COUNT
                    + " 条全流程计算记录进行对比，当前已选 " + calcIds.size() + " 条");
        }
        if (new HashSet<>(calcIds).size() != calcIds.size())
        {
            throw new ServiceException("存在重复选择的计算记录，请重新选择后再对比");
        }

        List<BalanceResult> results = new ArrayList<>();
        List<CompareColumn> columns = new ArrayList<>();
        boolean optionsMissing = false;
        for (Long calcId : calcIds)
        {
            if (calcId == null)
            {
                throw new ServiceException("存在无效的计算记录，请重新选择后再对比");
            }
            CalcRecord record = calcRecordMapper.selectCalcRecordById(calcId);
            requireBalanceRecord(record, calcId);
            BalanceResult balance = parseBalance(record.getResultSnapshot(), calcId);
            results.add(balance);

            CompareColumn column = new CompareColumn();
            column.setCalcId(calcId);
            column.setPlanName(record.getPlanName());
            column.setCalcTime(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, record.getCalcTime()));
            column.setRemark(record.getRemark());
            column.setFeasible(balance.isFeasible());
            column.setMaxResidual(balance.getMaxResidual());
            columns.add(column);

            RatioRequest input = parseInput(record.getInputSnapshot());
            if (input == null || input.getOptions() == null)
            {
                optionsMissing = true;
            }
        }

        CompareResult result = new CompareResult();
        result.setColumns(columns);
        result.setIndicators(IndicatorCatalog.extract(results));
        result.getNotes().add("对比直接读取各记录保存时的结果快照，不重新计算，因此可如实反映当时的计算结果");
        if (optionsMissing)
        {
            result.getNotes().add("部分记录的输入快照未保存模型参数（CalcOptions），"
                    + "其指标由当时的模型默认参数算得；如需按统一参数重新比选，请使用敏感性分析逐点重算");
        }
        return result;
    }

    @Override
    public SensitivityResult sensitivity(SensitivityRequest request)
    {
        if (request == null || request.getBaseCalcId() == null)
        {
            throw new ServiceException("请选择作为敏感性分析基线的一条全流程计算记录");
        }
        Long baseCalcId = request.getBaseCalcId();
        CalcRecord record = calcRecordMapper.selectCalcRecordById(baseCalcId);
        requireBalanceRecord(record, baseCalcId);
        RatioRequest base = parseInput(record.getInputSnapshot());
        if (base == null)
        {
            throw new ServiceException("计算记录 " + baseCalcId + " 的输入快照缺失或已损坏，无法作为敏感性分析的基线");
        }

        SensitivityResult result = SensitivityCalculator.scan(base, request.getField(),
                request.getStart(), request.getEnd(), request.getStep());
        result.setBaseCalcId(baseCalcId);
        result.setPlanName(record.getPlanName());

        BalanceResult baseBalance = tryParseBalance(record.getResultSnapshot());
        if (baseBalance == null)
        {
            result.getNotes().add("基线记录的结果快照无法解析，图中不显示基线对照值");
        }
        else
        {
            result.setBaseIndicators(IndicatorCatalog.extractOne(baseBalance));
        }
        result.getNotes().add("结果基于记录 " + baseCalcId + " 的输入快照实时重算，用于观察参数影响的规律，"
                + "教学级简化模型的结论不替代现场标定");
        return result;
    }

    /**
     * 校验记录可用于对比/扫描：必须存在、必须是全流程记录、必须有结果快照
     */
    private void requireBalanceRecord(CalcRecord record, Long calcId)
    {
        if (record == null)
        {
            throw new ServiceException("计算记录不存在：" + calcId);
        }
        // 数据可见范围：非超级管理员不得对比/扫描他人创建的记录
        if (!RecordScope.canView(record))
        {
            throw new ServiceException("没有权限查看计算记录 " + calcId);
        }
        if (!TYPE_BALANCE.equals(record.getCalcType()))
        {
            throw new ServiceException("计算记录 " + calcId + " 为配料计算记录，不含全流程平衡结果，"
                    + "无法参与方案对比或敏感性分析");
        }
        if (record.getResultSnapshot() == null || record.getResultSnapshot().isEmpty())
        {
            throw new ServiceException("计算记录 " + calcId + " 缺少结果快照，无法参与方案对比或敏感性分析");
        }
    }

    /**
     * 解析结果快照；解析失败或内容不完整时抛出中文业务异常（避免前端拿到空表或 500）
     */
    private BalanceResult parseBalance(String snapshot, Long calcId)
    {
        BalanceResult balance;
        try
        {
            balance = JSON.parseObject(snapshot, BalanceResult.class);
        }
        catch (RuntimeException e)
        {
            throw new ServiceException("计算记录 " + calcId + " 的结果快照已损坏，无法参与方案对比或敏感性分析");
        }
        if (balance == null || balance.getNodes() == null || balance.getNodes().isEmpty())
        {
            throw new ServiceException("计算记录 " + calcId + " 的结果快照内容不完整，无法参与方案对比或敏感性分析");
        }
        return balance;
    }

    /**
     * 解析结果快照（容错版本，供基线对照值使用）
     */
    private BalanceResult tryParseBalance(String snapshot)
    {
        try
        {
            return JSON.parseObject(snapshot, BalanceResult.class);
        }
        catch (RuntimeException e)
        {
            return null;
        }
    }

    /**
     * 解析输入快照（容错版本；为空或损坏时返回 null，由调用方降级处理）
     */
    private RatioRequest parseInput(String snapshot)
    {
        if (snapshot == null || snapshot.isEmpty())
        {
            return null;
        }
        try
        {
            return JSON.parseObject(snapshot, RatioRequest.class);
        }
        catch (RuntimeException e)
        {
            return null;
        }
    }
}
