package de.bsommerfeld.antiac.detection;

import java.util.concurrent.CompletableFuture;

/**
 * Represents a generic check operation that processes an input of type T and produces a result
 * encapsulated in a {@link CheckResult} object of type R.
 *
 * @param <T> the type of the input to the check
 * @param <R> the type of the result object returned by the check
 */
public interface Check<T, R> {

  /**
   * Executes the check operation using the provided input and returns the result encapsulated in a
   * {@link CheckResult} object.
   *
   * @param input the input of type T to be processed by the check
   * @return a {@link CheckResult} object containing the result of the check operation
   */
  CheckResult<R> execute(T input);

  /**
   * Executes the check operation asynchronously using the provided input and returns a {@link
   * CompletableFuture} encapsulating the result.
   *
   * @param input the input of type T to be processed asynchronously by the check
   * @return a {@link CompletableFuture} containing a {@link CheckResult} object with the result of
   *     the check operation
   */
  default CompletableFuture<CheckResult<R>> executeAsync(T input) {
    return CompletableFuture.supplyAsync(() -> execute(input));
  }

  // TODO: #and() #or() methods represented by a CompositeCheck
}
