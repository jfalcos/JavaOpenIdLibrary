package org.jfalco.lib.openid.exceptions;

public class OpenIdException extends Exception {
  public OpenIdException() {
    super();
  }

  public OpenIdException(String message) {
    super(message);
  }

  public OpenIdException(String message, Throwable cause) {
    super(message, cause);
  }
}
