package com.gman.schule.common;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log extends Logger {

    private FileHandler fileHandler;
    private SimpleFormatter formatter;

    public Log(String name) {
        super(name, null);

        try {
            fileHandler = new FileHandler("schule.log", true);
            formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);

            addHandler(fileHandler);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void error(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        info("E\t"+pw);
    }

}
