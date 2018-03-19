/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eoss;

/**
 *
 * @author hsbang
 */

import java.util.List;
import java.util.ArrayList;
import java.util.BitSet;

import ifeed_dm.EOSS.EOSSDataMining;
import ifeed_dm.EOSS.EOSSFeatureFetcher;
import ifeed_dm.EOSS.EOSSParams;
import ifeed_dm.LogicOperator;
import ifeed_dm.binaryInput.BinaryInputArchitecture;
import ifeed_dm.Feature;

import eoss.io.InputDatasetReader;
import ifeed_dm.logic.Connective;
import ifeed_dm.logic.Literal;
import ifeed_dm.logic.Formula;

import org.moeaframework.core.PRNG;

public class EOSSFeatureGenerator {
    /**
     * Generate the base features and store them
     */

    private List<BinaryInputArchitecture> architectures;
    private List<Feature> baseFeatures;
    private BitSet label;
    private int numberOfObservations;
    private EOSSFeatureFetcher featureFetcher;

    public EOSSFeatureGenerator(){

        InputDatasetReader reader = new InputDatasetReader();

        if(reader.readData()){

            this.label = reader.getLabel();
            this.architectures = reader.getArchs();
            List<Integer> behavioral = new ArrayList<>();
            List<Integer> non_behavioral = new ArrayList<>();

            int inputLength = EOSSParams.num_instruments * EOSSParams.num_orbits;
            for(int i = 0; i < inputLength; i++){
                if(this.label.get(i)){
                    behavioral.add(architectures.get(i).getID());
                }else{
                    non_behavioral.add(architectures.get(i).getID());
                }
            }

            EOSSDataMining eossDataMining = new EOSSDataMining(behavioral, non_behavioral, architectures, 0.0, 0.0, 0.0);
            this.baseFeatures = eossDataMining.generateBaseFeatures(false);

            this.numberOfObservations = reader.getNumberOfObservations();

            this.featureFetcher = new EOSSFeatureFetcher(this.baseFeatures, this.architectures);

        }else{
            throw new RuntimeException("Exception in " + this.getClass().getName() + ": Data file not read correctly.");
        }
    }

    public Connective generateRandomFeature(){
        // Randomly generate a feature tree

        Connective root;

        LogicOperator logic;
        // Select AND or OR as the logical connective used as a root node
        if(PRNG.nextInt(2) == 0){ // 0 or 1
            logic = LogicOperator.AND;
        }else{
            logic = LogicOperator.OR;
        }
        root = new Connective(logic);

        int numLiterals = PRNG.nextInt(EOSSFeatureExtractionParams.maxNumLiteral) + 1; // min: 1, max: maxNumLiteral

        for(int i = 0; i < numLiterals; i++){

            int baseFeatureIndex = PRNG.nextInt(baseFeatures.size());
            Feature featureToAdd = baseFeatures.get(baseFeatureIndex);

            if(i==0){
                root.addLiteral(featureToAdd.getName(), featureToAdd.getMatches(), false);

            }else{
                Formula node = this.selectRandomNode(root, null);
                if(node instanceof Connective){
                    ((Connective) node).setAddNewLiteral();

                }else{
                    Connective parent = this.findParentNode(root, node);
                    int index = parent.getLiteralChildren().indexOf(node);
                    parent.setAddNewLiteral(index);
                }

                root.setPlaceholder(featureToAdd.getName(), featureToAdd.getMatches());
                if(!root.fillPlaceholder()){
                    throw new RuntimeException("Exception in " + this.getClass().getName() + ": Replacing the placeholder with a new literal");
                }
            }
        }
        return root;
    }

    public Formula selectRandomNode(Connective root, Class type){

        int numOfNodes;
        if(type == Connective.class){
            numOfNodes = root.getDescendantConnectives(true).size();

        }else if(type == Literal.class){
            numOfNodes = root.getNumOfDescendantLiterals();

        }else{
            numOfNodes = root.getNumOfDescendantNodes() + 1; // num of connective nodes + num of literals + self (root node);
        }

        int randInt = PRNG.nextInt(numOfNodes);

        return selectNodeOfGivenIndex(root, randInt, type);
    }

    private Formula selectNodeOfGivenIndex(Connective root, int targetIndex, Class type) {

        String ntype = "";
        int maxIndex = 999;

        if(type == Connective.class){
            ntype = "connective";
            maxIndex = root.getDescendantConnectives(false).size();

        }else if(type == Literal.class){
            ntype = "literal";
            maxIndex = root.getNumOfDescendantLiterals();

        }else{
            ntype = "both";
            maxIndex = root.getNumOfDescendantNodes();

        }

        if(targetIndex > maxIndex){
            throw new RuntimeException("Exception in " + this.getClass().getName() + ": target index larger than the number of nodes.");
        }

        switch (ntype){
            case "connective":
                if (targetIndex == 0) {
                    // The root node is selected
                    return root;

                } else {
                    List<Connective> connectiveNodes = root.getDescendantConnectives(false);
                    for(int i = 1; i < connectiveNodes.size(); i++){
                        if(i == targetIndex) {
                            return connectiveNodes.get(i);
                        }
                    }
                }
                break;

            case "literal":
                List<Connective> connectiveNodes = root.getDescendantConnectives(false);
                int i = 0;
                for(Connective node: connectiveNodes){
                    for(Literal literal: node.getLiteralChildren()){
                        if(i == targetIndex){
                            return literal;
                        }
                        i++;
                    }
                }
                break;

            case "both":
                if (targetIndex == 0) {
                    // The root node is selected
                    return root;

                } else {
                    connectiveNodes = root.getDescendantConnectives(false);

                    i = 1;
                    for(Connective node: connectiveNodes){
                        if(i == targetIndex) {
                            return node;
                        }
                        i++;

                        for(Literal literal: node.getLiteralChildren()){
                            if(i == targetIndex){
                                return literal;
                            }
                            i++;
                        }
                    }
                }

            default:
                break;
        }

        throw new RuntimeException("Exception in " + this.getClass().getName() + ": could not find node of a given target index");
    }

    public Connective findParentNode(Connective root, Formula target){

        if(target instanceof Connective){
            for(Connective branch:root.getConnectiveChildren()){
                if(branch == target){
                    return root;
                }
            }

        }else{
            for(Literal literal:root.getLiteralChildren()){
                if(literal == target){
                    return root;
                }
            }
        }

        for(Connective branch: root.getConnectiveChildren()){
            Connective temp = findParentNode(branch, target);
            if(temp != null){
                return temp;
            }
        }

        return null;
    }

    public List<Feature> getBaseFeatures() {
        return baseFeatures;
    }

    public BitSet getLabel(){
        return this.label;
    }

    public int getNumberOfObservations(){
        return this.numberOfObservations;
    }
}
