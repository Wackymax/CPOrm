package za.co.cporm.model.query;

import java.io.Serializable;
import java.util.*;

/**
 * This class will keep track of the query and its supplied arguments , so that when multiple
 * queries are appended, the query and arguments will always match up.
 */
public class QueryBuilder implements Serializable{

    private final StringBuilder queryBuffer;
    private final List<Object> argsStore;

    public QueryBuilder() {

        queryBuffer = new StringBuilder();
        argsStore = new LinkedList<Object>();
    }

    public QueryBuilder(String init, Object... args){

        queryBuffer = new StringBuilder(init);
        argsStore = new LinkedList<Object>(Arrays.asList(args));
    }

    public void append(String query, Object... args) {

        queryBuffer.append(query);

        for (Object arg : args) {

            argsStore.add(String.valueOf(arg));
        }
    }

    public void append(QueryBuilder queryBuilder){

        queryBuffer.append(queryBuilder.getQueryString());
        argsStore.addAll(queryBuilder.getQueryArgs());
    }

    public String getQueryString(){

        return String.valueOf(queryBuffer);
    }

    private Collection<Object> getQueryArgs(){

        List<Object> queryArgs = new LinkedList<Object>();
        queryArgs.addAll(argsStore);

        return Collections.unmodifiableCollection(queryArgs);
    }

    public String[] getQueryArgsAsArray(){

        String[] args = new String[argsStore.size()];

        for (int i = 0; i < argsStore.size(); i++) {
            args[i] = String.valueOf(argsStore.get(i));
        }
        return args;
    }

    @Override
    public String toString() {
        return getQueryString();
    }
}
