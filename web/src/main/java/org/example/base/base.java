package org.example.base;

public class base extends LogMessage{
    protected LogData logData;

    public LogData getLogData() {
        return logData;
    }

    public void setLogData(LogData logData) {
        this.logData = logData;
    }

    public base(){
        this.logData=new LogData();
    }
}
