package org.temporaltree;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * DateBinner utility class that will generate bins for a given time range and
 * then provide a function to place records into those bnins
 * 
 * @param <T>
 */
public class DateBinner<T> {
	private final List<TemporalBin<T>> bins;

	/**
	 * Constructor that generates bins
	 * 
	 * @param timeUnit
	 * @param timeField
	 * @param startDatetimeUtc
	 * @param endDatetimeUtc
	 * @param zoneId
	 * @return
	 */
	public DateBinner(ChronoUnit timeUnit, ChronoField timeField, Instant startDatetimeUtc, Instant endDatetimeUtc,
			ZoneId zoneId) {
		bins = new ArrayList<>();
		ZonedDateTime startDate = ZonedDateTime.ofInstant(startDatetimeUtc, zoneId);
		ZonedDateTime endDate = ZonedDateTime.ofInstant(endDatetimeUtc, zoneId);

		ZonedDateTime currentTime = startDate.truncatedTo(timeUnit);

		long endTime = endDate.toInstant().toEpochMilli();

		while (currentTime.toInstant().toEpochMilli() <= endTime) {
			ZonedDateTime nextTime = getNext(timeUnit, timeField, currentTime);
			bins.add(new TemporalBin<T>(
					timeUnit,
					currentTime,
					currentTime.getLong(timeField),
					currentTime.toInstant().toEpochMilli(),
					nextTime.toInstant().toEpochMilli()));
			currentTime = nextTime;
		}
	};

	/**
	 * Bins temporal records
	 * 
	 * @param records
	 * @param dateAccessor
	 * @return
	 */
	public void placeRecords(List<T> records, DateAccessor<T> dateAccessor) {
		int maxBinIdx = bins.size() - 1;
		int minBinIdx = 0;
		for (T record : records) {
			long datetimeUtc = dateAccessor.getDateTime(record).toEpochMilli();

			int binIdx = minBinIdx;
			TemporalBin<T> bin = bins.get(binIdx);

			// move the minimum bin forward until we find the bin with a datetimeutc greater
			// than the records (as the records are in order)
			while (datetimeUtc >= bin.endUtcMs() && binIdx < maxBinIdx) {
				binIdx++;
				minBinIdx++;
				bin = bins.get(binIdx);
			}

			if (datetimeUtc >= bin.startUtcMs() && datetimeUtc < bin.endUtcMs()) {
				bin.records().add(record);
			}
		}
	}

	public List<TemporalBin<T>> getBins() {
		return bins;
	}

	/**
	 * Returns the start of the next time boundary
	 * 
	 * @param {string}   dateTimeKey (options include: year, month or day)
	 * @param {DateTime} zonedDatetime
	 * @returns {DateTime} zonedDatetime at the start of the next year, month, or
	 *          day
	 */
	public static ZonedDateTime getNext(ChronoUnit timeUnit, ChronoField timeField, ZonedDateTime zonedDatetime) {
		ZonedDateTime nextDate = null;
		if (timeUnit == ChronoUnit.HOURS) {
			nextDate = getNextHour(zonedDatetime);
		} else if (timeUnit == ChronoUnit.MINUTES) {
			nextDate = getNextMinute(zonedDatetime);
		} else if (timeUnit == ChronoUnit.SECONDS) {
			nextDate = getNextSecond(zonedDatetime);
		} else if (timeUnit == ChronoUnit.MILLIS) {
			nextDate = getNextMillisecond(zonedDatetime);
		} else if (timeUnit == ChronoUnit.DAYS) {
			ZonedDateTime initial = zonedDatetime.truncatedTo(timeUnit);
			ZoneOffset initialOffset = initial.getOffset();
			ZonedDateTime next = initial;
			ZoneOffset nextOffset = next.getOffset();
			while (next.getLong(timeField) == initial.getLong(timeField) && nextOffset.equals(initialOffset)) {
				next = next.plus(1, timeUnit);
				nextOffset = next.getOffset();
			}
			nextDate = next;
		} else if (timeUnit == ChronoUnit.MONTHS) {
			ZonedDateTime initial = zonedDatetime.with(TemporalAdjusters.firstDayOfMonth())
					.truncatedTo(ChronoUnit.DAYS);
			ZoneOffset initialOffset = initial.getOffset();
			ZonedDateTime next = initial;
			ZoneOffset nextOffset = next.getOffset();
			while (next.get(timeField) == initial.get(timeField) && nextOffset.equals(initialOffset)) {
				next = next.plus(1, timeUnit);
				nextOffset = next.getOffset();
			}
			nextDate = next;
		} else if (timeUnit == ChronoUnit.YEARS) {
			ZonedDateTime initial = zonedDatetime.with(TemporalAdjusters.firstDayOfYear()).truncatedTo(ChronoUnit.DAYS);
			ZoneOffset initialOffset = initial.getOffset();
			ZonedDateTime next = initial;
			ZoneOffset nextOffset = next.getOffset();
			while (next.get(timeField) == initial.get(timeField) && nextOffset.equals(initialOffset)) {
				next = next.plus(1, timeUnit);
				nextOffset = next.getOffset();
			}
			nextDate = next;
		}
		return nextDate;
	};

	/**
	 * Returns the start of the next year for a ZoneDateTime
	 * 
	 * @param {DateTime} zonedDatetime
	 * @returns {DateTime} zonedDatetime at the start of the next year
	 */
	public static ZonedDateTime getNextYear(ZonedDateTime zonedDatetime) {
		return getNext(ChronoUnit.YEARS, ChronoField.YEAR, zonedDatetime);
	};

	/**
	 * Returns the start of the next month for a ZoneDateTime
	 * 
	 * @param {DateTime} zonedDatetime
	 * @returns {DateTime} zonedDatetime at the start of the next month
	 */
	public static ZonedDateTime getNextMonth(ZonedDateTime zonedDatetime) {
		return getNext(ChronoUnit.MONTHS, ChronoField.MONTH_OF_YEAR, zonedDatetime);
	};

	/**
	 * Returns the start of the next day for a ZoneDateTime
	 * 
	 * @param {DateTime} zonedDatetime
	 * @returns {DateTime} zonedDatetime at the start of the next day
	 */
	public static ZonedDateTime getNextDay(ZonedDateTime zonedDatetime) {
		return getNext(ChronoUnit.DAYS, ChronoField.EPOCH_DAY, zonedDatetime);
	};

	/**
	 * Returns the start of the next hour for a ZoneDateTime
	 * 
	 * @param {DateTime} zonedDatetime
	 * @returns {DateTime} zonedDatetime at the start of the next hour
	 */
	public static ZonedDateTime getNextHour(ZonedDateTime zonedDatetime) {
		int initialHour = zonedDatetime.getHour();
		ZoneOffset initialOffset = zonedDatetime.getOffset();
		ZonedDateTime nextHour = zonedDatetime;
		ZoneOffset nextOffset = nextHour.getOffset();
		while (nextHour.getHour() == initialHour && nextOffset.equals(initialOffset)) {
			nextHour = nextHour.plus(15, ChronoUnit.MINUTES);
			nextOffset = nextHour.getOffset();
		}
		return nextHour;
	};

	/**
	 * Returns the start of the next minute for a ZonedDateTime
	 * 
	 * @param {DateTime} zonedDatetime
	 * @returns {DateTime} zonedDatetime at the start of the next hour
	 */
	public static ZonedDateTime getNextMinute(ZonedDateTime zonedDatetime) {
		ZonedDateTime nextMinute = zonedDatetime.truncatedTo(ChronoUnit.MINUTES);
		int initialMinute = zonedDatetime.getMinute();
		ZoneOffset initialOffset = zonedDatetime.getOffset();
		ZoneOffset nextOffset = nextMinute.getOffset();
		while (nextMinute.getMinute() == initialMinute && nextOffset.equals(initialOffset)) {
			nextMinute = nextMinute.plus(1, ChronoUnit.MINUTES);
			nextOffset = nextMinute.getOffset();
		}
		return nextMinute;
	};

	/**
	 * Returns the start of the next second for a ZoneDateTime
	 * 
	 * @param {DateTime} zonedDatetime
	 * @returns {DateTime} zonedDatetime at the start of the next second
	 */
	public static ZonedDateTime getNextSecond(ZonedDateTime zonedDatetime) {
		ZonedDateTime nextSecond = zonedDatetime.truncatedTo(ChronoUnit.SECONDS);
		int initialSecond = zonedDatetime.getSecond();
		ZoneOffset initialOffset = zonedDatetime.getOffset();
		ZoneOffset nextOffset = nextSecond.getOffset();
		while (nextSecond.getSecond() == initialSecond && nextOffset.equals(initialOffset)) {
			nextSecond = nextSecond.plus(1, ChronoUnit.SECONDS);
			nextOffset = nextSecond.getOffset();
		}
		return nextSecond;
	};

	/**
	 * Returns the start of the next millisecond for a ZoneDateTime
	 * 
	 * @param {DateTime} zonedDatetime
	 * @returns {DateTime} zonedDatetime at the start of the next millisecond
	 */
	public static ZonedDateTime getNextMillisecond(ZonedDateTime zonedDatetime) {
		ZonedDateTime nextMillisecond = zonedDatetime;
		int initialMillisecond = zonedDatetime.get(ChronoField.MILLI_OF_SECOND);
		ZoneOffset initialOffset = zonedDatetime.getOffset();
		ZoneOffset nextOffset = nextMillisecond.getOffset();
		while (nextMillisecond.get(ChronoField.MILLI_OF_SECOND) == initialMillisecond
				&& nextOffset.equals(initialOffset)) {
			nextMillisecond = nextMillisecond.plus(1, ChronoUnit.MILLIS);
			nextOffset = nextMillisecond.getOffset();
		}
		return nextMillisecond;
	};

}
