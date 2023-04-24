package ocisdk;

import com.oracle.bmc.auth.AbstractAuthenticationDetailsProvider;
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import com.oracle.bmc.model.BmcException;
import com.oracle.bmc.retrier.DefaultRetryCondition;
import com.oracle.bmc.retrier.RetryConfiguration;
import com.oracle.bmc.secrets.SecretsClient;
import com.oracle.bmc.secrets.requests.GetSecretBundleRequest;
import com.oracle.bmc.secrets.responses.GetSecretBundleResponse;
import com.oracle.bmc.waiter.DelayStrategy;
import com.oracle.bmc.waiter.ExponentialBackoffDelayStrategyWithJitter;
import com.oracle.bmc.waiter.MaxAttemptsTerminationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * javac -cp lib/oci-java-sdk-common-2.11.1.jar:lib/oci-java-sdk-core-2.11.1.jar:lib/oci-java-sdk-secrets-2.11.1.jar:lib/slf4j-api-1.7.25.jar -d . RetryableDemo.java
 */
public class RetryableDemo {

  Logger log = LoggerFactory.getLogger(RetryableDemo.class);

  void validateSecretId(String name, String value) {
    try {
      GetSecretBundleResponse secretBundleResponse = getSecretsClient()
          .getSecretBundle(getSecretBundleRequest(value));

      log.info("Successfully verified : {}", value);

      if ((null == secretBundleResponse) || (null == secretBundleResponse.getSecretBundle())) {
        log.info("Unable to verify {}", value);
        throw new RuntimeException("Unable to verify " + name + "=" + value);
      }
    } catch (BmcException bmcException) {
      int statusCode = bmcException.getStatusCode();
      log.error("Unable to verify {}", value, bmcException.getMessage());

      throw new RuntimeException(
          "Unable to verify " + name + "=" + value + "[statusCode=" + statusCode + "]", bmcException);
    }
  }

  GetSecretBundleRequest getSecretBundleRequest(String secretId) {
    return GetSecretBundleRequest.builder()
                                 .secretId(secretId)
                                 .retryConfiguration(retryConfiguration())
                                 .build();

  }

  private RetryConfiguration retryConfiguration() {
    return RetryConfiguration.builder()
                             .terminationStrategy(new MaxAttemptsTerminationStrategy(5))
                             .delayStrategy(delayStrategyWithJitter())
                             .retryCondition(exception -> new CustomRetryCondition().shouldBeRetried(exception))
                             .build();
  }

  private DelayStrategy delayStrategyWithJitter() {
    return new ExponentialBackoffDelayStrategyWithJitter(600000);
  }

  public SecretsClient getSecretsClient() {
    return SecretsClient.builder().build(authenticationDetailsProvider());
  }

  private AbstractAuthenticationDetailsProvider authenticationDetailsProvider() {
    return InstancePrincipalsAuthenticationDetailsProvider
        .builder()
        .build();
  }

  public static void main(String[] args) {
    new RetryableDemo().validateSecretId("testKey", args[0]);
  }

  class CustomRetryCondition extends DefaultRetryCondition {

    @Override
    public boolean shouldBeRetried(BmcException exception) {
      log.info("[CustomRetryCondition] StatusCode: {}, ServiceCode: {}",
               exception.getStatusCode(),
               exception.getServiceCode());
      boolean flag = super.shouldBeRetried(exception);

      log.info("DefaultRetryCondition  ?  {}", flag);

      return true;
    }
  }
}
