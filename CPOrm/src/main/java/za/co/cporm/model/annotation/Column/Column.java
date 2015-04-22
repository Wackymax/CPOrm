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

    /**If this setting is set to true, changes will be notified on the content resolver, if it is false it will not.
     * If more than one column is passed through on updates, changes will be notified if at least one of the columns being
     * updated is set to true.
     * @return true if changes are to be notified, false otherwise. Default is true.
     */
    boolean notifyChanges() default true;
}
