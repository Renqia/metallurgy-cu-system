package com.ruoyi.metallurgy.support;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.metallurgy.domain.CalcRecord;

/**
 * 计算记录的业务数据可见范围
 *
 * <p>规则（与"概览不绕过记录可见范围"的要求一致）：</p>
 * <ul>
 *   <li><b>超级管理员</b>（user_id = 1）可见全部计算记录；</li>
 *   <li><b>其他用户</b>仅可见本人创建（<code>create_by</code> 等于当前用户名）的记录。</li>
 * </ul>
 *
 * <p>实现说明：业务表 <code>t_calc_record</code> 只有 <code>create_by</code>、没有部门字段，
 * 因此若依原生的 {@code @DataScope}（依赖 dept_id）无法直接套用，
 * 这里以"创建人归属"作为数据可见范围的判定依据，并在列表查询、计数、详情与分析入口统一生效，
 * 避免出现"列表看不到、但用编号仍能直接打开"的绕过路径。</p>
 *
 * @author 【成员1】
 * @date 2026-10-14
 */
public final class RecordScope
{
    private RecordScope()
    {
    }

    /**
     * 当前登录用户是否为超级管理员（无安全上下文时按非管理员处理）
     *
     * @return true 超级管理员
     */
    public static boolean isAdmin()
    {
        try
        {
            return SecurityUtils.isAdmin(SecurityUtils.getUserId());
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * 当前登录用户名（无安全上下文时返回 null）
     *
     * @return 用户名
     */
    public static String currentUsername()
    {
        try
        {
            return SecurityUtils.getUsername();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * 给查询条件附加归属过滤：非超级管理员只统计/查询本人创建的记录
     *
     * @param query 查询条件（原地修改 createBy）
     */
    public static void applyOwnerFilter(CalcRecord query)
    {
        if (query == null || isAdmin())
        {
            return;
        }
        String username = currentUsername();
        if (username != null && !username.isEmpty())
        {
            query.setCreateBy(username);
        }
    }

    /**
     * 判断当前用户是否可见指定计算记录
     *
     * @param record 计算记录
     * @return true 可见
     */
    public static boolean canView(CalcRecord record)
    {
        if (record == null)
        {
            return false;
        }
        if (isAdmin())
        {
            return true;
        }
        String username = currentUsername();
        return username != null && username.equals(record.getCreateBy());
    }
}
