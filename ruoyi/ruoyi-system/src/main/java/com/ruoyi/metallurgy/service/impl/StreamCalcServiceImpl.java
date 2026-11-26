package com.ruoyi.metallurgy.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.metallurgy.calc.FlowCalculator;
import com.ruoyi.metallurgy.calc.RatioRequestUtils;
import com.ruoyi.metallurgy.calc.RatioSolver;
import com.ruoyi.metallurgy.calc.SankeyBuilder;
import com.ruoyi.metallurgy.domain.CalcRecord;
import com.ruoyi.metallurgy.domain.ProcessParam;
import com.ruoyi.metallurgy.domain.StreamFlow;
import com.ruoyi.metallurgy.dto.BalanceResult;
import com.ruoyi.metallurgy.dto.NodeBalance;
import com.ruoyi.metallurgy.dto.RatioRequest;
import com.ruoyi.metallurgy.dto.RatioResult;
import com.ruoyi.metallurgy.dto.SankeyData;
import com.ruoyi.metallurgy.dto.StreamItem;
import com.ruoyi.metallurgy.mapper.CalcRecordMapper;
import com.ruoyi.metallurgy.mapper.StreamFlowMapper;
import com.ruoyi.metallurgy.service.IProcessParamService;
import com.ruoyi.metallurgy.service.IStreamCalcService;

/**
 * 铜闪速冶炼计算 服务层实现
 *
 * <p>负责算法编排与数据落库：计算过程本身与持久化解耦，
 * 计算异常统一以 <code>ServiceException</code> 抛出（由若依全局异常处理器转为中文提示），
 * 不产生 500 白屏。</p>
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
@Service
public class StreamCalcServiceImpl implements IStreamCalcService
{
    /** 计算类型：配料计算 */
    private static final String TYPE_RATIO = "1";

    /** 计算类型：全流程平衡 */
    private static final String TYPE_BALANCE = "2";

    /** 物料流明细口径 */
    private static final String[] FLOW_ELEMENTS = { "Cu", "Fe", "S", "SiO2", "CaO", "合计" };

    @Autowired
    private CalcRecordMapper calcRecordMapper;

    @Autowired
    private StreamFlowMapper streamFlowMapper;

    @Autowired
    private IProcessParamService processParamService;

    @Override
    public RatioResult calcRatio(RatioRequest request)
    {
        RatioResult result = RatioSolver.solve(request);
        if (request.isSaveRecord())
        {
            saveRecord(request, TYPE_RATIO, result, null);
        }
        return result;
    }

    @Override
    @Transactional
    public CalcRecord calcBalance(RatioRequest request)
    {
        RatioResult ratio = resolveRatio(request);
        BalanceResult balance = FlowCalculator.calculate(ratio, request.getOptions());
        CalcRecord record = saveRecord(request, TYPE_BALANCE, ratio, balance);
        // 物质流明细落库（供报表查询与导出）
        streamFlowMapper.deleteStreamFlowByCalcId(record.getId());
        List<StreamFlow> flows = buildStreamFlows(record.getId(), balance);
        if (!flows.isEmpty())
        {
            streamFlowMapper.batchInsertStreamFlow(flows);
        }
        return record;
    }

    @Override
    public BalanceResult getBalanceResult(Long calcId)
    {
        CalcRecord record = calcRecordMapper.selectCalcRecordById(calcId);
        if (record == null)
        {
            throw new ServiceException("计算记录不存在：" + calcId);
        }
        if (record.getResultSnapshot() == null || record.getResultSnapshot().isEmpty())
        {
            throw new ServiceException("该记录为配料计算记录，无全流程平衡结果，请先执行全流程计算");
        }
        try
        {
            return JSON.parseObject(record.getResultSnapshot(), BalanceResult.class);
        }
        catch (RuntimeException e)
        {
            // 快照损坏时给出可诊断的中文提示，而不是把底层解析异常原文抛到页面
            throw new ServiceException("计算记录 " + calcId
                    + " 的结果快照已损坏，无法读取平衡结果，请检查数据库中的快照内容或删除该记录后重新计算");
        }
    }

    @Override
    public SankeyData buildSankey(Long calcId, String element)
    {
        return SankeyBuilder.build(getBalanceResult(calcId), element);
    }

    @Override
    public List<StreamFlow> getStreamFlows(Long calcId)
    {
        return streamFlowMapper.selectStreamFlowByCalcId(calcId);
    }

    /**
     * 解析配比：前端已给出并经 100% 校验的配比直接采用（全部锁定），否则重新求解
     *
     * @param request 请求
     * @return 配料结果
     */
    private RatioResult resolveRatio(RatioRequest request)
    {
        if (request.getItems() == null || request.getItems().isEmpty())
        {
            throw new ServiceException("请先选择参与计算的物料");
        }
        // 按用户给定配比评估（把配比作为锁定值），仍复用同一套成分与约束计算逻辑
        RatioRequestUtils.applyLockedRatioIfComplete(request);
        return RatioSolver.solve(request);
    }

    /**
     * 保存计算记录（返回带主键的记录对象）
     *
     * @param request 请求
     * @param calcType 计算类型（1 配料 / 2 全流程）
     * @param ratio 配料结果
     * @param balance 全流程结果（配料计算时为 null）
     * @return 计算记录
     */
    private CalcRecord saveRecord(RatioRequest request, String calcType, RatioResult ratio, BalanceResult balance)
    {
        CalcRecord record = new CalcRecord();
        record.setCalcType(calcType);
        record.setPlanId(request.getPlanId());
        if (request.getPlanId() != null)
        {
            ProcessParam param = processParamService.selectProcessParamById(request.getPlanId());
            if (param != null)
            {
                record.setPlanName(param.getPlanName());
            }
        }
        record.setInputSnapshot(JSON.toJSONString(request));
        record.setCreateBy(currentUsername());
        record.setRemark(request.getRemark());
        if (ratio != null)
        {
            record.setMixedCu(scale(ratio.getMixedCu(), 3));
            record.setMatteGradeCalc(ratio.getMatteGradeAchieved());
            record.setSlagFeSio2(ratio.getFeSio2Achieved());
        }
        if (balance != null)
        {
            record.setResultSnapshot(JSON.toJSONString(balance));
            record.setDirectYield(balance.getIndicators().get("闪速熔炼铜直收率"));
            if (balance.getMixedMaterial() != null)
            {
                record.setMixedCu(scale(balance.getMixedMaterial().getCu(), 3));
            }
            BigDecimal matteGrade = findSmeltingIndicator(balance, "冰铜品位");
            if (matteGrade != null)
            {
                record.setMatteGradeCalc(matteGrade);
            }
            BigDecimal feSio2 = findSmeltingIndicator(balance, "炉渣硅铁比");
            if (feSio2 != null)
            {
                record.setSlagFeSio2(feSio2);
            }
        }
        else
        {
            // 配料计算也保存结果快照，便于在"计算记录"中回看配比
            record.setResultSnapshot(JSON.toJSONString(ratio));
        }
        calcRecordMapper.insertCalcRecord(record);
        return record;
    }

    /**
     * 取闪速熔炼节点的指定指标
     */
    private BigDecimal findSmeltingIndicator(BalanceResult balance, String name)
    {
        for (NodeBalance node : balance.getNodes())
        {
            if ("闪速熔炼".equals(node.getNodeName()))
            {
                return node.getIndicators().get(name);
            }
        }
        return null;
    }

    /**
     * 由平衡结果装配物质流明细（节点 × 流向 × 流 × 口径）
     *
     * @param calcId 计算记录ID
     * @param balance 平衡结果
     * @return 明细集合
     */
    private List<StreamFlow> buildStreamFlows(Long calcId, BalanceResult balance)
    {
        List<StreamFlow> list = new ArrayList<>();
        for (NodeBalance node : balance.getNodes())
        {
            for (StreamItem item : node.getInputs())
            {
                addFlow(list, calcId, node.getNodeName(), "1", item);
            }
            for (StreamItem item : node.getOutputs())
            {
                addFlow(list, calcId, node.getNodeName(), "2", item);
            }
        }
        return list;
    }

    /**
     * 把一条流按 6 个口径（Cu/Fe/S/SiO2/CaO/合计）展开为明细记录
     */
    private void addFlow(List<StreamFlow> list, Long calcId, String nodeName, String flowType, StreamItem item)
    {
        for (String element : FLOW_ELEMENTS)
        {
            BigDecimal value = item.massOf(element);
            if (value == null || value.compareTo(BigDecimal.ZERO) <= 0)
            {
                continue;
            }
            StreamFlow flow = new StreamFlow();
            flow.setCalcId(calcId);
            flow.setNodeName(nodeName);
            flow.setFlowType(flowType);
            flow.setMaterialName(item.getName());
            flow.setElement(element);
            flow.setFlowValue(value);
            flow.setUnit(item.getUnit());
            list.add(flow);
        }
    }

    /**
     * 取当前登录用户名（无安全上下文时返回 system，避免定时任务等场景报错）
     */
    private String currentUsername()
    {
        try
        {
            return SecurityUtils.getUsername();
        }
        catch (Exception e)
        {
            return "system";
        }
    }

    /**
     * BigDecimal 统一小数位
     */
    private BigDecimal scale(BigDecimal value, int scale)
    {
        return value == null ? null : value.setScale(scale, RoundingMode.HALF_UP);
    }
}
