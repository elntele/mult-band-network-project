package br.cns24.experiments;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import br.cns24.MetricHelper;
import br.cns24.TMetric;
import br.cns24.experiments.setup.ApplicationSetup;
import br.cns24.experiments.setup.MetricExperimentSetup;
import br.cns24.experiments.setup.ParameterExperimentSetup;
import br.cns24.models.TModel;
import br.cns24.transformations.DegreeMatrix;
import br.cns24.transformations.Laplacian;
import br.cns24.util.FormatUtils;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

@XmlRootElement
public class ComplexNetwork {
	private int id;

	private String name;

	private HashMap<TMetric, Double> metricValues = new HashMap<TMetric, Double>();

	private Integer[][] adjacencyMatrix;

	private Integer[][] laplacianMatrix;

	private Integer[][] shortestPath;

	private double[][] nodePositions;

	private Double[][] distances;

	private Double[][] completeDistances;

	private TModel model;

	private double[] sequenceDegreeDistribution;

	private Integer[][] degreeMatrix;

	private ParameterExperimentSetup setup;

	private int xSOM;

	private int ySOM;

	private double[] realEigenvalues;

	private double[] fftValues;

	private double[] fftValuesPares;

	private double[] fftValuesImpares;

	private static final TMetric[] metricsTopologicalDistance = new TMetric[] { TMetric.ENTROPY,
			TMetric.CLUSTERING_COEFFICIENT, TMetric.HUB_DEGREE, TMetric.AVERAGE_PATH_LENGTH };

	public ComplexNetwork() {
	}

	public ComplexNetwork(int id, Integer[][] adjacencyMatrix, double[][] nodePositions, TModel model) {
		this.id = id;
		this.adjacencyMatrix = adjacencyMatrix;
		this.nodePositions = new double[adjacencyMatrix.length][2];
		for (int i = 0; i < adjacencyMatrix.length; i++) {
			this.nodePositions[i][0] = nodePositions[i][0];
			this.nodePositions[i][1] = nodePositions[i][1];
		}
		this.model = model;
		for (TMetric metric : TMetric.values()) {
			metricValues.put(metric, MetricHelper.getInstance().calculate(metric, adjacencyMatrix));
		}
		calculateDegreeSequenceDistribution();
	}

	public ComplexNetwork(int id, Integer[][] adjacencyMatrix, double[][] nodePositions, TModel model,
			List<TMetric> metrics) {
		this.id = id;
		this.adjacencyMatrix = adjacencyMatrix;
		this.nodePositions = new double[adjacencyMatrix.length][2];
		for (int i = 0; i < adjacencyMatrix.length; i++) {
			this.nodePositions[i][0] = nodePositions[i][0];
			this.nodePositions[i][1] = nodePositions[i][1];
		}
		this.model = model;
		calculateRealEigenvalues();
		for (TMetric metric : metrics) {
			// System.out.println("Calculando métrica " + metric + "...");
			metricValues.put(metric, MetricHelper.getInstance().calculate(metric, adjacencyMatrix));
		}
	}

	public ComplexNetwork(String name, Integer[][] adjacencyMatrix, double[][] nodePositions, TModel model,
			List<TMetric> metrics) {
		this(0, adjacencyMatrix, nodePositions, model, metrics);
		this.setName(name);
	}

	public ComplexNetwork(int id, Integer[][] adjacencyMatrix, double[][] nodePositions, TModel model, TMetric[] metrics) {
		this.id = id;
		this.adjacencyMatrix = adjacencyMatrix;
		this.nodePositions = new double[adjacencyMatrix.length][2];
		for (int i = 0; i < adjacencyMatrix.length; i++) {
			this.nodePositions[i][0] = nodePositions[i][0];
			this.nodePositions[i][1] = nodePositions[i][1];
		}
		this.model = model;
		calculateRealEigenvalues();
		for (TMetric metric : metrics) {
			metricValues.put(metric, MetricHelper.getInstance().calculate(metric, adjacencyMatrix));
		}
	}

	public ComplexNetwork(int id, Integer[][] adjacencyMatrix, double[][] nodePositions, Double[][] distances,
			TModel model, List<TMetric> metrics) {
		this.id = id;
		this.adjacencyMatrix = adjacencyMatrix;
		this.nodePositions = new double[adjacencyMatrix.length][2];
		for (int i = 0; i < adjacencyMatrix.length; i++) {
			this.nodePositions[i][0] = nodePositions[i][0];
			this.nodePositions[i][1] = nodePositions[i][1];
		}
		this.model = model;
		this.distances = distances;
		for (TMetric metric : metrics) {
			if (metric.isAdjacencyMatrix()) {
				metricValues.put(metric, MetricHelper.getInstance().calculate(metric, adjacencyMatrix));
			} else {
				metricValues.put(metric, MetricHelper.getInstance().calculateDouble(metric, distances));
			}
		}
	}
	
	private boolean isEmpty(Integer[][] adjacencyMatrix){
		for (int i = 0; i < adjacencyMatrix.length; i++) {
			for (int j = 0; j < adjacencyMatrix.length; j++) {
				if (adjacencyMatrix[i][j] != 0) {
					return false;
				}
			}
		}
		return true;
	}

	public ComplexNetwork(int id, Integer[][] adjacencyMatrix, double[][] nodePositions, Double[][] completeDistances, Double[][] distances,
			TModel model, List<TMetric> metrics) {
		this.id = id;
		this.adjacencyMatrix = adjacencyMatrix;
		this.nodePositions = new double[adjacencyMatrix.length][2];
		for (int i = 0; i < adjacencyMatrix.length; i++) {
			this.nodePositions[i][0] = nodePositions[i][0];
			this.nodePositions[i][1] = nodePositions[i][1];
		}
		this.model = model;
		this.distances = distances;
		this.completeDistances = completeDistances;
		for (TMetric metric : metrics) {
			if (isEmpty(adjacencyMatrix)) {
				metricValues.put(metric, 0.0);
			} else {
				if (metric.isAdjacencyMatrix()) {
					metricValues.put(metric, MetricHelper.getInstance().calculate(metric, adjacencyMatrix));
				} else {
					metricValues.put(metric, MetricHelper.getInstance().calculateDoubleComplete(metric, completeDistances, distances));
				}
			}
		}
	}

	public ComplexNetwork(int id, Integer[][] adjacencyMatrix, double[][] nodePositions, TModel model,
			ApplicationSetup setup) {
		this.id = id;
		this.adjacencyMatrix = adjacencyMatrix;
		this.nodePositions = new double[adjacencyMatrix.length][2];
		for (int i = 0; i < adjacencyMatrix.length; i++) {
			this.nodePositions[i][0] = nodePositions[i][0];
			this.nodePositions[i][1] = nodePositions[i][1];
		}
		this.model = model;
		for (MetricExperimentSetup metric : setup.getMetrics()) {
			metricValues.put(metric.getMetric(),
					MetricHelper.getInstance().calculate(metric.getMetric(), adjacencyMatrix));
		}
		calculateDegreeSequenceDistribution();
	}

	public double getTopologicalDistance(ComplexNetwork cn) {
		double td = 0;
		double metricValue1 = 0;
		double metricValue2 = 0;

		for (TMetric metric : metricsTopologicalDistance) {
			metricValue1 = cn.getMetricValues().get(metric);
			metricValue2 = this.getMetricValues().get(metric);
			td += Math.sqrt(((metricValue1 - metricValue2) * (metricValue1 - metricValue2)));
		}
		td /= metricsTopologicalDistance.length;

		return td;
	}

	public String[] getSummary(String status) {
		NumberFormat nf = NumberFormat.getInstance(new Locale("en-EL"));
		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		List<TMetric> metrics = TMetric.getDefaults();
		List<ComplexNetwork> cnsER = new Vector<>();
		List<ComplexNetwork> cnsBA = new Vector<>();
		List<ComplexNetwork> cnsWS = new Vector<>();
		List<ComplexNetwork> cnsRegular = new Vector<>();
		int numRedes = 1;
		boolean match = false;
		int numNodes = adjacencyMatrix.length;
		double density = getMetricValues().get(TMetric.DENSITY).doubleValue();
		appendMetrics(metrics, sb1, nf, status);
//		GenerativeProcedure gp = new ErdosRenyiM(density, numNodes);
//		ComplexNetwork cn = null;
//		int i = 0;
//		while (i < numRedes) {
//			cn = new ComplexNetwork(0, gp.transform(new Integer[numNodes][numNodes]), CircularNetwork
//					.getInstance().createNodePositions(numNodes), TModel.ERDOS_RENYI_N_M, metrics);
//			if (cn.getMetricValues().get(TMetric.ALGEBRAIC_CONNECTIVITY) > 0 || density < 0.026) {
//				cnsER.add(cn);
//				i++;
//				System.out.println(i);
//			}
//		}
//		appendMetrics(metrics, cnsER, sb1, nf, 0, "ER");
//		//
//		// GenerativeProcedure gp = new Toroid(density, numNodes);
//		// for (int i = 0; i < numRedes; i++) {
//		// cnsER.add(new ComplexNetwork(0, gp.transform(new
//		// Integer[numNodes][numNodes]), CircularNetwork
//		// .getInstance().createNodePositions(numNodes), TModel.TOROID,
//		// metrics));
//		// }
//		// appendMetrics(metrics, cnsER, sb1, nf, 0);
//
//		double rp = 0.15;
//		double e = 1.00;
//		double tdAnt = Double.MAX_VALUE;
//		double td = Double.MAX_VALUE;
//		gp = new WattsStrogatzDensity(rp, getK(density, numNodes) - 1, density, true);
//		if (match) {
//			do {
//				tdAnt = td;
//				td = 0;
//				for (i = 0; i < numRedes; i++) {
//					td += getTopologicalDistance(new ComplexNetwork(0, gp.transform(new Integer[numNodes][numNodes]),
//							CircularNetwork.getInstance().createNodePositions(numNodes), TModel.WATTS_STROGATZ, metrics));
//				}
//				td /= numRedes;
//				rp += .005;
//				((WattsStrogatzDensity) gp).setP(rp);
//			} while (td < tdAnt && rp < 0.4);
//			rp -= .005;
//			double tdWS = tdAnt;
//
//			td = Double.MAX_VALUE;
//			tdAnt = Double.MAX_VALUE;
//			gp = new BarabasiDensity(density, e);
//
//			do {
//				tdAnt = td;
//				td = 0;
//				for (i = 0; i < numRedes; i++) {
//					td += getTopologicalDistance(new ComplexNetwork(0, gp.transform(new Integer[numNodes][numNodes]),
//							CircularNetwork.getInstance().createNodePositions(numNodes), TModel.BARABASI, metrics));
//				}
//				td /= numRedes;
//				e -= .05;
//				((BarabasiDensity) gp).setExponent(e);
//			} while (td < tdAnt && e > 0.50);
//			e += .05;
//
//			if (tdWS < td) {
//				e = 1;
//			} else {
//				rp = 0.04;
//			}
//		}
//		int k = getK(density, numNodes);
////		gp = new KRegular(k);
////		for (int i = 0; i < numRedes; i++) {
////			cnsRegular.add(new ComplexNetwork(0, gp.transform(new Integer[numNodes][numNodes]), CircularNetwork
////					.getInstance().createNodePositions(numNodes), TModel.K_REGULAR, metrics));
////		}
////		appendMetrics(metrics, cnsRegular, sb1, nf, rp, k + "-regular");
//
//		gp = new WattsStrogatzDensity(rp, k + 1, density, true);
//		i = 0;
//		while (i < numRedes) {
//			cn = new ComplexNetwork(0, gp.transform(new Integer[numNodes][numNodes]), CircularNetwork
//					.getInstance().createNodePositions(numNodes), TModel.WATTS_STROGATZ, metrics);
//			if (cn.getMetricValues().get(TMetric.ALGEBRAIC_CONNECTIVITY) > 0 || density < 0.026) {
//				cnsWS.add(cn);
//				i++;
//				System.out.println(i);
//			}
//		}
//		appendMetrics(metrics, cnsWS, sb1, nf, rp, "WS");
//
//		gp = new BarabasiDensity(density, e);
//		i = 0;
//		while (i < numRedes) {
//			cn = new ComplexNetwork(0, gp.transform(new Integer[numNodes][numNodes]), CircularNetwork
//					.getInstance().createNodePositions(numNodes), TModel.BARABASI, metrics);
//			if (cn.getMetricValues().get(TMetric.ALGEBRAIC_CONNECTIVITY) > 0 || density < 0.026) {
//				cnsBA.add(cn);
//				i++;
//				System.out.println(i);
//			}
//		}
//		appendMetrics(metrics, cnsBA, sb1, nf, e, "BA");

		appendNDS(metrics, sb2, nf);
//		appendNDS(metrics, cnsER, sb2, nf, 0);
////		appendNDS(metrics, cnsRegular, sb2, nf, k);
//		appendNDS(metrics, cnsWS, sb2, nf, rp);
//		appendNDS(metrics, cnsBA, sb2, nf, e);

		return new String[] { sb1.toString() + "\n", sb2.toString() + "\n" };
	}

	/**
	 * @param metrics
	 * @param gp
	 */
	private void appendMetrics(List<TMetric> metrics, StringBuffer sb, NumberFormat nf, String status) {
		sb.append(name + ";");
		for (TMetric metric : metrics) {
			sb.append(nf.format(getMetricValues().get(metric).doubleValue()) + ";");
		}
//		sb.append("\n");
		sb.append(status);
	}

	/**
	 * @param metrics
	 * @param gp
	 */
	private void appendNDS(List<TMetric> metrics, StringBuffer sb, NumberFormat nf) {
		sb.append(name + ";");
		calculateDegreeSequenceDistribution();
		for (int i = 0; i < getSequenceDegreeDistribution().length; i++) {
			sb.append(nf.format(getSequenceDegreeDistribution()[i]) + ";");
		}
		sb.append("\n");
	}

	private void appendMetrics(List<TMetric> metrics, List<ComplexNetwork> cns, StringBuffer sb, NumberFormat nf,
			double param, String title) {
		double[] metricValues = new double[metrics.size()];
		for (ComplexNetwork cn : cns) {
			for (int i = 0; i < metrics.size(); i++) {
				metricValues[i] += cn.getMetricValues().get(metrics.get(i));
			}
		}
		// if (param != 0) {
		// sb.append(cns.get(0).getModel() + "(" + nf.format(param) + ");");
		// } else {
		// sb.append(cns.get(0).getModel() + ";");
		// }
		sb.append(title + ";");
		for (int i = 0; i < metrics.size(); i++) {
			sb.append(nf.format(metricValues[i] / cns.size()) + ";");
		}
		sb.append("\n");
	}

	private double appendNDS(List<TMetric> metrics, List<ComplexNetwork> cns, StringBuffer sb, NumberFormat nf,
			double param) {
		double e = 0;
		double[] dds = new double[cns.get(0).getAdjacencyMatrix().length];
		for (ComplexNetwork cn : cns) {
			cn.calculateDegreeSequenceDistribution();
			for (int i = 0; i < cn.getSequenceDegreeDistribution().length; i++) {
				dds[i] += cn.getSequenceDegreeDistribution()[i];
			}
		}

		if (param != 0) {
			sb.append(cns.get(0).getModel() + "(" + nf.format(param) + ");");
		} else {
			sb.append(cns.get(0).getModel() + ";");
		}

		for (int i = 0; i < dds.length; i++) {
			sb.append(nf.format(dds[i] / cns.size()) + ";");
			e += (getSequenceDegreeDistribution()[i] - dds[i]) * (getSequenceDegreeDistribution()[i] - dds[i]);
		}
		e /= dds.length;
		sb.append(nf.format(e) + ";");
		sb.append("\n");

		return e;
	}

	private static int getK(double density, int n) {
		int k = Math.max((int) Math.floor((density * (n - 1)) / 2.0) + 1, 1);
		double deltaK = Math.abs(density - (2.0 * k) / (n - 1));
		double deltaKp1 = Math.abs(density - (2.0 * (k + 1)) / (n - 1));
		double deltakm1 = deltaK;
		if (k > 1) {
			deltakm1 = Math.abs(density - (2.0 * (k - 1)) / (n - 1));
		}
		if (deltaKp1 < deltaK) {
			if (deltakm1 < deltaKp1) {
				k = k - 1;
			} else {
				k = k + 1;
			}
		} else if (deltakm1 < deltaK) {
			k = k - 1;
		}

		return k;
	}

	public static void main(String[] args) {
		DoubleFFT_1D fft = null;
		Integer[][] adjacencyMatrix = new Integer[1000][1000];

		for (int i = 0; i < adjacencyMatrix.length; i++) {
			adjacencyMatrix[i][i] = 0;
			for (int j = i + 1; j < adjacencyMatrix.length; j++) {
				if (Math.random() < 0.12) {
					adjacencyMatrix[i][j] = 1;
					adjacencyMatrix[j][i] = 1;
				} else {
					adjacencyMatrix[i][j] = 0;
					adjacencyMatrix[j][i] = 0;
				}
			}
		}

		Integer[][] laplacian = Laplacian.getInstance().transform(adjacencyMatrix);
		double[][] realValues = new double[adjacencyMatrix.length][adjacencyMatrix.length];
		for (int i = 0; i < adjacencyMatrix.length; i++) {
			for (int j = 0; j < adjacencyMatrix[i].length; j++) {
				realValues[i][j] = laplacian[i][j];
			}
		}
		RealMatrix rm = new Array2DRowRealMatrix(realValues);
		double[] realEigenvalues = null;
		double[] fftValues = null;
		try {
			EigenDecomposition solver = new EigenDecomposition(rm, 0);
			realEigenvalues = solver.getRealEigenvalues();
			double aux;
			for (int i = 0; i < realEigenvalues.length; i++) {
				for (int j = i; j < realEigenvalues.length; j++) {
					if (realEigenvalues[j] < realEigenvalues[i]) {
						aux = realEigenvalues[i];
						realEigenvalues[i] = realEigenvalues[j];
						realEigenvalues[j] = aux;
					}
				}
			}
			fft = new DoubleFFT_1D(realValues.length);
			// fftValues = new double[2 * realValues.length];
			fftValues = new double[realValues.length];
			for (int i = 0; i < realValues.length; i++) {
				fftValues[i] = realEigenvalues[i];
			}
			// fft.realForwardFull(fftValues);
			fft.realForward(fftValues);

			double sum = 0;
			double menor = Double.MAX_VALUE;
			double maior = Double.MIN_VALUE;
			for (double d : fftValues) {
				if (d < menor) {
					menor = d;
				}
				if (d > maior) {
					maior = d;
				}
			}
			for (double fftValue : fftValues) {
				fftValue += menor;
				fftValue /= (maior - menor);
				if (fftValue > 0) {
					sum += fftValue * (Math.log10(fftValue) / Math.log10(2));
				}
			}
			System.out.println(-sum);

		} catch (Exception e) {
			System.out.println("Falha ao calcular autovalores da matriz...");
			e.printStackTrace();
			realEigenvalues = new double[adjacencyMatrix.length];
			fftValues = new double[adjacencyMatrix.length];
		}
	}

	public void calculateRealEigenvalues() {
		DoubleFFT_1D fft = null;
		Integer[][] laplacian = Laplacian.getInstance().transform(adjacencyMatrix);
		double[][] realValues = new double[adjacencyMatrix.length][adjacencyMatrix.length];
		for (int i = 0; i < adjacencyMatrix.length; i++) {
			for (int j = 0; j < adjacencyMatrix[i].length; j++) {
				realValues[i][j] = laplacian[i][j];
			}
		}
		RealMatrix rm = new Array2DRowRealMatrix(realValues);
		try {
			EigenDecomposition solver = new EigenDecomposition(rm, 0);
			realEigenvalues = solver.getRealEigenvalues();
			double aux;
			for (int i = 0; i < realEigenvalues.length; i++) {
				for (int j = i; j < realEigenvalues.length; j++) {
					if (realEigenvalues[j] < realEigenvalues[i]) {
						aux = realEigenvalues[i];
						realEigenvalues[i] = realEigenvalues[j];
						realEigenvalues[j] = aux;
					}
				}
			}
			fft = new DoubleFFT_1D(realValues.length);
			// fftValues = new double[2 * realValues.length];
			fftValues = new double[realValues.length];
			fftValuesPares = new double[realValues.length / 2 + 1];
			fftValuesImpares = new double[realValues.length / 2 + 1];
			for (int i = 0; i < realValues.length; i++) {
				fftValues[i] = realEigenvalues[i];
			}
			// fft.realForwardFull(fftValues);
			fft.realForward(fftValues);
			for (int i = 0; i < fftValues.length; i++) {
				if (i % 2 == 0) {
					fftValuesPares[i / 2] = fftValues[i];
				} else {
					fftValuesImpares[i / 2] = fftValues[i];
				}
			}
		} catch (Exception e) {
			System.out.println("Falha ao calcular autovalores da matriz...");
			e.printStackTrace();
			realEigenvalues = new double[adjacencyMatrix.length];
			fftValues = new double[adjacencyMatrix.length];
			fftValuesPares = new double[realValues.length / 2];
			fftValuesImpares = new double[realValues.length / 2];
		}
	}

	public int getFirstZeroEvenComponent() {
		double anterior = fftValuesPares[0];

		for (int i = 0; i < fftValuesPares.length; i++) {
			if (Math.abs(Math.signum(fftValuesPares[i]) + Math.signum(anterior)) != 2) {
				return i;
			}
			anterior = fftValuesPares[i];
		}

		return 0;
	}

	public int getMaxOddComponent() {
		double max = Double.MIN_VALUE;
		int maxIndex = -1;

		for (int i = 0; i < fftValuesImpares.length; i++) {
			if (fftValuesImpares[i] > max) {
				max = fftValuesImpares[i];
				maxIndex = i;
			}
		}

		return maxIndex;
	}

	public void calculateDegreeSequenceDistribution() {
		degreeMatrix = DegreeMatrix.getInstance().transform(adjacencyMatrix);
		double[] sequence = new double[adjacencyMatrix.length-1];

		for (int i = 0; i < degreeMatrix.length; i++) {
			if (degreeMatrix[i][i] > 0) {
				sequence[degreeMatrix[i][i]-1]++;	
			}
		}
		for (int i = 0; i < sequence.length; i++) {
			sequence[i] /= (sequence.length+1);
		}

		this.sequenceDegreeDistribution = sequence;
	}

	public void calculateAbsDegreeSequenceDistribution() {
		degreeMatrix = DegreeMatrix.getInstance().transform(adjacencyMatrix);
		double[] sequence = new double[adjacencyMatrix.length];

		for (int i = 0; i < degreeMatrix.length; i++) {
			sequence[degreeMatrix[i][i]]++;
		}

		this.sequenceDegreeDistribution = sequence;
	}

	@Override
	public String toString() {
		return String.valueOf(id);
	}

	public void evaluate() {
		if (metricValues.isEmpty()) {
			for (TMetric metric : TMetric.getDefaults()) {
				metricValues.put(metric, MetricHelper.getInstance().calculate(metric, adjacencyMatrix));
			}
		}
	}

	public String printMetrics() {
		StringBuilder sb = new StringBuilder();
		if (metricValues.isEmpty()) {
			for (TMetric metric : TMetric.values()) {
				metricValues.put(metric, MetricHelper.getInstance().calculate(metric, adjacencyMatrix));
			}
		}
		for (TMetric metric : metricValues.keySet()) {
			sb.append(metric.getShortDescription()).append(": ")
					.append(FormatUtils.decimalFormat.format(metricValues.get(metric)));
			sb.append('\n');
		}
		return sb.toString();
	}

	public Map<TMetric, Double> getMetricValues() {
		return metricValues;
	}

	@XmlElement
	public void setMetricValues(HashMap<TMetric, Double> metricValues) {
		this.metricValues = metricValues;
	}

	public Integer[][] getAdjacencyMatrix() {
		return adjacencyMatrix;
	}

	@XmlElement
	public void setAdjacencyMatrix(Integer[][] adjacencyMatrix) {
		this.adjacencyMatrix = adjacencyMatrix;
	}

	public double[][] getNodePositions() {
		return nodePositions;
	}

	@XmlElement
	public void setNodePositions(double[][] nodePositions) {
		this.nodePositions = nodePositions;
	}

	public TModel getModel() {
		return model;
	}

	@XmlElement
	public void setModel(TModel model) {
		this.model = model;
	}

	public int getId() {
		return id;
	}

	@XmlElement
	public void setId(int id) {
		this.id = id;
	}

	public double[] getSequenceDegreeDistribution() {
		if (sequenceDegreeDistribution == null) {
			calculateAbsDegreeSequenceDistribution();
		}
		return sequenceDegreeDistribution;
	}

	public void setSequenceDegreeDistribution(double[] sequenceDegreeDistribution) {
		this.sequenceDegreeDistribution = sequenceDegreeDistribution;
	}

	public ParameterExperimentSetup getSetup() {
		return setup;
	}

	public void setSetup(ParameterExperimentSetup setup) {
		this.setup = setup;
	}

	public int getxSOM() {
		return xSOM;
	}

	@XmlElement
	public void setxSOM(int xSOM) {
		this.xSOM = xSOM;
	}

	public int getySOM() {
		return ySOM;
	}

	@XmlElement
	public void setySOM(int ySOM) {
		this.ySOM = ySOM;
	}

	/**
	 * M�todo acessor para obter o valor do atributo realEigenvalues.
	 * 
	 * @return O atributo realEigenvalues
	 */
	public double[] getRealEigenvalues() {
		return realEigenvalues;
	}

	/**
	 * M�todo acessor para alterar o valor do atributo realEigenvalues.
	 * 
	 * @param realEigenvalues
	 *            O valor a ser usado para o atributo realEigenvalues.
	 */
	@XmlTransient
	public void setRealEigenvalues(double[] realEigenvalues) {
		this.realEigenvalues = realEigenvalues;
	}

	/**
	 * M�todo acessor para obter o valor do atributo fftValues.
	 * 
	 * @return O atributo fftValues
	 */
	public double[] getFftValues() {
		return fftValues;
	}

	/**
	 * M�todo acessor para alterar o valor do atributo fftValues.
	 * 
	 * @param fftValues
	 *            O valor a ser usado para o atributo fftValues.
	 */
	@XmlTransient
	public void setFftValues(double[] fftValues) {
		this.fftValues = fftValues;
	}

	/**
	 * M�todo acessor para obter o valor do atributo degreeMatrix.
	 * 
	 * @return O atributo degreeMatrix
	 */
	public Integer[][] getDegreeMatrix() {
		return degreeMatrix;
	}

	/**
	 * M�todo acessor para alterar o valor do atributo degreeMatrix.
	 * 
	 * @param degreeMatrix
	 *            O valor a ser usado para o atributo degreeMatrix.
	 */
	@XmlTransient
	public void setDegreeMatrix(Integer[][] degreeMatrix) {
		this.degreeMatrix = degreeMatrix;
	}

	/**
	 * M�todo acessor para obter o valor do atributo fftValuesImpares.
	 * 
	 * @return O atributo fftValuesImpares
	 */
	public double[] getFftValuesImpares() {
		return fftValuesImpares;
	}

	/**
	 * M�todo acessor para alterar o valor do atributo fftValuesImpares.
	 * 
	 * @param fftValuesImpares
	 *            O valor a ser usado para o atributo fftValuesImpares.
	 */
	@XmlTransient
	public void setFftValuesImpares(double[] fftValuesImpares) {
		this.fftValuesImpares = fftValuesImpares;
	}

	/**
	 * M�todo acessor para obter o valor do atributo fftValuesPares.
	 * 
	 * @return O atributo fftValuesPares
	 */
	public double[] getFftValuesPares() {
		return fftValuesPares;
	}

	/**
	 * M�todo acessor para alterar o valor do atributo fftValuesPares.
	 * 
	 * @param fftValuesPares
	 *            O valor a ser usado para o atributo fftValuesPares.
	 */
	@XmlTransient
	public void setFftValuesPares(double[] fftValuesPares) {
		this.fftValuesPares = fftValuesPares;
	}

	/**
	 * @return o valor do atributo distances
	 */
	public Double[][] getDistances() {
		return distances;
	}

	/**
	 * Altera o valor do atributo distances
	 * 
	 * @param distances
	 *            O valor para setar em distances
	 */
	@XmlTransient
	public void setDistances(Double[][] distances) {
		this.distances = distances;
	}

	/**
	 * @return o valor do atributo laplacianMatrix
	 */
	public Integer[][] getLaplacianMatrix() {
		return laplacianMatrix;
	}

	/**
	 * Altera o valor do atributo laplacianMatrix
	 * 
	 * @param laplacianMatrix
	 *            O valor para setar em laplacianMatrix
	 */
	@XmlTransient
	public void setLaplacianMatrix(Integer[][] laplacianMatrix) {
		this.laplacianMatrix = laplacianMatrix;
	}

	/**
	 * @return o valor do atributo shortestPath
	 */
	public Integer[][] getShortestPath() {
		return shortestPath;
	}

	/**
	 * Altera o valor do atributo shortestPath
	 * 
	 * @param shortestPath
	 *            O valor para setar em shortestPath
	 */
	@XmlTransient
	public void setShortestPath(Integer[][] shortestPath) {
		this.shortestPath = shortestPath;
	}

	/**
	 * @return o valor do atributo name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Altera o valor do atributo name
	 * 
	 * @param name
	 *            O valor para setar em name
	 */
	public void setName(String name) {
		this.name = name;
	}
}
