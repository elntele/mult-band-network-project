package br.mnc;

import static br.bm.core.OpticalNetworkProblem.MAX_NUMBER_OF_WAVELENGHTS;
import static br.bm.core.SimonUtil.LAMBDA_FIRSTFIT;
import static br.bm.core.SimonUtil.UTILIZAR_DIJ;
import static java.lang.Math.sqrt;
import static org.uma.jmetal.util.AbstractAlgorithmRunner.printFinalSolutionSet;

import br.bm.core.DataToReloadProblem;
import br.bm.core.MultiBandNetWorkProfile;
import br.bm.core.Node;
import br.bm.core.OpticalNetworkMultiBandProblem;
import br.bm.model.robustness.AlgebraicConnectivityEvaluator;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;
import br.cns24.models.TModel;
import br.cns24.services.AllowedConnectionTable;
import br.cns24.services.Bands;
import br.cns24.model.EdgeSet;
import br.cns24.model.GmlData;
import br.cns24.model.GmlEdge;
import br.cns24.model.GmlNode;
import br.cns24.persistence.GmlDao;
import br.cns24.services.Equipments;
import br.cns24.services.LevelNode;
import br.cns24.services.PrintPopulation;

import org.uma.jmetal.problem.integerproblem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExternalNetworkEvaluatorSettings extends AbstractIntegerProblem {

  private static final long serialVersionUID = 1L;
  private GmlData gml;
  private Map<Integer, GmlNode> mapNode;
  public int contEvaluate = 0;
  private Integer[] upperBounds;
  private Integer[] lowerBounds;
  private Integer setSize;
  private int populationSize = 0;
  private List<DefaultIntegerSolution> localPopulation = new ArrayList<>();
  private String path;
  private String varAndFunPath;
  private int iterationsToPrint;
  private int execution;
  private final Map<Integer, ConstrainsMetrics> constraintsStatistics = new HashMap();
  private int load;
  private Double maxCapex;
  private boolean buildMaxCapex;
  private ArrayList<Integer> nodesWithRestriction;
  private final Map<Integer, ArrayList<Integer>> solutionsXNodeConstraint = new HashMap();

  @Override
  public IntegerSolution createSolution() {
    IntegerSolution integerSolution = new DefaultIntegerSolution(variableBounds(), numberOfObjectives(),
        numberOfConstraints());
    createRandomNetworkWithNodeNeighborhoodInformation(((DefaultIntegerSolution) integerSolution));
    PrintPopulation.printMatrix(integerSolution.variables(), gml.getNodes().size(), "none", "none",
        ((DefaultIntegerSolution) integerSolution).file, setSize);
    return integerSolution;
  }


  void createRandomNetworkWithNodeNeighborhoodInformation(IntegerSolution solution) {
    fullFillFile((DefaultIntegerSolution) solution);
    Random random = new Random();
    var possibleConnection = AllowedConnectionTable.getPossibleConnection(3, setSize);
    for (int i = 0; i < gml.getNodes().size(); i++) {
      for (int j = i + 1; j < gml.getNodes().size(); j++) {
        var index = Equipments.getLinkPosition(i, j, gml.getNodes().size(), setSize);
        var result = random.nextInt(100);

        if (result <= 80) {
          for (int w = 0; w < setSize; w++) {
            solution.variables()
                .set(index + w, 0);
          }

        } else {
          // in 20% of times randomly chosen the one type of connection: 0,1,3,5,7.
          // remembering that 0 means no connection and consequently no edge/fiber
          //feed file attribute of neighborhood
          for (int w = 0; w < setSize; w++) {
            var edge = possibleConnection[random.nextInt(possibleConnection.length)];
            solution.variables().set(index + w, edge);
            if (edge != 0) {
              ((DefaultIntegerSolution) solution).file.get(i).add(j);
              ((DefaultIntegerSolution) solution).file.get(j).add(i);
            }
          }
        }
      }
    }
  }


  private void fullFillFile(DefaultIntegerSolution solution) {
    for (int i = 0; i < gml.getNodes().size(); i++) {
      var set = new HashSet<Integer>();
      solution.file.put(i, set);
    }
  }


  /**
   * method used for jMetal algorithm to evaluate
   * solutions
   *
   * @param solution
   */
  @Override
  public IntegerSolution evaluate(IntegerSolution solution) {
    evaluateConstraints((DefaultIntegerSolution) solution);
    var numberOfNodes = gml.getNodes().size();
    var sizeThreeAlelos = 3 * numberOfNodes * (numberOfNodes - 1) / 2 + numberOfNodes + 1;
    var connectionsSize = solution.variables().size() - (numberOfNodes + 1);
    Integer[] vars = new Integer[sizeThreeAlelos];
    var count =0;
    for (int i = 0; i < solution.variables().size(); i++) {
      if (i< connectionsSize){
        var aleloList= Bands.getThreeAleloValue((Integer) solution.variables()
            .get(i));
        for (int w=0;w<aleloList.size(); w++){
          vars[(i*3)+w]=aleloList.get(w);
        }

      }else{
        vars[(connectionsSize*3)+count] = (Integer) solution.variables()
            .get(i);
        count+=1;
      }
    }


    System.out.println("conte Evaluate: " + this.contEvaluate);
    this.contEvaluate += 1;
    GmlData gmlData = getGmlData(gml.getNodes(), vars);
    if (solution.constraints()[0] == 1) {
      solution.objectives()[0] = 1.0;
      solution.objectives()[1] = 3;
    } else if (solution.constraints()[1] > 0) {
      OpticalNetworkMultiBandProblem P = new OpticalNetworkMultiBandProblem();
      var dataToReloadProblem = setProblemCharacteristic(solution);
      P.reloadProblemWithMultiBand(load, gmlData, dataToReloadProblem);
      Double[] objectives = P.evaluate(vars);
      solution.objectives()[0] = objectives[0];
      solution.objectives()[1] = objectives[1] / maxCapex + 1 + solution.constraints()[1];
    } else {
      OpticalNetworkMultiBandProblem P = new OpticalNetworkMultiBandProblem();
      var dataToReloadProblem = setProblemCharacteristic(solution);
      P.reloadProblemWithMultiBand(load, gmlData, dataToReloadProblem);
      Double[] objectives = P.evaluate(vars);
      solution.objectives()[0] = objectives[0];
      if (buildMaxCapex) {
        solution.objectives()[1] = objectives[1];
        buildMaxCapex = false;
      } else {
        solution.objectives()[1] = objectives[1] / maxCapex;
      }
    }
    localPopulation.add((DefaultIntegerSolution) solution);
    if (contEvaluate != 0 && contEvaluate % (populationSize * this.iterationsToPrint) == 0) {
      countSolutionWithRestriction();
      countNodeXConstraint();
      printPopulation();
    }

    if (localPopulation.size() == populationSize) {
      if (getIteration() < 100) {
        countNodeXConstraint();
      }
      localPopulation.clear();
      nodesWithRestriction.replaceAll(i -> 0);
      solutionsXNodeConstraint.entrySet().stream()
          .sorted(Map.Entry.comparingByKey())
          .forEach(entry -> System.out.println(entry.getValue()));
    }
    return solution;
  }

  private void countSolutionWithRestriction() {
    var iteration = getIteration();
    var constrainsMetrics = StatisticsConstraints.getMetrics(localPopulation, iteration);
    constraintsStatistics.put(iteration, constrainsMetrics);
  }


  private void countNodeXConstraint() {
    var iteration = getIteration();
    ArrayList<Integer> list = new ArrayList<>();
    list.addAll(this.nodesWithRestriction);
    solutionsXNodeConstraint.put(iteration, list);
  }


  private void printPopulation() {
    var iteration = getIteration();
    var varName = this.varAndFunPath + execution + "/VAR" + iteration + ".csv";
    var funName = this.varAndFunPath + execution + "/FUN" + iteration + ".csv";
    printFinalSolutionSet(localPopulation, varName, funName);
  }

  private int getIteration() {
    return contEvaluate / populationSize;
  }


  /**
   * this method calculate the  constraint
   * g1(x), g2(x).
   *
   * @param solution
   */
  public void evaluateConstraints(DefaultIntegerSolution solution) {
    var net = getMockNet();
    net.setCn(getLocalComplexNetWork(solution));
    var algebraicConnectivityEvaluator = new AlgebraicConnectivityEvaluator();
    var algebraicConnectivity = algebraicConnectivityEvaluator.evaluate(net).getValue();
    var inverseAlgebraicConnectivity = 1 / (algebraicConnectivity + 1);
    solution.constraints()[0] = inverseAlgebraicConnectivity;
    solution.constraints()[1] = inadequateEquipment(solution);
  }

  /**
   * this method mock a nMultiBandNetWorkProfile for
   * be used in calculation of algebraic connectivity
   */
  private MultiBandNetWorkProfile getMockNet() {
    var nodes = new Vector<Node>();
    return new MultiBandNetWorkProfile(null, nodes, 1000, 0.01, 100000, 17.0, true, true, true, true, false,
        true,
        10e9, 0.013e-9, 1.0, 0.04e-12 / sqrt(1000), 0.0, 10.0, LAMBDA_FIRSTFIT, UTILIZAR_DIJ, false,
        MAX_NUMBER_OF_WAVELENGHTS);
  }


  /**
   * this method calculate the rate over
   * node which not address the fiber technology,
   * and it represents the constraint g2(x):
   * sun of nodes that do not address each link
   * divided by node degree sun.
   *
   * @param solution
   */
  private Double inadequateEquipment(DefaultIntegerSolution solution) {
    List<Boolean> nodesVisited = new ArrayList<>(Collections.nCopies(gml.getNodes().size(), false));
    var numNode = gml.getNodes().size();
    var nodeBeginPart = solution.variables().size() - (numNode + 1);
    var wssNodes = solution.variables().subList(nodeBeginPart, solution.variables().size() - 1);
    var nodeNoteAttend = 0.0;
    int[] nodesDegree = new int[numNode];
    Arrays.fill(nodesDegree, 0);
    for (int i = 0; i < numNode; i++) {
      for (int j = i + 1; j < numNode; j++) {
        var beginLinkPosition = Equipments.getLinkPosition(i, j, numNode, setSize);
        for (int w = 0; w < setSize; w++) {
          var link = solution.variables().get(beginLinkPosition + w);
          if (link != 0) {
            nodesDegree[i] += 1;
            nodesDegree[j] += 1;
          }
          if (!LevelNode.thisNodeAddressThisLink(wssNodes.get(i), link)) {
            nodeNoteAttend += 1;
            // the line below is control of metrics, it not make part of constraint calculation
            if (!nodesVisited.get(i)) {
              nodesVisited.set(i, true);
              var value = this.nodesWithRestriction.get(i) + 1;
              this.nodesWithRestriction.set(i, value);
            }
          }
          if (!LevelNode.thisNodeAddressThisLink(wssNodes.get(j), link)) {
            nodeNoteAttend += 1;
            // the line below is control of metrics, it not make part of constraint calculation
            if (!nodesVisited.get(j)) {
              nodesVisited.set(j, true);
              var value = this.nodesWithRestriction.get(j) + 1;
              this.nodesWithRestriction.set(j, value);
            }
          }
        }
      }
    }
    var sumOfNodeDegrees = Arrays.stream(nodesDegree).sum();
    if (sumOfNodeDegrees == 0) sumOfNodeDegrees = 1;
    if (sumOfNodeDegrees == 0) sumOfNodeDegrees = 1;
    return (nodeNoteAttend / sumOfNodeDegrees);
  }


  /**
   * method called for evaluate method
   * to get a gmlData used in SIMTON
   *
   * @param nodes
   * @param vars
   */

  public GmlData getGmlData(List<GmlNode> nodes, Integer[] vars) {
    GmlData gmlData = new GmlData();
    gmlData.setNodes(nodes);
    gmlData.setEdgeSets(buildSet(nodes, vars));
    return gmlData;
  }


  /**
   * This method is called in getGmlData
   * but is to help evaluate.
   *
   * @param nodesList
   * @param vars
   */
  public List<EdgeSet> buildSet(List<GmlNode> nodesList, Integer[] vars) {
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
                    edgeSet.getEdges().add(buildEdge(nodeOriginIndex, nodeTargetIndex, fiberOne));
                  }
                  if (fiberTwo != 0) {
                    edgeSet.getEdges().add(buildEdge(nodeOriginIndex, nodeTargetIndex, fiberTwo));
                  }
                  if (fiberThree != 0) {
                    edgeSet.getEdges().add(buildEdge(nodeOriginIndex, nodeTargetIndex, fiberThree));

                  }
                  edgeSets.add(edgeSet);
                  varIndex[0] += setSize;
                  return edgeSets;
                })
                .flatMap(List::stream))
        .collect(Collectors.toList());

  }

  public GmlData getGml() {
    return gml;
  }

  private void gmlBuild() {

    try {
      this.gml = new GmlDao().loadGmlData(path);
    } catch (Exception e) {
      e.printStackTrace();
    }
    setGmlMap();
  }

  private void setGmlMap() {
    Map<Integer, GmlNode> map = new HashMap<Integer, GmlNode>();
    gml.getNodes()
        .stream()
        .forEach(node -> map.put(node.getId(), node));
    this.mapNode = map;
  }

  private GmlEdge buildEdge(Integer nodeOriginIndex, Integer nodeTargetIndex, Integer fiber) {
    var edge = new GmlEdge();
    edge.setSource(this.mapNode.get(gml.getNodes()
        .get(nodeOriginIndex)
        .getId()));
    edge.setTarget(this.mapNode.get(gml.getNodes()
        .get(nodeTargetIndex)
        .getId()));
    edge.setBand(Bands.getBand(fiber));
    return edge;
  }


  private DataToReloadProblem setProblemCharacteristic(IntegerSolution solution) {

    return new DataToReloadProblem(
        solution.variables().size(),
        solution.objectives().length,
        solution.variables(),
        lowerBounds,
        upperBounds,
        setSize
    );
  }


  /**
   * Method copied from OpticalNetworkMultiBandProblem.
   * Since only the algebraic connectivity will be calculated,
   * distances is being passed as an empty matrix to ComplexNetwork.
   * In the original function createMultibandComplexNetworkDistance in
   * OpticalNetworkMultiBandProblem, it is not empty.
   *
   * @param s
   */
  public ComplexNetwork getLocalComplexNetWork(DefaultIntegerSolution s) {
    var numNodes = gml.getNodes().size();
    var connections = s.variables().subList(0, s.variables().size() - (numNodes + 1));
    Integer[] solution = connections.toArray(new Integer[0]);
    List<TMetric> metrics = new ArrayList<TMetric>();
    metrics.add(TMetric.ALGEBRAIC_CONNECTIVITY);

    Integer[][] matrix = new Integer[numNodes][numNodes];
    var distances = new Double[numNodes][numNodes];

    Double[][] customDistances = new Double[numNodes][numNodes];
    for (int i = 0; i < matrix.length; i++) {
      matrix[i][i] = 0;
      customDistances[i][i] = 0.0;
      for (int j = i + 1; j < matrix.length; j++) {
        //TODO, jorge, daqui ateo o primeiro if é onde estou fazendo um parse para
        // interpretar o cromossomo como 'tem conexão ou não'
        var index = Equipments.getLinkPosition(i, j, gml.getNodes().size(), setSize);
        var sum = 0;
        for (int w = 0; w < setSize; w++) {
          sum += solution[index + w];
        }
        if (sum != 0) {
          matrix[i][j] = 1;
          matrix[j][i] = 1;
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

  public Map getConstraintsStatistics() {
    return constraintsStatistics;
  }

  private void calculatePenaltyFactor() {
    buildMaxCapex = true;
    var boundSize = this.upperBounds.length;
    var majorLink = this.upperBounds[0];
    var majorWss = this.upperBounds[boundSize - 2];
    var majorWaveLength = this.upperBounds[boundSize - 1];

    IntegerSolution s = new DefaultIntegerSolution(variableBounds(), numberOfObjectives(),
        numberOfConstraints());
    var numNodes = this.gml.getNodes().size();
    var connectionsSize = s.variables().size() - (numNodes + 1);
    for (int i = 0; i < s.variables().size(); i++) {
      if (i < connectionsSize) {
        s.variables().set(i, majorLink);
      } else if (i < s.variables().size() - 1) {
        s.variables().set(i, majorWss);
      } else {
        s.variables().set(i, majorWaveLength);
      }
    }
    evaluate(s);
    this.maxCapex = s.objectives()[1];

  }

  public Map<Integer, ArrayList<Integer>> getSolutionsXNodeConstraint() {
    return solutionsXNodeConstraint;
  }

  public ExternalNetworkEvaluatorSettings(Integer setSize, int populationSize, String path, int iterationsToPrint,
      int execution, int load) {
    super();
    this.populationSize = populationSize;
    this.setSize = setSize;
    this.path = path;
    this.iterationsToPrint = iterationsToPrint;
    this.execution = execution;
    this.load = load;
    gmlBuild();
    this.numberOfObjectives(2);
    this.numberOfConstraints(2);
    var numberOfNodes = gml.getNodes()
        .size();
    var numberOfVariables = (setSize * numberOfNodes * (numberOfNodes - 1) / 2 + numberOfNodes + 1);

    this.upperBounds = new Integer[numberOfVariables];
    this.lowerBounds = new Integer[numberOfVariables];

    /**problem configuration */
    List<Integer> ll = new Vector<>();
    List<Integer> ul = new Vector<>();
    var roadmPlusW_Part = numberOfNodes + 1;
    var matrixConnectionPart = numberOfVariables - roadmPlusW_Part;

    for (int i = 0; i < matrixConnectionPart; i++) {
      ll.add(0);
      ul.add(19);
      this.lowerBounds[i] = 0;
      this.upperBounds[i] = 19;
    }

    for (int i = numberOfVariables - roadmPlusW_Part; i < numberOfVariables; i++) {
      if (i < numberOfVariables - 1) {
        ll.add(1);
        ul.add(3);
        this.lowerBounds[i] = 1;
        this.upperBounds[i] = 3;
      } else {
        ll.add(10);
        ul.add(100);
        this.lowerBounds[i] = 10;
        this.upperBounds[i] = 100;
      }
    }
    this.variableBounds(ll, ul);
    /** end problem configuration*/


    String varAndFunPath = "src/result/VARSandFUNS/execution";
    this.varAndFunPath = varAndFunPath;
    new File(varAndFunPath + execution).mkdirs();

    calculatePenaltyFactor();
    this.nodesWithRestriction = new ArrayList<>(Collections.nCopies(gml.getNodes().size(), 0));

  }
}
