# Saimin-APP 
一个你在寻找的冻鳗风格催眠App。 

A hentai style saimin app you are looking for.

## 应用截图 Screenshot 
![screenshot](https://cdn.jsdelivr.net/gh/reed-overflow/Saimin-APP/art/Screenshot_1.png)

## TODO
- [x] Mode switch 
- [x] Home page animation
- [x] Sensitive mode
- [x] Bubbleseekbar
- [x] Background visual effects improve
- [x] 页面自定义：模式列表、自定义参数、动画效果与布局
- [x] 更新检查：按需检查 GitHub 最新正式版本
- [x] 首次进入应用教程：支持跳过和在设置中重看
- [x] 图标长按 Shortcut：主页和设置入口
- [x] 可选择的通知栏快捷进入：支持开启、关闭和通知权限处理

## 自定义模式

1. 打开侧边栏 → 设置，在「新增模式」中输入名称。
2. 点击「编辑模式列表」中的模式，可以重命名、添加或删除参数，也可删除整个模式（至少保留一个）。
3. 每个参数可以设置名称、最小值、最大值、步长和默认值，支持负数与小数。例如 `-10 ～ 10`、步长 `0.5`、默认值 `0`。范围和默认值必须与步长对齐，最多支持 100000 个刻度。
4. 在模式的「催眠动画 → 预览与调整动画」中选择同心波纹、旋转螺旋或摆钟，拖动滑条设置动画大小（10% ～ 100%）、主页区域高度（120 ～ 800 dp）、动画周期（1000 ～ 20000 毫秒），并调整前景和背景颜色、全屏播放。上方预览固定显示，实时反映设置及显示区域比例；保存后生效，取消不修改配置。
5. 在设置顶部的「当前模式」切换模式。主页会根据该模式生成参数滑条和动画，点击「恢复默认参数」可重置滑条。全屏播放通过底部按钮或返回键退出。

模式配置在本地自动保存，重启后保留；旧版基础／扩展模式及当前选择会迁移为可编辑模式。滑条实时数值是本次展示状态，切换模式或修改配置时使用设定的默认值；旋转屏幕保留当前调节值。模式与参数只用于恶搞展示。

主页使用粉白配色、细滑条与圆角按钮。打开开关前动画区域不绘制图形，关闭后立即清空。同心波纹从中心连续匀速扩散，保持可见直到越过区域四角，由区域边界自然裁切，不再提前淡出。波纹的「动画大小」控制环间距（越小越密，100% 时为十层）；螺旋和摆钟仍按显示区域短边缩放。主页、预览和全屏共用这一效果。

## 教程、更新与快捷入口

- 首次启动显示三步教程，可跳过，也可在设置的「使用教程」中重新查看。
- 「检查更新」或点击版本号会请求 GitHub Releases API，对比最新正式版本；支持无正式发布、网络失败和无法识别版本号的提示，可手动打开发布页。不会自动下载或安装。
- Android 7.1 及以上、支持快捷方式的桌面可长按图标，直接打开主页或设置。
- 「通知栏快捷入口」默认关闭，开启后显示常驻入口；Android 13 及以上会按需请求通知权限。被系统禁用时可通过「系统通知设置」恢复权限，再开启入口。关闭开关会移除通知。通知在下次启动应用时恢复，不依赖后台服务或开机广播。
- 核心展示和编辑功能离线可用，仅更新检查和打开外部链接需要网络。界面提供中文和英文文案。

## 版本与自动构建

当前版本为 `0.1.2b`，`versionCode` 为 `3`。发布新版时维护 `app/build.gradle` 中的这两个值，`versionCode` 必须递增。

`.github/workflows/android-release.yml` 在推送到 `main` 时运行，也支持在 Actions 页面选择 `main` 手动运行。使用 JDK 17、Android SDK 33，执行单元测试、Lint 和 Release APK 构建；成功后上传 Actions 构建产物，并创建包含 APK 的 **Release 草稿**，不会自动公开发布。草稿标签为 `v<版本>-main.<运行编号>`，绑定实际构建提交；同一次运行重试会替换草稿附件，不会改动已发布版本。

无需配置密钥即可生成测试签名的 Release APK。GitHub 临时运行器的测试密钥不保证跨次构建一致；需要稳定覆盖安装或正式发布时，在仓库 Settings → Secrets and variables → Actions 中配置以下四个 Secrets（使用既有应用的签名密钥才能覆盖旧安装）：

- `ANDROID_KEYSTORE_BASE64`：签名 keystore 的 Base64 内容。
- `ANDROID_STORE_PASSWORD`：keystore 密码。
- `ANDROID_KEY_ALIAS`：密钥别名。
- `ANDROID_KEY_PASSWORD`：密钥密码。

签名配置不完整时工作流会失败，避免误用测试签名。工作流使用自带的 `GITHUB_TOKEN` 创建草稿，需要仓库允许 `contents: write` 权限。推送工作流后即可在 Actions 查看结果；审核 APK 和发布说明后手动发布草稿。

本地验证命令：`./gradlew testDebugUnitTest lintDebug assembleRelease`。本地未配置签名时生成未签名 Release APK；CI 会签名并使用 `apksigner verify` 检查。

本次已通过单元测试、Lint、Release 构建及测试签名 APK 校验；尚未完成模拟器画面复核，也未在 GitHub 上实际触发工作流。

## Thanks
- [LicensesDialog](https://github.com/PSDev/LicensesDialog)
- [android-ripple-background](https://github.com/skyfishjy/android-ripple-background)
- [SwitchButton](https://github.com/kyleduo/SwitchButton)
- [BubbleSeekBar](https://github.com/woxingxiao/BubbleSeekBar)
- [RippleAnimation](https://github.com/Ifxcyr/RippleAnimation)
