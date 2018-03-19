/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eoss;

import java.io.File;

import org.moeaframework.core.Algorithm;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Variation;
import org.moeaframework.core.comparator.ParetoDominanceComparator;

import org.moeaframework.core.operator.GAVariation;
import org.moeaframework.core.operator.OnePointCrossover;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.core.operator.binary.BitFlip;
import org.moeaframework.util.TypedProperties;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.moeaframework.algorithm.EpsilonMOEA;
import org.moeaframework.core.EpsilonBoxDominanceArchive;
import org.moeaframework.core.Population;
import org.moeaframework.core.comparator.DominanceComparator;

import eoss.search.InstrumentedSearch;
import eoss.operators.FeatureMutation;
import eoss.operators.FeatureCrossOver;


/**
 *
 * @author hsbang
 */

public class EOSSFeatureExtractionMain {

    /**
     * pool of resources
     */
    private static ExecutorService pool;

    /**
     * List of future tasks to perform
     */
    private static ArrayList<Future<Algorithm>> futures;

    /**
     * First argument is the path to the project folder. Second argument is the
     * mode. Third argument is the number of ArchitecturalEvaluators to
     * initialize.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        //PATH
        if (args.length == 0) {
            args = new String[4];
            args[0] = "/Users/bang/workspace/featureGA";
            args[1] = "2"; //Mode
            args[2] = "1"; //numCPU
            args[3] = "1"; //numRuns
        }

        EOSSFeatureGenerator featureGenerator = new EOSSFeatureGenerator();

        System.out.println("Path set to " + args[0]);
        System.out.println("Running mode " + args[1]);
        System.out.println("Will get " + args[2] + " resources");
        System.out.println("Will do " + args[3] + " runs");

        String path = args[0];

        int MODE = Integer.parseInt(args[1]);
        int numCPU = Integer.parseInt(args[2]);
        int numRuns = Integer.parseInt(args[3]);

        pool = Executors.newFixedThreadPool(numCPU);
        futures = new ArrayList<>(numRuns);

        //parameters and operators for search
        TypedProperties properties = new TypedProperties();

        //search paramaters set here
        int popSize = 200;
        int maxEvals = 10000;
        properties.setInt("maxEvaluations", maxEvals);
        properties.setInt("populationSize", popSize);
        double crossoverProbability = 1.0;
        double mutationProbability = 1./60.;

        Variation mutation;
        Variation crossover;
        Variation gaVariation;

        Initialization initialization;
        Problem problem;

        //setup for epsilon MOEA
        DominanceComparator comparator = new ParetoDominanceComparator();
        double[] epsilonDouble = new double[]{0.001, 0.001};
        final TournamentSelection selection = new TournamentSelection(2, comparator);

        //setup for saving results
        properties.setBoolean("saveQuality", true);
        properties.setBoolean("saveCredits", true);
        properties.setBoolean("saveSelection", true);

        switch (MODE) {
            case 1: //MOEA/D

//                //setup MOEAD
//                NondominatedSortingPopulation ndsPopulation = new NondominatedSortingPopulation();
//
//                TournamentSelection tSelection = new TournamentSelection(2,
//                        new ChainedComparator(
//                                new ParetoDominanceComparator(),
//                                new CrowdingComparator()));
//
//                //NSGA crossover probability should be less than 1
//                double crossoverProbability08 = 0.8;
//                Variation singlecross08 = new OnePointCrossover(crossoverProbability08);
//
//                singlecross = new OnePointCrossover(crossoverProbability);
//                BitFlip = new BitFlip(mutationProbability);
//                Variation NSGAVariation = new GAVariation(singlecross08, BitFlip);
//
//                problem = getFeatureExtractionProblem(false);
//
//                initialization = new EOSSFeatureGenerator(problem, popSize, "random");
//                Algorithm nsga2 = new NSGAII(problem, ndsPopulation, null, tSelection, NSGAVariation,
//                        initialization);

                break;

            case 2: //Use epsilonMOEA
                for (int i = 0; i < numRuns; i++) {

                    mutation = new FeatureMutation(featureGenerator);
                    crossover = new FeatureCrossOver(featureGenerator);
                    gaVariation = new GAVariation(crossover, mutation);

                    Population population = new Population();
                    EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(epsilonDouble);

                    problem = new FeatureExtractionProblem(1, EOSSFeatureExtractionParams.numberOfObjectives, featureGenerator);
                    initialization = new EOSSInitialization(problem, popSize, "random");

                    Algorithm eMOEA = new EpsilonMOEA(problem, population, archive, selection, gaVariation, initialization);

                    InstrumentedSearch run;

                    run = new InstrumentedSearch(eMOEA, properties, path + File.separator + "results",  String.valueOf(i));

                    futures.add(pool.submit(run));
                }

                for (Future<Algorithm> run : futures) {
                    try {
                        run.get();

                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(EOSSFeatureExtractionMain.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;


            case 3://Hyperheuristic search
//                String origname = "AIAA_innovize_" + System.nanoTime();
//                for (int i = 0; i < numRuns; i++) {
//                    ICreditAssignment creditAssignment;
//                    String[] creditDefs = new String[]{"SIDo"};
//                    for (String credDef : creditDefs) {
//
//                        try {
//                            problem = getFeatureExtractionProblem(false);
//
//                            creditAssignment = CreditDefFactory.getInstance().getCreditDef(credDef, properties, problem);
//
//                            ArrayList<Variation> heuristics = new ArrayList();
//
//                            //add domain-independent heuristics
//                            heuristics.add(new CompoundVariation(new OnePointCrossover(crossoverProbability,2), new BitFlip(mutationProbability)));
//
//                            properties.setDouble("pmin", 0.03);
//
//                            //all other properties use default parameters
//                            INextOperator selector = AOSFactory.getInstance().getHeuristicSelector("AP", properties, heuristics);
//
//                            Population population = new Population();
//                            EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(epsilonDouble);
//
//                            initialization = new EOSSFeatureGenerator(problem, popSize, "random");
//
//                            AOSEpsilonMOEA hemoea = new AOSEpsilonMOEA(problem, population, archive, selection,
//                                    initialization, selector, creditAssignment);
//
//                            InstrumentedSearch run;
//                            if(i<numCPU){
//                                run = new InstrumentedSearch(hemoea, properties, path + File.separator + "result", origname + i, true);
//                            }else{
//                                run = new InstrumentedSearch(hemoea, properties, path + File.separator + "result", origname + i, false);
//                            }
//                            futures.add(pool.submit(run));
//                        } catch (IOException ex) {
//                            Logger.getLogger(EOSSFeatureExtractionMain.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                    }
//                }
//
//                for (Future<Algorithm> run : futures) {
//                    try {
//                        AOSEpsilonMOEA hemoea = (AOSEpsilonMOEA) run.get();
//                    } catch (InterruptedException | ExecutionException ex) {
//                        Logger.getLogger(EOSSFeatureExtractionMain.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//
//                pool.shutdown();
                break;

            case 4://innovization search


//                String fileName = "AIAA_innovize_" + System.nanoTime();
//                for (int i = 0; i < numRuns; i++) {
//                    try {
//                        problem = getFeatureExtractionProblem(false);
//
//                        ICreditAssignment creditAssignment = CreditDefFactory.getInstance().getCreditDef("SIDo", properties, problem);
//
//                        ArrayList<Variation> operators = new ArrayList();
//
//                        //add domain-independent heuristics
//                        Variation SingleCross = new CompoundVariation(new OnePointCrossover(crossoverProbability,2), new BitFlip(mutationProbability));
//                        operators.add(SingleCross);
//
//                        //set up OperatorReplacementStrategy
//                        EpochTrigger epochTrigger = new EpochTrigger(epochLength);
//                        EOSSOperatorCreator eossOpCreator = new EOSSOperatorCreator(crossoverProbability,mutationProbability);
//                        ArrayList<Variation> permanentOps = new ArrayList();
//                        permanentOps.add(SingleCross);
//                        RemoveNLowest operatorRemover = new RemoveNLowest(permanentOps, properties.getInt("nOpsToRemove", 2));
//                        OperatorReplacementStrategy ops = new OperatorReplacementStrategy(epochTrigger, operatorRemover, eossOpCreator);
//
//                        properties.setDouble("pmin", 0.03);
//
//                        //all other properties use default parameters
//                        INextOperator selector = AOSFactory.getInstance().getHeuristicSelector("AP", properties, operators);
//
//                        Population population = new Population();
//                        EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(epsilonDouble);
//
//                        initialization = new EOSSFeatureGenerator(problem, popSize, "random");
//
//                        AOSEpsilonMOEA hemoea = new AOSEpsilonMOEA(problem, population, archive, selection,
//                                initialization, selector, creditAssignment);
//
//                        AbstractPopulationLabeler labeler =  new NondominatedSortingLabeler(.25);
//                        InnovizationSearch run;
//                        if(i<numCPU){
//                            run = new InnovizationSearch(hemoea, properties, labeler, ops, path + File.separator + "result", fileName + i,true);
//                        }else{
//                            run = new InnovizationSearch(hemoea, properties, labeler, ops, path + File.separator + "result", fileName + i, false);
//                        }
//                        futures.add(pool.submit(run));
//                    } catch (IOException ex) {
//                        Logger.getLogger(EOSSFeatureExtractionMain.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//
//                }
//
//                for (Future<Algorithm> run : futures) {
//                    try {
//                        AOSEpsilonMOEA hemoea = (AOSEpsilonMOEA) run.get();
//
//                    } catch (InterruptedException | ExecutionException ex) {
//                        Logger.getLogger(EOSSFeatureExtractionMain.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//
//                pool.shutdown();
                break;

            default:
                System.out.println("Choose a mode between 1 and 9");
        }
    }

//    public static void initEOSSProblem(String path, String fuzzyMode, String testMode, String normalMode) {
//        EOSSDatabase.getInstance(); //to initiate database
//        new Params(path, fuzzyMode, testMode, normalMode);//FUZZY or CRISP;
//    }




}
