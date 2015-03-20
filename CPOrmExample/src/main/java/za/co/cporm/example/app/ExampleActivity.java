package za.co.cporm.example.app;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import za.co.cporm.example.app.model.domain.Role;
import za.co.cporm.example.app.model.domain.User;
import za.co.cporm.model.loader.CPOrmLoader;
import za.co.cporm.model.query.Select;


public class ExampleActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>{

  private ListView listview;
  private SimpleCursorAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_example);

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
        return new CPOrmLoader<User>(this, Select.from(User.class).and().in("role_id", Select.from(Role.class).include("_id").and().greaterThan("_id", 0)));
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.changeCursor(null);
    }

    private static class PopulateDataTask extends AsyncTask<Void, Void, Void>{
        private final Context context;

        public PopulateDataTask(Context context){

            //Use the app context for example purpose only
            this.context = context.getApplicationContext();
        }

        @Override
        protected Void doInBackground(Void... voids) {


            //CPHelper.deleteAll(context, Role.class);
            //CPHelper.deleteAll(context, User.class);

            Role role = new Role();
            role.setRoleName("role " + Select.from(Role.class).queryAsCount(context));
            role = role.insertAndReturn(context); //We need the returned object to get the database assigned id

            User user = new User();
            user.setUserName("user " + Select.from(User.class).queryAsCount(context));
            user.setGivenName("John");
            user.setFamilyName("Doe");
            user.setRoleId(role.getId());
            user.insert(context);

            user.setUserName("user " + Select.from(User.class).queryAsCount(context));
            user.setGivenName("Jane");
            user.setFamilyName("Soe");
            user.setRoleId(role.getId());
            user.insert(context);

            return null;
        }
    }
}
