# 沧凌移动工作台

独立 Flutter 子工程，Android 与 iOS 共用 Dart 页面和 HTTP API；不参与根目录 Maven 构建。

## 首版功能

- HTTPS 服务器配置、用户名密码登录，复用 `/api/v1/user/login`。
- Token 存入平台安全存储，密码不落盘；服务器配置、当前用户 ID / 用户名 / 昵称写入 SQLite；旧偏好设置只用于一次性迁移。
- 工作台、软件仓库、账号三个入口。
- 软件名称 / code / 软件集搜索，下拉刷新，加载、空状态、失败重试。
- 版本文件列表、操作系统和架构筛选，浏览器下载、校验值复制。
- HTTP / 业务码 401 清理本地会话；403 显示权限错误。

不包含软件管理、上传、推送通知、离线数据库和 App 内下载队列。

## 运行

安装 Flutter 3.50.0 stable（本工程验证版本）、Android Studio 和 Android SDK；运行 `flutter doctor` 完成环境检查。
iOS 构建与真机调试需要 macOS、Xcode 和相应签名环境。

```bash
# 本机环境已安装时，从仓库根目录加载路径：
. mobile/tool/env.sh
cd mobile
flutter pub get
flutter analyze
flutter test
flutter run
./tool/build.sh apk --debug
# macOS 上验证 iOS：
./tool/build.sh ios --no-codesign
```

登录页默认连接 `https://soft.cangling.cn:22002`，可改为其他 HTTPS 地址。
私有 CA 需先正确配置设备信任；客户端不会跳过证书校验。
退出登录后可以切换服务器。退出仅删除本机凭据，不撤销服务端持久化 Token。

## 接口

| 路径（POST） | 请求 | 响应 data |
| --- | --- | --- |
| `/api/v1/user/login` | `userName`, `password` | `token`, `userName`, `nickName`, `userId` |
| `/api/v1/software/querySoftware` | `{}` | `softwares` |
| `/api/v1/software/querySoftwareFiles` | `softwareId` | `files` |

统一响应为 `{ "code": 200, "message": "...", "data": {} }`。
登录后携带 `API-TOKEN` 请求头。移动端 DTO 不依赖 GWT / Nutz 实体。
下载调用系统浏览器，不会向下载地址转发 API Token；当前后端 `/software/**` 允许匿名下载。
如未来下载需鉴权，应增加短期签名链接或实现 App 内下载。

## 结构

```text
lib/
  core/                  # HTTP、SQLite 配置/用户资料、安全凭据存储
  features/auth/         # 登录
  features/software/     # DTO、软件和文件列表
  main.dart              # Material 3 主题、导航、工作台和账号
android/                 # Android 宿主工程
ios/                    # iOS 宿主工程
test/                    # 接口契约、异常处理、模型和登录表单测试
```

`.github/workflows/mobile.yml` 对移动端改动独立执行格式检查、分析、测试和 Android debug APK 构建；产物通过 Actions artifact 获取。iOS 构建需在 macOS 上单独验证。

APK 与服务端分别发布。正式发布前设置正式应用标识、图标、签名、版本和分发渠道。
Android 默认生成的 release 签名仅用于本地验证，不能作为正式发布凭据。

## 本机 Android 验证

环境脚本默认 SDK 为 `$HOME/Android/Sdk`、Flutter 为 `$HOME/tools/flutter-sdk`。
使用 `flutter doctor -v` 检查工具链，`flutter emulators` 查看模拟器。

```bash
flutter emulators --launch cangling_mobile_api36
flutter test integration_test/app_test.dart -d emulator-5554
flutter run -d emulator-5554
```

模拟器序列号以 `flutter devices` 输出为准。首次 Gradle 构建会下载依赖。

本机验证（2026-11-17）：Flutter 3.50.0、JDK 21、Android 36 x86_64 模拟器；debug APK 构建、6 项测试、1 项 Android 集成测试通过。集成测试覆盖启动、登录表单和 HTTPS 校验、安全存储读写，不使用真实账号。

## SQLite 数据结构

数据库 `cangling_mobile.db` 位于应用私有数据库目录，由 `LocalStore` 统一打开（schema v1）。

- `app_settings(key, value)`：服务器地址及后续配置。
- `current_user(id, server, user_id, username, nickname)`：当前账号资料，单行。

登录时服务器配置与用户资料通过同一 SQLite 事务保存；启动时读取恢复。
Token 只保存在安全存储，按服务器和用户 ID 与数据库关联；密码不保存。
退出清除当前用户与本机 Token，保留服务器配置。旧版 preferences / session 自动迁移。
数据库升级通过递增版本和添加 onUpgrade 迁移完成，不应删除数据库重建。

品牌图标源自 `cangling-update/src/assets/image-bot.svg` 的卫星地球图形。
`assets/branding/image-bot.source.svg` 保留源文件；安装 `rsvg-convert` 后运行
`python3 tool/generate_icons.py` 可重新生成 Android 自适应/传统及 iOS 图标。
Android 桌面入口已注册；主屏快捷方式是否自动添加由系统启动器决定，可从应用列表长按拖到主屏。

### Android 开发版发布与更新

`./tool/build.sh apk --release` 后，设置 `SOFTWARE_TOKEN`，执行
`python3 tool/publish_android.py`。需要登录鉴权的服务器另外设置 `API_TOKEN`。
可通过 `RELEASE_NOTES` 提供更新说明。任何 token 都不要提交到版本库。
版本来自 pubspec.yaml，使用 `major.minor.patch+build`，每次发布递增版本号和 build。仓库目录只用版本号（旧服务目录工具会把 `+` 转为空格），APK 文件名包含 build。

登录页和“我的”中的“检查更新”读取固定发布站点
https://soft.cangling.cn:22002 的“系统运维 / mobile”公开清单，按数字比较版本和 build，
仅提供 Android 通用 APK；通过浏览器下载，由系统确认安装。
iOS 后续需接入 TestFlight/App Store。当前 release 使用开发机 debug 签名，
便于覆盖本机开发安装；正式分发前应配置长期保存的发布签名，签名不同无法覆盖安装。

打包统一使用 `./tool/build.sh apk --debug`（或 `apk --release` / `ios --no-codesign`），自动注入北京时间的编译日期。登录页底部显示该日期；日期来自构建参数，设备时间变化不会影响它。直接 `flutter run` 调试时可传入 `--dart-define=BUILD_DATE=YYYY-MM-DD`，未注入时隐藏日期。

### 独立移动端 release

| 用途 | 命令 | 标签 | Actions |
| --- | --- | --- | --- |
| 服务端 | 根目录 `./release.sh` | `v1.0.x` | Server release |
| 移动端 | 根目录 `./release-mobile.sh` | `mobile-v0.2.x` | Mobile release |
| 移动端检查 | 提交 mobile 代码 / PR | 无 | Mobile checks（仅 debug 附件） |

先提交所有代码，再运行：

```bash
./release-mobile.sh --dry-run       # 预览，下一个 patch 和 build
./release-mobile.sh                 # 递增移动版本、提交、打 tag、原子推送分支与 tag
./release-mobile.sh --version 0.3.0 # 指定更高版本，build 自动递增
```

脚本只修改 `mobile/pubspec.yaml`，不修改根目录 `version.txt`，不触发服务端镜像部署。
实际发布拒绝未提交修改、重复标签和非递增版本。推送失败时本地提交/标签保留，排除问题后重试 `git push --atomic origin HEAD refs/tags/mobile-vX.Y.Z`，不要再次递增版本。
手动重跑 Actions 时填写已存在的 `mobile-vX.Y.Z` 标签，工作流会 checkout 该标签并核对 pubspec。
同一个版本的重复上传会覆盖同名 APK；不要对已发布标签改代码或换签名。

流程：校验标签及 Secrets → 格式检查、静态检查、测试 → 读取固定证书 → 编译 release APK（注入编译日期）→ Actions 附件 → 上传软件仓库 → 下载并核对大小及 SHA-256。
发布串行执行，失败会标红；上传后校验失败不会自动撤销已上传文件。

仓库 GitHub Actions Secrets：

- `MOBILE_SOFTWARE_TOKEN`：mobile 软件条目 token。
- `MOBILE_API_TOKEN`：具有软件管理权限的用户 API token。
- `MOBILE_KEYSTORE_BASE64`：固定签名文件的 base64 内容。
- `MOBILE_KEYSTORE_PASSWORD`、`MOBILE_KEY_ALIAS`、`MOBILE_KEY_PASSWORD`：签名配置。

为兼容已分发的 0.2.x APK，当前固定签名沿用开发机现有 debug 证书（不是每个 runner 临时生成的 debug key）。这是开发分发流程；正式生产签名需单独规划迁移，否则已安装用户无法直接覆盖升级。
证书通过环境变量传给 Gradle，工作流结束清理临时文件；PR 和 Mobile checks 不读取发布 Secrets。
本地可设置同名签名环境变量 `MOBILE_KEYSTORE_PATH`、`MOBILE_KEYSTORE_PASSWORD`、`MOBILE_KEY_ALIAS`、`MOBILE_KEY_PASSWORD`；`MOBILE_REQUIRE_SIGNING=true` 时禁止缺少证书。

移动端 CI 使用 JDK 21 运行 Gradle/Android lint（AGP 9.3 的分析器使用 Java 21 API），Java/Kotlin 应用代码仍以 Java 17 为编译目标。

若云端已构建签名 APK，但上传网络失败，可手动运行 **Mobile upload retry**，填写原 `Mobile release` 的 run ID 和同一 mobile 标签。它先检查公网连接，再下载该次 Actions 附件并上传，避免重新编译。
