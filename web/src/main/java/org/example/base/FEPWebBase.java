package org.example.base;

import org.slf4j.event.Level;

public class FEPWebBase  {
    private void logMessage(Level level, Object... msgs) {
        this.logMessage(level, null, msgs);
    }
    protected void infoMessage(Object... msgs) {
        this.logMessage(Level.INFO, msgs);
    }
}
