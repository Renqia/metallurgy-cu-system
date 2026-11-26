package com.ruoyi.metallurgy.mapper;

import java.util.List;
import com.ruoyi.metallurgy.domain.ProcessParam;

/**
 * 工艺参数方案 数据层
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
public interface ProcessParamMapper
{
    /**
     * 查询工艺参数方案
     *
     * @param id 工艺参数方案主键
     * @return 工艺参数方案
     */
    public ProcessParam selectProcessParamById(Long id);

    /**
     * 查询工艺参数方案列表
     *
     * @param processParam 工艺参数方案
     * @return 工艺参数方案集合
     */
    public List<ProcessParam> selectProcessParamList(ProcessParam processParam);

    /**
     * 统计工艺参数方案条数（供首页概览使用，避免全量查询后在内存里取 size）
     *
     * @param processParam 查询条件
     * @return 条数
     */
    public long countProcessParam(ProcessParam processParam);

    /**
     * 查询启用状态的方案（供计算页默认选中）
     *
     * @return 工艺参数方案集合
     */
    public List<ProcessParam> selectEnabledProcessParam();

    /**
     * 新增工艺参数方案
     *
     * @param processParam 工艺参数方案
     * @return 结果
     */
    public int insertProcessParam(ProcessParam processParam);

    /**
     * 修改工艺参数方案
     *
     * @param processParam 工艺参数方案
     * @return 结果
     */
    public int updateProcessParam(ProcessParam processParam);

    /**
     * 删除工艺参数方案
     *
     * @param id 工艺参数方案主键
     * @return 结果
     */
    public int deleteProcessParamById(Long id);

    /**
     * 批量删除工艺参数方案
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteProcessParamByIds(Long[] ids);

    /**
     * 统计引用了指定参数方案的计算记录条数（删除前校验）
     *
     * @param id 方案主键
     * @return 引用条数
     */
    public int countCalcRecordByPlanId(Long id);
}
