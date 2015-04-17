package za.co.cporm.model.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import za.co.cporm.model.CPOrmConfiguration;
import za.co.cporm.model.map.SqlColumnMappingFactory;

/**
 * Created by hennie.brink on 2015-03-18.
 */
public class ManifestHelper {

    public static final String METADATA_AUTHORITY = "AUTHORITY";
    public static final String METADATA_CPORM_CONFIG = "CPORM_CONFIG";
    public static final String METADATA_MAPPING_FACTORY = "MAPPING_FACTORY";

    public static final String DATABASE_DEFAULT_NAME = "CPOrm.db";

    public static String getAuthority(Context context){

        String authority = getMetaDataString(context, METADATA_AUTHORITY);
        if(TextUtils.isEmpty(authority)) throw new IllegalArgumentException("Authority must be provided as part of the meta data");

        return authority;
    }

    /**
     * This will try to instantiate the configuration base on a valid
     * Java Class name.
     * @param context the {@link android.content.Context} of the Android application
     * @return The model factory specified by the {@link #METADATA_CPORM_CONFIG}
     */
    public static CPOrmConfiguration getConfiguration(Context context) throws IllegalArgumentException{
        String className = getMetaDataString(context, METADATA_CPORM_CONFIG);
        try{
            Class modelFactory = Class.forName(className);
            if(CPOrmConfiguration.class.isAssignableFrom(modelFactory)){
                return (CPOrmConfiguration)modelFactory.getConstructor().newInstance();
            }
            else throw new IllegalArgumentException("The class provided is not and instance of CPOrmConfiguration: " + className);
        } catch (Exception ex){
            throw new IllegalArgumentException("Failed to create CPOrmConfiguration instance, is the meta data tag added to the application?", ex);
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
