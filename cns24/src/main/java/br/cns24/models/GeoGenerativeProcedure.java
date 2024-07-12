/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: GeoGenerativeProcedure.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	05/11/2014		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.models;

import br.cns24.Geolocation;

/**
 * 
 * @author Danilo Araujo
 * @since 05/11/2014
 */
public class GeoGenerativeProcedure extends GenerativeProcedure {
	protected Geolocation[] geoCoords;

	protected double[][] distances;

	public GeoGenerativeProcedure() {
	}

	protected double computeDistance(Geolocation startCoords,
			Geolocation destCoords) {
		double startLatRads = startCoords.getLatitude();
		double startLongRads = startCoords.getLongitude();
		double destLatRads = destCoords.getLatitude();
		double destLongRads = destCoords.getLongitude();
		double radius = 6371; // raio da Terra em km
		double distance = Math.acos(Math.sin(startLatRads)
				* Math.sin(destLatRads) + Math.cos(startLatRads)
				* Math.cos(destLatRads)
				* Math.cos(startLongRads - destLongRads))
				* radius;
		return distance;
	}

	protected double computeDistanceSimple(Geolocation startCoords,
			Geolocation destCoords) {
		return Math.sqrt((startCoords.getLatitude() - destCoords.getLatitude())
				* (startCoords.getLatitude() - destCoords.getLatitude())
				+ (startCoords.getLongitude() - destCoords.getLongitude())
				* (startCoords.getLongitude() - destCoords.getLongitude()));
	}

	protected double computeDistance(Geolocation startCoords,
			Geolocation destCoords, boolean convertToRadians) {
		if (convertToRadians) {
			return computeDistance(
					new Geolocation(
							degreesToRadians(startCoords.getLatitude()),
							degreesToRadians(startCoords.getLongitude())),
					new Geolocation(degreesToRadians(destCoords.getLatitude()),
							degreesToRadians(destCoords.getLongitude())));
		}
		return computeDistance(startCoords, destCoords);
	}

	protected double degreesToRadians(double degrees) {
		return (degrees * Math.PI) / 180;
	}

	/**
	 * Construtor da classe.
	 */
	public GeoGenerativeProcedure(Geolocation[] geoCoords) {
		this.geoCoords = geoCoords;
		int size = geoCoords.length;
		distances = new double[size][size];
		for (int i = 0; i < size; i++) {
			distances[i][i] = 0;
			for (int j = i + 1; j < size; j++) {
				distances[i][j] = computeDistance(geoCoords[i], geoCoords[j],
						true);
				distances[j][i] = distances[i][j];
			}
		}
	}

	/**
	 * Construtor da classe.
	 */
	public GeoGenerativeProcedure(Geolocation[] geoCoords, boolean geolocated) {
		this.geoCoords = geoCoords;
		int size = geoCoords.length;
		distances = new double[size][size];
		if (geolocated) {
			for (int i = 0; i < size; i++) {
				distances[i][i] = 0;
				for (int j = i + 1; j < size; j++) {
					distances[i][j] = computeDistance(geoCoords[i],
							geoCoords[j], true);
					distances[j][i] = distances[i][j];
				}
			}
		} else {
			for (int i = 0; i < size; i++) {
				distances[i][i] = 0;
				for (int j = i + 1; j < size; j++) {
					distances[i][j] = computeDistanceSimple(geoCoords[i],
							geoCoords[j]);
					distances[j][i] = distances[i][j];
				}
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.cns.models.GenerativeProcedure#transform(java.lang.Integer[][])
	 */
	@Override
	public Integer[][] transform(Integer[][] matrix) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.cns.models.GenerativeProcedure#name()
	 */
	@Override
	public String name() {
		// TODO Auto-generated method stub
		return null;
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
	 *            O valor para setar em distances
	 */
	public void setDistances(double[][] distances) {
		this.distances = distances;
	}

	/**
	 * @return o valor do atributo geoCoords
	 */
	public Geolocation[] getGeoCoords() {
		return geoCoords;
	}

	/**
	 * Altera o valor do atributo geoCoords
	 * 
	 * @param geoCoords
	 *            O valor para setar em geoCoords
	 */
	public void setGeoCoords(Geolocation[] geoCoords) {
		this.geoCoords = geoCoords;
	}

}
