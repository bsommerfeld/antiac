package de.bsommerfeld.antiac.detection;

/**
 * Represents the result of a check operation, encapsulating whether the check was successful and a
 * result object of the specified type.
 *
 * @param <R> the type of the result object returned by the check
 */
public interface CheckResult<R> {

  /**
   * Indicates whether the check operation was successful.
   *
   * @return true if the check was successful, false otherwise
   */
  boolean successful();

  /**
   * Retrieves the result object of the check operation.
   *
   * @return the result object of type R, or null if no result is available
   */
  R result();
}
