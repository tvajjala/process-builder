package util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class AppUtil {

    public static void runProcess(Class<? extends Object> className) {
        String javaHome = System.getProperty("java.home");
        String suid = UUID.randomUUID().toString().substring(1, 8);
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");

        List<String> command = new ArrayList<>(6);
        command.add(javaBin);
        command.add("-Dsuid=" + suid);
        command.add("-cp");
        command.add(classpath);
        command.add(className.getName());
        command.add(UUID.randomUUID().toString());
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.environment().put("fvocid", "thiru");
        builder.environment().put("suid", suid);
        builder.redirectErrorStream(true);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Process process = builder.start();
            new OutputThread(process.getInputStream(), "ERR", outputStream).start();
            int exitCode = process.waitFor();
            String output = new String(outputStream.toByteArray());
            // log.info("Child process output: {}", output);
            //new OutputThread(process.getInputStream(), "ProcessStream", outputStream);

            // log.info("Exit code {}", exitCode);
            if (0 != exitCode) {
                log.error("ERROR {}", output);
                //throw new Exception(error);
            }

        } catch (IOException | InterruptedException exception) {
            log.error("Failed to log", exception);
            // throw new Exception(exception.getMessage(), exception);
        }
    }

    public static String parseStream(final InputStream inputStream) {

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
