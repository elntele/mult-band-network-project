package br.bm.core;

import static br.bm.core.SimonUtil.LAMBDA_FIRSTFIT;
import static br.bm.core.SimonUtil.UTILIZAR_DIJ;
import static br.bm.rwa.Funcoes.INF;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import br.bm.model.cost.capex.CapexEvaluator;
import br.bm.model.cost.opex.EnergyConsumptionEvaluator;
import br.bm.model.performance.BlockingProbabilityEstimator;
import br.bm.model.robustness.AlgebraicConnectivityEvaluator;
import br.bm.model.robustness.NaturalConnectivityEvaluator;
import br.cns24.Geolocation;
import br.cns24.GravityModel;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;
import br.cns24.model.GmlData;
import br.cns24.model.GmlEdge;
import br.cns24.model.GmlNode;
import br.cns24.models.TModel;
import br.cns24.persistence.GmlDao;

public class OpticalNetworkProblem implements IProblem<Integer, Double> {
	public static final int NUMBER_OF_AMPLIFIER_LABELS = 10;
	public static final int NUMBER_OF_SWITCH_LABELS = 5;
	public static final int MAX_NUMBER_OF_WAVELENGHTS = 40;
	public static final int MIN_NUMBER_OF_WAVELENGHTS = 4;
	private static double GFIBRA = -0.2; // in dB/km

	private static double GMUX = -3.0; // in dB
	private static double GSWITCH = -3.0; // in dB
	private static double MF = 1.0;
	private boolean ganhodinamico_loc = true;

	private Vector<Vector<Double>> nodePositions = new Vector<Vector<Double>>();
	private Vector<Vector<Double>> amplifierCostsAndTypes = new Vector<Vector<Double>>();
	private Vector<Vector<Double>> switchCostsAndTypes = new Vector<Vector<Double>>();

	public static double[][] AMPLIFIERS_COSTS_AND_LABELS = new double[][] {
			{ 0, 0.75, 1.5, 2.25, 0.5, 1, 1.5, 0.25, 0.5, 0.75 }, // cost
			// index
			{ 0, 13, 16, 19, 13, 16, 19, 13, 16, 19 }, // amplifier saturation
			// power
			{ 0, 5, 5, 5, 7, 7, 7, 9, 9, 9 } // amplifier noise figure
	};

	public static double[][] SWITCHES_COSTS_AND_LABELS = new double[][] { { 0.25, 0.5, 0.75, 1, 1.5, 2.0 }, // costs
			{ 27, 30, 33, 35, 38, 40 } }; // isolation factor in dB

	public static final int BLOQ_WAVELENGTH = -1;
	public static final int BLOQ_BER = -2;
	public static final int BLOQ_DISPERSION = -3;

	public static final int FIBRAS = 1;
	public static final double MAX_TIME = 1e5;

	public static final double SNR_BLOCK = 17.0;

	public static final double SNR_THRESHOLD = pow(10, SNR_BLOCK / 10);

	public static final double MEAN_RATE_CALLS_DUR = 0.01;

	public static final double MEAN_RATE_CALLS_DUR_PARETO = 0.01;

	public static final double LARGURA_LINHA = 0.013e-9;

	public static final double TAXA_BITS = 10e9;

	public static final double GAMA = 1;

	protected int numberOfVariables;

	protected int numberOfObjectives;

	protected BlockingProbabilityEstimator bpEstimator;

	protected CapexEvaluator capexEvaluator;

	protected EnergyConsumptionEvaluator energyConsumptionEvaluator;

	protected AlgebraicConnectivityEvaluator algebraicConnectivityEvaluator;

	protected NaturalConnectivityEvaluator naturalConnectivityEvaluator;

	private static final GmlDao dao = new GmlDao();

	protected double networkLoad;

	protected GmlData data;

	protected double[][] geolocationCoords;

	protected Double[][] distances;

	protected int numNodes;

	protected Double[] lowerLimitObjective;

	protected Double[] upperLimitObjective;

	protected Integer[] lowerLimitVariable;

	protected Integer[] upperLimitVariable;

	protected NetworkProfile net;

	protected Double[][] traffic;

	protected Geolocation[] locations;

	protected Integer[] defaultSolution;

	/**
	 * Retorna o tempo estimado para execucao de um algoritmo de
	 * critografia/descriptografia quando ele for embarcado no transponder da
	 * rede optica, considerando o tempo medio que o AES executa em software com
	 * chave de 256 bits e o tempo do algoritmo que se deseja descobrir o
	 * equivalente no transponder.
	 * 
	 * @param swTimeAes256
	 *            Tempo medio para criptografar usando o AES com chave de 256
	 *            bits
	 * @param swTime
	 *            Tempo que se deseja converter
	 * @return Tempo que o algoritmo ira requerer para executar no transponder.
	 */
	private static double getCryptScaleFactor(double swTimeAes256, double swTime) {
		double time = (7.5e-6 * swTime) / swTimeAes256;

		return time;
	}
	
	/**
	 * Teste da funcao que converte para os tempos do tramsponder
	 */
	public static void main(String[] args) {
		//os tres tempos a seguir devem ser os tempos que voc� obt�m em tempo de execu��o
		double tempoAES256 = 1548;
		double tempo3DES = 6720;
		double tempoRC4 = 7290;
		
		//os tres tempos a seguir sao os tempos que voc� vai lan�ar no banco de dados (x 1e6)
		double tempoAESTransponder = getCryptScaleFactor(tempoAES256, tempoAES256);
		double tempo3DESTransponder = getCryptScaleFactor(tempoAES256, tempo3DES);
		double tempo3RC4Transponder = getCryptScaleFactor(tempoAES256, tempoRC4);
		
		System.out.printf("Tempo do AES no transponder = %.2f micro segundos\n", tempoAESTransponder*1e6);
		System.out.printf("Tempo do 3DES no transponder = %.2f micro segundos\n", tempo3DESTransponder*1e6);
		System.out.printf("Tempo do RC4 no transponder = %.2f micro segundos\n", tempo3RC4Transponder*1e6);
	}

	public OpticalNetworkProblem(int networkLoad, String gmlFile) {
		this.networkLoad = networkLoad;
		bpEstimator = new BlockingProbabilityEstimator(networkLoad);
		capexEvaluator = new CapexEvaluator();
		energyConsumptionEvaluator = new EnergyConsumptionEvaluator();
		algebraicConnectivityEvaluator = new AlgebraicConnectivityEvaluator();
		naturalConnectivityEvaluator = new NaturalConnectivityEvaluator();

		numberOfObjectives = 4;

		data = dao.loadGmlData(gmlFile);

		numNodes = data.getNodes().size();

		numberOfVariables = numNodes * (numNodes - 1) / 2 + 2;

		geolocationCoords = new double[numNodes][2];
		int count = 0;
		GeolocationConverter gc = new GeolocationConverter();
		for (GmlNode node : data.getNodes()) {
			geolocationCoords[count][0] = gc.mercX(node.getLongitude());
			geolocationCoords[count][1] = gc.mercY(node.getLatitude());
			count++;
		}

		defaultSolution = new Integer[numberOfVariables];
		for (int i = 0; i < numberOfVariables; i++) {
			defaultSolution[i] = 0;
		}
		for (GmlEdge edge : data.getEdges()) {
			count = 0;
			for (int i = 0; i < numNodes; i++) {
				for (int j = i + 1; j < numNodes; j++) {
					if ((edge.getSource().getId() == i && edge.getTarget().getId() == j)
							|| (edge.getSource().getId() == j && edge.getTarget().getId() == i)) {
						defaultSolution[count] = 1;
					}
					count++;
				}
			}
		}
		defaultSolution[numberOfVariables - 1] = 40;
		defaultSolution[numberOfVariables - 2] = 4;

		locations = new Geolocation[numNodes];
		for (int i = 0; i < locations.length; i++) {
			locations[i] = new Geolocation(data.getNodes().get(i).getLatitude(), data.getNodes().get(i).getLongitude());
		}
		traffic = createGravityTraffic(data);
		distances = new Double[numNodes][numNodes];
		for (int i = 0; i < numNodes; i++) {
			distances[i][i] = 0.0;
			for (int j = i + 1; j < numNodes; j++) {
				distances[i][j] = computeDistance(locations[i], locations[j], true);
				distances[j][i] = distances[i][j];
			}
		}

		lowerLimitObjective = new Double[] { 0.0, 0.0, 0.0, 0.0 };
		upperLimitObjective = new Double[] { 1.0, 100000.0, 100000000.0, 100.0 };

		upperLimitVariable = new Integer[(numNodes * (numNodes - 1)) / 2 + 2];
		lowerLimitVariable = new Integer[(numNodes * (numNodes - 1)) / 2 + 2];

		for (int i = 0; i < numberOfVariables; i++) {
			upperLimitVariable[i] = 1;
			lowerLimitVariable[i] = 0;
		}
		upperLimitVariable[numberOfVariables - 1] = 40;
		lowerLimitVariable[numberOfVariables - 1] = 4;
		upperLimitVariable[numberOfVariables - 2] = 5;
		lowerLimitVariable[numberOfVariables - 2] = 0;

		// build up matrixes with zeros
		// initialize matrix nodePositions_ppr with zeros
		for (int i = 0; i < numNodes; i++) {
			Vector<Double> temp_loc = new Vector<Double>();
			for (int j = 0; j < 2; j++) {
				temp_loc.add(0.0);
			}
			nodePositions.add(temp_loc);
		}

		// initialize matrix amplifierCostsAndTypes_ppr with zeros
		for (int i = 0; i < 3; i++) {
			Vector<Double> temp_loc = new Vector<Double>();
			for (int j = 0; j < NUMBER_OF_AMPLIFIER_LABELS; j++) {
				temp_loc.add(0.0);
			}
			amplifierCostsAndTypes.add(temp_loc);
		}

		// initialize matrix switchCostsAndTypes_ppr with zeros
		for (int i = 0; i < 2; i++) {
			Vector<Double> temp_loc = new Vector<Double>();
			for (int j = 0; j < NUMBER_OF_SWITCH_LABELS + 1; j++) {
				temp_loc.add(0.0);
			}
			switchCostsAndTypes.add(temp_loc);
		}
		// build up matrixes with correct values

		// initialize matrix nodePositions_ppr with the right values
		for (int i = 0; i < nodePositions.size(); i++)
			for (int j = 0; j < nodePositions.get(i).size(); j++)
				nodePositions.get(i).set(j, geolocationCoords[i][j]);

		// initialize matrix amplifierCostsAndTypes_ppr with the right values
		for (int i = 0; i < amplifierCostsAndTypes.size(); i++)
			for (int j = 0; j < amplifierCostsAndTypes.get(i).size(); j++)
				amplifierCostsAndTypes.get(i).set(j, AMPLIFIERS_COSTS_AND_LABELS[i][j]);

		// initialize matrix aswitchCostsAndTypes_ppr with the right values
		for (int i = 0; i < switchCostsAndTypes.size(); i++)
			for (int j = 0; j < switchCostsAndTypes.get(i).size(); j++)
				switchCostsAndTypes.get(i).set(j, SWITCHES_COSTS_AND_LABELS[i][j]);

		Vector<Node> nodes = new Vector<Node>();
		// network nodes
		double GSWITCH = -3.0; // in dB
		double SNR = 40.0; // in dB
		double LPOWER = 0.0; // in DBm
		for (int k = 0; k < numNodes; k++) {
			Node newNode = new Node(GSWITCH, LPOWER, SNR);
			nodes.add(newNode);
		}

		net = new NetworkProfile(null, nodes, networkLoad, 0.01, 100000, SNR_BLOCK, true, true, true, true, false, true,
				10e9, 0.013e-9, 1.0, 0.04e-12 / sqrt(1000), 0.0, 10.0, LAMBDA_FIRSTFIT, UTILIZAR_DIJ, false,
				MAX_NUMBER_OF_WAVELENGHTS);
	}// aqui

	private static Double[][] createGravityTraffic(GmlData data) {
		GravityModel gm = new GravityModel(data);
		Double[][] traffic = gm.getTrafficMatrix();

		CallSchedulerNonUniformHub.TRAFFICMATRIX = traffic;
		return traffic;
	}

	protected double computeDistance(Geolocation startCoords, Geolocation destCoords, boolean convertToRadians) {
		if (convertToRadians) {
			return computeDistance(
					new Geolocation(degreesToRadians(startCoords.getLatitude()),
							degreesToRadians(startCoords.getLongitude())),
					new Geolocation(degreesToRadians(destCoords.getLatitude()),
							degreesToRadians(destCoords.getLongitude())));
		}
		return computeDistance(startCoords, destCoords);
	}

	protected double computeDistance(Geolocation startCoords, Geolocation destCoords) {
		double startLatRads = startCoords.getLatitude();
		double startLongRads = startCoords.getLongitude();
		double destLatRads = destCoords.getLatitude();
		double destLongRads = destCoords.getLongitude();
		double radius = 6371; // raio da Terra em km
		double distance = Math
				.acos(Math.sin(startLatRads) * Math.sin(destLatRads)
						+ Math.cos(startLatRads) * Math.cos(destLatRads) * Math.cos(startLongRads - destLongRads))
				* radius;
		return distance;
	}

	protected double degreesToRadians(double degrees) {
		return (degrees * Math.PI) / 180;
	}

	@Override
	public Double[] evaluate(Integer[] variables) {
		Double[] objectives = new Double[numberOfObjectives];

		List<Integer> networkRepresentation_ppr = Arrays.asList(variables);
		Vector<Vector<Double>> adjacencyMatrixLabels;

		adjacencyMatrixLabels = buildAdjacencyMatrixLabels(networkRepresentation_ppr);

		Vector<Vector<Double>> adjacencyMatrix = buildAdjacencyMatrixDistances(networkRepresentation_ppr,
				adjacencyMatrixLabels);

		int vectorSize_loc = networkRepresentation_ppr.size();
		Double[][] distancias = getDistancias(adjacencyMatrix);
		Link[][] links;
		double[][] matrizGanho;

		// Creating the matrix that defines network topology
		links = new Link[numNodes][numNodes];
		for (int k = 0; k < numNodes; k++)
			links[k] = new Link[numNodes];

		// Creating the matrix that defines the gains in amp on network
		matrizGanho = new double[numNodes][numNodes];
		for (int k = 0; k < numNodes; k++)
			matrizGanho[k] = new double[numNodes];

		// inicia matriz de ganhos com zeros
		for (int k = 0; k < numNodes; k++)
			for (int w = 0; w < numNodes; w++)
				matrizGanho[k][w] = 0;

		// inicia matriz de ganhos com valor de ganhos corretos
		for (int k = 0; k < numNodes; k++)
			for (int w = 0; w < numNodes; w++)
				if (distancias[k][w] != INF)
					matrizGanho[k][w] = ((distancias[k][w] / 2.0) * 0.2 - GMUX - GSWITCH / 2) * MF;

		// determines the number of wavelength per link

		int NLAMBDAS = networkRepresentation_ppr.get(vectorSize_loc - 1);

		// instancia os enlaces

		for (int k = 0; k < numNodes; k++)
			for (int w = 0; w < numNodes; w++) {
				// reads the amplifier satuarion power and noise figure
				double NF = amplifierCostsAndTypes.get(2).get((int) round(adjacencyMatrixLabels.get(k).get(w)));
				double PSAT = amplifierCostsAndTypes.get(1).get((int) round(adjacencyMatrixLabels.get(k).get(w)));

				if (distancias[k][w] != INF)
					links[k][w] = new Link(k, w, FIBRAS, NLAMBDAS, GMUX, matrizGanho[k][w], NF, PSAT, distancias[k][w],
							GFIBRA, matrizGanho[k][w], NF, PSAT, ganhodinamico_loc);
				else
					links[k][w] = new Link(k, w, FIBRAS, 0, -4.0, 0.0, 5.0, 16.0, INF, -0.2, 0.0, 5.0, 16.0,
							ganhodinamico_loc);
			}

		net.setEpsilon(
				pow(10, -0.1 * switchCostsAndTypes.get(1).get(networkRepresentation_ppr.get(vectorSize_loc - 2))));
		net.setLinks(links);
		net.cleanBp();
		net.setCompleteDistances(distances);
		net.setCn(createComplexNetworkDistance(variables));
		net.setRawData(variables);

		objectives[0] = bpEstimator.evaluate(net).getValue();
		objectives[1] = capexEvaluator.evaluate(net).getValue();
		objectives[2] = energyConsumptionEvaluator.evaluate(net).getValue();
		objectives[3] = algebraicConnectivityEvaluator.evaluate(net).getValue();
		// objectives[4] =
		// naturalConnectivityEvaluator.evaluate(net).getValue();

		// criar a restricao manualmente, colocando todos para o maximo
		// quando a conectividade algebrica e zero
		if (objectives[3] < 0.0000001 || objectives[0] > 0.99) {
			for (int i = 0; i < getNumberOfObjectives() - 1; i++) {
				objectives[i] = getUpperLimitObjectiveAt(i);
			}
			objectives[3] = 0.0;
			// objectives[4] = 0.0;
		}

		return objectives;
	}

	public ComplexNetwork createComplexNetworkDistance(Integer[] solution) {
		List<TMetric> metrics = new ArrayList<TMetric>();
		metrics.add(TMetric.NATURAL_CONNECTIVITY);
		metrics.add(TMetric.ALGEBRAIC_CONNECTIVITY);
		metrics.add(TMetric.DENSITY);
		metrics.add(TMetric.AVERAGE_DEGREE);
		metrics.add(TMetric.AVERAGE_PATH_LENGTH);
		metrics.add(TMetric.PHYSICAL_AVERAGE_PATH_LENGTH);
		metrics.add(TMetric.CLUSTERING_COEFFICIENT);
		metrics.add(TMetric.DIAMETER);
		metrics.add(TMetric.PHYSICAL_DIAMETER);
		metrics.add(TMetric.ENTROPY);
		metrics.add(TMetric.DFT_LAPLACIAN_ENTROPY);
		metrics.add(TMetric.PHYSICAL_DFT_LAPLACIAN_ENTROPY);
		metrics.add(TMetric.SPECTRAL_RADIUS);
		metrics.add(TMetric.MAXIMUM_CLOSENESS);
		metrics.add(TMetric.PHYSICAL_DENSITY);

		Integer[][] matrix = new Integer[numNodes][numNodes];
		double value = 0;
		int counter = 0;
		Double[][] customDistances = new Double[numNodes][numNodes];
		for (int i = 0; i < matrix.length; i++) {
			matrix[i][i] = 0;
			customDistances[i][i] = 0.0;
			for (int j = i + 1; j < matrix.length; j++) {
				value = Integer.valueOf(solution[counter]);
				if (value != 0) {
					matrix[i][j] = 1;
					matrix[j][i] = 1;
					customDistances[i][j] = distances[i][j];
					customDistances[j][i] = distances[j][i];
				} else {
					matrix[i][j] = 0;
					matrix[j][i] = 0;
					customDistances[i][j] = 0.0;
					customDistances[j][i] = 0.0;
				}
				counter++;
			}
		}
		return new ComplexNetwork(0, matrix, new double[numNodes][numNodes], distances, customDistances, TModel.CUSTOM,
				metrics);
	}

	private Double[][] getDistancias(Vector<Vector<Double>> adjacencyMatrixDistances) {
		Double[][] distancias;

		// creates the adjacency distance matrix
		distancias = new Double[numNodes][numNodes];
		for (int k = 0; k < numNodes; k++)
			distancias[k] = new Double[numNodes];
		for (int k = 0; k < numNodes; k++)
			for (int w = 0; w < numNodes; w++)
				distancias[k][w] = adjacencyMatrixDistances.get(k).get(w);
		return distancias;
	}

	private Vector<Vector<Double>> buildAdjacencyMatrixDistances(List<Integer> solution,
			Vector<Vector<Double>> labelMatrix_loc) {
		Vector<Vector<Double>> adjacencyMatrix_loc = new Vector<Vector<Double>>();

		// fills up adjacencyMatrix_loc with zeros
		for (int i = 0; i < numNodes; i++) {
			Vector<Double> temp_loc = new Vector<Double>();
			assign(temp_loc, numNodes, 0.0);
			adjacencyMatrix_loc.add(temp_loc);
		}
		// melhorar este algoritmo dps da para fazer o segundo for comecando
		// apenas do i atual
		for (int i = 0; i < numNodes; i++)
			for (int j = 0; j < numNodes; j++) {
				if ((i == j) || (labelMatrix_loc.get(i).get(j) == 0))
					adjacencyMatrix_loc.get(i).set(j, INF);
				else {
					adjacencyMatrix_loc.get(i).set(j, getDistances()[i][j]);
				}
			}

		return adjacencyMatrix_loc;
	}

	private void assign(Vector<Double> temp_loc, int tam, double val) {
		for (int i = 0; i < tam; i++) {
			temp_loc.add(val);
		}
	}

	private Vector<Vector<Double>> buildAdjacencyMatrixLabels(List<Integer> networkRepresentation_ppr) {

		int vectorSize_loc = networkRepresentation_ppr.size();
		Vector<Vector<Double>> adjacencyMatrix_loc = new Vector<Vector<Double>>();

		// fills up adjacencyMatrix_loc with zeros
		for (int i = 0; i < numNodes; i++) {
			Vector<Double> temp_loc = new Vector<Double>();
			assign(temp_loc, numNodes, 0.0);
			adjacencyMatrix_loc.add(temp_loc);
		}
		int i = 0;
		int j = 1;

		for (int k = 0; (k < (vectorSize_loc - 2)); k++) {
			adjacencyMatrix_loc.get(i).set(j, (double) networkRepresentation_ppr.get(k));
			adjacencyMatrix_loc.get(j).set(i, (double) networkRepresentation_ppr.get(k));
			j++;
			if (j == numNodes) {
				i++;
				j = i + 1;
			}

		}
		return adjacencyMatrix_loc;
	}

	@Override
	public int getNumberOfObjectives() {
		return numberOfObjectives;
	}

	@Override
	public int getNumberOfVariables() {
		return numberOfVariables;
	}

	@Override
	public Double getLowerLimitObjectiveAt(int index) {
		return lowerLimitObjective[index];
	}

	@Override
	public Integer getLowerLimitVariableAt(int index) {
		return lowerLimitVariable[index];
	}

	@Override
	public Double getUpperLimitObjectiveAt(int index) {
		return upperLimitObjective[index];
	}

	@Override
	public Integer getUpperLimitVariableAt(int index) {
		return lowerLimitVariable[index];
	}

	public Double[][] getDistances() {
		return distances;
	}

	public void setDistances(Double[][] distances) {
		this.distances = distances;
	}

	public Double[][] getTraffic() {
		return traffic;
	}

	public void setTraffic(Double[][] traffic) {
		this.traffic = traffic;
	}

	public double[][] getGeolocationCoords() {
		return geolocationCoords;
	}

	public void setGeolocationCoords(double[][] geolocationCoords) {
		this.geolocationCoords = geolocationCoords;
	}

	public Geolocation[] getLocations() {
		return locations;
	}

	public void setLocations(Geolocation[] locations) {
		this.locations = locations;
	}

	public Integer[] getDefaultSolution() {
		return defaultSolution;
	}

	public void setDefaultSolution(Integer[] defaultSolution) {
		this.defaultSolution = defaultSolution;
	}
	public void reloadProblem(int networkLoad, GmlData gmlFile) {
		this.networkLoad = networkLoad;
		bpEstimator = new BlockingProbabilityEstimator(networkLoad);
		capexEvaluator = new CapexEvaluator();
		energyConsumptionEvaluator = new EnergyConsumptionEvaluator();
		algebraicConnectivityEvaluator = new AlgebraicConnectivityEvaluator();
		naturalConnectivityEvaluator = new NaturalConnectivityEvaluator();

		numberOfObjectives = 4;

		data = gmlFile;

		numNodes = data.getNodes().size();

		numberOfVariables = numNodes * (numNodes - 1) / 2 + 2;

		geolocationCoords = new double[numNodes][2];
		int count = 0;
		GeolocationConverter gc = new GeolocationConverter();
		for (GmlNode node : data.getNodes()) {
			geolocationCoords[count][0] = gc.mercX(node.getLongitude());
			geolocationCoords[count][1] = gc.mercY(node.getLatitude());
			count++;
		}

		defaultSolution = new Integer[numberOfVariables];
		for (int i = 0; i < numberOfVariables; i++) {
			defaultSolution[i] = 0;
		}
		for (GmlEdge edge : data.getEdges()) {
			count = 0;
			for (int i = 0; i < numNodes; i++) {
				for (int j = i + 1; j < numNodes; j++) {
					if ((edge.getSource().getId() == i && edge.getTarget().getId() == j)
							|| (edge.getSource().getId() == j && edge.getTarget().getId() == i)) {
						defaultSolution[count] = 1;
					}
					count++;
				}
			}
		}
		defaultSolution[numberOfVariables - 1] = 40;
		defaultSolution[numberOfVariables - 2] = 4;

		locations = new Geolocation[numNodes];
		for (int i = 0; i < locations.length; i++) {
			locations[i] = new Geolocation(data.getNodes().get(i).getLatitude(), data.getNodes().get(i).getLongitude());
		}
		traffic = createGravityTraffic(data);
		distances = new Double[numNodes][numNodes];
		for (int i = 0; i < numNodes; i++) {
			distances[i][i] = 0.0;
			for (int j = i + 1; j < numNodes; j++) {
				distances[i][j] = computeDistance(locations[i], locations[j], true);
				distances[j][i] = distances[i][j];
			}
		}

		lowerLimitObjective = new Double[] { 0.0, 0.0, 0.0, 0.0 };
		upperLimitObjective = new Double[] { 1.0, 100000.0, 100000000.0, 100.0 };

		upperLimitVariable = new Integer[(numNodes * (numNodes - 1)) / 2 + 2];
		lowerLimitVariable = new Integer[(numNodes * (numNodes - 1)) / 2 + 2];

		for (int i = 0; i < numberOfVariables; i++) {
			upperLimitVariable[i] = 1;
			lowerLimitVariable[i] = 0;
		}
		upperLimitVariable[numberOfVariables - 1] = 40;
		lowerLimitVariable[numberOfVariables - 1] = 4;
		upperLimitVariable[numberOfVariables - 2] = 5;
		lowerLimitVariable[numberOfVariables - 2] = 0;

		// build up matrixes with zeros
		// initialize matrix nodePositions_ppr with zeros
		for (int i = 0; i < numNodes; i++) {
			Vector<Double> temp_loc = new Vector<Double>();
			for (int j = 0; j < 2; j++) {
				temp_loc.add(0.0);
			}
			nodePositions.add(temp_loc);
		}

		// initialize matrix amplifierCostsAndTypes_ppr with zeros
		for (int i = 0; i < 3; i++) {
			Vector<Double> temp_loc = new Vector<Double>();
			for (int j = 0; j < NUMBER_OF_AMPLIFIER_LABELS; j++) {
				temp_loc.add(0.0);
			}
			amplifierCostsAndTypes.add(temp_loc);
		}

		// initialize matrix switchCostsAndTypes_ppr with zeros
		for (int i = 0; i < 2; i++) {
			Vector<Double> temp_loc = new Vector<Double>();
			for (int j = 0; j < NUMBER_OF_SWITCH_LABELS + 1; j++) {
				temp_loc.add(0.0);
			}
			switchCostsAndTypes.add(temp_loc);
		}
		// build up matrixes with correct values

		// initialize matrix nodePositions_ppr with the right values
		for (int i = 0; i < nodePositions.size(); i++)
			for (int j = 0; j < nodePositions.get(i).size(); j++)
				nodePositions.get(i).set(j, geolocationCoords[i][j]);

		// initialize matrix amplifierCostsAndTypes_ppr with the right values
		for (int i = 0; i < amplifierCostsAndTypes.size(); i++)
			for (int j = 0; j < amplifierCostsAndTypes.get(i).size(); j++)
				amplifierCostsAndTypes.get(i).set(j, AMPLIFIERS_COSTS_AND_LABELS[i][j]);

		// initialize matrix aswitchCostsAndTypes_ppr with the right values
		for (int i = 0; i < switchCostsAndTypes.size(); i++)
			for (int j = 0; j < switchCostsAndTypes.get(i).size(); j++)
				switchCostsAndTypes.get(i).set(j, SWITCHES_COSTS_AND_LABELS[i][j]);

		Vector<Node> nodes = new Vector<Node>();
		// network nodes
		double GSWITCH = -3.0; // in dB
		double SNR = 40.0; // in dB
		double LPOWER = 0.0; // in DBm
		for (int k = 0; k < numNodes; k++) {
			Node newNode = new Node(GSWITCH, LPOWER, SNR);
			nodes.add(newNode);
		}

		net = new NetworkProfile(null, nodes, networkLoad, 0.01, 100000, SNR_BLOCK, true, true, true, true, false, true,
				10e9, 0.013e-9, 1.0, 0.04e-12 / sqrt(1000), 0.0, 10.0, LAMBDA_FIRSTFIT, UTILIZAR_DIJ, false,
				MAX_NUMBER_OF_WAVELENGHTS);
	}

	public OpticalNetworkProblem() {
		super();
	}
	
	
	
}
