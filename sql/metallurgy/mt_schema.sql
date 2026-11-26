-- ----------------------------------------------------------------------------
-- 铜闪速冶炼工艺全流程计算与物质流分析系统 V1.0
-- 业务表结构脚本（4 张业务表）
-- 目标库：metallurgy_cu（MySQL 8.0）
-- 说明：
--   1. 本脚本只新增业务表，不修改任何若依系统表结构；
--   2. 所有列注释均为中文，且与页面表单标签、说明书术语保持一致
--      （若依代码生成器依赖列注释生成表单/列表标签）；
--   3. 审计字段与若依 BaseEntity 对应（create_by/create_time/update_by/update_time）。
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
-- 1、物料台账表
-- ----------------------------------------------------------------------------
drop table if exists t_material;
create table t_material (
  id              bigint(20)      not null auto_increment    comment '主键ID',
  material_name   varchar(64)     not null                   comment '物料名称',
  material_type   varchar(32)     not null                   comment '物料类型（铜精矿/熔剂/返料/烟尘/燃料）',
  cu_content      decimal(6,2)    default 0                  comment 'Cu含量(%)',
  fe_content      decimal(6,2)    default 0                  comment 'Fe含量(%)',
  s_content       decimal(6,2)    default 0                  comment 'S含量(%)',
  sio2_content    decimal(6,2)    default 0                  comment 'SiO2含量(%)',
  cao_content     decimal(6,2)    default 0                  comment 'CaO含量(%)',
  unit_price      decimal(12,2)   default 0                  comment '单价(元/吨)',
  stock_qty       decimal(12,2)   default 0                  comment '库存量(吨)',
  source          varchar(128)    default ''                 comment '来源/产地',
  remark          varchar(255)    default ''                 comment '备注',
  create_by       varchar(64)     default ''                 comment '创建者',
  create_time     datetime                                   comment '创建时间',
  update_by       varchar(64)     default ''                 comment '更新者',
  update_time     datetime                                   comment '更新时间',
  primary key (id),
  key idx_material_type (material_type)
) engine=innodb auto_increment=1 comment = '物料台账表';

-- ----------------------------------------------------------------------------
-- 2、工艺参数方案表
-- ----------------------------------------------------------------------------
drop table if exists t_process_param;
create table t_process_param (
  id              bigint(20)      not null auto_increment    comment '主键ID',
  plan_name       varchar(64)     not null                   comment '方案名称',
  oxygen_conc     decimal(6,2)    default 55.00              comment '富氧浓度(vol%)',
  oxygen_ratio    decimal(10,4)   default 0                  comment '氧料比(Nm3/t)',
  fe_sio2_ratio   decimal(6,3)    default 1.100              comment '硅铁比Fe/SiO2',
  matte_grade     decimal(6,2)    default 55.00              comment '目标冰铜品位(%)',
  capacity        decimal(12,3)   default 60.000             comment '处理量(t/h)',
  status          char(1)         default '0'                comment '状态（0草稿 1启用 2停用）',
  remark          varchar(255)    default ''                 comment '备注',
  create_by       varchar(64)     default ''                 comment '创建者',
  create_time     datetime                                   comment '创建时间',
  update_by       varchar(64)     default ''                 comment '更新者',
  update_time     datetime                                   comment '更新时间',
  primary key (id)
) engine=innodb auto_increment=1 comment = '工艺参数方案表';

-- ----------------------------------------------------------------------------
-- 3、计算记录表（保存输入/结果快照，保证计算可追溯）
-- ----------------------------------------------------------------------------
drop table if exists t_calc_record;
create table t_calc_record (
  id                bigint(20)    not null auto_increment    comment '主键ID',
  plan_id           bigint(20)    default null               comment '参数方案ID',
  plan_name         varchar(64)   default ''                 comment '参数方案名称（冗余，便于列表展示）',
  calc_type         char(1)       not null                   comment '计算类型（1配料计算 2全流程平衡）',
  input_snapshot    longtext                                 comment '输入快照（JSON文本）',
  result_snapshot   longtext                                 comment '结果快照（JSON文本）',
  mixed_cu          decimal(6,2)  default null               comment '混合料Cu品位(%)',
  matte_grade_calc  decimal(6,2)  default null               comment '计算冰铜品位(%)',
  slag_fe_sio2      decimal(6,3)  default null               comment '计算炉渣硅铁比',
  direct_yield      decimal(6,2)  default null               comment '铜直收率(%)',
  calc_time         datetime                                 comment '计算时间',
  create_by         varchar(64)   default ''                 comment '创建人',
  remark            varchar(255)  default ''                 comment '备注',
  primary key (id),
  key idx_calc_plan (plan_id)
) engine=innodb auto_increment=1 comment = '计算记录表';

-- ----------------------------------------------------------------------------
-- 4、物质流明细表（按节点/流向/元素保存流量，供桑基图与报表使用）
-- ----------------------------------------------------------------------------
drop table if exists t_stream_flow;
create table t_stream_flow (
  id              bigint(20)      not null auto_increment    comment '主键ID',
  calc_id         bigint(20)      not null                   comment '计算记录ID',
  node_name       varchar(32)     not null                   comment '工序节点（配料/闪速熔炼/转炉吹炼/阳极精炼/电解精炼）',
  flow_type       char(1)         not null                   comment '流向（1输入 2输出）',
  material_name   varchar(64)     not null                   comment '物料/产物名称',
  element         varchar(16)     not null                   comment '元素/化合物（Cu/Fe/S/SiO2/CaO/合计）',
  flow_value      decimal(16,4)   default 0                  comment '流量',
  unit            varchar(16)     default 'kg'               comment '单位',
  create_time     datetime                                   comment '创建时间',
  primary key (id),
  key idx_stream_calc (calc_id),
  key idx_stream_node (calc_id, node_name)
) engine=innodb auto_increment=1 comment = '物质流明细表';
