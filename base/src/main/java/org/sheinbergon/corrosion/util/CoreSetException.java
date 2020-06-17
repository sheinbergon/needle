package org.sheinbergon.corrosion.util;

import javax.annotation.Nonnull;

public class CoreSetException extends RuntimeException {
    public CoreSetException(final @Nonnull String message) {
        super(message);
    }
}
