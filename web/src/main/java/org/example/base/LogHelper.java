package org.example.base;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class LogHelper {
    private String loggerName;
    private Logger logger;
    private AtomicInteger stackFrameLevel = new AtomicInteger(0);

    public LogHelper() {
        this(null);
    }
    public LogHelper(String loggerName) {
        this.loggerName = loggerName;
    }

    protected Logger getLogger() {
        if (StringUtils.isBlank(this.loggerName)) {
            StackTraceElement[] stack = (new Throwable()).getStackTrace();
            return LoggerFactory.getLogger(stack[this.stackFrameLevel.get() + 2].getClassName());
        } else {
            if (this.logger == null) {
                logger = LoggerFactory.getLogger(this.loggerName);
            }
            return logger;
        }
    }
    public String exceptionMsg(Throwable t, Object... msgs) {
        String message = StringUtils.join(msgs);
        this.getLogger().error(message, t);
        return message;
    }
}
