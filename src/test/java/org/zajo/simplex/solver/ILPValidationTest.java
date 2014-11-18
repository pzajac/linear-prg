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

//        checkFile("ilpTest" + 6);
        for (int i = 1; i < 11; i++) {
            checkFile("ilpTest" + i);
        }

    }

    public void checkFile(String fileName) throws IOException {
        System.out.println(fileName);
        ILPSolver ilpSolver = new ILPSolver(new FileInputStream(new File(FOLDER, fileName)));
        Status nextIteration = ilpSolver.compute();
        File file = new File(FOLDER, fileName + ".output");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            String line = reader.readLine();
            switch (nextIteration) {
                case INFEASIBLE:
                    assertEquals("result " + fileName, "infeasible", line);
                    break;
                case UNBOUNDED:    
                    assertEquals("result " + fileName, "unbounded", line);
                    break;
                case FINAL:
                    double auxValue  = Double.parseDouble(line);
                    assertEquals("result " + fileName, auxValue, ilpSolver.getValue(), 1e-17);
                    break;
                default:
                    fail("invalid status " + nextIteration);
                    
            }
        } finally {
            reader.close();
        }
    }

}
