package za.co.cporm.model.generate;

/**
 * Interface to indicate that a Java Object should be treated as a view instead of a table
 * that will be auto generated.  For that purpose, and due to the complexity that views can take,
 * the creation of the Select Statement for the view is left to implementing class.
 *
 * Note: The class should still have all of the normal {@link za.co.cporm.model.annotation.Table} annotations.
 */
public interface TableView {

    /**
     * The SELECT statement that will be used to create the view.
     */
    String getTableViewSql();
}
