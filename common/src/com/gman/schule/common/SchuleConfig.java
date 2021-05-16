package com.gman.schule.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class SchuleConfig {

//    public static final Path configPath = Paths.get(System.getenv("APPDATA")+"\\Schule\\schule.json");
//    public static final Path configPath = Paths.get(System.getProperty("user.home")+"\\IdeaProjects\\Schule\\schule.json");
    private static final Gson gson = new GsonBuilder().create();

    private long id = System.currentTimeMillis();
    private boolean isEnabled = true;
    private Pause pause;

    private final List<ScheduleItem> scheduleItems = new ArrayList<>();
    private final List<RedirectItem> redirectItems = new ArrayList<>();

    public void addRedirectItem(RedirectItem item) {
        redirectItems.add(item);
    }

    public void removeRedirectItem(RedirectItem item) {
        redirectItems.remove(item);
    }

    public Pause getPause() {
        return pause;
    }

    public void setPause(Pause pause) {
        this.pause = pause;
    }

    public void addScheduleItem(ScheduleItem item) {
        scheduleItems.add(item);
    }

    public void removeScheduleItem(ScheduleItem item) {
        scheduleItems.remove(item);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public List<ScheduleItem> getScheduleItems() {
        return scheduleItems;
    }

    public List<RedirectItem> getRedirectItems() {
        return redirectItems;
    }


    public static Path getConfigPath(String user) {
        Path currentUserHome = Paths.get(System.getProperty("user.home"));
        Path appdata = Paths.get(System.getenv("APPDATA"));

        String specifiedUserHome = currentUserHome.getParent().toString() + System.getProperty("file.separator") + user;
        Path appDataSubpath = appdata.subpath(currentUserHome.getNameCount(), appdata.getNameCount());

        return Paths.get(specifiedUserHome, appDataSubpath.toString(), "Schule", "schule.json");
    }


    public boolean writeToConfigFile(String user) {
        Path configPath = getConfigPath(user);

        if (Files.exists(configPath)) {
            boolean configFileLock = getConfigFileLock(user);
            if (!configFileLock) {
                return false;
            }
        }
        String json = gson.toJson(this);
        try {
            if (!Files.exists(configPath)) {
                Files.createDirectories(configPath.getParent());
            }
            Files.write(configPath, json.getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static SchuleConfig loadFromConfigFile(String user) {
        Path configPath = getConfigPath(user);
        if (!Files.exists(configPath)) {
            return null;
        }
        boolean configFileLock = getConfigFileLock(user);
        if (!configFileLock) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();

        try (Stream<String> lines = Files.lines(configPath)) {
            lines.forEach(stringBuilder::append);
            String json = stringBuilder.toString();
            return gson.fromJson(json, SchuleConfig.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean getConfigFileLock(String user) {
        Path configPath = getConfigPath(user);
        if (!Files.exists(configPath)) {
            return true;
        }
        FileLock fileLock;
        try (RandomAccessFile raf = new RandomAccessFile(configPath.toFile(), "rw")) {
            while ((fileLock = raf.getChannel().tryLock()) == null) {
                Thread.sleep(250);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public static class ScheduleItem {
        private int dayOfWeek;
        private String specificDate;
        private String timeFrom;
        private String timeTo;
        private boolean isEnabled;

        public int getDayOfWeek() {
            return dayOfWeek;
        }

        public void setDayOfWeek(int dayOfWeek) {
            this.dayOfWeek = dayOfWeek;
        }

        public String getSpecificDate() {
            return specificDate;
        }

        public void setSpecificDate(String specificDate) {
            this.specificDate = specificDate;
        }

        public String getTimeFrom() {
            return timeFrom;
        }

        public void setTimeFrom(String timeFrom) {
            this.timeFrom = timeFrom;
        }

        public String getTimeTo() {
            return timeTo;
        }

        public void setTimeTo(String timeTo) {
            this.timeTo = timeTo;
        }

        public boolean isEnabled() {
            return isEnabled;
        }

        public void setEnabled(boolean enabled) {
            isEnabled = enabled;
        }

        public LocalTime parseTimeFrom() {
            return parseTime(timeFrom);
        }

        public LocalTime parseTimeTo() {
            return parseTime(timeTo);
        }

        private LocalTime parseTime(String time) {
            try {
                return LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    public static class RedirectItem {
        private String urlFrom;
        private String ipTo;
        private boolean isEnabled;

        public String getUrlFrom() {
            return urlFrom;
        }

        public void setUrlFrom(String urlFrom) {
            this.urlFrom = urlFrom;
        }

        public String getIpTo() {
            return ipTo;
        }

        public void setIpTo(String ipTo) {
            this.ipTo = ipTo;
        }

        public boolean isEnabled() {
            return isEnabled;
        }

        public void setEnabled(boolean enabled) {
            isEnabled = enabled;
        }
    }

    public static class UserItem {
        private String user;
        private boolean isEnabled;

        public UserItem(String user) {
            this.user = user;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }
    }

    public static class Pause {
        private long from;
        private long duration;

        public Pause(long from, long duration) {
            this.from = from;
            this.duration = duration;
        }

        public long getFrom() {
            return from;
        }

        public void setFrom(long from) {
            this.from = from;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }
    }
}
