package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class OutputThread extends Thread {

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
                // System.out.println("printing " + input);
                bw.write(input);
                bw.newLine();
                bw.flush();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
