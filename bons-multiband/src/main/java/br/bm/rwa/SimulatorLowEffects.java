package br.bm.rwa;

import static br.bm.core.Call.BIDIRECIONAL;
import static br.bm.core.SimonUtil.B0;
import static br.bm.core.SimonUtil.PLANCK;
import static br.bm.rwa.Dijkstra.dijkstra_fnb;
import static br.bm.rwa.Funcoes.INF;
import static br.bm.rwa.Funcoes.calculoPmd_fnb;
import static br.bm.rwa.Funcoes.calculoRd_fnb;
import static br.bm.rwa.Funcoes.somatorioPotSwitch;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import br.bm.core.Call;
import br.bm.core.CallList;
import br.bm.core.CallScheduler;
import br.bm.core.Fiber;
import br.bm.core.Link;
import br.bm.core.NetworkProfile;
import br.bm.rwa.OpticalNetworkSimulatorAbstract;

public class SimulatorLowEffects extends OpticalNetworkSimulatorAbstract {

	public static final double D_PMD = 0.05e-12 / sqrt(1000); // 0.04

	public static final double D_PMD_D_CF = 0;

	public static final double DELTA = 10;

	public static final double TIME_PULSE_BROADENING_PMD_P1 = (0.01) * (1 / TAXA_BITS);

	public static final double DELTA_PMD_P1 = (100) * (TAXA_BITS);

	@Override
	public void simulate(NetworkProfile network, CallScheduler scheduler) {
		int x = 0, y = 0;
		int nLambdaMax = 0, usarLambda = 0;
		int numCallsBlockedLackOfWaveLenght = 0;
		int numCallsBlockedUnacceptableBer = 0;
		int numCallsBlockedDispersion = 0;
		Vector<Link> rotaUplink = new Vector<Link>(), rotaDownlink = new Vector<Link>();
		Vector<Fiber> fibersUpLink = new Vector<Fiber>(), fibersDownLink = new Vector<Fiber>();
		double numOfCalls = network.getNumOfCalls();
		CallList listOfCalls = new CallList();
		Call tempCall = new Call();
		long qtdeTempoZerado = 0;

		// of minumum acceptatle snr from dB to linear
		nLambdaMax = 0;
		HashMap<Integer, List<Link>> cacheRotas = new HashMap<Integer, List<Link>>();
		// Procura qual a fibra que possui mais comprimentos de onda
		for (int k = 0; k < network.getNodes().size(); k++) {
			for (int i = 0; i < network.getNodes().size(); i++) {
				Vector<Link> rota = new Vector<Link>();
				dijkstra_fnb(network.getLinks(), k, i, rota, network.getNodes());
				cacheRotas.put(k * 1000 + i, rota);
				if (nLambdaMax < network.getLinks()[k][i].getFiber(0).getLambda())
					nLambdaMax = network.getLinks()[k][i].getFiber(0).getLambda();
			}
		}
		network.setnLambdaMax(nLambdaMax);

		for (int i = 1; i <= numOfCalls; i++) {
			if (scheduler.getCurrentTime() >= MAX_TIME) {
				listOfCalls.zerachamadas_mpu(scheduler.getCurrentTime());
				scheduler.resetTime_mpu();
				qtdeTempoZerado++;
			}
			scheduler.generateCallRequisition();
			x = scheduler.getNextSourceNode();
			y = scheduler.getNextDestinationNode();
			// remove ended calls
			listOfCalls.retirarChamada(scheduler.getCurrentTime(), network.getNodes());
			tempCall = new Call();
			tempCall.setup(x, y, scheduler.getCurrentTime() + scheduler.getDuration(), scheduler.getDuration(),
					BIDIRECIONAL);

			CallList tempListOfCalls = new CallList(); // creates a vector of
														// call to represent
														// current processing
														// call
			tempListOfCalls.addChamada(tempCall); // this is required due to
													// possible use of
													// regenerators

			int[] lambdaEncontrado_loc = { 0 };
			usarLambda = getStatusRota(network, x, y, rotaUplink, rotaDownlink, cacheRotas, lambdaEncontrado_loc);
			// ve se realmente existe uma rota
			if (usarLambda == BLOQ_WAVELENGTH) {
				numCallsBlockedLackOfWaveLenght++;
			} else if (usarLambda == BLOQ_BER) {
				numCallsBlockedUnacceptableBer++;
			} else if (usarLambda == BLOQ_DISPERSION) {
				numCallsBlockedDispersion++;
			} else { // the call has been accepted no regenerators were used
				tempCall.setWavelengthUp(usarLambda);
				tempCall.setWavelengthDown(usarLambda);

				fibersUpLink.clear();
				fibersDownLink.clear();

				for (int j = 0; j < rotaUplink.size(); j++) {
					// scanning all links in in found rote ja �
					// implementa��o p
					// multi-fibra
					fibersUpLink.add(rotaUplink.get(j).getFiber(rotaUplink.get(j).getAvailableFiber(usarLambda)));
					fibersDownLink.add(rotaDownlink.get(j).getFiber(rotaDownlink.get(j).getAvailableFiber(usarLambda)));
				}

				tempCall.alloc(fibersUpLink, fibersDownLink, network.getNodes());
				listOfCalls.addChamada(tempCall); // add actual tempCall to the
				// active list of Calls
			}
		}
		network.getBp().setBer(numCallsBlockedUnacceptableBer / numOfCalls);
		network.getBp().setLambda(numCallsBlockedLackOfWaveLenght / numOfCalls);
		network.getBp().setDispersion(numCallsBlockedDispersion / numOfCalls);
		network.getBp().setMeanDist(listOfCalls.getDistanciaMedia());
		network.getBp().setTotal(
				(numCallsBlockedLackOfWaveLenght + numCallsBlockedUnacceptableBer + numCallsBlockedDispersion)
						/ numOfCalls);

	}

	protected int getStatusRota(NetworkProfile network, int origem, int destino, Vector<Link> rotaUplink,
			Vector<Link> rotaDownlink, Map<Integer, List<Link>> cacheRotas, int[] lambdaEncontrado) {
		int lambdaEncontrado_loc = -1;
		rotaUplink.clear(); // limpa a vari�vel q calcula a rota direta
		rotaDownlink.clear(); // limpa a vari�vel q calcula a rota de volta
		boolean lambdaDisponivel_loc = true;
		rotaUplink.addAll(cacheRotas.get(origem * 1000 + destino));
		// encontra o comprimento de onda por First Fit para os casos de minhops
		// e menor distancia (dijkstra) reverte a rota encontrada
		for (int j = rotaUplink.size() - 1; j >= 0; j--) {
			int destination_loc = rotaUplink.get(j).getSource();
			int source_loc = rotaUplink.get(j).getDestination();
			rotaDownlink.add(network.getLinks()[source_loc][destination_loc]);
		}
		// procura para cada lambda
		for (int nLambda_loc = 0; (nLambda_loc < network.getnLambdaMax()); nLambda_loc++) {
			lambdaDisponivel_loc = true;
			for (int i = 0; i < rotaUplink.size(); i++) {
				// se lambda nao disponivel
				if ((rotaUplink.get(i).getFiber(0).isLambdaAvailable(nLambda_loc) == false)
						|| (rotaDownlink.get(i).getFiber(0).isLambdaAvailable(nLambda_loc) == false)) {
					lambdaDisponivel_loc = false;
					break;
				}
			}
			if (lambdaDisponivel_loc) {
				lambdaEncontrado_loc = nLambda_loc;
				lambdaEncontrado[0] = lambdaEncontrado_loc;
				break;
			}
		}

		if (lambdaEncontrado_loc == -1) // nao h� lambda disponivel
		{
			return BLOQ_WAVELENGTH;
		}

		// calcula o alargamento temporal devido � PMD da fibra de
		// transmiss�o
		double timePulseBroadeningPmd_loc = TIME_PULSE_BROADENING_PMD_P1 * calculoPmd_fnb(rotaUplink, TAXA_BITS, D_PMD);
		// calcula o alargamento temporal resultante devido � PMD
		double pulseBroadeningPmd_loc = sqrt(timePulseBroadeningPmd_loc * timePulseBroadeningPmd_loc);
		// calcula o delta t(%) resultante
		double totalDeltaPulseBroadening = abs(DELTA_PMD_P1 * pulseBroadeningPmd_loc
				+ calculoRd_fnb(rotaUplink, TAXA_BITS, LARGURA_LINHA, GAMA, lambdaEncontrado_loc));

		if (totalDeltaPulseBroadening > DELTA) {
			return BLOQ_DISPERSION;
		}
		// a qualidade do servi�o � insuficiente, retorna valor apropriado a
		// ser
		// tratado
		if (getSNR(network, rotaUplink, lambdaEncontrado_loc) < SNR_THRESHOLD
				|| getSNR(network, rotaDownlink, lambdaEncontrado_loc) < SNR_THRESHOLD) {
			return BLOQ_BER;
		}

		return lambdaEncontrado_loc;
	}

	public static double getSNR(NetworkProfile network, Vector<Link> path, int lambda) {
		int source, destino;
		double sIn, nIn, G1G2, LMux2Exp;
		double sInPart1, sInPart2, nInPart1, nInPart2, nInPart3, nInPartFwm;
		double somatorioPotencias = 0.0, epsilon_loc;
		double swAtenuation, muxDemuxGain, fiberGain;

		if (path.isEmpty()) {
			return INF;
		}
		source = path.get(0).getSource();
		epsilon_loc = network.getEpsilon();

		somatorioPotencias = somatorioPotSwitch(network.getLinks(), network.getNodes(), path, source, lambda);
		somatorioPotencias *= epsilon_loc;
		nIn = (network.getNodes().get(source).getLaserPower() / network.getNodes().get(source).getLaserSNR())
				+ somatorioPotencias;
		sIn = network.getNodes().get(source).getLaserPower();

		for (int j = 0; j < path.size(); j++) {
			nInPartFwm = 0.0;
			nInPart3 = 0.0;
			source = path.get(j).getSource();
			destino = path.get(j).getDestination();

			swAtenuation = network.getNodes().get(destino).getSwitchAtenuation();
			muxDemuxGain = path.get(j).getFiber(0).getMuxDemuxGain();
			fiberGain = path.get(j).getFiber(0).getGain();
			LMux2Exp = muxDemuxGain * muxDemuxGain * path.get(j).getFiber(0).getGain();

			G1G2 = path.get(j).getFiber(0).getG0Booster(path.get(j).getFiber(0).getSumPowerB())
					* path.get(j).getFiber(0).getG0PreAmp(path.get(j).getFiber(0).getSumPowerD());

			sInPart1 = (sIn * G1G2);
			sInPart2 = LMux2Exp * swAtenuation;

			sIn = sInPart1 * sInPart2;

			nInPart1 = G1G2 * LMux2Exp * swAtenuation;
			nInPart2 = nIn;
			nInPart3 = ((PLANCK * path.get(j).getFiber(0).getFrequency(lambda) * B0) / (2 * muxDemuxGain))
					* (path.get(j).getFiber(0).getBoosterF(path.get(j).getFiber(0).getSumPowerB()) + (path.get(j)
							.getFiber(0).getPreF(path.get(j).getFiber(0).getSumPowerD()) / (path.get(j).getFiber(0)
							.getG0Booster(path.get(j).getFiber(0).getSumPowerB()) * fiberGain)));

			somatorioPotencias = somatorioPotSwitch(network.getLinks(), network.getNodes(), path, destino, lambda);

			nIn = nInPart1 * (nInPart2 + nInPartFwm + nInPart3);
		}

		return (sIn / nIn);
	}

	/* (non-Javadoc)
	 * @see br.upe.jol.problems.simon.rwa.OpticalNetworkSimulatorAbstract#simulate(br.upe.jol.problems.simon.entity.NetworkProfile, br.upe.jol.problems.simon.entity.CallScheduler, int)
	 */
	@Override
	public void simulate(NetworkProfile network, CallScheduler callScheduler, int minCalls) {
		// TODO Auto-generated method stub
		
	}

}
