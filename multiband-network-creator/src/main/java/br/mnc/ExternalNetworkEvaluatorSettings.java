package br.mnc;

import br.bm.core.DataToReloadProblem;
import br.bm.core.Node;
import br.bm.core.OpticalNetworkProblem;
import br.cns24.model.Bands;
import br.cns24.model.EdgeSet;
import br.cns24.model.GmlData;
import br.cns24.model.GmlEdge;
import br.cns24.model.GmlNode;
import br.cns24.persistence.GmlDao;

import org.uma.jmetal.problem.integerproblem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;

import java.util.*;
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

  @Override
  public IntegerSolution createSolution() {
    IntegerSolution integerSolution = new DefaultIntegerSolution(variableBounds(), numberOfObjectives(),
        numberOfConstraints());
    //call an initializer here
    CreateRandomNetWork(integerSolution);
    System.out.println("oh eu aqui de novo");
    return integerSolution;
  }

  private void CreateRandomNetWork(IntegerSolution solution) {
    Random random = new Random();
    var connections = AllowedConnectionTable.getPossibleConnection();
    var maxLimit = numberOfVariables() - tailRoadmPlusW;
    IntStream.iterate(0, stepByIndexes -> stepByIndexes + 3)
        .limit(maxLimit / 3)
        .forEach(index -> {
          if (random.nextBoolean()) {
            solution.variables()
                .set(index, 0);
            solution.variables()
                .set(index + 1, 0);
            solution.variables()
                .set(index + 2, 0);
          } else {
            solution.variables()
                .set(index, connections[random.nextInt(connections.length)]);
            solution.variables()
                .set(index + 1, connections[random.nextInt(connections.length)]);
            solution.variables()
                .set(index + 2, connections[random.nextInt(connections.length)]);
          }
        });

    var nodeSize = gml.getNodes().size();
    var roadmPartIndex = numberOfVariables() - (nodeSize + 1); //+1 cause its difference includes w part
    for (int nodeObservedIndex = 0; nodeObservedIndex < nodeSize; nodeObservedIndex++) {
      var maxEquipment = 0;
      for (int otherNodeIndex = (nodeSize * nodeObservedIndex); otherNodeIndex < (nodeSize * nodeObservedIndex +nodeSize); otherNodeIndex++) {
        if (solution.variables().get(otherNodeIndex) > maxEquipment)
          maxEquipment = solution.variables().get(otherNodeIndex);
      }
      solution.variables().set(roadmPartIndex, selectSwitch(maxEquipment));
      roadmPartIndex++;
    }
  }


  private Integer selectSwitch(Integer equipment) {
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
      OpticalNetworkProblem P = new OpticalNetworkProblem();
      var dataToReloadProblem = setProblemCharacteristic(solution);
      P.reloadProblemWithMultiBand(load, gmlData, dataToReloadProblem);
      Double[] objectives = P.evaluate(vars);
      solution.objectives()[0] = objectives[0];
      solution.objectives()[1] = objectives[1];
    }
    return solution;
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
                  varIndex[0] += 3;
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
    String path = "./teste.gml";
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
    edge.setBand(buildBand(fiber));
    return edge;
  }

  private Bands buildBand(Integer fiber) {
    switch (fiber) {
      case 1 -> {
        return Bands.CBAND;
      }
      case 2 -> {
        return Bands.LBAND;
      }
      case 3 -> {
        return Bands.CLBAND;
      }
      case 4 -> {
        return Bands.SBAND;
      }
      case 5 -> {
        return Bands.CSBAND;
      }
      case 6 -> {
        return Bands.LSBAND;
      }
      case 7 -> {
        return Bands.CLSBAND;
      }
      default -> throw new IllegalStateException("Unexpected value: " + fiber);
    }
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

  public ExternalNetworkEvaluatorSettings() {
    super();
    gmlBuild();
    this.numberOfObjectives(2);
    var numberOfNodes = gml.getNodes()
        .size();
    var numberOfVariables = (3 * numberOfNodes * (numberOfNodes - 1) / 2 + numberOfNodes + 1);

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
