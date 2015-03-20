package za.co.cporm.model.annotation.Column;

import java.lang.annotation.*;
import java.lang.annotation.RetentionPolicy;

/**
 * Identifies a field as database column to be generated.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

    /**The name of the column, if none is provided, the field name will be converted and used instead*/
    String columnName() default "";

    /**If the field should be set required/nullable, the default is true*/
    boolean required() default true;
}
