package org.sheinbergon.corrosion.util;

import javax.annotation.Nonnull;

public class CorrosionException extends RuntimeException {
    public CorrosionException(final @Nonnull String message) {
        super(message);
    }
}
