package br.mnc;

import br.bm.core.OpticalNetworkProblem;
import br.cns24.model.Bands;
import br.cns24.model.EdgeSet;
import br.cns24.model.GmlData;
import br.cns24.model.GmlEdge;
import br.cns24.model.GmlNode;
import br.cns24.persistence.GmlDao;

import org.uma.jmetal.problem.integerproblem.impl.AbstractIntegerProblem;
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
    IntStream.iterate(0, i -> i + 3)
        .limit(solution.variables()
            .size() / 3)
        .forEach(i -> {
          if (random.nextBoolean()) {
            solution.variables()
                .set(i, 0);
            solution.variables()
                .set(i + 1, 0);
            solution.variables()
                .set(i + 2, 0);
          } else {
            solution.variables()
                .set(i, connections[random.nextInt(connections.length)]);
            solution.variables()
                .set(i + 1, connections[random.nextInt(connections.length)]);
            solution.variables()
                .set(i + 2, connections[random.nextInt(connections.length)]);
          }
        });
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
    if (gmlData.containsIsolatedNodes()) {
      solution.objectives()[0] = 1.0;
      solution.objectives()[1] = Double.MAX_VALUE;
    } else {
      OpticalNetworkProblem P = new OpticalNetworkProblem();
      P.reloadProblem(load, gmlData);
      Double[] objectives = P.evaluate(vars);
      solution.objectives()[0] = objectives[0];
      solution.objectives()[1] = objectives[1];
      solution.objectives()[2] = objectives[2];
      solution.objectives()[3] = 1 / (1 + objectives[3]);
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
    gmlData = gmlDao.loadGmlDataFromContent(gmlDao.createFileContent(gmlData));
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
                    edgeSet.getSet().add(buildEdge(nodeOriginIndex, nodeTargetIndex, fiberOne));
                  }
                  if (fiberTwo != 0) {
                    edgeSet.getSet().add(buildEdge(nodeOriginIndex, nodeTargetIndex, fiberTwo));
                  }
                  if (fiberThree != 0) {
                    edgeSet.getSet().add(buildEdge(nodeOriginIndex, nodeTargetIndex, fiberThree));

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
    String path = "./selectedCityInPernabucoState.gml";
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

  public ExternalNetworkEvaluatorSettings() {
    super();
    gmlBuild();
    this.numberOfObjectives(2);
    var numberOfNodes = gml.getNodes()
        .size();
    var numberOfVariables = (3 * numberOfNodes * (numberOfNodes - 1) / 2 + numberOfNodes + 1);

    /**problem configuration */
    List<Integer> ll = new Vector<>();
    List<Integer> ul = new Vector<>();
    var roadmPart = numberOfNodes + 1;
    var matrixConnectionPart = numberOfVariables - roadmPart;

    for (int i = 0; i < matrixConnectionPart; i++) {
      ll.add(0);
      ul.add(7);
    }

    for (int i = numberOfVariables - roadmPart; i < numberOfVariables; i++) {
      if (i < numberOfVariables - 1) {
        ll.add(1);
        ul.add(12);
      } else {
        ll.add(4);
        ul.add(40);
      }
    }
    this.variableBounds(ll, ul);
    /** end problem configuration*/
  }
}
