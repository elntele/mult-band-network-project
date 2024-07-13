/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: GmlData.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	12/10/2013		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import br.cns24.Geolocation;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;
import br.cns24.experiments.nodePositions.CircularNetwork;
import br.cns24.models.TModel;

/**
 * @author Danilo
 * @since 12/10/2013
 */
public class GmlData implements Cloneable {
  private String name;

  private Map<String, String> informations;

  private List<GmlNode> nodes;

  private List<GmlEdge> edges;

  private List<EdgeSet> edgeSets;

  private double minLatitude;

  private double maxLatitude;

  private double minLongitude;

  private double maxLongitude;

  private String status = "";

  private double[][] distances;

  private int maxWaveLengths;

  private double oxcIsolationFactor;

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#clone()
   */
  @Override
  public GmlData clone() {
    GmlData clone = new GmlData();
    for (String i : getInformations().keySet()) {
      clone.informations.put(i, this.informations.get(i));
    }
    for (GmlNode node : this.getNodes()) {
      HashMap<String, String> infos = new HashMap<String, String>();
      for (String i : node.getInformations().keySet()) {
        infos.put(i, node.getInformations().get(i));
      }
      clone.addNode(node.getId(), node.getLabel(), node.getCountry(), node.getLongitude(), node.getLatitude(),
          node.getInternal(), infos, node.getPopulation());

    }
    clone.minLatitude = minLatitude;
    clone.maxLatitude = maxLatitude;
    clone.minLongitude = minLongitude;
    clone.maxLongitude = maxLongitude;

    if (distances != null) {
      clone.setDistances(new double[distances.length][distances[0].length]);
      for (int i = 0; i < distances.length; i++) {
        for (int j = 0; j < distances[0].length; j++) {
          clone.distances[i][j] = distances[i][j];
        }
      }
    }

    return clone;
  }

  /**
   * Construtor da classe.
   */
  public GmlData() {
    super();
    informations = new HashMap<>();
    nodes = new Vector<>();
    edges = new Vector<>();
  }

  public boolean containsIsolatedNodes() {
    boolean contains = false;
    for (GmlNode node : nodes) {
      contains = false;
      for (GmlEdge edge : edges) {
        if (edge.getSource().equals(node) || edge.getTarget().equals(node)) {
          contains = true;
          break;
        }
      }
      if (!contains) {
        return true;
      }
    }
    return false;
  }

  /**
   * Owner: jorge Candeias.
   * look in all node to verify if
   * there are almost one set of edge
   * who connect this node to any other
   * node in graph.
   * @return
   */
  public boolean containsIsolatedNodesInMultiBandModel() {
    return nodes.stream().anyMatch(node ->
        edgeSets.stream().noneMatch(edgeSet ->
            edgeSet.getEdges().stream().anyMatch(edge ->
                edge.getSource().equals(node) || edge.getTarget().equals(node)
            )
        )
    );
  }


  public boolean containsGbLink() {
    for (GmlEdge edge : edges) {
      if (edge.getLabel() != null && edge.getLabel().toLowerCase().contains("gb")) {
        return true;
      }
    }
    return false;
  }

  protected double computeDistance(Geolocation startCoords, Geolocation destCoords) {
    double startLatRads = startCoords.getLatitude();
    double startLongRads = startCoords.getLongitude();
    double destLatRads = destCoords.getLatitude();
    double destLongRads = destCoords.getLongitude();
    double radius = 6371; // raio da Terra em km
    double distance = Math.acos(Math.sin(startLatRads) * Math.sin(destLatRads) + Math.cos(startLatRads)
        * Math.cos(destLatRads) * Math.cos(startLongRads - destLongRads))
        * radius;
    return distance;
  }

  protected double computeDistance(Geolocation startCoords, Geolocation destCoords, boolean convertToRadians) {
    if (convertToRadians) {
      return computeDistance(
          new Geolocation(degreesToRadians(startCoords.getLatitude()),
              degreesToRadians(startCoords.getLongitude())),
          new Geolocation(degreesToRadians(destCoords.getLatitude()), degreesToRadians(destCoords
              .getLongitude())));
    }
    return computeDistance(startCoords, destCoords);
  }

  protected double degreesToRadians(double degrees) {
    return (degrees * Math.PI) / 180;
  }

  private double[][] generateMatrix(GmlData gml, int numNodes) {
    double[][] distances = new double[numNodes][numNodes];

    Geolocation[] locations = new Geolocation[numNodes];
    for (int i = 0; i < locations.length; i++) {
      locations[i] = new Geolocation(gml.getNodes().get(i).getLatitude(), gml.getNodes().get(i).getLongitude());
    }

    for (int i = 0; i < numNodes; i++) {
      distances[i][i] = 0;
      for (int j = i + 1; j < numNodes; j++) {
        distances[i][j] = computeDistance(locations[i], locations[j], true);
        distances[j][i] = distances[i][j];
      }
    }
//		//jorge dividindo a matriz
//		for (int i=0;i<distances.length;i++){
//			for (int j=0;j<distances.length;j++){
//				distances[i][j]=distances[i][j]/2;
//			}
//		}

    return distances;
  }

  public ComplexNetwork createComplexNetworkDistance() {
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
    metrics.add(TMetric.PHYSICAL_AVERAGE_PATH_LENGTH_SD);
    metrics.add(TMetric.ASSORTATIVITY);
    int numNodes = nodes.size();

    Geolocation[] locations = new Geolocation[numNodes];
    for (int i = 0; i < locations.length; i++) {
      locations[i] = new Geolocation(nodes.get(i).getLatitude(), nodes.get(i).getLongitude());
    }
    Double[][] distances = new Double[numNodes][numNodes];
    for (int i = 0; i < numNodes; i++) {
      distances[i][i] = 0.0;
      for (int j = i + 1; j < numNodes; j++) {
        distances[i][j] = computeDistance(locations[i], locations[j], true);
        distances[j][i] = distances[i][j];
      }
    }

    Double[][] customDistances = new Double[numNodes][numNodes];
    Double[][] completeDistances = new Double[numNodes][numNodes];
    Integer[][] adjacencyMatrix = new Integer[numNodes][numNodes];

    for (int i = 0; i < numNodes; i++) {
      for (int j = 0; j < numNodes; j++) {
        adjacencyMatrix[i][j] = 0;
        customDistances[i][j] = 0.0;
        if (i != j) {
          completeDistances[i][j] = computeDistance(locations[i], locations[j], true);
        } else {
          completeDistances[i][j] = 0.0;
        }
      }
    }
    int idSource = -1;
    int idTarget = -1;
    int index = 0;
    for (GmlEdge edge : edges) {
      index = 0;
      idSource = -1;
      idTarget = -1;
      for (GmlNode node : nodes) {
        if (node.getId() == edge.getSource().getId()) {
          idSource = index;
        }
        if (node.getId() == edge.getTarget().getId()) {
          idTarget = index;
        }
        if (idSource != -1 && idTarget != -1) {
          break;
        }
        index++;
      }
      adjacencyMatrix[idSource][idTarget] = 1;
      adjacencyMatrix[idTarget][idSource] = 1;
      customDistances[idSource][idTarget] = distances[idSource][idTarget];
      customDistances[idTarget][idSource] = distances[idTarget][idSource];
    }
    return new ComplexNetwork(0, adjacencyMatrix, new double[numNodes][numNodes], completeDistances,
        customDistances, TModel.CUSTOM, metrics);
  }

  public ComplexNetwork createComplexNetwork() {
    ComplexNetwork cn = null;
    int numNodes = nodes.size();
    Integer[][] adjacencyMatrix = new Integer[numNodes][numNodes];

    for (int i = 0; i < numNodes; i++) {
      for (int j = 0; j < numNodes; j++) {
        adjacencyMatrix[i][j] = 0;
      }
    }
    int idSource = -1;
    int idTarget = -1;
    int index = 0;
    for (GmlEdge edge : edges) {
      index = 0;
      idSource = -1;
      idTarget = -1;
      for (GmlNode node : nodes) {
        if (node.getId() == edge.getSource().getId()) {
          idSource = index;
        }
        if (node.getId() == edge.getTarget().getId()) {
          idTarget = index;
        }
        if (idSource != -1 && idTarget != -1) {
          break;
        }
        index++;
      }
      adjacencyMatrix[idSource][idTarget] = 1;
      adjacencyMatrix[idTarget][idSource] = 1;
    }
    distances = generateMatrix(this, numNodes);
    cn = new ComplexNetwork(getInformations().get("Network") + " (n = " + numNodes + ")", adjacencyMatrix,
        CircularNetwork.getInstance().createNodePositions(numNodes), TModel.CUSTOM, TMetric.getDefaults());

    return cn;
  }

  public void addInformation(String key, String value) {
    informations.put(key, value);
  }

  public boolean addNode(int id, String label, String country, double longitude, double latitude, int internal,
      HashMap<String, String> informations, long population) {
    if (latitude == 181 || longitude == 181 || id == -1) {
      return false;
    }
    GmlNode node = new GmlNode(id, label, country, longitude, latitude, internal, population);
    node.setInformations(informations);
    nodes.add(node);
    return true;
  }

  public boolean addNode(GmlNode node) {
    if (node.getLatitude() == 181 || node.getLongitude() == 181 || node.getId() == -1) {
      return false;
    }
    nodes.add(node);
    return true;
  }

  public boolean addNode(int id, String label, String country, double longitude, double latitude, int internal) {
    return addNode(id, label, country, longitude, latitude, internal, null, 0);
  }

  public boolean addEdge(int idSource, int idTarget, String label, HashMap<String, String> infos) {
    GmlNode sourceNode = new GmlNode(idSource);
    GmlNode targetNode = new GmlNode(idTarget);
    boolean found = false;
    for (GmlNode sn : nodes) {
      if (sn.equals(sourceNode)) {
        sourceNode = sn;
        found = true;
        break;
      }
    }
    if (!found) {
      return false;
    }
    found = false;
    for (GmlNode dn : nodes) {
      if (dn.equals(targetNode)) {
        targetNode = dn;
        found = true;
        break;
      }
    }
    if (!found) {
      return false;
    }
    edges.add(new GmlEdge(sourceNode, targetNode, label, infos));

    return true;
  }

  public boolean addEdge(int idSource, int idTarget, String label) {
    return addEdge(idSource, idTarget, label, null);
  }

  /**
   * @return o valor do atributo informations
   */
  public Map<String, String> getInformations() {
    return informations;
  }

  /**
   * Altera o valor do atributo informations
   *
   * @param informations
   *     O valor para setar em informations
   */
  public void setInformations(Map<String, String> informations) {
    this.informations = informations;
  }

  /**
   * @return o valor do atributo nodes
   */
  public List<GmlNode> getNodes() {
    return nodes;
  }

  /**
   * Altera o valor do atributo nodes
   *
   * @param nodes
   *     O valor para setar em nodes
   */
  public void setNodes(List<GmlNode> nodes) {
    this.nodes = nodes;
  }

  /**
   * @return o valor do atributo edges
   */
  public List<GmlEdge> getEdges() {
    return edges;
  }

  /**
   * Altera o valor do atributo edges
   *
   * @param edges
   *     O valor para setar em edges
   */
  public void setEdges(List<GmlEdge> edges) {
    this.edges = edges;
  }

  /**
   * @return o valor do atributo minLatitude
   */
  public double getMinLatitude() {
    return minLatitude;
  }

  /**
   * Altera o valor do atributo minLatitude
   *
   * @param minLatitude
   *     O valor para setar em minLatitude
   */
  public void setMinLatitude(double minLatitude) {
    this.minLatitude = minLatitude;
  }

  /**
   * @return o valor do atributo maxLatitude
   */
  public double getMaxLatitude() {
    return maxLatitude;
  }

  /**
   * Altera o valor do atributo maxLatitude
   *
   * @param maxLatitude
   *     O valor para setar em maxLatitude
   */
  public void setMaxLatitude(double maxLatitude) {
    this.maxLatitude = maxLatitude;
  }

  /**
   * @return o valor do atributo minLongitude
   */
  public double getMinLongitude() {
    return minLongitude;
  }

  /**
   * Altera o valor do atributo minLongitude
   *
   * @param minLongitude
   *     O valor para setar em minLongitude
   */
  public void setMinLongitude(double minLongitude) {
    this.minLongitude = minLongitude;
  }

  /**
   * @return o valor do atributo maxLongitude
   */
  public double getMaxLongitude() {
    return maxLongitude;
  }

  /**
   * Altera o valor do atributo maxLongitude
   *
   * @param maxLongitude
   *     O valor para setar em maxLongitude
   */
  public void setMaxLongitude(double maxLongitude) {
    this.maxLongitude = maxLongitude;
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
   *     O valor para setar em name
   */
  public void setName(String name) {
    this.name = name;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    GmlData other = (GmlData) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

  /**
   * @return o valor do atributo status
   */
  public String getStatus() {
    return status;
  }

  /**
   * Altera o valor do atributo status
   *
   * @param status
   *     O valor para setar em status
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * @return o valor do atributo distances
   */
  public double[][] getDistances() {
    return distances;
  }

  /**
   * Altera o valor do atributo distances
   *
   * @param distances
   *     O valor para setar em distances
   */
  public void setDistances(double[][] distances) {
    this.distances = distances;
  }

  /**
   * @return o valor do atributo maxWaveLengths
   */
  public int getMaxWaveLengths() {
    return maxWaveLengths;
  }

  /**
   * Altera o valor do atributo maxWaveLengths
   *
   * @param maxWaveLengths
   *     O valor para setar em maxWaveLengths
   */
  public void setMaxWaveLengths(int maxWaveLengths) {
    this.maxWaveLengths = maxWaveLengths;
  }

  /**
   * @return o valor do atributo oxcIsolationFactor
   */
  public double getOxcIsolationFactor() {
    return oxcIsolationFactor;
  }

  /**
   * Altera o valor do atributo oxcIsolationFactor
   *
   * @param oxcIsolationFactor
   *     O valor para setar em oxcIsolationFactor
   */
  public void setOxcIsolationFactor(double oxcIsolationFactor) {
    this.oxcIsolationFactor = oxcIsolationFactor;
  }

  public List<EdgeSet> getEdgeSets() {
    return edgeSets;
  }

  public void setEdgeSets(List<EdgeSet> edgeSets) {
    this.edgeSets = edgeSets;
  }
}
