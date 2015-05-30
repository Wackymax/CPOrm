package za.co.cporm.model.generate;

/**
 * Generates the statements required to drop and create views
 */
public class TableViewGenerator {

    public static String createDropViewStatement(TableDetails tableDetails){

        //        tableQuery.append(databaseName);
//        tableQuery.append(".");

        return "DROP VIEW IF EXISTS " + tableDetails.getTableName();
    }

    public static String createViewStatement(TableDetails tableDetails, Class<? extends TableView> view){

        TableView tableView;
        try {
            tableView = view.getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to instantiate view " + view.getSimpleName(), e);
        }

        return "CREATE VIEW IF NOT EXISTS " + tableDetails.getTableName() + " AS " + tableView.getTableViewSql();
    }
}
