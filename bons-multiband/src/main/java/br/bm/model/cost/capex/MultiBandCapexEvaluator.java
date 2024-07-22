package br.bm.model.cost.capex;

import java.util.Arrays;
import java.util.List;

import br.bm.core.INetwork;
import br.bm.core.MultiBandNetWorkProfile;
import br.bm.core.SwitchesAndAmplifiersEquipments;
import br.bm.model.INetworkEvaluator;
import br.bm.model.NumericalResult;
import br.bm.model.TNetworkIndicator;
import br.cns24.model.Bands;

public class MultiBandCapexEvaluator implements INetworkEvaluator<INetwork, NumericalResult> {
  @Override
  public NumericalResult evaluate(INetwork network) {
    MultiBandNetWorkProfile net = (MultiBandNetWorkProfile) network;

    List<Integer> decisionVariablesList = Arrays.asList(net.getRawData());


    int vectorSize_loc = decisionVariablesList.size();
    double amplifierCost_loc = 0;
    double switchCost_loc = 0;

    double wavelengthCost_loc = 0;
    double dcfCost_loc = 0;
    double ssmfCost_loc = 0;
    double deploymentCost_loc = 0;
    double totalCost_loc = 0;
    int numNodes = net.getNodes().size();

    var nodeEquipments = decisionVariablesList.subList(vectorSize_loc - (numNodes + 1), vectorSize_loc - 1);

    var hashAmplifiers = SwitchesAndAmplifiersEquipments.getAmplifiersAsAnHashOfIndexes(nodeEquipments);

    int number_w_of_wave_lenghts = decisionVariablesList.get(vectorSize_loc - 1);

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
          switch (Bands.getBand(j)) {
            case Bands.CBAND ->
                amplifierCost_loc += 3.84 * SwitchesAndAmplifiersEquipments.getAmplifiersBandC()[0][1];
            case Bands.CLBAND ->
                amplifierCost_loc += 3.84 * SwitchesAndAmplifiersEquipments.getAmplifiersBandCL()[0][1];
            case Bands.CLSBAND ->
                amplifierCost_loc += 3.84 * SwitchesAndAmplifiersEquipments.getAmplifiersBandCLS()[0][1];
          }
        }
      }
      offset++;
      externalObserverCount++;
    }

    List<List<Double>> betaOXC_loc = SwitchesAndAmplifiersEquipments.getThisSwitchesList(nodeEquipments);
    externalObserverCount = 0;
    offset = 1;
    //being m the line index to run through matrix line
    for (int m = 0; m < numNodes; m++) {
      int nodeDegree_c = 0;
      int nodeDegree_cl = 0;
      int nodeDegree_cls = 0;
      int nodeDegree_loc = 0;

      var nextLimit = externalObserverCount + (numNodes - offset) * 3;
      // the next two 'loop for' is to calculate the node degree on index nodePosition
      //being  j the column index to run through matrix column
      for (int j = externalObserverCount; j < nextLimit; j++) {
        // internal of this loop we are running the fixed line of matrix
        if (decisionVariablesList.get(j) != 0) {
          //here the node degree is calculated in general way
          nodeDegree_loc++;
          //here the node degree is calculated accord the type of band
          switch (SwitchesAndAmplifiersEquipments.getSwitchType(m)) {
            case Bands.CBAND:
              nodeDegree_c++;
            case Bands.CLBAND:
              nodeDegree_cl++;
            case Bands.CSBAND, Bands.CLSBAND:
              nodeDegree_cls++;
          }
        }
      }
      offset++;
      //here the cost of w is differentiated accord the type of band to calculate the w cost
      wavelengthCost_loc += 2 * number_w_of_wave_lenghts *
          (nodeDegree_c * SwitchesAndAmplifiersEquipments.COST_MODULE_W_FOR_C_BAND +
              nodeDegree_cl * SwitchesAndAmplifiersEquipments.COST_MODULE_W_FOR_CL_BAND +
              nodeDegree_cls * SwitchesAndAmplifiersEquipments.COST_MODULE_W_FOR_CLS_BAND);

      //here the node degree in general way is used to calculate the switch cost
      switchCost_loc += ((0.05225 * number_w_of_wave_lenghts + 6.24) * nodeDegree_loc + 2.5) * betaOXC_loc.get(0).get(
          m);
      // as antes da duas equa��es anteriores vem do modelo de custo
    }

    double totalNetworkLength = 0;
    for (int i = 0; i < net.getNodes().size(); i++) {
      for (int j = 0; j < net.getNodes().size(); j++) {
        int k = indiceVetor(i, j, numNodes); // k2
        if (decisionVariablesList.get(k) != 0) {
          totalNetworkLength += net.getCompleteDistances()[i][j];
        }
      }
    }

    dcfCost_loc = 0.0036 * totalNetworkLength;
    ssmfCost_loc = 0.013 * totalNetworkLength;
    deploymentCost_loc = 0.2 * totalNetworkLength;

    // state the total network cost by adding the separated costs
    totalCost_loc = amplifierCost_loc + switchCost_loc + wavelengthCost_loc + dcfCost_loc + ssmfCost_loc
        + deploymentCost_loc;

    return new NumericalResult(TNetworkIndicator.CAPITAL_EXPENDITURE, totalCost_loc);
  }


  private int indiceVetor(int j, int i, int numberOfNodes) {
    return (j + (numberOfNodes - 1) * i - i * (i + 1) / 2);
  }

  private double amplifiersCost(){
    return 0.0;
  }



}
