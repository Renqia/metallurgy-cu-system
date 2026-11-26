package com.ruoyi.metallurgy.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 多方案对比请求
 *
 * <p>勾选 2~4 条全流程平衡计算记录（<code>calc_type=2</code>）进行指标并列对比。
 * 记录以结果快照为数据来源，只读、不落库。</p>
 *
 * @author 【成员1】
 * @date 2026-10-14
 */
public class CompareRequest
{
    /** 参与对比的计算记录ID（2~4 条，须为全流程平衡记录） */
    private List<Long> calcIds = new ArrayList<>();

    public List<Long> getCalcIds()
    {
        return calcIds;
    }

    public void setCalcIds(List<Long> calcIds)
    {
        this.calcIds = calcIds;
    }
}
