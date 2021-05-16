package com.gman.schuleui;

import com.gman.schule.common.SchuleConfig;

import javax.swing.*;

public class UserItem extends JPanel {
    private JCheckBox userCheckBox;
    private SchuleConfig.UserItem userItem;

    public UserItem() {
        this.add(userCheckBox);
    }

    public UserItem(SchuleConfig.UserItem userItem) {
        this();
        userCheckBox.setText(userItem.getUser());
    }

    public JCheckBox getUserCheckBox() {
        return userCheckBox;
    }

    public SchuleConfig.UserItem getUserItem() {
        return userItem;
    }
}
