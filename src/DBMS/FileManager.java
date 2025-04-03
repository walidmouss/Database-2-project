package DBMS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

public class FileManager 
{
	
	static String path = FileManager.class.getResource("FileManager.class").toString();
    static File directory = new File(path.substring(6,path.length()-17) + File.separator 
    		+ "Tables" + File.separator);
	
    public static boolean storeTable(String tableName, Table t)
	{
		try {
			TimeUnit.MILLISECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
		}
		File tableDirectory = new File(directory, tableName);
		tableDirectory.mkdirs();
	    File fl = new File(tableDirectory, tableName+".db");
	    
		try 
		{
			FileOutputStream fout = new FileOutputStream(fl);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(t);
			oos.close();
			return true;
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
    
    public static Table loadTable(String tableName)
	{
		try {
			TimeUnit.MILLISECONDS.sleep(1);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		File tableDirectory = new File(directory, tableName);
	    File fl = new File(tableDirectory, tableName+".db");
	    
	    Table res = null;
	    try 
		{
			FileInputStream fin = new FileInputStream(fl);
			ObjectInputStream ois = new ObjectInputStream(fin);
			res = (Table)ois.readObject();
			ois.close();
		} 
		catch (Exception e)
		{
			
		}
	    return res;
	}
    
	public static boolean storeTablePage(String tableName, int pageNumber, Page p)
	{
		try {
			TimeUnit.MILLISECONDS.sleep(1);
		} catch (InterruptedException e1) {
			
		}
		File tableDirectory = new File(directory, tableName);
		tableDirectory.mkdir();
	    File fl = new File(tableDirectory, ""+pageNumber+".db");
	    
		try 
		{
			FileOutputStream fout = new FileOutputStream(fl);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(p);
			oos.close();
			return true;
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public static Page loadTablePage(String tableName, int pageNumber)
	{
		try {
			TimeUnit.MILLISECONDS.sleep(1);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		File tableDirectory = new File(directory, tableName);
	    File fl = new File(tableDirectory, ""+pageNumber+".db");
	    
	    Page res = null;
	    try 
		{
			FileInputStream fin = new FileInputStream(fl);
			ObjectInputStream ois = new ObjectInputStream(fin);
			res = (Page)ois.readObject();
			ois.close();
		} 
		catch (Exception e)
		{
			
		}
	    return res;
	}
	
	private static void deleteDir(File file) 
	{
	    File[] contents = file.listFiles();
	    if (contents != null) {
	        for (File f : contents) 
	        {
	                deleteDir(f);
	        }
	    }
	    file.delete();
	}
	
	public static void reset()
	{
		deleteDir(directory);
		directory.mkdir();
	}
	
	private static String traceDir(File file) 
	{
		String res = file.getName();
		if(!file.getName().endsWith(".db"))
		{
			res+="{ ";
		}
	    File[] contents = file.listFiles();
	    if (contents != null) {
			Arrays.sort(contents, Comparator.comparing(File::getName));
	        for (File f : contents) 
	        {
	        	res+=traceDir(f)+" ";
	        }
	    }
	    if(!file.getName().endsWith(".db"))
		{
			res+="}";
		}
	    return res;
	}
	
	public static String trace()
	{
		return traceDir(directory);
	}
}
