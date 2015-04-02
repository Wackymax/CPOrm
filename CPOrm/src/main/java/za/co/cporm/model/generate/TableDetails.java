package za.co.cporm.model.generate;

import android.text.TextUtils;
import za.co.cporm.model.annotation.Index;
import za.co.cporm.model.annotation.TableConstraint;
import za.co.cporm.model.map.SqlColumnMapping;

import java.lang.reflect.Field;
import java.util.*;

/**
 * This class will contain all of the information retrieved from the reflection of a java object.
 * The goal is to do the reflection once, and then use this object from there on, onwards.  This should help us a bit with performance
 * and it contains use full quick shortcuts for manipulating java objects to and from sql.
 */
public class TableDetails {

    private final String tableName;
    private final Class tableClass;
    private final Collection<ColumnDetails> columns = new LinkedList<ColumnDetails>();
    private final Collection<Index> indices = new LinkedList<Index>();
    private final Collection<TableConstraint> constraints = new LinkedList<TableConstraint>();
    private final Collection<Class<?>> changeListener = new LinkedList<Class<?>>();

    public TableDetails(String tableName, Class tableClass){
        this.tableName = tableName;
        this.tableClass = tableClass;
    }

    public String getTableName() {
        return tableName;
    }

    public Class getTableClass() {
        return tableClass;
    }

    public ColumnDetails findPrimaryKeyColumn(){
        for (ColumnDetails column : columns) {
            if(column.isPrimaryKey()) return column;
        }

        return null;
    }

    public String[] getColumnNames(){
        List<String> columnNames = new ArrayList<String>(columns.size());
        for (ColumnDetails column : columns) {
            columnNames.add(column.getColumnName());
        }

        return columnNames.toArray(new String[]{});
    }

    public Collection<ColumnDetails> getColumns() {
        return Collections.unmodifiableCollection(columns);
    }

    public void addColumn(ColumnDetails column){
        columns.add(column);

        boolean hasPrimaryKey = false;

        for (ColumnDetails columnDetails : columns) {
            if(hasPrimaryKey && columnDetails.isPrimaryKey()) throw new IllegalStateException("Table may only have one primary key contraint on column definition, is a table constraints to specify more than one");

            hasPrimaryKey = hasPrimaryKey || columnDetails.isPrimaryKey();
        }
    }

    public Collection<Index> getIndices() {
        return Collections.unmodifiableCollection(indices);
    }

    public void addIndex(Index index) {
        indices.add(index);
    }

    public Collection<Class<?>> getChangeListeners() {
        return Collections.unmodifiableCollection(changeListener);
    }

    public void addChangeListener(Class<?> clazz) {
        changeListener.add(clazz);
    }

    public Collection<TableConstraint> getConstraints() {
        return constraints;
    }

    public void addConstraint(TableConstraint contConstraint){
        constraints.add(contConstraint);
    }

    /**
     * Contains all of the column information supplied by the {@link za.co.cporm.model.annotation.Column.Column} and other annotations
     * on the java fields.  It also contains a column mapping that will be used to convert java objects to and from SQL.
     */
    public static class ColumnDetails{
        private final String columnName;
        private final Field columnField;
        private final SqlColumnMapping columnTypeMapping;
        private final boolean primaryKey;
        private final boolean unique;
        private final boolean required;
        private final boolean autoIncrement;

        public ColumnDetails(String columnName, Field columnField, SqlColumnMapping columnTypeMapping, boolean primaryKey, boolean unique, boolean required, boolean autoIncrement) {
            this.columnName = columnName;
            this.columnField = columnField;
            this.columnTypeMapping = columnTypeMapping;
            this.primaryKey = primaryKey || autoIncrement;
            this.unique = unique;
            this.required = required;
            this.autoIncrement = autoIncrement;

            if(primaryKey && !required) throw new IllegalStateException("Column must be not required if primary key is set");

            if(TextUtils.isEmpty(columnName)) throw new IllegalArgumentException("A valid column name needs to be provided");
        }

        public String getColumnName() {
            return columnName;
        }

        public SqlColumnMapping getColumnTypeMapping() {
            return columnTypeMapping;
        }

        public Field getColumnField() {
            columnField.setAccessible(true);
            return columnField;
        }

        public boolean isPrimaryKey() {
            return primaryKey;
        }

        public boolean isUnique() {
            return unique;
        }

        public boolean isRequired() {
            return required;
        }

        public boolean isAutoIncrement() {
            return autoIncrement;
        }
    }
}
