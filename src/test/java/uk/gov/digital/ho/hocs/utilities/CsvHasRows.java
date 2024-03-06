package uk.gov.digital.ho.hocs.utilities;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.mockito.internal.matchers.text.ValuePrinter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.collection.IsMapContaining.hasEntry;

/**
 * Given a list of expected partial rows, will match a string that can be parsed as a CSV with the first row being
 * headers, and verify that each of the partial rows is a subset of at least one of the rows in the form.
 */
public class CsvHasRows extends BaseMatcher<String> {

    private final List<Map<String, String>> expectedPartialRows;

    public CsvHasRows(List<Map<String, String>> expectedPartialRows) {
        this.expectedPartialRows = expectedPartialRows;
    }

    @Override
    public boolean matches(Object actual) {
        return validateString(actual).isEmpty();
    }

    @Override
    public void describeMismatch(Object actual, Description mismatchDescription) {
        validateString(actual).ifPresent(mismatchDescription::appendText);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("CSV has rows containing:\n");

        expectedPartialRows.stream().limit(2).forEach(row -> description.appendText("  %s\n".formatted(describe(row))));

        if (expectedPartialRows.size() > 2) {
            description.appendText("  ... and %s other row(s)".formatted(expectedPartialRows.size() - 2));
        }
    }

    public static List<Map<String, String>> getRowsFromString(String csv) throws IOException {
        CsvSchema bootstrapSchema = CsvSchema.emptySchema().withHeader();
        CsvMapper mapper = new CsvMapper();

        try (MappingIterator<Map<String, String>> readValues =
                 mapper.readerFor(Map.class).with(bootstrapSchema).readValues(csv)) {
            return readValues.readAll();
        }
    }

    private Optional<String> validateString(Object actual) {
        if (!(actual instanceof String)) {
            return Optional.of("%s is not a string".formatted(describe(actual)));
        }

        List<Map<String, String>> actualRows;
        try {
            actualRows = getRowsFromString((String) actual);
        } catch (IOException e) {
            return Optional.of("Could not parse %s as a CSV: %s".formatted(describe(actual), e.getMessage()));
        }

        return expectedPartialRows
            .stream()
            .filter(row -> !csvHasRow(actualRows, row))
            .findFirst()
            .map(row -> "No row that is a superset of %s found in:\n%s".formatted(describe(row),describe(actualRows)));
    }

    private boolean csvHasRow(List<Map<String, String>> actualRows, Map<String, String> expectedRow) {
        return actualRows.stream().anyMatch(actualRow -> isSubset(actualRow, expectedRow));
    }

    private boolean isSubset(Map<String, String> superset, Map<String, String> possibleSubset) {
        return possibleSubset
            .entrySet()
            .stream()
            .allMatch(entry -> hasEntry(entry.getKey(), entry.getValue()).matches(superset));
    }

    private String describe(Object object) {
        return ValuePrinter.print(object);
    }
}
