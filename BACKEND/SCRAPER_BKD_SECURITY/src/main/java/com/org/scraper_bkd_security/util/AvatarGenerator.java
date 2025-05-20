package com.org.scraper_bkd_security.util;

import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;
import javax.imageio.ImageIO;

@Component
public class AvatarGenerator {


    public  byte[] generateAvatar(String username) {
        int size = 200;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        // High-quality rendering
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Background color
        Color bgColor = getColorFromHash(username);
        g.setColor(bgColor);
        g.fillRoundRect(0, 0, size, size, 48, 48); // rounded corners

        //generate dynamic icon color
        // Calculate brightness
        float brightness = (0.299f * bgColor.getRed() + 0.587f * bgColor.getGreen() + 0.114f * bgColor.getBlue()) / 255f;

// Icon color: adaptive
        Color iconColor = brightness > 0.7f
                ? new Color(0, 0, 0, 50)     // dark icon for light bg
                : new Color(255, 255, 255, 40); // light icon for dark bg

        // Icons
        String[] icons = {"$", "%", "💰", "🏷️","💶","⚡"};
        Random random = new Random(username.hashCode());
        g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
       // g.setColor(new Color(255, 255, 255, 40)); // very light white
      //  g.setColor(iconColor);
        g.setColor(new Color(0, 0, 0, 50));

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                int x = col * (size / 4) + 10;
                int y = row * (size / 4) + 40;
                String icon = icons[random.nextInt(icons.length)];
                g.drawString(icon, x, y);
            }
        }

        // First letter
        g.setFont(new Font("SansSerif", Font.BOLD, 100));
        g.setColor(new Color(255, 255, 255, 230));
        String firstLetter = username.substring(0, 1).toUpperCase();

        FontMetrics fm = g.getFontMetrics();
        int letterX = (size - fm.stringWidth(firstLetter)) / 2;
        int letterY = (size - fm.getHeight()) / 2 + fm.getAscent();

        // Optional drop shadow
        g.setColor(new Color(0, 0, 0, 80));
        g.drawString(firstLetter, letterX + 3, letterY + 3);

        g.setColor(Color.WHITE);
        g.drawString(firstLetter, letterX, letterY);

        g.dispose();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Color getColorFromHash(String input) {
        int hash = input.hashCode();
        int r = (hash >> 16) & 0xFF;
        int g = (hash >> 8) & 0xFF;
        int b = (hash) & 0xFF;
        return new Color((r + 128) % 256, (g + 128) % 256, (b + 128) % 256); // pastelish
    }


    public static void main(String[] args) throws IOException {
        String name = "aditya";
        int size = 88;
        AvatarGenerator obj=new AvatarGenerator();
      //  byte[] imageBytes=obj.generateAvatar(name,size);
        byte[] imageBytes = obj.generateAvatar(name);
        if (imageBytes != null) {
            try {
                // Convert byte array back to BufferedImage
                BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(imageBytes));

                // Save the image to the current directory
                File outputfile = new File(name + "_avatar.png");
                ImageIO.write(image, "png", outputfile);
                System.out.println("Avatar saved to: " + outputfile.getAbsolutePath());

                // Optionally, print Base64 encoded string
                System.out.println(Base64.getEncoder().encodeToString(imageBytes));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }
}
