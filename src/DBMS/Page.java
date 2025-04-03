package DBMS;

import java.io.Serializable;
import java.util.ArrayList;

public class Page implements Serializable
{
	private ArrayList<String[]> records;

    public Page() {
        this.setRecords(new ArrayList<>());
    }

	public ArrayList<String[]> getRecords() {
		return records;
	}

	public void setRecords(ArrayList<String[]> records) {
		this.records = records;
	}
	
}
