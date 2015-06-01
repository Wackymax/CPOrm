package za.co.cporm.example.app;

import android.app.Application;
import za.co.cporm.model.CPOrm;

/**
 * Created by hennie.brink on 2015-05-18.
 */
public class ExampleApplication extends Application {

    @Override
    public void onCreate() {

        super.onCreate();

        CPOrm.initialize(this);
    }
}
