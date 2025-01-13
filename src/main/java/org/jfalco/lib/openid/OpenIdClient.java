package org.jfalco.lib.openid;

import java.net.http.HttpClient;
import java.util.Map;
import org.jfalco.lib.openid.exceptions.OpenIdConfigurationException;
import org.jfalco.lib.openid.exceptions.OpenIdException;

/** Main class to configure and interact with the OpenID 2.0 authentication flow. */
public class OpenIdClient {

  private final String providerUrl; // e.g. "https://www.google.com/accounts/o8/id"
  private final String
      returnToUrl; // Where the user is redirected after the provider completes auth
  private final String realm; // e.g. "https://yourapp.com"
  private final String discoveryEndpoint; // Optional, if different from providerUrl
  private final HttpClient httpClient; // Modern HttpClient

  private OpenIdClient(Builder builder) {
    this.providerUrl = builder.providerUrl;
    this.returnToUrl = builder.returnToUrl;
    this.realm = builder.realm;
    this.discoveryEndpoint = builder.discoveryEndpoint;

    // If the user provided a custom HttpClient, let's base our final client on that,
    // otherwise we start with a default HttpClient.newBuilder().
    // Then we explicitly enable redirect following.
    var httpClientBuilder =
        (builder.httpClient != null) ? builder.httpClient.newBuilder() : HttpClient.newBuilder();

    // We set the followRedirects policy to ALWAYS or NORMAL
    // (NORMAL = follow cross-domain redirects with GET after a POST, etc.)
    httpClientBuilder.followRedirects(HttpClient.Redirect.NORMAL);
    this.httpClient = httpClientBuilder.build();
  }

  /**
   * Create a new OpenID authentication request to initiate the login flow.
   *
   * @return an OpenIdAuthRequest
   * @throws OpenIdException if discovery or other config steps fail
   */
  public OpenIdAuthRequest createAuthRequest() throws OpenIdException {
    // Discover provider endpoints if needed (XRDS, Yadis)
    var resolvedEndpoint =
        discoveryEndpoint != null
            ? discoveryEndpoint
            : OpenIdDiscoveryService.discover(providerUrl, httpClient);

    if (resolvedEndpoint == null || resolvedEndpoint.isEmpty()) {
      throw new OpenIdConfigurationException(
          "Unable to discover the OpenID endpoint from providerUrl");
    }

    return new OpenIdAuthRequest(resolvedEndpoint, returnToUrl, realm);
  }

  /**
   * Validate the response coming back from the OpenID provider.
   *
   * @param incomingParams The request parameters from the HTTP callback (query/form params).
   * @return The claimed identifier (unique ID for the authenticated user), or null if invalid or
   *     canceled.
   * @throws OpenIdException if validation fails or the parameters are invalid
   */
  public String validateResponse(Map<String, String> incomingParams) throws OpenIdException {
    return OpenIdResponseValidator.validate(incomingParams, httpClient);
  }

  // ---------------------------------------
  // Builder pattern for easy configuration
  // ---------------------------------------
  public static class Builder {
    private String providerUrl;
    private String returnToUrl;
    private String realm;
    private String discoveryEndpoint;
    private HttpClient httpClient;

    public Builder providerUrl(String providerUrl) {
      this.providerUrl = providerUrl;
      return this;
    }

    public Builder returnToUrl(String returnToUrl) {
      this.returnToUrl = returnToUrl;
      return this;
    }

    public Builder realm(String realm) {
      this.realm = realm;
      return this;
    }

    public Builder discoveryEndpoint(String discoveryEndpoint) {
      this.discoveryEndpoint = discoveryEndpoint;
      return this;
    }

    /** Optional - pass in your own HttpClient (with custom timeouts, policies, etc.) */
    public Builder httpClient(HttpClient httpClient) {
      this.httpClient = httpClient;
      return this;
    }

    public OpenIdClient build() {
      if (providerUrl == null || returnToUrl == null || realm == null) {
        throw new IllegalArgumentException("providerUrl, returnToUrl, and realm are required.");
      }
      return new OpenIdClient(this);
    }
  }
}
