package com.gman.schuleui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Login extends JFrame {
    private JPanel loginPanel;
    private JPasswordField passwordField;
    private JButton OKButton;

    public Login(String title) throws HeadlessException {
        super(title);

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setContentPane(loginPanel);
        this.pack();
        this.setLocationRelativeTo(null);

        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (passwordField.getPassword() != null && "test".equals(new String(passwordField.getPassword()))) {
                    // show configuration screen
                    dispose();
                    new ConfigForm("Schule");
                } else {
                    System.exit(0);
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Login login = new Login("Schule");
                login.setVisible(true);
            }
        });
    }
}
