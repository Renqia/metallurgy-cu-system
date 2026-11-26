package com.ruoyi.metallurgy.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.metallurgy.domain.ProcessParam;
import com.ruoyi.metallurgy.mapper.ProcessParamMapper;
import com.ruoyi.metallurgy.service.IProcessParamService;

/**
 * 工艺参数方案 服务层实现
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
@Service
public class ProcessParamServiceImpl implements IProcessParamService
{
    /** 富氧浓度参考下限（vol%，空气为 21） */
    private static final BigDecimal OXYGEN_CONC_MIN = new BigDecimal("21");

    /** 富氧浓度参考上限（vol%） */
    private static final BigDecimal OXYGEN_CONC_MAX = new BigDecimal("75");

    /** 目标冰铜品位参考下限（%） */
    private static final BigDecimal MATTE_GRADE_MIN = new BigDecimal("45");

    /** 目标冰铜品位参考上限（%） */
    private static final BigDecimal MATTE_GRADE_MAX = new BigDecimal("65");

    /** 硅铁比参考下限 */
    private static final BigDecimal FE_SIO2_MIN = new BigDecimal("0.8");

    /** 硅铁比参考上限 */
    private static final BigDecimal FE_SIO2_MAX = new BigDecimal("1.4");

    @Autowired
    private ProcessParamMapper processParamMapper;

    @Override
    public List<ProcessParam> selectProcessParamList(ProcessParam processParam)
    {
        return processParamMapper.selectProcessParamList(processParam);
    }

    @Override
    public long countProcessParam(ProcessParam processParam)
    {
        return processParamMapper.countProcessParam(processParam);
    }

    @Override
    public ProcessParam selectProcessParamById(Long id)
    {
        return processParamMapper.selectProcessParamById(id);
    }

    @Override
    public List<ProcessParam> selectEnabledProcessParam()
    {
        return processParamMapper.selectEnabledProcessParam();
    }

    @Override
    public int insertProcessParam(ProcessParam processParam)
    {
        checkRequired(processParam);
        return processParamMapper.insertProcessParam(processParam);
    }

    @Override
    public int updateProcessParam(ProcessParam processParam)
    {
        checkRequired(processParam);
        return processParamMapper.updateProcessParam(processParam);
    }

    @Override
    public int deleteProcessParamByIds(Long[] ids)
    {
        for (Long id : ids)
        {
            checkReferenced(id);
        }
        return processParamMapper.deleteProcessParamByIds(ids);
    }

    @Override
    public int deleteProcessParamById(Long id)
    {
        checkReferenced(id);
        return processParamMapper.deleteProcessParamById(id);
    }

    @Override
    public List<String> getParamWarnings(ProcessParam processParam)
    {
        List<String> warnings = new ArrayList<>();
        if (processParam == null)
        {
            return warnings;
        }
        checkRange(warnings, "富氧浓度", processParam.getOxygenConc(), OXYGEN_CONC_MIN, OXYGEN_CONC_MAX, "vol%");
        checkRange(warnings, "目标冰铜品位", processParam.getMatteGrade(), MATTE_GRADE_MIN, MATTE_GRADE_MAX, "%");
        checkRange(warnings, "硅铁比 Fe/SiO2", processParam.getFeSio2Ratio(), FE_SIO2_MIN, FE_SIO2_MAX, "");
        return warnings;
    }

    @Override
    public Map<String, String> getParamRanges()
    {
        Map<String, String> ranges = new LinkedHashMap<>();
        ranges.put("oxygenConc", "21 ~ 75 vol%（常用 40 ~ 65）");
        ranges.put("oxygenRatio", "由氧平衡反算，随物料成分与冰铜品位变化");
        ranges.put("feSio2Ratio", "0.8 ~ 1.4（常用 1.0 ~ 1.2）");
        ranges.put("matteGrade", "45 ~ 65 %（常用 50 ~ 60）");
        ranges.put("capacity", "大于 0，按产线能力填写");
        return ranges;
    }

    /**
     * 必填项与基本合法性校验（阻断保存）
     *
     * @param processParam 工艺参数方案
     */
    private void checkRequired(ProcessParam processParam)
    {
        if (StringUtils.isEmpty(processParam.getPlanName()))
        {
            throw new ServiceException("方案名称不能为空");
        }
        if (processParam.getCapacity() != null && processParam.getCapacity().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("处理量必须大于 0 t/h");
        }
        if (processParam.getOxygenRatio() != null && processParam.getOxygenRatio().compareTo(BigDecimal.ZERO) < 0)
        {
            throw new ServiceException("氧料比不能为负数");
        }
        if (processParam.getOxygenConc() != null
                && (processParam.getOxygenConc().compareTo(BigDecimal.ZERO) <= 0
                    || processParam.getOxygenConc().compareTo(new BigDecimal("100")) > 0))
        {
            throw new ServiceException("富氧浓度必须在 0 ~ 100 vol% 之间");
        }
    }

    /**
     * 参数越界检查（只提示、不阻断）
     */
    private void checkRange(List<String> warnings, String label, BigDecimal value,
            BigDecimal min, BigDecimal max, String unit)
    {
        if (value == null)
        {
            return;
        }
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0)
        {
            warnings.add(label + " 当前值 " + value.stripTrailingZeros().toPlainString() + unit
                    + " 超出参考区间 " + min.stripTrailingZeros().toPlainString() + " ~ "
                    + max.stripTrailingZeros().toPlainString() + unit + "，请确认工况是否合理");
        }
    }

    /**
     * 删除前校验方案是否已被计算记录引用
     *
     * @param id 方案主键
     */
    private void checkReferenced(Long id)
    {
        int count = processParamMapper.countCalcRecordByPlanId(id);
        if (count > 0)
        {
            throw new ServiceException("该参数方案已被 " + count + " 条计算记录引用，不能删除（可改为停用）");
        }
    }
}
