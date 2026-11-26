package com.ruoyi.metallurgy.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 物质流明细对象 t_stream_flow
 *
 * <p>把每次全流程计算的"节点 × 流向 × 物料 × 元素"流量落库，
 * 用于物质流报表展示与 Excel 导出。桑基图数据由结果快照即时装配，
 * 本表则承担明细留痕与报表导出的职责。</p>
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
public class StreamFlow extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 计算记录ID */
    private Long calcId;

    /** 工序节点 */
    @Excel(name = "工序节点")
    private String nodeName;

    /** 流向（1输入 2输出） */
    @Excel(name = "流向", readConverterExp = "1=输入,2=输出")
    private String flowType;

    /** 物料/产物名称 */
    @Excel(name = "物料/产物")
    private String materialName;

    /** 元素/化合物 */
    @Excel(name = "元素/化合物")
    private String element;

    /** 流量 */
    @Excel(name = "流量")
    private BigDecimal flowValue;

    /** 单位 */
    @Excel(name = "单位")
    private String unit;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getCalcId()
    {
        return calcId;
    }

    public void setCalcId(Long calcId)
    {
        this.calcId = calcId;
    }

    public String getNodeName()
    {
        return nodeName;
    }

    public void setNodeName(String nodeName)
    {
        this.nodeName = nodeName;
    }

    public String getFlowType()
    {
        return flowType;
    }

    public void setFlowType(String flowType)
    {
        this.flowType = flowType;
    }

    public String getMaterialName()
    {
        return materialName;
    }

    public void setMaterialName(String materialName)
    {
        this.materialName = materialName;
    }

    public String getElement()
    {
        return element;
    }

    public void setElement(String element)
    {
        this.element = element;
    }

    public BigDecimal getFlowValue()
    {
        return flowValue;
    }

    public void setFlowValue(BigDecimal flowValue)
    {
        this.flowValue = flowValue;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
}
