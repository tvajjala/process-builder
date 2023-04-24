package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class DemoTest {
  static {
    final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    ctx.reconfigure();
  }

  static Logger log = LoggerFactory.getLogger(DemoTest.class);

  public static void main(String[] args) {

    String sessionId = "655dd22e27d";
    String suid = sessionId.length() >= 12 ? sessionId.substring(sessionId.length() - 12) : "-";
    log.info(suid);

    System.setProperty("sid", "8838888");
    MDC.put("sid", "8838888");

    log.info("Logs per session");
  }
}
