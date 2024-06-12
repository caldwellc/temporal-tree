package org.temporaltree;

import java.time.ZonedDateTime;

/**
 * Abstract class for generating a key from a zoned date time
 */
public abstract class DateKeyGenerator {
    public abstract String generateKey(ZonedDateTime zonedDate);
}
