package EventType;

import java.sql.Date;

public class SSHLogFailedMessage {
//    String MESSAGE;
//    String REALTIME_TIMESTAMP;
//    String SYSTEM;
//    String targetIP;
//    Long rawTimeStamp;
//    ArrayList<String> listOfFailedMessage;
//
//    public SSHLogFailedMessage(String JSON) throws JsonProcessingException {
//        ObjectMapper mapper = new ObjectMapper();
//        JsonNode StringToJSON = mapper.readTree(JSON);
//        this.SYSTEM = StringToJSON.get("_SYSTEMD_UNIT").asText();
//
//        Long getRealTime = Long.parseLong((StringToJSON.get("__REALTIME_TIMESTAMP").asText().substring(0,13)));
//        Date date = new Date(getRealTime);
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        this.rawTimeStamp = getRealTime;
//        String realTime = formatter.format(date);
//
//        this.REALTIME_TIMESTAMP = realTime;
//        this.MESSAGE = StringToJSON.get("MESSAGE").asText();
//
//        String failedIP;
//        ArrayList<String> failedMess = new ArrayList<>();
//
//        if (this.MESSAGE.contains("Failed") || this.MESSAGE.contains("Invalid")) {
//            failedIP = this.MESSAGE;
//            failedMess.add(this.MESSAGE);
//        } else {
//            failedIP = "OK";
//        }
//        this.listOfFailedMessage = failedMess;
//        this.targetIP = failedIP;
//
//    }
//    public SSHLogFailedMessage(){
//
//    }
    private String ipAddr;
    private int port;
    private String date;


    public SSHLogFailedMessage(String IpAddr, int port, String date) {
        this.ipAddr = IpAddr;
        this.port = port;
        this.date = date;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public int getPort() {
        return port;
    }

    public String getDate() {
        return date;
    }

    public void setIpAddr(String IpAddr) {
        this.ipAddr = IpAddr;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
