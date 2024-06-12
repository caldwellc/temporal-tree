package org.temporaltree;

import java.time.ZonedDateTime;

/**
 * Abstract class to provide a mechanism for converting a zoned date time into a
 * title
 */
public abstract class DateTitleGenerator {
    public abstract String generateTitle(ZonedDateTime zonedDate);
}
