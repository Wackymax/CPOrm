package za.co.cporm.model.query;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Filter criteria usually contain one or more filter clauses, it is a grouping of
 * queries, each separated by a Conjunction.  This entire Criteria is a grouping of its own, and
 * as such will be wrapped in parenthesis.
 */
public class DataFilterCriteria implements DataFilterClause{

    Map<DataFilterClause, DataFilterConjunction> filterClauses;

    public DataFilterCriteria(){

        filterClauses = new LinkedHashMap<DataFilterClause, DataFilterConjunction>();
    }

    public void addClause(DataFilterClause clause){

        addClause(clause, null);
    }

    @Override
    public void addClause(DataFilterClause clause, DataFilterConjunction conjunction){

        if(conjunction == null) conjunction = DataFilterConjunction.AND;

        filterClauses.put(clause, conjunction);
    }

    public void addCriterion(String column, DataFilterCriterion.DataFilterOperator operator, Object filterValue){

        addClause(new DataFilterCriterion(column, operator, filterValue));
    }

    @Override
    public QueryBuilder getWhereClause() {

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

                builder.append(clause.getWhereClause());

                if(clauseIterator.hasNext()) builder.append(" ");
            }

            builder.append(")");
        }

        return builder;
    }

    public static class Builder<T extends DataFilterClause> implements DataFilterClause{

        private final T originator;
        private final DataFilterConjunction conjunction;
        private final DataFilterCriteria criteria;

        protected Builder(T originator, DataFilterConjunction conjunction){

            this.originator = originator;
            this.conjunction = conjunction;
            this.criteria = new DataFilterCriteria();
        }

        public DataFilterCriterion.Builder<DataFilterCriteria.Builder<T>> and(){
            return new DataFilterCriterion.Builder<DataFilterCriteria.Builder<T>>(this, DataFilterClause.DataFilterConjunction.AND);
        }

        public DataFilterCriterion.Builder<DataFilterCriteria.Builder<T>> or(){
            return new DataFilterCriterion.Builder<DataFilterCriteria.Builder<T>>(this, DataFilterClause.DataFilterConjunction.OR);
        }

        public DataFilterCriteria.Builder<DataFilterCriteria.Builder<T>> obAnd(){
            return new Builder<Builder<T>>(this, DataFilterConjunction.AND);
        }

        public DataFilterCriteria.Builder<DataFilterCriteria.Builder<T>> obOr(){
            return new Builder<Builder<T>>(this, DataFilterConjunction.OR);
        }

        public T closeBracket(){

            originator.addClause(criteria, conjunction);
            return originator;
        }

        @Override
        public QueryBuilder getWhereClause() {
            throw new UnsupportedOperationException("Get where clause cannot be called on a builder");
        }

        @Override
        public void addClause(DataFilterClause clause, DataFilterConjunction conjunction) {

            criteria.addClause(clause, conjunction);
        }
    }
}
