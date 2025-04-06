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
	static int dataPageSize = 100;
	
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
        if (table == null){
        	System.out.println("couldnt find the table so we could insert ... bye");
        	return;
        }
        int pageLength = table.getPages().size() - 1;
        if (table.getPages().isEmpty() || table.getPages().get(pageLength).getRecords().size() >= dataPageSize) { // second condition checks if last page is above limit
            Page newPage = new Page();
            newPage.getRecords().add(record);
    	    ArrayList<Page> allPages = new ArrayList<>(); // set an empty array of pages
    	    allPages = table.getPages(); // fill array with current pages in table
    	    allPages.add(newPage); // add a new page
    	    
            table.setPages(allPages); // write to table
            
            
            
            FileManager.storeTable(tableName, table);
        	System.out.println(allPages);
        	System.out.println("inserted record in new page");
        }
        else{
        	ArrayList<String[]> allPageRecords = new ArrayList<>(); // set an empty array of pages
        	allPageRecords = table.getPages().get(pageLength).getRecords(); // fill array with current pages in table
        	allPageRecords.add(record); // add a new page

    	    
        	table.getPages().get(table.getPages().size() - 1).setRecords(allPageRecords);
            FileManager.storeTable(tableName, table);
        	System.out.println("inserted record in same page");
        }
        

	}
	
	
	
	
	
	
	
	
	public static ArrayList<String []> select(String tableName)
	{
		Table current_table = FileManager.loadTable(tableName);
	    ArrayList<String[]> allPages = new ArrayList<>(); // thi is just an empty array to hold the record from all pages
	    if (current_table != null){
	    	for(Page curr_page : current_table.getPages()){ // loop on each pages in curr table
	    		
	    		ArrayList<String[]> pageData = curr_page.getRecords(); // store data of each page in an array
	    		allPages.addAll(pageData); // store all the data from all the page
	    	}
	    }
		return allPages;
		
	}
	
	
	
	
	
	
	
	public static ArrayList<String []> select(String tableName, int pageNumber, int recordNumber)
	{
		ArrayList<String[]> result = new ArrayList<>(); // placeholder cuz for some reason we need to store this in an array not just a string 
		
		Page current_Page = FileManager.loadTablePage("fries", 0);
		System.out.println(current_Page);
		String[] curr_record = current_Page.getRecords().get(recordNumber);
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
			for(int j=0 ; j<cols.length ; j++){
				for(int i=0 ; i<page.getRecords().size() ; i++){
					String[] records = page.getRecords().get(i);
					if(records[col_index[j]].equals(vals[j])){
						filteredRecords.add(records);
						
					}
				}
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
		// Define the column names for the "Students" table
        String[] columns = {"id", "name", "major", "semester", "gpa"};
        
        // Step 1: Create the "Students" table
        createTable("fries", columns);
        
        // Step 2: Insert the given rows into the "Students" table
        String[] r1 = {"1", "stud1", "CS", "5", "0.9"};
        String[] r2 = {"2", "stud2", "BI", "7", "1.2"};
        String[] r3 = {"3", "stud3", "CS", "2", "2.4"};
        String[] r4 = {"4", "stud4", "DMET", "9", "1.2"};
        String[] r5 = {"5", "stud5", "BI", "4", "3.5"};
        
        insert("fries", r1);
        insert("fries", r2);
        insert("fries", r3);
        insert("fries", r4);
        insert("fries", r5);
        
        // Step 3: Select all rows from the "Students" table
        
        // Step 4: Output the entire table content
        System.out.println("Output of selecting the whole table content:");
        ArrayList<String[]> result1 = select("fries");
        for (String[] array : result1) {
        	for(String str : array){
        		System.out.print(str + " ");
        	}
        	System.out.println();
        }
        
        /*
        System.out.println("--------------------------------");
        System.out.println("Output of selecting the output by position:");
        ArrayList<String[]> result2 = select("fries", 1, 1);
        for (String[] array : result2) {
        	for (String str : array) {
        		System.out.print(str + " ");
        		
        	}
        	System.out.println();
        }
        
        */
        
        System.out.println("--------------------------------");
        System.out.println("Output of selecting the output by column condition:");

        ArrayList<String[]> result3 = select("fries", new String[]{"gpa"}, new String[]{"1.2"});

        for (String[] array : result3) {
            for (String str : array) {
                System.out.print(str + " ");
            }
            System.out.println();
        }

        
        
	}
	
	
	
}