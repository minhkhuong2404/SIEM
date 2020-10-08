package EventType;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SSHAlert {
    private String alertMessage;

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public SSHAlert(String IpAddress, int port, String date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        String dateStr = formatter.format(date);
        this.alertMessage = "IP Address: " + IpAddress + " has some suspicious action at port " + port + " on " + date;
    }
}
