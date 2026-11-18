# 网盘 HTTP 服务

基础路径：`/api/v1/drive`。这是独立的 HTTP 模块，不改变软件仓库的数据和下载接口。

## 授权与目录规则

- 管理员使用现有登录会话或 `API-TOKEN` 用户凭证，可管理所有目录与目录 token。普通用户的登录身份不自动授予网盘权限。
- 网盘调用使用 `X-Drive-Token: drv_...`，也可使用 `Authorization: Bearer drv_...`。不要把 token 放在 URL 参数里。
- 每个真实目录允许多个 token；每个 token 的读、写、删除权限独立，可任意组合。父目录 token 对整棵子树生效，子目录 token 不允许访问父目录或其他目录。
- 显式提供网盘 token 时，文件操作始终受该 token 限制，即使浏览器同时存在管理员会话，也不会自动扩大权限。
- 管理接口只认管理员身份，网盘 token 不能创建或修改其他 token。
- token 原文只在创建时返回一次。数据库只保存 SHA-256 摘要，列表不会返回 token 原文或摘要；遗失原文时请创建新 token 并撤销旧 token。
- 更新权限或撤销后，新请求立即按新授权执行；已经开始的下载不强制中断。
- `root` 是虚拟根目录，仅管理员可列举并创建顶层目录，不允许删除或直接分配 token。使用者拿到具体目录 ID 和相应 token 后访问该目录。
- 目录最大深度 127 层。同一目录内名称区分大小写且唯一，文件和子目录也不能重名。中文、空格、`+` 可用于名称，禁止路径分隔符、`.` / `..`、控制字符及首尾空白。
- 上传不会覆盖同名内容，重名返回 409。删除旧文件需要独立的删除权限。
- 非空目录默认不删除。只有显式 `recursive=true` 才删除整棵子树，并撤销该子树的全部 token。单次递归删除最多处理 100,000 个条目。

## 接口

JSON 成功响应沿用项目的 `{code:200,message:"操作成功",data:...}`，实际 HTTP 状态为 200。
权限/参数等错误同时返回对应 HTTP 状态和 RpcResult code。文件下载直接返回字节流。

| 方法 | 路径 | 参数 / JSON | 权限 |
| --- | --- | --- | --- |
| GET | `/entries` | `parentId`（默认 root）、`page`（默认 1）、`pageSize`（默认 200，最大 1000） | 读 |
| POST | `/directories` | `{"parentId":"目录ID或root","name":"目录名"}` | 父目录写 |
| PUT | `/files` | URL 参数 `parentId`、`name`；`Content-Type: application/octet-stream`，请求体为文件原始内容 | 父目录写 |
| GET / HEAD | `/files/{id}/content` | 可带 `Range`、`If-Range` | 文件所属子树读 |
| DELETE | `/entries/{id}` | 可选 `recursive=true` | 目标删除 |
| GET | `/directories/{id}/tokens` | 无 | 管理员 |
| POST | `/directories/{id}/tokens` | `{"name":"自动备份","read":true,"write":true,"delete":false}` | 管理员 |
| PATCH | `/directories/{id}/tokens/{tokenId}` | `{"read":true,"write":false,"delete":false}`，完整替换权限组合 | 管理员 |
| DELETE | `/directories/{id}/tokens/{tokenId}` | 无 | 管理员 |

目录列表 `data` 返回 `entries`、`total`、`page`、`pageSize`，目录排前、名称升序。
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
  drive-upload-max-bytes: 1073741824 # 默认 1 GiB；也会限制无 Content-Length 的流
```

`drive-root` 默认是系统 uploadRoot 后附加 `.drive`（例如 `/data/dev/upload.drive`），与 `/upload/**`、`/software/**` 的公开路径分开。自定义时也应使用独立的非公开目录。
部署容器时须持久化此目录，并同时备份数据库和文件存储。文件以内部随机 ID 存盘，不使用用户文件名作为磁盘路径，也不解析用户提供的服务器路径。

上传先写临时文件，验证大小和摘要，再原子发布文件及登记元数据；中断或失败会清理临时文件。删除先在数据库事务中移除元数据和 token，再清理磁盘文件；磁盘清理失败会记录日志，遗留文件不会继续通过接口访问。进程强制终止可能留下临时/孤立文件，清理前应核对数据库引用。
当前写入/删除在单个应用实例中串行保护，并有数据库同级名称唯一索引；多写入实例部署前还应补充跨实例目录锁和存储清理协调。

验证命令：`mvn -Dtest=DriveServiceTest test`。覆盖权限隔离、子树边界、token 摘要与撤销、上传大小/中断/重名、符号链接拒绝、递归删除、HTTP 授权与 Range/HEAD 下载。
