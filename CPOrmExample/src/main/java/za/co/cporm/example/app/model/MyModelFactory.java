package za.co.cporm.example.app.model;

import za.co.cporm.example.app.model.domain.Role;
import za.co.cporm.example.app.model.domain.User;
import za.co.cporm.model.ModelFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hennie.brink on 2015-03-20.
 */
public class MyModelFactory implements ModelFactory {

    @Override
    public List<Class<?>> getDataModelObjects() {
        List<Class<?>> domainObjects = new ArrayList<Class<?>>();
        domainObjects.add(User.class);
        domainObjects.add(Role.class);

        return domainObjects;
    }
}
