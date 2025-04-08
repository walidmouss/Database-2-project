package DBMS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class DBApp {
    static HashMap<String, ArrayList<String>> traceMap = new HashMap<>();
    static int dataPageSize = 2;

    public static void createTable(String tableName, String[] columnsNames) {
        File tableDirectory = new File(FileManager.directory, tableName);
        if (tableDirectory.exists()) {
            System.out.println("table already exist");
            return;
        }
        Table newTable = new Table(tableName, columnsNames);
        boolean success = FileManager.storeTable(tableName, newTable);

        if (success) {
            System.out.println("table " + tableName + " created successfully");
        } else {
            System.out.println("failed to create table " + tableName);
        }
    }
    
    public static void insert(String tableName, String[] record) {
        long start = System.currentTimeMillis();
        Table table = FileManager.loadTable(tableName);
        if (table == null) {
            System.out.println("couldn't find the table so we could insert ... bye");
            return;
        }

        int pageLength = table.getPages().size();
        System.out.println("Current page count: " + pageLength);

        if(pageLength == 0)
            System.out.println("hakoona matata");

        Page newPage = new Page();

        if (pageLength == 0) {
            newPage.getRecords().add(record);
            table.getPages().add(newPage);
            FileManager.storeTablePage(tableName, 0, newPage);
            System.out.println("inserted record in new page");
        } else {
            Page lastPage = FileManager.loadTablePage(tableName, pageLength - 1);
            if (lastPage.getRecords().size() >= dataPageSize) {
                newPage.getRecords().add(record);
                table.getPages().add(newPage);
                FileManager.storeTablePage(tableName, pageLength, newPage);
                System.out.println("last page full, inserted record in new page");
            } else {
                lastPage.getRecords().add(record);
                FileManager.storeTablePage(tableName, pageLength - 1, lastPage);
                System.out.println("inserted record in existing page");
            }
        }

        FileManager.storeTable(tableName, table);
    }

    public static ArrayList<String[]> select(String tableName) {
        long start = System.currentTimeMillis();
        Table current_table = FileManager.loadTable(tableName);
        ArrayList<String[]> allRecords = new ArrayList<>();
        int pageCount = 0;

        if (current_table != null) {
            int pageIndex = 0; // to keep track of which page we're reading for trace log
            for (Page curr_page : current_table.getPages()) {
                ArrayList<String[]> pageData = curr_page.getRecords();
                allRecords.addAll(pageData);
                //System.out.println("Page " + pageIndex + ": " + pageData.size() + " record(s)");
                pageIndex++;
            }
            pageCount = current_table.getPages().size();
        }

        long end = System.currentTimeMillis();
        String log = "Select all pages: " + pageCount + ", records: " + allRecords.size()+ ", execution time (mil): " + (end - start);
        traceMap.putIfAbsent(tableName, new ArrayList<>());
        traceMap.get(tableName).add(log);

        return allRecords;
    }

    public static ArrayList<String[]> select(String tableName, int pageNumber, int recordNumber) {
        long start = System.currentTimeMillis();
        ArrayList<String[]> result = new ArrayList<>();
        Table current_table = FileManager.loadTable(tableName);

        if (current_table == null || pageNumber >= current_table.getPages().size()) {
            System.out.println("Invalid table or page number");
            return result;
        }

        Page curr_page = current_table.getPages().get(pageNumber);
        if (recordNumber >= curr_page.getRecords().size()) {
            System.out.println("Invalid record number");
            return result;
        }

        result.add(curr_page.getRecords().get(recordNumber));

        long end = System.currentTimeMillis();
        String log = "Select pointer page: " + pageNumber + ", record: " + recordNumber +", total output count: " + result.size() +", execution time (mil): " + (end - start);

        traceMap.putIfAbsent(tableName, new ArrayList<>());
        traceMap.get(tableName).add(log);
        return result;
    }

    public static ArrayList<String[]> select(String tableName, String[] cols, String[] value) {
        long start = System.currentTimeMillis();
        Table curr_table = FileManager.loadTable(tableName);
        String[] col_names = curr_table.getColumnNames();
        int[] col_index = new int[cols.length];
        for (int i = 0; i < cols.length; i++) {
            for (int j = 0; j < col_names.length; j++) {
                if (col_names[j].equals(cols[i])) 
                    col_index[i] = j;
                
            }
        }

        ArrayList<String[]> filteredRecords = new ArrayList<>();
        for (Page page : curr_table.getPages()) {
            for (String[] record : page.getRecords()) {
                boolean cond_met = true;
                for (int i = 0; i < cols.length; i++) {
                    if (!record[col_index[i]].equals(value[i])) {
                        cond_met = false;
                        break;
                    }
                }
                if (cond_met) filteredRecords.add(record);
            }
        }

        long end = System.currentTimeMillis();
        String log = "Select condition: " + Arrays.toString(cols) + " -> " + Arrays.toString(value)+ ", records: " + filteredRecords.size()+ ", execution time (mil): " + (end - start);
        if (!traceMap.containsKey(tableName)) {
            traceMap.put(tableName, new ArrayList<>());
        }
        traceMap.get(tableName).add(log);
        return filteredRecords;
    }

    public static String getFullTrace(String tableName) {
        Table table = FileManager.loadTable(tableName);
        if (table == null) return "The table doesn't exist";

        StringBuilder str = new StringBuilder();
        str.append("Table name: ").append(table.getTableName()).append(", Columns: ").append(Arrays.toString(table.getColumnNames())).append("\n");

        ArrayList<String> tableTrace = traceMap.getOrDefault(tableName, new ArrayList<>());
        for (String line : tableTrace) {
            str.append(line).append("\n");
        }

        int recordsCount = 0;
        for (Page page : table.getPages()) {
            recordsCount += page.getRecords().size();
        }

        str.append("Pages Count: ").append(table.getPages().size())
           .append(", Records Count: ").append(recordsCount).append("\n");

        return str.toString();
    }

    public static String getLastTrace(String tableName) {
        ArrayList<String> tableTrace = traceMap.get(tableName); // to get trace for the table youre asking for
        if (tableTrace == null || tableTrace.isEmpty()) {
            return "No trace available for table: " + tableName; // make sure trace is not empty for that table
        }
        return tableTrace.get(tableTrace.size() - 1);
    }

    public static void main(String[] args) throws IOException {
        String[] columns = {"id", "name", "major", "semester", "gpa"};
        createTable("potatoes", columns);

        String[] r1 = {"1", "stud1", "CS", "5", "0.9"};
        String[] r2 = {"2", "stud2", "BI", "7", "1.2"};
        String[] r3 = {"3", "stud3", "CS", "2", "2.4"};
        String[] r4 = {"4", "stud4", "DMET", "9", "1.2"};
        String[] r5 = {"5", "stud5", "BI", "4", "3.5"};

        insert("potatoes", r1);
        insert("potatoes", r2);
        insert("potatoes", r3);
        insert("potatoes", r4);
        insert("potatoes", r5);
        /*
        String[] columns2 = {"name" , "job"};
        createTable("macdonalds" , columns2);
        
        String[] s1 = {"john" , "burger flipper"};
        String[] s2 = {"hamza" , "fries stirrer"};

        insert("macdonalds", s1);
        insert("macdonalds", s2);
        
        */

        System.out.println("Output of selecting the whole table content:");
        ArrayList<String[]> result1 = select("potatoes");

        for (String[] array : result1) {
            for (String str : array) {
                System.out.print(str + " ");
            }
            System.out.println();
        }

        System.out.println("--------------------------------");
        System.out.println("Output of selecting by position:");
        ArrayList<String[]> result2 = select("potatoes", 1, 1);
        for (String[] array : result2) {
            for (String str : array) {
                System.out.print(str + " ");
            }
            System.out.println();
        }

        System.out.println("--------------------------------");
        System.out.println("Output of selecting by column condition:");
        ArrayList<String[]> result3 = select("potatoes", new String[]{"gpa"}, new String[]{"1.2"});
        for (String[] array : result3) {
            for (String str : array) {
                System.out.print(str + " ");
            }
            System.out.println();
        }

        System.out.println("--------------------------------");
        System.out.println("Full Trace of the table:");
        System.out.println(getFullTrace("potatoes"));
        

        System.out.println("--------------------------------");
        System.out.println("Full Trace of the table:");
        System.out.println(getFullTrace("macdonalds"));

        System.out.println("--------------------------------");
        System.out.println("Last Trace of the table:");
        System.out.println(getLastTrace("potatoes"));

        System.out.println("--------------------------------");
        System.out.println("Trace of the Tables Folder:");
        System.out.println(FileManager.trace());
        FileManager.reset();

        System.out.println("--------------------------------");
        System.out.println("Trace of the Tables Folder after resetting:");
        System.out.println(FileManager.trace());
    }
}
