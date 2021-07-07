package com.gman.schuleui;

import com.gman.schule.common.SchuleConfig;
import com.gman.schule.common.Utils;

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ConfigForm extends JFrame {

    private JPanel schedulePanel;
    private JPanel redirectPanel;
    private JScrollPane scheduleScroll;
    private JPanel scheduleScrollPanel;
    private JScrollPane redirectScroll;
    private JPanel redirectScrollPanel;
    private JButton addScheduleButton;
    private JPanel mainPanel;
    private JButton addRedirectButton;
    private JButton saveAllAndExitButton;
    private JCheckBox enableConfigCheckbox;
    private JPanel confirmPanel;
    private JPanel usersPanel;
    private JComboBox usersComboBox;
    private JLabel pauseLabel;
    private JTextField pauseValue;

    public ConfigForm(String title) throws HeadlessException {
        super(title);

        this.setSize(800,800);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        addScheduleButton.addActionListener(e -> addConfigScheduleItem(null));
        addRedirectButton.addActionListener(e -> addConfigRedirectItem(null));

        saveAllAndExitButton.addActionListener(e -> saveAllAndExit());

        usersComboBox.addItemListener(e -> loadExistingConfiguration((String) e.getItem()));

        loadUsers();
        loadExistingConfiguration(getSelectedUser());
    }

    private void createUIComponents() {
        scheduleScrollPanel = new JPanel();
        scheduleScrollPanel.setLayout(new BoxLayout(scheduleScrollPanel, BoxLayout.Y_AXIS));
        scheduleScroll = new JScrollPane(scheduleScrollPanel);

        redirectScrollPanel = new JPanel();
        redirectScrollPanel.setLayout(new BoxLayout(redirectScrollPanel, BoxLayout.Y_AXIS));
        redirectScroll = new JScrollPane(redirectScrollPanel);
    }

    private void addConfigRedirectItem(SchuleConfig.RedirectItem redirectItem) {
        redirectScrollPanel.add(new ConfigRedirectItem(redirectItem));
        redirectScroll.validate();
        redirectScroll.repaint();
    }

    private void addConfigScheduleItem(SchuleConfig.ScheduleItem scheduleItem) {
        scheduleScrollPanel.add(new ConfigScheduleItem(scheduleItem));
        scheduleScroll.validate();
        scheduleScroll.repaint();
    }


    private void loadExistingConfiguration(String user) {
        scheduleScrollPanel.removeAll();
        scheduleScrollPanel.validate();
        scheduleScrollPanel.repaint();

        redirectScrollPanel.removeAll();
        redirectScrollPanel.validate();
        redirectScrollPanel.repaint();

        SchuleConfig schuleConfig = SchuleConfig.loadFromConfigFile(user);
        if (schuleConfig == null) { return; }

        for (SchuleConfig.ScheduleItem scheduleItem : schuleConfig.getScheduleItems()) {
            addConfigScheduleItem(scheduleItem);
        }

        for (SchuleConfig.RedirectItem redirectItem : schuleConfig.getRedirectItems()) {
            addConfigRedirectItem(redirectItem);
        }

        enableConfigCheckbox.setSelected(schuleConfig.isEnabled());
        SchuleConfig.Pause pause = schuleConfig.getPause();
        if (pause != null) {
            long remaining = (pause.getFrom() + pause.getDuration()) - System.currentTimeMillis();
            if (remaining > 0) {
                pauseValue.setText(""+TimeUnit.MILLISECONDS.toMinutes(remaining));
            }
        }
    }

    private void loadUsers() {
        String currentUser = Utils.getCurrentSystemUser();

        List<String> systemUsers = Utils.getSystemUsers();
        systemUsers.sort((user1, user2) -> {
            if (currentUser.equals(user1)) {
                return -1;
            } else {
                return user1.compareTo(user2);
            }
        });
        for (String systemUser : systemUsers) {
            usersComboBox.addItem(systemUser);
        }
    }

    private String getSelectedUser() {
        return (String) usersComboBox.getSelectedItem();
    }


    private void saveAllAndExit() {
        SchuleConfig schuleConfig = new SchuleConfig();
        schuleConfig.setEnabled(enableConfigCheckbox.isSelected());

        String pauseVal = pauseValue.getText();
        if (pauseVal != null && pauseVal.matches("^[0-9]{1,3}$")) {
            int durationMinutes = Integer.parseInt(pauseVal);
            SchuleConfig.Pause pause = new SchuleConfig.Pause(System.currentTimeMillis(), TimeUnit.MINUTES.toMillis(durationMinutes));
            schuleConfig.setPause(pause);
        }

        Component[] scheduleItems = scheduleScrollPanel.getComponents();
        if (scheduleItems != null && scheduleItems.length > 0) {
            for (Component scheduleItem : scheduleItems) {

                ConfigScheduleItem configScheduleItem = (ConfigScheduleItem) scheduleItem;
                String activeDay = (String) configScheduleItem.getDaysOfWeek().getItemAt(configScheduleItem.getDaysOfWeek().getSelectedIndex());
                if (activeDay == null || activeDay.length() == 0) {
                    continue;
                }

                int dayOfWeek = -1;
                switch (activeDay) {
                    case "Monday": dayOfWeek = DayOfWeek.MONDAY.getValue(); break;
                    case "Tuesday": dayOfWeek = DayOfWeek.TUESDAY.getValue(); break;
                    case "Wednesday": dayOfWeek = DayOfWeek.WEDNESDAY.getValue(); break;
                    case "Thursday": dayOfWeek = DayOfWeek.THURSDAY.getValue(); break;
                    case "Friday": dayOfWeek = DayOfWeek.FRIDAY.getValue(); break;
                    case "Saturday": dayOfWeek = DayOfWeek.SATURDAY.getValue(); break;
                    case "Sunday": dayOfWeek = DayOfWeek.SUNDAY.getValue(); break;
                }
                String timeFrom = configScheduleItem.getTimeFrom().getText();
                String timeTo = configScheduleItem.getTimeTo().getText();
                boolean enabled = configScheduleItem.getEnabledCheckBox().isSelected();

                SchuleConfig.ScheduleItem item = new SchuleConfig.ScheduleItem();
                item.setDayOfWeek(dayOfWeek);
                item.setTimeFrom(timeFrom);
                item.setTimeTo(timeTo);
                item.setEnabled(enabled);

                schuleConfig.addScheduleItem(item);
            }
        }

        Component[] redirectItems = redirectScrollPanel.getComponents();
        if (redirectItems != null && redirectItems.length > 0) {
            for (Component redirectItem : redirectItems) {

                ConfigRedirectItem configRedirectItem = (ConfigRedirectItem) redirectItem;

                String ipFrom = configRedirectItem.getUrlFrom().getText();
                String urlTo = configRedirectItem.getIpTo().getText();
                boolean enabled = configRedirectItem.getEnabledCheckBox().isSelected();

                SchuleConfig.RedirectItem item = new SchuleConfig.RedirectItem();
                item.setUrlFrom(ipFrom);
                item.setIpTo(urlTo);
                item.setEnabled(enabled);

                schuleConfig.addRedirectItem(item);
            }
        }

        boolean result = schuleConfig.writeToConfigFile(getSelectedUser());
        if (result) {
            System.exit(0);
        }
    }

}
