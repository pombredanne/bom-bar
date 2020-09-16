package com.philips.research.collector.core.domain.licenses;

import java.util.Arrays;
import java.util.Optional;

/**
 * Value that is guarded by the ordinal values of an enum.
 * The value is exposed if the enum is at least its guard value.
 *
 * @param <T> Type of the value held by the condition
 */
public class Conditional<T> {
    private final T value;
    private final Enum<?>[] guards;

    /**
     * Creates a guarded value.
     *
     * @param guard minimal enum for the value to be exposed
     */
    public Conditional(T value, Enum<?>... guard) {
        this.value = value;
        this.guards = guard;
    }

    /**
     * @return the value, irrespective of the guard
     */
    public T get() {
        return value;
    }

    /**
     * @return the value if the minimal ordinal value of all guards are met by a provided condition
     */
    public Optional<T> get(Enum<?>... conditions) {
        final var matches = Arrays.stream(guards)
                .allMatch(g -> Arrays.stream(conditions)
                        .anyMatch(c -> g.getClass().isInstance(c) && c.ordinal() >= g.ordinal()));
        return matches ? Optional.of(value) : Optional.empty();
    }
}