
package org.zajo.simplex.solver;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author pzajac
 */
public class InputParser {

    private int rows;
    private int columns;
    // list if masic indices m integers
    private int basicIndexes[];
    private int nonBasicIndexes[];
    private double bValues[];
    private double aMatrix[][];
    private double objectiveCoefs[];
    
    BufferedReader reader;

    public int[] getBasicIndexes() {
        return basicIndexes;
    }

    public int[] getNonBasicIndexes() {
        return nonBasicIndexes;
    }

    public double[] getbValues() {
        return bValues;
    }

    public double[][] getaMatrix() {
        return aMatrix;
    }

    public double[] getObjectiveCoefs() {
        return objectiveCoefs;
    }
    
    /**
     *
     * @param reader
     */
    public void parseMatrix(BufferedReader reader) throws IOException {
        this.reader = reader;
        int[] firstLine = readInts();
        rows = firstLine[0];
        columns = firstLine[1];
        basicIndexes = readInts();
        nonBasicIndexes = readInts();
        
        bValues = readFloats();
        aMatrix = new double[rows][];
        for (int i = 0 ; i < rows ; i++) {
            aMatrix[i] = readFloats();
        }
        objectiveCoefs = readFloats();
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    
    private int readInt() throws IOException {
        return  Integer.parseInt(reader.readLine());
    }

    private int[] readInts() throws IOException {
        String line = reader.readLine();
        Iterable<String> values = Splitter.on(" ").omitEmptyStrings().trimResults().split(line);
        List<String> list = Lists.newArrayList(values);
        int[] ints = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            ints[i] = Integer.parseInt(list.get(i));
        }
        return ints;
    }

    private double[] readFloats() throws IOException {
        String line = reader.readLine();
        Iterable<String> values = Splitter.on(" ").omitEmptyStrings().trimResults().split(line);
        List<String> list = Lists.newArrayList(values);
        double[] doubles = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            doubles[i] = Double.parseDouble(list.get(i));
        }
        return doubles;
    }

  
    
    int getMaxIndex() {
        return Math.max(getMaxVal(basicIndexes), getMaxVal(nonBasicIndexes));
    }
    
    private int getMaxVal(int []vals) {
        int max = -1;
        for (int i = 0; i < vals.length; i++) {
            max = Math.max(max, vals[i]);
        }
        return max;
    }
    
}
