package za.co.cporm.model.generate;

import android.content.Context;
import android.text.TextUtils;
import za.co.cporm.model.annotation.ChangeListeners;
import za.co.cporm.model.annotation.Column.Column;
import za.co.cporm.model.annotation.Column.PrimaryKey;
import za.co.cporm.model.annotation.Column.Unique;
import za.co.cporm.model.annotation.*;
import za.co.cporm.model.map.SqlColumnMapping;
import za.co.cporm.model.map.SqlColumnMappingFactory;
import za.co.cporm.model.util.ManifestHelper;
import za.co.cporm.model.util.NamingUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class will convert any valid Java Object marked with the {@link za.co.cporm.model.annotation.Table} annotation
 * to a valid {@link za.co.cporm.model.generate.TableDetails} object.
 */
public class ReflectionHelper {

    /**
     * Creates a {@link za.co.cporm.model.generate.TableDetails} object containing the reflection information retrieved from
     * the supplied java object.
     * @param context The context that can be used to get meta information
     * @param dataModelObject The object to analyse
     * @return The {@link za.co.cporm.model.generate.TableDetails} containing the reflection information
     */
    public static TableDetails getTableDetails(Context context, Class<?> dataModelObject){
        Table table = dataModelObject.getAnnotation(Table.class);
        if(table == null) throw new IllegalArgumentException("Object does not have Table annotation: " + dataModelObject.getSimpleName());

        String tableName = TextUtils.isEmpty(table.tableName()) ? NamingUtils.getSQLName(dataModelObject.getSimpleName()) : table.tableName();
        TableDetails tableDetails = new TableDetails(tableName, dataModelObject);
        SqlColumnMappingFactory columnMappingFactory = ManifestHelper.getMappingFactory(context);

        for (Map.Entry<Field, Column> columnFieldEntry : getColumns(dataModelObject).entrySet()) {

            Column column = columnFieldEntry.getValue();
            Field field = columnFieldEntry.getKey();

            boolean autoIncrement = false;
            if(field.isAnnotationPresent(PrimaryKey.class)){
                autoIncrement = field.getAnnotation(PrimaryKey.class).autoIncrement();
            }

            String columnName = column.columnName();
            if(TextUtils.isEmpty(columnName)) columnName = NamingUtils.getSQLName(field.getName());
            SqlColumnMapping columnMapping = columnMappingFactory.findColumnMapping(field.getType());

            tableDetails.addColumn(new TableDetails.ColumnDetails(columnName, field, columnMapping, field.isAnnotationPresent(PrimaryKey.class), field.isAnnotationPresent(Unique.class), column.required(), autoIncrement, column.notifyChanges()));
        }

        if(tableDetails.getColumns().isEmpty()) throw new IllegalStateException("No columns are defined for table " + tableDetails.getTableName());
        if(tableDetails.findPrimaryKeyColumn() == null && !TableView.class.isAssignableFrom(dataModelObject)) throw new IllegalStateException("No primary key column defined for table " + tableDetails.getTableName());

        if(dataModelObject.isAnnotationPresent(Indices.class)){

            for (Index index : dataModelObject.getAnnotation(Indices.class).indices()) {

                tableDetails.addIndex(index);
            }
        }

        if(dataModelObject.isAnnotationPresent(ChangeListeners.class)){

            for (Class<?> changeListener : dataModelObject.getAnnotation(ChangeListeners.class).changeListeners()) {

                tableDetails.addChangeListener(changeListener);
            }
        }

        if(table.constraints().length > 0){
            for (TableConstraint tableConstraint : table.constraints()) {
                tableDetails.addConstraint(tableConstraint);
            }
        }
        return tableDetails;
    }

    public static Map<Field, Column> getColumns(Class<?> dataModelObject){

        Map<Field, Column> columns = new LinkedHashMap<Field, Column>();

        for (Field field : getAllObjectFields(dataModelObject)) {
            if(field.isAnnotationPresent(Column.class) && !Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())){
                columns.put(field, field.getAnnotation(Column.class));
            }
        }

        return columns;
    }

    private static List<Field> getAllObjectFields(Class<?> object){

        if(object.isInterface() && object.isEnum()) return new LinkedList<Field>();

        List<Field> objectFields = new LinkedList<Field>();
        for (Field field : object.getDeclaredFields()) {

            objectFields.add(field);
        }

        if(object.getSuperclass() != null){
            objectFields.addAll(getAllObjectFields(object.getSuperclass()));
        }

        return objectFields;
    }
}
