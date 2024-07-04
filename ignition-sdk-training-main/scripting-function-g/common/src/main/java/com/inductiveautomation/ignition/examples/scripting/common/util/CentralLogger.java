package com.inductiveautomation.ignition.examples.scripting.common.util;

import java.io.IOException;
import java.util.logging.*;


/**
 * This class is designed to be used in different layers of the program, i.e. to perform centralized logging
 * @author Mehdi Haidari
 * */
public class CentralLogger {

    private static final Object lock = new Object();
    private static final Logger _log = Logger.getLogger(CentralLogger.class.getSimpleName());

    static {
        try {
            // Disable default console handler
            Logger rootLogger = Logger.getLogger("");
            for (Handler handler : rootLogger.getHandlers()) {
                rootLogger.removeHandler(handler);
            }

            // Initialize console handler
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL);
            consoleHandler.setFormatter(new SimpleFormatter());
            rootLogger.addHandler(consoleHandler);

            // Initialize file handler
            FileHandler fileHandler = new FileHandler("./src/log/log.txt", false);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new SimpleFormatter());
            rootLogger.addHandler(fileHandler);

            // Set logger level to all
            rootLogger.setLevel(Level.ALL);
        } catch (IOException ex) {
            logInfo(ex.getMessage(), _log);
        }
    }


    // The main logging method
    public static void logInfo(String message, Logger logger) {
        synchronized (lock) {
            String callerClassName = logger.getName();
            String callerThreadName = Thread.currentThread().getName();
            logger.logp(Level.INFO, callerClassName, null, "[" + callerThreadName + "] " + message);
        }
    }
}
