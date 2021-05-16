package com.gman.schule.common;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static String getCurrentSystemUser() {
        return System.getProperty("user.name");
    }

    public static List<String> getSystemUsers() {
        Path usersPath = Paths.get(System.getProperty("user.home")).getParent();

        List<String> systemUsers = new ArrayList<>();

        for (File file : usersPath.toFile().listFiles()) {
            if (file.isDirectory() && !file.isHidden() && !file.getName().equals("Public")) {
                systemUsers.add(file.getName());
            }
        }
        return systemUsers;
    }
}
