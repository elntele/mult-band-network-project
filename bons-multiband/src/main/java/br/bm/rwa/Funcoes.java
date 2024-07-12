package br.bm.rwa;

import static br.bm.core.Fiber.ESPACAMENTO_FREQUENCIA;
import static br.bm.core.Fiber.FREQUENCIA_FINAL;
import static br.bm.core.SimonUtil.B0;
import static br.bm.core.SimonUtil.C;
import static br.bm.core.SimonUtil.D0;
import static br.bm.core.SimonUtil.LAMBDA0;
import static br.bm.core.SimonUtil.PLANCK;
import static br.bm.core.SimonUtil.S0;
import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.util.Vector;

import br.bm.core.Link;
import br.bm.core.NetworkBP;
import br.bm.core.NetworkProfile;
import br.bm.core.Node;

public class Funcoes {
	public static final double lambdaZero = 1528.77e-9;/* 1538.19e-9; *//*
																		 * 1544.53e-9;
																		 */// 1550.0e-9;
	public static final double INF = 1e50;
	public static final double SSMF = S0; // em (s/m^3)
	public static final double DSMF = ((1.0e-6) * D0 + S0 * (lambdaZero - LAMBDA0));
	public static final double LAMBDA_REF_DCF = 1550e-9;
	public static final double SDCF = -1.87e3; // em (s/m^3)
	public static final double DDCF_REF = -110.0e-6; // em (s/m^2)

	/******************************************************************
	 ***** FUNCTION....: calculoDSMF_fnb DESCRIPTION.: Calculates and returns DSMF
	 * PARAMETERS.:
	 *******************************************************************/
	public static double DSMF_fnb() {
		return ((1.0e-6) * D0 + S0 * (lambdaZero - LAMBDA0));
	}

	/******************************************************************
	 ***** FUNCTION....: calculoDDCF_fnb DESCRIPTION.: Calculates and returns DDCF
	 * PARAMETERS.:
	 *******************************************************************/
	public static double DDCF_fnb() {
		return (DDCF_REF + SDCF * (lambdaZero - LAMBDA_REF_DCF));
	}

	/******************************************************************
	 ***** FUNCTION....: calculoPmd_fnb DESCRIPTION.: Calculates and returns PMD
	 * PARAMETERS.: vector with path found, bits rate, PMD coefficient
	 *******************************************************************/
	public static double calculoPmd_fnb(Vector<Link> path_par, double taxaBits_par, double Dpmd_par, boolean pmd_par) {
		double somatorioPmd_loc = 0.0;
		double quadDpmd_loc = pow(Dpmd_par, 2);// quadrado de Dpmd

		if (pmd_par == true) {
			// percorre o vetor de
			// links
			for (int i = 0; i < path_par.size(); i++) {
				// multiplica por 1000, pois eh em metros
				somatorioPmd_loc += (path_par.get(i).getLength() * (1000.0) * quadDpmd_loc);
			}
		}

		return (sqrt(somatorioPmd_loc) * taxaBits_par * 100);
	}

	public static double calculoPmd_fnb(Vector<Link> path_par, double taxaBits_par, double Dpmd_par) {
		double somatorioPmd_loc = 0.0;
		double quadDpmd_loc = pow(Dpmd_par, 2);// quadrado de Dpmd

		for (int i = 0; i < path_par.size(); i++) {
			somatorioPmd_loc += (path_par.get(i).getLength() * (1000.0) * quadDpmd_loc);
		}

		return (sqrt(somatorioPmd_loc) * taxaBits_par * 100);
	}

	/******************************************************************
	 ***** FUNCTION....: calculoPmd_Dcf_fnb DESCRIPTION.: Calculates and returns PMD
	 * of DCF fiber PARAMETERS.: vector with path found, bits rate, PMD
	 * coefficient
	 *******************************************************************/
	public static double calculoPmd_Dcf_fnb(Vector<Link> path_par, double taxaBits_par, double Dpmd_Dcf_par,
			boolean pmdDcf_par, double gama_par) {
		double somatorioPmd_Dcf_loc = 0.0;
		double quadDpmd_Dcf_loc = pow(Dpmd_Dcf_par, 2);// quadrado de Dpmd

		if (pmdDcf_par == true) {
			for (int i = 0; i < path_par.size(); i++) {// percorre o vetor de
				// links
				// multiplica por 1000, pois � em metros
				somatorioPmd_Dcf_loc += (quadDpmd_Dcf_loc * abs((gama_par * DSMF_fnb() * path_par.get(i).getLength() * (1000.0))
						/ DDCF_fnb()));
			}
		}

		return (sqrt(somatorioPmd_Dcf_loc) * taxaBits_par * 100);

	}

	/******************************************************************
	 ***** FUNCTION....: calculoRd_fnb DESCRIPTION.: Calculates and returns RD
	 * PARAMETERS.:
	 *******************************************************************/
	public static double calculoRd_fnb(Vector<Link> path_par, double taxaBits_par, double larguraDeLinha_par,
			double gama_par, int lambdaEncontrado_par, boolean rd_par) {

		double somatorioRd_loc = 0.0;
		double deltaLambda_loc = ((C / (FREQUENCIA_FINAL - (lambdaEncontrado_par) * ESPACAMENTO_FREQUENCIA)) - lambdaZero); // em
		// m
		// double dcfLenght = fabs((dDcf*path_par[i].getLength())/dDcf)
		if (rd_par == true) {
			for (int i = 0; i < path_par.size(); i++) {// percorre o vetor de
				// links
				somatorioRd_loc += (DSMF_fnb() + deltaLambda_loc * SSMF) * (path_par.get(i).getLength() * (1000.0))
						+ (DDCF_fnb() + deltaLambda_loc * SDCF)
						* abs((gama_par * DSMF_fnb() * path_par.get(i).getLength() * (1000.0)) / DDCF_fnb()); // em
				// (s/m)

			}
		}

		return ((somatorioRd_loc) * (taxaBits_par) * (100.0) * (larguraDeLinha_par));
	}

	public static double calculoRd_fnb(Vector<Link> path_par, double taxaBits_par, double larguraDeLinha_par,
			double gama_par, int lambdaEncontrado_par) {

		double somatorioRd_loc = 0.0;
		// em m
		double deltaLambda_loc = ((C / (FREQUENCIA_FINAL - (lambdaEncontrado_par) * ESPACAMENTO_FREQUENCIA)) - lambdaZero);
		// percorre o vetor de links
		for (int i = 0; i < path_par.size(); i++) {
			// em (s/m)
			somatorioRd_loc += (DSMF_fnb() + deltaLambda_loc * SSMF) * (path_par.get(i).getLength() * (1000.0))
					+ (DDCF_fnb() + deltaLambda_loc * SDCF)
					* abs((gama_par * DSMF_fnb() * path_par.get(i).getLength() * (1000.0)) / DDCF_fnb());

		}

		return ((somatorioRd_loc) * (taxaBits_par) * (100.0) * (larguraDeLinha_par));
	}

	/******************************************************************
	 ***** FUNCTION....: estaCaminho_fnb DESCRIPTION.: Function to check if a link
	 * (pair of nodes) is in path PARAMETERS.: vector with the path found, a
	 * source and a destination node (where destination node is the one you want
	 * to survey the cross-talk effect)
	 *******************************************************************/
	public static boolean estaCaminho(Vector<Link> path_par, int origem, int destino) {
		for (Link link : path_par)
			if (link.getFiber(0).getSourceNode() == origem && link.getFiber(0).getDestinationNode() == destino)
				return true;
		return false;
	}

	/******************************************************************
	 ***** FUNCTION....: somatorioPotSwitch_fnb DESCRIPTION.: Returns the cross-talk
	 * effect value in specified node PARAMETERS.: Matrix of Links, vector with
	 * nodes, vector with path found by routing algoritm, node you want to
	 * observe cross-talk effect, lambda used in call
	 *******************************************************************/
	public static double somatorioPotSwitch(Link[][] mLinks_par, Vector<Node> vectorOfNodes_par, Vector<Link> path_par,
			int no, int lambda_par) {
		double potenciaTemp, somatorioPotencias = 0.0;
		double potenciaTempA;

		for (int i = 0; i < vectorOfNodes_par.size(); i++)
			if (mLinks_par[i][no].getLength() != INF)
				if (!estaCaminho(path_par, i, no)) {
					potenciaTemp = mLinks_par[i][no].getFiber(0).getPowerF(lambda_par);
					potenciaTempA = mLinks_par[i][no].getFiber(0).getPowerA(lambda_par);

					if ((potenciaTemp > 0.0) && (potenciaTempA != 0.0))
						somatorioPotencias += potenciaTemp;
					potenciaTemp = 0.0;
				}

		return (somatorioPotencias);
	}

	public static double getSNR(NetworkProfile network, Vector<Link> path, int lambda) {
		int source, destino;
		double sIn, nIn, G1G2, LMux2Exp;
		double sInPart1, sInPart2, nInPart1, nInPart2, nInPart3, nInPartFwm, nInPartCrTalk;
		double potFwm;
		double somatorioPotencias = 0.0, epsilon_loc;
		double swAtenuation, muxDemuxGain, fiberGain;

		if (path.isEmpty()) {
			return INF;
		}
		source = path.get(0).getSource();
		epsilon_loc = network.getEpsilon();

		if (network.isCrTalk()) {
			somatorioPotencias = somatorioPotSwitch(network.getLinks(), network.getNodes(), path, source, lambda);
			somatorioPotencias *= epsilon_loc;
		}
		nIn = (network.getNodes().get(source).getLaserPower() / network.getNodes().get(source).getLaserSNR())
				+ somatorioPotencias;
		sIn = network.getNodes().get(source).getLaserPower();

		for (int j = 0; j < path.size(); j++) {
			nInPartFwm = 0.0;
			nInPartCrTalk = 0.0;
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
			if (network.isAmplifiers() == true) {
				nInPart3 = ((PLANCK * path.get(j).getFiber(0).getFrequency(lambda) * B0) / (2 * muxDemuxGain))
						* (path.get(j).getFiber(0).getBoosterF(path.get(j).getFiber(0).getSumPowerB()) + (path.get(j)
								.getFiber(0).getPreF(path.get(j).getFiber(0).getSumPowerD()) / (path.get(j).getFiber(0)
								.getG0Booster(path.get(j).getFiber(0).getSumPowerB()) * fiberGain)));
			}

			if (network.isFwm() == true) {
				potFwm = path.get(j).getFiber(0).getSumPowerFWM(lambda, network.getNodes());
				nInPartFwm = potFwm
						/ (muxDemuxGain * fiberGain * path.get(j).getFiber(0).getG0Booster(
								path.get(j).getFiber(0).getSumPowerB()));
			}

			if (network.isCrTalk() == true) {
				somatorioPotencias = somatorioPotSwitch(network.getLinks(), network.getNodes(), path, destino, lambda);
				nInPartCrTalk = (somatorioPotencias * epsilon_loc)
						/ (muxDemuxGain * muxDemuxGain * swAtenuation * fiberGain * G1G2);
			}

			nIn = nInPart1 * (nInPart2 + nInPartFwm + nInPart3 + nInPartCrTalk);
		}

		return (sIn / nIn);
	}

	/**
	 * Calculates and returns noise factor through a recursive way, given lambda
	 * and path
	 * 
	 * @param network
	 * @param path
	 * @return
	 */
	public static double snr_fnb(NetworkProfile network, Vector<Link> path, int lambda) {
		int source, destino;
		double sIn, nIn, G1G2, LMux2Exp;
		double sInPart1, sInPart2, nInPart1, nInPart2, nInPart3, nInPartFwm, nInPartCrTalk;
		double potFwm;
		double somatorioPotencias = 0.0;
		double swAtenuation, muxDemuxGain, fiberGain;

		if (path.isEmpty()) {
			return INF;
		}
		source = path.get(0).getSource();

		somatorioPotencias = somatorioPotSwitch(network.getLinks(), network.getNodes(), path, source, lambda);
		somatorioPotencias *= network.getEpsilon();
		nIn = (network.getNodes().get(source).getLaserPower() / network.getNodes().get(source).getLaserSNR())
				+ somatorioPotencias;
		sIn = network.getNodes().get(source).getLaserPower();

		for (int j = 0; j < path.size(); j++) {
			nInPartFwm = 0.0;
			nInPartCrTalk = 0.0;
			nInPart3 = 0.0;
			source = path.get(j).getSource();
			destino = path.get(j).getDestination();

			swAtenuation = network.getNodes().get(destino).getSwitchAtenuation();
			muxDemuxGain = path.get(j).getFiber(0).getMuxDemuxGain();
			fiberGain = path.get(j).getFiber(0).getGain();
			LMux2Exp = muxDemuxGain * muxDemuxGain * path.get(j).getFiber(0).getGain();

			G1G2 = path.get(j).getFiber(0).getG0Booster(path.get(j).getFiber(0).getSumPowerB())
					* path.get(j).getFiber(0).getG0PreAmp(path.get(j).getFiber(0).getSumPowerD());

			// CALCULO PARA SIN
			sInPart1 = (sIn * G1G2);
			sInPart2 = LMux2Exp * swAtenuation;

			sIn = sInPart1 * sInPart2;

			// CACULO PARA NIN
			nInPart1 = G1G2 * LMux2Exp * swAtenuation;
			nInPart2 = nIn;
			nInPart3 = ((PLANCK * path.get(j).getFiber(0).getFrequency(lambda) * B0) / (2 * muxDemuxGain))
					* (path.get(j).getFiber(0).getBoosterF(path.get(j).getFiber(0).getSumPowerB()) + (path.get(j)
							.getFiber(0).getPreF(path.get(j).getFiber(0).getSumPowerD()) / (path.get(j).getFiber(0)
							.getG0Booster(path.get(j).getFiber(0).getSumPowerB()) * fiberGain)));

			// CALCULO DA PERDA GERADA POR FWM PARA O NIN
			potFwm = path.get(j).getFiber(0).getSumPowerFWM(lambda, network.getNodes());
			nInPartFwm = potFwm
					/ (muxDemuxGain * fiberGain * path.get(j).getFiber(0).getG0Booster(
							path.get(j).getFiber(0).getSumPowerB()));

			somatorioPotencias = somatorioPotSwitch(network.getLinks(), network.getNodes(), path, destino, lambda);
			nInPartCrTalk = (somatorioPotencias * network.getEpsilon())
					/ (muxDemuxGain * muxDemuxGain * swAtenuation * fiberGain * G1G2);

			nIn = nInPart1 * (nInPart2 + nInPartFwm + nInPart3 + nInPartCrTalk);
		}

		return (sIn / nIn);
	}

	/******************************************************************
	*****  FUNCTION....: checkPhysImpairm_fnb()
	*****  DESCRIPTION.:
	*****
	*****  PARAMETERS.:
	*****
	*******************************************************************/
	public static boolean checkPhysImpairm_fnb(NetworkProfile network,
			Vector<Link> path, int lambda,
			double sInlinkLoc, double nInlinkLoc, double sOutlinkLoc,
			double nOutlinkLoc, double[] outputPulseBroadening,
			double inputPulseBroadening, NetworkBP bp){

		
		bp.setBer(0);
		bp.setDispersion(0);
		double tempPulseBroadening, alargamentoDesteTrecho, alargamentoDesteTrechoDcf;
		tempPulseBroadening = inputPulseBroadening;
		alargamentoDesteTrecho = 0.0;
		alargamentoDesteTrechoDcf = 0.0;
		
		if(network.isRd()){
			tempPulseBroadening += calculoRd_fnb(path, network.getTaxaBits(), lambda,
					network.getGama(), network.getnLambdaMax(), network.isRd());
		}
		
		if(network.isPmd()){
			alargamentoDesteTrecho = (0.01/network.getTaxaBits())*calculoPmd_fnb(path, network.getTaxaBits(),
					network.getdPmd(), network.isPmd());
		}
		
		if(network.isPmdDcf()){
			alargamentoDesteTrechoDcf = (0.01/network.getTaxaBits())*calculoPmd_Dcf_fnb(path, network.getTaxaBits(),
					network.getdPmdDcf(), network.isPmdDcf(), network.getGama());
		}
		
		//calcula o delta t(%) total da PMD (TX e DCF)
		double currentDeltaPmd = 100*network.getTaxaBits()*Math.sqrt(alargamentoDesteTrecho*alargamentoDesteTrecho
				+ alargamentoDesteTrechoDcf*alargamentoDesteTrechoDcf);
		
		outputPulseBroadening[0] = tempPulseBroadening + currentDeltaPmd;
		
		if( Math.abs(outputPulseBroadening[0])>network.getDelta() ){
			bp.setDispersion(1);
		}
		
		double fatorDeRuido = snr_fnb(network, path, lambda);
		
		if(fatorDeRuido < network.getSnrThresholddB()){
			bp.setBer(1);
		}
		
		return true;
	}

}
