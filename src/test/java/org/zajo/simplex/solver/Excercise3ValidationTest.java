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
public class Excercise3ValidationTest extends TestCase {

    private static final String FOLDER = "/home/pzajac/coursera/LinearProgramming/programming/initializationTests/unitTests";

    public Excercise3ValidationTest(String testName) {
        super(testName);
    }

    public void testParse() throws IOException {

         checkFile("idict" + 3);
        for (int i = 1 ; i < 11; i++) {
            checkFile("idict" + i);
        }

    }

    public void checkFile(String fileName) throws IOException {
        PivotSolver solver = read(fileName);
        PivotSolver auxPropblem = solver.toAuxilaryProblem();
        Status nextIteration = null;
        do {
            nextIteration = auxPropblem.nextIteration();
        } while (nextIteration == Status.OK);

        BufferedReader reader = new BufferedReader(new FileReader(new File(FOLDER,fileName + ".out")));
        try {
            String line = reader.readLine();
            double auxValue = Double.parseDouble(line);
            assertEquals("result " + fileName, auxValue, auxPropblem.getCurrentValue(), 1e-4);
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
