package za.co.cporm.model.generate;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import za.co.cporm.model.annotation.Index;
import za.co.cporm.model.annotation.TableConstraint;
import za.co.cporm.model.map.SqlColumnMapping;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * This class will contain all of the information retrieved from the reflection of a java object.
 * The goal is to do the reflection once, and then use this object from there on, onwards.  This should help us a bit with performance
 * and it contains use full quick shortcuts for manipulating java objects to and from sql.
 */
public class TableDetails {

    private final String tableName;
    private final String authority;
    private final Class tableClass;
    private final Constructor tableClassConstructor;
    private final List<ColumnDetails> columns = new LinkedList<ColumnDetails>();
    private final List<Index> indices = new LinkedList<Index>();
    private final List<TableConstraint> constraints = new LinkedList<TableConstraint>();
    private final List<Class<?>> changeListener = new LinkedList<Class<?>>();

    public TableDetails(String tableName, String authority, Class tableClass){
        this.tableName = tableName;
        this.authority = authority;
        this.tableClass = tableClass;

        try {
            tableClassConstructor = tableClass.getConstructor();
            tableClassConstructor.setAccessible(true);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not create a new instance of data model object: " + tableName);
        }
    }

    public String getTableName() {
        return tableName;
    }

    public String getAuthority() {

        return authority;
    }

    public Class getTableClass() {
        return tableClass;
    }

    public Object createNewModelInstance() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return tableClassConstructor.newInstance();
    }

    public ColumnDetails findPrimaryKeyColumn(){

        for (int i = 0; i < columns.size(); i++) {
            ColumnDetails column = columns.get(i);
            if(column.primaryKey) return column;
        }

        return null;
    }

    public String[] getColumnNames(){

        String[] columnNames = new String[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            ColumnDetails columnDetails = columns.get(i);
            columnNames[i] = columnDetails.columnName;
        }

        return columnNames;
    }

    public List<ColumnDetails> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    public ColumnDetails findColumn(String name) {

        for (int i = 0; i < columns.size(); i++) {
            ColumnDetails column =  columns.get(i);

            if(column.columnName.equalsIgnoreCase(name))
                return column;
        }

        return null;
    }

    public void addColumn(ColumnDetails column){
        columns.add(column);

        boolean hasPrimaryKey = false;

        for (ColumnDetails columnDetails : columns) {
            if(hasPrimaryKey && columnDetails.isPrimaryKey()) throw new IllegalStateException("Table may only have one primary key constraint on column definition, is a table constraints to specify more than one");

            hasPrimaryKey = hasPrimaryKey || columnDetails.isPrimaryKey();
        }
    }

    public List<Index> getIndices() {
        return Collections.unmodifiableList(indices);
    }

    public void addIndex(Index index) {
        indices.add(index);
    }

    public List<Class<?>> getChangeListeners() {
        return Collections.unmodifiableList(changeListener);
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
        private final boolean notifyChanges;

        public ColumnDetails(String columnName, Field columnField, SqlColumnMapping columnTypeMapping, boolean primaryKey, boolean unique, boolean required, boolean autoIncrement, boolean notifyChanges) {
            this.columnName = columnName;
            this.columnField = columnField;
            this.columnTypeMapping = columnTypeMapping;
            this.primaryKey = primaryKey || autoIncrement;
            this.unique = unique;
            this.required = required;
            this.autoIncrement = autoIncrement;
            this.notifyChanges = notifyChanges;

            if(primaryKey && !required) throw new IllegalStateException("Column must be not required if primary key is set");

            if(TextUtils.isEmpty(columnName)) throw new IllegalArgumentException("A valid column name needs to be provided");

            columnField.setAccessible(true);
        }

        public String getColumnName() {
            return columnName;
        }

        public SqlColumnMapping getColumnTypeMapping() {
            return columnTypeMapping;
        }

        public Field getColumnField() {
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

        public boolean notifyChanges() {

            return notifyChanges;
        }

        public void setFieldValue(Cursor cursor, int columnIndex, Object dataModelObject) throws IllegalAccessException {

            columnField.set(dataModelObject, columnTypeMapping.getColumnValue(cursor, columnIndex));
        }

        public void setContentValue(ContentValues contentValues, Object dataModelObject) throws IllegalAccessException {

            Object value = columnField.get(dataModelObject);

            if (value == null) contentValues.putNull(columnName);
            else columnTypeMapping.setColumnValue(contentValues, columnName, value);
        }
    }
}
