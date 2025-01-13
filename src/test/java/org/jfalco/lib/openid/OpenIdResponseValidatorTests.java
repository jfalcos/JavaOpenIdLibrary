package org.jfalco.lib.openid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

import org.jfalco.lib.openid.exceptions.OpenIdException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class OpenIdResponseValidatorTests {

  @Test
  void testValidateSuccess() throws Exception {
    var params = new HashMap<String, String>();
    params.put("openid.mode", "id_res");
    params.put("openid.claimed_id", "https://user.example.com/");
    params.put("openid.op_endpoint", "https://op.example.com/endpoint");
    params.put("openid.sig", "signature");

    var mockClient = mock(HttpClient.class);
    var mockResponse = mock(HttpResponse.class);

    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn("ns:http://specs.openid.net/auth/2.0\nis_valid:true");

    // Capture the HttpRequest to inspect it
    ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);

    when(mockClient.send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);

    var result = OpenIdResponseValidator.validate(params, mockClient);
    assertEquals("https://user.example.com/", result);

    var sentRequest = requestCaptor.getValue();
    assertEquals("POST", sentRequest.method());
    // Check the body or headers as needed
  }

  @Test
  void testValidateUserCanceled() throws OpenIdException {
    var params = new HashMap<String, String>();
    params.put("openid.mode", "cancel");

    // We don’t need an HttpClient mock for canceled
    var result = OpenIdResponseValidator.validate(params, mock(HttpClient.class));
    assertNull(result);
  }

  @Test
  void testValidateInvalidMode() {
    var params = new HashMap<String, String>();
    params.put("openid.mode", "unknown");

    assertThrows(OpenIdException.class, () ->
            OpenIdResponseValidator.validate(params, mock(HttpClient.class))
    );
  }

  @Test
  void testValidateMissingParams() {
    var params = new HashMap<String, String>();
    // openid.mode missing
    assertThrows(OpenIdException.class, () ->
            OpenIdResponseValidator.validate(params, mock(HttpClient.class))
    );
  }

  @Test
  void testValidateNoOpEndpoint() {
    var params = new HashMap<String, String>();
    params.put("openid.mode", "id_res");

    assertThrows(OpenIdException.class, () ->
            OpenIdResponseValidator.validate(params, mock(HttpClient.class))
    );
  }

  @Test
  void testValidateNon200Response() throws Exception {
    var params = new HashMap<String, String>();
    params.put("openid.mode", "id_res");
    params.put("openid.claimed_id", "https://user.example.com/");
    params.put("openid.op_endpoint", "https://op.example.com/endpoint");

    var mockClient = mock(HttpClient.class);
    var mockResponse = mock(HttpResponse.class);

    when(mockResponse.statusCode()).thenReturn(400);
    when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);

    assertThrows(OpenIdException.class, () ->
            OpenIdResponseValidator.validate(params, mockClient)
    );
  }

  @Test
  void testValidateInvalidSignature() throws Exception {
    var params = new HashMap<String, String>();
    params.put("openid.mode", "id_res");
    params.put("openid.claimed_id", "https://user.example.com/");
    params.put("openid.op_endpoint", "https://op.example.com/endpoint");

    var mockClient = mock(HttpClient.class);
    var mockResponse = mock(HttpResponse.class);

    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn("ns:http://specs.openid.net/auth/2.0\nis_valid:false");

    when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);

    assertThrows(OpenIdException.class, () ->
            OpenIdResponseValidator.validate(params, mockClient)
    );
  }
}