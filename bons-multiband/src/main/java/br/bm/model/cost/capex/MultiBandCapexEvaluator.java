package br.bm.model.cost.capex;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import br.bm.core.INetwork;
import br.bm.core.MultiBandNetWorkProfile;
import br.bm.core.Equipments;
import br.bm.model.INetworkEvaluator;
import br.bm.model.NumericalResult;
import br.bm.model.TNetworkIndicator;
import br.cns24.model.Bands;

public class MultiBandCapexEvaluator implements INetworkEvaluator<INetwork, NumericalResult> {

  @Override
  public NumericalResult evaluate(INetwork network) {

    var amplifierCost_loc = amplifiersCost(network);
    var waveAndSwitchCost = wavelengthAndSwitchAndCost(network);
    var wavelengthCost_loc = waveAndSwitchCost.getFirst();
    var switchCost_loc = waveAndSwitchCost.getSecond();
    var totalNetworkLength= getTotalFiberLength(network);
    var dcfCost_loc = Equipments.DCF_COST* totalNetworkLength;
    var ssmfCost_loc = Equipments.SSMF_COT * totalNetworkLength;
    var deploymentCost_loc = Equipments.IMPLANT_COST * totalNetworkLength;
    // state the total network cost by adding the separated costs
    var totalCost_loc = amplifierCost_loc + switchCost_loc + wavelengthCost_loc + dcfCost_loc + ssmfCost_loc
        + deploymentCost_loc;

    return new NumericalResult(TNetworkIndicator.CAPITAL_EXPENDITURE, totalCost_loc);
  }


  private int indiceVetor(int j, int i, int numberOfNodes) {
    return (j + (numberOfNodes - 1) * i - i * (i + 1) / 2);
  }


  /**
   * this method calculate the amplifiers cost
   *
   * @param network
   */
  private double amplifiersCost(INetwork network) {
    MultiBandNetWorkProfile net = (MultiBandNetWorkProfile) network;
    List<Integer> decisionVariablesList = Arrays.asList(net.getRawData());
    var amplifierCost_loc = 0.0;
    int numNodes = net.getNodes().size();
    var externalObserverCount = 0;
    //Example of works to variable offset:
    // to 4 nodes if you are in the node one, you will analise the
    // 3 connection: node 1 to node 2; node 1 to node 3 and node 1 to node 4.
    // offset here is these connections.
    var offset = 1;
    for (int i = 0; i < numNodes; i++) {
      var nextLimit = externalObserverCount + (numNodes - offset) * 3;
      for (int j = externalObserverCount; j < nextLimit; j++) {
        if (decisionVariablesList.get(j) != 0) {
          switch (Bands.getBand(decisionVariablesList.get(j))) {
            case Bands.CBAND -> amplifierCost_loc += 3.84 * Equipments.getAmplifiersBandC()[0][1];
            case Bands.CLBAND -> amplifierCost_loc += 3.84 * Equipments.getAmplifiersBandCL()[0][1];
            case Bands.CSBAND,Bands.CLSBAND -> amplifierCost_loc += 3.84 * Equipments.getAmplifiersBandCLS()[0][1];
            default -> throw new RuntimeException("invalid band " + Bands.getBand(j));
          }
        }
      }
      offset++;
      externalObserverCount++;
    }
    return amplifierCost_loc;
  }

  /**
   * this method calculate the cost with
   * switches and wavelength and return
   * a tuple with wave length cost in
   * first position and  switch cost in
   * second position
   *
   * @param network
   */

  private Pair<Double, Double> wavelengthAndSwitchAndCost(INetwork network) {

    MultiBandNetWorkProfile net = (MultiBandNetWorkProfile) network;
    List<Integer> decisionVariablesList = Arrays.asList(net.getRawData());
    int numberOfVariables = decisionVariablesList.size();
    int numNodes = net.getNodes().size();
    var nodeEquipments = decisionVariablesList.subList(numberOfVariables - (numNodes + 1), numberOfVariables - 1);
    int number_w_of_wave_lenghts = decisionVariablesList.get(numberOfVariables - 1);
    var wavelengthCost = 0.0;
    var switchCost = 0.0;

    List<List<Double>> switches = Equipments.getThisSwitchesList(nodeEquipments);
    var externalObserverCount = 0;
    var offset = 1;
    //being m the line index to run through matrix line
    for (int i = 0; i < numNodes; i++) {
      int nodeDegreeC = 0;
      int nodeDegreeCl = 0;
      int nodeDegreeCls = 0;
      int nodeDegree = 0;

      var nextLimit = externalObserverCount + (numNodes - offset) * 3;
      // calculate the node degree on index nodePosition
      for (int j = externalObserverCount; j < nextLimit; j++) {//j  run through matrix column
        if (decisionVariablesList.get(j) != 0) {
          //here the node degree is calculated in general way
          nodeDegree++;
          //here the node degree is calculated accord the type of band
          switch (Equipments.getSwitchType(i)) {
            case Bands.CBAND:
              nodeDegreeC++;
            case Bands.CLBAND:
              nodeDegreeCl++;
            case Bands.CSBAND, Bands.CLSBAND:
              nodeDegreeCls++;
          }
        }
      }
      offset++;
      //here the cost of w is differentiated accord the type of band to calculate the w cost
      wavelengthCost += 2 * number_w_of_wave_lenghts *
          (nodeDegreeC * Equipments.COST_MODULE_W_FOR_C_BAND +
              nodeDegreeCl * Equipments.COST_MODULE_W_FOR_CL_BAND +
              nodeDegreeCls * Equipments.COST_MODULE_W_FOR_CLS_BAND);

      //here the node degree in general way is used to calculate the switch cost
      switchCost += ((0.05225 * number_w_of_wave_lenghts + 6.24) * nodeDegree + 2.5) * switches.get(0).get(
          i);
      // the two before equations comes from cost model
    }

    return new Pair<>(wavelengthCost, switchCost);
  }

  private Double getTotalFiberLength(INetwork network){
    MultiBandNetWorkProfile net = (MultiBandNetWorkProfile) network;
    List<Integer> decisionVariablesList = Arrays.asList(net.getRawData());
    int numNodes = net.getNodes().size();
    var externalObserverCount = 0;
    double totalNetworkLength = 0;
    var offset = 1;
    for (int i = 0; i < numNodes; i++) {
      var nextLimit = externalObserverCount + (numNodes - offset) * 3;
      for (int j = externalObserverCount; j < nextLimit; j++) {
        if (decisionVariablesList.get(j) != 0) {
          totalNetworkLength += net.getCompleteDistances()[i][j];
        }
      }
      offset++;
      externalObserverCount++;
    }
    return totalNetworkLength;
  }


}
