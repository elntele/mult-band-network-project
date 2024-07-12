/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: DFELaplacianEntropyExperiment.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	14/04/2014		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.experiments.dftLaplacianEntropy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Vector;

import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;
import br.cns24.experiments.nodePositions.CircularNetwork;
import br.cns24.models.BarabasiDensity;
import br.cns24.models.ErdosRenyiP;
import br.cns24.models.NewmanWatts;
import br.cns24.models.TModel;
import br.cns24.models.WattsStrogatz;

/**
 * 
 * @author Danilo
 * @since 14/04/2014
 */
public class DFELaplacianEntropyExperiment {
	private static final double START_DENSITY = 0.02;

	private static final double DENSITY_INCREMENT = 0.02;

	public static final String START_FILE_NAME_ERDOS = "dftLaplacianEntropy/complenet-erdos-";
	public static final String START_FILE_NAME_BARBASI = "dftLaplacianEntropy/complenet-barabasi-";
	public static final String START_FILE_NAME_WATTS = "dftLaplacianEntropy/complenet-watts-strogatz-";
	public static final String START_FILE_NAME_NWATTS = "dftLaplacianEntropy/complenet-newman-watts-";
	public static final String END_FILENAME = ".txt";
	public static final String NEW_LINE = "\n";
	public static final String TAB_SEP = "\t";
	public static final NumberFormat nf = NumberFormat.getInstance();

	public static void main(String[] args) {
		List<TMetric> metricsToCalculate = new Vector<>();
		metricsToCalculate.add(TMetric.DENSITY);
		metricsToCalculate.add(TMetric.NORMALIZED_DFT_LAPLACIAN_ENTROPY);
		nf.setMinimumFractionDigits(4);
		nf.setMaximumFractionDigits(4);

		try {
			watts(100, 30, metricsToCalculate);
			erdos(100, 30, metricsToCalculate);
			barabasi(100, 30, metricsToCalculate);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static StringBuffer getHeader(List<TMetric> metricsToCalculate) {
		StringBuffer sb = new StringBuffer("ID\tp");
		
		for (TMetric metric : metricsToCalculate) {
			sb.append("\t");
			sb.append(metric.toString());
		}
		
		return sb;
	}

	private static int getK(double density, int n) {
		int k =  (int)Math.round((density * n)/2.0);
		
		return k < 1 ? 1 : k;
	}

	private static void erdos(int numNodes, int independentRuns, List<TMetric> metricsToCalculate) throws IOException {
		FileWriter fwMetrics = null;
		StringBuilder sbMetrics = new StringBuilder();

		FileWriter fwNetwork = null;
		StringBuilder sbNetwork = new StringBuilder();

		ComplexNetwork network = null;
		ErdosRenyiP erdos = null;
		Integer[][] matrix = new Integer[numNodes][numNodes];

		for (double p = START_DENSITY; p <= 1; p += DENSITY_INCREMENT) {
			fwMetrics = new FileWriter(new File(START_FILE_NAME_ERDOS + numNodes + "-" + nf.format(p) + END_FILENAME));
			fwNetwork = new FileWriter(new File(START_FILE_NAME_ERDOS + numNodes + "-" + nf.format(p) + "-network"
					+ END_FILENAME));

			sbMetrics = new StringBuilder();
			sbNetwork = new StringBuilder();

			sbMetrics.append(getHeader(metricsToCalculate)).append(NEW_LINE);
			for (int id = 1; id <= independentRuns; id++) {
				erdos = new ErdosRenyiP(p);
				network = new ComplexNetwork(id, erdos.transform(matrix), CircularNetwork.getInstance()
						.createNodePositions(numNodes), TModel.ERDOS_RENYI_N_P, metricsToCalculate);

				createNetworkContent(sbNetwork, network);
				createMetricsContent(sbMetrics, network, p, id, metricsToCalculate);
			}
			sbMetrics.append(NEW_LINE);

			System.out.println("Gravando resultados do experimento " + START_FILE_NAME_ERDOS + numNodes + "-"
					+ nf.format(p) + END_FILENAME);

			fwMetrics.write(sbMetrics.toString());
			fwNetwork.write(sbNetwork.toString());

			fwMetrics.close();
			fwNetwork.close();
		}
	}

	private static void barabasi(int numNodes, int independentRuns, List<TMetric> metricsToCalculate)
			throws IOException {
		FileWriter fwMetrics = null;
		StringBuilder sbMetrics = new StringBuilder();

		FileWriter fwNetwork = null;
		StringBuilder sbNetwork = new StringBuilder();

		ComplexNetwork network = null;
		BarabasiDensity barabasi = null;

		for (double p = START_DENSITY; p <= 1; p += DENSITY_INCREMENT) {
			fwMetrics = new FileWriter(new File(START_FILE_NAME_BARBASI + numNodes + "-" + nf.format(p) + END_FILENAME));
			fwNetwork = new FileWriter(new File(START_FILE_NAME_BARBASI + numNodes + "-" + nf.format(p) + "-network"
					+ END_FILENAME));

			sbMetrics = new StringBuilder();
			sbNetwork = new StringBuilder();

			sbMetrics.append(getHeader(metricsToCalculate)).append(NEW_LINE);
			for (int id = 1; id <= independentRuns; id++) {
				barabasi = new BarabasiDensity(p);
				network = new ComplexNetwork(id, barabasi.grow(null, numNodes), CircularNetwork.getInstance()
						.createNodePositions(numNodes), TModel.BARABASI_DENSITY, metricsToCalculate);

				createNetworkContent(sbNetwork, network);
				createMetricsContent(sbMetrics, network, p, id, metricsToCalculate);
			}
			sbMetrics.append(NEW_LINE);

			System.out.println("Gravando resultados do experimento " + START_FILE_NAME_BARBASI + numNodes + "-"
					+ nf.format(p) + END_FILENAME);

			fwMetrics.write(sbMetrics.toString());
			fwNetwork.write(sbNetwork.toString());

			fwMetrics.close();
			fwNetwork.close();
		}
	}

	private static void watts(int numNodes, int independentRuns, List<TMetric> metricsToCalculate) throws IOException {
		FileWriter fwMetrics = null;
		StringBuilder sbMetrics = new StringBuilder();

		FileWriter fwNetwork = null;
		StringBuilder sbNetwork = new StringBuilder();

		ComplexNetwork network = null;
		WattsStrogatz watts = null;
		Integer[][] matrix = new Integer[numNodes][numNodes];
		double probability = 0.1;

		for (double p = START_DENSITY; p <= 1; p += DENSITY_INCREMENT) {
			fwMetrics = new FileWriter(new File(START_FILE_NAME_WATTS + numNodes + "-" + nf.format(p) + END_FILENAME));
			fwNetwork = new FileWriter(new File(START_FILE_NAME_WATTS + numNodes + "-" + nf.format(p) + "-network"
					+ END_FILENAME));

			sbMetrics = new StringBuilder();
			sbNetwork = new StringBuilder();

			sbMetrics.append(getHeader(metricsToCalculate)).append(NEW_LINE);
			for (int id = 1; id <= independentRuns; id++) {
				watts = new WattsStrogatz(probability, getK(p, numNodes));
				network = new ComplexNetwork(id, watts.transform(matrix), CircularNetwork.getInstance()
						.createNodePositions(numNodes), TModel.WATTS_STROGATZ, metricsToCalculate);

				createNetworkContent(sbNetwork, network);
				createMetricsContent(sbMetrics, network, p, id, metricsToCalculate);
			}
			sbMetrics.append(NEW_LINE);

			System.out.println("Gravando resultados do experimento " + START_FILE_NAME_WATTS + numNodes + "-"
					+ nf.format(p) + END_FILENAME);

			fwMetrics.write(sbMetrics.toString());
			fwNetwork.write(sbNetwork.toString());

			fwMetrics.close();
			fwNetwork.close();
		}
	}

	private static void newmanWatts(int numNodes, int independentRuns, List<TMetric> metricsToCalculate)
			throws IOException {
		FileWriter fwMetrics = null;
		StringBuilder sbMetrics = new StringBuilder();

		FileWriter fwNetwork = null;
		StringBuilder sbNetwork = new StringBuilder();

		ComplexNetwork network = null;
		NewmanWatts watts = null;
		Integer[][] matrix = new Integer[numNodes][numNodes];

		for (double p = START_DENSITY; p <= 1; p += DENSITY_INCREMENT) {
			fwMetrics = new FileWriter(new File(START_FILE_NAME_NWATTS + numNodes + "-" + nf.format(p) + END_FILENAME));
			fwNetwork = new FileWriter(new File(START_FILE_NAME_NWATTS + numNodes + "-" + nf.format(p) + "-network"
					+ END_FILENAME));

			sbMetrics = new StringBuilder();
			sbNetwork = new StringBuilder();

			sbMetrics.append(getHeader(metricsToCalculate)).append(NEW_LINE);
			for (int id = 1; id <= independentRuns; id++) {
				watts = new NewmanWatts(0.1, getK(p, numNodes));
				network = new ComplexNetwork(id, watts.transform(matrix), CircularNetwork.getInstance()
						.createNodePositions(numNodes), TModel.NEWMAN_WATTS, metricsToCalculate);

				createNetworkContent(sbNetwork, network);
				createMetricsContent(sbMetrics, network, p, id, metricsToCalculate);
			}
			sbMetrics.append(NEW_LINE);

			System.out.println("Gravando resultados do experimento " + START_FILE_NAME_NWATTS + numNodes + "-"
					+ nf.format(p) + END_FILENAME);

			fwMetrics.write(sbMetrics.toString());
			fwNetwork.write(sbNetwork.toString());

			fwMetrics.close();
			fwNetwork.close();
		}
	}

	private static void createMetricsContent(StringBuilder sbMetrics, ComplexNetwork network, double p, int id, List<TMetric> metricsToCalculate) {
		network.calculateRealEigenvalues();
		sbMetrics.append(id).append(TAB_SEP);
		sbMetrics.append(nf.format(p)).append(TAB_SEP);
		for (TMetric metric : metricsToCalculate) {
			sbMetrics.append(nf.format(network.getMetricValues().get(metric))).append(TAB_SEP);	
		}

		sbMetrics.append(NEW_LINE);
	}

	private static void createNetworkContent(StringBuilder sbNetwork, ComplexNetwork network) {
		for (Integer[] linha : network.getAdjacencyMatrix()) {
			for (int valor : linha) {
				sbNetwork.append(valor).append(TAB_SEP);
			}
		}
		sbNetwork.append(NEW_LINE);
	}
}
