import com.espertech.esper.common.client.*;
import com.espertech.esper.common.client.configuration.*;
import com.espertech.esper.compiler.client.*;
import com.espertech.esper.runtime.client.*;
import com.fasterxml.jackson.databind.*;

import java.io.*;
import java.text.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import EventType.*;

public class Main {
    public static int count = 0;
    private final static int MAX_FAILED = 3;
    Long[] start = {0L};
    Long[] end = {0L};
    private EPCompiled epCompiledLog;
    private EPCompiled epCompiledLogFail;

    Configuration configuration = new Configuration();

    public static void main(String[] args) throws IOException, EPCompileException {

        Main setup = new Main();
        setup.Compiler();
        EPRuntime runtime = setup.Runtime();
//        statementFailed.addListener( (newData, oldData, newStatement, newRuntime) -> {
//            String System_unit = (String) newData[0].get("SYSTEM");
//            String Realtime_timestamp = (String) newData[0].get("REALTIME_TIMESTAMP");
//            String targetIP = (String) newData[0].get("targetIP");
//            String Message = (String) newData[0].get("MESSAGE");
//            Long rawTimeStamp = (Long) newData[0].get("rawTimeStamp");
//            ArrayList<String> listOfFailedMessage = (ArrayList<String>) newData[0].get("listOfFailedMessage");
//
//            String output = String.format("In %s, at %s, checked connection: %s", System_unit, Realtime_timestamp, targetIP);
//            if (targetIP.contains("Failed") || targetIP.contains("Invalid")) {
//                System.out.println(output);
//            }
//            System.out.println(listOfFailedMessage);
//            String[] getIPandPort  = output.split(" ");
//            String getIP;
//            String getPort = "";
//            String IPADDRESS_PATTERN =
//                    "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
//
//            Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
//            Matcher matcher = pattern.matcher(output);
//            if (matcher.find()) {
//                getIP = matcher.group();
//            } else{
//                getIP = "0.0.0.0";
//            }
//
//            if (output.contains("Invalid")){
//                getPort =  getIPandPort[getIPandPort.length - 1];
//            } else if (output.contains("Failed")) {
//                getPort =  getIPandPort[getIPandPort.length - 2];
//            }
//
//            if (output.contains("Failed") || output.contains("Invalid")){
//                count[0] += 1;
//                if (count[0] == 1)
//                    start[0] = rawTimeStamp;
//            } else {
//                count[0] = 0;
//            }
//
//            if (count[0] >= 3){
//                end[0] = rawTimeStamp;
//                if (end[0] - start[0] < 20000)
//                    System.out.println("IP " + getIP + " has some suspicious action at port " + getPort);
//            }
//        });

        ProcessBuilder pb = new ProcessBuilder("bash", "-c", "journalctl -u ssh.service -o json");
        pb.redirectErrorStream(true);
        try {
            Process process = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = br.readLine()) != null) {
//            SSHLogFailedMessage failedLogMessage = new SSHLogFailedMessage(line);
//            runtimeFailed.getEventService().sendEventBean(failedLogMessage, "SSHFailedMessage");
                ObjectMapper mapper = new ObjectMapper();
                JsonNode StringToJSON = mapper.readTree(line);

                String Message = StringToJSON.get("MESSAGE").asText();
                String RealTimestamp = StringToJSON.get("__REALTIME_TIMESTAMP").asText();

                runtime.getEventService().sendEventBean(new SSHLogMessage(Message, RealTimestamp), "SSHLogMessage");
            }
        } catch (IOException e){
            System.err.println("Cannot execute bash command.");
            e.printStackTrace();
        }

    }

    public void Compiler() throws EPCompileException {
        configuration.getCommon().addEventType(SSHLogMessage.class);
        CompilerArguments Values = new CompilerArguments(configuration);
        EPCompiler EPCompiler = EPCompilerProvider.getCompiler();
        try {
            epCompiledLog = EPCompiler.compile("@name('ssh-log-message') select MESSAGE, realTimeStamp from SSHLogMessage", Values);
        } catch (EPCompileException ex) {
            // handle exception here
            throw new RuntimeException(ex);
        }

        configuration.getCommon().addEventType(SSHLogFailedMessage.class);
        CompilerArguments failedValues = new CompilerArguments(configuration);
        EPCompiler EPCompilerFailed = EPCompilerProvider.getCompiler();
        try {
            epCompiledLogFail = EPCompilerFailed.compile("@name('ssh-log-failed-message') select ipAddr, port, date from SSHLogFailedMessage", failedValues);
        } catch (EPCompileException ex) {
            // handle exception here
            throw new RuntimeException(ex);
        }

    }

    public EPRuntime Runtime() {
        EPRuntime runtime = EPRuntimeProvider.getDefaultRuntime(configuration);
        EPDeployment FailedLog;
        EPDeployment Alert;

        try {
            FailedLog = runtime.getDeploymentService().deploy(epCompiledLog);
            Alert = runtime.getDeploymentService().deploy(epCompiledLogFail);
        }
        catch (EPDeployException ex) {
            // handle exception here
            throw new RuntimeException(ex);
        }

        EPStatement sendFail = runtime.getDeploymentService().getStatement(FailedLog.getDeploymentId(),
                "ssh-log-message");
        sendFail.addListener((newData, oldData, stmt, rt) -> {
            String message = (String) newData[0].get("MESSAGE");
            String Realtime_timestamp = (String) newData[0].get("realTimeStamp");

            long getRealTime = Long.parseLong((Realtime_timestamp.substring(0,13)));
//            Date date = Date.from(Instant.ofEpochMilli(getRealTime));

            Date date = new Date(getRealTime);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String StringDate = formatter.format(date);

            String output = String.format("At %s, SSH failed connection: %s", StringDate, message);
            if (message.contains("Failed") || message.contains("Invalid")) {
                System.out.println(output);
            }

            String[] getIPandPort  = output.split(" ");
            String getIP;
            String getPort = "";
            String IPADDRESS_PATTERN =
                    "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

            Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
            Matcher matcher = pattern.matcher(output);

            if (matcher.find()) {
                getIP = matcher.group();
            } else{
                getIP = "0.0.0.0";
            }

            if (output.contains("Invalid")){
                getPort = getIPandPort[getIPandPort.length - 1];
                count++;

            } else if (output.contains("Failed")) {
                getPort = getIPandPort[getIPandPort.length - 2];
                count++;

            } else {
                count = 0;
            }

            if (count == 1){
                start[0] = Long.parseLong(Realtime_timestamp);
            } else if (count >= 3) {
                end[0] = Long.parseLong(Realtime_timestamp);
                if (end[0] - start[0] <= 60000) {
                    System.out.println("Warning! Too many failed login attempts ");
                }
            }

            rt.getEventService().sendEventBean(new SSHLogFailedMessage(getIP, Integer.parseInt(getPort), StringDate), "SSHLogFailedMessage");
        });

        EPStatement sendAlert = runtime.getDeploymentService().getStatement(Alert.getDeploymentId(),
                "ssh-log-failed-message");
        sendAlert.addListener((newData, oldData, stmt, rt) -> {
            String senderIpAddr = (String) newData[0].get("ipAddr");
            int port = (int) newData[0].get("port");
            String date = (String) newData[0].get("date");

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
//            String dateStr = sdf.format(date);

            // Send event SSHAlert
            if (count >= MAX_FAILED ) {

                System.out.println("IP Address: " + senderIpAddr + " has some suspicious action at port " + port + " on " + date);
                rt.getEventService().sendEventBean(new SSHAlert(senderIpAddr, port, date),
                        "SSHAlert");
            }
        });
        return runtime;
    }
}