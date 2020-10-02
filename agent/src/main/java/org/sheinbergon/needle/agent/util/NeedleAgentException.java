package org.sheinbergon.needle.agent.util;

import org.sheinbergon.needle.util.NeedleException;

import javax.annotation.Nonnull;

public final class NeedleAgentException extends NeedleException {

    /**
     * Instantiate an {@link NeedleAgentException} using the given message.
     *
     * @param message the error message.
     */
    public NeedleAgentException(final @Nonnull String message) {
        super(message);
    }

    /**
     * Instantiate an {@link NeedleAgentException} using the given exception.
     *
     * @param x the exception
     */
    public NeedleAgentException(final @Nonnull Exception x) {
        super(x);
    }
}
