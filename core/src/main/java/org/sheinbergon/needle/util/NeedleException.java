package org.sheinbergon.needle.util;

import javax.annotation.Nonnull;

public class NeedleException extends RuntimeException {
    public NeedleException(final @Nonnull String message) {
        super(message);
    }
}
