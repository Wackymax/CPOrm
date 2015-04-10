package za.co.cporm.model.util;

import android.content.Context;
import za.co.cporm.model.generate.ReflectionHelper;
import za.co.cporm.model.generate.TableDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class will maintain a cache of all the java objects and their relevant table details,
 * the main function of this is to reduce the amount of times we have to use reflection to get the table details.
 * All of the table details are loaded on demand.  The methods on this class is synchronized to prevent
 * multiple threads from altering the cache at the same time.
 */
public class TableDetailsCache {

    private final Map<Class<?>, TableDetails> cache;

    public TableDetailsCache(){

        cache = new HashMap<Class<?>, TableDetails>();
    }

    /**
     * Initializes the cache with all of the supplied entries
     * @param objects The objects for which to retrieve table details
     */
    public synchronized void init(Context context, List<Class<?>> objects){

        for (Class<?> object : objects) {
            findTableDetails(context, object);
        }
    }

    /**
    * Attempts to find the table details for the supplied object from the local cache.
    * @param object The object to find the table details for
    * @return The {@link za.co.cporm.model.generate.TableDetails} for the supplied object if it is found
    */
    public synchronized TableDetails findTableDetails(Context context, Class<?> object){

        if(!cache.containsKey(object)){
            try {
                cache.put(object, ReflectionHelper.getTableDetails(context, object));
            }
            catch (Exception ex){
                throw new IllegalArgumentException("Failed load table details for object " + object.getSimpleName(), ex);
            }

            //Check if it exists after we attempted to add it
            if(!cache.containsKey(object)) throw new IllegalArgumentException("No table details could be found for supplied object: " + object.getSimpleName());
        }

        return cache.get(object);
    }
}
