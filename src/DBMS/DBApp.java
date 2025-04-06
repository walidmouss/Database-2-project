package DBMS;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

public class DBApp
{
	static int dataPageSize = -100;
	
	public static void createTable(String tableName, String[] columnsNames)
	{
		 File tableDirectory = new File(FileManager.directory , tableName);
		    if (tableDirectory.exists()) {
		        System.out.println("table already exist");
		        return;
		    }
		    Table newTable = new Table(tableName , columnsNames);
		    boolean success = FileManager.storeTable(tableName , newTable);
		    
		    if (success) {
		        System.out.println("table" + tableName + "created successfull");
		    } else {
		        System.out.println("failed to create table" + tableName);
		    }
		
	}
	
	public static void insert(String tableName, String[] record)
	{
		Table table = FileManager.loadTable(tableName);
        if (table == null) 
        	return;

        if (table.getPages().isEmpty() || table.getPages().get(table.getPages().size() - 1).getRecords().size() >= dataPageSize) {
            Page newPage = new Page();
            table.getPages().add(newPage);
        }
		
	}
	
	public static ArrayList<String []> select(String tableName)
	{
		Table current_table = FileManager.loadTable(tableName);
	    ArrayList<String[]> allPages = new ArrayList<>(); // thi is just an empty array to hold the record from all pages
	    if (current_table != null){
	    	for(Page curr_page : current_table.getPages()){ // loop on each pages in curr page
	    		ArrayList<String[]> pageData = curr_page.getRecords(); // store data of each page in an array
	    		allPages.addAll(pageData); // store all the data from all the page
	    	}
	    }
		return allPages;
		
	}
	
	public static ArrayList<String []> select(String tableName, int pageNumber, int recordNumber)
	{
		ArrayList<String[]> result = new ArrayList<>(); // placeholder cuz for some reason we need to store this in an array not just a string 
		Table current_table = FileManager.loadTable(tableName);
		ArrayList<Page> allPages = current_table.getPages(); // this stores all pages in table
		Page curr_page = allPages.get(pageNumber);
		String[] curr_record = curr_page.getRecords().get(recordNumber);
		result.add(curr_record);
		return result;
		
		///////////////////////////////////////////////////////////////////////////////////
		////////////////// THE PREVIOUS PART NEEDS IF ELSE FOR ERROR TRACKING /////////////
		///////////////////////////////////////////////////////////////////////////////////
		
	}
	
	public static ArrayList<String []> select(String tableName, String[] cols, String[] vals)
	{
		Table curr_table = FileManager.loadTable(tableName);
		String[] col_names = curr_table.getColumnNames();
		int[] col_index = new int[cols.length];
		
		for(int i=0 ; i<cols.length ; i++){
			for(int j=0 ; j<col_names.length ; j++){
				if(col_names[j].equals(cols[i])){
					col_index[i] = j;
				}
			}
		}
		
		ArrayList<Page> allPages = curr_table.getPages(); // this stores all pages in table
		ArrayList<String[]> filteredRecords = new ArrayList<>();
		for(Page page : allPages){
			int i=0;
			for(String[] record : page.getRecords()){
				if(record[col_index[i]] == vals[i]){
					filteredRecords.add(record);
				}
				i++;
			}
		}
		return filteredRecords;
	}
	
	public static String getFullTrace(String tableName)
	{
		
		return "";
	}
	
	public static String getLastTrace(String tableName)
	{
		
		return "";
	}
	
	
	public static void main(String []args) throws IOException
	{
		String[] columns = {"id", "name", "age"};
	    
	    // Creating table "Students"
	    createTable("Students", columns);
	    String[] r1 = {"1", "stud1", "SCS", "5", "0.9"};
	    insert("student", r1);
	    
	    // Verify if the table exists
	    File tableDirectory = new File(FileManager.directory, "Students");
	    
	    if (tableDirectory.exists()) {
	        System.out.println(" Table 'Students' created successfully.");
	    } else {
	        System.out.println("Table creation failed.");
	    }
	    Table loadedTable = FileManager.loadTable("Students");
	    
	    if (loadedTable != null) {
	        System.out.println("Table Loaded:");
	        System.out.println("Table Name: " + loadedTable.getTableName());
	        System.out.println("Columns: " + Arrays.toString(loadedTable.getColumnNames()));
	    } else {
	        System.out.println("Failed to load table.");
	    }
	    
	   
	}
	
	
	
}