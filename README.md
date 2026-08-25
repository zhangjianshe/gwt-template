## 开发平台

#  IT 基本环境配置

# 2026.7.24 添加操作docker-compose 应用的能力 
- 需要基础镜像必须安装 docker和docker compose
- 以容器方式启动应该用的时候 必须将宿主机的docker引入容器
```shell
   -v /var/run/docker.sock:/var/run/docker.sock
```
- 将宿主机的 应用目录映射到系统目录 /opt/cangling-apps
```shell
   -v /dir/(contain docker-compose.yaml):/opt/cangling-apps
```