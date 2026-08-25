#!/bin/bash

# ==================== 1. 配置你的 LDAP 连接参数 ====================
LDAP_HOST="ldap.cangling.cn"                               # 你的 LDAP 服务器 IP
LDAP_PORT="389"                                     # 端口，默认 389
BIND_DN="cn=admin,dc=cangling,dc=cn"                # 管理员账号 (ManagerDN)
BIND_PASS="Cangling2025*"                           # 管理员密码

SEARCH_BASE="ou=users,dc=cangling,dc=cn"            # 你的 SearchBase 路径
# ===================================================================

# 提示输入要测试的用户名或邮箱
read -p "请输入要测试的登录账号 (UserName 或 Email): " TEST_USER

if [ -z "$TEST_USER" ]; then
    echo -e "\033[31m[错误] 输入不能为空！\033[0m"
    exit 1
fi

# 完美复现你的 Java 过滤规则
FILTER="(&(objectClass=inetOrgPerson)(|(uid=${TEST_USER})(mail=${TEST_USER})))"

echo "------------------------------------------------------------"
echo -e "🔍 开始在 \033[34m$SEARCH_BASE\033[0m 下检索..."
echo -e "🧪 运行过滤器: \033[33m$FILTER\033[0m"
echo "------------------------------------------------------------"

# 💡 核心修复：将旧版的 -h $LDAP_HOST -p $LDAP_PORT 替换为新版标准的 -H URI 语法
LDAP_URI="ldap://${LDAP_HOST}:${LDAP_PORT}"

# 执行 ldapsearch 捞取 DN、uid 和 mail 属性
# -x: 简单认证, -LLL: 纯净输出格式, -o ldif-wrap=no: 防止长文本折行
RAW_RESULT=$(ldapsearch -x -H "$LDAP_URI" \
                        -D "$BIND_DN" -w "$BIND_PASS" \
                        -b "$SEARCH_BASE" \
                        -s sub \
                        -o ldif-wrap=no \
                        "$FILTER" "dn" "uid" "mail" 2>&1)

# 检查命令本身是否执行失败（比如密码错误或无法连接）
if [ $? -ne 0 ]; then
    echo -e "\033[31m[连接失败] ldapsearch 执行出错，请检查连接参数或密码！\033[0m"
    echo "$RAW_RESULT"
    exit 1
fi

# 统计命中的记录数量 (通过统计吐出的 dn: 行数)
MATCH_COUNT=$(echo "$RAW_RESULT" | grep -i "^dn:" | wc -l)

if [ "$MATCH_COUNT" -eq 0 ]; then
    echo -e "🟢 \033[32m结果正常:\033[0m 没有任何匹配项。该账号可放心注册。"
elif [ "$MATCH_COUNT" -eq 1 ]; then
    echo -e "🟢 \033[32m结果正常 (符合预期):\033[0m 精确匹配到 1 个唯一用户。"
    echo "============================================================"
    echo "$RAW_RESULT" | grep -E "^(dn|uid|mail):"
    echo "============================================================"
else
    # 💥 精准复现你的 actual 3 报错场景
    echo -e "❌ \033[31m[警报] 触发多匹配错误 (Incorrect result size)！\033[0m"
    echo -e "💥 期望结果: \033[32m1\033[0m, 实际匹配到: \033[31m$MATCH_COUNT\033[0m"
    echo -e "👉 这会直接导致 Spring Security 登录直接崩溃。"
    echo "------------------------------------------------------------"
    echo -e "\033[33m以下是导致你登录卡死的 $MATCH_COUNT 个冲突账号列表，请对照清理：\033[0m"
    echo "------------------------------------------------------------"

    # 格式化输出这几个内鬼的分路 DN
    echo "$RAW_RESULT" | awk '
        /^[Dd][Nn]:/ {print "\033[36m📍 账号节点 (DN):\033[0m " $0}
        /^[Uu][Ii][Dd]:/ {print "   ├─ 用户标识 (uid): " $0}
        /^[Mm][Aa][Ii][Ll]:/ {print "   └─ 绑定邮箱 (mail): " $0 "\n"}
    ' | sed 's/dn: //g' | sed 's/uid: //g' | sed 's/mail: //g'
fi