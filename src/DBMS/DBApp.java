package DBMS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class DBApp {
    static HashMap<String, ArrayList<String>> traceMap = new HashMap<>();
    static int dataPageSize = 2;
    static String table_name = "hakoona matata";

    public static void createTable(String tableName, String[] columnsNames) {
        File tableDirectory = new File(FileManager.directory, tableName);
        if (tableDirectory.exists()) {
            System.out.println("table already exist");
            return;
        }
        Table newTable = new Table(tableName, columnsNames);
        boolean success = FileManager.storeTable(tableName, newTable);

        if (!success) {
        	System.out.println("failed to create table " + tableName);
            //System.out.println("table " + tableName + " created successfully");
        }

        StringBuilder str = new StringBuilder();
        str.append("Table created name:").append(tableName).append(", columnsNames:").append(Arrays.toString(columnsNames));

        traceMap.put(tableName, new ArrayList<>(Collections.singletonList(str.toString())));

    
    
    }
    
    
    
    ///////////////////////////////////////////////////////////////////////
    /////////////////////////////INSERT FUNCTION///////////////////////////
    ///////////////////////////////////////////////////////////////////////
    
    
    
    
    static int currentPage = 0; //this is the page we insert in
    public static void insert(String tableName, String[] record) {
         
    	if(table_name != tableName){
    		currentPage = 0;
    		table_name = tableName;
    	}
    	
    	long start = System.currentTimeMillis();
        Table table = FileManager.loadTable(tableName);
        if (table == null) {
            System.out.println("couldn't find the table so we could insert ... bye");
            return;
        }

        int pageLength = table.getPages().size();
        //System.out.println("Current page count: " + pageLength);

        Page newPage = new Page();
        Page lastPage = new Page();
        if (pageLength == 0) {
            newPage.getRecords().add(record);
            table.getPages().add(newPage);
            FileManager.storeTablePage(tableName, 0, newPage);
            //System.out.println("inserted record in new page");
        } else {
        	if(FileManager.loadTablePage(tableName, currentPage) == null){

                System.out.println("null pointer for page ya bashmohandes");
                
        	}
        	else{
        		lastPage = FileManager.loadTablePage(tableName, currentPage);
        	}
            if (lastPage.getRecords().size() == dataPageSize) {
                newPage.getRecords().add(record);
                table.getPages().add(newPage);
                currentPage ++;
                FileManager.storeTablePage(tableName, currentPage, newPage);
                //System.out.println("last page full, inserted record in new page");
            } else {
                lastPage.getRecords().add(record);
                FileManager.storeTablePage(tableName, currentPage, lastPage);
                //System.out.println("inserted record in existing page");
            }
        }

        //System.out.println("CURRENT PAGES ARE : " + table.getPages());
        FileManager.storeTable(tableName, table);
        long end = System.currentTimeMillis();
        String log = "Inserted:" + Arrays.toString(record) + ", at page number:" + currentPage + ", execution time (mil): "+(end-start);

        traceMap.putIfAbsent(tableName, new ArrayList<>());
        traceMap.get(tableName).add(log);
    }

    public static ArrayList<String[]> select(String tableName) {
        long start = System.currentTimeMillis();
        Table current_table = FileManager.loadTable(tableName);
        ArrayList<String[]> allRecords = new ArrayList<>();
        int pageCount = 0;

        if (current_table != null) {
            int pageIndex = 0; // to keep track of which page we're reading for trace log

            //System.out.println("CURRENT PAGE SIZE : " + current_table.getPages().size());
            for (int i=0 ; i< current_table.getPages().size(); i++) {
                //System.out.println("CURRENT PAGES NUMBER IS : " + i);
            	
            	Page curr_page = FileManager.loadTablePage(tableName, i);
            	ArrayList<String[]> pageData;
            	//if(curr_page != null){  
            	pageData = curr_page.getRecords();
                 allRecords.addAll(pageData);
//            	}
                //System.out.println("Page " + pageIndex + ": " + pageData.size() + " record(s)");
                pageIndex++;
            }
            pageCount = current_table.getPages().size();
        }

        long end = System.currentTimeMillis();
        String log = "Select all pages:" + pageCount + ", records:" + allRecords.size()+ ", execution time (mil):" + (end - start);
        traceMap.putIfAbsent(tableName, new ArrayList<>());
        traceMap.get(tableName).add(log);

        return allRecords;
    }


    ///////////////////////////////////////////////////////////////////////
    /////////////////////////////SELECT WHOLE TABLE////////////////////////
    ///////////////////////////////////////////////////////////////////////
    
    public static ArrayList<String[]> select(String tableName, int pageNumber, int recordNumber) {
        long start = System.currentTimeMillis();
        ArrayList<String[]> result = new ArrayList<>();
        
        Table current_table = FileManager.loadTable(tableName);
        if (current_table == null || pageNumber < 0 || pageNumber >= current_table.getPages().size()) {
            System.out.println("Invalid table or page number");
            return result;
        }

        Page curr_page = FileManager.loadTablePage(tableName, pageNumber);
        if (curr_page == null) {
            System.out.println("Page data is missing (null)");
            return result;
        }

        if (recordNumber < 0 || recordNumber >= curr_page.getRecords().size()) {
            System.out.println("Invalid record number: " + recordNumber + " for page size " + curr_page.getRecords().size());
            return result;
        }

        result.add(curr_page.getRecords().get(recordNumber));

        long end = System.currentTimeMillis();
        String log = "Select pointer page:" + pageNumber + ", record:" + recordNumber +
                     ", total output count:" + result.size() + ", execution time (mil):" + (end - start);

        traceMap.putIfAbsent(tableName, new ArrayList<>());
        traceMap.get(tableName).add(log);
        return result;
    }


    
    ///////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////SELECT WHOLE TABLE///////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    
    
    public static ArrayList<String[]> select(String tableName, String[] cols, String[] value) {
        long start = System.currentTimeMillis();
        Table curr_table = FileManager.loadTable(tableName);
        String[] col_names = curr_table.getColumnNames();
        int[] col_index = new int[cols.length];
        for (int i = 0; i < cols.length; i++) {
            for (int j = 0; j < col_names.length; j++) {
                if (col_names[j].equals(cols[i])) 
                    col_index[i] = j; // stores position of cols in actual collumn table
                
            }
        }
        
        ArrayList<ArrayList<Integer>> recordPerPage = new ArrayList<>();
        ArrayList<Integer> helperArray = new ArrayList<>();
    	ArrayList<String[]> filteredRecords = new ArrayList<>();
        
        
        for (int j=0 ; j<curr_table.getPages().size() ; j++) { //loops on pages in table
        	int recordsCounter = 0;
        	Page page = FileManager.loadTablePage(tableName, j);
            for (String[] record : page.getRecords()) {// loops on each record in current page
                boolean cond_met = false;
                for (int i = 0; i < cols.length; i++) {
                    if (record[col_index[i]].equals(value[i])) {
                        cond_met = true;
                        //System.out.println("record [col_index] : " + record[col_index[i]]);
                        //System.out.println("value[i] : " + value[i]);
                        //System.out.println("page (j) : " + j);
                        recordsCounter++; 
                        helperArray = new ArrayList<>();
                        helperArray.add(0, j);
                        helperArray.add(1, recordsCounter);
                        recordPerPage.add(helperArray);
                        
                        //System.out.println("helper array : " + helperArray);
                        //System.out.println("page we are storing in : " + j);
                        //System.out.println("records count : " + recordPerPage);
                        //System.out.println();
                        break;
                    }
                }
                if (cond_met) filteredRecords.add(record);
            }
        }
 
        long end = System.currentTimeMillis();
        String log = "Select condition:" + Arrays.toString(cols) + "->" + Arrays.toString(value)+", Records per page:" + recordPerPage + ", records:" + filteredRecords.size()+ ", execution time (mil): " + (end - start);
        if (!traceMap.containsKey(tableName)) {
            traceMap.put(tableName, new ArrayList<>());
        }
        traceMap.get(tableName).add(log);
        return filteredRecords;
    }

    

    ///////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////GET FULL TRACE/////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    
    
    public static String getFullTrace(String tableName) {
        Table table = FileManager.loadTable(tableName);
        if (table == null) return "The table doesn't exist";

        StringBuilder str = new StringBuilder();
        ArrayList<String> tableTrace = traceMap.getOrDefault(tableName, new ArrayList<>());
        for (String line : tableTrace) {
            str.append(line).append("\n");
        }

        int recordsCount = 0;
        for (int i=0 ; i< table.getPages().size(); i++) {
        	Page page = FileManager.loadTablePage(tableName, i);
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
        createTable("pepsi", columns);

        String[] r1 = {"1", "stud1", "CS", "5", "0.9"};
        String[] r2 = {"2", "stud2", "BI", "7", "1.2"};
        String[] r3 = {"3", "stud3", "CS", "2", "2.4"};
        String[] r4 = {"4", "stud4", "DMET", "9", "1.2"};
        String[] r5 = {"5", "stud5", "BI", "4", "3.5"};

        insert("pepsi", r1);
        insert("pepsi", r2);
        insert("pepsi", r3);
        insert("pepsi", r4);
        insert("pepsi", r5);
        
        
        

        System.out.println("Output of selecting the whole table content:");
        ArrayList<String[]> result1 = select("pepsi");

        for (String[] array : result1) {
            for (String str : array) {
                System.out.print(str + " ");
            }
            System.out.println();
        }

        System.out.println("--------------------------------");
        System.out.println("Output of selecting by position:");
        ArrayList<String[]> result2 = select("pepsi", 1, 1);
        for (String[] array : result2) {
            for (String str : array) {
                System.out.print(str + " ");
            }
            System.out.println();
        }

        System.out.println("--------------------------------");
        System.out.println("Output of selecting the output by column condition:");
        ArrayList<String[]> result3 = select("pepsi", new String[]{"gpa"}, new String[]{"1.2"});
        for (String[] array : result3) {
            for (String str : array) {
                System.out.print(str + " ");
            }
            System.out.println();
        }

        System.out.println("--------------------------------");
        System.out.println("Full Trace of the table:");
        System.out.println(getFullTrace("pepsi"));
        

        System.out.println("--------------------------------");
        System.out.println("Last Trace of the table:");
        System.out.println(getLastTrace("pepsi"));

        System.out.println("--------------------------------");
        System.out.println("Trace of the Tables Folder:");
        System.out.println(FileManager.trace());
        FileManager.reset();

        System.out.println("--------------------------------");
        System.out.println("Trace of the Tables Folder after resetting:");
        System.out.println(FileManager.trace());
    }
}
