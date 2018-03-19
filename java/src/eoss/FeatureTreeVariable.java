/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eoss;

import org.moeaframework.core.Variable;
import org.moeaframework.core.PRNG;

import ifeed_dm.logic.Connective;
import ifeed_dm.LogicOperator;
import ifeed_dm.Feature;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author hsbang
 */

public class FeatureTreeVariable implements Variable, Serializable {

    private static final long serialVersionUID = 4142639957025157845L;

    Connective root;
    EOSSFeatureGenerator featureGenerator;

    public FeatureTreeVariable(Connective root, EOSSFeatureGenerator featureGenerator) {
        this.root = root;
        this.featureGenerator = featureGenerator;
    }

    public void setFeature(Connective root){
        this.root = root;
    }

    public Connective getRoot(){ return this.root; }

    @Override
    public void randomize(){
        // Randomly generate a feature tree and save it
        this.root = this.featureGenerator.generateRandomFeature();
    }

    @Override
    public Variable copy() {
        return new FeatureTreeVariable(this.root.copy(), this.featureGenerator);
    }

    /**
     * Returns a string containing the value of the decision
     * @return
     */
    @Override
    public String toString() {
        return this.root.getName();
    }

    @Override
    public int hashCode() {
        int hash = 7;
//        hash = 89 * hash + this.value;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FeatureTreeVariable other = (FeatureTreeVariable) obj;
        if (this.root.toString() != other.toString()) {
            // TODO: The method to compare two features need to be modified
            return false;
        }
        return true;
    }

}
