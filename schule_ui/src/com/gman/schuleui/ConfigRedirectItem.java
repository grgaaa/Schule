package com.gman.schuleui;

import com.gman.schule.common.SchuleConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConfigRedirectItem extends JPanel {
    private JTextField urlFrom;
    private JTextField ipTo;
    private JLabel ipFromLabel;
    private JLabel urlToLabel;
    private JButton deleteRedirectItemBtn;
    private JCheckBox enabledCheckBox;

    public ConfigRedirectItem() {
        this.add(enabledCheckBox);
        this.add(ipFromLabel);
        this.add(urlFrom);
        this.add(urlToLabel);
        this.add(ipTo);
        this.add(deleteRedirectItemBtn);

        urlFrom.setColumns(10);
        ipTo.setColumns(10);

        deleteRedirectItemBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Container ancestorOfClass = SwingUtilities.getAncestorOfClass(JPanel.class, (Component) e.getSource());
                Container itemContainer = ancestorOfClass.getParent();
                itemContainer.remove(ancestorOfClass);
                itemContainer.invalidate();
                itemContainer.repaint();
            }
        });
    }

    public ConfigRedirectItem(SchuleConfig.RedirectItem redirectItem) {
        this();
        if (redirectItem != null) {
            urlFrom.setText(redirectItem.getUrlFrom());
            ipTo.setText(redirectItem.getIpTo());
            enabledCheckBox.setSelected(redirectItem.isEnabled());
        }
    }

    public JTextField getUrlFrom() {
        return urlFrom;
    }

    public JTextField getIpTo() {
        return ipTo;
    }

    public JCheckBox getEnabledCheckBox() {
        return enabledCheckBox;
    }
}
