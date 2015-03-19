package za.co.cporm.model.generate;

/**
 * Generates the statements required to drop and create views
 */
public class TableViewGenerator {

    public static String createDropViewStatement(TableDetails tableDetails){

        StringBuilder tableQuery = new StringBuilder();

        tableQuery.append("DROP VIEW IF EXISTS ");
//        tableQuery.append(databaseName);
//        tableQuery.append(".");
        tableQuery.append(tableDetails.getTableName());

        return tableQuery.toString();
    }

    public static String createViewStatement(TableDetails tableDetails, Class<? extends TableView> view){

        TableView tableView = null;
        try {
            tableView = view.getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to instantiate view " + view.getSimpleName(), e);
        }

        StringBuilder tableQuery = new StringBuilder();

        tableQuery.append("CREATE VIEW IF NOT EXISTS ");
        tableQuery.append(tableDetails.getTableName());
        tableQuery.append(" AS ");
        tableQuery.append(tableView.getTableViewSql());

        return tableQuery.toString();
    }
}
