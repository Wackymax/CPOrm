package za.co.cporm.model.query;

import android.content.Context;
import za.co.cporm.model.map.SqlColumnMappingFactory;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Filter criteria usually contain one or more filter clauses, it is a grouping of
 * queries, each separated by a Conjunction.  This entire Criteria is a grouping of its own, and
 * as such will be wrapped in parenthesis.
 */
public class DataFilterCriteria implements DataFilterClause<DataFilterCriteria>{

    private final Map<DataFilterClause, DataFilterConjunction> filterClauses;

    /**
     * Creates a new instance, using the context to determine the conversion for arguments to sql friendly format
     */
    public DataFilterCriteria(){
        filterClauses = new LinkedHashMap<DataFilterClause, DataFilterConjunction>();
    }

    public DataFilterCriteria addClause(DataFilterClause clause){

        addClause(clause, null);

        return this;
    }

    @Override
    public DataFilterCriteria addClause(DataFilterClause clause, DataFilterConjunction conjunction){

        if(conjunction == null) conjunction = DataFilterConjunction.AND;

        if(clause != null)
            filterClauses.put(clause, conjunction);

        return this;
    }

    public DataFilterCriteria addCriterion(String column, DataFilterCriterion.DataFilterOperator operator, Object filterValue){

        addClause(new DataFilterCriterion(column, operator, filterValue));

        return this;
    }

    @Override
    public QueryBuilder buildWhereClause(Context context, SqlColumnMappingFactory columnMappingFactory) {

        QueryBuilder builder = new QueryBuilder();

        if(!filterClauses.isEmpty()){

            boolean isFirst = true;
            Iterator<DataFilterClause> clauseIterator = filterClauses.keySet().iterator();
            builder.append("(");
            while(clauseIterator.hasNext()) {

                DataFilterClause clause = clauseIterator.next();
                if(!isFirst){
                    builder.append(filterClauses.get(clause).toString());
                    builder.append(" ");
                }
                else isFirst = false;

                builder.append(clause.buildWhereClause(context, columnMappingFactory));

                if(clauseIterator.hasNext()) builder.append(" ");
            }

            builder.append(")");
        }

        return builder;
    }

    @Override
    public String getWhereClause() {

        QueryBuilder builder = new QueryBuilder();

        if(hasFilterValue()){

            DataFilterClause previousClause = null;
            Iterator<DataFilterClause> clauseIterator = filterClauses.keySet().iterator();
            builder.append("(");
            while(clauseIterator.hasNext()) {

                DataFilterClause clause = clauseIterator.next();
                if(!clause.hasFilterValue())
                    continue;

                if(previousClause != null && previousClause.hasFilterValue()){
                    builder.append(filterClauses.get(previousClause).toString());
                    builder.append(" ");
                }

                builder.append(clause.getWhereClause());
                previousClause = clause;

                if(clauseIterator.hasNext()) builder.append(" ");
            }

            builder.append(")");
        }

        return builder.toString();
    }

    @Override
    public boolean hasFilterValue() {

        boolean hasFilterValue = false;

        for (DataFilterClause clause : filterClauses.keySet()) {

            hasFilterValue = hasFilterValue || clause.hasFilterValue();
        }
        return hasFilterValue;
    }

    public static class Builder<T extends DataFilterClause<T>> implements DataFilterClause<Builder<T>>{

        private final T originator;
        private final DataFilterConjunction conjunction;
        private final DataFilterCriteria criteria;

        protected Builder(T originator, DataFilterConjunction conjunction){

            this.originator = originator;
            this.conjunction = conjunction;
            this.criteria = new DataFilterCriteria();
        }

        public DataFilterCriterion.Builder<Builder<T>> and(){
            return new DataFilterCriterion.Builder<Builder<T>>(this, DataFilterClause.DataFilterConjunction.AND);
        }

        public DataFilterCriteria.Builder<T> and(DataFilterClause dataFilterClause){

            return addClause(dataFilterClause, DataFilterConjunction.AND);
        }

        public DataFilterCriterion.Builder<DataFilterCriteria.Builder<T>> or(){
            return new DataFilterCriterion.Builder<Builder<T>>(this, DataFilterClause.DataFilterConjunction.OR);
        }

        public DataFilterCriteria.Builder<T> or(DataFilterClause dataFilterClause){
            return addClause(dataFilterClause, DataFilterConjunction.OR);
        }

        public Builder<Builder<T>> openBracketAnd(){
            return new Builder<Builder<T>>(this, DataFilterConjunction.AND);
        }

        public Builder<Builder<T>> openBracketOr(){
            return new Builder<Builder<T>>(this, DataFilterConjunction.OR);
        }

        public T closeBracket(){

            originator.addClause(criteria, conjunction);
            return originator;
        }

        @Override
        public QueryBuilder buildWhereClause(Context context, SqlColumnMappingFactory columnMappingFactory) {
            throw new UnsupportedOperationException("This cannot be called on a builder");
        }

        @Override
        public String getWhereClause() {
            throw new UnsupportedOperationException("This cannot be called on a builder");
        }

        @Override
        public Builder<T> addClause(DataFilterClause clause, DataFilterConjunction conjunction) {

            criteria.addClause(clause, conjunction);

            return this;
        }

        @Override
        public boolean hasFilterValue() {

            return criteria.hasFilterValue();
        }
    }
}
