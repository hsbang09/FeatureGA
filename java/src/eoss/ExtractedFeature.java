/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eoss;

import eoss.FeatureTreeVariable;

import java.util.Objects;
import org.moeaframework.core.Solution;

/**
 * The solution is defined by the types of decisions and the values of those
 * decisions
 *
 * @author hsbang
 */

public class ExtractedFeature extends Solution {

    private static final long serialVersionUID = -2195550924166538032L;

    public ExtractedFeature(Solution solution) {
        super(solution);
        if (solution instanceof ExtractedFeature) {
            ExtractedFeature feature = (ExtractedFeature) solution;
            FeatureTreeVariable tree = (FeatureTreeVariable) feature.getVariable(0);
            super.setVariable(0, tree.copy());
        }
    }

    public ExtractedFeature(FeatureTreeVariable input, int numberOfObjectives) {
        super(1, numberOfObjectives);
        super.setVariable(0, input);
    }

    @Override
    public Solution copy() {
        return new ExtractedFeature(this);
    }

    /**
     * Returns the values of each decision
     *
     * @return
     */
    @Override
    public String toString() {
        // TODO: To be implemented
        String out = "";
        for (int i = 0; i < getNumberOfObjectives(); i++) {
            out += getVariable(i).toString() + "::";
        }
        return out;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        for (int i = 0; i < getNumberOfVariables(); i++) {
            hash = 67 * hash + Objects.hashCode(this.getVariable(i));
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        // TODO: Needs implementation
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExtractedFeature other = (ExtractedFeature) obj;
        for (int i = 0; i < getNumberOfVariables(); i++) {
            if (!this.getVariable(i).equals(other.getVariable(i))) {
                return false;
            }
        }
        return true;
    }

}
