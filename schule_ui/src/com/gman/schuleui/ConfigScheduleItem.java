package com.gman.schuleui;

import com.gman.schule.common.SchuleConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.DayOfWeek;

public class ConfigScheduleItem extends JPanel {
    private JComboBox daysOfWeek;
    private JTextField timeFrom;
    private JTextField timeTo;
    private JButton deleteButton;
    private JLabel txtFrom;
    private JLabel txtTo;
    private JCheckBox enabledCheckBox;

    public ConfigScheduleItem() {
        this.add(enabledCheckBox);
        this.add(daysOfWeek);
        this.add(txtFrom);
        this.add(timeFrom);
        this.add(txtTo);
        this.add(timeTo);
        this.add(deleteButton);

        timeFrom.setColumns(5);
        timeTo.setColumns(5);

        fillDaysOfWeek();

        deleteButton.addActionListener(new ActionListener() {
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

    public ConfigScheduleItem(SchuleConfig.ScheduleItem scheduleItem) {
        this();
        if (scheduleItem != null) {

            DayOfWeek scheduleDayOfWeek = DayOfWeek.of(scheduleItem.getDayOfWeek());

            int comboBoxIdx = switch (scheduleDayOfWeek) {
                case MONDAY -> 1;
                case TUESDAY -> 2;
                case WEDNESDAY -> 3;
                case THURSDAY -> 4;
                case FRIDAY -> 5;
                case SATURDAY -> 6;
                case SUNDAY -> 7;
            };
            daysOfWeek.setSelectedIndex(comboBoxIdx);
            timeFrom.setText(scheduleItem.getTimeFrom());
            timeTo.setText(scheduleItem.getTimeTo());
            enabledCheckBox.setSelected(scheduleItem.isEnabled());
        }
    }

    public JComboBox getDaysOfWeek() {
        return daysOfWeek;
    }

    public JTextField getTimeFrom() {
        return timeFrom;
    }

    public JTextField getTimeTo() {
        return timeTo;
    }

    public JCheckBox getEnabledCheckBox() {
        return enabledCheckBox;
    }

    private void fillDaysOfWeek() {
        daysOfWeek.addItem("");
        daysOfWeek.addItem("Monday");
        daysOfWeek.addItem("Tuesday");
        daysOfWeek.addItem("Wednesday");
        daysOfWeek.addItem("Thursday");
        daysOfWeek.addItem("Friday");
        daysOfWeek.addItem("Saturday");
        daysOfWeek.addItem("Sunday");
    }

}
