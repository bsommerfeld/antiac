package de.bsommerfeld.antiac.click;

/**
 * Represents a click event with associated metadata including
 * a timestamp and the type of the click.
 */
public interface Click {

    /**
     * Returns the timestamp of the click event in milliseconds.
     *
     * @return the timestamp as a long value representing milliseconds
     *         since the epoch.
     */
    long timestamp();

    /**
     * Retrieves the type of the click event.
     *
     * @return the click type as an instance of {@code ClickType}.
     */
    ClickType clickType();
}
