package org.zajo.simplex.solver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import static junit.framework.Assert.assertEquals;
import junit.framework.TestCase;

/**
 *
 * @author pzajac
 */
public class PivotSolverTest extends TestCase {

    private static final String FOLDER = "/home/pzajac/coursera/LinearProgramming/programming/part1TestCases/unitTests";

    public PivotSolverTest(String testName) {
        super(testName);
    }

    public void testParse() throws IOException {
//        PivotSolver solver = read("dict6");
//        System.out.println(solver.toString());
//        
//        System.out.println("status:" + solver.nextIteration());
//        System.out.println("enterIndex:" + solver.getEnteringIndex());
//        System.out.println("leavingIndex:" + solver.getLeavingIndex());
//        System.out.println("Value:" + solver.getCurrentValue());
//        
//        checkFile("dict2");
//        checkFile("dict3");
//        checkFile("dict4");
        checkFile("dict6");
        checkFile("dict5");
        File folder = new File(FOLDER);
        String[] listFiles = folder.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return !(name.contains(".")) && name.startsWith("dict");
            }
        });
        for (int i = 0; i < listFiles.length; i++) {
            checkFile(listFiles[i]);

        }
    }

    public void checkFile(String fileName) throws IOException {
        PivotSolver solver = read(fileName);
        System.out.println(solver.toString());
        solver.nextIteration();
        System.out.println(fileName);

        BufferedReader reader = new BufferedReader(new FileReader(new File(FOLDER, fileName + ".output")));
        String firstLine = reader.readLine();
        Status status = Status.OK;


        if ("UNBOUNDED".equals(firstLine)) {
            assertEquals("Unbound " + fileName, Status.UNBOUNDED, solver.getStatus());
        } else {
            assertEquals("entering index for " + fileName, Integer.parseInt(firstLine), solver.getEnteringIndex());
            String line = reader.readLine();
            assertEquals("leaving index for " + fileName, Integer.parseInt(line), solver.getLeavingIndex());
            line = reader.readLine();
            assertEquals("value for " + fileName, Double.parseDouble(line), solver.getCurrentValue(), 1e-3);
        }

        System.out.println(solver.getSingleIterationReport());

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
