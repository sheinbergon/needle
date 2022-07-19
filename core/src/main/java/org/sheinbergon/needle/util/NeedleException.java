package org.sheinbergon.needle.util;

import javax.annotation.Nonnull;

public class NeedleException extends RuntimeException {
  /**
   * Instantiate an {@link NeedleException} using the specified error message.
   *
   * @param message the error message.
   */
  public NeedleException(final @Nonnull String message) {
    super(message);
  }

  /**
   * Instantiate an {@link NeedleException} using the given exception.
   *
   * @param x the exception
   */
  public NeedleException(final @Nonnull Exception x) {
    super(x);
  }
}
