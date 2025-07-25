/*
 * This file is generated by jOOQ.
 */
package sanity.nil.block.db.tables;


import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import sanity.nil.block.db.BlockDb;
import sanity.nil.block.db.Keys;
import sanity.nil.block.db.tables.records.BlocksRecord;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Blocks extends TableImpl<BlocksRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>block_db.blocks</code>
     */
    public static final Blocks BLOCKS = new Blocks();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<BlocksRecord> getRecordType() {
        return BlocksRecord.class;
    }

    /**
     * The column <code>block_db.blocks.created_at</code>.
     */
    public final TableField<BlocksRecord, OffsetDateTime> CREATED_AT = createField(DSL.name("created_at"), SQLDataType.TIMESTAMPWITHTIMEZONE(6), this, "");

    /**
     * The column <code>block_db.blocks.hash</code>.
     */
    public final TableField<BlocksRecord, String> HASH = createField(DSL.name("hash"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>block_db.blocks.status</code>.
     */
    public final TableField<BlocksRecord, String> STATUS = createField(DSL.name("status"), SQLDataType.VARCHAR(255), this, "");

    private Blocks(Name alias, Table<BlocksRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private Blocks(Name alias, Table<BlocksRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>block_db.blocks</code> table reference
     */
    public Blocks(String alias) {
        this(DSL.name(alias), BLOCKS);
    }

    /**
     * Create an aliased <code>block_db.blocks</code> table reference
     */
    public Blocks(Name alias) {
        this(alias, BLOCKS);
    }

    /**
     * Create a <code>block_db.blocks</code> table reference
     */
    public Blocks() {
        this(DSL.name("blocks"), null);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : BlockDb.BLOCK_DB;
    }

    @Override
    public UniqueKey<BlocksRecord> getPrimaryKey() {
        return Keys.CONSTRAINT_AD;
    }

    @Override
    public List<Check<BlocksRecord>> getChecks() {
        return Arrays.asList(
            Internal.createCheck(this, DSL.name("CONSTRAINT_A"), "\"status\" IN('AWAITING_UPLOAD', 'UPLOADED')", true)
        );
    }

    @Override
    public Blocks as(String alias) {
        return new Blocks(DSL.name(alias), this);
    }

    @Override
    public Blocks as(Name alias) {
        return new Blocks(alias, this);
    }

    @Override
    public Blocks as(Table<?> alias) {
        return new Blocks(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Blocks rename(String name) {
        return new Blocks(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Blocks rename(Name name) {
        return new Blocks(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Blocks rename(Table<?> name) {
        return new Blocks(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Blocks where(Condition condition) {
        return new Blocks(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Blocks where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Blocks where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Blocks where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Blocks where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Blocks where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Blocks where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Blocks where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Blocks whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Blocks whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
