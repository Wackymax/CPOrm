package za.co.cporm.example.app.model;

import za.co.cporm.example.app.model.domain.Role;
import za.co.cporm.example.app.model.domain.User;
import za.co.cporm.model.CPOrmConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hennie.brink on 2015-03-20.
 */
public class MyCPOrmConfiguration implements CPOrmConfiguration {

    @Override
    public String getDatabaseName() {

        return "example.db";
    }

    @Override
    public int getDatabaseVersion() {

        return 3;
    }

    @Override
    public boolean recreateDatabaseOnFailedUpgrade() {
        return true;
    }

    @Override
    public boolean isQueryLoggingEnabled() {

        return false;
    }

    @Override
    public String upgradeResourceDirectory() {
        return null;
    }

    @Override
    public List<Class<?>> getDataModelObjects() {
        List<Class<?>> domainObjects = new ArrayList<Class<?>>();
        domainObjects.add(User.class);
        domainObjects.add(Role.class);

        return domainObjects;
    }
}
