package eflang.vibe;

import javax.swing.*;

/**
 * VIBE - Visual Interface for Building Earfuck
 * @author Ryan Norris
 *
 */

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.create();
        });
    }
}
