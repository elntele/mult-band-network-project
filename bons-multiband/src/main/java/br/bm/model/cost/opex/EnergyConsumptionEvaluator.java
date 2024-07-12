package br.bm.model.cost.opex;

import java.util.Arrays;
import java.util.List;

import br.bm.core.INetwork;
import br.bm.core.NetworkProfile;
import br.bm.model.INetworkEvaluator;
import br.bm.model.NumericalResult;
import br.bm.model.TNetworkIndicator;
import br.cns24.TMetric;

public class EnergyConsumptionEvaluator implements INetworkEvaluator<INetwork, NumericalResult> {
	private boolean evaluateIP;
	
	private boolean evaluateEthernet;
	
	private boolean evaluateOTN;
	
	private boolean evaluateWDM;
	
	public EnergyConsumptionEvaluator() {
		evaluateIP = true;
		evaluateEthernet = true;
		evaluateOTN = true;
		evaluateWDM = true;
	}

	@Override
	public NumericalResult evaluate(INetwork network) {
		NetworkProfile profile = (NetworkProfile) network;
		int numberOfNodes = profile.getNodes().size();
		int nc = 2; // facilities for cooling and overhead
		int npr = 2; // facilities for protection
		int nd = 1; // constant related to the number of ip/mpls demands
		int dc = 1; // average demand capacity
		List<Integer> decisionVariablesList = Arrays.asList(profile.getRawData());
		int constantFactor = nc * npr * nd * dc;

		double H = profile.getCn().getMetricValues().get(TMetric.AVERAGE_PATH_LENGTH); // average
																						// layer
																						// hop
																						// count
		double pIP = 0;
		double pEthernet = 0;
		double pOTN = 0;
		double pWDM = 0;
		
		int vectorSize = decisionVariablesList.size();
		double amplifierConsumption = 0;
		double trConsumption = 0;
		double switchConsumption = 0;
		double totalConsumption = 0;

		int numberOfWavelenghts = decisionVariablesList.get(vectorSize - 1);

		for (int m = 0; m < numberOfNodes; m++) {
			int nodeDegree = 0;

			// os proximos dois for sao para calcular o grau do no de indice m
			for (int j = m + 1; j < numberOfNodes; j++) { // varre para uma
				// linha fixa da matriz
				int k = indiceVetor(j, m, numberOfNodes); // k1
				if (decisionVariablesList.get(k) != 0)
					nodeDegree++;
			}
			for (int i = 0; i < m; i++) { // varre para uma coluna fixa da
				// matriz
				int k = indiceVetor(m, i, numberOfNodes); // k2
				if (decisionVariablesList.get(k) != 0)
					nodeDegree++;
			}
			if (nodeDegree > 0) {
				trConsumption += 5 * numberOfWavelenghts;
				switchConsumption += (150 + numberOfWavelenghts * ((85 + 50) * nodeDegree) )/ (40 * 10 * nodeDegree);
			}
		}

		int links = 0;
		double totalNetworkLength = 0;
		for (int i = 0; i < numberOfNodes; i++) {
			for (int j = 0; j < numberOfNodes; j++) {
				int k = indiceVetor(i, j, numberOfNodes);
				if (decisionVariablesList.get(k) != 0) {
					totalNetworkLength += profile.getCompleteDistances()[i][j];
					links++;
				}
			}
		}
		double averagePathLengthLightpaths = H * (totalNetworkLength/links);
		if (averagePathLengthLightpaths > 0) {
			amplifierConsumption = (0.27 * averagePathLengthLightpaths) / 80;
		}

		// state the total network cost by adding the separated costs
		pWDM = amplifierConsumption + switchConsumption + trConsumption; //nao e considerado consumo por regeradores
		pWDM *= H ;

		pIP = 10 * 2 * (1.0/npr + H);
		
		pEthernet = 3.8 * 2 * (1.0/npr + H);
		
		pOTN = 3.4 * 2 * (1.0/npr + H);
		
//		System.out.printf("%.2f %.2f %.2f %.2f", pWDM, pIP, pEthernet, pOTN);

		if (evaluateIP) {
			totalConsumption += pIP < 0 ? 0 : pIP;
		}
		if (evaluateEthernet) {
			totalConsumption += pEthernet < 0 ? 0 : pEthernet;
		}
		if (evaluateOTN) {
			totalConsumption += pOTN < 0 ? 0 : pOTN;
		}
		if (evaluateWDM) {
			totalConsumption += pWDM < 0 ? 0 : pWDM;
		}
		totalConsumption *= constantFactor * profile.getNetworkLoad()/2.5;
		return new NumericalResult(TNetworkIndicator.ENERGY_CONSUMPTION, totalConsumption);
	}

	private int indiceVetor(int j, int i, int numberOfNodes) {
		return (j + (numberOfNodes - 1) * i - i * (i + 1) / 2);
	}

}
