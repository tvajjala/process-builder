package demo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessDemo {
    public static void main(String[] args) {
        Map<String, String> env = new HashMap<>();
        env.put("x-sfaas-mdc-sf_customerTenancyName", "customerTenancyNameThiru");
        env.put("x-sfaas-mdc-wfInstanceId", "wfInstanceIdThiru");
        env.put("x-sfaas-mdc-sf_workRequestId", "WorkReqThiru");
        env.put("x-sfaas-mdc-sf_customerTenancyId", "customerTenancyIdThiruTesting");
        env.put("x-sfaas-mdc-CREATED_BY_PROCESS_ID", "CREATED_BY_PROCESS_IDThiruTesting");
        env.put("x-sfaas-mdc-CREATED_BY_PROCESS", "CREATED_BY_PROCESSThiruTesting");
        List<String> command = new ArrayList<>(6);
        command.add("/scratch/tools/mv-creator-tool/bin/mv-creator-tool.sh");
        command.add("start");
        command.add("-f");
        command.add("/scratch/tvajjala/OL8_upgrade_intg_test_20230911_004.json");
        System.out.println(command);
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        builder.environment().putAll(env);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); ByteArrayOutputStream errStream = new ByteArrayOutputStream()) {
            Process process = builder.start();
            new OutputThread(process.getErrorStream(), "ERR", errStream).start();
            new OutputThread(process.getErrorStream(), "OUT", outputStream).start();
            int exitCode = process.waitFor();
            String err = new String(errStream.toByteArray());
            String output = new String(outputStream.toByteArray());
            System.out.println(output);

            System.out.println(err);
            System.out.println("Exit " + exitCode);

        } catch (IOException | InterruptedException exception) {
            exception.printStackTrace();
        }

    }
}


class OutputThread extends Thread {

    private InputStream is;
    private OutputStream out;

    public OutputThread(InputStream is, String name, OutputStream out) {
        super(name);
        this.is = is;
        this.out = out;
    }

    public OutputThread(InputStream is, String name) {
        super(name);
        this.is = is;
    }

    public void run() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            BufferedWriter bw;

            if (out != null) {
                bw = new BufferedWriter(new OutputStreamWriter(out));
            } else {
                bw = new BufferedWriter(new OutputStreamWriter(System.out));
            }

            String input;
            while ((input = br.readLine()) != null) {
                bw.write(input);
                bw.newLine();
                bw.flush();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}