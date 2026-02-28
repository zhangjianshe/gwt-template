package cn.mapway.gwt_template.server.service.user;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class AvatarGenerator {

    public static void main(String[] args) {
        String username = "Gemini AI"; // 目标用户名
        generateAvatar(username, "avatar.png");
    }

    public static void generateAvatar(String name, String outputPath) {
        int size = 128; // 头像尺寸 (128-128)

        // 1. 创建画布
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();

        // 开启抗锯齿，让文字和圆角更平滑
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 2. 根据用户名生成背景颜色
        int nameHash = name.hashCode();
        Color backgroundColor = new Color(nameHash & 0xFF, (nameHash >> 8) & 0xFF, (nameHash >> 16) & 0xFF).brighter();
        g2.setColor(backgroundColor);
        g2.fillRect(0, 0, size, size);

        // 3. 绘制文字 (提取首字母)
        String initial = name.trim().substring(0, 1).toUpperCase();
        g2.setColor(Color.WHITE); // 文字颜色
        g2.setFont(new Font("SansSerif", Font.BOLD, size / 2));

        // 获取文字的边界以实现居中
        FontMetrics fm = g2.getFontMetrics();
        int x = (size - fm.stringWidth(initial)) / 2;
        int y = ((size - fm.getHeight()) / 2) + fm.getAscent();

        g2.drawString(initial, x, y);
        g2.setClip(new Ellipse2D.Float(0, 0, size, size));
        g2.dispose();

        // 4. 保存为文件
        try {
            ImageIO.write(image, "png", new File(outputPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}