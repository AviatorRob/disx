package xyz.ar06.disx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ar06.disx.config.DisxConfigHandler;

public class DisxLogger {
    private static final Logger logger = LoggerFactory.getLogger("disx");

    private static String getCallerPrefix(){
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement caller = stackTrace[3];
        String callerFileName = caller.getFileName();
        String callerPrefix = "[" + callerFileName.substring(0, callerFileName.length() - 5) + "] ";
        return callerPrefix;
    }
    public static void debug(Object input){
        if (DisxModInfo.isDEBUG()) {
            logger.info(getCallerPrefix() + String.valueOf(input));
        }
    }

    public static void error(Object input){
        logger.error(getCallerPrefix() + String.valueOf(input));
    }

    public static void info(Object input){
        logger.info(getCallerPrefix() + String.valueOf(input));
    }

    public static void warn(Object input){
        logger.warn(getCallerPrefix() + String.valueOf(input));
    }

}
