package cn.mapway.gwt_template.client.workspace.gantt;

public class CalendarTimes {
    // 毫秒常量定义
    public static final double MS_PER_HOUR = 3600 * 1000.0;
    public static final double MS_PER_DAY = 24 * MS_PER_HOUR;
    public static final double MS_PER_WEEK = 7 * MS_PER_DAY;
    public static final double MS_PER_MONTH = 30 * MS_PER_DAY; // 约数，计算用
    public static final double MS_PER_QUARTER = 91 * MS_PER_DAY;
    public static final double MS_PER_YEAR = 365 * MS_PER_DAY;
    // 预创建两个对象用于计算
    private final elemental2.core.JsDate tempDateCalc1 = new elemental2.core.JsDate();
    private final elemental2.core.JsDate tempDateCalc2 = new elemental2.core.JsDate();

    /**
     * 根据当前像素密度，自动计算最佳的时间步进
     */
    public double getBestStepMs(double dayWidth) {
        if (dayWidth >= 1000) return MS_PER_HOUR;
        if (dayWidth >= 300) return MS_PER_HOUR * 6;      // 增加 6 小时档位
        if (dayWidth >= 150) return MS_PER_HOUR * 12;     // 增加 12 小时档位
        if (dayWidth >= 40) return MS_PER_DAY;           // 每天
        if (dayWidth >= 10) return MS_PER_WEEK;           // 中：每周一格
        if (dayWidth >= 2) return MS_PER_MONTH;          // 小：每月一格
        if (dayWidth >= 0.5) return MS_PER_QUARTER;        // 极小：每季度一格
        if (dayWidth >= 0.1) return MS_PER_YEAR;           // 极细：每年一格
        return MS_PER_YEAR * 10;                           // 宏观：十年一格
    }

    /**
     * 获取给定日期是当年的第几周
     *
     * @param date 目标日期
     * @return 周数 (1-53)
     */
    public int getWeekOfYear(elemental2.core.JsDate date) {
        // 1. 创建当年的 1 月 1 日
        elemental2.core.JsDate startOfYear = new elemental2.core.JsDate(date.getFullYear(), 0, 1);

        // 2. 计算 1 月 1 日是周几 (0是周日, 1是周一...)
        // 如果你希望周一作为一周的开始，需要调整偏移
        double startDayOfWeek = startOfYear.getDay();
        if (startDayOfWeek == 0) startDayOfWeek = 7; // 将周日转为 7

        // 3. 计算目标日期相对于 1 月 1 日的天数差
        double diffInMs = date.getTime() - startOfYear.getTime();
        double diffInDays = Math.floor(diffInMs / (1000 * 60 * 60 * 24));

        // 4. 核心公式：(天数差 + 1月1日的周偏移) / 7
        return (int) Math.ceil((diffInDays + startDayOfWeek) / 7);
    }

    /**
     * 判断是否为视觉上的重要节点
     */
    public boolean isImportantNode(elemental2.core.JsDate date, double stepMs) {
        if (stepMs == MS_PER_DAY) return date.getDay() == 1; // 周一
        if (stepMs == MS_PER_WEEK) return date.getDate() <= 7; // 每月第一周
        if (stepMs < MS_PER_DAY) return date.getHours() == 0 || date.getHours() == 12; // 零点或正午
        return false;
    }

    public String[] getLabelCandidates(elemental2.core.JsDate date, double stepMs) {
        int month = date.getMonth() + 1;
        int day = date.getDate();
        int hours = date.getHours();

        if (stepMs < MS_PER_DAY) { // 小时级别
            String timeStr = (hours < 10 ? "0" + hours : hours) + ":00";
            String period = hours < 12 ? "上午" : "下午";
            return new String[]{timeStr, String.valueOf(hours), String.valueOf(hours).substring(0, 1)};
        }

        if (stepMs <= MS_PER_DAY) { // 天级别
            return new String[]{
                    month + "月" + day + "日",
                    String.valueOf(day),
                    "" // 实在太窄就不显示
            };
        }

        if (stepMs <= MS_PER_DAY * 7) { // 周级别
            int weekNum = getWeekOfYear(date);
            return new String[]{
                    "第" + weekNum + "周",
                    "W" + weekNum,
                    String.valueOf(weekNum)
            };
        }

        if (stepMs <= MS_PER_MONTH * 1.5) { // 月级别
            return new String[]{month + "月份", month + "月", String.valueOf(month)};
        }

        if (stepMs <= MS_PER_MONTH * 4) { // 季度级别
            int Q = (int) Math.floor((month - 1) / 3) + 1;
            return new String[]{"第" + Q + "季度", Q + "季度", "Q" + Q, String.valueOf(Q)};
        }

        if (stepMs <= MS_PER_YEAR * 1.5) { // 年级别
            int year = date.getFullYear();
            return new String[]{year + "年度", year + "年", String.valueOf(year).substring(2)};
        }

        // 十年级别
        int year = date.getFullYear();
        return new String[]{year + "年", String.valueOf(year).substring(2)};
    }

    public elemental2.core.JsDate getDynamicAlignedStart(long time, double stepMs) {
        elemental2.core.JsDate d = new elemental2.core.JsDate((double) time);
        d.setHours(0, 0, 0, 0); // 必须重置时分秒

        if (stepMs >= MS_PER_YEAR * 0.9) { // 年或十年
            d.setMonth(0);
            d.setDate(1);
        } else if (stepMs >= MS_PER_QUARTER * 0.9) { // 季度
            // 关键：将月份对齐到 0, 3, 6, 9 (即 Q1, Q2, Q3, Q4 的起始月)
            int currentMonth = d.getMonth();
            int quarterStartMonth = (currentMonth / 3) * 3;
            d.setMonth(quarterStartMonth);
            d.setDate(1);
        } else if (stepMs >= MS_PER_MONTH * 0.9) { // 月
            d.setDate(1);
        } else if (stepMs >= MS_PER_WEEK * 0.9) { // 周
            int day = d.getDay();
            int diff = (day == 0 ? 6 : day - 1);
            d.setDate(d.getDate() - diff);
        }

        // 为了防止拖动时左侧露出空白，我们将对齐点再向左预推一个步进
        // 这样在任何时刻，屏幕左边缘外都有一个完整的格子作为缓冲
        double bufferMs = calculateActualStepMsFast(d.getTime(), -stepMs); // 注意这里需要支持负向计算
        d.setTime(d.getTime() + bufferMs);

        return d;
    }

    public double calculateActualStepMsFast(double currentTimeMs, double stepMs) {
        tempDateCalc1.setTime(currentTimeMs);
        tempDateCalc2.setTime(currentTimeMs);

        // ... 前面的小时、天判断 ...


        if (stepMs <= MS_PER_DAY * 7) {
            return stepMs; // 小时、天、周是固定的
        } else if (stepMs <= MS_PER_MONTH * 1.5) {
            tempDateCalc2.setMonth(tempDateCalc1.getMonth() + 1);
        } else if (stepMs <= MS_PER_QUARTER * 1.5) {
            tempDateCalc2.setMonth(tempDateCalc1.getMonth() + 3); // 步进一季
        } else if (stepMs <= MS_PER_YEAR * 1.5) {
            tempDateCalc2.setFullYear(tempDateCalc1.getFullYear() + 1);
        } else {
            tempDateCalc2.setFullYear(tempDateCalc1.getFullYear() + 10); // 步进十年
        }
        return tempDateCalc2.getTime() - tempDateCalc1.getTime();
    }

    /**
     * 根据 dayWidth 动态决定上层标题的粒度
     */
    public double getTopStepMs(double bottomStepMs) {
        if (bottomStepMs <= MS_PER_HOUR * 4) return MS_PER_DAY;     // 底层是小时 -> 顶层是天
        if (bottomStepMs <= MS_PER_DAY) return MS_PER_MONTH;       // 底层是天 -> 顶层是月
        if (bottomStepMs <= MS_PER_WEEK) return MS_PER_YEAR;      // 底层是周 -> 顶层是年
        if (bottomStepMs <= MS_PER_MONTH * 1.5) return MS_PER_YEAR;// 底层是月 -> 顶层是年
        if (bottomStepMs <= MS_PER_QUARTER * 1.5) return MS_PER_YEAR; // 底层是季度 -> 顶层是年
        return MS_PER_YEAR * 10;                                  // 底层是年 -> 顶层是十年
    }

    public String getTopLabelByStep(elemental2.core.JsDate date, double topStepMs, double periodW) {
        int year = date.getFullYear();
        int month = date.getMonth() + 1;

        if (topStepMs <= MS_PER_DAY) { // 顶层是天（底层是小时）
            if (periodW > 120) return year + "年" + month + "月" + date.getDate() + "日";
            return month + "月" + date.getDate() + "日";
        }

        if (topStepMs <= MS_PER_MONTH * 1.5) { // 顶层是月（底层是天）
            if (periodW > 80) return year + "年" + month + "月";
            return month + "月";
        }

        if (topStepMs <= MS_PER_YEAR * 1.5) { // 顶层是年（底层是周/月/季）
            if (periodW > 60) return year + "年";
            return String.valueOf(year);
        }

        // 顶层是十年（底层是年）
        int decadeStart = (year / 10) * 10;
        return decadeStart + " - " + (decadeStart + 10);
    }

}
