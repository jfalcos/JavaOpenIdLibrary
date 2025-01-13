package org.jfalco.lib.openid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.jfalco.lib.openid.exceptions.OpenIdException;
import org.junit.jupiter.api.Test;

public class OpenIdDiscoveryServiceTests {

  @Test
  void testDiscoverSuccess() throws Exception {
    var mockClient = mock(HttpClient.class);
    var mockResponse = mock(HttpResponse.class);

    when(mockResponse.statusCode()).thenReturn(200);
    when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    var result = OpenIdDiscoveryService.discover("https://example.com/openid", mockClient);
    assertEquals("https://example.com/openid", result);
  }

  @Test
  void testDiscoverNonHttp() {
    var mockClient = mock(HttpClient.class);
    assertThrows(
        OpenIdException.class, () -> OpenIdDiscoveryService.discover("invalid-url", mockClient));
  }

  @Test
  void testDiscoverHttpError() throws Exception {
    var mockClient = mock(HttpClient.class);
    var mockResponse = mock(HttpResponse.class);

    when(mockResponse.statusCode()).thenReturn(404);
    when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    assertThrows(
        OpenIdException.class,
        () -> OpenIdDiscoveryService.discover("https://example.com/openid", mockClient));
  }

  @Test
  void testDiscoverIOException() throws Exception {
    var mockClient = mock(HttpClient.class);

    when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenThrow(new java.io.IOException("Network error"));

    assertThrows(
        OpenIdException.class,
        () -> OpenIdDiscoveryService.discover("https://example.com/openid", mockClient));
  }
}
