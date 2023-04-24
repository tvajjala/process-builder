package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProcessBuilderDemo {

  /**
   * logger
   */
  static Logger log = LoggerFactory.getLogger(ProcessBuilderDemo.class);

  public static void main(String[] args) throws Exception {
    log.info("Running process");
    new ProcessBuilderDemo().runMultiProcess();
  }

  void runMultiProcess() {

    ExecutorService executorService = Executors.newFixedThreadPool(10);

    for (int i = 0; i < 10; i++) {
      executorService.submit(new ProcessRunner());
    }
    executorService.shutdown();
    //executorService.awaitTermination(5, TimeUnit.SECONDS);
  }

  class ProcessRunner implements Runnable {
    @Override
    public void run() {
      runProcess(Main.class);
    }
  }

  public static void runProcess(Class<? extends Object> className) {
    String javaHome = System.getProperty("java.home");
    String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
    String classpath = System.getProperty("java.class.path");

    List<String> command = new ArrayList<>(6);
    command.add(javaBin);
    command.add("-Dsuid=888888");
    command.add("-cp");
    command.add(classpath);
    command.add(className.getName());
    command.add(UUID.randomUUID().toString());
    ProcessBuilder builder = new ProcessBuilder(command);
    builder.redirectErrorStream(true);

    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      Process process = builder.start();
      new OutputThread(process.getInputStream(), "ERR", outputStream).start();
      int exitCode = process.waitFor();
      String output = new String(outputStream.toByteArray());
      log.info("Child process output: {}", output);
      //new OutputThread(process.getInputStream(), "ProcessStream", outputStream);

      log.info("Exit code {}", exitCode);
      if (0 != exitCode) {
        log.error("ERROR {}", output);
        //throw new Exception(error);
      }

    } catch (IOException | InterruptedException exception) {
      log.error("Failed to log", exception);
      // throw new Exception(exception.getMessage(), exception);
    }

  }

  static String parseStream(final InputStream inputStream) {

    final StringBuilder stringBuilder = new StringBuilder();

    try (final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        stringBuilder.append(line);
        stringBuilder.append("\n");
      }
      return stringBuilder.toString().trim();// to remove lead/tail new lines
    } catch (final IOException ioException) {
      throw new RuntimeException("Unable to read the stream", ioException);
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
        System.out.println("printing " + input);
        bw.write(input);
        bw.newLine();
        bw.flush();
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

}
