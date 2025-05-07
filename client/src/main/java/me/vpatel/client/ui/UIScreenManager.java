package me.vpatel.client.ui;

import javax.swing.*;

public class UIScreenManager {
    private static JFrame currentFrame;
    public static void showScreen(JFrame newFrame) {
        if (currentFrame != null) {
            currentFrame.dispose();
        }
        currentFrame = newFrame;
        currentFrame.setVisible(true);
    }

    public static JFrame getCurrentFrame() {
        return currentFrame;
    }
}