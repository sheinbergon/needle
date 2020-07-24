package org.sheinbergon.needle.util;

import javax.annotation.Nonnull;

public class NeedleException extends RuntimeException {
    /**
     * Instantiate an {@code AffinityDescriptorException} using the specified error message.
     *
     * @param message the error message.
     */
    public NeedleException(final @Nonnull String message) {
        super(message);
    }
}
