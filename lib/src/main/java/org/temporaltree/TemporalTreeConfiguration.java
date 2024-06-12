package org.temporaltree;

import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

/**
 * Configuration object for breaking data into a temporal tree using a top-down
 * approach
 */
public record TemporalTreeConfiguration(ChronoUnit timeUnit, ChronoField timeField, DateKeyGenerator keyGenerator,
        DateTitleGenerator titleGenerator, Integer breakLimit, TemporalTreeConfiguration breakConfiguration) {
}
