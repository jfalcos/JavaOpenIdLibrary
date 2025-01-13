package org.jfalco.lib.openid;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.jfalco.lib.openid.exceptions.OpenIdException;

/** Represents an OpenID 2.0 authentication request. */
public class OpenIdAuthRequest {

  private final String openIdEndpoint;
  private final String returnToUrl;
  private final String realm;

  public OpenIdAuthRequest(String openIdEndpoint, String returnToUrl, String realm) {
    this.openIdEndpoint = openIdEndpoint;
    this.returnToUrl = returnToUrl;
    this.realm = realm;
  }

  /** Generates the full redirect URL with all required OpenID 2.0 parameters. */
  public String getAuthenticationUrl() throws OpenIdException {
    try {
      var sb = new StringBuilder(openIdEndpoint);
      // If endpoint already has a '?' (query params), append '&', otherwise '?'
      if (!openIdEndpoint.contains("?")) {
        sb.append("?");
      } else {
        sb.append("&");
      }

      sb.append("openid.ns=").append(encode("http://specs.openid.net/auth/2.0"));
      sb.append("&openid.mode=").append("checkid_setup");
      sb.append("&openid.return_to=").append(encode(returnToUrl));
      sb.append("&openid.realm=").append(encode(realm));
      sb.append("&openid.claimed_id=")
          .append(encode("http://specs.openid.net/auth/2.0/identifier_select"));
      sb.append("&openid.identity=")
          .append(encode("http://specs.openid.net/auth/2.0/identifier_select"));

      return sb.toString();
    } catch (UnsupportedEncodingException e) {
      throw new OpenIdException("Error building authentication URL", e);
    }
  }

  private String encode(String value) throws UnsupportedEncodingException {
    // Modern usage with StandardCharsets
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
