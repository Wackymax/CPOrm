package za.co.cporm.model;

import java.util.List;

/**
 * The {@link CPOrmConfiguration} to be provided by the android meta data.  This must be implemented
 * by anyone that uses this ORM, as it will provide us with all of the model objects and other settings that we should cater for.
 */
public interface CPOrmConfiguration {

    /**
     * Provides us with the name of the database file to create
     * @return The name of the database file to create
     */
    String getDatabaseName();

    /**
     * Provides us with the version of the current database, if this version is increased, then the database will be upgraded.
     * Numbering should start at one and only ever increase.
     * @return The version of this database.
     */
    int getDatabaseVersion();

    /**
     * If query logging should be enabled.  If this setting is set to true, then all values will be printed for any interaction with the content provider, as well
     * as all database queries that are run.
     * @return True if query logging should be enabled.
     */
    boolean isQueryLoggingEnabled();

    /**
     * Return the database model objects that will be used to construct the database, and to interact with the ORM.<br>
     * All of the returned objects <em>MUST</em> have the required annotations {@link za.co.cporm.model.annotation.Table}<br>
     * Views can also be returned here by implementing the {@link za.co.cporm.model.generate.TableView} interface<br>
     * @return The tables and views that should be created.
     */
    List<Class<?>> getDataModelObjects();

    /**
     * If this option is true speed improvements will be enabled by calling enhanced methods directly
     * on the Content Provider to improve loading times of single item queries
     * @return true if custom method calling should be allowed
     */
    boolean allowContentProviderMethodCalling();
}
