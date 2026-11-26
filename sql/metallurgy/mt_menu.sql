-- ----------------------------------------------------------------------------
-- 铜闪速冶炼工艺全流程计算与物质流分析系统 V1.0
-- 菜单与按钮权限初始化脚本
-- 说明：
--   1. menu_id 从 2000 起编号，避开若依系统菜单（现有最大 menu_id = 1060）；
--   2. 菜单 perms 用于前端路由可见性判断，按钮 perms 用于后端 @PreAuthorize 校验；
--   3. 前端页面路径由 component = 'metallurgy/<模块>/index' 决定，
--      必须与 ruoyi-ui/src/views/metallurgy/<模块>/index.vue 精确对应；
--   4. 目录权限授予角色 2（普通角色），admin 用户为超级管理员自带 *:*:*，
--      无需单独授权即可看到全部菜单。
-- ----------------------------------------------------------------------------

-- ----------------------------
-- 1、一级目录
-- ----------------------------
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values (2000, '铜冶金计算', 0, 5, 'metallurgy', null, '', '', 1, 0, 'M', '0', '0', '', 'chart', 'admin', sysdate(), '铜闪速冶炼全流程计算与物质流分析');

-- ----------------------------
-- 2、物料台账管理
-- ----------------------------
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values (2001, '物料台账', 2000, 1, 'material', 'metallurgy/material/index', '', 'MetallurgyMaterial', 1, 0, 'C', '0', '0', 'metallurgy:material:list', 'table', 'admin', sysdate(), '铜精矿/熔剂/返料/烟尘等物料台账');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values (2101, '物料查询', 2001, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'metallurgy:material:query', '#', 'admin', sysdate(), '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values (2102, '物料新增', 2001, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'metallurgy:material:add', '#', 'admin', sysdate(), '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values (2103, '物料修改', 2001, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'metallurgy:material:edit', '#', 'admin', sysdate(), '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values (2104, '物料删除', 2001, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'metallurgy:material:remove', '#', 'admin', sysdate(), '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values (2105, '物料导出', 2001, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'metallurgy:material:export', '#', 'admin', sysdate(), '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values (2106, '物料导入', 2001, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'metallurgy:material:import', '#', 'admin', sysdate(), '');

-- ----------------------------
-- 3、工艺参数方案
-- ----------------------------
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values (2002, '工艺参数方案', 2000, 2, 'param', 'metallurgy/param/index', '', 'MetallurgyParam', 1, 0, 'C', '0', '0', 'metallurgy:param:list', 'edit', 'admin', sysdate(), '富氧浓度/氧料比/硅铁比/目标冰铜品位等参数方案');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values (2201, '方案查询', 2002, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'metallurgy:param:query', '#', 'admin', sysdate(), '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values (2202, '方案新增', 2002, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'metallurgy:param:add', '#', 'admin', sysdate(), '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values (2203, '方案修改', 2002, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'metallurgy:param:edit', '#', 'admin', sysdate(), '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values (2204, '方案删除', 2002, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'metallurgy:param:remove', '#', 'admin', sysdate(), '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values (2205, '方案导出', 2002, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'metallurgy:param:export', '#', 'admin', sysdate(), '');

-- ----------------------------
-- 4、配料计算
-- ----------------------------
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values (2003, '配料计算', 2000, 3, 'ratio', 'metallurgy/ratio/index', '', 'MetallurgyRatio', 1, 0, 'C', '0', '0', 'metallurgy:calc:ratio', 'cascader', 'admin', sysdate(), '目标冰铜品位与硅铁比约束下的配料求解');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values (2301, '配料计算执行', 2003, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'metallurgy:calc:ratio', '#', 'admin', sysdate(), '');

-- ----------------------------
-- 5、全流程物料平衡
-- ----------------------------
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values (2004, '全流程物料平衡', 2000, 4, 'balance', 'metallurgy/balance/index', '', 'MetallurgyBalance', 1, 0, 'C', '0', '0', 'metallurgy:calc:balance', 'tree', 'admin', sysdate(), '铜闪速冶炼全流程元素守恒与物料平衡');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values (2401, '全流程计算执行', 2004, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'metallurgy:calc:balance', '#', 'admin', sysdate(), '');

-- ----------------------------
-- 6、物质流分析
-- ----------------------------
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values (2005, '物质流分析', 2000, 5, 'stream', 'metallurgy/stream/index', '', 'MetallurgyStream', 1, 0, 'C', '0', '0', 'metallurgy:stream:view', 'chart', 'admin', sysdate(), '物质流桑基图与指标报表');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values (2501, '物质流导出', 2005, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'metallurgy:stream:export', '#', 'admin', sysdate(), '');

-- ----------------------------
-- 7、计算记录
-- ----------------------------
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values (2006, '计算记录', 2000, 6, 'record', 'metallurgy/record/index', '', 'MetallurgyRecord', 1, 0, 'C', '0', '0', 'metallurgy:record:list', 'list', 'admin', sysdate(), '历史计算输入/结果快照');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values (2601, '记录详情', 2006, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'metallurgy:record:query', '#', 'admin', sysdate(), '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values (2602, '记录删除', 2006, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'metallurgy:record:remove', '#', 'admin', sysdate(), '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values (2603, '记录导出', 2006, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'metallurgy:record:export', '#', 'admin', sysdate(), '');

-- ----------------------------
-- 8、角色授权（普通角色 role_id = 2）
-- ----------------------------
insert into sys_role_menu (role_id, menu_id)
select 2, menu_id from sys_menu where menu_id between 2000 and 2603;
