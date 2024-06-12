package org.temporaltree;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;

public class TreeGenerator<T> {

    private static final TemporalTreeConfiguration DEFAULT_TREE_CONFIGURATION = initDefaultTreeConfiguration();
    private static final DateTimeFormatter hourStartFormat = DateTimeFormatter.ofPattern("hh:mm");
    private static final DateTimeFormatter hourEndFormat = DateTimeFormatter.ofPattern("hh:mm a z");
    private static final DateTimeFormatter minuteStartFormat = DateTimeFormatter.ofPattern("hh:mm:ss");
    private static final DateTimeFormatter minuteEndFormat = DateTimeFormatter.ofPattern("hh:mm:ss a z");

    /**
     * Initializes the default temporal tree configuration
     * 
     * @return
     */
    private static TemporalTreeConfiguration initDefaultTreeConfiguration() {
        DateTimeFormatter minuteKeyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm z");
        DateTimeFormatter hourKeyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH z");
        DateTimeFormatter dayKeyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dayTitleFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy");

        TemporalTreeConfiguration minuteConfiguration = new TemporalTreeConfiguration(ChronoUnit.MINUTES,
                ChronoField.MINUTE_OF_HOUR, new DateKeyGenerator() {
                    public String generateKey(ZonedDateTime zonedDateTime) {
                        return minuteKeyFormatter.format(zonedDateTime);
                    }
                }, new DateTitleGenerator() {
                    public String generateTitle(ZonedDateTime zonedDateTime) {
                        ZonedDateTime next = DateBinner.getNextMinute(zonedDateTime).minusSeconds(1);
                        return generateMinuteLabel(zonedDateTime, next);
                    }
                }, -1, null);

        TemporalTreeConfiguration hourConfiguration = new TemporalTreeConfiguration(ChronoUnit.HOURS,
                ChronoField.HOUR_OF_DAY, new DateKeyGenerator() {
                    public String generateKey(ZonedDateTime zonedDateTime) {
                        return hourKeyFormatter.format(zonedDateTime);
                    }
                }, new DateTitleGenerator() {
                    public String generateTitle(ZonedDateTime zonedDateTime) {
                        ZonedDateTime next = DateBinner.getNextHour(zonedDateTime).minusMinutes(1);
                        return generateHourLabel(zonedDateTime, next);
                    }
                }, 240, minuteConfiguration);

        TemporalTreeConfiguration dateConfiguration = new TemporalTreeConfiguration(ChronoUnit.DAYS,
                ChronoField.EPOCH_DAY, new DateKeyGenerator() {
                    public String generateKey(ZonedDateTime zonedDateTime) {
                        return dayKeyFormatter.format(zonedDateTime);
                    }
                }, new DateTitleGenerator() {
                    public String generateTitle(ZonedDateTime zonedDateTime) {
                        return dayTitleFormatter.format(zonedDateTime);
                    }
                }, 96, hourConfiguration);

        return dateConfiguration;
    }

    /**
     * Generates an hour label
     * 
     * @param hour
     * @return
     */
    public static String generateHourLabel(ZonedDateTime zonedDateTimeStart, ZonedDateTime zonedDateTimeEnd) {
        return hourStartFormat.format(zonedDateTimeStart) + " - " + hourEndFormat.format(zonedDateTimeEnd);
    };

    /**
     * Generates a minute label
     * 
     * @param {number} minute
     * @returns
     */
    public static String generateMinuteLabel(ZonedDateTime zonedDateTimeStart, ZonedDateTime zonedDateTimeEnd) {
        return minuteStartFormat.format(zonedDateTimeStart) + " - " + minuteEndFormat.format(zonedDateTimeEnd);
    };

    public ObjectNode generateIndexTree(List<T> records, LeafGenerator<T> leafGenerator, int startIdx, int endIdx) {
        if (endIdx - startIdx <= 100) {
            StringBuilder keyBuilder = new StringBuilder();
            keyBuilder.append(startIdx);
            keyBuilder.append("-");
            keyBuilder.append(Math.max(endIdx, 0));
            String key = keyBuilder.toString();
            ObjectNode node = TreeUtils.createNodeWithChildren(key, key);
            for (int i = startIdx; i <= endIdx; i++) {
                ObjectNode leaf = leafGenerator.generateLeaf(records.get(i));
                if (leaf != null) {
                    ((ArrayNode) node.get("children")).add(leaf);
                }
            }
            return node;
        } else {
            StringBuilder keyBuilder = new StringBuilder();
            keyBuilder.append(startIdx);
            keyBuilder.append("-");
            keyBuilder.append(Math.max(endIdx, 0));
            String key = keyBuilder.toString();
            ObjectNode node = TreeUtils.createNodeWithChildren(key, key);
            int factor = 10;
            while ((endIdx - startIdx) / factor > 10) {
                factor *= 10;
            }
            for (int i = startIdx; i < Math.min(records.size(), endIdx); i += factor) {
                ((ArrayNode) node.get("children")).add(
                        generateIndexTree(records, leafGenerator, i, Math.min(records.size() - 1, i + factor - 1)));
            }
            return node;
        }
    };

    public ArrayNode generateTemporalTreeChildren(List<T> records, DateAccessor<T> dateAccessor,
            LeafGenerator<T> leafGenerator, ZoneId zoneId, TemporalTreeConfiguration treeConfiguration) {
        TemporalTreeConfiguration configuration = treeConfiguration != null ? treeConfiguration
                : DEFAULT_TREE_CONFIGURATION;
        ArrayNode children = TreeUtils.OBJECT_MAPPER.createArrayNode();
        if (records.size() > 0) {
            // determine datetime boundary for records provided
            Instant startDatetimeUtc = dateAccessor.getDateTime(records.get(0));
            Instant endDatetimeUtc = dateAccessor.getDateTime(records.get(records.size() - 1));

            DateBinner<T> dateBinner = new DateBinner<>(configuration.timeUnit(), configuration.timeField(),
                    startDatetimeUtc, endDatetimeUtc, zoneId);
            dateBinner.placeRecords(records, dateAccessor);

            // iterate over bins, filtering out bins without records, and determine if leafs
            // should be generated or if the bins need to be broken down further
            dateBinner.getBins().stream().filter(bin -> bin.records().size() > 0).forEach(bin -> {
                if (configuration.breakLimit() != -1 && bin.records().size() >= configuration.breakLimit()) {
                    TemporalTreeConfiguration childConfiguration = configuration.breakConfiguration();
                    ObjectNode node = TreeUtils.createNodeWithChildren(
                            configuration.keyGenerator().generateKey(bin.date()),
                            configuration.titleGenerator().generateTitle(bin.date()));
                    ((ArrayNode) node.get("children")).addAll(generateTemporalTreeChildren(bin.records(),
                            dateAccessor, leafGenerator, zoneId, childConfiguration));
                    children.add(node);
                } else {
                    ObjectNode node = TreeUtils.createNodeWithChildren(
                            configuration.keyGenerator().generateKey(bin.date()),
                            configuration.titleGenerator().generateTitle(bin.date()));
                    ((ArrayNode) node.get("children")).addAll(
                            bin.records().stream().map(leafGenerator::generateLeaf).collect(Collectors.toList()));
                    children.add(node);
                }
            });
        }
        return children;
    };
}
