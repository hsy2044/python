package org.example.base;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang3.StringUtils;

@XStreamAlias("LogData")
public class LogData {

    @XStreamAlias("ProgramName")
    private String programName = StringUtils.EMPTY;

    @XStreamAlias("Message")
    private String message = StringUtils.EMPTY;

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
