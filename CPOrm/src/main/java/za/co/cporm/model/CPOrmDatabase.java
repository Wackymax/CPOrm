package za.co.cporm.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import za.co.cporm.model.generate.TableDetails;
import za.co.cporm.model.generate.TableGenerator;
import za.co.cporm.model.generate.TableView;
import za.co.cporm.model.generate.TableViewGenerator;
import za.co.cporm.model.util.CPOrmCursorFactory;
import za.co.cporm.model.util.ManifestHelper;
import za.co.cporm.model.util.TableDetailsCache;

/**
 * Handles the creation of the database and all of its objects
 */
public class CPOrmDatabase extends SQLiteOpenHelper {
    private static final String TAG = "CPOrmDatabase";

    private final Context context;
    private final ModelFactory modelFactory;
    private final TableDetailsCache tableDetailsCache;

    public CPOrmDatabase(Context context) {
        super(context, ManifestHelper.getDatabaseName(context), new CPOrmCursorFactory(ManifestHelper.getDebugEnabled(context)), ManifestHelper.getDatabaseVersion(context));
        this.modelFactory = ManifestHelper.getModelFactory(context);
        this.context = context;
        this.tableDetailsCache = new TableDetailsCache();
        this.tableDetailsCache.init(context, modelFactory.getDataModelObjects());
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        for (Class<?> dataModelObject : modelFactory.getDataModelObjects()) {

            if(TableView.class.isAssignableFrom(dataModelObject)){
                String createStatement = TableViewGenerator.createViewStatement(findTableDetails(dataModelObject), (Class<? extends TableView>) dataModelObject);

                Log.d(TAG, "Creating View: " + createStatement);
                sqLiteDatabase.execSQL(createStatement);
            }
            else {
                String createStatement = TableGenerator.generateTableCreate(findTableDetails(dataModelObject), false);
                Log.d(TAG, "Creating Table: " + createStatement);
                sqLiteDatabase.execSQL(createStatement);
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        for (Class<?> dataModelObject : modelFactory.getDataModelObjects()) {

            if(TableView.class.isAssignableFrom(dataModelObject)){
                String statement = TableViewGenerator.createDropViewStatement(findTableDetails(dataModelObject));
                Log.d(TAG, "Dropping View: " + statement);
                sqLiteDatabase.execSQL(statement);
            } else {
                String statement = TableGenerator.generateTableDrop(findTableDetails(dataModelObject), false);
                Log.d(TAG, "Dropping Table: " + statement);
                sqLiteDatabase.execSQL(statement);
            }
        }

        onCreate(sqLiteDatabase);
    }

    private TableDetails findTableDetails(Class<?> object){

        return tableDetailsCache.findTableDetails(context, object);
    }

    /**
     * Returns the table details cache that can be used to lookup table details for java objects.  This
     * should be used instead of {@link za.co.cporm.model.generate.ReflectionHelper}, so that we do not
     * try to do reflection to much
     * @return
     */
    public TableDetailsCache getTableDetailsCache() {
        return tableDetailsCache;
    }

    /**
     * @return the model factory that contains all of the model objects. This should be accessed sparingly.
     */
    public ModelFactory getModelFactory() {
        return modelFactory;
    }
}
