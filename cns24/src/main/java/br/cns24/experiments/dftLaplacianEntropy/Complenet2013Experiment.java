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
import br.cns24.models.WattsStrogatzDensity;

public class Complenet2013Experiment {
	public static final String HEADER_TABLE = "ID\tp\tDensity\tAC\tEf0\tOnmax";
	public static final String START_FILE_NAME_ERDOS = "complenet/complenet-erdos-";
	public static final String START_FILE_NAME_BARBASI = "complenet/complenet-barabasi-";
	public static final String START_FILE_NAME_WATTS = "complenet/complenet-watts-strogatz-";
	public static final String START_FILE_NAME_NWATTS = "complenet/complenet-newman-watts-";
	public static final String END_FILENAME = ".txt";
	public static final String NEW_LINE = "\n";
	public static final String TAB_SEP = "\t";
	public static final NumberFormat nf = NumberFormat.getInstance();
	
	public static void main(String[] args) {
		StringBuffer sbFzc = new StringBuffer();
		StringBuffer sbHvc = new StringBuffer();
		int numRuns = 100;
		ComplexNetwork cn = null;
		WattsStrogatzDensity ws = null;
		BarabasiDensity ba = null;
		ErdosRenyiP er = null;
		double density = 0.10;
		List<TMetric> metrics = new Vector<TMetric>();
		metrics.add(TMetric.DENSITY);
		metrics.add(TMetric.DFT_LAPLACIAN_ENTROPY);
		metrics.add(TMetric.HUB_DEGREE);
		
		int[] numNodes = new int[]{100, 200, 400};
		
		for (int i = 0; i < numRuns; i++) {
			for (int n : numNodes) {
				ba = new BarabasiDensity(density);
				cn = new ComplexNetwork(0, ba.grow(null, n), new double[n][n], TModel.BARABASI_DENSITY, metrics);
				cn.calculateRealEigenvalues();
				sbHvc.append(cn.getMaxOddComponent()).append(" ");
				sbFzc.append(cn.getFirstZeroEvenComponent()).append(" ");

				er = new ErdosRenyiP(density);
				cn = new ComplexNetwork(0, er.transform(new Integer[n][n]), new double[n][n], TModel.ERDOS_RENYI_N_P, metrics);
				cn.calculateRealEigenvalues();
				sbHvc.append(cn.getMaxOddComponent()).append(" ");
				sbFzc.append(cn.getFirstZeroEvenComponent()).append(" ");
				
				ws = new WattsStrogatzDensity(0.10, getK(density, n), density, true);
				cn = new ComplexNetwork(0, ws.transform(new Integer[n][n]), new double[n][n], TModel.WATTS_STROGATZ_DENSITY, metrics);
				cn.calculateRealEigenvalues();
				sbHvc.append(cn.getMaxOddComponent()).append(" ");
				sbFzc.append(cn.getFirstZeroEvenComponent()).append(" ");
			}
			sbHvc.append("\n");
			sbFzc.append("\n");
		}
		System.out.println(sbHvc);
		System.out.println();
		System.out.println(sbFzc);
	}

	public static void main1(String[] args) {
		List<TMetric> metricsToCalculate = new Vector<>();
		metricsToCalculate.add(TMetric.DENSITY);
		metricsToCalculate.add(TMetric.CLUSTERING_COEFFICIENT);
		metricsToCalculate.add(TMetric.ASSORTATIVITY);
		metricsToCalculate.add(TMetric.LAPLACIAN_ENTROPY);
		nf.setMinimumFractionDigits(4);
		nf.setMaximumFractionDigits(4);

		try {
//			watts(453, 10, metricsToCalculate);
//			erdos(113, 10, metricsToCalculate);
//			barabasi(113, 10, metricsToCalculate);
			barabasi(100, 21, metricsToCalculate);
			watts(100, 21, metricsToCalculate);
			erdos(100, 21, metricsToCalculate);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static int getK(double density, int n){
		return (int)Math.round(density * (n - 1));
	}

	private static void erdos(int numNodes, int independentRuns, List<TMetric> metricsToCalculate) throws IOException {
		FileWriter fwMetrics = null;
		StringBuilder sbMetrics = new StringBuilder();
		
		FileWriter fwNetwork = null;
		StringBuilder sbNetwork = new StringBuilder();
		
		ComplexNetwork network = null;
		ErdosRenyiP erdos = null;
		Integer[][] matrix = new Integer[numNodes][numNodes];
		

		for (double p = 0.35; p <= 0.35; p += 0.01) {
			fwMetrics = new FileWriter(new File(START_FILE_NAME_ERDOS + numNodes + "-" + nf.format(p) + END_FILENAME));
			fwNetwork = new FileWriter(new File(START_FILE_NAME_ERDOS + numNodes + "-" + nf.format(p) + "-network" + END_FILENAME));
			
			sbMetrics = new StringBuilder();
			sbNetwork = new StringBuilder();
			
			sbMetrics.append(HEADER_TABLE).append(NEW_LINE);
			for (int id = 1; id <= independentRuns; id++) {
				erdos = new ErdosRenyiP(p);
				network = new ComplexNetwork(id, erdos.transform(matrix), CircularNetwork.getInstance()
						.createNodePositions(numNodes), TModel.ERDOS_RENYI_N_P, metricsToCalculate);
				
				createNetworkContent(sbNetwork, network);
				createMetricsContent(sbMetrics, network, p, id);
			}
			sbMetrics.append(NEW_LINE);
			
			System.out.println("Gravando resultados do experimento " + START_FILE_NAME_ERDOS + numNodes + "-" + nf.format(p) + END_FILENAME);
			
			fwMetrics.write(sbMetrics.toString());
			fwNetwork.write(sbNetwork.toString());
			
			fwMetrics.close();
			fwNetwork.close();
		}
	}
	
	private static void barabasi(int numNodes, int independentRuns, List<TMetric> metricsToCalculate) throws IOException {
		FileWriter fwMetrics = null;
		StringBuilder sbMetrics = new StringBuilder();
		
		FileWriter fwNetwork = null;
		StringBuilder sbNetwork = new StringBuilder();
		
		ComplexNetwork network = null;
		BarabasiDensity barabasi = null;

		for (double p = 0.02; p <= 0.98; p += 0.02) {
			fwMetrics = new FileWriter(new File(START_FILE_NAME_BARBASI + numNodes + "-" + nf.format(p) + END_FILENAME));
			fwNetwork = new FileWriter(new File(START_FILE_NAME_BARBASI + numNodes + "-" + nf.format(p) + "-network" + END_FILENAME));
			
			sbMetrics = new StringBuilder();
			sbNetwork = new StringBuilder();
			
			sbMetrics.append(HEADER_TABLE).append(NEW_LINE);
			for (int id = 1; id <= independentRuns; id++) {
				barabasi = new BarabasiDensity(p);
				network = new ComplexNetwork(id, barabasi.grow(null, numNodes), CircularNetwork.getInstance()
						.createNodePositions(numNodes), TModel.BARABASI_DENSITY, metricsToCalculate);
				
				createNetworkContent(sbNetwork, network);
				createMetricsContent(sbMetrics, network, p, id);
			}
			sbMetrics.append(NEW_LINE);
			
			System.out.println("Gravando resultados do experimento " + START_FILE_NAME_BARBASI + numNodes + "-" + nf.format(p) + END_FILENAME);
			
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
		WattsStrogatzDensity watts = null;
		Integer[][] matrix = new Integer[numNodes][numNodes];
		double probability = 0.1;

		for (double p = 0.02; p <= 0.98; p += 0.02) {
			fwMetrics = new FileWriter(new File(START_FILE_NAME_WATTS + numNodes + "-" + nf.format(p) + END_FILENAME));
			fwNetwork = new FileWriter(new File(START_FILE_NAME_WATTS + numNodes + "-" + nf.format(p) + "-network" + END_FILENAME));
			
			sbMetrics = new StringBuilder();
			sbNetwork = new StringBuilder();
			
			sbMetrics.append(HEADER_TABLE).append(NEW_LINE);
			for (int id = 1; id <= independentRuns; id++) {
				watts = new WattsStrogatzDensity(probability, getK(p, numNodes), p, true);
				network = new ComplexNetwork(id, watts.transform(matrix), CircularNetwork.getInstance()
						.createNodePositions(numNodes), TModel.WATTS_STROGATZ, metricsToCalculate);
				
				createNetworkContent(sbNetwork, network);
				createMetricsContent(sbMetrics, network, p, id);
			}
			sbMetrics.append(NEW_LINE);
			
			System.out.println("Gravando resultados do experimento " + START_FILE_NAME_WATTS + numNodes + "-" + nf.format(p) + END_FILENAME);
			
			fwMetrics.write(sbMetrics.toString());
			fwNetwork.write(sbNetwork.toString());
			
			fwMetrics.close();
			fwNetwork.close();
		}
	}
	
	private static void newmanWatts(int numNodes, int independentRuns, List<TMetric> metricsToCalculate) throws IOException {
		FileWriter fwMetrics = null;
		StringBuilder sbMetrics = new StringBuilder();
		
		FileWriter fwNetwork = null;
		StringBuilder sbNetwork = new StringBuilder();
		
		ComplexNetwork network = null;
		NewmanWatts watts = null;
		Integer[][] matrix = new Integer[numNodes][numNodes];
		

		for (double p = 0.02; p <= 0.98; p += 0.02) {
			fwMetrics = new FileWriter(new File(START_FILE_NAME_NWATTS + numNodes + "-" + nf.format(p) + END_FILENAME));
			fwNetwork = new FileWriter(new File(START_FILE_NAME_NWATTS + numNodes + "-" + nf.format(p) + "-network" + END_FILENAME));
			
			sbMetrics = new StringBuilder();
			sbNetwork = new StringBuilder();
			
			sbMetrics.append(HEADER_TABLE).append(NEW_LINE);
			for (int id = 1; id <= independentRuns; id++) {
				watts = new NewmanWatts(0.1, getK(p, numNodes));
				network = new ComplexNetwork(id, watts.transform(matrix), CircularNetwork.getInstance()
						.createNodePositions(numNodes), TModel.NEWMAN_WATTS, metricsToCalculate);
				
				createNetworkContent(sbNetwork, network);
				createMetricsContent(sbMetrics, network, p, id);
			}
			sbMetrics.append(NEW_LINE);
			
			System.out.println("Gravando resultados do experimento " + START_FILE_NAME_NWATTS + numNodes + "-" + nf.format(p) + END_FILENAME);
			
			fwMetrics.write(sbMetrics.toString());
			fwNetwork.write(sbNetwork.toString());
			
			fwMetrics.close();
			fwNetwork.close();
		}
	}

	private static void createMetricsContent(StringBuilder sbMetrics, ComplexNetwork network, double p, int id) {
		sbMetrics.append(id).append(TAB_SEP);
		sbMetrics.append(nf.format(p)).append(TAB_SEP);
		sbMetrics.append(nf.format(network.getMetricValues().get(TMetric.DENSITY))).append(TAB_SEP);
		sbMetrics.append(nf.format(network.getMetricValues().get(TMetric.ASSORTATIVITY))).append(TAB_SEP);
		sbMetrics.append(nf.format(network.getMetricValues().get(TMetric.CLUSTERING_COEFFICIENT))).append(TAB_SEP);
		sbMetrics.append(nf.format(network.getMetricValues().get(TMetric.AVERAGE_PATH_LENGTH))).append(TAB_SEP);
		sbMetrics.append(nf.format(network.getMetricValues().get(TMetric.LAPLACIAN_ENTROPY))).append(TAB_SEP);
		
		network.calculateRealEigenvalues();
		sbMetrics.append(network.getFirstZeroEvenComponent()).append(TAB_SEP);
		sbMetrics.append(network.getMaxOddComponent()).append(TAB_SEP);
		
		
//		for (double par : network.getFftValuesPares()) {
//			sbMetrics.append(nf.format(par)).append(TAB_SEP);
//		}
//		for (double par : network.getFftValuesImpares()) {
//			sbMetrics.append(nf.format(par)).append(TAB_SEP);
//		}
//		for (double par : network.getRealEigenvalues()) {
//			sbMetrics.append(nf.format(par)).append(TAB_SEP);
//		}
		
		sbMetrics.append(NEW_LINE);
	}

	private static void createNetworkContent(StringBuilder sbNetwork, ComplexNetwork network) {
		for (Integer[] linha : network.getAdjacencyMatrix()){
			for (int valor : linha){
				sbNetwork.append(valor).append(TAB_SEP);	
			}
		}
		sbNetwork.append(NEW_LINE);
	}
}
