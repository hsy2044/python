package org.example.base;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.slf4j.event.Level;

public class LogMessage {
    private static final LogHelper log = new LogHelper("logger");

    protected void logMessage(LogData data) {
        logMessage(Level.INFO, data);
    }

    public static void logMessage(Level level, LogData aaLog) {
        String message = StringUtils.join(aaLog);
        setLogContextField(aaLog);
        switch (level) {
            case INFO:
                log.getLogger().info(message);
                break;
            case ERROR:
                log.getLogger().error(message);
                break;
            case DEBUG:
                log.getLogger().debug(message);
                break;
            case WARN:
                log.getLogger().warn(message);
                break;
            case TRACE:
                log.getLogger().trace(message);
                break;
        }
        aaLog.setMessage(StringUtils.EMPTY);
        aaLog.setProgramName(StringUtils.EMPTY);
    }

    private static void setLogContextField(LogData aaLog) {
        MDC.put("ProgramName",aaLog.getProgramName());
        MDC.put("message",aaLog.getMessage());
    }
}