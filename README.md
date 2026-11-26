# 铜闪速冶炼工艺全流程计算与物质流分析系统 V1.0

《计算机在冶金中的应用》课程大作业。面向铜闪速熔炼工艺，以元素守恒（Cu / Fe / S / SiO₂ / CaO）为核心，完成配料计算、闪速熔炼 → 转炉吹炼 → 阳极精炼 → 电解精炼的全流程物料平衡，并以桑基图与报表量化各工序的物质流向，为工艺布局优化与资源利用提供定量依据。

## 技术栈

- 后端：Java 17 + Spring Boot 3.5.16（在 RuoYi-Vue 3.9.2 框架上二次开发）
- 持久层：MyBatis + MySQL 8.0；缓存与会话：Redis
- 前端：Vue 3 + Vite 6 + Element Plus + ECharts（桑基图）

## 目录结构

```
ruoyi/            后端工程（ruoyi-admin 为启动模块；冶金业务代码位于 ruoyi-system/src/main/java/com/ruoyi/metallurgy）
ruoyi-ui/         前端工程（Vue 3 + Vite；冶金业务页面位于 src/views/metallurgy）
sql/metallurgy/   业务脚本（mt_schema 建表、mt_menu / mt_menu_v2 菜单、mt_data 示例数据）
运行说明.md        编译与运行说明（从零复现系统的步骤）
```

## 功能模块

- 物料台账管理、工艺参数方案
- 配料计算（质量守恒 + 渣型约束下的配比求解）
- 全流程物料平衡（五节点元素守恒 + 平衡校核）
- 物质流分析（桑基图 + 明细报表 + 导出）
- 敏感性分析与方案对比
- 计算记录（输入/结果快照，可追溯）
- 系统基础（登录、用户、角色、菜单权限，复用框架）

## 环境要求

JDK 17、Maven 3.8+、MySQL 8.0、Redis 5.0+、Node.js 18 / 20 / ≥22、Chrome 或 Edge 最新版。

## 快速启动

完整步骤见 [运行说明.md](运行说明.md)，要点如下：

1. 创建数据库 `metallurgy_cu`（utf8mb4），依次导入 `ruoyi/sql/ry_20260417.sql`、`ruoyi/sql/quartz.sql`，再导入 `sql/metallurgy/` 下的 `mt_schema.sql`、`mt_menu.sql`、`mt_menu_v2.sql`、`mt_data.sql`；
2. 启动 Redis（本项目使用 16379 端口，`redis-server --port 16379`）；
3. 修改 `ruoyi/ruoyi-admin/src/main/resources/application-druid.yml` 中的数据库账号与口令（本仓库为占位符），并确认 `application.yml` 中的 Redis 端口；
4. 构建并启动后端：`cd ruoyi && mvn clean package -DskipTests && java -jar ruoyi-admin/target/ruoyi-admin.jar`（监听 8080）；
5. 启动前端：`cd ruoyi-ui && npm install && npm run dev`，访问 http://localhost:5173 ，默认账号 `admin / admin123`。

## 说明与边界

- 数据与工艺参数均为课程演示用示例值，非任何具体矿山或企业的实测值。
- 计算模型为基于元素守恒的教学级简化模型，用于教学演示与工艺趋势分析，不替代现场标定或商业流程模拟软件。
- 数据库连接口令以占位符形式给出，部署前请自行填写并妥善保管；本地演示环境未设置 Redis 访问密码，联网部署时请加固。