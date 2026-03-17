package db;

public class LongTest {
    public static void main(String[] args) {
        long maxValue = Long.MAX_VALUE;

        System.out.println("数值: " + maxValue);
        // 直接输出二进制字符串
        System.out.println("二进制: " + Long.toBinaryString(maxValue));
        // 如果你想看到完整的 64 位（补齐前面的 0）
        System.out.println("完整64位: " + String.format("%64s", Long.toBinaryString(maxValue)).replace(' ', '0'));
    }
}
