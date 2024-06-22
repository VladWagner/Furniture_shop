package gp.wagner.backend.infrastructure;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ProfileImageGenerator {

    private static final Color[] colors;

    static {
        colors = new Color[]{
          new Color(187,173,237,255),
          Color.PINK,
          new Color(143,193,230,255)
        };
    }

    public static BufferedImage createImgWithChars(String symbols){

        BufferedImage image = new BufferedImage(256,256,BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics2D = image.createGraphics();

        // Antialiaing позволит улучшить качество текста
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //GradientPaint gradient = new GradientPaint(0, 0, Color.BLUE, image.getWidth(), image.getHeight(), Color.RED);

        graphics2D.setColor(Utils.getRandomArrayElement(colors));

        graphics2D.fillRect(0, 0, image.getWidth(), image.getHeight());

        // Цвет текста
        graphics2D.setColor(Color.WHITE);

        graphics2D.setFont(new Font(Font.SANS_SERIF, Font.BOLD,120));

        FontMetrics metrics = graphics2D.getFontMetrics();

        // Определить позицию текста

        int coordX = (image.getWidth() - metrics.stringWidth(symbols))/2;
        int coordY = ((image.getHeight() - metrics.getHeight())/2) + metrics.getAscent();


        // Нарисовать текст
        graphics2D.drawString(symbols, coordX, coordY);
        graphics2D.dispose();

        return image;
    }

}
