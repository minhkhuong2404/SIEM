package EventType;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class SSHLogMessage {
    private String MESSAGE;
    private String realTimeStamp;

    public String getMESSAGE() {
        return MESSAGE;
    }

    public String getrealTimeStamp() {
        return realTimeStamp;
    }

    public void setMESSAGE(String message) {
        this.MESSAGE = message;
    }

    public void setrealTimeStamp(String realTimeStamp) {
        this.realTimeStamp = realTimeStamp;
    }

    @Override
    public String toString(){
        long epochTimestamp = Long.parseLong(this.realTimeStamp.substring(0, 13));

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        Date date = Date.from(Instant.ofEpochMilli(epochTimestamp));
        String realTime = formatter.format(date);
        return "On " + realTime + ": " + MESSAGE;
    }

    public SSHLogMessage(String message, String realTimeStamp) {
        this.MESSAGE = message;
        this.realTimeStamp = realTimeStamp;
    }
}