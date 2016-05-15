package za.co.cporm.model;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Path;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import za.co.cporm.model.generate.TableDetails;
import za.co.cporm.model.generate.TableGenerator;
import za.co.cporm.model.generate.TableView;
import za.co.cporm.model.generate.TableViewGenerator;
import za.co.cporm.model.util.CPOrmCursorFactory;
import za.co.cporm.model.util.TableDetailsCache;
import za.co.cporm.util.CPOrmLog;

/**
 * Handles the creation of the database and all of its objects
 */
public class CPOrmDatabase extends SQLiteOpenHelper {

    private final Context context;
    private final CPOrmConfiguration cPOrmConfiguration;
    private final TableDetailsCache tableDetailsCache;

    public CPOrmDatabase(Context context, CPOrmConfiguration cPOrmConfiguration) {
        super(context, cPOrmConfiguration.getDatabaseName(), new CPOrmCursorFactory(cPOrmConfiguration.isQueryLoggingEnabled()), cPOrmConfiguration.getDatabaseVersion());
        this.cPOrmConfiguration = cPOrmConfiguration;
        this.context = context;
        this.tableDetailsCache = new TableDetailsCache();
        this.tableDetailsCache.init(context, cPOrmConfiguration.getDataModelObjects());
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        for (Class<?> dataModelObject : cPOrmConfiguration.getDataModelObjects()) {

            if (TableView.class.isAssignableFrom(dataModelObject)) {
                String createStatement = TableViewGenerator.createViewStatement(findTableDetails(dataModelObject), (Class<? extends TableView>) dataModelObject);

                if (cPOrmConfiguration.isQueryLoggingEnabled()) {
                    CPOrmLog.d("Creating View: " + createStatement);
                }
                sqLiteDatabase.execSQL(createStatement);
            } else {
                String createStatement = TableGenerator.generateTableCreate(findTableDetails(dataModelObject), cPOrmConfiguration.isQueryLoggingEnabled());

                if (cPOrmConfiguration.isQueryLoggingEnabled()) {
                    CPOrmLog.d("Creating Table: " + createStatement);
                }
                sqLiteDatabase.execSQL(createStatement);

                for (String index : TableGenerator.generateIndecesCreate(findTableDetails(dataModelObject), cPOrmConfiguration.isQueryLoggingEnabled())) {

                    if (cPOrmConfiguration.isQueryLoggingEnabled()) {
                        CPOrmLog.d("Creating Index: " + createStatement);
                    }
                    sqLiteDatabase.execSQL(index);
                }
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        String upgradeDir = cPOrmConfiguration.upgradeResourceDirectory();
        if (!TextUtils.isEmpty(upgradeDir)) {

            boolean upgraded = false;
            String[] scripts = null;
            try {
                scripts = context.getResources().getAssets().list(upgradeDir);
            } catch (IOException e) {
                CPOrmLog.e("Failed to load upgrade scripts", e);
            }

            if (scripts != null) {
                for (String script : scripts) {

                    try {
                        upgraded = upgradeFromScript(sqLiteDatabase, oldVersion, newVersion, script);
                    } catch (Exception e) {
                        CPOrmLog.e("Failed to execute upgrade script " + script, e);
                        CPOrmLog.e("Recreating database");
                        upgraded = false;
                    }
                }
            }
            if (upgraded) {
                return;
            }
        }

        //First check if scripts are available
        for (Class<?> dataModelObject : cPOrmConfiguration.getDataModelObjects()) {

            if (TableView.class.isAssignableFrom(dataModelObject)) {
                String statement = TableViewGenerator.createDropViewStatement(findTableDetails(dataModelObject));
                if (cPOrmConfiguration.isQueryLoggingEnabled()) {
                    CPOrmLog.d("Dropping View: " + statement);
                }
                sqLiteDatabase.execSQL(statement);
            } else {
                String statement = TableGenerator.generateTableDrop(findTableDetails(dataModelObject), false);
                if (cPOrmConfiguration.isQueryLoggingEnabled()) {
                    CPOrmLog.d("Dropping Table: " + statement);
                }
                sqLiteDatabase.execSQL(statement);
            }
        }

        onCreate(sqLiteDatabase);
    }

    private boolean upgradeFromScript(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion, String script) throws IOException {
        if (script.length() > 0) {

            Matcher matcher = Pattern.compile("^[0-9]+").matcher(script);

            if (matcher.find()) {

                String scriptFile = cPOrmConfiguration.upgradeResourceDirectory() + File.separator + script;
                Integer number = Integer.valueOf(matcher.group());

                if (number > oldVersion && number <= newVersion) {

                    BufferedReader scriptStream = new BufferedReader(new InputStreamReader(context.getResources().getAssets().open(scriptFile)));
                    try {

                        StringBuilder scriptContent = new StringBuilder();
                        String line;
                        while ((line = scriptStream.readLine()) != null) {
                            scriptContent.append(line);
                        }

                        CPOrmLog.i("Executing upgrade script " + scriptFile);
                        sqLiteDatabase.execSQL(scriptContent.toString());
                    } finally {
                        scriptStream.close();
                    }
                    return number == newVersion;
                }
            } else
                CPOrmLog.e("Failed to parse upgrade script " + script + ", requires a starting integer indicating the version number");
        }

        return false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onConfigure(SQLiteDatabase db) {

        super.onConfigure(db);
        if (!db.isReadOnly()) {
            db.enableWriteAheadLogging();
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {

        super.onOpen(db);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN && Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {

            db.enableWriteAheadLogging();
        }
    }

    private TableDetails findTableDetails(Class<?> object) {

        return tableDetailsCache.findTableDetails(context, object);
    }

    /**
     * Returns the table details cache that can be used to lookup table details for java objects.  This
     * should be used instead of {@link za.co.cporm.model.generate.ReflectionHelper}, so that we do not
     * try to do reflection to much
     *
     * @return The table details cache object that can be used to obtain table details
     */
    public TableDetailsCache getTableDetailsCache() {
        return tableDetailsCache;
    }

    /**
     * @return the model factory that contains all of the model objects. This should be accessed sparingly.
     */
    public CPOrmConfiguration getcPOrmConfiguration() {
        return cPOrmConfiguration;
    }
}
