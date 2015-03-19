package za.co.cporm.model;

import java.util.List;

/**
 * The model factory to be provided by the android meta data.  This must be implemented
 * by anyone that uses this ORM, as it will provide us with all of the model objects that we should cater for.
 */
public interface ModelFactory {

    List<Class<?>> getDataModelObjects();
}
