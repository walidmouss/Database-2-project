package DBMS;
import java.io.Serializable;
import java.util.*;

public class BitmapIndex implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, BitSet> indexMap;
    private int size;

    public BitmapIndex() {
        this.indexMap = new HashMap<>();
        this.size = 0;
    }

    // Build index from a list of values (e.g., from one column across all rows)
    public void buildIndex(List<String> columnValues) {
        indexMap.clear();
        size = columnValues.size();

        for (int i = 0; i < size; i++) {
            String value = columnValues.get(i);
            indexMap.putIfAbsent(value, new BitSet(size));
            indexMap.get(value).set(i);
        }
    }

    // Get the bitstring representation of the positions where a value appears
    public String getBitString(String value) {
        BitSet bitSet = indexMap.get(value);
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < size; i++) {
            sb.append(bitSet != null && bitSet.get(i) ? '1' : '0');
        }

        return sb.toString();
    }

    public Map<String, BitSet> getIndexMap() {
        return indexMap;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
