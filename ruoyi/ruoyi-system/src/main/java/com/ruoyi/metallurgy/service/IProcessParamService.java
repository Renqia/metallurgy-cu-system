package com.ruoyi.metallurgy.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.metallurgy.domain.ProcessParam;

/**
 * 工艺参数方案 服务层
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
public interface IProcessParamService
{
    /**
     * 查询工艺参数方案列表
     *
     * @param processParam 工艺参数方案
     * @return 工艺参数方案集合
     */
    public List<ProcessParam> selectProcessParamList(ProcessParam processParam);

    /**
     * 统计工艺参数方案条数（供首页概览使用）
     *
     * @param processParam 查询条件
     * @return 条数
     */
    public long countProcessParam(ProcessParam processParam);

    /**
     * 查询工艺参数方案
     *
     * @param id 工艺参数方案主键
     * @return 工艺参数方案
     */
    public ProcessParam selectProcessParamById(Long id);

    /**
     * 查询启用状态的参数方案
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
     * 批量删除工艺参数方案
     *
     * @param ids 需要删除的工艺参数方案主键集合
     * @return 结果
     */
    public int deleteProcessParamByIds(Long[] ids);

    /**
     * 删除工艺参数方案信息
     *
     * @param id 工艺参数方案主键
     * @return 结果
     */
    public int deleteProcessParamById(Long id);

    /**
     * 收集参数越界警告（**不阻断保存**）
     *
     * <p>工艺参数存在通用参考区间（见《需求与范围.md》），但现场工况可能超出文献范围，
     * 因此越界只提示、不拒绝保存。返回的列表为空表示全部参数都在参考区间内。</p>
     *
     * @param processParam 工艺参数方案
     * @return 警告信息列表（每项为一条中文提示）
     */
    public List<String> getParamWarnings(ProcessParam processParam);

    /**
     * 获取全部工艺参数的参考区间（供前端表单实时提示使用）
     *
     * @return 参数名 -> 区间描述
     */
    public Map<String, String> getParamRanges();
}
