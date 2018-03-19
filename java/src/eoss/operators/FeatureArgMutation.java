package eoss.operators;

import eoss.EOSSFeatureExtractionParams;
import eoss.EOSSFeatureGenerator;
import eoss.ExtractedFeature;
import eoss.FeatureTreeVariable;
import ifeed_dm.Filter;
import ifeed_dm.Feature;
import ifeed_dm.binaryInput.BinaryInputArchitecture;
import ifeed_dm.logic.Connective;
import ifeed_dm.logic.Literal;
import ifeed_dm.RepairOperator;
import ifeed_dm.EOSS.EOSSFeatureFetcher;
import ifeed_dm.EOSS.EOSSRepairOperatorFetcher;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Variation;
import org.moeaframework.core.Solution;
import java.util.List;


public class FeatureArgMutation implements Variation{

    List<BinaryInputArchitecture> architectures;
    EOSSFeatureFetcher featureFetcher;
    EOSSRepairOperatorFetcher repairOperatorFetcher;
    EOSSFeatureGenerator featureGenerator;

    public FeatureArgMutation(List<BinaryInputArchitecture> architectures, EOSSFeatureGenerator featureGenerator){
        this.architectures = architectures;
        this.featureFetcher = new EOSSFeatureFetcher(architectures);
        this.repairOperatorFetcher = new EOSSRepairOperatorFetcher();
        this.featureGenerator = featureGenerator;
    }

    @Override
    public Solution[] evolve(Solution[] parents){

        // Single child created
        Solution[] out = new Solution[1];

        // Select a single literal to modify
        FeatureTreeVariable tree = (FeatureTreeVariable) parents[0].getVariable(0);
        Connective root = tree.getRoot().copy();

        Literal randomNode = (Literal) featureGenerator.selectRandomNode(root, Literal.class);
        Connective parent = featureGenerator.findParentNode(root, randomNode);

        parent.getLiteralChildren().remove(randomNode);

        RepairOperator op = repairOperatorFetcher.fetch(randomNode.getName());
        op.mutate();
        Feature modifiedFeature = featureFetcher.fetch(((Filter) op).getName());

        // Add new literal to the given node
        parent.addLiteral(modifiedFeature.getName(), modifiedFeature.getMatches());

        FeatureTreeVariable newTree = new FeatureTreeVariable(root, this.featureGenerator);
        Solution sol = new ExtractedFeature(newTree, EOSSFeatureExtractionParams.numberOfObjectives);

        out[0] = sol;
        return out;
    }

    @Override
    public int getArity(){
        return 1;
    }
}
