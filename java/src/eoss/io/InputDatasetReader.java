package eoss.io;

import eoss.EOSSFeatureExtractionParams;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.BitSet;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import ifeed_dm.binaryInput.BinaryInputArchitecture;

public class InputDatasetReader {

    private String filePath;
    private BitSet label;
    private List<BinaryInputArchitecture> archs;
    private int numberOfObservations;

    public InputDatasetReader(){
        this.filePath = EOSSFeatureExtractionParams.dataset;
        archs = new ArrayList<>();
    }

    public boolean readData(){

        String dataFileContent = "";

        try(FileReader fr = new FileReader(new File(this.filePath))){
            BufferedReader bufferedReader = new BufferedReader(fr);
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line.trim());
                stringBuffer.append("\n");
            }
            fr.close();
            dataFileContent = stringBuffer.toString().trim();

        }catch(IOException exc){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, exc);
            return false;
        }

        try{
            String[] lines = dataFileContent.split("\n");

            this.numberOfObservations = lines.length;
            this.label = new BitSet(lines.length);

            for(int i = 0; i < lines.length; i++){
                String[] lineSplit = lines[i].split(",");
                // label, bitString, objectives

                String _label = lineSplit[0];
                String _inputs = lineSplit[1];

                if(Integer.parseInt(_label) == 1){
                    label.set(i);
                }

                BitSet inputs = new BitSet(_inputs.length());
                char[] _inputs_char = _inputs.toCharArray();
                for(int j = 0; j < _inputs.length(); j++){
                    char bit = _inputs_char[j];
                    if(Character.getNumericValue(bit) == 1){
                        inputs.set(j);
                    }
                }

                double[] outputs = new double[0];
                archs.add(new BinaryInputArchitecture(i, inputs, outputs));
            }

        }catch(Exception exc){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, exc);
            return false;
        }
        return true;
    }

    public BitSet getLabel(){
        return this.label;
    }

    public List<BinaryInputArchitecture> getArchs(){
        return this.archs;
    }

    public int getNumberOfObservations(){
        return this.numberOfObservations;
    }
}
