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
        /**
         * isso tem que modificar, tem que observar se a conexão é
         * diferente de zero e parar de observar o link e começar
         * a observar os dois nós. Ai conta um amplificador de acordo
         * com o tipo de wss no nó_ij e outro amplificador de acordo
         * com o tipo de wss no nó_ji.
         * Observe que esse vezes dois ai de cada opção do switch case
         * quem colocou fui eu, e ainda assim ta errado, porque ta
         * considerando o mesmo amplificador nas duas pontas do
         * enlace tx e rx, mas pode ter um tipo de amplificador
         * em uma ponta e outro tipo na outra ponta já que os wss
         * podem ser diferentes, exemplo um nó cl se comunicando com
         * um nó c porque o enlace é c.
         */
        if (connections.get(j) != 0) {
          switch (Bands.getBand(connections.get(j))) {
            case Bands.CBAND -> amplifierCost_loc += 2 * (3.84 * Equipments.getAmplifiersBandC()[0][1]);
            case Bands.CLBAND, Bands.LBAND -> amplifierCost_loc += 2 * (3.84 * Equipments.getAmplifiersBandCL()[0][1]);
            case Bands.CSBAND, Bands.LSBAND, Bands.SBAND, Bands.CLSBAND ->
                amplifierCost_loc += 2 * (3.84 * Equipments.getAmplifiersBandCLS()[0][1]);
            default -> throw new RuntimeException(
                String.format("invalid band %1$s,  connection position %2$s, connections %3$s", Bands.getBand(
                    connections.get(j)), j, connections));
          }

        }
        externalObserverCount = j;
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
   */
  private Pair<Double, Double> wavelengthAndSwitchAndCost() {

    var nodeEquipmentTypes = variables.subList(variables.size() - (numNodes + 1), variables.size() - 1);
    int wNumber = variables.get(variables.size() - 1);
    var wavelengthCost = 0.0;
    var switchCost = 0.0;
    int[] nodeDegrees = new int[numNodes];
    Arrays.fill(nodeDegrees, 0);
    var externalCount = 0;
    var offset = 1;
    //being m the line index to run through matrix line
    for (int i = 0; i < numNodes; i++) {
      var indexJ = i + 1;
      var stepInConnections = 0;
      var nextLimit = externalCount + (numNodes - offset) * 3;
      // calculate the node degree on index nodePosition
      //j  run through matrix column
      for (int j = externalCount; j < nextLimit; j++) {
        stepInConnections++;
        if (connections.get(j) != 0) {
          nodeDegrees[i] += 1;
          nodeDegrees[indexJ] += 1;
        }
        if (stepInConnections % 3 == 0) {
          indexJ++;
        }
        externalCount = j;
      }
      externalCount++;
      offset++;
      wavelengthCost += 2 * wNumber * nodeDegrees[i] * Equipments.getOltCost(i);
      switchCost += ((0.05225 * wNumber) + (6.24 * nodeDegrees[i]) + 2.5) * Equipments.getSwitchCost(
          nodeEquipmentTypes.get(i));
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
