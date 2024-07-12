package br.cns24.persistence;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.List;
import java.util.Vector;

import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;
import br.cns24.experiments.nodePositions.CircularNetwork;
import br.cns24.experiments.nodePositions.RandomNodePositions;
import br.cns24.experiments.setup.ApplicationSetup;
import br.cns24.experiments.setup.MetricExperimentSetup;
import br.cns24.models.BarabasiDensity;
import br.cns24.models.ErdosRenyiM;
import br.cns24.models.TModel;
import br.cns24.models.WattsStrogatzDensity;

public class SimpleEdgeDataSet {
	public static ComplexNetwork importNetwork(String path) throws IOException {
		ComplexNetwork cn = null;

		FileReader fr = new FileReader(new File(path));
		LineNumberReader lnr = new LineNumberReader(fr);

		String line = lnr.readLine();
		boolean startEdges = false;
		int sourceNode = 0;
		int endNode = 0;
		int maxNode = 0;
		List<int[]> edges = new Vector<>();
		int numEdges = 0;
		String[] aLine = null;
		while (!startEdges) {
			if (line.startsWith("%")) {
				line = lnr.readLine();
			} else {
				startEdges = true;
			}
		}
		while (line != null && !line.trim().equals("")) {
			numEdges++;
			aLine = line.trim().split("	");
			sourceNode = Integer.parseInt(aLine[0]);
			for (int i = 1; i < aLine.length; i++) {
				if (!aLine[i].trim().equals("")) {
					endNode = Integer.parseInt(aLine[i]);
					break;
				}
			}
			edges.add(new int[] { sourceNode, endNode });
			if (sourceNode > maxNode) {
				maxNode = sourceNode;
			}
			if (endNode > maxNode) {
				maxNode = endNode;
			}
			line = lnr.readLine();
		}
		Integer[][] adjacencyMatrix = new Integer[maxNode][maxNode];
		for (int i = 0; i < maxNode; i++) {
			for (int j = 0; j < maxNode; j++) {
				adjacencyMatrix[i][j] = 0;
			}
		}

		System.out.println("Edges: " + numEdges);
		System.out.println("Density: " + numEdges / (maxNode * (maxNode - 1.0)));

		int repetidos = 0;
		for (int[] edge : edges) {
			if (adjacencyMatrix[edge[0] - 1][edge[1] - 1] == 1) {
				repetidos++;
			}
			adjacencyMatrix[edge[0] - 1][edge[1] - 1] = 1;
			adjacencyMatrix[edge[1] - 1][edge[0] - 1] = 1;
		}
		System.out.println("Repetidos: " + repetidos);

		ApplicationSetup appSetup = new ApplicationSetup();

		try {
			appSetup = ApplicationSetupDaoXml.getInstance().load("cns-config.xml");
		} catch (IOException e) {
		}

		List<TMetric> metric = new Vector<>();
		for (MetricExperimentSetup mes : appSetup.getMetrics()) {
			metric.add(mes.getMetric());
		}

		cn = new ComplexNetwork(1, adjacencyMatrix, CircularNetwork.getInstance().createNodePositions(maxNode),
				TModel.CUSTOM, metric);

		lnr.close();
		fr.close();

		return cn;
	}

	public static ComplexNetwork getNetwork(String path, List<TMetric> metrics, String delimiter) throws IOException {
		ComplexNetwork cn = null;

		FileReader fr = new FileReader(new File(path));
		LineNumberReader lnr = new LineNumberReader(fr);

		String line = lnr.readLine();
		boolean startEdges = false;
		if (delimiter == null) {
			startEdges = true;
		}
		int sourceNode = 0;
		int endNode = 0;
		int maxNode = 0;
		List<int[]> edges = new Vector<>();
		int numEdges = 0;
		String[] aLine = null;
		while (line != null && !line.trim().equals("")) {
			if (startEdges) {
				numEdges++;
				aLine = line.trim().split(" ");
				sourceNode = Integer.parseInt(aLine[0]);
				for (int i = 1; i < aLine.length; i++) {
					if (!aLine[i].trim().equals("")) {
						endNode = Integer.parseInt(aLine[i]);
						break;
					}
				}
				edges.add(new int[] { sourceNode, endNode });
				if (sourceNode > maxNode) {
					maxNode = sourceNode;
				}
				if (endNode > maxNode) {
					maxNode = endNode;
				}
			} else if (line.contains(delimiter)) {
				startEdges = true;
			}
			line = lnr.readLine();
		}
		Integer[][] adjacencyMatrix = new Integer[maxNode][maxNode];
		for (int i = 0; i < maxNode; i++) {
			for (int j = 0; j < maxNode; j++) {
				adjacencyMatrix[i][j] = 0;
			}
		}

		System.out.println("Edges: " + numEdges);
		System.out.println("Density: " + numEdges / (maxNode * (maxNode - 1.0)));

		int repetidos = 0;
		for (int[] edge : edges) {
			if (adjacencyMatrix[edge[0] - 1][edge[1] - 1] == 1) {
				repetidos++;
			}
			adjacencyMatrix[edge[0] - 1][edge[1] - 1] = 1;
			adjacencyMatrix[edge[1] - 1][edge[0] - 1] = 1;
		}
		System.out.println("Repetidos: " + repetidos);

		cn = new ComplexNetwork(1, adjacencyMatrix, RandomNodePositions.getInstance().createNodePositions(maxNode),
				TModel.CUSTOM, metrics);

		lnr.close();
		fr.close();

		return cn;
	}

	public static ComplexNetwork getTNet(String path, List<TMetric> metrics, String delimiter) throws IOException {
		ComplexNetwork cn = null;

		FileReader fr = new FileReader(new File(path));
		LineNumberReader lnr = new LineNumberReader(fr);

		String line = lnr.readLine();
		int sourceNode = 0;
		int endNode = 0;
		int maxNode = 0;
		List<int[]> edges = new Vector<>();
		int numEdges = 0;
		String[] values = null;
		while (line != null && !line.trim().equals("")) {
			numEdges++;
			line = line.trim();
			values = line.split(" ");
			sourceNode = Integer.parseInt(values[0]);
			for (int i = 1; i < values.length; i++) {
				if (!values[i].trim().equals("")) {
					endNode = Integer.parseInt(values[i]);
					break;
				}
			}
			edges.add(new int[] { sourceNode, endNode });
			if (sourceNode > maxNode) {
				maxNode = sourceNode;
			}
			if (endNode > maxNode) {
				maxNode = endNode;
			}
			line = lnr.readLine();
		}
		Integer[][] adjacencyMatrix = new Integer[maxNode][maxNode];
		for (int i = 0; i < maxNode; i++) {
			for (int j = 0; j < maxNode; j++) {
				adjacencyMatrix[i][j] = 0;
			}
		}

		int repetidos = 0;
		for (int[] edge : edges) {
			if (adjacencyMatrix[edge[0] - 1][edge[1] - 1] == 1) {
				repetidos++;
			}
			adjacencyMatrix[edge[0] - 1][edge[1] - 1] = 1;
			adjacencyMatrix[edge[1] - 1][edge[0] - 1] = 1;
		}
		System.out.println("Repetidos: " + repetidos);
		System.out.println("Edges: " + numEdges);
		System.out.println("Edges �nicos: " + (numEdges - repetidos));
		System.out.println("Density: " + 2 * (numEdges - repetidos) / (maxNode * (maxNode - 1.0)));

		cn = new ComplexNetwork(1, adjacencyMatrix, RandomNodePositions.getInstance().createNodePositions(maxNode),
				TModel.CUSTOM, metrics);

		lnr.close();
		fr.close();

		return cn;
	}

	public static ComplexNetwork getDatNet(String path, List<TMetric> metrics, String delimiter) throws IOException {
		ComplexNetwork cn = null;

		FileReader fr = new FileReader(new File(path));
		LineNumberReader lnr = new LineNumberReader(fr);

		String line = lnr.readLine();
		boolean startEdges = false;
		if (delimiter == null) {
			startEdges = true;
		}
		int sourceNode = 0;
		int endNode = 0;
		int maxNode = 0;
		List<int[]> edges = new Vector<>();
		int numEdges = 0;
		String[] cols = null;
		while (line != null && !line.trim().equals("")) {
			if (startEdges) {
				sourceNode++;
				line = line.trim();
				cols = line.split(" ");
				endNode = 0;
				maxNode = cols.length;
				if (sourceNode > maxNode) {
					break;
				}
				for (String col : cols) {
					endNode++;
					if (Integer.parseInt(col) > 0) {
						edges.add(new int[] { sourceNode, endNode });
						numEdges++;
					}
				}
			} else if (line.contains(delimiter)) {
				startEdges = true;
			}
			line = lnr.readLine();
		}
		Integer[][] adjacencyMatrix = new Integer[maxNode][maxNode];
		for (int i = 0; i < maxNode; i++) {
			for (int j = 0; j < maxNode; j++) {
				adjacencyMatrix[i][j] = 0;
			}
		}

		System.out.println("Edges: " + numEdges);
		System.out.println("Density: " + numEdges / (maxNode * (maxNode - 1.0)));

		int repetidos = 0;
		for (int[] edge : edges) {
			if (adjacencyMatrix[edge[0] - 1][edge[1] - 1] == 1) {
				repetidos++;
			}
			adjacencyMatrix[edge[0] - 1][edge[1] - 1] = 1;
			adjacencyMatrix[edge[1] - 1][edge[0] - 1] = 1;
		}
		System.out.println("Repetidos: " + repetidos);

		cn = new ComplexNetwork(1, adjacencyMatrix, RandomNodePositions.getInstance().createNodePositions(maxNode),
				TModel.CUSTOM, metrics);

		lnr.close();
		fr.close();

		return cn;
	}

	public static void main(String[] args) throws IOException {
		List<TMetric> metrics = new Vector<>();
		metrics.add(TMetric.HUB_DEGREE);
		metrics.add(TMetric.AVERAGE_DEGREE);
		metrics.add(TMetric.AVERAGE_PATH_LENGTH);
		metrics.add(TMetric.DIAMETER);
		metrics.add(TMetric.CLUSTERING_COEFFICIENT);
		metrics.add(TMetric.ENTROPY);
		metrics.add(TMetric.DFT_LAPLACIAN_ENTROPY);
		metrics.add(TMetric.DENSITY);
		metrics.add(TMetric.ASSORTATIVITY);

		String[][] dataSets = new String[][] {
		// {"Jazz musicians network", "F:\\doutorado\\datasets\\jazz\\jazz.net",
		// "*Edges"},
		 {"Caenorhabditis elegans metabolic network",
		 "C:\\doutorado\\datasets\\celegans_metabolic\\celegans_metabolic.net",
		 "*Edges"},
		// { "Email", "C:\\doutorado\\datasets\\email\\email.txt", "" },
		// {"Euroroad", "F:\\doutorado\\datasets\\euroroad.net", "*edges"},
		// {"Slovenian computer scientists",
		// "F:\\doutorado\\datasets\\scsc.net", "*edges"},
		// { "Amazon Web Pages", "F:\\doutorado\\datasets\\amazon.net", "*edges"
		// },
		// { "US Airports 2010", "F:\\doutorado\\datasets\\USairport_2010.txt",
		// null },
		// { "Highland tribes", "F:\\doutorado\\datasets\\highland_tribes.dat",
		// "DATA:" },
//		{ "zachary.dat", "F:\\doutorado\\datasets\\zachary.dat", "DATA:" },
		// { "Highland tribes",
		// "C:\\doutorado\\datasets\\ucidata-gama.tar\\out.ucidata-gama",
		// "% 58 16 16" },
		// { "US Power grid", "F:\\doutorado\\datasets\\USpowergrid_n4941.txt",
		// null },
		// {"E-mail network URV", "C:\\doutorado\\datasets\\email\\email.txt",
		// null},
		// { "Sociopatterns-infectious network",
		// "F:\\doutorado\\datasets\\sociopatterns-infectious\\out.sociopatterns-infectious",
		// "% 17298 410 410" },
//		 {"Sociopatterns-hipertext network",
//		 "C:\\doutorado\\datasets\\sociopatterns-hypertext\\out.sociopatterns-hypertext",
//		 "% 20818 113 113"},
		// { "radoslaw_email",
		// "C:\\doutorado\\datasets\\radoslaw_email\\out.radoslaw_email",
		// "% 82927 154 140" },
		// {"Similarity",
		// "C:\\doutorado\\datasets\\dbpedia-similar\\out.dbpedia-similar",
		// "% sym unweighted"},
		// {"Hamsterster network",
		// "F:\\doutorado\\datasets\\petster-hamster\\out.petster-hamster",
		// "% sym unweighted"}
		};
		double density = 0;
		int nodes = 0;
		for (String[] dataSet : dataSets) {
			System.out.println("An�lise do dataset " + dataSet[0]);

			ComplexNetwork cn = null;
			if (dataSet[2] == null) {
				cn = getTNet(dataSet[1], metrics, dataSet[2]);
			} else {
				if (dataSet[1].endsWith(".dat")) {
					cn = getDatNet(dataSet[1], metrics, dataSet[2]);
				} else {
					cn = getNetwork(dataSet[1], metrics, dataSet[2]);
				}
			}
			cn.calculateRealEigenvalues();
			cn.evaluate();
			System.out.print(cn.printMetrics());
			density = cn.getMetricValues().get(TMetric.DENSITY);
			nodes = cn.getAdjacencyMatrix().length;
			 System.out.println("FZC: " + cn.getFirstZeroEvenComponent());
			 System.out.println("HVC: " + cn.getMaxOddComponent());
			System.out.println("Número de nós: " + cn.getAdjacencyMatrix().length);

			// System.out.println("Autovalores da matriz laplaciana");
			// for (double lambda : cn.getRealEigenvalues()){
			// System.out.print(FormatUtils.getInstance().getValue(lambda) +
			// " ");
			// }
			//
			// System.out.println("\nDFT (�mpares)");
			// for (double fft : cn.getFftValuesImpares()){
			// System.out.print(FormatUtils.getInstance().getValue(fft) + " ");
			// }
			//
			// System.out.println("\nDFT (pares)");
			// for (double fft : cn.getFftValuesPares()){
			// System.out.print(FormatUtils.getInstance().getValue(fft) + " ");
			// }
			System.out.println("\n");
			
			System.out.println("Auto");
			for (double v : cn.getRealEigenvalues()) {
				System.out.print(String.format("%.2f ", v));
			}
			System.out.println();
			System.out.println("ODD");
			for (double v : cn.getFftValuesImpares()) {
				System.out.print(String.format("%.2f ", v));
			}
			System.out.println();
			System.out.println("EVEN");
			for (double v : cn.getFftValuesPares()) {
				System.out.print(String.format("%.2f ", v));
			}
			System.out.println();
		}
		generateSW(metrics, nodes, density);
//		generateErdosRenyi(metrics, nodes, density);
		generateBarabasi(metrics, nodes, density);

	}

	private static void generateErdosRenyi(List<TMetric> metrics, int nodes, double density) {
		ErdosRenyiM erdos = new ErdosRenyiM(density, nodes);
		ComplexNetwork cn = new ComplexNetwork(1, erdos.transform(new Integer[nodes][nodes]), RandomNodePositions
				.getInstance().createNodePositions(nodes), TModel.CUSTOM, metrics);

		// while (cn.getMetricValues().get(TMetric.DIAMETER) >
		// cn.getAdjacencyMatrix().length - 1) {
		// cn = new ComplexNetwork(1, erdos.transform(new
		// Integer[nodes][nodes]), RandomNodePositions.getInstance()
		// .createNodePositions(nodes), TModel.CUSTOM, metrics);
		// }

		System.out.println("An�lise da rede de Erdos-Renyi ");

		cn.calculateRealEigenvalues();
		cn.evaluate();
		System.out.print(cn.printMetrics());
		System.out.println("Número de nós: " + cn.getAdjacencyMatrix().length);

		System.out.println("\n");
	}

	private static void generateBarabasi(List<TMetric> metrics, int nodes, double density) {
		BarabasiDensity barabasi = new BarabasiDensity(density, 1.0);
		ComplexNetwork cn = null;
		int n = 30;
		double tle = 0;

		double cc = 0;
		double apl = 0;
		double hd = 0;
		double ent = 0;

		System.out.println("An�lise da rede de Barabasi");

		for (int i = 0; i < n; i++) {
			cn = new ComplexNetwork(2, barabasi.grow(null, nodes), CircularNetwork.getInstance().createNodePositions(
					nodes), TModel.BARABASI_DENSITY, metrics);
			tle += cn.getMetricValues().get(TMetric.DFT_LAPLACIAN_ENTROPY).doubleValue();

			cc += cn.getMetricValues().get(TMetric.CLUSTERING_COEFFICIENT).doubleValue();
			ent += cn.getMetricValues().get(TMetric.ENTROPY).doubleValue();
			apl += cn.getMetricValues().get(TMetric.AVERAGE_PATH_LENGTH).doubleValue();
			hd += cn.getMetricValues().get(TMetric.HUB_DEGREE).doubleValue();
			if (cn.getFirstZeroEvenComponent() == 57) {
				System.out.println("AV");
				for (double v : cn.getRealEigenvalues()) {
					System.out.print(String.format("%.2f ", v));
				}
				System.out.println();
				System.out.println("ODD");
				for (double v : cn.getFftValuesImpares()) {
					System.out.print(String.format("%.2f ", v));
				}
				System.out.println();
				System.out.println("EVEN");
				for (double v : cn.getFftValuesPares()) {
					System.out.print(String.format("%.2f ", v));
				}
				System.out.println();
				break;
			}
		}
		tle /= n;
		cc /= n;
		apl /= n;
		hd /= n;
		ent /= n;

		System.out.println("\n");
		System.out.println("TLE m�dio BA = " + tle);
		System.out.println("CC m�dio BA = " + cc);
		System.out.println("APL m�dio BA = " + apl);
		System.out.println("HD m�dio BA = " + hd);
		System.out.println("ENT m�dio BA = " + ent);
	}

	private static int getK(double density, int n) {
		return (int)Math.round(density * (n - 1));
	}

	private static void generateSW(List<TMetric> metrics, int nodes, double density) {
		WattsStrogatzDensity watts = new WattsStrogatzDensity(0.10, getK(density, nodes), density, true);
		ComplexNetwork cn = null;
		int n = 1;
		double tle = 0;

		double cc = 0;
		double apl = 0;
		double hd = 0;
		double ent = 0;

		System.out.println("An�lise da rede de WS");

		for (int i = 0; i < n; i++) {
			cn = new ComplexNetwork(3, watts.transform(new Integer[nodes][nodes]), CircularNetwork.getInstance()
					.createNodePositions(nodes), TModel.NEWMAN_WATTS, metrics);
			tle += cn.getMetricValues().get(TMetric.DFT_LAPLACIAN_ENTROPY).doubleValue();

			cc += cn.getMetricValues().get(TMetric.CLUSTERING_COEFFICIENT).doubleValue();
			ent += cn.getMetricValues().get(TMetric.ENTROPY).doubleValue();
			apl += cn.getMetricValues().get(TMetric.AVERAGE_PATH_LENGTH).doubleValue();
			hd += cn.getMetricValues().get(TMetric.HUB_DEGREE).doubleValue();
			hd += cn.getMetricValues().get(TMetric.ASSORTATIVITY).doubleValue();
			
			System.out.println("AV");
			for (double v : cn.getRealEigenvalues()) {
				System.out.print(String.format("%.2f ", v));
			}
			System.out.println();
			System.out.println("ODD");
			for (double v : cn.getFftValuesImpares()) {
				System.out.print(String.format("%.2f ", v));
			}
			System.out.println();
			System.out.println("EVEN");
			for (double v : cn.getFftValuesPares()) {
				System.out.print(String.format("%.2f ", v));
			}
			System.out.println();
			
		}
		tle /= n;
		cc /= n;
		apl /= n;
		hd /= n;
		ent /= n;

		System.out.println("\n");
		System.out.println("TLE m�dio WS = " + tle);
		System.out.println("CC m�dio WS = " + cc);
		System.out.println("APL m�dio WS = " + apl);
		System.out.println("HD m�dio WS = " + hd);
		System.out.println("ENT m�dio WS = " + ent);
		System.out.println();
	}
}
