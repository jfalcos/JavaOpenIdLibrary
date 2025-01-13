package org.jfalco.lib.openid;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.jfalco.lib.openid.exceptions.OpenIdException;

/** Responsible for validating the OpenID 2.0 response using "check_authentication". */
public class OpenIdResponseValidator {

  public static String validate(Map<String, String> params, HttpClient httpClient)
      throws OpenIdException {
    if (!params.containsKey("openid.mode")) {
      throw new OpenIdException("Invalid OpenID response: missing openid.mode");
    }
    var mode = params.get("openid.mode");
    if ("cancel".equals(mode)) {
      // The user canceled the login
      return null;
    }
    if (!"id_res".equals(mode)) {
      throw new OpenIdException("Invalid openid.mode: " + mode);
    }

    // The user’s claimed ID
    var claimedId = params.get("openid.claimed_id");
    // The OpenID Provider endpoint
    var providerEndpoint = params.get("openid.op_endpoint");
    if (providerEndpoint == null) {
      throw new OpenIdException("No openid.op_endpoint found in the response");
    }

    // Build the check_authentication request body
    var requestBodyBuilder = new StringBuilder();
    for (var entry : params.entrySet()) {
      if (entry.getKey().startsWith("openid.")) {
        requestBodyBuilder
            .append(entry.getKey())
            .append("=")
            .append(urlEncode(entry.getValue()))
            .append("&");
      }
    }
    // Overwrite openid.mode to check_authentication
    requestBodyBuilder.append("openid.mode=check_authentication");

    var requestBody = requestBodyBuilder.toString();
    var httpRequest =
        HttpRequest.newBuilder()
            .uri(URI.create(providerEndpoint))
            .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .build();

    try {
      var response =
          httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
      if (response.statusCode() != 200) {
        throw new OpenIdException(
            "Provider responded with non-200 status: " + response.statusCode());
      }

      // The response is typically key-value pairs like:
      // ns:http://specs.openid.net/auth/2.0
      // is_valid:true
      var responseBody = response.body();
      if (responseBody.contains("is_valid:true")) {
        return claimedId;
      } else {
        throw new OpenIdException("OpenID validation failed: " + responseBody);
      }
    } catch (IOException | InterruptedException e) {
      Thread.currentThread().interrupt(); // best practice if InterruptedException
      throw new OpenIdException("Error verifying OpenID response", e);
    }
  }

  private static String urlEncode(String value) {
    // Minimal URL encoding for x-www-form-urlencoded
    return value.replace(" ", "%20").replace("&", "%26").replace("=", "%3D");
  }
}
