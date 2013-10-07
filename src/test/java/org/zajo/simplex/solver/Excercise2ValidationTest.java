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
public class Excercise2ValidationTest extends TestCase {

    private static final String FOLDER = "/home/pzajac/coursera/LinearProgramming/programming/part2TestCases/unitTests";

    public Excercise2ValidationTest(String testName) {
        super(testName);
    }

    public void testParse() throws IOException {

        for (int i = 1 ; i < 11; i++) {
            checkFile("dict" + i);
        }

    }

    public void checkFile(String fileName) throws IOException {
        PivotSolver solver = read(fileName);
        Status nextIteration = null;
        do {
            nextIteration = solver.nextIteration();
        } while (nextIteration == Status.OK);

        BufferedReader reader = new BufferedReader(new FileReader(new File(FOLDER,fileName + ".output")));
        try {
            String line = reader.readLine();
            if (Status.UNBOUNDED.toString().equals(line)) {
                assertEquals(fileName, nextIteration, Status.UNBOUNDED);
            } else {
                double value = Double.parseDouble(line);
                assertEquals("value for " + fileName, value, solver.getCurrentValue(), 1e-4);
                int steps = Integer.parseInt(reader.readLine());
                assertTrue("steps for " + fileName, steps<= solver.getIterations());
            }
            byte bytes[] = new byte[1000];
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
