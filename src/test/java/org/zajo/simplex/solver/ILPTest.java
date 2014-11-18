package org.zajo.simplex.solver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
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

        for (int i = 1; i < 10; i++) {
            checkFile("ilpTest" + i);
        }

    }

    public void checkFile(String fileName) throws IOException {
        ILPSolver ilpSolver = new ILPSolver(new FileInputStream(new File(FOLDER, fileName)));
        Status nextIteration = ilpSolver.compute();
        File file = new File(FOLDER,fileName + ".output");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            String line = reader.readLine();
            if (nextIteration == Status.UNBOUNDED) {
                assertEquals("result " + fileName, "infeasible", line);
            } else {
                double auxValue = Double.parseDouble(line);
                assertEquals("result " + fileName, auxValue, ilpSolver.getValue(), 1e-4);
            }
        } finally {
            reader.close();
        }
    }

}
