package cn.mapway.gwt_template.shared.rpc.project.module;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

/**
 * 增强版基于字符串位的权限处理类
 * 整合了超级用户判定、权限合并与继承逻辑
 */
public class CommonPermission implements Serializable, IsSerializable {
    private static final char ALLOW = '1';
    private static final char DENY = '0';
    private final static String INIT_PERMISSION = "000000000000";
    private final StringBuilder bits;

    public CommonPermission() {
        this.bits = new StringBuilder(INIT_PERMISSION);
    }

    public CommonPermission(String data) {
        this.bits = new StringBuilder((data == null || data.isEmpty()) ? INIT_PERMISSION : data);
    }

    public static CommonPermission from(String data) {
        return new CommonPermission(data);
    }

    public static CommonPermission empty() {
        return new CommonPermission(null);
    }

    // --- 快捷工厂方法 ---

    public static CommonPermission owner() {
        return new CommonPermission().set(ProjectPermissionKind.OWNER, true);
    }

    public static CommonPermission admin() {
        return new CommonPermission().set(ProjectPermissionKind.ADMIN, true);
    }

    public static CommonPermission all() {
        CommonPermission commonPermission = new CommonPermission();
        for (ProjectPermissionKind kind : ProjectPermissionKind.values()) {
            commonPermission.set(kind, true);
        }
        return commonPermission;
    }

    // --- 核心逻辑：超级权限判定 ---

    /**
     * 是否拥有管理级权限（创建人或管理员）
     */
    public boolean isSuper() {
        // 直接访问位图，避免 has() 递归调用
        return isBitSet(ProjectPermissionKind.OWNER.getIndex())
                || isBitSet(ProjectPermissionKind.ADMIN.getIndex());
    }

    /**
     * 内部基础判断，不带业务溢出逻辑
     */
    private boolean isBitSet(int index) {
        if (index < 0 || index >= bits.length()) return false;
        return bits.charAt(index) == ALLOW;
    }

    /**
     * 核心判断逻辑：包含超级权限溢出逻辑
     * 如果用户是 ADMIN/OWNER，则默认拥有所有非系统级权限
     */
    public boolean has(ProjectPermissionKind type) {
        if (type == null) return false;
        // 如果是超级用户，且当前查询的不是系统位（OWNER/ADMIN本身），则默认允许
        if (!type.isSystem() && isSuper()) return true;

        return isBitSet(type.getIndex());
    }

    // --- 语义化快捷判断（基于继承逻辑） ---

    public boolean canRead() {
        return has(ProjectPermissionKind.READ);
    }

    public boolean canUpdate() {
        return has(ProjectPermissionKind.UPDATE);
    }

    public boolean canDelete() {
        return has(ProjectPermissionKind.DELETE);
    }

    public boolean canCreate() {
        return has(ProjectPermissionKind.CREATE);
    }

    public boolean isAdmin() {
        return isBitSet(ProjectPermissionKind.ADMIN.getIndex());
    }

    public CommonPermission setAdmin(boolean allow) {
        return set(ProjectPermissionKind.ADMIN, allow);
    }

    public boolean isCoder() {
        return isBitSet(ProjectPermissionKind.CODER.getIndex());
    }

    // --- 状态设置方法 ---

    public CommonPermission setCoder(boolean allow) {
        return set(ProjectPermissionKind.CODER, allow);
    }

    public boolean isOwner() {
        return isBitSet(ProjectPermissionKind.OWNER.getIndex());
    }

    /**
     * 是否是项目秘书
     *
     * @return
     */
    public boolean isSecretary() {
        return isBitSet(ProjectPermissionKind.SECRETARY.getIndex());
    }

    public CommonPermission set(ProjectPermissionKind type, boolean allow) {
        if (type == null) return this;
        int index = type.getIndex();
        ensureCapacity(index);
        bits.setCharAt(index, allow ? ALLOW : DENY);
        return this;
    }

    public CommonPermission seSecretary(boolean allow) {
        return set(ProjectPermissionKind.SECRETARY, allow);
    }

    public CommonPermission setRead(boolean allow) {
        return set(ProjectPermissionKind.READ, allow);
    }

    public CommonPermission setUpdate(boolean allow) {
        return set(ProjectPermissionKind.UPDATE, allow);
    }

    public CommonPermission setDelete(boolean allow) {
        return set(ProjectPermissionKind.DELETE, allow);
    }

    public CommonPermission setCreate(boolean allow) {
        return set(ProjectPermissionKind.CREATE, allow);
    }

    public CommonPermission setOwner() {
        return set(ProjectPermissionKind.OWNER, true);
    }


    private void ensureCapacity(int index) {
        while (bits.length() <= index) {
            bits.append(DENY);
        }
    }

    /**
     * 权限合并（OR 操作）
     * 用于将“项目权限”与“资源权限”合并，得出用户的最终有效权限
     */
    public CommonPermission merge(CommonPermission other) {
        if (other == null) return this;
        String otherData = other.toString();
        for (int i = 0; i < otherData.length(); i++) {
            if (otherData.charAt(i) == ALLOW) {
                ensureCapacity(i);
                bits.setCharAt(i, ALLOW);
            }
        }
        return this;
    }

    public CommonPermission merge(String other) {
        if (other == null || other.isEmpty()) return this;

        // 无论 other 多长或多短，我们只按索引位进行 OR 运算
        int len = other.length();
        for (int i = 0; i < len; i++) {
            if (other.charAt(i) == ALLOW) {
                // 只有当 other 明确说是 1 时，我们才把当前位设为 1
                ensureCapacity(i);
                bits.setCharAt(i, ALLOW);
            }
        }
        return this;
    }

    @Override
    public String toString() {
        return bits.toString();
    }

    public CommonPermission clear() {
        bits.setLength(0);
        bits.append(INIT_PERMISSION);
        return this;
    }
}