package org.playerhook.games.util;

import com.google.common.collect.ImmutableList;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public interface MapSerializable {

    enum PrivacyLevel {
        /**
         * Contains all the information.
         *
         * Use this level for example to save items to the database.
         */
        INTERNAL,

        /**
         * Contains all information present for authenticated user such as player of the sesssion.
         */
        PROTECTED,

        /**
         * Use this level to return publicly available information about the serialized object.
         */
        PUBLIC
    }

    static <T> ImmutableList<T> loadList(Object list, Function<Object, T> loader) {
        if (!(list instanceof Iterable)) {
            return ImmutableList.of();
        }
        ImmutableList.Builder<T> builder = ImmutableList.builder();

        for (Object payload : (Iterable) list) {
            builder.add(loader.apply(payload));
        }
        return builder.build();
    }

    static int loadInteger(Map<String, Object> map, String property) {
        return Integer.valueOf(Optional.ofNullable(map.get(property)).map(Object::toString).orElseThrow(() -> new IllegalArgumentException(property + " is missing!")));
    }
    static long loadLong(Map<String, Object> map, String property) {
        return Long.valueOf(Optional.ofNullable(map.get(property)).map(Object::toString).orElseThrow(() -> new IllegalArgumentException(property + " is missing!")));
    }

    static String loadString(Map<String, Object> map, String property) {
        return Optional.ofNullable(map.get(property)).map(Object::toString).orElse(null);
    }

    static Instant loadInstant(Map<String, Object> map, String property) {
        return Optional.ofNullable(map.get(property)).map(Object::toString).map(Long::parseLong).map(Instant::ofEpochMilli).orElseThrow(() -> new IllegalArgumentException("No " + property + "  for " + map));
    }

    static URL loadURL(Map<String, Object> map, String property) {
        return Optional.ofNullable(map.get(property)).map(o -> {
            try {
                return new URL(o.toString());
            } catch (MalformedURLException e) {
                throw new RuntimeException("Error parsing the URL", e);
            }
        }).orElse(null);
    }

    Map<String, Object> toMap(PrivacyLevel level);

}
