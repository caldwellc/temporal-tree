package org.temporaltree;

import org.junit.Test;

import org.temporaltree.DateAccessor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DateBinnerTest {

	private static final long pacChadDSTStartMs = 1712411100000L; // 3:30 - 3:45 -> 2:45 shift
	private static final long pacChadDSTEndMs = 1727531100000L; // 2:30 - 2:45 -> 3:45 shift
	private static final long usEastDTStartMs = 1710053100000L; // 1:45 - 2:00 -> 3:00 shift
	private static final long usEastDSTEndMs = 1730612700000L; // 1:45 - 2:00 -> 1:00 shift

	@Test
	public void testGetNextYear() {
		ZoneId id = ZoneId.of("Z");
		ZonedDateTime zonedDatetime = ZonedDateTime.now(id);
		ZonedDateTime nextYear = DateBinner.getNextYear(zonedDatetime);
		assertEquals(zonedDatetime.getYear() + 1, nextYear.getYear());
	}

	@Test
	public void testGetNextMonth() {
		ZoneId id = ZoneId.of("Z");
		ZonedDateTime now = ZonedDateTime.now(id);
		ZonedDateTime nextMonth = DateBinner.getNextMonth(now);
		if (now.getMonthValue() == 12) {
			assertEquals(1, nextMonth.getMonthValue());
		} else {
			assertEquals(now.getMonthValue() + 1, nextMonth.getMonthValue());
		}
	}

	@Test
	public void testGetNextDay() {
		ZoneId id = ZoneId.of("Z");
		ZonedDateTime now = ZonedDateTime.now(id);
		ZonedDateTime nextDay = DateBinner.getNextDay(now);
		assertEquals(now.getLong(ChronoField.EPOCH_DAY) + 1, nextDay.getLong(ChronoField.EPOCH_DAY));
	}

	@Test
	public void testGetNextHour() {
		ZoneId id = ZoneId.of("Pacific/Chatham");
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(pacChadDSTStartMs), id);
		ZonedDateTime nextDay = DateBinner.getNext(ChronoUnit.HOURS, ChronoField.HOUR_OF_DAY, zonedDateTime);
		assertEquals(zonedDateTime.getHour() - 1, nextDay.getHour());

		id = ZoneId.of("Pacific/Chatham");
        zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(pacChadDSTEndMs), id);
		nextDay = DateBinner.getNext(ChronoUnit.HOURS, ChronoField.HOUR_OF_DAY, zonedDateTime);
		assertEquals(zonedDateTime.getHour() + 1, nextDay.getHour());

		id = ZoneId.of("US/Eastern");
        zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(usEastDTStartMs), id);
		nextDay = DateBinner.getNext(ChronoUnit.HOURS, ChronoField.HOUR_OF_DAY, zonedDateTime);
		assertEquals(zonedDateTime.getHour() + 2, nextDay.getHour());

		id = ZoneId.of("US/Eastern");
        zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(usEastDSTEndMs), id);
		nextDay = DateBinner.getNext(ChronoUnit.HOURS, ChronoField.HOUR_OF_DAY, zonedDateTime);
		assertEquals(zonedDateTime.getHour(), nextDay.getHour());
	}

	@Test
	public void testGetNextMinute() {
		long offsetInMs = 840000L; // 14 minutes to add to initial time, as it is 15min away from shift
		ZoneId id = ZoneId.of("Pacific/Chatham");
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(pacChadDSTStartMs + offsetInMs), id);
		ZonedDateTime nextDay = DateBinner.getNext(ChronoUnit.MINUTES, ChronoField.MINUTE_OF_HOUR, zonedDateTime);
		assertEquals(zonedDateTime.getHour() - 1, nextDay.getHour());
		assertEquals((zonedDateTime.getMinute() + 1) % 60, nextDay.getMinute());

		id = ZoneId.of("Pacific/Chatham");
        zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(pacChadDSTEndMs + offsetInMs), id);
		nextDay = DateBinner.getNext(ChronoUnit.MINUTES, ChronoField.MINUTE_OF_HOUR, zonedDateTime);
		assertEquals(zonedDateTime.getHour() + 1, nextDay.getHour());
		assertEquals((zonedDateTime.getMinute() + 1) % 60, nextDay.getMinute());

		id = ZoneId.of("US/Eastern");
        zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(usEastDTStartMs + offsetInMs), id);
		nextDay = DateBinner.getNext(ChronoUnit.MINUTES, ChronoField.MINUTE_OF_HOUR, zonedDateTime);
		assertEquals(zonedDateTime.getHour() + 2, nextDay.getHour());
		assertEquals((zonedDateTime.getMinute() + 1) % 60, nextDay.getMinute());

		id = ZoneId.of("US/Eastern");
        zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(usEastDSTEndMs + offsetInMs), id);
		nextDay = DateBinner.getNext(ChronoUnit.MINUTES, ChronoField.MINUTE_OF_HOUR, zonedDateTime);
		assertEquals(zonedDateTime.getHour(), nextDay.getHour());
		assertEquals((zonedDateTime.getMinute() + 1) % 60, nextDay.getMinute());
	}

	@Test
	public void testGetNextSecond() {
		long offsetInMs = 899000L; // 14 min 59 seconds to add to initial time, as it is 15min away from shift
		ZoneId id = ZoneId.of("Pacific/Chatham");
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(pacChadDSTStartMs + offsetInMs), id);
		ZonedDateTime nextDay = DateBinner.getNext(ChronoUnit.SECONDS, ChronoField.SECOND_OF_DAY, zonedDateTime);
		assertEquals(zonedDateTime.getHour() - 1, nextDay.getHour());
		assertEquals((zonedDateTime.getMinute() + 1) % 60, nextDay.getMinute());
		assertEquals((zonedDateTime.getSecond() + 1) % 60, nextDay.getSecond());

		id = ZoneId.of("Pacific/Chatham");
        zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(pacChadDSTEndMs + offsetInMs), id);
		nextDay = DateBinner.getNext(ChronoUnit.SECONDS, ChronoField.SECOND_OF_DAY, zonedDateTime);
		assertEquals(zonedDateTime.getHour() + 1, nextDay.getHour());
		assertEquals((zonedDateTime.getMinute() + 1) % 60, nextDay.getMinute());
		assertEquals((zonedDateTime.getSecond() + 1) % 60, nextDay.getSecond());

		id = ZoneId.of("US/Eastern");
        zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(usEastDTStartMs + offsetInMs), id);
		nextDay = DateBinner.getNext(ChronoUnit.SECONDS, ChronoField.SECOND_OF_DAY, zonedDateTime);
		assertEquals(zonedDateTime.getHour() + 2, nextDay.getHour());
		assertEquals((zonedDateTime.getMinute() + 1) % 60, nextDay.getMinute());
		assertEquals((zonedDateTime.getSecond() + 1) % 60, nextDay.getSecond());

		id = ZoneId.of("US/Eastern");
        zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(usEastDSTEndMs + offsetInMs), id);
		nextDay = DateBinner.getNext(ChronoUnit.SECONDS, ChronoField.SECOND_OF_DAY, zonedDateTime);
		assertEquals(zonedDateTime.getHour(), nextDay.getHour());
		assertEquals((zonedDateTime.getMinute() + 1) % 60, nextDay.getMinute());
		assertEquals((zonedDateTime.getSecond() + 1) % 60, nextDay.getSecond());
	}

	@Test
	public void testGetNextMillisecond() {
		long offsetInMs = 899999L; // 14 min 59 seconds to add to initial time, as it is 15min away from shift
		ZoneId id = ZoneId.of("Pacific/Chatham");
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(pacChadDSTStartMs + offsetInMs), id);
		ZonedDateTime nextDay = DateBinner.getNext(ChronoUnit.MILLIS, ChronoField.MILLI_OF_SECOND, zonedDateTime);
		assertEquals(zonedDateTime.getHour() - 1, nextDay.getHour());
		assertEquals((zonedDateTime.getMinute() + 1) % 60, nextDay.getMinute());
		assertEquals((zonedDateTime.getSecond() + 1) % 60, nextDay.getSecond());
		assertEquals((zonedDateTime.get(ChronoField.MILLI_OF_SECOND) + 1) % 1000, nextDay.get(ChronoField.MILLI_OF_SECOND));

		id = ZoneId.of("Pacific/Chatham");
        zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(pacChadDSTEndMs + offsetInMs), id);
		nextDay = DateBinner.getNext(ChronoUnit.MILLIS, ChronoField.MILLI_OF_SECOND, zonedDateTime);
		assertEquals(zonedDateTime.getHour() + 1, nextDay.getHour());
		assertEquals((zonedDateTime.getMinute() + 1) % 60, nextDay.getMinute());
		assertEquals((zonedDateTime.getSecond() + 1) % 60, nextDay.getSecond());
		assertEquals((zonedDateTime.get(ChronoField.MILLI_OF_SECOND) + 1) % 1000, nextDay.get(ChronoField.MILLI_OF_SECOND));

		id = ZoneId.of("US/Eastern");
        zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(usEastDTStartMs + offsetInMs), id);
		nextDay = DateBinner.getNext(ChronoUnit.MILLIS, ChronoField.MILLI_OF_SECOND, zonedDateTime);
		assertEquals(zonedDateTime.getHour() + 2, nextDay.getHour());
		assertEquals((zonedDateTime.getMinute() + 1) % 60, nextDay.getMinute());
		assertEquals((zonedDateTime.getSecond() + 1) % 60, nextDay.getSecond());
		assertEquals((zonedDateTime.get(ChronoField.MILLI_OF_SECOND) + 1) % 1000, nextDay.get(ChronoField.MILLI_OF_SECOND));

		id = ZoneId.of("US/Eastern");
        zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(usEastDSTEndMs + offsetInMs), id);
		nextDay = DateBinner.getNext(ChronoUnit.MILLIS, ChronoField.MILLI_OF_SECOND, zonedDateTime);
		assertEquals(zonedDateTime.getHour(), nextDay.getHour());
		assertEquals((zonedDateTime.getMinute() + 1) % 60, nextDay.getMinute());
		assertEquals((zonedDateTime.getSecond() + 1) % 60, nextDay.getSecond());
		assertEquals((zonedDateTime.get(ChronoField.MILLI_OF_SECOND) + 1) % 1000, nextDay.get(ChronoField.MILLI_OF_SECOND));
	}

	@Test
	public void testDateBinner() {
		ZoneId id = ZoneId.of("Z");
        ZonedDateTime start = ZonedDateTime.ofInstant(Instant.ofEpochMilli(usEastDTStartMs), id).truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime end = start.plus(7, ChronoUnit.DAYS);
		DateBinner<Long> dateBinner = new DateBinner<>(ChronoUnit.DAYS, ChronoField.EPOCH_DAY, start.toInstant(), end.toInstant(), id);

		List<Long> records = new ArrayList<>();
		long offset = 0;
		for (int i = 0; i < 24 * 7; i++) {
			records.add(start.toInstant().toEpochMilli() + offset);
			offset += 60 * 60 * 1000;
		}

		DateAccessor<Long> dateAccessor = new DateAccessor<Long>() {
			@Override
			public Instant getDateTime(Long record) {
				return Instant.ofEpochMilli(record);
			}
		};

		// ensure the bins have been generated (there should be 8 as the final datetime is inclusive the of end datetime)
		assertEquals(dateBinner.getBins().size(), 8);
		dateBinner.getBins().forEach(bin -> {
			assertEquals(bin.records().size(), 0);
		});

		// place records
		dateBinner.placeRecords(records, dateAccessor);

		for (int i = 0; i < dateBinner.getBins().size(); i++) {
			TemporalBin<Long> bin = dateBinner.getBins().get(i);
			if (i == 7) {
				assertEquals(bin.records().size(), 0);
			} else {
				assertEquals(bin.records().size(), 24);
				bin.records().forEach(record -> {
					assertTrue(bin.endUtcMs() >= record);
					assertTrue(bin.startUtcMs() <= record);
				});
			}
		}

		dateBinner.getBins().forEach((bin) -> {
			if (bin.records().size() > 0) {
				assertEquals(bin.records().size(), 24);
				bin.records().forEach(record -> {
					assertTrue(bin.endUtcMs() >= record);
					assertTrue(bin.startUtcMs() <= record);
				});
			}
		});
	}
}
