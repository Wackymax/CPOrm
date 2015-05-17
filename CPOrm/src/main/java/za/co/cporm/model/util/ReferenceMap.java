package za.co.cporm.model.util;

import android.content.Context;
import za.co.cporm.model.CPOrm;
import za.co.cporm.model.annotation.References;
import za.co.cporm.model.generate.ReflectionHelper;

import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.util.IdentityHashMap;

/**
 * Created by hennie.brink on 2015-05-17.
 */
public class ReferenceMap extends IdentityHashMap<Class<?>, SoftReference<Object>> {

    private final Object referenceObject;

    public ReferenceMap(Object referenceObject) {

        this.referenceObject = referenceObject;
    }

    @SuppressWarnings("unchecked")
    public <T> T findReferent(Context context, Class<T> referenceToFind) {

        if (containsKey(referenceToFind)) {
            SoftReference<Object> softReference = super.get(referenceToFind);
            Object referent = softReference.get();
            if(referent != null)
                return (T)referent;
        }

        for (Field field : ReflectionHelper.getAllObjectFields(referenceObject.getClass())) {
            try {
                if (field.isAnnotationPresent(References.class) && field.getAnnotation(References.class).value() == referenceToFind) {

                    if(!field.isAccessible())
                        field.setAccessible(true);

                    T reference = CPOrm.findByPrimaryKey(context, referenceToFind, field.get(referenceObject));

                    if(reference != null)
                        put(referenceToFind, new SoftReference<Object>(reference));

                    return reference;
                }
            } catch (IllegalAccessException e) {
                throw new CPOrmException("Could not access required field " + field.getName(), e);
            }
        }
        throw new CPOrmException("No Reference found to " + referenceToFind.getSimpleName() + " from " + referenceObject.getClass().getSimpleName());
    }
}
