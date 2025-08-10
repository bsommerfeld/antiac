package de.bsommerfeld.antiac.detection;

import java.util.Objects;

/**
 * Simple immutable implementation of CheckResult.
 */
public final class SimpleCheckResult<R> implements CheckResult<R> {
    private final boolean successful;
    private final R result;

    public SimpleCheckResult(boolean successful, R result) {
        this.successful = successful;
        this.result = result;
    }

    @Override
    public boolean successful() {
        return successful;
    }

    @Override
    public R result() {
        return result;
    }

    @Override
    public String toString() {
        return "SimpleCheckResult{" +
                "successful=" + successful +
                ", result=" + Objects.toString(result) +
                '}';
    }
}
