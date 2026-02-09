package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.SysUserKeyEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateUserKeyRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateUserKeyResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.common.config.keys.AuthorizedKeyEntry;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.security.PublicKey;
import java.sql.Timestamp;

/**
 * UpdateUserKeyExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateUserKeyExecutor extends AbstractBizExecutor<UpdateUserKeyResponse, UpdateUserKeyRequest> {
    @Resource
    Dao dao;

    @Override
    protected BizResult<UpdateUserKeyResponse> process(BizContext context, BizRequest<UpdateUserKeyRequest> bizParam) {
        UpdateUserKeyRequest request = bizParam.getData();
        log.info("UpdateUserKeyExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        SysUserKeyEntity requestKey = request.getKey();
        assertNotNull(requestKey, "请提供公钥");
        assertTrue(Strings.isNotBlank(requestKey.getName()), "请为公钥提供一个合适的名称");

        if (Strings.isBlank(requestKey.getId())) {
            try {
                // 1. Parse and Resolve
                // We trim to ensure no trailing newlines from the UI break the parser
                AuthorizedKeyEntry entry = AuthorizedKeyEntry.parseAuthorizedKeyEntry(requestKey.getKey().trim());
                PublicKey publicKey = entry.resolvePublicKey(null, null, null);

                if (publicKey == null) {
                    return BizResult.error(500, "公钥解析失败：不支持的加密算法");
                }

                // 2. Calculate Fingerprint
                // This generates the standard "SHA256:..." string
                String fingerprint = KeyUtils.getFingerPrint(publicKey);

                // 3. Check for duplicates before inserting
                if (dao.fetch(SysUserKeyEntity.class, fingerprint) != null) {
                    return BizResult.error(500, "该公钥已存在，请勿重复添加");
                }

                requestKey.setId(fingerprint);
                requestKey.setLastUsed(new Timestamp(System.currentTimeMillis()));
                requestKey.setUserId(user.getUser().getUserId());
                requestKey.setUserName(user.getUser().getUserName());
                requestKey.setCreateTime(new Timestamp(System.currentTimeMillis()));
                if (requestKey.getExpiredTime() == null || requestKey.getExpiredTime() <= 0) {
                    requestKey.setExpiredTime(0L);
                }

                dao.insert(requestKey);

            } catch (Exception e) {
                log.error("[GIT KEY] 解析异常: {}", requestKey.getKey(), e);
                return BizResult.error(500, "公钥格式不正确: " + e.getMessage());
            }
        } else {
            // Update existing key metadata (name/expiry)
            SysUserKeyEntity keyInDb = dao.fetch(SysUserKeyEntity.class, requestKey.getId());
            assertNotNull(keyInDb, "未找到指定的公钥记录");

            // Security check: ensure the key belongs to the logged-in user
            if (!keyInDb.getUserId().equals(user.getUser().getUserId())) {
                return BizResult.error(403, "无权修改此公钥");
            }

            keyInDb.setName(requestKey.getName());
            keyInDb.setExpiredTime(requestKey.getExpiredTime() == null ? 0L : requestKey.getExpiredTime());

            dao.update(keyInDb);
        }

        return BizResult.success(new UpdateUserKeyResponse());
    }
}
