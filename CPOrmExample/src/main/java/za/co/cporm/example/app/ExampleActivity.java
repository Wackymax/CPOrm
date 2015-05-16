package za.co.cporm.example.app;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import za.co.cporm.example.app.model.domain.Role;
import za.co.cporm.example.app.model.domain.User;
import za.co.cporm.model.CPOrm;
import za.co.cporm.model.loader.CPOrmLoader;
import za.co.cporm.model.query.Select;
import za.co.cporm.model.util.CPOrmCursor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class ExampleActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "CPOrm Example";

    private ListView listview;
    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1,
                null,
                new String[]{"user_name"},
                new int[]{android.R.id.text1},
                0);
        listview = (ListView) findViewById(R.id.listView);
        listview.setAdapter(adapter);

        getLoaderManager().initLoader(1, Bundle.EMPTY, this);

        new PopulateDataTask(this).execute();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        Select selectUser = Select.from(User.class);
        Select selectRole = Select.from(Role.class) //Specify the class to select from
                .include("_id") //Restrict the selection to only required columns
                .and() //Add a new criterion
                .greaterThan("_id", 0); //Specify the criterion column, type and value
        selectUser.and().in("role_id", selectRole); //Add role selection as inner query
        selectUser.limit(1000); //Limit the select to 1000 records

        CPOrmLoader<User> userCPOrmLoader = new CPOrmLoader<>(this, selectUser);//Give the select to the cursor loader to load the data
        userCPOrmLoader.setUpdateThrottle(200); //Set an update throttle because we will be inserting a lot of data causing frequent changes

        return userCPOrmLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        adapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        adapter.changeCursor(null);
    }

    private static class PopulateDataTask extends AsyncTask<Void, Void, Void> {

        private final Context context;

        private long testTime = TimeUnit.SECONDS.toMillis(10);

        public PopulateDataTask(Context context) {

            //Use the app context for example purpose only
            this.context = context.getApplicationContext();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            CPOrm.deleteAll(context, Role.class);
            CPOrm.deleteAll(context, User.class);

            Role role = new Role();
            role.setRoleName("role " + Select.from(Role.class).queryAsCount(context));
            role = role.insertAndReturn(context); //We need the returned object to get the database assigned id

            List<String> mobileNumbers = new ArrayList<>();
            mobileNumbers.add("12345");
            mobileNumbers.add("67890");
            //Demonstrates cursor begin notified of data source changes
            Log.i(TAG, "Testing single insert performance");
            long time = System.currentTimeMillis();
            int recordCount = 0;

            while ((System.currentTimeMillis() - time) < testTime) {
                User user = new User();
                user.setUserName("user loader " + recordCount);
                user.setGivenName("Loading " + recordCount);
                user.setFamilyName("User");
                user.setRoleId(role.getId());
                user.setMobileNumbers(mobileNumbers);
                user.insert(context);
                recordCount++;
            }

            Log.i(TAG, "Inserted " + recordCount + " records in " + (System.currentTimeMillis() - time) + " seconds");
            Log.i(TAG, "Inserted " + (recordCount / TimeUnit.MILLISECONDS.toSeconds(testTime)) + " records in 1 second");

            int batchSize = 500;
            Log.i(TAG, "Testing batch insert performance, " + 200 + " record batch size");
            time = System.currentTimeMillis();
            recordCount = 0;

            while ((System.currentTimeMillis() - time) < testTime) {

                List<User> recordsToInsert = new ArrayList<>();
                for (int i = 1; i <= batchSize; i++) {

                    User user = new User();
                    user.setUserName("user loader batch " + (recordCount + i));
                    user.setGivenName("Loading batch " + (recordCount + i));
                    user.setFamilyName("User");
                    user.setRoleId(role.getId());
                    recordsToInsert.add(user);
                }

                recordCount += CPOrm.insertAll(context, recordsToInsert);
                recordsToInsert.clear();
            }

            Log.i(TAG, "Inserted " + recordCount + " records in " + (System.currentTimeMillis() - time) + " seconds");
            Log.i(TAG, "Inserted " + (recordCount / TimeUnit.MILLISECONDS.toSeconds(testTime)) + " records in 1 second");

            Log.i(TAG, "Testing single update performance");
            User userToUpdate = Select.from(User.class).first(context);
            recordCount = 0;
            time = System.currentTimeMillis();

            while ((System.currentTimeMillis() - time) < testTime) {

                userToUpdate.setFamilyName("User Updated");
                userToUpdate.update(context);
                recordCount++;
            }

            Log.i(TAG, "Updated " + recordCount + " records in " + (System.currentTimeMillis() - time) + " seconds");
            Log.i(TAG, "Updated " + (recordCount / TimeUnit.MILLISECONDS.toSeconds(testTime)) + " records in 1 second");

            Log.i(TAG, "Testing read performance (No Cache)");
            CPOrmCursor<User> cursor = Select.from(User.class).limit(1000).queryAsCursor(context);
            recordCount = 0;
            time = System.currentTimeMillis();

            while ((System.currentTimeMillis() - time) < testTime) {

                if (cursor.moveToNext()) {

                    User retrievedUser = cursor.inflate();
                    recordCount++;
                } else {

                    cursor.moveToFirst();
                }
            }
            cursor.close();

            Log.i(TAG, "Read " + recordCount + " records in " + (System.currentTimeMillis() - time) + " seconds");
            Log.i(TAG, "Read " + (recordCount / TimeUnit.MILLISECONDS.toSeconds(testTime)) + " records in 1 second");


            Log.i(TAG, "Testing read performance (Cache Enabled - Random Read)");
            cursor = Select.from(User.class).limit(1000).queryAsCursor(context);
            cursor.enableCache();
            int cursorCount = cursor.getCount();
            recordCount = 0;
            time = System.currentTimeMillis();

            while ((System.currentTimeMillis() - time) < testTime) {

                int position = (int) (Math.random() * cursorCount);
                if (position == cursorCount)
                    position = 0;

                if (cursor.moveToPosition(position)) {

                    User retrievedUser = cursor.inflate();
                    recordCount++;
                } else {

                    cursor.moveToFirst();
                }
            }
            cursor.close();

            Log.i(TAG, "Read " + recordCount + " records in " + (System.currentTimeMillis() - time) + " seconds");
            Log.i(TAG, "Read " + (recordCount / TimeUnit.MILLISECONDS.toSeconds(testTime)) + " records in 1 second");


            Log.i(TAG, "Testing read performance (Cache Enabled - 200 objects - Random Read)");
            cursor = Select.from(User.class).limit(1000).queryAsCursor(context);
            cursor.enableCache(200);
            cursorCount = cursor.getCount();
            recordCount = 0;
            time = System.currentTimeMillis();

            while ((System.currentTimeMillis() - time) < testTime) {

                int position = (int) (Math.random() * cursorCount);
                if (position == cursorCount)
                    position = 0;

                if (cursor.moveToPosition(position)) {

                    User retrievedUser = cursor.inflate();
                    recordCount++;
                } else {

                    cursor.moveToFirst();
                }
            }
            cursor.close();

            Log.i(TAG, "Read " + recordCount + " records in " + (System.currentTimeMillis() - time) + " seconds");
            Log.i(TAG, "Read " + (recordCount / TimeUnit.MILLISECONDS.toSeconds(testTime)) + " records in 1 second");

            User first = Select.from(User.class).first(context);
            Log.i(TAG, "Found referenced record: " + first.findByReference(context, Role.class));
            Log.i(TAG, "Found referenced record: " + first.findByReference(context, Role.class));
            Log.i(TAG, "Performance tests complete");
            return null;
        }
    }
}
