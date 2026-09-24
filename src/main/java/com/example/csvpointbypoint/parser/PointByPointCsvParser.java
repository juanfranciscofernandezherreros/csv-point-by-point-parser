package com.example.csvpointbypoint.parser;

import com.example.csvpointbypoint.dto.PointByPointEventDTO;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PushbackReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class PointByPointCsvParser {
    private static final Set<String> DATA_COLUMNS = Set.of(
            "match_id", "record_type", "quarter", "sequence", "home_score", "away_score",
            "home_points_added", "away_points_added", "leader_side", "advantage",
            "advantage_direction", "home_is_winning", "away_is_winning");
    private static final Set<String> URL_COLUMNS = Set.of("link_url", "source_url");

    public long countRows(Path path) throws IOException {
        AtomicLong count = new AtomicLong();
        parseInChunks(path, 1_000, rows -> count.addAndGet(rows.size()));
        return count.get();
    }

    public void parseInChunks(Path path, int chunkSize, Consumer<List<PointByPointEventDTO>> chunkConsumer)
            throws IOException {
        parseInChunks(path, chunkSize, chunkConsumer, ignored -> {});
    }

    public void parseInChunks(Path path, int chunkSize, Consumer<List<PointByPointEventDTO>> chunkConsumer,
            LongConsumer rowObserver) throws IOException {
        if (chunkSize <= 0) throw new IllegalArgumentException("chunkSize must be greater than zero");
        try (PushbackReader reader = new PushbackReader(Files.newBufferedReader(path), 1)) {
            int first = reader.read();
            if (first != -1 && first != '\uFEFF') reader.unread(first);
            try (CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true)
                    .setTrim(true).build().parse(reader)) {
                validateHeader(parser.getHeaderNames());
                List<PointByPointEventDTO> chunk = new ArrayList<>(Math.min(chunkSize, 1_000));
                long rowsRead = 0;
                for (CSVRecord record : parser) {
                    if (record.size() == 1 && record.get(0).isBlank()) continue;
                    if (record.size() != parser.getHeaderNames().size()) {
                        throw new IllegalArgumentException("CSV row " + record.getRecordNumber()
                                + " has " + record.size() + " columns; expected " + parser.getHeaderNames().size());
                    }
                    chunk.add(parseRecord(record));
                    rowObserver.accept(++rowsRead);
                    if (chunk.size() == chunkSize) {
                        chunkConsumer.accept(List.copyOf(chunk));
                        chunk.clear();
                    }
                }
                if (!chunk.isEmpty()) chunkConsumer.accept(List.copyOf(chunk));
            }
        }
    }

    private void validateHeader(List<String> columns) {
        Set<String> names = new HashSet<>(columns);
        Set<String> allowed = new HashSet<>(DATA_COLUMNS);
        allowed.addAll(URL_COLUMNS);
        if (names.size() != columns.size() || !names.containsAll(DATA_COLUMNS) || !allowed.containsAll(names)) {
            throw new IllegalArgumentException("CSV header must contain the 13 point-by-point data columns"
                    + " and may additionally contain link_url and source_url; found " + columns);
        }
    }

    private PointByPointEventDTO parseRecord(CSVRecord record) {
        return new PointByPointEventDTO(
                required(record, "match_id"), required(record, "record_type"), required(record, "quarter"),
                integer(record, "sequence"), integer(record, "home_score"), integer(record, "away_score"),
                integer(record, "home_points_added"), integer(record, "away_points_added"),
                optional(record, "leader_side"), optional(record, "advantage"),
                optional(record, "advantage_direction"), booleanValue(record, "home_is_winning"),
                booleanValue(record, "away_is_winning"));
    }

    private String required(CSVRecord record, String column) {
        String value = record.get(column);
        if (value.isBlank()) throw invalid(record, column, "must not be blank");
        return value;
    }

    private String optional(CSVRecord record, String column) {
        String value = record.get(column);
        return value.isBlank() ? null : value;
    }

    private int integer(CSVRecord record, String column) {
        String value = required(record, column);
        try { return Integer.parseInt(value); }
        catch (NumberFormatException exception) { throw invalid(record, column, "must be an integer"); }
    }

    private boolean booleanValue(CSVRecord record, String column) {
        String value = required(record, column);
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        throw invalid(record, column, "must be true or false");
    }

    private IllegalArgumentException invalid(CSVRecord record, String column, String reason) {
        return new IllegalArgumentException("CSV row " + record.getRecordNumber()
                + ", column " + column + " " + reason);
    }
}
