package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.Closeable;
import java.io.IOException;

import static org.apache.http.impl.client.HttpClientBuilder.create;

@Slf4j
public class WebApiExecutor implements Closeable {

  public static final int CLIENT_CONNECTION_TIMEOUT = 60000;
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private final CloseableHttpClient httpClient;

  public WebApiExecutor() {
    final RequestConfig requestConfig =
        RequestConfig.custom()
            .setConnectionRequestTimeout(CLIENT_CONNECTION_TIMEOUT)
            .setConnectTimeout(CLIENT_CONNECTION_TIMEOUT)
            .setSocketTimeout(CLIENT_CONNECTION_TIMEOUT)
            .build();
    this.httpClient = create().setDefaultRequestConfig(requestConfig).build();
  }

  public <T> T execute(final HttpUriRequest request, final Class<T> responseType) {
    try (final CloseableHttpResponse response = httpClient.execute(request)) {
      final int status = response.getStatusLine().getStatusCode();
      final HttpEntity entity = response.getEntity();
      final String body = entity != null ? EntityUtils.toString(entity) : null;
      if (status >= 200 && status < 300) {
        return OBJECT_MAPPER.readValue(body, responseType);
      } else {
        throw new RuntimeException("Request failed: " + status + " - " + body);
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {
    try {
      httpClient.close();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }

  }
}
