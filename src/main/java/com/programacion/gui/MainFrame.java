package com.programacion.gui;

import javax.swing.JFrame;
import java.awt.BorderLayout;

public class MainFrame extends JFrame {

    public MainFrame() {
        super("G2-G3 Taller II");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);
    }
}
