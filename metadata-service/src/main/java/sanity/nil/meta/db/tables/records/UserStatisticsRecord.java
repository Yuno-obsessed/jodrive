/*
 * This file is generated by jOOQ.
 */
package sanity.nil.meta.db.tables.records;


import org.jooq.Record2;
import org.jooq.impl.UpdatableRecordImpl;
import sanity.nil.meta.db.tables.UserStatistics;

import java.util.UUID;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class UserStatisticsRecord extends UpdatableRecordImpl<UserStatisticsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>metadata_db.user_statistics.statistics_id</code>.
     */
    public void setStatisticsId(Short value) {
        set(0, value);
    }

    /**
     * Getter for <code>metadata_db.user_statistics.statistics_id</code>.
     */
    public Short getStatisticsId() {
        return (Short) get(0);
    }

    /**
     * Setter for <code>metadata_db.user_statistics.user_id</code>.
     */
    public void setUserId(UUID value) {
        set(1, value);
    }

    /**
     * Getter for <code>metadata_db.user_statistics.user_id</code>.
     */
    public UUID getUserId() {
        return (UUID) get(1);
    }

    /**
     * Setter for <code>metadata_db.user_statistics.value</code>.
     */
    public void setValue(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>metadata_db.user_statistics.value</code>.
     */
    public String getValue() {
        return (String) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record2<Short, UUID> key() {
        return (Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached UserStatisticsRecord
     */
    public UserStatisticsRecord() {
        super(UserStatistics.USER_STATISTICS);
    }

    /**
     * Create a detached, initialised UserStatisticsRecord
     */
    public UserStatisticsRecord(Short statisticsId, UUID userId, String value) {
        super(UserStatistics.USER_STATISTICS);

        setStatisticsId(statisticsId);
        setUserId(userId);
        setValue(value);
        resetChangedOnNotNull();
    }
}
