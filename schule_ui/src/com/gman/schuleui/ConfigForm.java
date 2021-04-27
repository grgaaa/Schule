package com.gman.schuleui;

import com.gman.schule.common.SchuleConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.DayOfWeek;

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

    public ConfigForm(String title) throws HeadlessException {
        super(title);

        this.setSize(800,600);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        addScheduleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addConfigScheduleItem(null);
            }
        });
        addRedirectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addConfigRedirectItem(null);
            }
        });

        saveAllAndExitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveAllAndExit();
            }
        });

        loadExistingConfiguration();
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

    private void loadExistingConfiguration() {
        SchuleConfig schuleConfig = SchuleConfig.loadFromConfigFile();
        if (schuleConfig == null) { return; }

        for (SchuleConfig.ScheduleItem scheduleItem : schuleConfig.getScheduleItems()) {
            addConfigScheduleItem(scheduleItem);
        }

        for (SchuleConfig.RedirectItem redirectItem : schuleConfig.getRedirectItems()) {
            addConfigRedirectItem(redirectItem);
        }

    }

    private void saveAllAndExit() {
        SchuleConfig schuleConfig = new SchuleConfig();
        schuleConfig.setEnabled(enableConfigCheckbox.isSelected());

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
        schuleConfig.writeToConfigFile();
        System.exit(0);
    }

}
