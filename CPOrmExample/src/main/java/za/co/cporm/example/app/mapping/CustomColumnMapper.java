package za.co.cporm.example.app.mapping;

import za.co.cporm.model.map.SqlColumnMapping;
import za.co.cporm.model.map.SqlColumnMappingFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hennie.brink on 2015-05-16.
 */
public class CustomColumnMapper extends SqlColumnMappingFactory {

    private List<SqlColumnMapping> customMappings;

    public CustomColumnMapper() {
        super();

        customMappings = new ArrayList<>();
        customMappings.add(new ExampleColumnMapping());
    }

    @Override
    public SqlColumnMapping findColumnMapping(Class<?> fieldType) {

        for (SqlColumnMapping customMapping : customMappings) {

            Class<?> javaType = customMapping.getJavaType();

            if(javaType.equals(fieldType) || javaType.isAssignableFrom(fieldType)) {

                return customMapping;
            }
        }
        return super.findColumnMapping(fieldType);
    }
}
