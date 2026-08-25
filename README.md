# gwt-template · Cangling DEV Server（苍岭开发平台）

> 一个基于 **Spring Boot + GWT** 的一体化云开发平台，集成了代码仓库、项目管理、DNS 管理、LDAP 管理、Docker 应用管理、证书服务等能力，可作为企业级研发运维（DevOps）门户使用。

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)

---

## 目录

- [项目简介](#项目简介)
- [技术栈](#技术栈)
- [核心功能](#核心功能)
- [目录结构](#目录结构)
- [环境要求](#环境要求)
- [构建与运行](#构建与运行)
- [部署](#部署)
- [配置说明](#配置说明)
- [辅助脚本](#辅助脚本)
- [版本发布](#版本发布)
- [许可证](#许可证)

---

## 项目简介

`gwt-template` 是一个 **Spring Boot 后端 + GWT 前端** 的 Web 应用。前端使用 Google Web Toolkit（GWT 2.12）编译为 JavaScript，后端使用 Spring Boot 提供 REST/RPC 服务，通过 GWT-RPC 进行前后端通信。

应用启动时打印的名称为 **Cangling DEV Server**，定位为一套「开发平台」，可为开发团队提供统一的工作台：

- 代码仓库托管（基于 JGit / SSH / HTTP）
- 项目与工作区管理（任务、缺陷、团队、甘特图、日历、Wiki）
- DNS 管理（PowerDNS、Cloudflare、Traefik）
- LDAP 目录管理
- Docker 应用管理（docker-compose 编排）
- 软件制品 / 文件管理
- 站内消息 / 邮箱
- WebHook 集成
- 基于 RBAC 的用户与权限体系

---

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 前端 | GWT 2.12.2、Elemental2、Ace Editor、Xterm.js、Gridstack、Markdown |
| 后端 | Spring Boot 2.3.12、Spring Security、Spring WebSocket、Spring Data LDAP |
| 数据 | PostgreSQL、JDBC、HikariCP、Nutz |
| 代码托管 | JGit 5.13.3、Apache MINA SSHD（SSH Git） |
| 文档处理 | Apache POI 5.4.0、Flexmark（Markdown） |
| 其他 | Lombok、BouncyCastle、EdDSA |
| 构建 | Maven、GWT Maven Plugin 1.1.0 |
| 运行环境 | JDK 11、Docker |

关键依赖（`cn.mapway` 套件）：

- `api-tools-doc`：API 文档工具
- `biz-common`：通用业务组件
- `mapway-gwt-common`：GWT 通用组件
- `mapway-module-rbac`：RBAC 权限模块
- `mapway-gwt-ace`：Ace 编辑器封装
- `mapway-gwt-xterm`：Xterm 终端封装

---

## 核心功能

### 1. 工作台 / 仪表盘（dashboard / desktop）
- 桌面化界面，支持桌面项（Desktop Item）与仪表盘（Dashboard）配置。

### 2. 工作区与项目管理（workspace）
- 项目（Project）、任务（Task）、缺陷（Issue）、团队（Team）、成员管理
- 甘特图（Gantt）、日历（Calendar）、看板视图
- Wiki 文档（基于组件化的 Wiki 引擎）
- 资源（Resource）与任务导入/导出（Excel）

### 3. 代码仓库（repository）
- 基于 JGit 的 Git 仓库托管
- 支持 HTTP 与 SSH（Apache MINA SSHD，默认端口 `2222`）协议
- 仓库成员、权限（Owner / 读写）、WebHook 管理
- 分支 / 引用（refs）查询

### 4. DNS 管理（dns / powerdns / cloudflare）
- PowerDNS 权威服务器管理（Zone、Record、RRSet）
- Cloudflare 域名解析管理
- Traefik 路由 / 证书配置

### 5. LDAP 管理（ldap）
- LDAP 目录浏览与节点管理
- 条目增删改查、导入/导出（DIF / Excel）
- 与 Spring Security LDAP 集成实现登录认证

### 6. Docker 应用管理（docker）
- 管理部署在 `/opt/cangling-apps` 下的 docker-compose 应用
- 应用启停、重启、目录浏览与文件读写

### 7. 软件与文件（soft / file）
- 软件制品及文件的上传、下载与分类管理
- 支持大文件上传（默认上限 1000MB）

### 8. 消息与邮箱（message）
- 站内消息收发、用户邮箱（Mailbox）管理

### 9. 日志（log）
- 系统日志查询（`SysLog`）

### 10. WebHook（webhook）
- WebHook 注册、实例管理与触发

### 11. 用户与权限（user / RBAC）
- 基于 `mapway-module-rbac` 的用户、角色、资源点（Resource Point）权限体系
- 支持注册、用户信息维护、LDAP 设置

---

## 目录结构

```
gwt-template
├── src/main/java/cn/mapway/gwt_template
│   ├── client/          # GWT 前端源码（Java 编写，编译为 JS）
│   │   ├── dashboard/   # 仪表盘
│   │   ├── desktop/     # 桌面
│   │   ├── dns/         # DNS 管理（powerdns / cloudflare / traefik）
│   │   ├── docker/      # Docker 应用管理
│   │   ├── editor/      # 在线编辑器
│   │   ├── ldap/        # LDAP 管理
│   │   ├── log/         # 日志
│   │   ├── repository/  # 代码仓库
│   │   ├── software/    # 软件管理
│   │   ├── user/        # 用户
│   │   ├── widget/      # 通用组件
│   │   └── workspace/   # 工作区 / 项目管理
│   ├── server/          # Spring Boot 后端
│   │   ├── api/         # 对外 API
│   │   ├── config/      # 配置（db / git / security / websocket / startup）
│   │   ├── compile/     # 编译相关
│   │   ├── generator/   # 代码生成器
│   │   ├── service/     # 各业务服务实现
│   │   └── servlet/     # Servlet
│   ├── shared/          # 前后端共享代码（RPC DTO、DB 实体等）
│   │   ├── db/          # 数据库实体
│   │   ├── rpc/         # GWT-RPC 请求/响应模型
│   │   └── wiki/        # Wiki 组件
│   └── GwtTemplateApplication.java  # 启动入口
├── src/main/resources
│   ├── application.properties     # 应用配置
│   ├── templates/index.ftlh       # 页面模板
│   ├── static/                    # 静态资源（js/css/img/icons/video）
│   └── sqls/views.sql             # 数据库视图脚本
├── it/                   # 集成测试 / 本地 IT 环境部署（docker-compose）
├── tools/                # 辅助工具脚本
├── Dockerfile            # 应用镜像构建
├── build.sh / pack.sh / package.sh / release*.sh  # 构建与发布脚本
└── pom.xml               # Maven 配置
```

---

## 环境要求

- **JDK 11**（Maven 编译器 `release=11`）
- **Maven 3.x**
- **Docker / Docker Compose**（部署与 Docker 应用管理功能需要）
- **PostgreSQL**（推荐 `postgis:17`）

---

## 构建与运行

### 本地构建

```bash
./build.sh
```

等价于：

```bash
mvn clean compile gwt:compile package install -DskipTests=true
```

### 本地运行

构建完成后，可直接运行生成的 Jar：

```bash
java -jar target/gwt-template-1.0.0-SNAPSHOT.jar
```

### GWT 开发模式

使用 GWT Maven 插件的 CodeServer（开发时热更新前端）：

```bash
mvn gwt:codeserver
```

> GWT 模块名为 `cn.mapway.gwt_template.ClientApp`，编译产物输出到 `src/main/resources/static/js`。

---

## 部署

### 构建 Docker 镜像

```bash
./pack.sh
# 构建镜像 hub.cangling.cn/cangling/gwt-template:latest
```

或推送指定版本：

```bash
./package.sh
# 构建并推送 hub.cangling.cn/cangling/gwt-template:1.0
```

### 一键构建 + 发布 + 远程更新

```bash
./release-local.sh
```

该脚本会依次执行：Maven 构建 → Docker 镜像构建 → 打标签 → 推送镜像 → 触发远程开发服务器更新。

### 基础镜像说明

`Dockerfile` 基于 `docker.io/mapway/gdal-base:v4.2.33`，容器启动命令：

```dockerfile
ENTRYPOINT ["java", "-jar", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseContainerSupport", "app.jar"]
```

### IT 环境部署（`it/` 目录）

`it/` 目录提供了一套完整的本地 IT 运行环境（docker-compose），包含：

- **PowerDNS**（权威服务器 + Recursor）
- **Step-CA**（ACME 证书服务）
- **OpenLDAP**（目录服务）
- **dev-db**（PostgreSQL + PostGIS）
- **dev-app**（本应用）
- **Traefik**（反向代理 / 证书 / SSH 路由）

启动步骤参见 `it/README.md`：

```bash
./init.sh          # 初始化 Step-CA
docker-compose up -d
./init-acme.sh     # 初始化 ACME
```

---

## 配置说明

主要配置项位于 `src/main/resources/application.properties`：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `spring.application.name` | `gwt-template` | 应用名称 |
| `spring.servlet.multipart.max-file-size` | `1000MB` | 单文件上传上限 |
| `app.repoRoot` | `${REPO_ROOT:/data/dev/reporoot}` | Git 仓库根目录 |
| `app.certRoot` | `${CERT_ROOT:/data/dev/certroot}` | 证书根目录 |
| `app.uploadRoot` | `${UPLOAD_ROOT:/data/dev/upload}` | 上传文件根目录 |
| `app.sshPort` | `${SSH_PORT:2222}` | Git SSH 服务端口 |
| `app.projectResRoot` | `${PROJECT_RES_ROOT:/data/dev/projectroot}` | 项目资源根目录 |
| `nutz.db.*` | 空 | Nutz 数据库连接配置 |
| `management.health.ldap.enabled` | `false` | LDAP 健康检查开关 |

> 应用启动时会调用 `StartBootPrepare.prepare()`，从 `/mapway/app.json`（容器内挂载）读取运行时配置。

---

## 辅助脚本

| 脚本 | 说明 |
| --- | --- |
| `build.sh` | 本地完整构建（compile + gwt:compile + package + install） |
| `pack.sh` | 构建 Docker 镜像（`latest`） |
| `package.sh` | 构建并推送 `1.0` 版本镜像 |
| `release.sh` | 版本号递增、打 tag 并推送到 `origin`（GitHub） |
| `release-cl.sh` | 版本号递增、打 tag 并推送到 `cl`（内部 GitLab） |
| `release-local.sh` | 本地构建镜像 + 推送 + 远程更新开发服务器 |
| `tools/check-user.sh` | LDAP 用户检索诊断脚本（排查多匹配登录问题） |

---

## 版本发布

版本号维护在 `version.txt`（当前 `1.0.187`），发布流程：

```bash
./release.sh        # 推送到 origin（GitHub）
./release-cl.sh     # 推送到 cl（内部 GitLab）
```

发布脚本会：

1. 检查工作区是否干净
2. 递增 `version.txt` 的补丁版本号（`X.Y.Z` → `X.Y.(Z+1)`）
3. 提交版本号变更
4. 打 `vX.Y.Z` 标签并推送

支持 `--dry-run` 模式预览（不产生实际变更）：

```bash
./release.sh --dry-run
```

GitHub Actions（`.github/workflows`）在推送 `v*.*.*` 标签时，会自动完成 Maven 构建、GWT 编译、多架构（amd64/arm64）Docker 镜像构建并推送到 Harbor，最后触发远程服务器自动更新。

---

## 许可证

本项目采用 [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)。

项目地址：<https://github.com/zhangjianshe/gwt-template.git>

开发者：zhangjianshe（<zhangjianshe@gmail.com>）
