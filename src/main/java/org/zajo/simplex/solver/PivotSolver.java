package org.zajo.simplex.solver;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;

/**
 *
 * @author pzajac
 */
public class PivotSolver {
    public static final double DELTA = 1e-6;

    List<Vector> rows;
    int[] basicVars;
    Vector lastList;
    private final InputParser parser;
    
    Status status = Status.OK;
    
    int enteringIndex = -1;
    int leavingIndex = -1;
    
    int oldEnteringIndex = -1;
    int oldLeavingIndex = -1;
    int iterations;
    
    Set<Integer> allVars = Sets.newHashSet();
    
    
    public PivotSolver(InputParser parser) {
        this.parser = parser;
        
        int maxIndex = parser.getMaxIndex();
        rows = Lists.newArrayList();
        int rowIdx = 0;
        for (double[] rowVals :  parser.getaMatrix()) {
            Vector row = new DenseVector(maxIndex + 1);
            initRow(parser, rowVals,0, row);
            row.set(0, parser.getbValues()[rowIdx++]);
            rows.add(row);
        }
        int[] basicIndexes = parser.getBasicIndexes();
        basicVars = new int[basicIndexes.length];
        for (int i = 0; i < basicIndexes.length; i++) {
            basicVars[i] = basicIndexes[i];
        }
        
        lastList = new DenseVector(maxIndex + 1);
        double[] objectiveCoefs = parser.getObjectiveCoefs();
        lastList.set(0, objectiveCoefs[0]);
        initRow(parser, objectiveCoefs, 1, lastList);
        
        addToAllVars(parser.getBasicIndexes());
        addToAllVars(parser.getNonBasicIndexes());
    }

    private PivotSolver(PivotSolver solver) {
        parser = solver.parser;
         int maxIndex = parser.getMaxIndex();
        rows = Lists.newArrayList();
        int rowIdx = 0;
        for (double[] rowVals :  parser.getaMatrix()) {
            Vector row = new DenseVector(maxIndex + 2);
            initRow(parser, rowVals,0, row);
            row.set(0, parser.getbValues()[rowIdx++]);
            row.set(maxIndex + 1, 1);
            rows.add(row);
        }
        int[] basicIndexes = parser.getBasicIndexes();
        basicVars = new int[basicIndexes.length];
        for (int i = 0; i < basicIndexes.length; i++) {
            basicVars[i] = basicIndexes[i];
        }
        

        lastList = new DenseVector(maxIndex + 2);
        double[] objectiveCoefs = parser.getObjectiveCoefs();
        lastList.set(maxIndex +1, -1);
        
        addToAllVars(parser.getBasicIndexes());
        addToAllVars(parser.getNonBasicIndexes());
        addToAllVars(new int[]{maxIndex + 1});
    }
    
    public Set<Integer> getNonBasicIndexes() {
        HashSet<Integer> copy = Sets.newHashSet(allVars);
        for (int i = 0; i < basicVars.length; i++) {
            copy.remove(basicVars[i]);
        }
        return copy;
    }
    
    PivotSolver toAuxilaryProblem() {
        PivotSolver ps =  new PivotSolver(this);
        ps.firstAxilaryStep();
        return ps;
    }
    
    public Status nextIteration() {
        List<Integer> enteringIndexes = Lists.newArrayList();
        enteringIndex = -1;
        leavingIndex = -1;
        for (Integer nbi : getNonBasicIndexes()) {
            if (lastList.get(nbi) > 0) {
                enteringIndexes.add(nbi);
            }
        }
        Collections.sort(enteringIndexes);
        if (enteringIndexes.isEmpty()) {
            status = Status.FINAL;
            return status;
        }
//        for (Integer ei : enteringIndexes) {
        int ei = enteringIndexes.get(0);
        double bestVal = -Double.MAX_VALUE;
        boolean found = false;
        for (int i = 0; i < rows.size(); i++) {
            Vector vector = rows.get(i);
            double val = vector.get(ei);
            double b = vector.get(0);
            if (b >= 0 && val < 0) {
                double tmpVal = b / val;
                if (tmpVal > bestVal || (Math.abs(tmpVal - bestVal) < DELTA && basicVars[leavingIndex] > basicVars[i])) {
                    leavingIndex = i;
                    bestVal = tmpVal;
                    found = true;
                    enteringIndex = ei;
                }
            }
        }
        updateForEnteringAndLeavingIndexes();
        return status;
    }
    
    private void updateDictionary() {
        Vector leavingVector = rows.get(leavingIndex);
        double leavingVal = leavingVector.get(enteringIndex);
        leavingVector.set(enteringIndex, 0);
        leavingVector.set(basicVars[leavingIndex], -1);
        leavingVector.scale(-1.0/leavingVal);
        
        for (Vector vector : rows) {
            substituteToVector(vector, leavingVector);
        }
        substituteToVector(lastList, leavingVector);
        basicVars[leavingIndex] = enteringIndex;
    }
    
    private void initRow(InputParser parser, double[] rowVals,int rowValsIdx, Vector row) {
        int[] nonBasicIndexes = parser.getNonBasicIndexes();
        for (int i = 0; i < nonBasicIndexes.length; i++) {
            int idx = nonBasicIndexes[i];
            double val = rowVals[i + rowValsIdx];
            row.set(idx, val);
        }
    }

    private void addToAllVars(int[] indexes) {
        for (int i = 0; i < indexes.length; i++) {
            allVars.add(indexes[i]);
        }
    }
    
    private void vect2Str(Vector vector, StringBuilder builder) {
        for (int j = 0; j < vector.size(); j++) {
            builder.append(vector.get(j)).append(" ");
        }
    }
    
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Rows:\n");
        for (int i = 0; i < rows.size(); i++) {
            Vector vector = rows.get(i);
            builder.append(basicVars[i]).append(" |");
            vect2Str(vector, builder);
            builder.append("\n");
            
        }
        builder.append("\n   |");
        vect2Str(lastList, builder);
        return builder.toString();
    }

    private void substituteToVector(Vector vector, Vector leavingVector) {
        double scale = vector.get(enteringIndex);
        vector.set(enteringIndex, 0);
        vector.add(scale, leavingVector);
    }

    int getEnteringIndex() {
        return oldEnteringIndex;
    }

    int getLeavingIndex() {
        return oldLeavingIndex;
    }

    double getCurrentValue() {
        return lastList.get(0);
    }

    Status getStatus() {
        return status;
    }
    
    public String getSingleIterationReport() {
        StringBuilder report = new StringBuilder();
        if (status == Status.UNBOUNDED) {
            report.append(Status.UNBOUNDED);
        } else {
            report.append(getEnteringIndex()).append("\n");
            report.append(getLeavingIndex()).append("\n");
            report.append(getCurrentValue());
        }
        return report.toString();
    }
    
    public String getReport() {
        StringBuilder builder = new StringBuilder();
        if (status == Status.FINAL) {
            builder.append(getCurrentValue() + "\n" + iterations);
        } else {
            builder.append(status);
        }
        return  builder.toString();
    }

    public int getIterations() {
        return iterations;
    }

    private void updateForEnteringAndLeavingIndexes() {
        if (enteringIndex > 0 && leavingIndex > -1) {
            iterations++;
            status = Status.OK;
            oldEnteringIndex = enteringIndex;
            oldLeavingIndex = this.basicVars[leavingIndex];
            
            updateDictionary();
        } else {
            status = Status.UNBOUNDED;
        }
    }

    private Status firstAxilaryStep() {
        enteringIndex = this.allVars.size();
        double leavingVal = Double.MAX_VALUE;
        for (int i = 0; i < rows.size(); i++) {
            Vector vector = rows.get(i);
            if(vector.get(0) < leavingVal) {
                leavingIndex = i;
                leavingVal = vector.get(0);
            }
        }    
        updateForEnteringAndLeavingIndexes();
        return status;
    }
    
}
