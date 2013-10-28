package org.zajo.simplex.solver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * ILP simplex solver.
 *
 * @author pzajac
 */
public class ILPSolver {

    private double value;
    private final InputParser inputParser;

    private Status status = Status.OK;

    public ILPSolver(InputStream is) throws IOException {
        this.inputParser = new InputParser();
        try {
            InputStreamReader reader = new InputStreamReader(is);

            inputParser.parseMatrix(new BufferedReader(reader));
        } finally {
            is.close();
        }
    }

    public Status compute() throws IOException {
        PivotSolver solver = new PivotSolver(inputParser);
        PivotSolver origProblem = solver;
        boolean auxilary = solver.needsAuxilary();
        if (auxilary) {
            solver = solver.toAuxilaryProblem();
        }
        // compute LP problem
        do {
            status = solver.nextIteration();
        } while (status == Status.OK);
        if (status == Status.FINAL && auxilary) {
            if (Math.abs(solver.getCurrentValue()) > PivotSolver.DELTA) {
                status = Status.UNBOUNDED;
            } else {
                solver.auxilaryToOrig(origProblem);
            }
        }
        // isert cut planes
        while (status == Status.FINAL && solver.insertCutPlanes()) {
            // convert to dual problem
            solver = solver.convertToDualProblem();
            // compute lp problem
            do {
                status = solver.nextIteration();
            } while (status == Status.OK);
            if (status != Status.FINAL) {
                break;
            }
            // convert to original problem
            solver = solver.convertToDualProblem();
        }
        if (status != Status.UNBOUNDED) {
            value = solver.getCurrentValue();
        }

        return status;
    }

    public double getValue() {
        if (status != Status.FINAL) {
            throw new IllegalStateException("Dictionary is not final.");
        }
        return value;
    }

    public Status getStatus() {
        return status;
    }

}
