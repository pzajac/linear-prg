package org.zajo.simplex.solver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import junit.framework.TestCase;

/**
 *
 * @author pzajac
 */
public class Excercise2Test extends TestCase {

    private static final String FOLDER = "/home/pzajac/coursera/LinearProgramming/programming/part2TestCases/assignmentParts";

    public Excercise2Test(String testName) {
        super(testName);
    }

    public void testParse() throws IOException {

            checkFile("part1");
            checkFile("part2");
            checkFile("part3");
            checkFile("part4");
            checkFile("part5");

    }

    public void checkFile(String fileName) throws IOException {
        PivotSolver solver = read(fileName + ".dict");
        Status nextIteration = null;
        do {
            nextIteration = solver.nextIteration();
        } while (nextIteration == Status.OK);

        PrintWriter pw = new PrintWriter(new File(FOLDER,fileName + ".output"));
        try {
            pw.println(solver.getReport());
        } finally {
            pw.close();
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
