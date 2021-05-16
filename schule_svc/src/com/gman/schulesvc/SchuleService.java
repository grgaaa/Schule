package com.gman.schulesvc;

import com.gman.schule.common.Log;
import com.gman.schule.common.SchuleConfig;
import com.gman.schule.common.Utils;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SchuleService {

    private static final Path hostFile = Paths.get(System.getenv("WINDIR")+"\\System32\\drivers\\etc\\hosts");
    private static final Path hostBak = Paths.get(System.getenv("WINDIR")+"\\System32\\drivers\\etc\\bak_hosts");
//    private static final Path hostFile = Paths.get(System.getProperty("user.home")+"\\IdeaProjects\\Schule\\hosts");
//    private static final Path hostBak = Paths.get(System.getProperty("user.home")+"\\IdeaProjects\\Schule\\bak_hosts");

    private static final Log LOG = new Log("SchuleService");
    private static final Object LOCK = new Object();

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final List<ScheduledFuture> scheduledTasks = new ArrayList<>();
    private SchuleConfig schuleConfig;

    private volatile boolean isActive = false;

    private static final SchuleService instance = new SchuleService();

    public static void main(String[] args) {
        try {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    LOG.error(e);
                    System.exit(0);
                }
            });
            // restore default before applying in case the restore task wasn't able to execute (shutdown before timeTo)
            instance.restoreHost();

            instance.executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        instance.loadConfiguration(Utils.getCurrentSystemUser());
                    } catch (Exception e) {
                        e.printStackTrace();
                        LOG.error(e);
                    }
                }
            },0,30, TimeUnit.SECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void loadConfiguration(String user) {
        SchuleConfig newSchuleConfig = SchuleConfig.loadFromConfigFile(user);

        if (schuleConfig != null && newSchuleConfig != null
                && Objects.equals(schuleConfig.getId(), newSchuleConfig.getId())) {
            return;
        }

        if (newSchuleConfig == null && !scheduledTasks.isEmpty()) {
             cancelSchule();
             restoreHost();
        } else {
            if (newSchuleConfig.isEnabled()) {
                cancelSchule();
                if (isActive) { restoreHost(); }
                applySchuleConfig(newSchuleConfig);
            } else {
                if (!scheduledTasks.isEmpty()) {
                    cancelSchule();
                    restoreHost();
                }
            }
        }
        schuleConfig = newSchuleConfig;
    }

    private void applySchuleConfig(SchuleConfig schuleConfig) {

        List<SchuleConfig.ScheduleItem> scheduleItems = schuleConfig.getScheduleItems();

        if (scheduleItems == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        SchuleConfig.Pause pause = schuleConfig.getPause();
        if (pause != null) {
            Date pauseFrom = new Date(pause.getFrom());
            Date pauseTo = new Date(pause.getFrom() + pause.getDuration());

            Date now1 = new Date();
            if (now1.after(pauseFrom) && now1.before(pauseTo) && isActive) {
                LOG.info(String.format("pausing for %s minutes", TimeUnit.MILLISECONDS.toMinutes(pause.getDuration())));

                restoreHost();
                ScheduledFuture<?> applyConfigFuture = executorService.schedule(() -> applySchuleConfig(schuleConfig), 1000 + now1.toInstant().until(pauseTo.toInstant(), ChronoUnit.MILLIS), TimeUnit.MILLISECONDS);
                addScheduledTask(applyConfigFuture);
                return;
            }
        }

        for (SchuleConfig.ScheduleItem scheduleItem : scheduleItems) {
            if (!scheduleItem.isEnabled()) {
                continue;
            }
            int activeDay = scheduleItem.getDayOfWeek();

            LocalTime timeFrom = scheduleItem.parseTimeFrom();
            LocalTime timeTo = scheduleItem.parseTimeTo();

            LocalDateTime dateTime = LocalDateTime.now()
                    .withHour(timeFrom.getHour())
                    .withMinute(timeFrom.getMinute())
                    .withSecond(0);

            TemporalAdjuster adjuster = TemporalAdjusters.nextOrSame(DayOfWeek.of(activeDay));
            LocalDateTime scheduledDateTime = dateTime.with(adjuster);

            // if the schule alarm should be fired now
            if (scheduledDateTime.toLocalDate().equals(now.toLocalDate())) {
                if (nowIsTimeBetween(timeFrom, timeTo)) {
                    LOG.info("schedule is now");
                    // apply host rules
                    executorService.execute(new HijackHostFile(schuleConfig));
                    // schedule host restore
                    ScheduledFuture<?> hostRestoreFuture = executorService.schedule(new RestoreHostFile(), now.toLocalTime().until(timeTo, ChronoUnit.MILLIS), TimeUnit.MILLISECONDS);
                    addScheduledTask(hostRestoreFuture);

                    TemporalAdjuster nextWeek = TemporalAdjusters.next(DayOfWeek.of(activeDay));
                    scheduledDateTime = scheduledDateTime.with(nextWeek);

                } else {
                    if (now.toLocalTime().isAfter(timeTo)) {
                        TemporalAdjuster nextWeek = TemporalAdjusters.next(DayOfWeek.of(activeDay));
                        scheduledDateTime = scheduledDateTime.with(nextWeek);
                    }
                }
            }
            LOG.info("hijack time: "+scheduledDateTime);
            // schedule host hijack
            ScheduledFuture<?> hijackFuture = executorService.scheduleAtFixedRate(new HijackHostFile(schuleConfig), now.until(scheduledDateTime, ChronoUnit.MILLIS), TimeUnit.DAYS.toMillis(7), TimeUnit.MILLISECONDS);
            addScheduledTask(hijackFuture);

            // schedule host restore
            LocalDateTime scheduledRestoreTime = scheduledDateTime
                    .withHour(timeTo.getHour())
                    .withMinute(timeTo.getMinute())
                    .withSecond(0);

            LOG.info("restore time: "+scheduledRestoreTime);

            ScheduledFuture<?> restoreFuture = executorService.scheduleAtFixedRate(new RestoreHostFile(), now.until(scheduledRestoreTime, ChronoUnit.MILLIS), TimeUnit.DAYS.toMillis(7), TimeUnit.MILLISECONDS);
            addScheduledTask(restoreFuture);
        }
    }

    private void addScheduledTask(ScheduledFuture<?> future) {
        synchronized (LOCK) {
            scheduledTasks.add(future);
        }
    }

    private boolean nowIsTimeBetween(LocalTime timeFrom, LocalTime timeTo) {
        return LocalTime.now().isAfter(timeFrom) && LocalTime.now().isBefore(timeTo);
    }

    private void cancelSchule() {
        synchronized (LOCK) {
            if (!scheduledTasks.isEmpty()) {
                LOG.info("canceling schule tasks");
                for (Iterator<ScheduledFuture> it = scheduledTasks.iterator(); it.hasNext(); ) {
                    ScheduledFuture next = it.next();
                    if (!next.isDone()) {
                        next.cancel(true);
                    }
                    it.remove();
                }
            }
        }
    }

    private void restoreHost() {
        LOG.info("executing");
        executorService.execute(new RestoreHostFile());
    }


    private void showTrayNotification(boolean activated) {
        SystemTray tray = SystemTray.getSystemTray();

        //If the icon is a file
//        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
        //Alternative (if the icon is on the classpath):
        Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/res/icon.png"));

        TrayIcon trayIcon = new TrayIcon(image, "Schule");
        //Let the system resize the image if needed
        trayIcon.setImageAutoSize(true);
        //Set tooltip text for the tray icon
        trayIcon.setToolTip(String.format("Shule is %s.", (activated ? "ON" : "OFF")));
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }

        trayIcon.displayMessage("Ja kk je pa de? :)", String.format("Shule is %s.", (activated ? "ACTIVE" : "OFF")), TrayIcon.MessageType.INFO);
    }

    private static class HijackHostFile implements Runnable {

        private SchuleConfig schuleConfig;

        public HijackHostFile(SchuleConfig schuleConfig) {
            this.schuleConfig = schuleConfig;
        }

        @Override
        public void run() {
            LOG.info("hijacking host file");
            try {
                // create backup copy if it doesn't exist
                if (!Files.exists(hostBak, LinkOption.NOFOLLOW_LINKS)) {
                    Files.copy(hostFile, hostBak, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                }

                List<String> collect = schuleConfig.getRedirectItems().stream()
                        .filter(SchuleConfig.RedirectItem::isEnabled)
                        .map(item -> item.getIpTo()+"    "+item.getUrlFrom()).collect(Collectors.toList());

                Files.write(hostFile, collect, StandardCharsets.US_ASCII);

                instance.showTrayNotification(true);
                instance.isActive = true;

            } catch (Exception e) {
                LOG.error(e);
                e.printStackTrace();
            }
        }
    }

    private static class RestoreHostFile implements Runnable {

        @Override
        public void run() {
            LOG.info("restoring host file");
            try {
                if (Files.exists(hostBak)) {
                    Files.copy(hostBak, hostFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                }
                instance.showTrayNotification(false);
                instance.isActive = false;

            } catch (Exception e) {
                LOG.error(e);
                e.printStackTrace();
            }
        }
    }
}
