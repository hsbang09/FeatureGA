package eoss.operators;

import eoss.EOSSFeatureExtractionParams;
import eoss.EOSSFeatureGenerator;
import eoss.ExtractedFeature;
import eoss.FeatureTreeVariable;
import ifeed_dm.Feature;
import ifeed_dm.logic.Connective;
import ifeed_dm.logic.Literal;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Variation;
import org.moeaframework.core.Solution;
import java.util.List;


public class FeatureMutation implements Variation{

    EOSSFeatureGenerator featureGenerator;

    public FeatureMutation(EOSSFeatureGenerator featureGenerator){
        this.featureGenerator = featureGenerator;
    }

    @Override
    public Solution[] evolve(Solution[] parents){

        Solution[] out = new Solution[1];

        int randInt = PRNG.nextInt(featureGenerator.getBaseFeatures().size());
        Feature featureToAdd = featureGenerator.getBaseFeatures().get(randInt);

        FeatureTreeVariable tree = (FeatureTreeVariable) parents[0].getVariable(0);
        Connective root = tree.getRoot().copy();

        Literal randomNode = (Literal) featureGenerator.selectRandomNode(root, Literal.class);
        Connective parent = featureGenerator.findParentNode(root, randomNode);

        parent.getLiteralChildren().remove(randomNode);
        parent.addLiteral(featureToAdd.getName(), featureToAdd.getMatches());

        FeatureTreeVariable newTree = new FeatureTreeVariable(root, featureGenerator);
        Solution sol = new ExtractedFeature(newTree, EOSSFeatureExtractionParams.numberOfObjectives);

        out[0] = sol;
        return out;
    }

    @Override
    public int getArity(){
        return 1;
    }
}
