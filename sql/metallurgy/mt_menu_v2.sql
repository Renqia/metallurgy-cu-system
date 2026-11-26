-- ----------------------------------------------------------------------------
-- 铜闪速冶炼工艺全流程计算与物质流分析系统 V1.0 —— 菜单裁剪与新增脚本（v1.2）
--
-- 变更说明（对应 01-项目管理/开发文档/开发交接.md 的"菜单裁剪与恢复"章节）：
--   1) 删除与本系统无关的若依原装菜单及其按钮权限与角色关联：
--      若依官网(4)、系统工具整套(3,115,116,117 及子按钮)、
--      系统监控中定时任务/数据监控/服务监控/缓存监控/缓存列表(110,111,112,113,114 及子按钮)、
--      部门管理(103)、岗位管理(104)、通知公告(107) 及各自子按钮
--   2) 调整顶层菜单顺序：铜冶金计算(2000) 在前
--   3) 新增"敏感性分析"菜单(2007) 与按钮权限，以及计算记录的"方案对比"按钮权限
--
-- 【v1.2 相比 v1.1 的改动】
--   a) 用递归查询收集"待删菜单的全部后代"，不再只清一层，避免深层自定义子菜单被遗留为孤儿权限；
--   b) 整个变更包在单个事务中（DDL 备份除外），中途失败不会留下半执行状态；
--   c) 执行前自动生成两张备份表（sys_menu_bak_v2 / sys_role_menu_bak_v2），并在文末给出可直接
--      执行的回滚语句，回滚不再依赖"重跑框架脚本"；
--   d) 幂等性增强：待删集合的删除天然可重复执行，新增块仍为先 DELETE 后 INSERT，重复执行结果一致；
--   e) 修正菜单 2007 的备注语义：扫描字段为"目标冰铜品位 / 转炉吹炼渣硅铁比"，
--      **不是**闪速熔炼目标硅铁比（见《需求与范围.md》§5 模型边界）。
--
-- 【前置条件】
--   1) 前端页面 02-源码/ruoyi-ui/src/views/metallurgy/analysis/index.vue 必须已存在，
--      否则菜单 2007 点开为白屏（component 路径为 metallurgy/analysis/index）；
--   2) 角色语义（已核实）：role_id=1 为"超级管理员"，role_id=2 为"普通角色"；
--      超级管理员不依赖 sys_role_menu（走旁路），此处仍一并补关联以便普通角色可见。
--
-- 【执行方式】（Windows 中文路径注意：先复制到纯英文路径再导入）
--   方式一（cmd.exe / Git Bash，支持 < 重定向）：
--     mysql -u root -p --default-character-set=utf8mb4 metallurgy_cu < mt_menu_v2.sql
--   方式二（PowerShell 不支持 < 重定向，请先进客户端再 source）：
--     mysql -u root -p --default-character-set=utf8mb4
--     mysql> USE metallurgy_cu;
--     mysql> source D:/path/to/mt_menu_v2.sql
--   注意：-p 为交互输入密码，请勿把真实口令写入本文件或文档。
--
-- 【回滚方式】见文末第 5 节（脚本会自动备份，按其中的 SQL 即可恢复）。
--
-- 【执行状态】本脚本已在本地开发库 metallurgy_cu 执行并通过重复执行验证；
--   已执行库再次运行属无副作用重跑（删除为幂等、新增为先删后插）。
-- ----------------------------------------------------------------------------

USE metallurgy_cu;

-- ========== 0. 执行前备份（可重复执行：每次覆盖上一次的备份） ==========

DROP TABLE IF EXISTS sys_menu_bak_v2;
CREATE TABLE sys_menu_bak_v2 AS SELECT * FROM sys_menu;

DROP TABLE IF EXISTS sys_role_menu_bak_v2;
CREATE TABLE sys_role_menu_bak_v2 AS SELECT * FROM sys_role_menu;

-- 收集"待删菜单 + 其全部后代"（递归，覆盖任意层级），供后续按集合删除
DROP TEMPORARY TABLE IF EXISTS tmp_menu_to_delete;
CREATE TEMPORARY TABLE tmp_menu_to_delete (menu_id BIGINT NOT NULL PRIMARY KEY);

INSERT IGNORE INTO tmp_menu_to_delete (menu_id)
SELECT menu_id FROM (
    WITH RECURSIVE menu_tree AS (
        SELECT menu_id FROM sys_menu
        WHERE menu_id IN (3, 4, 103, 104, 107, 110, 111, 112, 113, 114, 115, 116, 117)
        UNION ALL
        SELECT m.menu_id FROM sys_menu m INNER JOIN menu_tree t ON m.parent_id = t.menu_id
    )
    SELECT menu_id FROM menu_tree
) AS tree;

START TRANSACTION;

-- ========== 1. 删除无关菜单（先角色关联，再菜单本体；含全部层级后代） ==========

DELETE rm FROM sys_role_menu rm INNER JOIN tmp_menu_to_delete t ON t.menu_id = rm.menu_id;
DELETE m FROM sys_menu m INNER JOIN tmp_menu_to_delete t ON t.menu_id = m.menu_id;

-- ========== 2. 调整顶层菜单顺序：铜冶金计算在前 ==========

UPDATE sys_menu SET order_num = 1 WHERE menu_id = 2000;   -- 铜冶金计算
UPDATE sys_menu SET order_num = 2 WHERE menu_id = 1;      -- 系统管理
UPDATE sys_menu SET order_num = 3 WHERE menu_id = 2;      -- 系统监控（保留在线用户与日志）

-- ========== 3. 新增"敏感性分析"菜单与按钮权限 ==========

-- 3.1 幂等清理（重复执行时先移除上一次插入的行，再重建）
DELETE FROM sys_role_menu WHERE menu_id IN (2007, 2701, 2702, 2703, 2704);
DELETE FROM sys_menu WHERE menu_id IN (2007, 2701, 2702, 2703, 2704);

-- 3.2 敏感性分析菜单（挂"铜冶金计算"目录下；component 必须与前端页面路径一致）
--    注意：菜单的 perms 使用独立的"页面可见"权限 metallurgy:analysis:list，
--    与按钮 2701 的"执行扫描"权限 metallurgy:analysis:sensitivity 分开，
--    避免菜单与按钮共用同一权限串导致鉴权语义不清（无法单独收权）。
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (2007, '敏感性分析', 2000, 7, 'analysis', 'metallurgy/analysis/index', NULL, 1, 0, 'C', '0', '0', 'metallurgy:analysis:list', 'chart', 'admin', NOW(), '目标冰铜品位/转炉吹炼渣硅铁比参数扫描与趋势分析');

-- 3.3 按钮权限：敏感性分析扫描 / 导出；方案对比 / 对比导出
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (2701, '扫描计算', 2007, 1, '', NULL, NULL, 1, 0, 'F', '0', '0', 'metallurgy:analysis:sensitivity', '#', 'admin', NOW(), '执行参数扫描'),
       (2702, '结果导出', 2007, 2, '', NULL, NULL, 1, 0, 'F', '0', '0', 'metallurgy:analysis:export', '#', 'admin', NOW(), '导出扫描结果'),
       (2703, '方案对比', 2006, 7, '', NULL, NULL, 1, 0, 'F', '0', '0', 'metallurgy:record:compare', '#', 'admin', NOW(), '多方案指标对比'),
       (2704, '对比导出', 2006, 8, '', NULL, NULL, 1, 0, 'F', '0', '0', 'metallurgy:record:compareExport', '#', 'admin', NOW(), '导出对比结果');

-- 3.4 为超级管理员(角色1) 与普通角色(角色2) 补角色菜单关联
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.role_id IN (1, 2)
  AND m.menu_id IN (2007, 2701, 2702, 2703, 2704);

COMMIT;

DROP TEMPORARY TABLE IF EXISTS tmp_menu_to_delete;

-- ========== 4. 校验输出（期望：待删菜单 0 行、新菜单 5 行、孤儿关联 0 行、孤儿子菜单 0 行） ==========

SELECT '待删菜单残留数（期望 0）' AS item, COUNT(*) AS value
FROM sys_menu WHERE menu_id IN (3, 4, 103, 104, 107, 110, 111, 112, 113, 114, 115, 116, 117);

SELECT '新增菜单数（期望 5）' AS item, COUNT(*) AS value
FROM sys_menu WHERE menu_id IN (2007, 2701, 2702, 2703, 2704);

SELECT '孤儿角色关联数（期望 0）' AS item, COUNT(*) AS value
FROM sys_role_menu rm LEFT JOIN sys_menu m ON m.menu_id = rm.menu_id WHERE m.menu_id IS NULL;

SELECT '孤儿子菜单数（期望 0）' AS item, COUNT(*) AS value
FROM sys_menu c LEFT JOIN sys_menu p ON p.menu_id = c.parent_id
WHERE c.parent_id <> 0 AND p.menu_id IS NULL;

SELECT menu_id, parent_id, menu_name, path, order_num
FROM sys_menu
WHERE menu_type IN ('M', 'C')
ORDER BY parent_id, order_num;

-- ========== 5. 回滚方式（仅在需要恢复裁剪前的菜单时执行，默认不要执行） ==========
--
-- 脚本第 0 节已把执行前的 sys_menu / sys_role_menu 完整备份到
-- sys_menu_bak_v2 / sys_role_menu_bak_v2，回滚步骤：
--
--   START TRANSACTION;
--   TRUNCATE TABLE sys_role_menu;
--   INSERT INTO sys_role_menu SELECT * FROM sys_role_menu_bak_v2;
--   TRUNCATE TABLE sys_menu;
--   INSERT INTO sys_menu SELECT * FROM sys_menu_bak_v2;
--   COMMIT;
--
-- 回滚要点与注意事项：
--   1) 必须先恢复 sys_role_menu 再恢复 sys_menu（避免恢复过程中出现孤儿关联），
--      或按上面顺序在事务内一并完成；
--   2) TRUNCATE 为 DDL，会隐式提交，故整段回滚不可放在同一事务中"半途中断"，
--      如需更稳妥可改为 DELETE FROM sys_role_menu; / DELETE FROM sys_menu; + INSERT；
--   3) 回滚后请重新登录以刷新菜单路由（前端路由由 /getRouters 动态下发）；
--   4) 备份表确认无误后可手动删除：DROP TABLE sys_menu_bak_v2, sys_role_menu_bak_v2;
--      （两张表为纯备份，不参与任何业务逻辑，删除不影响系统运行）；
--   5) 本脚本只改动菜单与角色关联数据，不涉及业务表（t_material / t_process_param /
--      t_calc_record / t_stream_flow），因此不存在业务数据丢失风险。
-- ----------------------------------------------------------------------------
