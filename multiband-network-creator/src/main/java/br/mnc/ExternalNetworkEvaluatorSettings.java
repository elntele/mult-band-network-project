package br.mnc;

import br.bm.core.DataToReloadProblem;
import br.bm.core.OpticalNetworkMultiBandProblem;
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
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExternalNetworkEvaluatorSettings extends AbstractIntegerProblem {

  private static final long serialVersionUID = 1L;
  private GmlData gml;
  private Map<Integer, GmlNode> mapNode;
  public int contEvaluate = 0;
  private Integer[] upperBoundsMatrixPart;
  private Integer[] lowerBoundsMatrixPart;
  private Integer[] upperBounds;
  private Integer[] lowerBounds;
  private Integer tailRoadmPlusW;
  private Integer setSize;
  private Integer conteCreate = 0;

  @Override
  public IntegerSolution createSolution() {
    IntegerSolution integerSolution = new DefaultIntegerSolution(variableBounds(), numberOfObjectives(),
        numberOfConstraints());
    //call an initializer here
    //CreateRandomNetWork(((DefaultIntegerSolution) integerSolution));
    createRandomNetworkWithNodeNeighborhoodInformation(((DefaultIntegerSolution) integerSolution));
  //  System.out.println("file na criação: " + ((DefaultIntegerSolution) integerSolution).file);
   // System.out.println("variables na criação: ");
    PrintPopulation.printMatrix(integerSolution.variables(), gml.getNodes().size(), "none", "none", "none",
        ((DefaultIntegerSolution) integerSolution).file);
    //   System.out.println("oh eu aqui de novo");
    return integerSolution;
  }

  /**
   * this method create a random solution
   * considering, in variables, the part of
   * connections  and the part of ROADM
   * separated but considering a relationship
   * between the two parts.
   * //chromosome sample to 4 nodes: being (123) a 3-fiber set
   * // (123) (123) (123) (123) (123) (123) Node Node Node Node w
   * the below matrix represents the connection part
   * // connection matrix
   * //   1   2     3     4
   * // 1 x (123) (123) (123)
   * // 2     x   (123) (123)
   * // 3           x   (123)
   * // 4                 x
   * The way to running through the variables using the
   * nodes number in a 'chain for': for(i=...i++){for(j=...j++){}},
   * is introduced here.
   * as the column have 3 times more position them line,
   * the 'chained for' will need an external observer counter
   * to hold the final index position of the 'internal for'
   * in each iteration. It's because the 'internal for' next
   * iteration will begin in the last stopped position +1.
   *
   * @param solution
   */

  private void CreateRandomNetWork(IntegerSolution solution) {
    Random random = new Random();
    var connections = AllowedConnectionTable.getPossibleConnection();
    var connectionPartLimit = numberOfVariables() - tailRoadmPlusW; //the size of connection part
    IntStream.iterate(0, stepByIndexes -> stepByIndexes + setSize)
        .limit(connectionPartLimit / setSize)
        .forEach(index -> {
          // in 50% of times don't create connection between nodes
          if (random.nextBoolean()) {
            solution.variables()
                .set(index, 0);
            solution.variables()
                .set(index + 1, 0);
            solution.variables()
                .set(index + 2, 0);
          } else {
            // in 50% of times randomly chosen the one type of connection: 0,1,3,5,7.
            // remembering that 0 means no connection and consequently no edge/fiber
            boolean neighbor = false;
            solution.variables()
                .set(index, connections[random.nextInt(connections.length)]);
            solution.variables()
                .set(index + 1, connections[random.nextInt(connections.length)]);
            solution.variables()
                .set(index + 2, connections[random.nextInt(connections.length)]);
          }
        });

    var numNode = gml.getNodes().size();
    var roadmIndex = numberOfVariables() - (numNode + 1); //+1 cause its difference includes w part

    var externalObserverCount = 0;
    //here is the 'chained for' described on top of method
    var offset = 1;
    for (int i = 0; i < numNode; i++) { //go through matrix column by index i
      var maxEquipment = 0;
      var nextLimit = externalObserverCount + (numNode - offset) * setSize;
      for (int j = externalObserverCount; j < nextLimit; j++) {//go through matrix line by index j
        if (solution.variables().get(j) > maxEquipment) {
          maxEquipment = solution.variables().get(j);
        }
        externalObserverCount = j;
      }
      externalObserverCount++;
      offset += 1;

      solution.variables().set(roadmIndex, randomlySelectSwitch(maxEquipment));
      roadmIndex++;
    }
  }


  void createRandomNetworkWithNodeNeighborhoodInformation(
      IntegerSolution solution
  ) {
    fullFillFile((DefaultIntegerSolution) solution);
    Random random = new Random();
    var possibleConnection = AllowedConnectionTable.getPossibleConnection();
    for (int i = 0; i < gml.getNodes().size(); i++) {
      for (int j = i + 1; j < gml.getNodes().size(); j++) {
        var index = Equipments.getLinkPosition(i, j, gml.getNodes().size(), setSize);
        var result = random.nextDouble();

        if (result <= 0.875) {
          for (int w = 0; w < setSize; w++) {
            solution.variables()
                .set(index + w, 0);
          }

        } else {
          // in 50% of times randomly chosen the one type of connection: 0,1,3,5,7.
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


  private Integer randomlySelectSwitch(Integer equipment) {
    Random random = new Random();
    switch (equipment) {
      case 0 -> {
        return 0;
      }
      case 1 -> {
        return random.nextInt(1, 4);
      }
      case 3 -> {
        return random.nextInt(5, 8);
      }
      case 5, 7 -> {
        return random.nextInt(9, 12);
      }
      default -> throw new IllegalStateException("Unexpected value: " + equipment);
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
    int load = 100;
    Integer[] vars = new Integer[solution.variables()
        .size()];
    for (int i = 0; i < vars.length; i++) {
      vars[i] = (Integer) solution.variables()
          .get(i);
    }

    System.out.println("conte Evaluate: " + this.contEvaluate);
    this.contEvaluate += 1;
    GmlData gmlData = getGmlData(gml.getNodes(), vars);
    if (solution.constraints()[0] > 0) {
      solution.objectives()[0] = 1.0;
      solution.objectives()[1] = Double.MAX_VALUE;
    } else if (solution.constraints()[1] > 0) {
      solution.objectives()[0] = 1.0;
      solution.objectives()[1] = Double.MAX_VALUE / 2;
    } else if (solution.constraints()[2] > 0) {
      solution.objectives()[0] = 1.0;
      solution.objectives()[1] = Double.MAX_VALUE / 5;
    } else {
      OpticalNetworkMultiBandProblem P = new OpticalNetworkMultiBandProblem();
      var dataToReloadProblem = setProblemCharacteristic(solution);
      P.reloadProblemWithMultiBand(load, gmlData, dataToReloadProblem);
      Double[] objectives = P.evaluate(vars);
      //    solution.objectives()[0] = objectives[0];
      Random random = new Random();
      solution.objectives()[0] = random.nextDouble();// para testes
      solution.objectives()[1] = objectives[1];
    }
    return solution;
  }

 /* @Override
  public IntegerSolution evaluate(IntegerSolution solution) {
    evaluateConstraints((DefaultIntegerSolution) solution);
    int load = 100;
    Integer[] vars = new Integer[solution.variables()
        .size()];
    for (int i = 0; i < vars.length; i++) {
      vars[i] = (Integer) solution.variables()
          .get(i);
    }

    System.out.println("conte Evaluate: " + this.contEvaluate);
    this.contEvaluate += 1;
    GmlData gmlData = getGmlData(gml.getNodes(), vars);
    if (gmlData.containsIsolatedNodesInMultiBandModel()) {
      solution.objectives()[0] = 1.0;
      solution.objectives()[1] = Double.MAX_VALUE;
    } else {
      OpticalNetworkMultiBandProblem P = new OpticalNetworkMultiBandProblem();
      var dataToReloadProblem = setProblemCharacteristic(solution);
      P.reloadProblemWithMultiBand(load, gmlData, dataToReloadProblem);
      Double[] objectives = P.evaluate(vars);
      //    solution.objectives()[0] = objectives[0];
      solution.objectives()[0] = 1.0;// para testes
      solution.objectives()[1] = objectives[1];
    }
    return solution;
  }*/


  /**
   * this method calculate the  constraint
   * g1(x), g2(x), g3(x).
   *
   * @param solution
   */
  public void evaluateConstraints(DefaultIntegerSolution solution) {
    solution.constraints()[0] = isolatedNodes(solution);
    solution.constraints()[1] = inadequateEquipment(solution);
    solution.constraints()[2] = additionalBandBeforeBandC(solution);
  }

  /**
   * this method calculate the rate over
   * node isolated, and it represents
   * the constraint g1(x).
   *
   * @param solution
   */
  private Double isolatedNodes(DefaultIntegerSolution solution) {
    double isolatedNodeCount = 0.0;
    for (var entry : solution.file.entrySet()) {
      Set<Integer> set = (Set<Integer>) entry.getValue();
      if (set.isEmpty()) {
        isolatedNodeCount++;
      }
    }
    return isolatedNodeCount / gml.getNodes().size();
  }


  /**
   * this method calculate the rate over
   * node who not address the fiber technology,
   * and it represents the constraint g2(x):
   * sun of nodes that do not address each link
   * divided by node degree sun.
   *
   * @param solution
   */
  private Double inadequateEquipment(DefaultIntegerSolution solution) {
    var numNode = gml.getNodes().size();
    var nodeBeginPart = solution.variables().size() - (numNode + 1);
    var wssNodes = solution.variables().subList(nodeBeginPart, solution.variables().size() - 1);
    var nodeNoteAttend = 0.0;
    int[] nodesDegree = new int[numNode];
    for (int i = 0; i < nodesDegree.length; i++) {
      nodesDegree[i] = 0;
    }
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
          }
          if (!LevelNode.thisNodeAddressThisLink(wssNodes.get(j), link)) {
            nodeNoteAttend += 1;
          }
        }
      }
    }
    var sumOfNodeDegrees = Arrays.stream(nodesDegree).sum();
    if (sumOfNodeDegrees == 0) sumOfNodeDegrees = 1;
    return (nodeNoteAttend / sumOfNodeDegrees);
  }

  /**
   * this method calculate the rate over
   * links with additional band being used
   * without use of c band, and it represents
   * the constraint g3(x).
   *
   * @param solution
   */
  private Double additionalBandBeforeBandC(DefaultIntegerSolution solution) {
    var numNode = gml.getNodes().size();
    var setViolateRoles = 0.0;
    for (int i = 0; i < numNode; i++) {
      for (int j = i + 1; j < numNode; j++) {
        var beginLinkPosition = Equipments.getLinkPosition(i, j, numNode, setSize);
        for (int w = 0; w < setSize; w++) {
          var link = solution.variables().get(beginLinkPosition + w);
          switch (link) {
            case 2, 4, 6 -> setViolateRoles += 1;
          }
        }
      }
    }
    var totalFiber = 3 * ((numNode * numNode) - numNode) / 2;
    return (setViolateRoles / totalFiber);
  }

  /**
   * method called for evaluate method
   * to get a gmlData used in SIMTON
   *
   * @param nodes
   * @param vars
   */

  public GmlData getGmlData(List<GmlNode> nodes, Integer[] vars) {
    GmlDao gmlDao = new GmlDao();
    GmlData gmlData = new GmlData();
    gmlData.setNodes(nodes);
    //var links = buildEdges(nodes, vars);
    gmlData.setEdgeSets(buildSet(nodes, vars));
    //gmlData.setEdges(links);
    //gmlData.createComplexNetwork();
    // gmlData = gmlDao.loadGmlDataFromContent(gmlDao.createFileContent(gmlData));
    return gmlData;
  }

  /**
   * This method is called in getGmlData
   * but is to help evaluate.
   *
   * @param nodesList
   * @param vars
   */
  public List<GmlEdge> buildEdges(List<GmlNode> nodesList, Integer[] vars) {
    //it is used to allows modification inside lambda
    int[] varIndex = { 0 };
    List<GmlEdge> edges = new ArrayList<>();
    List<Integer> varList = Arrays.asList(vars);

    return IntStream.range(0, nodesList.size())
        .boxed()
        .flatMap(i -> IntStream.range(i, nodesList.size())
            .filter(j -> i != j)
            .mapToObj(j -> {
              GmlEdge edge = null;
              if (varList.get(varIndex[0]) == 1) {
                edge = new GmlEdge();
                edge.setSource((GmlNode) this.mapNode.get(nodesList.get(i)
                    .getId()));
                edge.setTarget((GmlNode) this.mapNode.get(nodesList.get(j)
                    .getId()));
                edges.add(edge);
              }
              varIndex[0] += 3;
              return edge;
            }))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

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
    //String path = "./selectedCityInPernabucoState.gml";
    String path = "./teste2.gml";
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
        upperBounds
    );
  }

  public ExternalNetworkEvaluatorSettings(Integer setSize) {
    super();
    this.setSize = setSize;
    gmlBuild();
    this.numberOfObjectives(2);
    this.numberOfConstraints(3);
    var numberOfNodes = gml.getNodes()
        .size();
    var numberOfVariables = (setSize * numberOfNodes * (numberOfNodes - 1) / 2 + numberOfNodes + 1);

    this.upperBounds = new Integer[numberOfVariables];
    this.lowerBounds = new Integer[numberOfVariables];

    /**problem configuration */
    List<Integer> ll = new Vector<>();
    List<Integer> ul = new Vector<>();
    var roadmPlusW_Part = numberOfNodes + 1;
    this.tailRoadmPlusW = roadmPlusW_Part;
    var matrixConnectionPart = numberOfVariables - roadmPlusW_Part;

    for (int i = 0; i < matrixConnectionPart; i++) {
      ll.add(0);
      ul.add(7);
      this.lowerBounds[i] = 0;
      this.upperBounds[i] = 7;
    }


    for (int i = numberOfVariables - roadmPlusW_Part; i < numberOfVariables; i++) {
      if (i < numberOfVariables - 1) {
        ll.add(1);
        ul.add(12);
        this.lowerBounds[i] = 1;
        this.upperBounds[i] = 12;
      } else {
        ll.add(4);
        ul.add(40);
        this.lowerBounds[i] = 4;
        this.upperBounds[i] = 40;
      }
    }
    this.variableBounds(ll, ul);
    /** end problem configuration*/
  }
}
