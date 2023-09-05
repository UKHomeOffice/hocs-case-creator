package uk.gov.digital.ho.hocs.entrypoint.model;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapUtils {

    @SafeVarargs
    public static <K, V> Map<K, V> concatEntries(Stream<Map.Entry<K, V>> ...parts) {
        return Arrays.stream(parts)
                     .flatMap(Function.identity())
                     .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
