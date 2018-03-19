package eoss.operators;

import eoss.EOSSFeatureExtractionParams;
import eoss.EOSSFeatureGenerator;
import eoss.ExtractedFeature;
import eoss.FeatureTreeVariable;
import ifeed_dm.Feature;
import ifeed_dm.LogicOperator;
import ifeed_dm.logic.Connective;
import ifeed_dm.logic.Literal;
import ifeed_dm.logic.Formula;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Variation;
import org.moeaframework.core.Solution;
import java.util.List;

public class FeatureCrossOver implements Variation{

    EOSSFeatureGenerator featureGenerator;

    public FeatureCrossOver(EOSSFeatureGenerator featureGenerator){
        this.featureGenerator = featureGenerator;
    }

    @Override
    public Solution[] evolve(Solution[] parents){

        Solution[] out = new Solution[2];

        FeatureTreeVariable tree1 = (FeatureTreeVariable) parents[0].getVariable(0);
        FeatureTreeVariable tree2 = (FeatureTreeVariable) parents[1].getVariable(0);

//        Solution sol1 = new ExtractedFeature((FeatureTreeVariable) tree1.copy(), EOSSFeatureExtractionParams.numberOfObjectives);
//        Solution sol2 = new ExtractedFeature((FeatureTreeVariable) tree2.copy(), EOSSFeatureExtractionParams.numberOfObjectives);

        // Copy the root nodes
        Connective root1 = tree1.getRoot().copy();
        Connective root2 = tree2.getRoot().copy();

        Formula subtree1 = featureGenerator.selectRandomNode(root1, null);
        Formula subtree2 = featureGenerator.selectRandomNode(root2, null);

        Connective parent1 = featureGenerator.findParentNode(root1, subtree1);
        Connective parent2 = featureGenerator.findParentNode(root2, subtree2);

        if(parent1 == null){ // Subtree1 is root1
//            System.out.println(root1.getName());
//            System.out.println(subtree1.getName());
            LogicOperator temp;
            if(root1.getLogic() == LogicOperator.AND){
                temp = LogicOperator.OR;
            }else{
                temp = LogicOperator.AND;
            }
            parent1 = new Connective(temp);
            parent1.addChild( (Connective) subtree1);
        }

        if(parent2 == null){
            LogicOperator temp;
            if(root2.getLogic() == LogicOperator.AND){
                temp = LogicOperator.OR;
            }else{
                temp = LogicOperator.AND;
            }
            parent2 = new Connective(temp);
            parent2.addChild( (Connective) subtree2);
        }

        // Swap the subtrees
        if(subtree1 instanceof Connective){
            Connective thisNode = (Connective) subtree1;
            parent1.getConnectiveChildren().remove(thisNode);
            parent2.addChild(thisNode);
        }else{
            Literal thisNode = (Literal) subtree1;
            parent1.getLiteralChildren().remove(thisNode);
            parent2.addLiteral(thisNode);
        }

        // Swap the subtrees
        if(subtree2 instanceof Connective){
            Connective thisNode = (Connective) subtree2;
            parent2.getConnectiveChildren().remove(thisNode);
            parent1.addChild(thisNode);
        }else{
            Literal thisNode = (Literal) subtree2;
            parent2.getLiteralChildren().remove(thisNode);
            parent1.addLiteral(thisNode);
        }

        FeatureTreeVariable newTree1 = new FeatureTreeVariable(root1, this.featureGenerator);
        FeatureTreeVariable newTree2 = new FeatureTreeVariable(root2, this.featureGenerator);

        Solution sol1 = new ExtractedFeature(newTree1, EOSSFeatureExtractionParams.numberOfObjectives);
        Solution sol2 = new ExtractedFeature(newTree2, EOSSFeatureExtractionParams.numberOfObjectives);

        out[0] = sol1;
        out[1] = sol2;
        return out;
    }

    @Override
    public int getArity(){
        return 2;
    }
}
