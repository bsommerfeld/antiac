package de.bsommerfeld.antiac.click;

import java.util.Collection;

/**
 * Represents an interface for calculating or tracking the clicks per second (CPS) in the context of
 * a system that processes click events.
 *
 * <p>This interface may provide methods for measuring or evaluating the rate of clicks over a
 * specific period of time, typically used in environments where user interactions such as clicks
 * need to be monitored or analyzed.
 */
public interface Cps {

  /**
   * Adds a click event to the system for processing or tracking.
   *
   * @param click the click event to be added, containing metadata such as timestamp and click type
   */
  void add(Click click);

  /**
   * Retrieves the most recent click event that has been added to the system.
   *
   * @return the last added {@code Click} instance, or {@code null} if no click events have been
   *     recorded.
   */
  Click last();

  /**
   * Converts the current state or value of the CPS (clicks per second) into an integer
   * representation.
   *
   * @return the integer value representing the CPS, typically calculated or measured based on the
   *     clicks recorded in the system.
   */
  int asInt();

  /**
   * Checks whether the system or collection of click events is currently empty.
   *
   * @return {@code true} if no click events have been recorded or tracked; {@code false} otherwise.
   */
  boolean empty();

  /**
   * Retrieves all click events that have been recorded or processed by the system.
   *
   * @return a collection of {@code Click} instances representing all recorded click events. The
   *     collection is typically ordered based on the sequence in which the clicks were added.
   */
  Collection<Click> all();
}
