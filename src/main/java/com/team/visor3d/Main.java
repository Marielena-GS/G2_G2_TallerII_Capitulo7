package com.team.visor3d;

import com.formdev.flatlaf.FlatLightLaf;
import com.team.visor3d.gui.MainFrame;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        FlatLightLaf.setup();

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}