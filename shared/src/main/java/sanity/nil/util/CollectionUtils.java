package sanity.nil.util;

import java.util.Collection;

public class CollectionUtils {

    public static boolean isEmpty(final Collection<?> c) {
        return c == null || c.isEmpty();
    }

    public static boolean isNotEmpty(final Collection<?> c) {
        return !isEmpty(c);
    }
}
