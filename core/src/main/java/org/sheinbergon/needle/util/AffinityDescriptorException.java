package org.sheinbergon.needle.util;

import javax.annotation.Nonnull;

public class AffinityDescriptorException extends RuntimeException {
    /**
     * Instantiate an {@code AffinityDescriptorException} using the specified error message.
     *
     * @param message the error message.
     */
    public AffinityDescriptorException(final @Nonnull String message) {
        super(message);
    }
}
