package cn.mapway.gwt_template.shared.rpc.file;

public enum SecurityLevel {
    /**
     * 公开 (Rank 0): 可对社会公开
     */
    PUBLIC("公开", 0, "#28a745"),

    /**
     * 内部 (Rank 1): 仅限组织内部知悉
     */
    INTERNAL("内部", 1, "#17a2b8"),

    /**
     * 秘密 (Rank 2): 泄露会损害国家/单位利益
     */
    CONFIDENTIAL("秘密", 2, "#ffc107"),

    /**
     * 机密 (Rank 3): 泄露会造成严重损害
     */
    SECRET("机密", 3, "#fd7e14"),

    /**
     * 绝密 (Rank 4): 泄露会造成特别严重损害
     */
    TOP_SECRET("绝密", 4, "#dc3545");

    private final String text;
    private final Integer rank;
    private final String color;

    SecurityLevel(String text, int rank, String color) {
        this.text = text;
        this.rank = rank;
        this.color = color;
    }

    /**
     * 根据名称（中文或英文名）安全解析枚举
     */
    public static SecurityLevel fromString(String value) {
        if (value == null) return INTERNAL;
        for (SecurityLevel level : SecurityLevel.values()) {
            if (level.text.equals(value) || level.name().equalsIgnoreCase(value)) {
                return level;
            }
        }
        return INTERNAL;
    }

    /**
     * 根据 Rank 数值解析枚举
     */
    public static SecurityLevel fromRank(Integer rank) {
        if (rank == null) return INTERNAL;
        for (SecurityLevel level : SecurityLevel.values()) {
            if (level.rank == rank) {
                return level;
            }
        }
        return INTERNAL;
    }

    public String getText() {
        return text;
    }

    public Integer getRank() {
        return rank;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return this.text;
    }

    /**
     * 核心逻辑：判断当前权限是否可以查看目标密级
     *
     * @param targetLevel 目标文件的密级
     * @return true 表示有权限
     */
    public boolean canAccess(SecurityLevel targetLevel) {
        return this.rank >= targetLevel.getRank();
    }
}