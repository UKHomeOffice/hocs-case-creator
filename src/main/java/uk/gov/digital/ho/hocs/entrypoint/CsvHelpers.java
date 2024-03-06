package uk.gov.digital.ho.hocs.entrypoint;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvFactory;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
public class CsvHelpers {
    public static Stream<Map<String, String>> getRowsFromInputStream(InputStream inputStream) throws IOException {
        CsvSchema bootstrapSchema = CsvSchema.emptySchema().withHeader();
        CsvMapper mapper = new CsvMapper(CsvFactory.builder().enable(CsvParser.Feature.SKIP_EMPTY_LINES).build());

        MappingIterator<Map<String, String>> readValues =
            mapper.readerFor(Map.class).with(bootstrapSchema).readValues(inputStream);

        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(readValues, 0),
            false
        );
    }

    public static StreamingResponseBody streamCsvToResponseBody(
        Stream<Map<String, String>> rows,
        List<String> columnOrdering
    ) {
        return outputStream -> {
            //noinspection ResultOfMethodCallIgnored
            rows.reduce(
                (JsonGenerator) null,
                (generator, row) -> {
                    try {
                        if (generator == null) {
                            generator = buildCsvGenerator(columnOrdering, row.keySet(), outputStream);
                        }

                        generator.writeObject(row);
                    } catch (Exception e) {
                        log.error("Failed to write row {}", row, e);
                    }

                    return generator;
                },
                (a, b) -> a == null ? a : b
            );
        };
    }

    private static JsonGenerator buildCsvGenerator(
        List<String> columnOrdering,
        Set<String> columns,
        OutputStream outputStream
    ) throws IOException {
        CsvMapper mapper = new CsvMapper();
        CsvSchema.Builder builder = CsvSchema.builder();

        columnOrdering.forEach(builder::addColumn);
        columns.stream()
               .filter(key -> !builder.hasColumn(key))
               .forEach(builder::addColumn);

        CsvSchema schema = builder.build().withHeader();

        return mapper.writer(schema).createGenerator(outputStream);
    }
}
