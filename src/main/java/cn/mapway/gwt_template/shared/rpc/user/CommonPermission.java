package cn.mapway.gwt_template.shared.rpc.user;

public class CommonPermission {
    private static final int READ   = 0b00000001;
    private static final int WRITE  = 0b00000010;
    private static final int DELETE = 0b00000100;
    private static final int CREATE = 0b00001000;
    private static final int ADMIN  = 0b00010000;
    int value;

    public CommonPermission(int permission) {
        this.value = permission;
    }

    public CommonPermission() {
        value = 0;
    }

    public static CommonPermission fromPermission(Integer permission) {
        if (permission == null) {
            return new CommonPermission(0);
        } else {
            return new CommonPermission(permission);
        }
    }

    public boolean canRead() {
        return (value & READ) != 0;
    }

    public CommonPermission setRead(boolean read) {
        if (read) {
            value |= READ;
        } else {
            value &= ~READ;
        }
        return this;
    }
    public boolean isAdmin() {
        return (value & ADMIN) != 0;
    }

    public CommonPermission setAdmin(boolean admin) {
        if (admin) {
            value |= ADMIN;
        } else {
            value &= ~ADMIN;
        }
        return this;
    }
    public boolean canWrite() {
        return (value & WRITE) != 0;
    }

    public CommonPermission setWrite(boolean write) {
        if (write) {
            value |= WRITE;
        } else {
            value &= ~WRITE;
        }
        return this;
    }

    public boolean canDelete() {
        return (value & DELETE) != 0;
    }

    public CommonPermission setDelete(boolean delete) {
        if (delete) {
            value |= DELETE;
        } else {
            value &= ~DELETE;
        }
        return this;
    }

    public boolean canCreate() {
        return (value & CREATE) != 0;
    }

    public CommonPermission setCreate(boolean create) {
        if (create) {
            value |= CREATE;
        } else {
            value &= ~CREATE;
        }
        return this;
    }

    public CommonPermission setAll() {
        value = READ | WRITE | DELETE | CREATE| ADMIN;
        return this;
    }

    public int getPermission() {
        return value;
    }
}
