package uk.gov.digital.ho.hocs.entrypoint;

import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ChunkedStreamTest {

    @Test
    public void chunkedStreamOf_groupsStreamElementsIntoListsOfTheDesiredSize() {
        assertThat(
            ChunkedStream.of(Stream.of(1, 2, 3, 4, 5), 2).collect(Collectors.toList())
        ).contains(
            List.of(1, 2),
            List.of(3, 4),
            List.of(5)
        );

        assertThat(
            ChunkedStream.of(Stream.of(1, 2, 3, 4, 5), 3).collect(Collectors.toList())
        ).contains(
            List.of(1, 2, 3),
            List.of(4, 5)
        );
    }
}
