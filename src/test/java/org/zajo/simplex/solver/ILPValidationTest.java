package org.zajo.simplex.solver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import junit.framework.TestCase;

/**
 *
 * @author pzajac
 */
public class ILPValidationTest extends TestCase {

    private static final String FOLDER = "/home/pzajac/coursera/LinearProgramming/programming/ilpTest/";

    public ILPValidationTest(String testName) {
        super(testName);
    }

    public void testParse() throws IOException {

         checkFile("ilpTest" + 1);
//        for (int i = 1 ; i < 11; i++) {
//            checkFile("idict" + i);
//        }

    }

    public void checkFile(String fileName) throws IOException {
        PivotSolver solver = read(fileName);
//        PivotSolver auxPropblem = solver.toAuxilaryProblem();
        Status nextIteration = null;
        // compute LP problem
        do {
            nextIteration = solver.nextIteration();
        } while (nextIteration == Status.OK);
 
        System.out.println("initial final: " + solver.toString());
        // isert cut planes
        while(solver.insertCutPlanes() && nextIteration == Status.FINAL) {
            System.out.println("cut planes inserted: " + solver.toString());
            // convert to dual problem
            solver = solver.convertToDualProblem();
            System.out.println("dual problem:" + solver);
            // compute lp problem
            do {
                nextIteration = solver.nextIteration();
            } while (nextIteration == Status.OK);
            if (nextIteration != Status.FINAL) {
                break;
            }
            System.out.println("dual final:" + solver);
            // convert to original problem
            solver = solver.convertToDualProblem();
            System.out.println("original final: " + solver);
        }
        BufferedReader reader = new BufferedReader(new FileReader(new File(FOLDER,fileName + ".output")));
        try {
            String line = reader.readLine();
            double auxValue = Double.parseDouble(line);
            assertEquals("result " + fileName, auxValue, solver.getCurrentValue(), 1e-4);
        } finally {
            reader.close();
        }
    }

    private PivotSolver read(String name) throws IOException {
        File f = new File(FOLDER, name);
        Reader reader = new FileReader(f);
        try {
            BufferedReader breader = new BufferedReader(reader);
            InputParser parser = new InputParser();
            parser.parseMatrix(breader);
            return new PivotSolver(parser);
        } finally {
            reader.close();
        }
    }
}
