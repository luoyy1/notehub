# NoteHub 迭代记录

## 当前版本

- 版本：v0.1.0 baseline
- 日期：2026-05-18
- 仓库：https://github.com/luoyy1/notehub
- 基线提交：`1020553 Initial NoteHub baseline`

## 当前状态

- 前端：Vue 3 + Vite，包含首页、时间轴、功能规划、愿望清单等页面。
- 后端：Spring Boot，提供事件、日历标记、愿望清单、动态路由接口。
- 本地运行：
  - 前端默认端口：`3000`
  - 后端默认端口：`18080`
  - 启动脚本：`dev.cmd`
  - 检查脚本：`check.cmd`
- 已确认：
  - 前端类型检查可通过。
  - 前端构建可通过，但 Vite 8/Rolldown 在 Windows 路径下偶发构建路径问题。
  - 后端 Maven 生命周期可通过，目前还没有测试源码。
  - API 代理链路已跑通：`/api/routes`、`/api/events`、`/api/wishes`。

## 已知问题

- 时间轴页面存在布局遮挡问题，当前 CSS 调整未彻底解决，需要重新设计布局结构。
- Codex 内置浏览器无法打开本地地址，报 `net::ERR_BLOCKED_BY_CLIENT`，可通过手动浏览器测试。
- 后端缺少自动化测试，当前 `mvn test` 显示 `No tests to run`。
- 前端接口 fallback 会静默使用 mock 数据，开发时不容易发现真实后端异常。

## 简略迭代计划

1. 修复时间轴页面
   - 放弃过度依赖绝对定位的横向画布。
   - 改成可滚动但不裁切内容的稳定布局。
   - 优先保证桌面端和移动端都不遮挡。

2. 补测试基线
   - 后端先补 `DateUtil`、`CalculatorService` 单元测试。
   - 再补事件、愿望接口的 Controller 层测试。
   - 前端至少保留 `typecheck + build` 作为基础检查。

3. 优化本地开发体验
   - 固化 `dev.cmd`、`check.cmd`。
   - 处理 Vite 8/Rolldown 偶发构建问题，必要时锁定更稳定版本。
   - 开发环境区分 mock fallback 和真实接口错误。

4. 后续功能方向
   - 完善事件编辑体验。
   - 增强愿望清单的状态流转和筛选。
   - 增加数据备份/导入导出能力。
