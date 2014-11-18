package org.zajo.simplex.solver;

/**
 * Status from single pivoting operation
 */
public enum Status {
    /**
     * Pivoting oparation finished. The dictionary is not still final.
     */
    OK,
    /**
     * The problem is unbounded.
     */
    UNBOUNDED,
    INFEASIBLE,
    /**
     * Dictionary is final.
     */
    FINAL
}
