package com.ruoyi.metallurgy.calc;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * 铜闪速冶炼计算所用物性系数与模型默认参数
 *
 * <p>全部系数由原子量推导，推导过程写入软件说明书"理论依据"章节，便于校核：</p>
 * <ul>
 *   <li>Cu 相对原子质量 63.546，S 32.06，Fe 55.845，O 16.00</li>
 *   <li>Cu2S（辉铜矿/冰铜主相）：M = 2×63.546 + 32.06 = 159.152，Cu 质量分数 = 127.092/159.152 = 0.79856</li>
 *   <li>FeS（磁黄铁矿/冰铜次相）：M = 55.845 + 32.06 = 87.905，Fe 质量分数 = 0.63542，S 质量分数 = 0.36458</li>
 *   <li>FeO：M = 55.845 + 16.00 = 71.845，Fe 质量分数 = 0.77730</li>
 *   <li>SO2：M = 32.06 + 2×16.00 = 64.06，S 质量分数 = 0.50047</li>
 * </ul>
 *
 * @author 【成员1】
 * @date 2026-10-08
 */
public final class MetallurgyConstants
{
    private MetallurgyConstants()
    {
    }

    /** 高精度计算上下文 */
    public static final MathContext MC = new MathContext(12);

    /** Cu2S 中 Cu 的质量分数 */
    public static final BigDecimal CU_IN_CU2S = new BigDecimal("0.7985574");

    /** Cu2S 中 Cu2S 质量与 Cu 质量之比（= 1 / CU_IN_CU2S） */
    public static final BigDecimal CU2S_PER_CU = new BigDecimal("1.2522582");

    /** Cu2S 中 S 的质量分数 */
    public static final BigDecimal S_IN_CU2S = new BigDecimal("0.2014426");

    /** FeS 中 Fe 的质量分数 */
    public static final BigDecimal FE_IN_FES = new BigDecimal("0.6354221");

    /** FeS 中 S 的质量分数 */
    public static final BigDecimal S_IN_FES = new BigDecimal("0.3645779");

    /** FeO 中 Fe 的质量分数（用于由 Fe 质量折算 FeO 质量，即渣中 Fe 的赋存形态） */
    public static final BigDecimal FE_IN_FEO = new BigDecimal("0.7773000");

    /** SO2 中 S 的质量分数（用于由入烟气的 S 折算 SO2 质量） */
    public static final BigDecimal S_IN_SO2 = new BigDecimal("0.5004683");

    // ------------------------------------------------------------------
    // 模型默认参数（示例取值，均可通过请求覆盖；写入说明书并标注为示例值）
    // ------------------------------------------------------------------

    /** 默认烟尘率(%)：闪速熔炼烟尘量占混合料的比例 */
    public static final BigDecimal DEFAULT_DUST_RATE = new BigDecimal("2.00");

    /** 默认渣含铜(%)：闪速熔炼炉渣中的铜损失 */
    public static final BigDecimal DEFAULT_SLAG_CU = new BigDecimal("0.80");

    /** 默认粗铜品位(%)：转炉吹炼产物粗铜的含铜量 */
    public static final BigDecimal DEFAULT_BLISTER_GRADE = new BigDecimal("98.50");

    /** 默认吹炼渣含铜(%) */
    public static final BigDecimal DEFAULT_BLISTER_SLAG_CU = new BigDecimal("1.50");

    /** 默认吹炼渣硅铁比（Fe/SiO2） */
    public static final BigDecimal DEFAULT_BLISTER_FE_SIO2 = new BigDecimal("1.80");

    /** 默认阳极铜品位(%) */
    public static final BigDecimal DEFAULT_ANODE_GRADE = new BigDecimal("99.50");

    /** 默认阳极精炼铜回收率(%)（与阳极铜品位是两个独立概念：前者是分配率，后者是产物纯度） */
    public static final BigDecimal DEFAULT_REFINE_YIELD = new BigDecimal("99.50");

    /** 默认阳极精炼渣含铜(%)（火法精炼渣含铜较高） */
    public static final BigDecimal DEFAULT_REFINE_SLAG_CU = new BigDecimal("25.00");

    /** 默认电解铜回收率(%) */
    public static final BigDecimal DEFAULT_ELECTRO_YIELD = new BigDecimal("99.50");

    /** 默认阴极铜品位(%) */
    public static final BigDecimal DEFAULT_CATHODE_GRADE = new BigDecimal("99.99");

    /** 默认阳极泥率(%)：阳极泥质量占阳极铜质量比例 */
    public static final BigDecimal DEFAULT_SLIME_RATE = new BigDecimal("0.80");

    /** 用于代入计算的石英石成分示例值(%) */
    public static final BigDecimal QUARTZ_SIO2 = new BigDecimal("92.00");

    /** 石英石 Fe 含量示例值(%) */
    public static final BigDecimal QUARTZ_FE = new BigDecimal("1.50");

    /** 石英石 CaO 含量示例值(%) */
    public static final BigDecimal QUARTZ_CAO = new BigDecimal("2.00");

    /** 元素平衡残差容差(%)：超过该值视为计算异常，页面给出警示 */
    public static final BigDecimal RESIDUAL_TOLERANCE = new BigDecimal("0.50");

    /** 计算基准(kg 混合料) */
    public static final BigDecimal BASIS_MASS = new BigDecimal("100");
}
