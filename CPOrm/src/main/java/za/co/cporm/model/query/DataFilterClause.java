package za.co.cporm.model.query;

import android.content.Context;

import java.io.Serializable;

/**
 * Defines a basic clause interface, that can be used to join queries together
 */
public interface DataFilterClause extends Serializable{

    /** The filter conjunction, this is equal to SQL AND and OR */
    public enum DataFilterConjunction{AND, OR};

    /** The where clause for this query */
    QueryBuilder buildWhereClause(Context context);

    /** The where clause for this query, without parameters */
    String getWhereClause();

    void addClause(DataFilterClause clause, DataFilterConjunction conjunction);
}
