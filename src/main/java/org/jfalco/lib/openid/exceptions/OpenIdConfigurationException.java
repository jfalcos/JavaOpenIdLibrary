package org.jfalco.lib.openid.exceptions;

public class OpenIdConfigurationException extends OpenIdException {
  public OpenIdConfigurationException(String message) {
    super(message);
  }

  public OpenIdConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }
}
