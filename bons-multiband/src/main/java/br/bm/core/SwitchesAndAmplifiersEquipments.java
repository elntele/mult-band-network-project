package br.bm.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import br.cns24.model.Bands;

public class SwitchesAndAmplifiersEquipments {


  private final static double[][] SWITCHES_COSTS_AND_LABELS = new double[][]{
      { 0.0, 2.5, 5, 7.5, 10, 3, 6, 9, 12, 25, 10.5, 15.75, 21 }, // costs
      { 0.0, 27, 30, 33, 35, 27, 30, 33, 35, 27, 30, 33, 35 }// isolation factor
  };

  private final static double[][] SWITCHES_COSTS_AND_LABELS_BAND_C = new double[][]{
      { 0.0, 2.5, 5, 7.5, 10 }, // costs
      { 0.0, 27, 30, 33, 35 }
  };

  private final static double[][] SWITCHES_COSTS_AND_LABELS_BAND_CL = new double[][]{
      { 0.0, 3, 6, 9, 12 }, // costs
      { 0.0, 27, 30, 33, 35 }
  };
  private final static double[][] SWITCHES_COSTS_AND_LABELS_BAND_CLS = new double[][]{
      { 0.0, 25, 10.5, 15.75, 21 }, // costs
      { 0.0, 27, 30, 33, 35 }
  };

  private final static double[][] AMPLIFIERS_COSTS_AND_LABELS_BAND_C = {
      {0, 0.25, 0.5, 0.75, 1},
      {0, 14, 17, 14, 17},
      {0, 9, 9, 5, 5}
  };

  private final static double[][] AMPLIFIERS_COSTS_AND_LABELS_BAND_Cl = {
      {0, 0.57, 1.14, 1.71, 2.28},
      {0, 17.7, 22, 17.5, 22},
      {0, 12.5, 12.5, 7, 7}
  };

  private final static double[][] AMPLIFIERS_COSTS_AND_LABELS_BAND_ClS = {
      {0, 0.8, 1.7, 2.4, 3.3},
      {0, 13, 16, 13, 16},
      {0, 12.6, 12.6, 7, 7}
  };

  private final static double[][] AMPLIFIERS_COSTS_AND_LABELS ={
      {0, 0.25, 0.5, 0.75, 1, 0.57, 1.14, 1.71, 2.28, 0.8, 1.7, 2.4, 3.3}, //cost
      {0, 14, 17, 14, 17, 17.7, 22, 17.5, 22,  13, 16, 13, 16},// saturation power
      {0, 9, 9, 5, 5, 12.5, 12.5, 7, 7, 12.6, 12.6, 7, 7} //noise figure
  };


  public final static double COST_MODULE_W_FOR_C_BAND=1.0;
  public final static double COST_MODULE_W_FOR_CL_BAND=1.2;
  public final static double COST_MODULE_W_FOR_CLS_BAND=1.4;




  public static double[][] getEpsilonOrIsolationFactorForThisSwitchList() {
    return SWITCHES_COSTS_AND_LABELS;
  }

  public static double[][] getSwitchesBandC() {

    return SWITCHES_COSTS_AND_LABELS_BAND_C;
  }

  public static double[][] getSwitchesBandCL() {

    return SWITCHES_COSTS_AND_LABELS_BAND_CL;
  }

  public static double[][] getSwitchesBandCCLS() {

    return SWITCHES_COSTS_AND_LABELS_BAND_CLS;
  }

  public static double[][] getSwitch( int index) {
    return new double[][] {{SWITCHES_COSTS_AND_LABELS[0][index]},{SWITCHES_COSTS_AND_LABELS[1][index]}} ;
  }

  public static List<Double> getEpsilonOrIsolationFactorForThisSwitchList(List <Integer> indexes){
    return indexes.stream()
        .map(index -> {
          if (index >= 0 && index < SWITCHES_COSTS_AND_LABELS[1].length) {
            return SWITCHES_COSTS_AND_LABELS[1][index];
          } else {
            throw new IndexOutOfBoundsException("Index out of range: " + index);
          }
        })
        .collect(Collectors.toList());

  }

  public static double[][] getAllSwitch(List <Integer> indexes) {
  return SWITCHES_COSTS_AND_LABELS;
  }


  public static double[][] getAmplifiersBandC(){
    return AMPLIFIERS_COSTS_AND_LABELS_BAND_C;
  }
  public static double[][] getAmplifiersBandCL(){
    return AMPLIFIERS_COSTS_AND_LABELS_BAND_Cl;
  }
  public static double[][] getAmplifiersBandCLS(){
    return AMPLIFIERS_COSTS_AND_LABELS_BAND_ClS;
  }
  public static double[][] getAllAmplifiers() {
    return AMPLIFIERS_COSTS_AND_LABELS;
  }

  public static List<List<Double>> getThisAmpliflierList(List <Integer> indexes){

    return indexes.stream()
        .map(index -> {
          if (index >= 0 && index < AMPLIFIERS_COSTS_AND_LABELS[1].length) {
            List<Double> matrixColumn = new ArrayList<>();
            matrixColumn.add(AMPLIFIERS_COSTS_AND_LABELS[0][index]);
            matrixColumn.add(AMPLIFIERS_COSTS_AND_LABELS[1][index]);
            matrixColumn.add(AMPLIFIERS_COSTS_AND_LABELS[2][index]);
            return matrixColumn;
          } else {
            throw new IndexOutOfBoundsException("Index out of range: " + index);
          }
        })
        .collect(Collectors.toList());
  }

  public static List<List<Double>> getThisSwitchesList(List <Integer> indexes){

    return indexes.stream()
        .map(index -> {
          if (index >= 0 && index < SWITCHES_COSTS_AND_LABELS[1].length) {
            List<Double> matrixColumn = new ArrayList<>();
            matrixColumn.add(SWITCHES_COSTS_AND_LABELS[0][index]);
            matrixColumn.add(SWITCHES_COSTS_AND_LABELS[1][index]);
            return matrixColumn;
          } else {
            throw new IndexOutOfBoundsException("Index out of range: " + index);
          }
        })
        .collect(Collectors.toList());
  }

  public static Bands getSwitchType(int type){
    switch (type){
      case 0: return Bands.NOBAND;
      case 1,2,3,4: return Bands.CBAND;
      case 5,6,7,8: return Bands.CLBAND;
      case 9,10,11,12: return Bands.CLSBAND;
      default: throw new RuntimeException("illegal ROADM");
    }
  }


  public static HashMap<Integer, Double> getAmplifiersAsAnHashOfIndexes(List <Integer> indexes){
    HashMap hashMap = new HashMap<>();
     IntStream.range(0, indexes.size()).forEach(index->{
       if (indexes.get(index)>=0 || indexes.get(index)<AMPLIFIERS_COSTS_AND_LABELS[0].length){
         hashMap.put(indexes. get(index),AMPLIFIERS_COSTS_AND_LABELS[0][index]);
       }else{
         throw new IndexOutOfBoundsException("Index out of range: " + indexes.get(index));
       }

    });

     return hashMap;
  }
}
