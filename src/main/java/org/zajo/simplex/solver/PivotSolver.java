package org.zajo.simplex.solver;

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
    List<Integer> basicVars;
    Vector lastList;
    private final InputParser parser;
    
    Status status = Status.OK;
    
    int enteringIndex = -1;
    int leavingIndex = -1;
    
    int oldEnteringIndex = -1;
    int oldLeavingIndex = -1;
    int iterations;
    
    Set<Integer> allVars = Sets.newHashSet();
    
    
    private PivotSolver(List<Vector> rows, List<Integer> basicVars, Vector lastList, InputParser parser, Set<Integer> allVars) {
        this.rows = rows;
        this.basicVars = basicVars;
        this.lastList = lastList;
        this.parser = parser;
        this.allVars = allVars;
    }
    
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
        basicVars = Lists.newArrayList();
        for (int i = 0; i < basicIndexes.length; i++) {
            basicVars.add(basicIndexes[i]);
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
        basicVars = Lists.newArrayList();
        for (int i = 0; i < basicIndexes.length; i++) {
            basicVars.add(basicIndexes[i]);
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
        copy.removeAll(basicVars);
        return copy;
    }
    
    /**
     * Convert LP to AxilaryProblem (another one slack variable.
     * @return auxilary problem)
     */
    PivotSolver toAuxilaryProblem() {
        PivotSolver ps =  new PivotSolver(this);
        ps.firstAxilaryStep();
        return ps;
    }
    
    /**
     * Perform single pivoting step.
     * @return status 
     */
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
                if (tmpVal > bestVal
                        || (Math.abs(tmpVal - bestVal) < DELTA 
                        && basicVars.get(leavingIndex) > basicVars.get(i))) {
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
        leavingVector.set(basicVars.get(leavingIndex), -1);
        leavingVector.scale(-1.0/leavingVal);
        
        for (Vector vector : rows) {
            substituteToVector(vector, leavingVector);
        }
        substituteToVector(lastList, leavingVector);
        basicVars.set(leavingIndex, enteringIndex);
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
            builder.append(basicVars.get(i)).append(" |");
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
            oldLeavingIndex = this.basicVars.get(leavingIndex);
            
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

    /**
     * Insert cutting planes for solving ILP problem.
     * @return true if at least one cutting plane has been inserted
     */
    boolean insertCutPlanes() {
        List<Integer> canditesToCut = Lists.newArrayList();
        // find all candidates for cut
        for (int i = 0; i < rows.size(); i++) {
            Vector vector = rows.get(i);
            if (canInsertCutFor(vector)) {
                canditesToCut.add(i);
            }
        }
        if (canditesToCut.isEmpty()) {
            return false;
        }
        int newVar = allVars.size() + 1;
        extendsRowsVariables(canditesToCut.size());
        // insert the cut planes
        for (Integer rowIdx : canditesToCut) {
            insertCut(rowIdx);
        }
        
        return true;
        
    }

    /**
     * Convert pivot table to new new one dual problem.
     * @return converted dual problem
     */
    public PivotSolver convertToDualProblem() {
        List<Integer> nonBasicIndexes = Lists.newArrayList(getNonBasicIndexes());
        int numRows = nonBasicIndexes.size();
        List<Vector> newRows = Lists.newArrayList();
        Vector newLastList = new DenseVector(allVars.size() + 1);
        for (int i = 0 ; i < numRows ; i++) {
           Integer newBasicIndex = nonBasicIndexes.get(i);
           Vector newRow = new DenseVector(allVars.size() + 1);
           
           newRow.set(0, - lastList.get(newBasicIndex));
            for (int oldRowIdx = 0; oldRowIdx < rows.size(); oldRowIdx++) {
                Vector oldRow = rows.get(oldRowIdx);
                newRow.set(basicVars.get(oldRowIdx), -oldRow.get(newBasicIndex));
            }
            newRows.add(newRow);
            
        }
        newLastList.set(0, -lastList.get(0)); 
        for (int oldRowIdx = 0; oldRowIdx < rows.size(); oldRowIdx++) {
            Vector oldRow = rows.get(oldRowIdx);
            newLastList.set(basicVars.get(oldRowIdx), -oldRow.get(0));
        }
        
        
        PivotSolver solver = new PivotSolver(newRows, nonBasicIndexes, newLastList, parser, Sets.newHashSet(allVars));
        return solver;
    }

    /**
     * Test whether or not can insert cut plane for row 
     * @param row source row
     * @return truen if cut plane can be inserted
     */
    private boolean canInsertCutFor(Vector row) {
        double val = row.get(0);
        double rVal = Math.rint(val);
        if (Math.abs(val - rVal) > DELTA) {
            return true;
        }
        return false;
    }

    private void insertCut(Integer rowIdx) {
        Vector oldRow = rows.get(rowIdx);
        Vector newRow = new DenseVector(oldRow.size());
        
        double val = oldRow.get(0);
        
        if (val >= -DELTA) {
            val = Math.floor(val) - val;
        } else {
            throw new IllegalStateException("negative value: " + val);
        }
        newRow.set(0, val);
        
        for (int col = 1 ; col < oldRow.size() ; col++) {
            val = oldRow.get(col);
            if (val < 0) {
                val = Math.abs(val);
                val -= Math.floor(val);
            } else if (val > 0) {
                val = Math.abs(val);
                val = Math.ceil(val) - val;
            }
            newRow.set(col, val);
        }
        rows.add(newRow);
    }
    

    /**
     * Add new variables to pivot table.
     * @param count number of new variables
     */
    private void extendsRowsVariables(int count) {
        for (int rowIdx = 0; rowIdx < rows.size(); rowIdx++) {
            Vector origRow = rows.get(rowIdx);
            Vector newRow = new DenseVector(origRow.size() + count);
            int col = 0;
            for (; col < origRow.size(); col++) {
                newRow.set(col, origRow.get(col));
            }
            // clear new values
            for (; col < newRow.size(); col++) {
                newRow.set(col, 0);
            }
            rows.set(rowIdx, newRow);
            
        }
        // extends variables
        int startNewVar = allVars.size() + 1;
        for (int newVarIdx = 0; newVarIdx < count; newVarIdx++) {
            final int newVar = startNewVar + newVarIdx;
            allVars.add(newVar);
            basicVars.add(newVar);
        }
    }

    /**
     * @return true if the problem needs to be converted to auxilary problem
     */
    public boolean needsAuxilary() {
        for (Vector row : rows) {
            if (row.get(0) < 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 
     * @return last line in pivot table
     */
    public Vector getLastList() {
        return lastList;
    }

    
    /**
     * convert auxilary proble back to origin problem. It updates last
     * line of pivot table by substituting the orig Problem last line. 
     * @param origProblem original LP problem
     */
    void auxilaryToOrig(PivotSolver origProblem) {
        Vector lastList1 = origProblem.getLastList();
        Vector origLastList = new DenseVector(lastList.size());
        for (int i = 0; i < lastList1.size(); i++) {
            origLastList.set(i, lastList1.get(i));
        }
        Vector newLastist = new DenseVector(lastList.size());
        origLastList.set(lastList1.size(), 0);
        for (int i = 0; i < basicVars.size(); i++) {
            Integer basicVar = basicVars.get(i);
            double scale = origLastList.get(basicVar);
            newLastist.add(scale, rows.get(i));
        }
       
        lastList = newLastist;
        removeVariable(allVars.size());
    }

    /**
     * Remove variable from pivot table. It's used for removing x0 from
     * auxilary problem.
     * @param var variable index to be removed (from 1).
     */
    private void removeVariable(int var) {
        for (int i = 0; i < basicVars.size(); i++) {
            Integer integer = basicVars.get(i);
            if (integer == var) {
                rows.remove(i);
                basicVars.remove(i);
            }
            
        }
        
        List<Vector> newRows = Lists.newArrayList();
        for (Vector oldRow : rows) {
            newRows.add(removeVarFromRow(oldRow, var));
        }
        rows = newRows;
        allVars.remove(var);
        lastList = removeVarFromRow(lastList, var);
    }

    private Vector removeVarFromRow(Vector oldRow, int var) {
        Vector newRow = new DenseVector(oldRow.size() - 1);
        int j = 0;
        for (int i = 0; i < oldRow.size(); i++) {
            if (i != var) {
                newRow.set(j++, oldRow.get(i));
            }
        }
        return newRow;
    }
    
}
