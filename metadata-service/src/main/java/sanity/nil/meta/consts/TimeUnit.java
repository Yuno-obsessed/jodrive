package sanity.nil.meta.consts;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public enum TimeUnit {
    MINUTE,
    HOUR,
    DAY,
    MONTH;

    public TemporalUnit toTemporalUnit() {
        return switch (this) {
            case MINUTE -> ChronoUnit.MINUTES;
            case HOUR -> ChronoUnit.HOURS;
            case DAY -> ChronoUnit.DAYS;
            case MONTH -> ChronoUnit.MONTHS;
        };
    }
}
