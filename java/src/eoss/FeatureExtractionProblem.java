package eoss;

import ifeed_dm.LogicOperator;
import ifeed_dm.logic.Connective;
import org.moeaframework.core.Solution;
import org.moeaframework.problem.AbstractProblem;

import java.util.BitSet;

/**
 *
 * @author hsbang
 */

public class FeatureExtractionProblem extends AbstractProblem {

    public EOSSFeatureGenerator featureGenerator;

    public FeatureExtractionProblem(int numberOfVariables, int numberOfObjectives){
        super(numberOfVariables, numberOfObjectives);
        this.featureGenerator = new EOSSFeatureGenerator();
    }

    public FeatureExtractionProblem(int numberOfVariables, int numberOfObjectives, EOSSFeatureGenerator featureGenerator){
        super(numberOfVariables, numberOfObjectives);
        this.featureGenerator = featureGenerator;
    }

    @Override
    public void evaluate(Solution solution){

        FeatureTreeVariable tree;

        if (solution instanceof ExtractedFeature) {
            ExtractedFeature feature = (ExtractedFeature) solution;
            tree = (FeatureTreeVariable) feature.getVariable(0);

        }else{
            throw new IllegalArgumentException("Wrong solution type: " + solution.getClass().getName());
        }

        BitSet featureMatches = tree.getRoot().getMatches();
        double[] metrics = this.computeMetrics(featureMatches, this.featureGenerator.getLabel());

        // Set two confidences as objectives
        solution.setObjective(0, -metrics[2]);
        solution.setObjective(1, -metrics[3]);
        solution.setObjective(2, tree.getRoot().getNumOfDescendantLiterals());

        //System.out.println("Number of literals: " + tree.getRoot().getNumOfDescendantLiterals());

        // TODO: Add another objective, which accounts for the complexity
    }

    @Override
    public Solution newSolution(){
        FeatureTreeVariable featureTree = new FeatureTreeVariable(new Connective(LogicOperator.AND), this.featureGenerator);
        return new ExtractedFeature(featureTree, EOSSFeatureExtractionParams.numberOfObjectives);
    }

    public EOSSFeatureGenerator getFeatureGenerator() {
        return featureGenerator;
    }

    private double[] computeMetrics(BitSet feature, BitSet labels) {

        int numberOfObservations = this.featureGenerator.getNumberOfObservations();

        double[] out = new double[4];

        BitSet copyMatches = (BitSet) feature.clone();
        copyMatches.and(labels);
        double cnt_SF = (double) copyMatches.cardinality();
        out[0] = cnt_SF / (double) numberOfObservations; //support

        // Check if it passes minimum support threshold
        //compute the confidence and lift
        double cnt_S = (double) labels.cardinality();
        double cnt_F = (double) feature.cardinality();
        out[1] = (cnt_SF / cnt_S) / (cnt_F / (double) numberOfObservations); //lift
        out[2] = (cnt_SF) / (cnt_F);   // confidence (feature -> selection)
        out[3] = (cnt_SF) / (cnt_S);   // confidence (selection -> feature)

        return out;
    }
}
