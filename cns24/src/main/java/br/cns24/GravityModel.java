package br.cns24;
import br.cns24.model.GmlData;
import br.cns24.model.GmlEdge;
import br.cns24.model.GmlNode;

/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: GravityModel.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	12/11/2014		Vers�o inicial
 * ****************************************************************************
 */

/**
 * 
 * @author Danilo Araujo
 * @since 12/11/2014
 */
public class GravityModel {

	private GmlData gml;

	private Double[][] trafficMatrix;

	private Double[][] distances;

	/**
	 * Construtor da classe.
	 */
	public GravityModel(GmlData gml) {
		this.gml = gml;
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
	
	public double getGeoMaxDistance() {
		double distance[] = new double[distances.length];
		
		for (int i = 0; i < distances.length; i++)  {
			distance[i] = Double.MAX_VALUE;
		}
		
		for (int i = 0; i < distances.length; i++)  {
			for (int j = 0; j < distances.length; j++)  {
				if (distances[i][j] > 0 && distances[i][j] < distance[i]) {
					distance[i] = distances[i][j];
				}
			}
		}
		double d = 0;
		for (int i = 0; i < distances.length; i++)  {
			if (d < distance[i]) {
				d = distance[i];
			}
		}
		return d;
	}

	public double getGeoMinDistance() {
		double d = Double.MAX_VALUE;
		for (int i = 0; i < distances.length; i++)  {
			for (int j = 0; j < distances.length; j++)  {
				if (distances[i][j] > 0 && distances[i][j] < d) {
					d = distances[i][j];
				}
			}
		}
		return d;
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

	private void generateMatrix() {
		int numNodes = gml.getNodes().size();
		double maxT = Double.MIN_VALUE;
		trafficMatrix = new Double[numNodes][numNodes];
		Geolocation[] locations = new Geolocation[numNodes];
		for (int i = 0; i < locations.length; i++) {
			locations[i] = new Geolocation(gml.getNodes().get(i).getLatitude(), gml.getNodes().get(i).getLongitude());
		}
		double maxD = 0;
		double minD = Double.MAX_VALUE;
		double sd = 0;
		distances = new Double[numNodes][numNodes];
		double maxPop = 0;
		for (int i = 0; i < numNodes; i++) {
			distances[i][i] = 0.0;
			for (int j = i + 1; j < numNodes; j++) {
				distances[i][j] = computeDistance(locations[i], locations[j], true);
				distances[j][i] = distances[i][j];
//				sd += distances[j][i];
				if (maxD < distances[i][j]) {
					maxD = distances[i][j];
				}
				if (minD > distances[i][j]) {
					minD = distances[i][j];
				}
			}
			if (gml.getNodes().get(i).getPopulation() > maxPop) {
				maxPop = gml.getNodes().get(i).getPopulation();
			}
		}
		sd = (maxD * maxD) / (maxPop * maxPop);
		long[] populations = new long[numNodes];
		long[] degree = new long[numNodes];

		for (int i = 0; i < numNodes; i++) {
			for (GmlNode node : gml.getNodes()) {
				if (i == node.getId()) {
					populations[i] = node.getPopulation() > 0 ? node.getPopulation() : 1;
				}
			}
		}
		for (int i = 0; i < numNodes; i++) {
			degree[i] = 1;
			for (GmlEdge edge : gml.getEdges()) {
				if (i == edge.getSource().getId() || i == edge.getTarget().getId()) {
					degree[i]++;
				}
			}
		}
		double k = 0.01;
		for (int i = 0; i < numNodes; i++) {
			trafficMatrix[i][i] = 0.0;
			for (int j = i + 1; j < numNodes; j++) {
				if (distances[i][j] == 0) {
					trafficMatrix[i][j] = 0.0;
				} else {
					trafficMatrix[i][j] = (k * populations[i] * populations[j]) / (distances[i][j] * distances[i][j]);
				}
				trafficMatrix[i][j] = (k * degree[i] * degree[j]) / (distances[i][j] * distances[i][j]);
				trafficMatrix[j][i] = trafficMatrix[i][j];
				if (maxT <= trafficMatrix[i][j]) {
					maxT = trafficMatrix[i][j];
				}
			}
		}
		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++) {
				trafficMatrix[i][j] /= maxT;
			}
		}
	}

	public double getTraffic(int i, int j) {
		if (trafficMatrix == null) {
			generateMatrix();
		}
		return trafficMatrix[i][j];
	}

	/**
	 * @return o valor do atributo trafficMatrix
	 */
	public Double[][] getTrafficMatrix() {
		if (trafficMatrix == null) {
			generateMatrix();
		}
		return trafficMatrix;
	}

	/**
	 * @return o valor do atributo distances
	 */
	public Double[][] getDistances() {
		return distances;
	}

	/**
	 * Altera o valor do atributo distances
	 * @param distances O valor para setar em distances
	 */
	public void setDistances(Double[][] distances) {
		this.distances = distances;
	}

}
