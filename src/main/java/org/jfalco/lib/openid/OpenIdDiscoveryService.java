package org.jfalco.lib.openid;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.jfalco.lib.openid.exceptions.OpenIdConfigurationException;
import org.jfalco.lib.openid.exceptions.OpenIdException;

/**
 * Very simplified "discovery" logic for demonstration purposes. In production, consider a robust
 * Yadis/XRDS parser.
 */
public class OpenIdDiscoveryService {

  /**
   * Attempts to discover the OpenID endpoint by doing a HEAD request.
   *
   * @param openIdIdentifier the user-supplied or known OpenID URL
   * @param httpClient the HttpClient to use
   * @return the same URL if reachable (2xx or 3xx), otherwise throws
   */
  public static String discover(String openIdIdentifier, HttpClient httpClient)
      throws OpenIdException {
    if (!openIdIdentifier.startsWith("http")) {
      throw new OpenIdConfigurationException(
          "OpenID identifier must be a valid URL (must start with http)");
    }

    var request =
        HttpRequest.newBuilder()
            .uri(URI.create(openIdIdentifier))
            // HEAD method in HttpClient: pass "noBody" + method override
            .method("HEAD", HttpRequest.BodyPublishers.noBody())
            .build();

    try {
      var response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
      var statusCode = response.statusCode();
      if (statusCode >= 200 && statusCode < 400) {
        return openIdIdentifier;
      } else {
        throw new OpenIdException("Unable to reach OpenID identifier. HTTP code: " + statusCode);
      }
    } catch (IOException | InterruptedException e) {
      Thread.currentThread().interrupt(); // recommended if InterruptedException
      throw new OpenIdException("Error during discovery", e);
    }
  }
}
