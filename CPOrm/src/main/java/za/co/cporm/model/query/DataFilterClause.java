package za.co.cporm.model.query;

/**
 * Defines a basic clause interface, that can be used to join queries together
 */
public interface DataFilterClause {

    /** The filter conjunction, this is equal to SQL AND and OR */
    public enum DataFilterConjunction{AND, OR};

    /** The where clause for this query */
    QueryBuilder getWhereClause();

    void addClause(DataFilterClause clause, DataFilterConjunction conjunction);
}
