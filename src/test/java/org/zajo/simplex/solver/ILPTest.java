package org.zajo.simplex.solver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import junit.framework.TestCase;

/**
 *
 * @author pzajac
 */
public class ILPTest extends TestCase {

    private static final String FOLDER = "/home/pzajac/coursera/LinearProgramming/programming/ilpAssigments/";

    public ILPTest(String testName) {
        super(testName);
    }

    public void testParse() throws IOException {

        
//         checkFile("part" + 2);
        for (int i = 1; i < 6; i++) {
            checkFile("part" + i);
        }

    }

    public void checkFile(String fileName) throws IOException {
        System.out.println(fileName);
        ILPSolver ilpSolver = new ILPSolver(new FileInputStream(new File(FOLDER, fileName + ".dict")));
        Status nextIteration = ilpSolver.compute();
        File file = new File(FOLDER,fileName + ".output");
        PrintWriter writer = new PrintWriter(new FileWriter(file));
        try {
            
            switch(nextIteration) {
                case INFEASIBLE:
                    writer.println("infeasible");
                    break;
                case UNBOUNDED:
                    writer.println("unbounded");
                    break;
                case FINAL:
                    writer.println(ilpSolver.getValue());
                    break;
                default:
                    throw new IllegalStateException("Invalid state " + nextIteration);
            }
        } finally {
            writer.close();
        }
    }

}
