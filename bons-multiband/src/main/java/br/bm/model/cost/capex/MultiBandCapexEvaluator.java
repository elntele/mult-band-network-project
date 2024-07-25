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
  private List<Integer> variables;
  private int numNodes;
  private List<Integer> connections;

  @Override
  public NumericalResult evaluate(INetwork network) {
    MultiBandNetWorkProfile net = (MultiBandNetWorkProfile) network;
    variables = Arrays.asList(net.getRawData());
    numNodes = net.getNodes().size();
    connections = variables.subList(0, variables.size() - (numNodes + 1));

    var amplifierCost = amplifiersCost();
    var waveAndSwitchCost = wavelengthAndSwitchAndCost();
    var wavelengthCost = waveAndSwitchCost.getFirst();
    var switchCost = waveAndSwitchCost.getSecond();
    var totalNetworkLength = getTotalFiberLength(network);
    var dcfCost = Equipments.DCF_COST * totalNetworkLength;
    var ssmfCost = Equipments.SSMF_COT * totalNetworkLength;
    var deploymentCost_loc = Equipments.IMPLANT_COST * totalNetworkLength;
    // state the total network cost by adding the separated costs
    var totalCost_loc = amplifierCost + switchCost + wavelengthCost + dcfCost + ssmfCost
        + deploymentCost_loc;

    return new NumericalResult(TNetworkIndicator.CAPITAL_EXPENDITURE, totalCost_loc);
  }


  /**
   * this method calculate the amplifiers cost
   *
   */
  private double amplifiersCost() {

    var amplifierCost_loc = 0.0;
    var externalObserverCount = 0;
    //Example of works to variable offset:
    // to 4 nodes if you are in the node one, you will analise the
    // 3 connection: node 1 to node 2; node 1 to node 3 and node 1 to node 4.
    // offset here is these connections.
    var offset = 1;
    for (int i = 0; i < numNodes; i++) {
      var nextLimit = externalObserverCount + (numNodes - offset) * 3;
      for (int j = externalObserverCount; j < nextLimit; j++) {
        if (connections.get(j) != 0) {
          switch (Bands.getBand(connections.get(j))) {
            case Bands.CBAND -> amplifierCost_loc += 2*(3.84 * Equipments.getAmplifiersBandC()[0][1]);
            case Bands.CLBAND -> amplifierCost_loc += 2*(3.84 * Equipments.getAmplifiersBandCL()[0][1]);
            case Bands.CSBAND, Bands.CLSBAND -> amplifierCost_loc += 2*(3.84 * Equipments.getAmplifiersBandCLS()[0][1]);
            default -> throw new RuntimeException("invalid band " + Bands.getBand(j));
          }

        }
        externalObserverCount =j;
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
   */
  private Pair<Double, Double> wavelengthAndSwitchAndCost() {

    var nodeEquipments = variables.subList( variables.size() - (numNodes + 1),  variables.size() - 1);
    int numberOfw = variables.get( variables.size() - 1);
    var wavelengthCost = 0.0;
    var switchCost = 0.0;

    Integer[] nodeDegrees= new Integer[numNodes];

    List<List<Double>> switches = Equipments.getThisSwitchesList(nodeEquipments);
    var externalCount = 0;
    var offset = 1;
    //being m the line index to run through matrix line
    for (int i = 0; i < numNodes; i++) {
      int nodeDegreeC = 0;
      int nodeDegreeCl = 0;
      int nodeDegreeCls = 0;
      int nodeDegree = 0;

      var nextLimit = externalCount + (numNodes - offset) * 3;
      // calculate the node degree on index nodePosition
      //j  run through matrix column
      for (int j = externalCount; j < nextLimit; j++) {
        if (connections.get(j) != 0) {
          //here the node degree is calculated in general way
          nodeDegree++;
          //here the node degree is calculated accord the type of band
          switch (Bands.getBand(connections.get(j))) {
            case Bands.CBAND:
              nodeDegreeC++;
            case Bands.CLBAND:
              nodeDegreeCl++;
            case Bands.CSBAND, Bands.CLSBAND:
              nodeDegreeCls++;
          }
        }
        externalCount=j;
      }
      externalCount++;
      offset++;
      //here the cost of w is differentiated accord the type of band to calculate the w cost
      wavelengthCost += 2 * numberOfw *
          (nodeDegreeC * Equipments.COST_MODULE_W_FOR_C_BAND +
              nodeDegreeCl * Equipments.COST_MODULE_W_FOR_CL_BAND +
              nodeDegreeCls * Equipments.COST_MODULE_W_FOR_CLS_BAND);

      //here the node degree in general way is used to calculate the switch cost
      switchCost += ((0.05225 * numberOfw + 6.24) * nodeDegree + 2.5) * switches.get(i).get(
          0);
      // the two before equations comes from cost model
    }

    return new Pair<>(wavelengthCost, switchCost);
  }

  /**
   * this method returns the total length of fiber
   * in network
   *
   * @param network
   */
  private Double getTotalFiberLength(INetwork network) {
    MultiBandNetWorkProfile net = (MultiBandNetWorkProfile) network;
    var totalNetworkLength = 0.0;
    var externalCount = 0;
    var offset = 1;
    for (int i = 0; i < numNodes; i++) {
      var nextLimit = externalCount + (numNodes - offset) * 3;
      var indexJ = i + 1;
      var stepTree = 0;
      for (int j = externalCount; j < nextLimit; j++) {
        stepTree++;
        if (connections.get(j) != 0) {
          totalNetworkLength += net.getCompleteDistances()[i][indexJ];
        }
        if (stepTree % 3 == 0) {
          indexJ++;
        }
        externalCount = j;
      }
      offset++;
      externalCount++;
    }
    return totalNetworkLength;
  }
}
