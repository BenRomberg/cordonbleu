package com.benromberg.cordonbleu.util;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class CollectionUtil {
    public static <T> List<T> sortCaseInsensitive(List<T> list, Function<T, String> accessor) {
        Collections.sort(list, (item1, item2) -> accessor.apply(item1).compareToIgnoreCase(accessor.apply(item2)));
        return list;
    }

    public static <T> Stream<T> toStream(Optional<T> optional) {
        if (optional.isPresent()) {
            return Stream.of(optional.get());
        }
        return Stream.empty();
    }
}
