/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eoss;

/**
 *
 * @author hsbang
 */

import org.moeaframework.core.*;


public class EOSSInitialization implements Initialization {
    /**
     * Generate the base features and store them
     */

    /**
     * The problem.
     */
    private final Problem problem;

    /**
     * The initial population size.
     */
    private final int populationSize;

    /**
     * parallel purpose random generator
     */
    private final ParallelPRNG pprng;

    /**
     * type of initialization
     */
    private final String type;


    /**
     * Constructs a random initialization operator.
     *
     * @param problem the problem
     * @param populationSize the initial population size
     */
    public EOSSInitialization(Problem problem, int populationSize, String type) {
        this.problem = problem;
        this.populationSize = populationSize;
        this.pprng = new ParallelPRNG();
        this.type = type;
    }


    @Override
    public Solution[] initialize() {
        Solution[] initialPopulation = new Solution[populationSize];

        for (int i = 0; i < populationSize; i++) {

            Solution solution = problem.newSolution();

            for (int j = 0; j < solution.getNumberOfVariables(); j++) {
                Variable variable = solution.getVariable(j);
                switch (type) {
                    case "random":
                        randInitializeVariable(variable);
                        break;
                    case "fullfactorial":
                        throw new UnsupportedOperationException("Full factorial enumeration is not yet supported");
                    default:
                        throw new IllegalArgumentException("No such initialization type: " + type);
                }
            }

            initialPopulation[i] = solution;
        }

        return initialPopulation;
    }

    /**
     * Initializes the specified decision variable randomly. This method
     * supports all built-in types, and can be extended to support custom types.
     *
     * @param variable the variable to be initialized
     */
    protected void randInitializeVariable(Variable variable) {
        variable.randomize();
    }
}
