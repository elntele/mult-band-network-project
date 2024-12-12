package br.bm.core;

import static br.bm.core.SimonUtil.LAMBDA_FIRSTFIT;
import static br.bm.core.SimonUtil.UTILIZAR_DIJ;
import static br.bm.rwa.Funcoes.INF;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import br.bm.model.cost.capex.MultiBandCapexEvaluator;
import br.bm.model.cost.opex.EnergyConsumptionEvaluator;
import br.bm.model.performance.BlockingProbabilityEstimator;
import br.bm.model.robustness.AlgebraicConnectivityEvaluator;
import br.bm.model.robustness.NaturalConnectivityEvaluator;
import br.cns24.Geolocation;
import br.cns24.GravityModel;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;
import br.cns24.services.Bands;
import br.cns24.model.EdgeSet;
import br.cns24.services.Equipments;
import br.cns24.model.GmlData;
import br.cns24.model.GmlEdge;
import br.cns24.model.GmlNode;
import br.cns24.models.TModel;
import br.cns24.persistence.GmlDao;

public class OpticalNetworkMultiBandProblem implements IProblem<Integer, Double> {


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
  // amplifierCostsAndTypes ta pegando valores de  AMPLIFIERS_COSTS_AND_LABELS
  // na linha 843, tem que ver se isso tem que ser corrigido porque tem as caracteristicas
  // do amplificador que são passadas para o objeto Link
  private Vector<Vector<Double>> amplifierCostsAndTypes = new Vector<Vector<Double>>();
  private Vector<Vector<Double>> switchCostsAndTypes = new Vector<Vector<Double>>();
  private Integer setSize;

  public static double[][] AMPLIFIERS_COSTS_AND_LABELS = new double[][]{
      { 0, 0.75, 1.5, 2.25, 0.5, 1, 1.5, 0.25, 0.5, 0.75 }, // cost
      // index
      { 0, 13, 16, 19, 13, 16, 19, 13, 16, 19 }, // amplifier saturation
      // power
      { 0, 5, 5, 5, 7, 7, 7, 9, 9, 9 } // amplifier noise figure
  };

  public static double[][] SWITCHES_COSTS_AND_LABELS = new double[][]{ { 0.25, 0.5, 0.75, 1, 1.5, 2.0 }, // costs
      { 27, 30, 33, 35, 38, 40 } }; // isolation factor in dB
  public List<Integer> switchIndexes;

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

  protected MultiBandCapexEvaluator capexEvaluator;

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

  protected MultiBandNetWorkProfile net;

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
   *     Tempo medio para criptografar usando o AES com chave de 256
   *     bits
   * @param swTime
   *     Tempo que se deseja converter
   * @return Tempo que o algoritmo ira requerer para executar no transponder.
   */
  private static double getCryptScaleFactor(double swTimeAes256, double swTime) {
    double time = (7.5e-6 * swTime) / swTimeAes256;

    return time;
  }

  /*  */

  /**
   * Teste da funcao que converte para os tempos do tramsponder
   *//*
  public static void main(String[] args) {
    //os tres tempos a seguir devem ser os tempos que voc� obt�m em tempo de execu��o
    double tempoAES256 = 1548;
    double tempo3DES = 6720;
    double tempoRC4 = 7290;

    //os tres tempos a seguir sao os tempos que voc� vai lan�ar no banco de dados (x 1e6)
    double tempoAESTransponder = getCryptScaleFactor(tempoAES256, tempoAES256);
    double tempo3DESTransponder = getCryptScaleFactor(tempoAES256, tempo3DES);
    double tempo3RC4Transponder = getCryptScaleFactor(tempoAES256, tempoRC4);

    System.out.printf("Tempo do AES no transponder = %.2f micro segundos\n", tempoAESTransponder * 1e6);
    System.out.printf("Tempo do 3DES no transponder = %.2f micro segundos\n", tempo3DESTransponder * 1e6);
    System.out.printf("Tempo do RC4 no transponder = %.2f micro segundos\n", tempo3RC4Transponder * 1e6);
  }*/
  public static void main(String[] args) {
    var setSize = 3;
    var nodeNumber = 10;
    var numberOfObjectives = 2;
    var roadmPlusW_Part = nodeNumber + 1;
    var solutionSize = (setSize * nodeNumber * (nodeNumber - 1) / 2) + roadmPlusW_Part;
    var matrixConnectionPart = solutionSize - roadmPlusW_Part;
    Integer[] vars = new Integer[solutionSize];
    /**
     * 6  4  2  11  6  12  6  10  5  11
     *
     * file: {0=[1,2,6], 1=[2,5,7], 2=[1,7], 3=[4, 5], 4=[3, 8], 5=[3,8,9], 6=[0,7], 7=[6,9], 8=[4,5], 9=[5,7]}
     *
     *
     *     0     1     2     3     4     5     6     7     8     9
     * 0    x  (107) (330) (000) (000) (000) (033) (000) (000) (000)
     * 1          x  (001) (000) (000) (010) (000) (030) (000) (000)
     * 2                x  (010) (000) (000) (000) (000) (000) (000)
     * 3                      x  (030) (717) (000) (000) (000) (000)
     * 4                            x  (000) (000) (000) (303) (000)
     * 5                                  x  (000) (000) (030) (001)
     * 6                                        x  (077) (000) (000)
     * 7                                              x  (000) (070)
     * 8                                                    x  (000)
     * 9                                                          x
     */

    //var str = "3 0 3 1 1 0 1 0 0 1 0 0 0 1 0 0 0 0 5 5 1 9 22";
    var str = "1 0 7 3 3 0 0 0 0 0 0 0 0 0 0 0 3 3 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 1 0 0 0 0 0 3 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 3 0 7 1 7 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 3 0 3 0 0 0 0 0 0 0 0 0 0 3 0 0 0 1 0 7 7 0 0 0 0 0 0 0 0 0 0 7 0 0 0 0 6 4 2 11 6 12 6 10 5 11 80";
    var arrayStr = str.split(" ");
    for (int i = 0; i < vars.length; i++) {
      vars[i] = Integer.parseInt(arrayStr[i]);
    }
    List<Integer> solutionRawDateAsList = Arrays.asList(vars);
    var upperBounds = new Integer[solutionSize];
    var lowerBounds = new Integer[solutionSize];
    for (int i = 0; i < matrixConnectionPart; i++) {
      lowerBounds[i] = 0;
      upperBounds[i] = 7;
    }

    for (int i = solutionSize - roadmPlusW_Part; i < solutionSize; i++) {
      if (i < solutionSize - 1) {
        lowerBounds[i] = 1;
        upperBounds[i] = 12;
      } else {
        lowerBounds[i] = 10;
        upperBounds[i] = 100;
      }
    }


    var dataToReloadProblem = new DataToReloadProblem(
        solutionSize,
        numberOfObjectives,
        solutionRawDateAsList,
        lowerBounds,
        upperBounds,
        setSize
    );

    String path = "./teste2.gml";
    GmlData gml = null;
    try {
      gml = new GmlDao().loadGmlData(path);
    } catch (Exception e) {
      e.printStackTrace();
    }
    Map<Integer, GmlNode> mapNode = new HashMap<Integer, GmlNode>();
    gml.getNodes()
        .stream()
        .forEach(node -> mapNode.put(node.getId(), node));

    GmlData gmlData = getGmlData(gml.getNodes(), vars, setSize, gml, mapNode);
    OpticalNetworkMultiBandProblem P = new OpticalNetworkMultiBandProblem();
    P.reloadProblemWithMultiBand(100, gmlData, dataToReloadProblem);
    Double[] objectives = P.evaluate(vars);
  }

  /**
   * this is an aid function to main function for test
   *
   * @param nodes
   * @param vars
   */
  private static GmlData getGmlData(List<GmlNode> nodes, Integer[] vars, int setSize, GmlData gml,
      Map<Integer, GmlNode> mapNode) {
    GmlData gmlData = new GmlData();
    gmlData.setNodes(nodes);
    gmlData.setEdgeSets(buildSet(nodes, vars, setSize, gml, mapNode));
    return gmlData;
  }

  private static List<EdgeSet> buildSet(List<GmlNode> nodesList, Integer[] vars, int setSize, GmlData gml,
      Map<Integer, GmlNode> mapNode) {
    //it is used to allows modification inside lambda
    int[] varIndex = { 0 };
    List<Integer> varList = Arrays.asList(vars);

    return IntStream.range(0, nodesList.size()).boxed().flatMap(
            nodeOriginIndex -> IntStream.range(nodeOriginIndex, nodesList.size()).filter(
                    nodeTargetIndex -> nodeOriginIndex != nodeTargetIndex).mapToObj(nodeTargetIndex -> {
                  List<EdgeSet> edgeSets = new ArrayList<>();
                  var edgeSet = new EdgeSet();
                  var fiberOne = varList.get(varIndex[0]);
                  var fiberTwo = varList.get(varIndex[0] + 1);
                  var fiberThree = varList.get(varIndex[0] + 2);

                  if (fiberOne != 0) {
                    edgeSet.getEdges().add(buildEdge(nodeOriginIndex, nodeTargetIndex, fiberOne, gml, mapNode));
                  }
                  if (fiberTwo != 0) {
                    edgeSet.getEdges().add(buildEdge(nodeOriginIndex, nodeTargetIndex, fiberTwo, gml, mapNode));
                  }
                  if (fiberThree != 0) {
                    edgeSet.getEdges().add(buildEdge(nodeOriginIndex, nodeTargetIndex, fiberThree, gml, mapNode));

                  }
                  edgeSets.add(edgeSet);
                  varIndex[0] += setSize;
                  return edgeSets;
                })
                .flatMap(List::stream))
        .collect(Collectors.toList());

  }

  /**
   * * this is an aid function to main function for test
   *
   * @param nodeOriginIndex
   * @param nodeTargetIndex
   * @param fiber
   */
  private static GmlEdge buildEdge(Integer nodeOriginIndex, Integer nodeTargetIndex, Integer fiber, GmlData gml,
      Map<Integer, GmlNode> mapNode) {
    var edge = new GmlEdge();
    edge.setSource(mapNode.get(gml.getNodes()
        .get(nodeOriginIndex)
        .getId()));
    edge.setTarget(mapNode.get(gml.getNodes()
        .get(nodeTargetIndex)
        .getId()));
    edge.setBand(Bands.getBand(fiber));
    return edge;
  }

  public OpticalNetworkMultiBandProblem(int networkLoad, String gmlFile) {
    this.networkLoad = networkLoad;
    bpEstimator = new BlockingProbabilityEstimator(networkLoad);
    // capexEvaluator = new CapexEvaluator();
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

    lowerLimitObjective = new Double[]{ 0.0, 0.0, 0.0, 0.0 };
    upperLimitObjective = new Double[]{ 1.0, 100000.0, 100000000.0, 100.0 };

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

    net = new MultiBandNetWorkProfile(null, nodes, networkLoad, 0.01, 100000, SNR_BLOCK, true, true, true, true, false,
        true,
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

  /**
   * description by jorge: solutions is a list of Integer and
   * variables is a copy of solution as an Array and
   * networkRepresentation_ppr is a copy of variable as a list
   *
   * @param variables
   */
  @Override
  public Double[] evaluate(Integer[] variables) {
    Double[] objectives = new Double[numberOfObjectives+1];// Todo, jorge, gambiarra pra poder pegar a probabilidade algebrica
    //TODO: jorge, after all, investigate if these conversions are really
    // necessary, for this, begin the investigation on evaluate method in
    // ExternalNetworkEvaluateSettings class.
    List<Integer> networkRepresentation_ppr = Arrays.asList(variables);
    Vector<Vector<Double>> adjacencyMatrixLabels;
    //already adapted to set connection concept
    adjacencyMatrixLabels = buildAdjacencyMatrixLabels(networkRepresentation_ppr);
    //already adapted to set connection concept
    Vector<Vector<Double>> adjacencyMatrix = buildAdjacencyMatrixDistances(
        adjacencyMatrixLabels
    );

    int vectorSize_loc = networkRepresentation_ppr.size();
    //me parece que distancias e adjacencyMatrix são exattamente a
    // mesma coisa, investigar depois e ver qual a necessidade
    Double[][] distancias = getDistancias(adjacencyMatrix);


    // Creating the matrix of Links that defines network topology
    var links = new Link[numNodes][numNodes];
    for (int k = 0; k < numNodes; k++)
      links[k] = new Link[numNodes];

    // Creating the matrix that defines the gains in amp on network
    var GainMatrix = new double[numNodes][numNodes];
    for (int k = 0; k < numNodes; k++)
      GainMatrix[k] = new double[numNodes];

    // fulfill gainMatrix with zeros
    for (int k = 0; k < numNodes; k++) {
      for (int w = 0; w < numNodes; w++) {
        GainMatrix[k][w] = 0;
      }
    }

    // fulfill gainMatrix with zeros definitive values
    for (int k = 0; k < numNodes; k++) {
      for (int w = 0; w < numNodes; w++) {
        if (distancias[k][w] != INF) {
          // the mux gain and wss gain is not defined since last project. So, the both gain is given
          // as the last project without optimization and equals  GMUX and GSWITCH constants.
          // therefore, it's already adapted to the concepts od multi band and Set.
          GainMatrix[k][w] = ((distancias[k][w] / 2.0) * 0.2 - GMUX - GSWITCH / 2) * MF;
        }
      }
    }
    // determines the number of wavelength per link

    int NLAMBDAS = networkRepresentation_ppr.get(vectorSize_loc - 1);

    // instancia os enlaces
    //TODO jorge nesse ponto se constroem os links que foi verificado o uso na probabilidade
    // de bloqueio desde o loop que informa o dijkstra na parte onde se calcuna nLambida
    // maximo. É preciso estudar esse loop e vê como considerar os 3 enlaces para calculo
    // do lambida.
    // já mexido:
    // numero de fibra passou de uma para o numero de fibras do set; foi adicionado o parametro
    // lista bands, que é uma lista de bandas que pode conter os valores esperados c, cl e cls
    // de acordo com as fibras do set no cromossomo.
    for (int k = 0; k < numNodes; k++) {
      for (int w = 0; w < numNodes; w++) {
        // reads the amplifier satuarion power and noise figure
        double NF = amplifierCostsAndTypes.get(2).get((int) round(adjacencyMatrixLabels.get(k).get(w)));
        double PSAT = amplifierCostsAndTypes.get(1).get((int) round(adjacencyMatrixLabels.get(k).get(w)));

        if (distancias[k][w] != INF) {
          var indexOfSet = Equipments.getLinkPosition(k, w, numNodes, setSize);
          var numberOdFibers = 0;
          var bandsInEachFiber = new ArrayList<Integer>();
          //Here, the chromosome fiber order lost its sense: if there
          //are f1 and f3, it doesn’t matter, it’s just 2 fibers, and
          //in the first one, there is one of the bands which is in the
          //first position of the list `bandsInEachFiber`, and so on.
          for (int i = 0; i < setSize; i++) {
            if (networkRepresentation_ppr.get(indexOfSet + i) != 0) {
              numberOdFibers += 1;
              bandsInEachFiber.add(networkRepresentation_ppr.get(indexOfSet + i));
            }
          }
          links[k][w] = new Link(k, w, numberOdFibers, NLAMBDAS, GMUX, GainMatrix[k][w], NF, PSAT, distancias[k][w],
              GFIBRA, GainMatrix[k][w], NF, PSAT, ganhodinamico_loc, bandsInEachFiber);
        } else {
          links[k][w] = new Link(k, w, FIBRAS, 0, -4.0, 0.0, 5.0, 16.0, INF, -0.2, 0.0, 5.0, 16.0,
              ganhodinamico_loc, List.of(0));
        }
      }
    }




    /*net.setEpsilon(
        pow(10, -0.1 * switchCostsAndTypes.get(1).get(networkRepresentation_ppr.get(vectorSize_loc - 2))));*/

    // epsilon é a list os isolationFactor. It was a unique value
    // but it was changed because now the wss is of different types
    net.setEpsilon(Equipments.getIsolationFactorEpsilonForThisSwitchList(this.switchIndexes));
    net.setLinks(links);
    net.cleanBp();
    net.setCompleteDistances(distances);
    net.setCn(createMultibandComplexNetworkDistance(variables));
    net.setRawData(variables);

    objectives[0] = bpEstimator.evaluate(net).getValue();
    objectives[1] = capexEvaluator.evaluate(net).getValue();
    //   objectives[2] = energyConsumptionEvaluator.evaluate(net).getValue();
      objectives[2] = algebraicConnectivityEvaluator.evaluate(net).getValue();
    // objectives[4] =
    // naturalConnectivityEvaluator.evaluate(net).getValue();

    // criar a restricao manualmente, colocando todos para o maximo
    // quando a conectividade algebrica e zero
   /* if (objectives[3] < 0.0000001 || objectives[0] > 0.99) {
      for (int i = 0; i < getNumberOfObjectives() - 1; i++) {
        objectives[i] = getUpperLimitObjectiveAt(i);
      }
      objectives[3] = 0.0;
      // objectives[4] = 0.0;
    }*/

    return objectives;
  }

 /* *//**
   * o que esse méthodo basicamente cria aqui é
   *  um objeto ComplexNetwork. Para fazer isso
   * ele cria 3 coisas:
   * 1 - uma lista de Enuns, tipo TMetrics, chamada de metrics
   * onde vai todas as metricas como conectividade algébrica.
   * 2 - uma matrix de adjacências com valores de 0 ou 1 e
   * completa, em que a_ij tem o mesmo valor de a_ji.
   * 3- uma matrix de distâncias também completa onde d_ij
   * o mesmo valor de d_ji
   *//*

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
  }*/

  /**
   * o que esse méthodo basicamente cria aqui é
   *  um objeto ComplexNetwork. Para fazer isso
   * ele cria 3 coisas:
   * 1 - uma lista de Enuns, tipo TMetrics, chamada de metrics
   * onde vai todas as metricas como conectividade algébrica.
   * 2 - uma matrix de adjacências com valores de 0 ou 1 e
   * completa, em que a_ij tem o mesmo valor de a_ji.
   * 3- uma matrix de distâncias também completa onde d_ij
   * o mesmo valor de d_ji
   */

  public ComplexNetwork createMultibandComplexNetworkDistance(Integer[] solution) {
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

    Double[][] customDistances = new Double[numNodes][numNodes];
    for (int i = 0; i < matrix.length; i++) {
      matrix[i][i] = 0;
      customDistances[i][i] = 0.0;
      for (int j = i + 1; j < matrix.length; j++) {
        //TODO, jorge, aqui é onde estou fazendo um parse para interpretar
        // o cromossomo como 'tem conexão ou não', exatamente como era
        // no problema passado, veja que eu não mudo o cromossomo, apenas
        // manipulo a matrix de conexão lendo o cromossomo de 3 em 3 posições.
        // o méthodo antigo esta comentado acima, olhe como era esse trecho.
        // quando for alterar a conectividade algébrica é provável que tenha
        // que alterar aqui de novo.
        var index = Equipments.getLinkPosition(i,j,numNodes,setSize);
        var sum =0;
        for (int w=0;w<setSize;w++){
          sum+= solution[index+w];
        }
        if (sum != 0) {
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

  /**
   * description by jorge:
   * this method fulfill a distance matrix where
   * if nodeI and nodeJ is not connected or nodeI
   * is equals nodeJ distanceMatrix[i][j] = INF,
   * where INF is a constant witch means infinite
   * distance. Otherwise, distanceMatrix[i][j] =
   * real distance. This method don't need adaptations
   * to work with set concept.
   *
   * @param labelMatrix_loc
   */
  private Vector<Vector<Double>> buildAdjacencyMatrixDistances(
      Vector<Vector<Double>> labelMatrix_loc) {
    var adjacencyMatrix_loc = new Vector<Vector<Double>>();

    // fills up adjacencyMatrix_loc with zero
    for (int i = 0; i < numNodes; i++) {
      Vector<Double> temp_loc = new Vector<Double>();
      assign(temp_loc, numNodes, 0.0);
      adjacencyMatrix_loc.add(temp_loc);
    }

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

  /**
   * this function receives an empty Vector<Double>
   * called temp_loc and fulfill with a value in
   * variable val and repeat this until arrive in size
   * indicated in variable tam.
   *
   * @param temp_loc
   * @param tam
   * @param val
   */
  private void assign(Vector<Double> temp_loc, int tam, double val) {
    for (int i = 0; i < tam; i++) {
      temp_loc.add(val);
    }
  }

  /**
   * by jorge:
   * List<Integer> networkRepresentation_ppr is a
   * copy of solution including all parts.
   * This function is already adapted to consider a connection set.
   * Then, considering the setSize the method verify all connection
   * in each connection set in chromosome and decide if a node is
   * connected or not with others. So, this method build and return
   * a connection matrix.
   * this method was inspired in original by Danilo
   *
   * @param networkRepresentation_ppr
   */

  private Vector<Vector<Double>> buildAdjacencyMatrixLabels(List<Integer> networkRepresentation_ppr) {

    int vectorSize_loc = networkRepresentation_ppr.size();
    Vector<Vector<Double>> adjacencyMatrix_loc = new Vector<Vector<Double>>();

    // fills up adjacencyMatrix_loc[nodesSize][nodesSize] with zeros
    IntStream.iterate(0, i -> i < numNodes, i -> i + 1).forEach(i -> {
      Vector<Double> temp_loc = new Vector<Double>();
      assign(temp_loc, numNodes, 0.0);
      adjacencyMatrix_loc.add(temp_loc);
    });

    // this loop run over chromosome walking with step setSize by setSize
    // over connection matrix part. To do it, is used the function
    // Equipments.getLinkPosition() witch returns chromosome's index in
    // connections matrix part.
    for (int i = 0; i < numNodes; i++) {
      for (int j = i + 1; j < numNodes; j++) {
        var index = Equipments.getLinkPosition(i, j, numNodes, setSize);
        var sumLinks = 0;
        for (int set = 0; set < setSize; set++) {
          sumLinks += networkRepresentation_ppr.get(index + set);
        }
        if (sumLinks > 0) {
          adjacencyMatrix_loc.get(i).set(j, 1.0);
          adjacencyMatrix_loc.get(j).set(i, 1.0);
        } else {
          adjacencyMatrix_loc.get(i).set(j, 0.0);
          adjacencyMatrix_loc.get(j).set(i, 0.0);
        }
      }
    }
    return adjacencyMatrix_loc;
  }

/*
  private Vector<Vector<EdgeSet>> buildAdjacencyMatrixLabelsMultiBand(List<Integer> networkRepresentation_ppr) {

    int vectorSize_loc = networkRepresentation_ppr.size();
    Vector<Vector<EdgeSet>> adjacencyMatrix_loc = new Vector<Vector<EdgeSet>>();

    // fills up adjacencyMatrix_loc with zeros
    IntStream.range(0, numNodes).forEach(i -> {
      Vector<EdgeSet> temp_loc = new Vector<EdgeSet>();
      IntStream.range(0, numNodes).forEach(index -> {
        var edgeset = new EdgeSet();
        IntStream.range(0, 3).forEach(indexEdge -> {
          GmlEdge gmlEdge = new GmlEdge();
          gmlEdge.setBand(Bands.NOBAND);
          edgeset.getEdges().add(gmlEdge);
        });
        temp_loc.add(edgeset);
      });
      adjacencyMatrix_loc.add(temp_loc);
    });


    final AtomicIntegerArray i = new AtomicIntegerArray(1);
    final AtomicIntegerArray j = new AtomicIntegerArray(1);
    i.set(0, 0);
    j.set(0, 1);

    IntStream.range(0, (vectorSize_loc - (numNodes + 1))).forEach(k -> {
      var edgeset = new EdgeSet();
      GmlEdge gmlEdgeOne = new GmlEdge();
      GmlEdge gmlEdgeTwo = new GmlEdge();
      GmlEdge gmlEdgeTree = new GmlEdge();
      gmlEdgeOne.setBand(Bands.getBand(networkRepresentation_ppr.get(k)));
      gmlEdgeTwo.setBand(Bands.getBand(networkRepresentation_ppr.get(k + 1)));
      gmlEdgeTree.setBand(Bands.getBand(networkRepresentation_ppr.get(k + 2)));
      edgeset.getEdges().add(gmlEdgeOne);
      edgeset.getEdges().add(gmlEdgeTwo);
      edgeset.getEdges().add(gmlEdgeTree);
      adjacencyMatrix_loc.get(i.get(0)).set(j.get(0), edgeset);
      adjacencyMatrix_loc.get(j.get(0)).set(i.get(0), edgeset);
      j.addAndGet(0, 1);

      if (j.get(0) == numNodes) {
        i.addAndGet(0, 1);
        j.addAndGet(0, i.get(0) + 1);
      }
    });

    return adjacencyMatrix_loc;
  }*/


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


  public void reloadProblemWithMultiBand(int networkLoad, GmlData gmlFile, DataToReloadProblem dataToReloadProblem) {
    this.networkLoad = networkLoad;
    bpEstimator = new BlockingProbabilityEstimator(networkLoad);
    capexEvaluator = new MultiBandCapexEvaluator();
    energyConsumptionEvaluator = new EnergyConsumptionEvaluator();
    algebraicConnectivityEvaluator = new AlgebraicConnectivityEvaluator();
    naturalConnectivityEvaluator = new NaturalConnectivityEvaluator();

    numberOfObjectives = dataToReloadProblem.numberOfObjectives();
    setSize = dataToReloadProblem.setSize();

    data = gmlFile;

    numNodes = data.getNodes().size();

    numberOfVariables = dataToReloadProblem.numberOfVariables();

    this.switchIndexes = dataToReloadProblem.variable().subList(numberOfVariables - (numNodes + 1),
        numberOfVariables - 1);


    geolocationCoords = new double[numNodes][2];
    int count = 0;
    GeolocationConverter gc = new GeolocationConverter();
    for (GmlNode node : data.getNodes()) {
      geolocationCoords[count][0] = gc.mercX(node.getLongitude());
      geolocationCoords[count][1] = gc.mercY(node.getLatitude());
      count++;
    }

    defaultSolution = new Integer[numberOfVariables];
    var variables = dataToReloadProblem.variable();

    variables.toArray(defaultSolution);

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

    lowerLimitObjective = new Double[]{ 0.0, 0.0 };
    upperLimitObjective = new Double[]{ 1.0, 100000.0 };

    upperLimitVariable = dataToReloadProblem.upperBounds().clone();
    lowerLimitVariable = dataToReloadProblem.lowerBounds().clone();


    // build up matrixes with zeros
    // initialize matrix nodePositions_ppr with zeros //aqui
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

    // initialize matrix nodePositions_ppr with the right values // aqui
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


    net = new MultiBandNetWorkProfile(null, nodes, networkLoad, 0.01, 100000, SNR_BLOCK, true, true, true, true, false,
        true,
        10e9, 0.013e-9, 1.0, 0.04e-12 / sqrt(1000), 0.0, 10.0, LAMBDA_FIRSTFIT, UTILIZAR_DIJ, false,
        MAX_NUMBER_OF_WAVELENGHTS);
  net.setSetSize(setSize);
  }

  public OpticalNetworkMultiBandProblem() {
  }
}
