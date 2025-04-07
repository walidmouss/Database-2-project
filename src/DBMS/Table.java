package DBMS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class Table implements Serializable {
    private String tableName;
    private String[] columnNames;
    private ArrayList<Page> pages;
    //private transient ArrayList<String> traceLog;

    public Table(String tableName, String[] columnNames) {
        this.tableName = tableName;
        this.columnNames = columnNames;
        this.pages = new ArrayList<>();
        //this.traceLog = new ArrayList<>(); 
    }

    public String getTableName() {
        return tableName;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public ArrayList<Page> getPages() {
        return pages;
    }

    public void setPages(ArrayList<Page> pages) {
        this.pages = pages;
    }

}
