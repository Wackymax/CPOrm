package za.co.cporm.model.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import za.co.cporm.model.ModelFactory;
import za.co.cporm.model.map.SqlColumnMappingFactory;

/**
 * Created by hennie.brink on 2015-03-18.
 */
public class ManifestHelper {

    public static final String METADATA_AUTHORITY = "AUTHORITY";
    public static final String METADATA_MODEL_FACTORY = "MODEL_FACTORY";
    public static final String METADATA_MAPPING_FACTORY = "MAPPING_FACTORY";
    public static final String METADATA_DATABASE = "DATABASE";
    public static final String METADATA_VERSION = "VERSION";
    public static final String METADATA_DOMAIN_PACKAGE_NAME = "DOMAIN_PACKAGE_NAME";
    public static final String METADATA_QUERY_LOG = "QUERY_LOG";

    public static final String DATABASE_DEFAULT_NAME = "CPOrm.db";

    public static String getAuthority(Context context){

        String authority = getMetaDataString(context, METADATA_AUTHORITY);
        if(TextUtils.isEmpty(authority)) throw new IllegalArgumentException("Authority must be provided as part of the meta data");

        return authority;
    }

    /**
     * This will try to instantiate the model factory base on a valid
     * Java Class name.
     * @param context the {@link android.content.Context} of the Android application
     * @return The model factory specified by the {@link #METADATA_MODEL_FACTORY}
     */
    public static ModelFactory getModelFactory(Context context) throws IllegalArgumentException{
        String className = getMetaDataString(context, METADATA_MODEL_FACTORY);
        try{
            Class modelFactory = Class.forName(className);
            if(ModelFactory.class.isAssignableFrom(modelFactory)){
                return (ModelFactory)modelFactory.getConstructor().newInstance();
            }
            else throw new IllegalArgumentException("The class provided is not and instance of ModelFactory: " + className);
        } catch (Exception ex){
            throw new IllegalArgumentException("Failed to create ModelFactory instance", ex);
        }
    }

    /**
     * This will try to instantiate the mapping factory based on the class name provided in the meta.  If
     * no class name is found, or the meta is not set, the default factory {@link za.co.cporm.model.map.SqlColumnMappingFactory} will be loaded.
     * @param context the {@link android.content.Context} of the Android application
     * @return The mapping factory specified by the {@link #METADATA_MAPPING_FACTORY}
     * @throws IllegalArgumentException
     */
    public static SqlColumnMappingFactory getMappingFactory(Context context) throws IllegalArgumentException{
        String className = getMetaDataString(context, METADATA_MAPPING_FACTORY);

        if(TextUtils.isEmpty(className))
            className = SqlColumnMappingFactory.class.getCanonicalName();

        try{
            Class modelFactory = Class.forName(className);
            if(SqlColumnMappingFactory.class.isAssignableFrom(modelFactory)){
                return (SqlColumnMappingFactory)modelFactory.getConstructor().newInstance();
            }
            else throw new IllegalArgumentException("The class provided is not and instance of SqlColumnMappingFactory: " + className);
        } catch (Exception ex){
            throw new IllegalArgumentException("Failed to create ModelFactory instance", ex);
        }
    }

    /**
     * Grabs the database version from the manifest.
     *
     * @param context  the {@link android.content.Context} of the Android application
     * @return the database version as specified by the {@link #METADATA_VERSION} version or 1 of
     *         not present
     */
    public static int getDatabaseVersion(Context context) {
        Integer databaseVersion = getMetaDataInteger(context, METADATA_VERSION);

        if ((databaseVersion == null) || (databaseVersion == 0)) {
            databaseVersion = 1;
        }

        return databaseVersion;
    }

    /**
     * Grabs the domain name of the model classes from the manifest.
     *
     * @param context  the {@link android.content.Context} of the Android application
     * @return the package String that Sugar uses to search for model classes
     */
    public static String getDomainPackageName(Context context){
        String domainPackageName = getMetaDataString(context, METADATA_DOMAIN_PACKAGE_NAME);

        if (domainPackageName == null) {
            domainPackageName = "";
        }

        return domainPackageName;
    }

    /**
     * Grabs the name of the database file specified in the manifest.
     *
     * @param context  the {@link android.content.Context} of the Android application
     * @return the value for the {@value #METADATA_DATABASE} meta data in the AndroidManifest or
     *         {@link #DATABASE_DEFAULT_NAME} if not present
     */
    public static String getDatabaseName(Context context) {
        String databaseName = getMetaDataString(context, METADATA_DATABASE);

        if (databaseName == null) {
            databaseName = DATABASE_DEFAULT_NAME;
        }

        return databaseName;
    }

    /**
     * Grabs the debug flag from the manifest.
     *
     * @param context  the {@link android.content.Context} of the Android application
     * @return true if the debug flag is enabled
     */
    public static boolean getDebugEnabled(Context context) {
        return getMetaDataBoolean(context, METADATA_QUERY_LOG);
    }

    private static String getMetaDataString(Context context, String name) {
        String value = null;

        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            value = ai.metaData.getString(name);
        } catch (Exception e) {
            Log.d("sugar", "Couldn't find config value: " + name);
        }

        return value;
    }

    private static Integer getMetaDataInteger(Context context, String name) {
        Integer value = null;

        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            value = ai.metaData.getInt(name);
        } catch (Exception e) {
            Log.d("sugar", "Couldn't find config value: " + name);
        }

        return value;
    }

    private static Boolean getMetaDataBoolean(Context context, String name) {
        Boolean value = false;

        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            value = ai.metaData.getBoolean(name);
        } catch (Exception e) {
            Log.d("CPOrm", "Couldn't find config value: " + name);
        }

        return value;
    }
}
