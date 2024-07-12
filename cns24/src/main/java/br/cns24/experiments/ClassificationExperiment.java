/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: ClassificationExperiment.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	05/09/2013		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.experiments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import br.cns24.TMetric;
import br.cns24.experiments.nodePositions.CircularNetwork;
import br.cns24.models.AnchoredErdosRenyi;
import br.cns24.models.BarabasiDensity;
import br.cns24.models.ErdosRenyiM;
import br.cns24.models.GenerativeProcedure;
import br.cns24.models.KRegular;
import br.cns24.models.TModel;
import br.cns24.models.WattsStrogatzDensity;
import br.cns24.models.kStar;

/**
 * 
 * @author Danilo
 * @since 05/09/2013
 */
public class ClassificationExperiment {
	public static final String HEADER_TABLE = "ID\tp\tGH\tGM\tAPL\tDiam.\tClust.\tEntrop\tDLE\tLE\tDensity\tFZC\tHVC";
	public static final String START_FILE_NAME_ERDOS = "experiments/complenet-erdos-";
	public static final String START_FILE_NAME_KREGULAR = "experiments/complenet-kregular-";
	public static final String START_FILE_NAME_KSTAR = "experiments/complenet-kstar-";
	public static final String START_FILE_NAME_BARBASI = "experiments/complenet-barabasi-";
	public static final String START_FILE_NAME_WATTS = "experiments/complenet-watts-strogatz-";
	public static final String START_FILE_NAME_NWATTS = "experiments/complenet-newman-watts-";
	public static final String END_FILENAME = ".txt";
	public static final String NEW_LINE = "\n";
	public static final String TAB_SEP = "\t";
	public static final NumberFormat nf = NumberFormat.getInstance();

	public static final NumberFormat nf2 = NumberFormat.getInstance(new Locale("en-UK"));

	public static void degreeModels(List<TMetric> metricsToCalculate, int numNodes, double p) {
		GenerativeProcedure model = null;
		ComplexNetwork cn = null;

		double[] sequence = new double[numNodes];
		int runs = 30;
		// for (int i = 0; i < runs; i++) {
		// model = new ErdosRenyiM(p, numNodes);
		// cn = new ComplexNetwork(1, model.transform(new
		// Integer[numNodes][numNodes]), CircularNetwork.getInstance()
		// .createNodePositions(numNodes), TModel.CUSTOM, metricsToCalculate);
		// System.out.println(cn.getMetricValues().get(TMetric.DENSITY));
		// acumulateDegree(sequence, cn);
		// }
		//
		// printDegree(sequence, runs);
		//
		// System.out.println();
		//
		// for (int i = 0; i < runs; i++) {
		// model = new WattsStrogatzDensity(5 / 100.0, getK(p, numNodes), p);
		// cn = new ComplexNetwork(1, model.transform(new
		// Integer[numNodes][numNodes]), CircularNetwork.getInstance()
		// .createNodePositions(numNodes), TModel.CUSTOM, metricsToCalculate);
		// System.out.println(cn.getMetricValues().get(TMetric.DENSITY));
		// acumulateDegree(sequence, cn);
		// }
		//
		// printDegree(sequence, runs);
		// System.out.println();
		model = new AnchoredErdosRenyi((int) (numNodes * (numNodes - 1) * p / 2.0));
		for (int i = 0; i < runs; i++) {
			cn = new ComplexNetwork(1, model.transform(new Integer[numNodes][numNodes]), CircularNetwork.getInstance()
					.createNodePositions(numNodes), TModel.CUSTOM, metricsToCalculate);
			acumulateDegree(sequence, cn);
		}
		printDegree(sequence, runs);
		System.out.println();
		model = new BarabasiDensity(p, 1.00);
		for (int i = 0; i < runs; i++) {
			cn = new ComplexNetwork(1, model.grow(null, numNodes), CircularNetwork.getInstance().createNodePositions(
					numNodes), TModel.CUSTOM, metricsToCalculate);
			acumulateDegree(sequence, cn);
		}
		printDegree(sequence, runs);
		System.out.println();
		model = new BarabasiDensity(p, 1.40);
		for (int i = 0; i < runs; i++) {
			cn = new ComplexNetwork(1, model.grow(null, numNodes), CircularNetwork.getInstance().createNodePositions(
					numNodes), TModel.CUSTOM, metricsToCalculate);
			acumulateDegree(sequence, cn);
		}
		printDegree(sequence, runs);
	}

	/**
	 * @param cn
	 */
	private static void acumulateDegree(double[] sequence, ComplexNetwork cn) {
		cn.calculateDegreeSequenceDistribution();
		for (int i = 0; i < cn.getSequenceDegreeDistribution().length; i++) {
			sequence[i] += cn.getSequenceDegreeDistribution()[i];
		}
	}

	/**
	 * @param cn
	 */
	private static void printDegree(double[] sequence, double runs) {
		for (int i = 0; i < sequence.length; i++) {
			sequence[i] /= runs;
		}
		for (double n : sequence) {
			System.out.print(n + " ");
		}
		sequence = new double[sequence.length];
	}

	public static void main(String[] args) {
		List<TMetric> metricsToCalculate = new Vector<>();
		metricsToCalculate.add(TMetric.HUB_DEGREE);
		metricsToCalculate.add(TMetric.AVERAGE_DEGREE);
		metricsToCalculate.add(TMetric.AVERAGE_PATH_LENGTH);
		metricsToCalculate.add(TMetric.DIAMETER);
		metricsToCalculate.add(TMetric.CLUSTERING_COEFFICIENT);
		metricsToCalculate.add(TMetric.ENTROPY);
		metricsToCalculate.add(TMetric.DFT_LAPLACIAN_ENTROPY);
		metricsToCalculate.add(TMetric.LAPLACIAN_ENTROPY);
		metricsToCalculate.add(TMetric.DENSITY);
		nf.setMinimumFractionDigits(4);
		nf.setMaximumFractionDigits(4);
		nf2.setMinimumFractionDigits(4);
		nf2.setMaximumFractionDigits(4);

		try {
//			newmanWatts(14, 100, 0, metricsToCalculate);
			// barabasi(14, 100, metricsToCalculate);
			// barabasi(100, 30, metricsToCalculate, 1.4);
			// erdos(14, 100, metricsToCalculate);
			// kregular(14, 1, metricsToCalculate);
			// kstar(1000, 1, metricsToCalculate);
		} catch (Exception e) {
			e.printStackTrace();
		}

//		consolidate(14, 0);
		// consolidatePr(100);
		// consolidateSFExponent(14);
		 degreeModels(metricsToCalculate, 100, 0.02);
	}

	public static void consolidatePr(int numNodes) {
		double independentRuns = 30;
		try {
			File file = null;
			FileWriter fw = null;
			StringBuilder sb = new StringBuilder();
			fw = new FileWriter(new File("experiments/complenet-consol-pr" + "-" + numNodes + END_FILENAME));
			for (double p = 0.02; p <= 1; p += 0.02) {
				file = new File(START_FILE_NAME_WATTS + numNodes + "-" + nf.format(p) + "-5" + END_FILENAME);
				processFile(file, sb, independentRuns, p, numNodes);
				file = new File(START_FILE_NAME_WATTS + numNodes + "-" + nf.format(p) + "-10" + END_FILENAME);
				processFile(file, sb, independentRuns, p, numNodes);
				file = new File(START_FILE_NAME_WATTS + numNodes + "-" + nf.format(p) + "-15" + END_FILENAME);
				processFile(file, sb, independentRuns, p, numNodes);
				file = new File(START_FILE_NAME_WATTS + numNodes + "-" + nf.format(p) + "-20" + END_FILENAME);
				processFile(file, sb, independentRuns, p, numNodes);
				file = new File(START_FILE_NAME_ERDOS + numNodes + "-" + nf.format(p) + END_FILENAME);
				processFile(file, sb, independentRuns, p, numNodes);
				sb.append(NEW_LINE);
			}
			fw.write(sb.toString());
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void consolidateSFExponent(int numNodes) {
		double independentRuns = 30;
		try {
			File file = null;
			FileWriter fw = null;
			StringBuilder sb = new StringBuilder();
			fw = new FileWriter(new File("experiments/complenet-consol-sf-e" + "-" + numNodes + END_FILENAME));
			for (double p = 0.02; p <= 1; p += 0.02) {
				file = new File(START_FILE_NAME_BARBASI + numNodes + "-" + nf.format(0.8) + "-" + nf.format(p)
						+ END_FILENAME);
				processFile(file, sb, independentRuns, p, numNodes);
				file = new File(START_FILE_NAME_BARBASI + numNodes + "-" + nf.format(1.0) + "-" + nf.format(p)
						+ END_FILENAME);
				processFile(file, sb, independentRuns, p, numNodes);
				file = new File(START_FILE_NAME_BARBASI + numNodes + "-" + nf.format(1.4) + "-" + nf.format(p)
						+ END_FILENAME);
				processFile(file, sb, independentRuns, p, numNodes);
				file = new File(START_FILE_NAME_BARBASI + numNodes + "-" + nf.format(2.0) + "-" + nf.format(p)
						+ END_FILENAME);
				processFile(file, sb, independentRuns, p, numNodes);
				file = new File(START_FILE_NAME_ERDOS + numNodes + "-" + nf.format(p) + END_FILENAME);
				processFile(file, sb, independentRuns, p, numNodes);
				sb.append(NEW_LINE);
			}
			fw.write(sb.toString());
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void consolidate(int numNodes, double a) {
		double independentRuns = 30;
		try {
			File file = null;
			FileWriter fw = null;
			StringBuilder sb = new StringBuilder();
			fw = new FileWriter(new File("experiments/complenet-consol" + "-" + numNodes + END_FILENAME));
			for (double p = 0.02; p <= 1.0; p += 0.02) {
				file = new File(START_FILE_NAME_WATTS + numNodes + "-" + nf.format(p) + "-5" + END_FILENAME);
				processFile(file, sb, independentRuns, p, numNodes);
				file = new File(START_FILE_NAME_BARBASI + numNodes + "-" + nf.format(p) + END_FILENAME);
				processFile(file, sb, independentRuns, p, numNodes);
				file = new File(START_FILE_NAME_ERDOS + numNodes + "-" + nf.format(p) + END_FILENAME);
				processFile(file, sb, independentRuns, p, numNodes);
				file = new File(START_FILE_NAME_KREGULAR + numNodes + "-" + nf.format(p) + END_FILENAME);
				processFile(file, sb, independentRuns, p, numNodes);
				// file = new File(START_FILE_NAME_BARBASI + numNodes + "-" +
				// nf.format(a) + "-" + nf.format(p)
				// + END_FILENAME);
				// processFile(file, sb, independentRuns, p, numNodes);
				sb.append(NEW_LINE);
			}
			fw.write(sb.toString());
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void erdos(int numNodes, int independentRuns, List<TMetric> metricsToCalculate) throws IOException {
		FileWriter fwMetrics = null;
		StringBuilder sbMetrics = new StringBuilder();

		FileWriter fwNetwork = null;
		StringBuilder sbNetwork = new StringBuilder();

		ComplexNetwork network = null;
		ErdosRenyiM erdos = null;
		Integer[][] matrix = new Integer[numNodes][numNodes];

		for (double p = 0.02; p <= 1.0; p += 0.02) {
			fwMetrics = new FileWriter(new File(START_FILE_NAME_ERDOS + numNodes + "-" + nf.format(p) + END_FILENAME));
			fwNetwork = new FileWriter(new File(START_FILE_NAME_ERDOS + numNodes + "-" + nf.format(p) + "-network"
					+ END_FILENAME));

			sbMetrics = new StringBuilder();
			sbNetwork = new StringBuilder();

			sbMetrics.append(HEADER_TABLE).append(NEW_LINE);
			for (int id = 1; id <= independentRuns; id++) {
				erdos = new ErdosRenyiM(p, numNodes);
				network = new ComplexNetwork(id, erdos.transform(matrix), CircularNetwork.getInstance()
						.createNodePositions(numNodes), TModel.ERDOS_RENYI_N_M, metricsToCalculate);
				// while (network.getMetricValues().get(TMetric.DIAMETER) >
				// network.getAdjacencyMatrix().length-1){
				// network = new ComplexNetwork(id, erdos.transform(matrix),
				// CircularNetwork.getInstance()
				// .createNodePositions(numNodes), TModel.ERDOS_RENYI_N_M,
				// metricsToCalculate);
				// }
				System.out.println("Erdos " + id);
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

	private static void kregular(int numNodes, int independentRuns, List<TMetric> metricsToCalculate)
			throws IOException {
		FileWriter fwMetrics = null;
		StringBuilder sbMetrics = new StringBuilder();

		FileWriter fwNetwork = null;
		StringBuilder sbNetwork = new StringBuilder();

		ComplexNetwork network = null;
		KRegular kregular = null;
		Integer[][] matrix = new Integer[numNodes][numNodes];

		for (double p = 0.02; p <= 1; p += 0.02) {
			fwMetrics = new FileWriter(
					new File(START_FILE_NAME_KREGULAR + numNodes + "-" + nf.format(p) + END_FILENAME));
			fwNetwork = new FileWriter(new File(START_FILE_NAME_KREGULAR + numNodes + "-" + nf.format(p) + "-network"
					+ END_FILENAME));

			sbMetrics = new StringBuilder();
			sbNetwork = new StringBuilder();

			sbMetrics.append(HEADER_TABLE).append(NEW_LINE);
			for (int id = 1; id <= independentRuns; id++) {
				kregular = new KRegular(getK(p, numNodes));
				network = new ComplexNetwork(id, kregular.transform(matrix), CircularNetwork.getInstance()
						.createNodePositions(numNodes), TModel.K_REGULAR, metricsToCalculate);
				System.out.println("kregular " + id);
				createNetworkContent(sbNetwork, network);
				createMetricsContent(sbMetrics, network, p, id, metricsToCalculate);
			}
			sbMetrics.append(NEW_LINE);

			System.out.println("Gravando resultados do experimento " + START_FILE_NAME_KREGULAR + numNodes + "-"
					+ nf.format(p) + END_FILENAME);

			fwMetrics.write(sbMetrics.toString());
			fwNetwork.write(sbNetwork.toString());

			fwMetrics.close();
			fwNetwork.close();
		}
	}

	private static void kstar(int numNodes, int independentRuns, List<TMetric> metricsToCalculate) throws IOException {
		FileWriter fwMetrics = null;
		StringBuilder sbMetrics = new StringBuilder();

		FileWriter fwNetwork = null;
		StringBuilder sbNetwork = new StringBuilder();

		ComplexNetwork network = null;
		kStar kregular = null;
		Integer[][] matrix = new Integer[numNodes][numNodes];

		for (double p = 0.02; p <= 1; p += 0.02) {
			fwMetrics = new FileWriter(new File(START_FILE_NAME_KSTAR + numNodes + "-" + nf.format(p) + END_FILENAME));
			fwNetwork = new FileWriter(new File(START_FILE_NAME_KSTAR + numNodes + "-" + nf.format(p) + "-network"
					+ END_FILENAME));

			sbMetrics = new StringBuilder();
			sbNetwork = new StringBuilder();

			sbMetrics.append(HEADER_TABLE).append(NEW_LINE);
			for (int id = 1; id <= independentRuns; id++) {
				kregular = new kStar(getKStar(p, numNodes));
				network = new ComplexNetwork(id, kregular.transform(matrix), CircularNetwork.getInstance()
						.createNodePositions(numNodes), TModel.K_STAR, metricsToCalculate);
				System.out.println("kstar " + id);
				createNetworkContent(sbNetwork, network);
				createMetricsContent(sbMetrics, network, p, id, metricsToCalculate);
			}
			sbMetrics.append(NEW_LINE);

			System.out.println("Gravando resultados do experimento " + START_FILE_NAME_KSTAR + numNodes + "-"
					+ nf.format(p) + END_FILENAME);

			fwMetrics.write(sbMetrics.toString());
			fwNetwork.write(sbNetwork.toString());

			fwMetrics.close();
			fwNetwork.close();
		}
	}

	private static int getKStar(double density, int n) {
		int k = Math.max((int) Math.round(((n * (n - 1) * density) / 2.0 - 1) * (1.0 / (n - 2))), 1);
		return k;
	}

	private static int getK(double density, int n) {
		int k = Math.max((int) Math.round((density * (n - 1)) / 2.0), 1);
		return k;
	}

	private static void newmanWatts(int numNodes, int independentRuns, int rp, List<TMetric> metricsToCalculate)
			throws IOException {
		FileWriter fwMetrics = null;
		StringBuilder sbMetrics = new StringBuilder();

		FileWriter fwNetwork = null;
		StringBuilder sbNetwork = new StringBuilder();

		ComplexNetwork network = null;
		WattsStrogatzDensity watts = null;
		Integer[][] matrix = new Integer[numNodes][numNodes];

		for (double p = 0.02; p <= 1.0; p += 0.02) {
			fwMetrics = new FileWriter(new File(START_FILE_NAME_WATTS + numNodes + "-" + nf.format(p) + "-" + rp
					+ END_FILENAME));
			fwNetwork = new FileWriter(new File(START_FILE_NAME_WATTS + numNodes + "-" + nf.format(p) + "-" + rp
					+ "-network" + END_FILENAME));

			sbMetrics = new StringBuilder();
			sbNetwork = new StringBuilder();

			sbMetrics.append(HEADER_TABLE).append(NEW_LINE);
			for (int id = 1; id <= independentRuns; id++) {
				// System.out.println("Iniciando gera��o da rede " + id);
				watts = new WattsStrogatzDensity(rp / 100.0, getK(p, numNodes), p);
				System.out.println("Criando rede complexa " + id);
				network = new ComplexNetwork(id, watts.transform(matrix), CircularNetwork.getInstance()
						.createNodePositions(numNodes), TModel.NEWMAN_WATTS, metricsToCalculate);
				// System.out.println("Rede complexa " + id + " criada");
				// while (network.getMetricValues().get(TMetric.DIAMETER) >
				// network.getAdjacencyMatrix().length-1){
				// network = new ComplexNetwork(id, watts.transform(matrix),
				// CircularNetwork.getInstance()
				// .createNodePositions(numNodes), TModel.NEWMAN_WATTS,
				// metricsToCalculate);
				// }
				// System.out.println("Criando arquivo da rede...");
				createNetworkContent(sbNetwork, network);
				// System.out.println("Criando arquivo das métricas...");
				createMetricsContent(sbMetrics, network, p, id, metricsToCalculate);
			}
			sbMetrics.append(NEW_LINE);

			System.out.println("Gravando resultados do experimento " + START_FILE_NAME_WATTS + numNodes + "-"
					+ nf.format(p) + "-" + rp + END_FILENAME);

			fwMetrics.write(sbMetrics.toString());
			fwNetwork.write(sbNetwork.toString());

			fwMetrics.close();
			fwNetwork.close();
		}
	}

	private static void barabasi(int numNodes, int independentRuns, List<TMetric> metricsToCalculate, double a)
			throws IOException {
		FileWriter fwMetrics = null;
		StringBuilder sbMetrics = new StringBuilder();

		FileWriter fwNetwork = null;
		StringBuilder sbNetwork = new StringBuilder();

		ComplexNetwork network = null;
		BarabasiDensity barabasi = null;

		for (double p = 0.02; p <= 1.0; p += 0.02) {
			fwMetrics = new FileWriter(new File(START_FILE_NAME_BARBASI + numNodes + "-" + nf.format(a) + "-"
					+ nf.format(p) + END_FILENAME));
			fwNetwork = new FileWriter(new File(START_FILE_NAME_BARBASI + numNodes + "-" + nf.format(a) + "-"
					+ nf.format(p) + "-network" + END_FILENAME));

			sbMetrics = new StringBuilder();
			sbNetwork = new StringBuilder();

			sbMetrics.append(HEADER_TABLE).append(NEW_LINE);
			for (int id = 1; id <= independentRuns; id++) {
				// barabasi = new GeneralizedScaleFree(p, a);
				barabasi = new BarabasiDensity(p, a);
				network = new ComplexNetwork(id, barabasi.grow(null, numNodes), CircularNetwork.getInstance()
						.createNodePositions(numNodes), TModel.GENERALIZED_SCALE_FREE, metricsToCalculate);
				// while (network.getMetricValues().get(TMetric.DIAMETER) >
				// network.getAdjacencyMatrix().length-1){
				// network = new ComplexNetwork(id, barabasi.grow(null,
				// numNodes), CircularNetwork.getInstance()
				// .createNodePositions(numNodes), TModel.BARABASI_DENSITY,
				// metricsToCalculate);
				// }
				createNetworkContent(sbNetwork, network);
				createMetricsContent(sbMetrics, network, p, id, metricsToCalculate);
				System.out.println("Barabasi " + id);
			}
			sbMetrics.append(NEW_LINE);

			System.out.println("Gravando resultados do experimento " + START_FILE_NAME_BARBASI + numNodes + "-"
					+ nf.format(a) + "-" + nf.format(p) + END_FILENAME);

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

		for (double p = 0.02; p <= 1.0; p += 0.02) {
			fwMetrics = new FileWriter(new File(START_FILE_NAME_BARBASI + numNodes + "-" + nf.format(p) + END_FILENAME));
			fwNetwork = new FileWriter(new File(START_FILE_NAME_BARBASI + numNodes + "-" + nf.format(p) + "-network"
					+ END_FILENAME));

			sbMetrics = new StringBuilder();
			sbNetwork = new StringBuilder();

			sbMetrics.append(HEADER_TABLE).append(NEW_LINE);
			barabasi = new BarabasiDensity(p);
			for (int id = 1; id <= independentRuns; id++) {
				network = new ComplexNetwork(id, barabasi.grow(null, numNodes), CircularNetwork.getInstance()
						.createNodePositions(numNodes), TModel.BARABASI_DENSITY, metricsToCalculate);
				createNetworkContent(sbNetwork, network);
				createMetricsContent(sbMetrics, network, p, id, metricsToCalculate);
				System.out.println("Barabasi " + id);
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

	private static void createMetricsContent(StringBuilder sbMetrics, ComplexNetwork network, double p, int id,
			List<TMetric> metricsToCalculate) {
		sbMetrics.append(id).append(TAB_SEP);
		sbMetrics.append(nf.format(p)).append(TAB_SEP);

		for (TMetric metric : metricsToCalculate) {
			sbMetrics.append(nf.format(network.getMetricValues().get(metric))).append(TAB_SEP);
		}

		// network.calculateRealEigenvalues();

		sbMetrics.append(network.getFirstZeroEvenComponent()).append(TAB_SEP);
		sbMetrics.append(network.getMaxOddComponent()).append(TAB_SEP);

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

	private static void consolErdos(int numNodes, double independentRuns) throws Exception {
		File file = null;
		FileWriter fw = null;
		StringBuilder sb = new StringBuilder();

		fw = new FileWriter(new File(START_FILE_NAME_ERDOS + "consol" + "-" + numNodes + END_FILENAME));
		sb.append("p\tdm\tacm\tm1\tm2").append(NEW_LINE);
		for (double p = 0.01; p <= 1; p += 0.01) {
			file = new File(START_FILE_NAME_ERDOS + numNodes + "-" + nf.format(p) + END_FILENAME);
			processFile(file, sb, independentRuns, p, numNodes);
		}
		fw.write(sb.toString());
		fw.close();
	}

	private static void consolBarabasi(int numNodes, double independentRuns) throws Exception {
		File file = null;
		FileWriter fw = null;
		StringBuilder sb = new StringBuilder();
		fw = new FileWriter(new File(START_FILE_NAME_BARBASI + "consol" + "-" + numNodes + END_FILENAME));
		sb.append("p\tdm\tacm\tm1\tm2").append(NEW_LINE);
		for (double p = 0.01; p <= 1; p += 0.01) {
			file = new File(START_FILE_NAME_BARBASI + numNodes + "-" + nf.format(p) + END_FILENAME);
			processFile(file, sb, independentRuns, p, numNodes);
		}
		fw.write(sb.toString());
		fw.close();
	}

	private static void consolWatts(int numNodes, double independentRuns) throws Exception {
		File file = null;
		FileWriter fw = null;
		StringBuilder sb = new StringBuilder();
		fw = new FileWriter(new File(START_FILE_NAME_WATTS + "consol" + "-" + numNodes + END_FILENAME));
		sb.append("p\tdm\tacm\tm1\tm2").append(NEW_LINE);
		for (double p = 0.01; p <= 1; p += 0.01) {
			file = new File(START_FILE_NAME_WATTS + numNodes + "-" + nf.format(p) + END_FILENAME);
			processFile(file, sb, independentRuns, p, numNodes);
		}
		fw.write(sb.toString());
		fw.close();
	}

	private static void consolNewmanWatts(int numNodes, double independentRuns) throws Exception {
		File file = null;
		FileWriter fw = null;
		StringBuilder sb = new StringBuilder();
		fw = new FileWriter(new File(START_FILE_NAME_NWATTS + "consol" + "-" + numNodes + END_FILENAME));
		sb.append("p\tdm\tacm\tm1\tm2").append(NEW_LINE);
		for (double p = 0.01; p <= 1; p += 0.01) {
			file = new File(START_FILE_NAME_NWATTS + numNodes + "-" + nf.format(p) + END_FILENAME);
			processFile(file, sb, independentRuns, p, numNodes);
		}
		fw.write(sb.toString());
		fw.close();
	}

	private static void processFile(File file, StringBuilder sb, double independentRuns, double p, int nodes)
			throws FileNotFoundException, IOException, ParseException {
		String content;
		String[] linhas;
		String[] linha;
		char[] buffer;
		FileReader fr;
		double density = 0;
		double hubDegree = 0;
		double meanDegree = 0;
		double avaragePathLength = 0;
		double diameter = 0;
		double clusteringCoefficient = 0;
		double entropy = 0;
		double tLaplacianEntropy = 0;
		double LaplacianEntropy = 0;
		double realDensity = 0;
		double fzc = 0;
		double hvc = 0;
		System.out.println("Processando " + file.getAbsolutePath());
		fr = new FileReader(file);

		buffer = new char[(int) file.length()];

		fr.read(buffer);

		content = new String(buffer);

		linhas = content.split("\n");
		int count = 0;
		int max = (int) Math.min(independentRuns, linhas.length - 1);
		for (int id = 1; id <= max; id++) {
			linha = linhas[id].split("\t");

			// if (nf.parse(linha[5]).doubleValue() > nodes){
			// continue;
			// }
			if (nf.parse(linha[8]).doubleValue() == 0) {
				continue;
			}

			density += nf.parse(linha[1]).doubleValue();
			hubDegree += nf.parse(linha[2]).doubleValue();
			meanDegree += nf.parse(linha[3]).doubleValue();
			avaragePathLength += nf.parse(linha[4]).doubleValue();
			diameter += nf.parse(linha[5]).doubleValue();
			clusteringCoefficient += nf.parse(linha[6]).doubleValue();
			entropy += nf.parse(linha[7]).doubleValue();
			tLaplacianEntropy += nf.parse(linha[8]).doubleValue();
			LaplacianEntropy += nf.parse(linha[9]).doubleValue();
			realDensity += nf.parse(linha[10]).doubleValue();
			fzc += nf.parse(linha[11]).doubleValue();
			hvc += nf.parse(linha[12]).doubleValue();

			count++;
		}
		sb.append(nf2.format(p)).append(TAB_SEP);
		sb.append(nf2.format(hubDegree / count)).append(TAB_SEP);
		sb.append(nf2.format(meanDegree / count)).append(TAB_SEP);
		sb.append(nf2.format(avaragePathLength / count)).append(TAB_SEP);
		sb.append(nf2.format(diameter / count)).append(TAB_SEP);
		sb.append(nf2.format(clusteringCoefficient / count)).append(TAB_SEP);
		sb.append(nf2.format(entropy / count)).append(TAB_SEP);
		sb.append(nf2.format(tLaplacianEntropy / count)).append(TAB_SEP);
		sb.append(nf2.format(LaplacianEntropy / count)).append(TAB_SEP);
		sb.append(nf2.format(realDensity / count)).append(TAB_SEP);
		sb.append(nf2.format(fzc / count)).append(TAB_SEP);
		sb.append(nf2.format(hvc / count)).append(TAB_SEP);

		fr.close();
	}

}
