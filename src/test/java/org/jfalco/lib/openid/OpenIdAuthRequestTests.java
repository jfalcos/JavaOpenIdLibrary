package org.jfalco.lib.openid;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jfalco.lib.openid.exceptions.OpenIdException;
import org.junit.jupiter.api.Test;

public class OpenIdAuthRequestTests {

  @Test
  void testGetAuthenticationUrl() throws OpenIdException {
    var request =
        new OpenIdAuthRequest(
            "https://provider.com/openid", "https://myapp.com/callback", "https://myapp.com");

    var url = request.getAuthenticationUrl();
    assertTrue(url.startsWith("https://provider.com/openid?"));
    assertTrue(url.contains("openid.ns=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0"));
    assertTrue(url.contains("openid.mode=checkid_setup"));
    assertTrue(url.contains("openid.return_to=https%3A%2F%2Fmyapp.com%2Fcallback"));
    assertTrue(url.contains("openid.realm=https%3A%2F%2Fmyapp.com"));
    assertTrue(
        url.contains(
            "openid.claimed_id=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0%2Fidentifier_select"));
    assertTrue(
        url.contains(
            "openid.identity=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0%2Fidentifier_select"));
  }

  @Test
  void testGetAuthenticationUrlWhenAlreadyHasQueryParams() throws OpenIdException {
    var request =
        new OpenIdAuthRequest(
            "https://provider.com/openid?foo=bar",
            "https://myapp.com/callback",
            "https://myapp.com");

    var url = request.getAuthenticationUrl();
    // Since endpoint already had "?foo=bar", library should append "&"
    assertTrue(url.startsWith("https://provider.com/openid?foo=bar&"));
    assertTrue(url.contains("openid.mode=checkid_setup"));
  }
}
