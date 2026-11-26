package com.ruoyi.metallurgy.service;

import com.ruoyi.metallurgy.dto.CompareRequest;
import com.ruoyi.metallurgy.dto.CompareResult;
import com.ruoyi.metallurgy.dto.SensitivityRequest;
import com.ruoyi.metallurgy.dto.SensitivityResult;

/**
 * 方案对比与敏感性分析 服务层
 *
 * <p>两个能力都<b>只读</b>既有计算记录的快照，不新增数据表、不写入数据库：</p>
 * <ul>
 *   <li>方案对比：并列展示 2~4 条全流程记录的结果指标，用于"同一配比不同参数"或"不同配比"的方案比选；</li>
 *   <li>敏感性分析：以一条记录为基线，对指定模型参数逐点真实重算，观察指标趋势。</li>
 * </ul>
 *
 * @author 【成员1】
 * @date 2026-10-14
 */
public interface IAnalysisService
{
    /**
     * 多方案指标对比
     *
     * @param request 对比请求（含 2~4 条全流程计算记录ID）
     * @return 对比结果（列 = 记录，行 = 统一指标目录）
     */
    public CompareResult compare(CompareRequest request);

    /**
     * 敏感性分析（逐点重算，不落库）
     *
     * @param request 扫描请求（基线记录、扫描字段、起点、终点、步长）
     * @return 扫描结果（含失败点与基线对照值）
     */
    public SensitivityResult sensitivity(SensitivityRequest request);
}
