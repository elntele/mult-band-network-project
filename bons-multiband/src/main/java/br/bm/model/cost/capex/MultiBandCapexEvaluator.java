package br.bm.model.cost.capex;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import br.bm.core.INetwork;
import br.bm.core.MultiBandNetWorkProfile;
import br.cns24.services.Equipments;
import br.bm.model.INetworkEvaluator;
import br.bm.model.NumericalResult;
import br.bm.model.TNetworkIndicator;
import br.cns24.services.Bands;

public class MultiBandCapexEvaluator implements INetworkEvaluator<INetwork, NumericalResult> {
  private List<Integer> variables;
  private int numNodes;
  private List<Integer> connections;
  private int setSize;

  @Override
  public NumericalResult evaluate(INetwork network) {
    MultiBandNetWorkProfile net = (MultiBandNetWorkProfile) network;
    variables = Arrays.asList(net.getRawData());
    numNodes = net.getNodes().size();
    connections = variables.subList(0, variables.size() - (numNodes + 1));
    this.setSize = net.getSetSize();

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
   */
  private double amplifiersCost() {

    var amplifierCost_loc = 0.0;
    for (int i = 0; i < numNodes; i++) {
      for (int j = i + 1; j < numNodes; j++) {
        var index = Equipments.getLinkPosition(i, j, numNodes, setSize);
        for (int w = 0; w < setSize; w++) {
          if (connections.get(index + w) != 0) {
            switch (Bands.getBand(connections.get(index + w))) {
              case Bands.CBAND -> amplifierCost_loc += 2 * (3.84 * Equipments.getAmplifierSolutionCostToCBand());
              case Bands.CLBAND -> amplifierCost_loc += 2 * (3.84 * Equipments.getAmplifierSolutionCostToCLBand());
              case Bands.CLSBAND -> amplifierCost_loc += 2 * (3.84 * Equipments.getAmplifierSolutionCostToCLSBand());
              default -> throw new RuntimeException(
                  String.format("invalid band %1$s,  connection position %2$s, connections %3$s", Bands.getBand(
                      connections.get(j)), j, connections));
            }

          }
        }
      }
    }
    return amplifierCost_loc;
  }

  /**
   * this method calculate the cost with
   * switches and wavelength and return
   * a tuple with wave length cost in
   * first position and  switch cost in
   * second position
   */
  private Pair<Double, Double> wavelengthAndSwitchAndCost() {

    var nodeEquipmentTypes = variables.subList(variables.size() - (numNodes + 1), variables.size() - 1);
    int wNumber = variables.get(variables.size() - 1);
    var wavelengthCost = 0.0;
    var switchCost = 0.0;
    int[] nodeDegrees = new int[numNodes];
    int[] wInEachNode = new int[numNodes];
    double[] sumOLTCost = new double[numNodes];

    Arrays.fill(nodeDegrees, 0);
    Arrays.fill(wInEachNode, 0);
    Arrays.fill(sumOLTCost, 0);

    //being m the line index to run through matrix line
    for (int i = 0; i < numNodes; i++) {
      for (int j = i + 1; j < numNodes; j++) {
        var index = Equipments.getLinkPosition(i, j, numNodes, setSize);
        for (int w = 0; w < setSize; w++) {
          if (connections.get(index + w) != 0) {
            nodeDegrees[i] += 1;
            nodeDegrees[j] += 1;
            var band = Bands.getBand(connections.get(index + w));
            var cost = Equipments.getOltCostInBandFunction(band, wNumber);
            var totalW = Bands.getTotalChannels(band, (double) wNumber);
            sumOLTCost[i] += cost;
            sumOLTCost[j] += cost;
            wInEachNode[i] += totalW;
            wInEachNode[j] += totalW;
          }
        }
        // it is inside loop for, so, 'i' is dynamic and known
        // the two next equations comes from cost model but was adopted.
        wavelengthCost += 2 * sumOLTCost[i];
        switchCost += ((0.05225 * wInEachNode[i]) + (6.24 * nodeDegrees[i]) + 2.5) * Equipments.getWssSolutionCost(
            nodeEquipmentTypes.get(i));
      }
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
