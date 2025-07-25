/*
 * This file is generated by jOOQ.
 */
package sanity.nil.meta.db.tables.records;


import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;
import sanity.nil.meta.db.tables.WorkspaceSubscriptions;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class WorkspaceSubscriptionsRecord extends UpdatableRecordImpl<WorkspaceSubscriptionsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>metadata_db.workspace_subscriptions.id</code>.
     */
    public void setId(Short value) {
        set(0, value);
    }

    /**
     * Getter for <code>metadata_db.workspace_subscriptions.id</code>.
     */
    public Short getId() {
        return (Short) get(0);
    }

    /**
     * Setter for <code>metadata_db.workspace_subscriptions.description</code>.
     */
    public void setDescription(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>metadata_db.workspace_subscriptions.description</code>.
     */
    public String getDescription() {
        return (String) get(1);
    }

    /**
     * Setter for <code>metadata_db.workspace_subscriptions.title</code>.
     */
    public void setTitle(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>metadata_db.workspace_subscriptions.title</code>.
     */
    public String getTitle() {
        return (String) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Short> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached WorkspaceSubscriptionsRecord
     */
    public WorkspaceSubscriptionsRecord() {
        super(WorkspaceSubscriptions.WORKSPACE_SUBSCRIPTIONS);
    }

    /**
     * Create a detached, initialised WorkspaceSubscriptionsRecord
     */
    public WorkspaceSubscriptionsRecord(Short id, String description, String title) {
        super(WorkspaceSubscriptions.WORKSPACE_SUBSCRIPTIONS);

        setId(id);
        setDescription(description);
        setTitle(title);
        resetChangedOnNotNull();
    }
}
