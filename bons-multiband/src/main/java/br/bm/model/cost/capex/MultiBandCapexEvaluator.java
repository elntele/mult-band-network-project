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
    double wavelengthCost_c = 0;
    double wavelengthCost_cl = 0;
    double wavelengthCost_cs = 0;
    double wavelengthCost_cls = 0;
    double wavelengthCost_loc = 0;
    double dcfCost_loc = 0;
    double ssmfCost_loc = 0;
    double deploymentCost_loc = 0;
    double totalCost_loc = 0;
    int numNodes = net.getNodes().size();

    var nodeEquipments = decisionVariablesList.subList(vectorSize_loc - (numNodes + 1), numNodes);

    var amplifiers = SwitchesAndAmplifiersEquipments.getThisAmpliflierList(nodeEquipments);

    int numberOfWavelenghts_loc = decisionVariablesList.get(vectorSize_loc - 1);
    for (int i = 0; i < vectorSize_loc - (numNodes + 1); i++) {
      if (i != 0) {
        amplifierCost_loc += 3.84 * amplifiers.get(0).get(decisionVariablesList.get(getAmplifierIndex(i)));
      } else {
        amplifierCost_loc += 3.84 * amplifiers.get(0).get(decisionVariablesList.get(0));
      }

    }


    List<List<Double>> betaOXC_loc = SwitchesAndAmplifiersEquipments.getThisSwitchesList(nodeEquipments);
    var externalObserverCount = 0;
    var offset = 1;
    //being m the line index to run through matrix line
    for (int m = 0; m < numNodes; m++) {
      int nodeDegree_c = 0;
      int nodeDegree_cl = 0;
      int nodeDegree_cs = 0;
      int nodeDegree_cls = 0;

      var nextLimit = externalObserverCount + (numNodes - offset) * 3;
      // the next two 'loop for' is to calculate the node degree on index nodePosition
      //being  j the column index to run through matrix column
      for (int j = externalObserverCount; j < nextLimit; j++) { // varre para uma
        // internal of this loop we are running the fixed lif of matrix
        if (decisionVariablesList.get(j)!=0){
          switch (SwitchesAndAmplifiersEquipments.getSwitchType(m)){
            case Bands.NOBAND: continue;
            case Bands.CBAND: nodeDegree_c++;;
            case Bands.CLBAND: nodeDegree_cl++;
            case Bands.CSBAND, Bands.CLSBAND: nodeDegree_cls++;


          }
        }
      }
      for (int i = 0; i < m; i++) { // varre para uma coluna fixa da
        // matriz
        int k = indiceVetor(m, i, numNodes); // k2
        if (decisionVariablesList.get(k) != 0)
          nodeDegree_cls++;
      }
      //jorge aqui é uma variavel global que acumula, então tem que
      // fazer um switch pra ela só ser acionada quando o nó for
      // do tipo dela
      wavelengthCost_c += 2 * numberOfWavelenghts_loc * nodeDegree_c;
      wavelengthCost_cl += 2 * numberOfWavelenghts_loc * nodeDegree_cl;
      wavelengthCost_cs += 2 * numberOfWavelenghts_loc * nodeDegree_cs;
      wavelengthCost_cls += 2 * numberOfWavelenghts_loc * nodeDegree_cls;

      switchCost_loc += ((0.05225 * numberOfWavelenghts_loc + 6.24) * nodeDegree_cls + 2.5) * betaOXC_loc.get(0).get(
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

  private Integer getAmplifierIndex(int index) {

    switch (index) {
      case 1, 2, 3, 4: {
        return 1;
      }
      case 5, 6, 7, 8: {
        return 5;
      }
      case 9, 10, 11, 12: {
        return 9;
      }
      default:
        throw new RuntimeException("index out of bound of amplifiers" + index);
    }

  }

  private int indiceVetor(int j, int i, int numberOfNodes) {
    return (j + (numberOfNodes - 1) * i - i * (i + 1) / 2);
  }

}
