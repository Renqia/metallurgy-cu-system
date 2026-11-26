-- ----------------------------------------------------------------------------
-- 铜闪速冶炼工艺全流程计算与物质流分析系统 V1.0
-- 示例数据脚本
-- 说明：
--   1. 下列物料成分为课程大作业的【示例数据】，用于功能演示与回归测试，
--      并非任何具体矿山或企业的实测值（见 01-项目管理/开发文档/需求与范围.md）；
--   2. 本脚本是示例数据的事实来源，字段说明见 01-项目管理/开发文档/数据字典.md，
--      测试基准以 CalcEngineTest.java 中实际输入为准。
-- ----------------------------------------------------------------------------

-- ----------------------------
-- 1、物料台账示例数据
-- ----------------------------
delete from t_material;
insert into t_material (material_name, material_type, cu_content, fe_content, s_content, sio2_content, cao_content, unit_price, stock_qty, source, remark, create_by, create_time)
values ('铜精矿A（示例）', '铜精矿', 24.00, 26.00, 31.00, 6.00, 2.00, 5200.00, 1200.00, '示例产地A', '示例数据，非实测值', 'admin', sysdate());
insert into t_material (material_name, material_type, cu_content, fe_content, s_content, sio2_content, cao_content, unit_price, stock_qty, source, remark, create_by, create_time)
values ('铜精矿B（示例）', '铜精矿', 28.00, 24.00, 30.00, 8.00, 1.50, 5800.00, 800.00, '示例产地B', '示例数据，非实测值', 'admin', sysdate());
insert into t_material (material_name, material_type, cu_content, fe_content, s_content, sio2_content, cao_content, unit_price, stock_qty, source, remark, create_by, create_time)
values ('石英石（熔剂）', '熔剂', 0.00, 1.50, 0.00, 92.00, 2.00, 420.00, 500.00, '示例产地C', '造渣用熔剂', 'admin', sysdate());
insert into t_material (material_name, material_type, cu_content, fe_content, s_content, sio2_content, cao_content, unit_price, stock_qty, source, remark, create_by, create_time)
values ('石灰石（熔剂）', '熔剂', 0.00, 0.50, 0.00, 3.00, 52.00, 350.00, 600.00, '示例产地D', '调整渣型用熔剂', 'admin', sysdate());
insert into t_material (material_name, material_type, cu_content, fe_content, s_content, sio2_content, cao_content, unit_price, stock_qty, source, remark, create_by, create_time)
values ('返料烟尘（闪速炉）', '返料', 12.00, 20.00, 6.00, 8.00, 3.00, 0.00, 150.00, '闪速炉余热锅炉+电收尘', '返回配料系统', 'admin', sysdate());
insert into t_material (material_name, material_type, cu_content, fe_content, s_content, sio2_content, cao_content, unit_price, stock_qty, source, remark, create_by, create_time)
values ('吹炼渣（返回）', '返料', 2.50, 42.00, 1.00, 24.00, 3.00, 0.00, 200.00, '转炉吹炼', '返回闪速熔炼', 'admin', sysdate());

-- ----------------------------
-- 2、工艺参数方案示例数据
-- ----------------------------
delete from t_process_param;
insert into t_process_param (plan_name, oxygen_conc, oxygen_ratio, fe_sio2_ratio, matte_grade, capacity, status, remark, create_by, create_time)
values ('基准方案（富氧55%）', 55.00, 210.0000, 1.100, 55.00, 60.000, '1', '示例基准工况，用于回归测试', 'admin', sysdate());
insert into t_process_param (plan_name, oxygen_conc, oxygen_ratio, fe_sio2_ratio, matte_grade, capacity, status, remark, create_by, create_time)
values ('高品位对比方案（富氧65%）', 65.00, 265.0000, 1.050, 62.00, 60.000, '0', '提高冰铜品位的对比工况', 'admin', sysdate());
insert into t_process_param (plan_name, oxygen_conc, oxygen_ratio, fe_sio2_ratio, matte_grade, capacity, status, remark, create_by, create_time)
values ('低富氧对比方案（富氧40%）', 40.00, 150.0000, 1.200, 50.00, 55.000, '2', '降低富氧浓度的对比工况', 'admin', sysdate());
