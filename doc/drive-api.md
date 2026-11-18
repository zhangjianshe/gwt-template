# 网盘 HTTP 服务

基础路径：`/api/v1/drive`。这是独立的 HTTP 模块，不改变软件仓库的数据和下载接口。

## 授权与目录规则

- 登录会话或 `API-TOKEN` 确定当前用户；`owner_id` 表示目录/文件归属，`created_by` 记录创建人。普通用户可访问自己的网盘，以及按用户 ID 授权给自己的目录子树。请求中的 `ownerId`、`createdBy` 不作为身份来源。
- “我的网盘”（`scope=mine`，缺省）只列当前用户的顶层内容；“共享给我”（`scope=shared`）列直接授予读权限的目录入口，允许从嵌套目录进入，不暴露其父目录和兄弟目录；“全部网盘”（`scope=all`）仅管理员可用。分页和数量均在过滤后计算。
- 所有者拥有读、写、删除及授权管理权限。用户共享的读、写、删除权限独立，对子树继承；多个有效共享的权限取并集。共享用户不能转授权或创建目录 token。
- 在共享目录内创建的内容继承目录归属人，另记录实际创建者；撤销共享后，创建者不会因为上传过文件而保留权限。目录所有者与管理员可配置用户共享及目录 token。
- 网盘调用使用 `X-Drive-Token: drv_...`，也可使用 `Authorization: Bearer drv_...`。不要把 token 放在 URL 参数里。
- 每个真实目录允许多个 token；每个 token 的读、写、删除权限独立，可任意组合。父目录 token 对整棵子树生效，子目录 token 不允许访问父目录或其他目录。
- 显式提供网盘 token 时，文件操作始终受该 token 限制，即使浏览器同时存在管理员会话，也不会自动扩大权限。
- 授权管理接口只认目录所有者或管理员的用户身份；显式提供目录 token 时不能管理用户共享或 token，即使同时有管理员登录会话。
- token 原文只在创建时返回一次。数据库只保存 SHA-256 摘要，列表不会返回 token 原文或摘要；遗失原文时请创建新 token 并撤销旧 token。
- 更新权限或撤销后，新请求立即按新授权执行；已经开始的下载不强制中断。
- `root` 是虚拟根目录，按上述视图过滤列表；登录用户可在自己的根目录创建内容，不允许删除 root 或给 root 分配 token。普通用户不能查询 root 的全局统计；应选择实际目录统计。不同用户可以创建同名顶层目录，用户自己的同级名称仍唯一。
- 目录最大深度 127 层。同一目录内名称区分大小写且唯一，文件和子目录也不能重名。中文、空格、`+` 可用于名称，禁止路径分隔符、`.` / `..`、控制字符及首尾空白。
- 上传不会覆盖同名内容，重名返回 409。删除旧文件需要独立的删除权限。
- 非空目录默认不删除。只有显式 `recursive=true` 才删除整棵子树，并撤销该子树的全部用户共享和 token。单次递归删除最多处理 100,000 个条目。

## 接口

JSON 成功响应沿用项目的 `{code:200,message:"操作成功",data:...}`，实际 HTTP 状态为 200。
权限/参数等错误同时返回对应 HTTP 状态和 RpcResult code。文件下载直接返回字节流。

| 方法 | 路径 | 参数 / JSON | 权限 |
| --- | --- | --- | --- |
| GET | `/entries` | `parentId`（默认 root）、`scope`（mine/shared/all）、`page`（默认 1）、`pageSize`（默认 200，最大 1000） | 读 |
| GET | `/directories/{id}/statistics` | 读取已保存的递归统计；首次缺失时异步初始化；支持管理员查询 root | 读 |
| POST | `/directories/{id}/statistics/recalculate` | 异步校准，立即返回状态；重复请求合并 | 读 |
| POST | `/directories` | `{"parentId":"目录ID或root","name":"目录名"}` | 父目录写 |
| PUT | `/files` | URL 参数 `parentId`、`name`；`Content-Type: application/octet-stream`，请求体为文件原始内容 | 父目录写 |
| GET / HEAD | `/files/{id}/content` | 可带 `Range`、`If-Range` | 文件所属子树读 |
| DELETE | `/entries/{id}` | 可选 `recursive=true` | 目标删除 |
| GET | `/directories/{id}/tokens` | 无 | 所有者或管理员 |
| POST | `/directories/{id}/tokens` | `{"name":"自动备份","read":true,"write":true,"delete":false}` | 所有者或管理员 |
| PATCH | `/directories/{id}/tokens/{tokenId}` | `{"read":true,"write":false,"delete":false}`，完整替换权限组合 | 所有者或管理员 |
| DELETE | `/directories/{id}/tokens/{tokenId}` | 无 | 所有者或管理员 |

目录列表 `data` 返回 `entries`、`directoryStatistics`、`total`、`page`、`pageSize`，目录排前、名称升序。
每个条目包含 `id`、`parentId`、`name`、`directory`、`size`、`sha256`、`createTime`；目录大小为 0，文件摘要为 SHA-256。
创建 token 返回 `data.token` 原文与 `data.grant` 元数据；元数据中的 `permissions` 为位组合：读=1、写=2、删除=4，例如 3 表示读写，7 表示全部权限。

主要错误码：400 参数无效；401 缺少/无效/已撤销凭证；403 无操作权限或超出目录范围；404 条目不存在；409 重名/非空目录/存储异常；413 超出上传限制；503 文件存储不可用。
下载使用 `Content-Disposition: attachment`、`nosniff`、`private, no-store`，支持 206/416 Range 响应，不提供公开静态文件 URL。

## curl 示例

下面所有 token 均从环境变量读取，不要将真实凭证提交到脚本或版本库。

```bash
BASE='https://soft.cangling.cn:22002'
# ADMIN_API_TOKEN 为具备管理员身份的用户 API token
curl -sS "$BASE/api/v1/drive/directories" \
  -H "API-TOKEN: $ADMIN_API_TOKEN" -H 'Content-Type: application/json' \
  -d '{"parentId":"root","name":"团队共享"}'

# 将上一步 data.id 设置为 DIRECTORY_ID；创建可读可写、不可删除的 token
curl -sS "$BASE/api/v1/drive/directories/$DIRECTORY_ID/tokens" \
  -H "API-TOKEN: $ADMIN_API_TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"上传客户端","read":true,"write":true,"delete":false}'
# 将返回的 data.token 安全保存为 DRIVE_TOKEN，data.grant.id 保存为 TOKEN_ID

curl -sS --get "$BASE/api/v1/drive/entries" \
  -H "X-Drive-Token: $DRIVE_TOKEN" --data-urlencode "parentId=$DIRECTORY_ID"

curl -sS "$BASE/api/v1/drive/directories" \
  -H "X-Drive-Token: $DRIVE_TOKEN" -H 'Content-Type: application/json' \
  -d "{\"parentId\":\"$DIRECTORY_ID\",\"name\":\"资料\"}"

# 文件原始流上传，不使用 multipart；中文名称需作为 URL 参数编码
curl -sS --upload-file ./report.pdf \
  "$BASE/api/v1/drive/files?parentId=$DIRECTORY_ID&name=report.pdf" \
  -H "X-Drive-Token: $DRIVE_TOKEN" -H 'Content-Type: application/octet-stream'
# 上传结果 data.id 保存为 FILE_ID
curl --fail --output ./download.pdf \
  "$BASE/api/v1/drive/files/$FILE_ID/content" -H "X-Drive-Token: $DRIVE_TOKEN"
# 续传（已有部分 download.pdf）
curl --fail -C - --output ./download.pdf \
  "$BASE/api/v1/drive/files/$FILE_ID/content" -H "X-Drive-Token: $DRIVE_TOKEN"

# 只有带删除权限的 token 才能删除文件/目录；此处使用 DELETE_TOKEN
curl -X DELETE "$BASE/api/v1/drive/entries/$FILE_ID" -H "X-Drive-Token: $DELETE_TOKEN"
curl -X DELETE "$BASE/api/v1/drive/entries/$DIRECTORY_ID?recursive=true" \
  -H "X-Drive-Token: $DELETE_TOKEN"

# 管理员随时撤销 token
curl -X DELETE "$BASE/api/v1/drive/directories/$DIRECTORY_ID/tokens/$TOKEN_ID" \
  -H "API-TOKEN: $ADMIN_API_TOKEN"
```

## 存储与部署

新增 `drive_entry`、`drive_token` 表，在应用启动时单独检查和创建，不要求重建现有业务表或修改全库版本号。

可选 Spring 配置：

```yaml
app:
  drive-root: /data/private-drive
  drive-upload-max-bytes: 10737418240 # 默认 10 GiB；也会限制无 Content-Length 的流
```

`drive-root` 默认是系统 uploadRoot 后附加 `.drive`（例如 `/data/dev/upload.drive`），与 `/upload/**`、`/software/**` 的公开路径分开。自定义时也应使用独立的非公开目录。
部署容器时须持久化此目录，并同时备份数据库和文件存储。文件以内部随机 ID 存盘，不使用用户文件名作为磁盘路径，也不解析用户提供的服务器路径。

上传先写临时文件，验证大小和摘要，再原子发布文件及登记元数据；中断或失败会清理临时文件。删除先在数据库事务中移除元数据和 token，再清理磁盘文件；磁盘清理失败会记录日志，遗留文件不会继续通过接口访问。进程强制终止可能留下临时/孤立文件，清理前应核对数据库引用。
当前写入/删除在单个应用实例中串行保护，并有数据库同级名称唯一索引；多写入实例部署前还应补充跨实例目录锁和存储清理协调。

验证命令：`mvn -Dtest=DriveServiceTest test`。覆盖权限隔离、子树边界、token 摘要与撤销、上传大小/中断/重名、符号链接拒绝、递归删除、HTTP 授权与 Range/HEAD 下载。

## 用户共享与升级

| 方法 | 路径 | 请求 / 响应 | 权限 |
| --- | --- | --- | --- |
| GET | `/directories/{id}/share-candidates?query=登录用户名` | 最多 30 位有效用户，仅返回 userId、userName、nickName | 所有者或管理员 |
| GET | `/directories/{id}/shares` | `data.shares`，含用户及 permissions | 所有者或管理员 |
| PUT | `/directories/{id}/shares/{userId}` | `{"read":true,"write":false,"delete":false}`；完整替换直接授权 | 所有者或管理员 |
| DELETE | `/directories/{id}/shares/{userId}` | 撤销该目录的直接授权；祖先/其他目录授权仍可独立生效 | 所有者或管理员 |

共享列表和候选用户中的 userId 以字符串返回，避免浏览器丢失长整数精度。文件列表另返回 `permissions`（当前目录）、`manage`（是否可管理授权）和 `entryPermissions`（当前页每项的权限位），前端据此控制操作；后端在每次实际操作中独立校验，不依赖前端隐藏按钮。

分片上传会话绑定用户 ID（目录 token 会话继续绑定 token 摘要）；即便拥有相同目录权限，也不能使用另一个用户的上传会话。查询、续传、完成和取消均重新验证当前权限。批量删除需要登录，对全部目标先鉴权，执行时再校验权限，排队期间被撤销授权的目标不会按管理员身份删除。

服务启动时增加 `owner_id`、`created_by` 和 `drive_user_grant` 表，并将同级名称索引更新为按归属人隔离。历史记录不自动归到任何普通用户名下，仍只有管理员或原目录 token 可访问；管理员可显式共享这些历史目录。原目录 token 和文件内容不变。旧版统一 `admin` 凭证创建的未完成分片会话不会自动归给任一用户，升级后请重新创建上传会话，遗留分片按原 TTL 清理。

验证：`mvn -Dtest=DriveServiceTest,DriveResumableUploadTest,DriveDeleteTaskTest,DriveUserAccessTest test`，包含跨用户隔离、共享/撤销、token 优先级、Range/HEAD、分片会话越权、排队删除重验和历史记录迁移。

## GWT 网盘管理页面

前端模块代码为 `drive_frame`，名称为“网盘”。GWT 编译会将其加入模块清单，服务端重启后导入功能资源，获授该模块入口的登录用户可从顶部菜单进入。

- 使用“我的网盘”“共享给我”两个页签；管理员额外显示“全部网盘”。“共享授权”复用系统 `SearchUserPanel` 用户选择对话框，支持设置读写删除权限和撤销共享；授权列表随内容收缩，长列表在窗口内滚动。
- 左侧按需加载目录树；右侧面包屑支持返回上级，文件列表按每页 100 项分页。
- 新建目录、上传和删除成功后自动刷新当前列表；目录变化同时刷新目录树。
- 上传支持多选文件、逐个传输、进度与取消。使用原始字节流 PUT，与 HTTP API 一致，不覆盖同名内容。
- 下载为同源原生下载链接，使用当前登录会话，凭证不会附加到 URL；文件由服务端流式发送。
- 删除非空目录需在确认框中显式勾选递归删除；错误保留在确认框内，便于调整后重试。
- “目录 token”显示名称和权限，不显示既有原文；支持创建、复制新 token、修改权限和确认撤销。关闭弹窗清空新 token 的展示内容。
- 根目录可新建自己的目录；选择实际目录后按权限启用上传、删除、统计。共享和 token 管理仅对所有者与管理员启用。切换账号或页签时清空旧列表和选择状态。
- 目录请求和分页请求带有视图版本检查，较早的响应不会覆盖当前目录；失败时提供重试入口。

编译验证：`mvn -DskipTests compile gwt:compile -Dgwt.forceCompilation=true`。模块使用 GWT JSON 组件，已在 `ClientApp.gwt.xml` 中显式继承。

## 可恢复的目录同步

`tools/drive-sync.py` 使用 Python 3.9+ 标准库，无额外安装依赖。源目录的内容直接放入指定网盘目录，不会再嵌套一层源目录名称。

在网盘 UI 选择目标目录、打开“目录 token”可复制目录 ID。创建至少有“读、写”权限的 token；同步不需要删除权限。凭证只通过环境变量提供，不写入源码、命令参数、日志或 SQLite。

```bash
# 以 Bash 为例，隐藏输入目录 token
read -rs -p 'Drive token: ' DRIVE_TOKEN; echo
export DRIVE_TOKEN
export DIRECTORY_ID='从页面复制的目录ID'

# 先预览：不创建目录、不上传文件，也不创建本地状态库
python3 tools/drive-sync.py --source ~/dev/cangling-repo \
  --directory "$DIRECTORY_ID" --ignore-existing --dry-run

# 实际同步。中断后重新运行完全相同的命令，即可继续
python3 tools/drive-sync.py --source ~/dev/cangling-repo \
  --directory "$DIRECTORY_ID" --ignore-existing
```

默认排除 `.git`、`.hg`、`.svn`、`node_modules`、`__pycache__`、`.pytest_cache`、`.mypy_cache`、`.ruff_cache`、`.gradle`、`.dart_tool`、`.cache`、`target`、`build`。保留 `dist`、`downloads`、`output` 等可能包含发布文件的目录。

- `--exclude '*.tmp'` / `--exclude 'path/to/cache'`：额外排除名称或源相对路径 glob，可重复传入。
- `--no-default-excludes`：关闭内置排除规则。
- `--ignore-existing`：默认策略，目标已有同名文件就跳过，**即使源文件后来发生变化，也不覆盖**；同名目录与文件类型冲突时报错。
- `--verify-existing`：已有文件必须大小和 SHA-256 都相同，否则报错，不覆盖。可与 `--dry-run` 组合做只读核对。
- `--state-file PATH`：默认 `~/.local/state/cangling-drive-sync/state.sqlite3`（尊重 `XDG_STATE_HOME`）。保存上传 ID、路径、源大小/mtime/inode 和摘要，权限 0600，不保存 token。不要将同一源和目标并行启动多份同步命令。
- `--retries 4` / `--timeout 120`：网络失败自动重试；HTTP 401/403 不自动重试。
- `--max-files 10`：先测试少量新文件；`--quiet` 只输出最终汇总和错误。
- 管理员自动化可用 `--api-token-env DEV_API_TOKEN`，仍不接受命令行中的 token 明文；日常同步优先使用目录范围的读写 token。

完整文件 SHA-256 会在上传前由客户端计算，服务端在完成时再次校验；源文件在上传期间改变则报错，不发布混合版本。符号链接和特殊文件不跟随、不上传。重新执行会忽略已经完整发布的文件，对未完成文件从**服务端确认偏移量**继续。没有删除目标端多余文件的行为。

### 分块 HTTP 协议

原始流 `/files` 上传接口继续可用。新增以下接口，均需要目标目录的写权限，且上传会话绑定创建时的凭证身份；每个请求重新鉴权，撤销后不可继续。

| 方法 | 路径 | 参数 |
| --- | --- | --- |
| POST | `/uploads` | JSON：`uploadId`（可选，32位小写UUID，用于幂等初始化）、`parentId`、`name`、`totalSize`、`sha256` |
| GET | `/uploads/{id}` | 返回服务端确认进度 |
| PUT | `/uploads/{id}/chunk?offset=...&size=...` | 原始字节流，`X-Chunk-SHA256` 请求头，单片最大 8 MiB |
| POST | `/uploads/{id}/complete` | 完整长度及 SHA-256 校验后发布 |
| DELETE | `/uploads/{id}` | 取消自己的上传会话，只删除暂存数据，不删除已发布文件 |

返回 `data` 包含 `uploadId`、`parentId`、`name`、`totalSize`、`receivedSize`、`sha256`、`chunkSize`、`completed`，完成后另有 `entry`。重复发送已经确认的相同分片、重复完成请求均安全；偏移冲突返回 409，此时重新 GET 进度。分片或整文件摘要错误返回 422；过期会话返回 410。

新增 `drive_upload` 表自动创建。分片数据在持久化网盘根目录的 `.parts` 下；每片先验证摘要并 fsync，再更新数据库偏移量。崩溃留下但未被确认的尾部会在续传前截断。服务端或客户端重启都不会丢失已确认的进度，客户端仍须保留状态库和未变化的源文件。

默认单文件上限为 10 GiB，可用 `app.drive-upload-max-bytes` 调整。上传会话和暂存文件在停止更新 7 天后自动清理，清理不会删除已发布的文件。目录/token 被删除或撤销后会话不可继续，暂存文件仍按过期规则清理。

已完成的条目仅在最终校验成功后出现在正常目录列表中，因此“忽略已存在”不会把半个文件误认为已完成。磁盘发布后数据库写入结果不确定的情况，使用固定 entry ID 安全重试完成；不会覆盖同名的其他文件。当前仅支持单实例写入部署。

测试：`mvn -Dtest=DriveServiceTest,DriveResumableUploadTest test` 和 `python3 tools/test_drive_sync.py`。

## SHA-256 内容去重与旧数据迁移

`drive_entry.id` 始终标识逻辑文件，名称、父目录、创建时间及权限语义保持不变。文件新增 `blob_id`，引用 `drive_blob.id`（64 位 SHA-256，数据库主键保证内容身份唯一）。`drive_blob` 保存内容大小和创建时间；实际内容位于私有根目录 `.blobs/<sha256>`。多个目录、不同名称的文件可共享内容；目录本身不引用 blob。

原始上传和分片上传都必须先传输全部字节，由服务端计算/验证 SHA-256 后才发布条目；不会仅凭客户端提供的摘要执行秒传，也不对未授权用户暴露共享内容下载接口。下载仍从原文件 ID 校验目录权限，再读取对应 blob；Range 和文件名保持原行为。

应用启动时自动逐文件迁移旧 UUID 存储：核对原大小及完整摘要、原子移动首份内容（重复内容复用）、事务写入 blob 和 entry 引用、事务成功后清理重复 UUID 副本。迁移不重新生成文件或目录 ID、不修改名称、层级、时间。中途退出可重启继续；已移动但未提交的文件可从内容地址恢复。发现文件丢失或摘要不符时停止迁移并报错，保留可检查的数据。运行环境仍限单写入实例。

删除单个文件或递归目录时，先事务删除逻辑条目，再按已提交的 `drive_entry.blob_id` 查询剩余引用；存在其他引用时保留内容。只有最后一个引用被删除后才清理 blob 记录及内容。物理清理失败只留下不可访问的孤立内容，不恢复已删除条目。

升级前必须同时备份数据库和私有存储目录。回滚旧版本前，必须将现有文件按 `drive_entry.id` 还原为 UUID 磁盘文件；不能仅切回旧 JAR，因为旧版本不识别共享内容路径。开发站点部署保留升级前数据库、UUID 文件硬链接快照及镜像，快照保留期间，磁盘空闲量不会完整体现去重节省。

“大小统计”按钮针对当前选中目录，递归统计所有已发布文件的逻辑大小（字节）、文件数和子目录数；子目录数不含当前目录。相同内容的多个逻辑文件分别计入总大小，因此它不代表去重后的物理占用或删除后可释放空间。接口读取持久化统计，不扫描磁盘文件，并沿用目录范围的读取权限。


### 持久化统计和后台校准

`drive_directory_stats` 与目录 ID 一一关联（包括虚拟 root），保存 `file_count`、`directory_count`、`total_size`、`initialized`、`state`、`updated_time` 和 `calibrated_time`。大小按逻辑文件累计，使用 64 位数值；目录数不包含自身。

- 新建目录在插入目录的同一事务中建立零计数，并给父目录及全部祖先目录数加一。
- 普通/分片上传发布条目时，在同一事务中为所属目录及全部祖先增加文件数和字节数。未完成分片不计入，重复完成请求不重复累计。
- 删除文件或递归删除子树时，在元数据删除的同一事务内从所有保留祖先扣减实际删除的文件数、目录数及总字节，删除对应子目录统计记录。统计或条目写入失败会一起回滚。
- 首次升级及应用启动后在后台校准现有目录；尚无基线的记录显示初始化中，直到后台根据完整快照发布准确结果。正常操作不依赖反复递归查询。
- GET 返回缓存结果及 `initialized`、`state`（PENDING/RUNNING/READY/FAILED）、`updatedTime`、`calibratedTime`。界面保留已保存结果并轮询后台任务；关闭弹窗停止轮询。
- POST `statistics/recalculate` 立即返回 RUNNING；单后台线程合并请求，统一校准目录及祖先，避免层级间统计不一致。计算在锁外执行，提交前核对写入版本，期间有文件操作则丢弃旧快照并重试。校准结果在一个事务中发布。
- 后台失败保留上次结果并记录 FAILED，可手动重试；应用重启后重新触发校准。仍要求单实例写入部署，直接修改数据库之后应手动校准。

目录列表的 `directoryStatistics` 是仅包含当前页目录 ID 的统计映射，批量从现有统计表读取。前端目录“大小”列显示其中已初始化的 `totalSize`，空目录显示 0 B；缺失或未初始化时显示“统计中”，完成后自动刷新，离开页面停止刷新。文件大小仍使用条目原始 `size`，不修改目录条目中的 `size` 字段。


## 批量删除与 WebSocket 进度

列表第一列支持当前页全选、取消和部分选中状态；切换目录/分页会清空选择，统计自动刷新保留仍存在的选项。顶部“删除”按钮显示所选数量；确认弹窗列出名称，删除非空目录必须显式勾选递归。创建时间显示相对时间，悬停显示原时间，大小列右对齐。

登录用户 POST `/api/v1/drive/delete-tasks`（须拥有每个目标的删除权限），请求为 `{"ids":["条目ID"],"recursive":false}`。每次最多 100 项，拒绝重复项和目录/子项重叠选择；每个用户同时只允许一个未完成任务，服务端队列有界。返回任务 ID 和一次展示的 `watchToken`。GET `/api/v1/drive/delete-tasks/{id}` 仅允许提交该任务的用户查询；目录 token 不能用于这两个接口。

连接同源 WSS `/ws/drive-delete` 后，首条消息发送 `{"taskId":"任务ID","watchToken":"订阅凭证"}`，凭证不进入 URL。服务端校验摘要和 24 小时有效期后立即推送快照，随后推送单调递增 `version`、state、processed/total、succeeded/failed、currentName、phase、entryProcessed/entryTotal 和逐项结果；不广播给其他用户。慢连接合并待发快照，避免积压。未授权订阅关闭连接，不发送任务信息。

“已处理项”指所选条目；递归删除额外显示扫描/处理文件记录/清理阶段及其内部计数。事务提交后才将所选条目标记成功。各条目独立执行，单项失败继续处理其他项，最终状态为 SUCCESS/PARTIAL/FAILED。删除仍沿用目录统计事务更新、共享内容引用保护。

前端优先接收 WebSocket 快照，并定时查询状态兜底，忽略过期版本；关闭进度窗口后任务继续，顶部“删除进度”可重新打开。失败项保留选择供重新确认。服务重启将尚未完成任务标记 INTERRUPTED，不自动继续删除，需刷新目录核对剩余条目后重新提交。任务记录保存在 `drive_delete_task`，完成或中断后保留约 24 小时，按小时清理；进度信息不包含管理员 API token，订阅凭证只保存摘要。
