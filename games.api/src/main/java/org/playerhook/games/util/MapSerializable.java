package org.playerhook.games.util;

import com.google.common.collect.ImmutableList;
import org.playerhook.games.api.Rules;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public interface MapSerializable {

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

    static String loadString(Map<String, Object> map, String property) {
        return Optional.ofNullable(map.get(property)).map(Object::toString).orElse(null);
    }

    static Rules loadRules(Map<String, Object> map, String property) {
        String ruleClassName = Optional.ofNullable(map.get(property)).map(Object::toString).orElseThrow(() -> new IllegalArgumentException(property + " is missing!"));
        try {
            Class<?> clazz = Class.forName(ruleClassName);
            if (Rules.class.isAssignableFrom(clazz)) {
                return (Rules) clazz.newInstance();
            }
            throw new IllegalArgumentException("Class " + ruleClassName + " is not a rule");
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Rule class " + ruleClassName + " does not exist on the classpath");
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Rule class " + ruleClassName + " cannot be instantiated");
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Consturctor for class " + ruleClassName + " cannot be accessed");
        }
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

    Map<String, Object> toMap(boolean includeInternalState);

}
