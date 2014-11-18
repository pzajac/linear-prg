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
        System.out.println("original problem: " + origProblem);
        boolean auxilary = solver.needsAuxilary();
        if (auxilary) {
            solver = solver.toAuxilaryProblem();
            System.out.println("auxilary problem: " + solver);
        }
        // compute LP problem
        do {
            status = solver.nextIteration();
            System.out.println("iteration: " + solver);
        } while (status == Status.OK);
        if (status == Status.FINAL && auxilary) {
            if (Math.abs(solver.getCurrentValue()) > PivotSolver.DELTA) {
                status = Status.INFEASIBLE;
            } else {
                solver.auxilaryToOrig(origProblem);
                System.out.println("feasible auxilary problem: " + solver);
                do {
                    status = solver.nextIteration();
                    System.out.println("iteration  " + status + ": " + solver);
                } while (status == Status.OK);
            }
        }
        // isert cut planes
        while (status == Status.FINAL && solver.insertCutPlanes()) {
            System.out.println("cut planes:" + solver);
            // convert to dual problem
            solver = solver.convertToDualProblem();
            System.out.println("dual problem: " + solver);
            // compute lp problem
            do {
                status = solver.nextIteration();
                System.out.println("iteration: " + solver);
            } while (status == Status.OK);
            if (status != Status.FINAL) {
                status = Status.INFEASIBLE;
                break;
            }
            // convert to original problem
            solver = solver.convertToDualProblem();
            System.out.println("converted dual problem: " + solver);
        }
        if (status == Status.FINAL) {
            value = Math.rint(solver.getCurrentValue());
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
