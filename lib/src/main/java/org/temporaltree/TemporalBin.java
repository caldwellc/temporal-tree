package org.temporaltree;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * A TemporalBin is a bin that represents a zoned time range and contains the
 * utc epoch millisecond start/end values for that range.
 * Additionally the bin contains a values array for the data to be placed
 */
public record TemporalBin<T>(ChronoUnit timeUnit, ZonedDateTime date, long value, long startUtcMs, long endUtcMs, List<T> records) {
    public TemporalBin(ChronoUnit timeUnit, ZonedDateTime date, long value, long startUtcMs, long endUtcMs) {
        this(timeUnit, date, value, startUtcMs, endUtcMs, new ArrayList<T>());
    }

    public void addRecord(T record) {
        this.records.add(record);
    }
}
