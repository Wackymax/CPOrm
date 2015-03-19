package za.co.cporm.model.map;

import za.co.cporm.model.map.types.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * The factory that will contain all of the available column conversion for the system.
 * This class can be extended, and the class name provided as part of the meta information to load the
 * extended class instead.
 */
public class SqlColumnMappingFactory {

    private final List<SqlColumnMapping> columnMappings;

    public SqlColumnMappingFactory(){

        columnMappings = new ArrayList<SqlColumnMapping>();
        columnMappings.add(new BooleanType());
        columnMappings.add(new CalendarType());
        columnMappings.add(new DateType());
        columnMappings.add(new DoubleType());
        columnMappings.add(new FloatType());
        columnMappings.add(new IntegerType());
        columnMappings.add(new LongType());
        columnMappings.add(new ShortType());
        columnMappings.add(new StringType());
        columnMappings.add(new UUIDType());
    }

    public SqlColumnMapping findColumnMapping(Type fieldType){

        for (SqlColumnMapping columnMapping : columnMappings) {
            if(columnMapping.getJavaType().equals(fieldType)) return columnMapping;
        }

        throw new IllegalArgumentException("No valid SQL mapping found for type " + fieldType);
    }
}
