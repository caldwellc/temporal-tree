package org.temporaltree;

import java.time.Instant;

public interface DateAccessor<T> {
    Instant getDateTime(T record);
}
