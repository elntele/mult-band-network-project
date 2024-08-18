package br.bm.model.cost.capex;

import java.util.Arrays;
import java.util.List;

import br.bm.core.INetwork;
import br.bm.core.MultiBandNetWorkProfile;
import br.bm.core.NetworkProfile;
import br.bm.model.INetworkEvaluator;
import br.bm.model.NumericalResult;
import br.bm.model.TNetworkIndicator;

public class CapexEvaluator implements INetworkEvaluator<INetwork, NumericalResult> {

	public static double[][] AMPLIFIERS_COSTS_AND_LABELS = new double[][] {
			{ 0, 0.75, 1.5, 2.25, 0.5, 1, 1.5, 0.25, 0.5, 0.75 }, // cost
			// index
			{ 0, 13, 16, 19, 13, 16, 19, 13, 16, 19 }, // amplifier saturation
			// power
			{ 0, 5, 5, 5, 7, 7, 7, 9, 9, 9 } // amplifier noise figure
	};

	public static double[][] SWITCHES_COSTS_AND_LABELS = new double[][] { { 0.25, 0.5, 0.75, 1, 1.5, 2.0 }, // costs
			{ 27, 30, 33, 35, 38, 40 } }; // isolation factor in dB

	@Override
	public NumericalResult evaluate(INetwork network) {
		NetworkProfile net = (NetworkProfile) network;

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

		int numberOfWavelenghts_loc = decisionVariablesList.get(vectorSize_loc - 1);
		for (int i = 0; i < vectorSize_loc - 2; i++) {
			amplifierCost_loc += 3.84 * AMPLIFIERS_COSTS_AND_LABELS[0][decisionVariablesList.get(i)];
		}

		double betaOXC_loc = SWITCHES_COSTS_AND_LABELS[0][decisionVariablesList.get(vectorSize_loc - 2)];
		for (int m = 0; m < numNodes; m++) {
			int nodeDegree_loc = 0;
			// os proximos dois for sao para calcular o grau do no de indice m
			for (int j = m + 1; j < numNodes; j++) { // varre para uma
				// linha fixa da matriz
				int k = indiceVetor(j, m, numNodes); // k1
				if (decisionVariablesList.get(k) != 0)
					nodeDegree_loc++;
			}
			for (int i = 0; i < m; i++) { // varre para uma coluna fixa da
				// matriz
				int k = indiceVetor(m, i, numNodes); // k2
				if (decisionVariablesList.get(k) != 0)
					nodeDegree_loc++;
			}

			wavelengthCost_loc += 2 * numberOfWavelenghts_loc * nodeDegree_loc;
			switchCost_loc += ((0.05225 * numberOfWavelenghts_loc + 6.24) * nodeDegree_loc + 2.5) * betaOXC_loc;
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

}
