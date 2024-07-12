/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: GeokRegularTraffic.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	23/11/2014		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.models;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import br.cns24.Geolocation;
import br.cns24.metrics.AlgebraicConnectivity;
import br.cns24.metrics.ClusteringCoefficientEntropy;
import br.cns24.metrics.NaturalConnectivity;
import br.cns24.transformations.DegreeMatrix;

/**
 * 
 * @author Danilo Araujo
 * @since 23/11/2014
 */
public class GeokRegularTraffic extends GeoGenerativeProcedure {

	private double d;

	private Geolocation refPoint;

	private Double[][] traffic;

	private double[] posto;

	private static final double P_SKIP = 0.9;

	private double averageTraffic;

	private double maxTraffic;

	private double averagePosto;

	/**
	 * Construtor da classe.
	 * 
	 * @param geoCoords
	 */
	public GeokRegularTraffic(Geolocation[] geoCoords, Double[][] traffic, double d) {
		super(geoCoords);
		this.d = d;
		double maxLong, maxLat, minLong, minLat;
		maxLong = Double.NEGATIVE_INFINITY;
		maxLat = Double.NEGATIVE_INFINITY;
		minLong = Double.MAX_VALUE;
		minLat = Double.MAX_VALUE;

		maxTraffic = 0;
		this.traffic = traffic;
		posto = new double[traffic.length];
		for (int i = 0; i < traffic.length; i++) {
			for (int j = 0; j < traffic.length; j++) {
				posto[i] += traffic[i][j];
				averagePosto += posto[i];
				averageTraffic += traffic[i][j];
				if (maxTraffic < traffic[i][j]) {
					maxTraffic = traffic[i][j];
				}
			}
		}
		averagePosto /= traffic.length;
		averageTraffic /= (traffic.length * traffic.length - traffic.length);

		for (Geolocation g : geoCoords) {
			if (maxLat < g.getLatitude()) {
				maxLat = g.getLatitude();
			}
			if (maxLong < g.getLongitude()) {
				maxLong = g.getLongitude();
			}
			if (minLat > g.getLatitude()) {
				minLat = g.getLatitude();
			}
			if (minLong > g.getLongitude()) {
				minLong = g.getLongitude();
			}
		}
		refPoint = new Geolocation((maxLat + minLat) / 2, (maxLong + minLong) / 2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.cns.models.GeoGenerativeProcedure#transform(java.lang.Integer[][])
	 */
	public Integer[][] transform1(Integer[][] matrix) {
		int numNodes = matrix.length;
		Integer[][] newMatrix = new Integer[numNodes][numNodes];
		int numTotalLinks = 0;
		double[] distRef = new double[numNodes];
		double[] orderedDistances = new double[numNodes];
		double[] orderedPosto = new double[numNodes];

		// criar array com distancias ordenadas
		double[][] oDistance = new double[distances.length][distances.length];
		for (int i = 0; i < matrix.length; i++) {
			newMatrix[i][i] = 0;
			distRef[i] = computeDistance(geoCoords[i], refPoint, true);
			orderedPosto[i] = posto[i];
			for (int j = 0; j < matrix.length; j++) {
				newMatrix[i][j] = 0;
				newMatrix[j][i] = 0;
				oDistance[i][j] = distances[i][j];
			}
		}
		for (int i = 0; i < matrix.length; i++) {
			orderedDistances[i] = distRef[i];
			Arrays.sort(oDistance[i]);
		}
		Arrays.sort(orderedDistances);
		Arrays.sort(orderedPosto);

		Map<Integer, Integer> mapDistRef = new HashMap<Integer, Integer>();

		Map<Integer, Set<Integer>> mapaIncluidos = new HashMap<Integer, Set<Integer>>();
		Map<Integer, Integer> mapaPosto = new HashMap<Integer, Integer>();
		for (int i = 0; i < numNodes; i++) {
			mapaIncluidos.put(i, new HashSet<Integer>());
			for (int j = 0; j < numNodes; j++) {
				if (orderedDistances[i] == distRef[j]) {
					mapDistRef.put(i, j);
				}
				if (orderedPosto[i] == posto[j]) {
					mapaPosto.put(i, j);
				}
			}
		}

		// la�o para criar um grafo de Gabriel
		double distance, distanceI, distanceJ;
		for (int i = 0; i < matrix.length; i++) {
			newMatrix[i][i] = 0;
			for (int j = i + 1; j < matrix.length; j++) {
				distance = distances[i][j];
				boolean key = false;
				for (int k = 0; k < matrix.length; k++) {
					if (k != j && k != i) {
						distanceI = distances[i][k];
						distanceJ = distances[j][k];
						if (distanceI < distance && distanceJ < distance) {
							key = true;
							break;
						}
					}
				}
				if (!key) {
					newMatrix[i][j] = 1;
					newMatrix[j][i] = 1;
					numTotalLinks++;
				}
			}
		}

		Integer[][] dm = DegreeMatrix.getInstance().transform(newMatrix);
		// la�o para criar enlaces adicionais, at� atingir a densidade desejada
		int maxLinks = (int) Math.round(0.5 * d * numNodes * (numNodes - 1));
		maxLinks += numTotalLinks;
		while (numTotalLinks < maxLinks) {
			lblExt: for (int o = matrix.length - 1; o > 0; o--) {
				int i = mapaPosto.get(o);
				for (int l = 0; l < matrix.length; l++) {
					if (Math.random() < traffic[i][l] / maxTraffic && l != i && newMatrix[i][l] != 1) {
						// if
						// (ClusteringCoefficientEntropy.getInstance().calculate(newMatrix)
						// > 0
						// && varECC(newMatrix, i, l) > 0.15) {
						// break;
						// }
						dm[i][i]++;
						dm[l][l]++;
						newMatrix[i][l] = 1;
						newMatrix[l][i] = 1;
						numTotalLinks++;
						if (numTotalLinks >= maxLinks) {
							break lblExt;
						}
						continue lblExt;
					}
				}
			}
		}
		int attempts = 0;
		int maxAttempts = matrix.length * (matrix.length - 1) / 2;
		while (numTotalLinks > maxLinks && attempts < maxAttempts) {
			lblExt: for (int o = matrix.length - 1; o >= 0; o--) {
				int i = mapDistRef.get(o);
				if (Math.random() < posto[i] / orderedPosto[orderedPosto.length - 1]) {
					continue;
				}
				for (int j = 1; j < matrix.length; j++) {
					for (int l = 0; l < matrix.length; l++) {
						if (oDistance[i][j] == distances[i][l] && newMatrix[i][l] != 0 && dm[i][i] > 1
								&& dm[l][l] > 1) {
							attempts++;
							// System.out.println(attempts);
							if (traffic[i][l] > P_SKIP * averageTraffic) {
								break;
							}
							newMatrix[i][l] = 0;
							newMatrix[l][i] = 0;
							dm[i][i]--;
							dm[l][l]--;
							numTotalLinks--;
							if (numTotalLinks <= maxLinks) {
								break lblExt;
							}
							continue lblExt;
						}
					}
				}
			}
		}

		return newMatrix;
	}

	public boolean increaseECC(Integer[][] matrix, int i, int j) {
		double originalECC = ClusteringCoefficientEntropy.getInstance().calculate(matrix);
		double newECC = newECC(matrix, i, j);
		return newECC > originalECC;
	}

	public double varECC(Integer[][] matrix, int i, int j) {
		double originalECC = ClusteringCoefficientEntropy.getInstance().calculate(matrix);
		double newECC = newECC(matrix, i, j);
		return Math.abs((newECC - originalECC) / newECC);
	}

	public double newECC(Integer[][] matrix, int i, int j) {
		Integer[][] modified = new Integer[matrix.length][matrix.length];
		for (int k = 0; k < matrix.length; k++) {
			for (int l = 0; l < matrix.length; l++) {
				modified[k][l] = matrix[k][l];
			}
		}
		modified[i][j] = 1;
		modified[j][i] = 1;
		double newECC = ClusteringCoefficientEntropy.getInstance().calculate(modified);
		return newECC;
	}

	public double newECCRem(Integer[][] matrix, int i, int j) {
		Integer[][] modified = new Integer[matrix.length][matrix.length];
		for (int k = 0; k < matrix.length; k++) {
			for (int l = 0; l < matrix.length; l++) {
				modified[k][l] = matrix[k][l];
			}
		}
		modified[i][j] = 0;
		modified[j][i] = 0;
		double newECC = ClusteringCoefficientEntropy.getInstance().calculate(modified);
		return newECC;
	}

	public double newNCRem(Integer[][] matrix, int i, int j) {
		Integer[][] modified = new Integer[matrix.length][matrix.length];
		for (int k = 0; k < matrix.length; k++) {
			for (int l = 0; l < matrix.length; l++) {
				modified[k][l] = matrix[k][l];
			}
		}
		modified[i][j] = 0;
		modified[j][i] = 0;
		double newECC = NaturalConnectivity.getInstance().calculate(modified);
		return newECC;
	}

	public boolean isStilConected(Integer[][] matrix, int i, int j) {
		Integer[][] modified = new Integer[matrix.length][matrix.length];
		for (int k = 0; k < matrix.length; k++) {
			for (int l = 0; l < matrix.length; l++) {
				modified[k][l] = matrix[k][l];
			}
		}
		modified[i][j] = 0;
		modified[j][i] = 0;
		return AlgebraicConnectivity.getInstance().calculate(modified) > 1e-5;
	}

	public Integer[][] transformAtual(Integer[][] matrix) {
		int numNodes = matrix.length;
		Integer[][] newMatrix = new Integer[numNodes][numNodes];
		int numTotalLinks = 0;
		double[] distRef = new double[numNodes];
		double[] orderedDistances = new double[numNodes];
		double[] orderedPosto = new double[numNodes];

		// criar array com distancias ordenadas
		double[][] oDistance = new double[distances.length][distances.length];
		for (int i = 0; i < matrix.length; i++) {
			newMatrix[i][i] = 0;
			distRef[i] = computeDistance(geoCoords[i], refPoint, true);
			orderedPosto[i] = posto[i];
			for (int j = 0; j < matrix.length; j++) {
				newMatrix[i][j] = 0;
				newMatrix[j][i] = 0;
				oDistance[i][j] = distances[i][j];
			}
		}
		for (int i = 0; i < matrix.length; i++) {
			orderedDistances[i] = distRef[i];
			Arrays.sort(oDistance[i]);
		}
		Arrays.sort(orderedDistances);
		Arrays.sort(orderedPosto);

		Map<Integer, Integer> mapDistRef = new HashMap<Integer, Integer>();

		Map<Integer, Set<Integer>> mapaIncluidos = new HashMap<Integer, Set<Integer>>();
		Map<Integer, Integer> mapaPosto = new HashMap<Integer, Integer>();
		for (int i = 0; i < numNodes; i++) {
			mapaIncluidos.put(i, new HashSet<Integer>());
			for (int j = 0; j < numNodes; j++) {
				if (orderedDistances[i] == distRef[j]) {
					mapDistRef.put(i, j);
				}
				if (orderedPosto[i] == posto[j]) {
					mapaPosto.put(i, j);
				}
			}
		}

		// la�o para criar um grafo de Gabriel
		double distance, distanceI, distanceJ;
		for (int i = 0; i < matrix.length; i++) {
			newMatrix[i][i] = 0;
			for (int j = i + 1; j < matrix.length; j++) {
				distance = distances[i][j];
				boolean key = false;
				for (int k = 0; k < matrix.length; k++) {
					if (k != j && k != i) {
						distanceI = distances[i][k];
						distanceJ = distances[j][k];
						if (distanceI < distance && distanceJ < distance) {
							key = true;
							break;
						}
					}
				}
				if (!key) {
					newMatrix[i][j] = 1;
					newMatrix[j][i] = 1;
					numTotalLinks++;
				}
			}
		}

		Integer[][] dm = DegreeMatrix.getInstance().transform(newMatrix);
		// la�o para criar enlaces adicionais, at� atingir a densidade desejada
		// int maxLinks = (int) Math.round(0.5 * d * numNodes * (numNodes - 1));
		int maxLinks = (int) Math.round(d * ((numNodes - 1) / 2.0));
		// maxLinks += numTotalLinks;
		int maxDegreeIt = 2;
		int attempts = 0;
		int maxAttempts = matrix.length * (matrix.length - 1) / 2;
		while (numTotalLinks < maxLinks && attempts < maxAttempts) {
			lblExt: for (int o = 0; o < matrix.length; o++) {
				int i = mapDistRef.get(o);
				if (Math.random() > posto[i] / orderedPosto[orderedPosto.length - 1] || dm[i][i] > maxDegreeIt) {
					continue;
				}
				for (int j = 1; j < matrix.length; j++) {
					for (int l = 0; l < matrix.length; l++) {
						if (oDistance[i][j] == distances[i][l] && l != i && newMatrix[i][l] != 1) {
							attempts++;
							if (traffic[i][l] < P_SKIP * averageTraffic || dm[l][l] > maxDegreeIt) {
								break;
							}
							// if (traffic[i][l]/maxTraffic < Math.random()) {
							// break;
							// }
							dm[i][i]++;
							dm[l][l]++;
							newMatrix[i][l] = 1;
							newMatrix[l][i] = 1;
							numTotalLinks++;
							if (numTotalLinks >= maxLinks) {
								break lblExt;
							}
							continue lblExt;
						}
					}
				}
			}
			maxDegreeIt++;
		}

		// int[] orderedDegree = new int[dm.length];
		// for (int i = 0; i < orderedDegree.length; i++) {
		// orderedDegree[i] = dm[i][i];
		// }
		// Arrays.sort(orderedDegree);
		// Map<Integer, Integer> degree = new HashMap<Integer, Integer>();
		// for (int i = 0; i < orderedDegree.length; i++) {
		// for (int j = 0; j < orderedDegree.length; j++) {
		// if (o) {
		//
		// }
		// }
		// orderedDegree[i] = dm[i][i];
		// }
		// for (int i = 0; i < matrix.length; i++) {
		// for (int j = i + 1; j < matrix.length; j++) {
		// if (Math.random() > traffic[i][j] && dm[i][i] > 1 && dm[j][j] > 1) {
		// newMatrix[i][j] = 0;
		// newMatrix[j][i] = 0;
		// dm[i][i]--;
		// dm[j][j]--;
		// }
		// }
		// }

		// int numRemovedLinks = 0;
		// if (d > 0) {
		// numRemovedLinks = 3;
		// }
		// boolean stop = false;
		// while (numRemovedLinks > 0 && !stop) {
		// double minNC = Double.MAX_VALUE;
		// double[][] newNCs = new double[numNodes][numNodes];
		// for (int i = 0; i < numNodes; i++) {
		// for (int j = i + 1; j < numNodes; j++) {
		// if (newMatrix[i][j] == 1 && dm[i][i] > 1 && dm[j][j] > 1) {
		// newNCs[i][j] = newNCRem(newMatrix, i, j);
		// if (minNC > newNCs[i][j]) {
		// minNC = newNCs[i][j];
		// }
		// } else {
		// newNCs[i][j] = Double.MAX_VALUE;
		// }
		// }
		// }
		// stop = true;
		// lblExt: for (int i = 0; i < numNodes; i++) {
		// for (int j = i + 1; j < numNodes; j++) {
		// if (minNC == newNCs[i][j] && dm[i][i] > 1 && dm[j][j] > 1 &&
		// isStilConected(newMatrix, i, j)) {
		// newMatrix[i][j] = 0;
		// newMatrix[j][i] = 0;
		// dm[i][i]--;
		// dm[j][j]--;
		// numRemovedLinks--;
		// stop = false;
		// break lblExt;
		// }
		// }
		// }
		// }
		//
		// int numAddedLinks = 0;
		// if (d > 0) {
		// numAddedLinks = (int) (maxLinks * 0.1);
		// }
		// stop = false;
		// numAddedLinks = numAddedLinks - numRemovedLinks;
		// while (numAddedLinks > 0 && !stop) {
		// double maxEcc = 0;
		// double[][] newEccs = new double[numNodes][numNodes];
		// for (int i = 0; i < numNodes; i++) {
		// for (int j = i + 1; j < numNodes; j++) {
		// if (newMatrix[i][j] == 0) {
		// newEccs[i][j] = newECCRem(newMatrix, i, j);
		// if (maxEcc < newEccs[i][j]) {
		// maxEcc = newEccs[i][j];
		// }
		// } else {
		// newEccs[i][j] = -1;
		// }
		// }
		// }
		// stop = true;
		// lblExt: for (int i = 0; i < numNodes; i++) {
		// for (int j = i + 1; j < numNodes; j++) {
		// if (maxEcc == newEccs[i][j]) {
		// newMatrix[i][j] = 1;
		// newMatrix[j][i] = 1;
		// dm[i][i]++;
		// dm[j][j]++;
		// numAddedLinks--;
		// stop = false;
		// break lblExt;
		// }
		// }
		// }
		// }

		// Double[] lc =
		// LocalClusteringCoefficient.getInstance().calculate(newMatrix);
		// double[] orderedLc = new double[lc.length];
		// double maxLc = 0;
		// for (int i = 0; i < lc.length; i++) {
		// orderedLc[i] = lc[i];
		// if (maxLc < lc[i]) {
		// maxLc = lc[i];
		// }
		// }
		// Arrays.sort(orderedLc);
		// for (int j = 0; j < matrix.length; j++) {
		// for (int l = j + 1; l < matrix.length; l++) {
		// if (Math.random() < (lc[j] + lc[l])/ (2 * maxLc) && dm[j][j]> 1 &&
		// dm[l][l]> 1) {
		// newMatrix[j][l] = 0;
		// newMatrix[l][j] = 0;
		// dm[j][j]--;
		// dm[l][l]--;
		// // numTotalLinks--;
		// int newSource = (int)(Math.random() * newMatrix.length);
		// int newDest = (int)(Math.random() * newMatrix.length);
		// while (newDest == newSource || newMatrix[newSource][newDest] == 1) {
		// newSource = (int)(Math.random() * newMatrix.length);
		// newDest = (int)(Math.random() * newMatrix.length);
		// }
		// newMatrix[newSource][newDest] = 1;
		// newMatrix[newDest][newSource] = 1;
		// dm[newSource][newSource]++;
		// dm[newDest][newDest]++;
		// lc = LocalClusteringCoefficient.getInstance().calculate(newMatrix);
		// maxLc = 0;
		// for (int i = 0; i < lc.length; i++) {
		// if (maxLc < lc[i]) {
		// maxLc = lc[i];
		// }
		// }
		// }
		// }
		// }

		// while (numTotalLinks > maxLinks) {
		// lblExt: for (int o = 0; o < mapaPosto.size(); o++) {
		// int i = mapaPosto.get(o);
		// for (int j = 1; j < matrix.length; j++) {
		// for (int l = 0; l < matrix.length; l++) {
		// if (orderedPosto[j] == posto[l] && l != i && newMatrix[i][l] != 0 &&
		// dm[i][i] > 1) {
		// // if (traffic[i][l] > P_SKIP * averageTraffic) {
		// // break;
		// // }
		// newMatrix[i][l] = 0;
		// newMatrix[l][i] = 0;
		// numTotalLinks--;
		// if (numTotalLinks <= maxLinks) {
		// break lblExt;
		// }
		// continue lblExt;
		// }
		// }
		// }
		// }
		// }

		// rewiring considerando o clustering
		// Double[] lc =
		// LocalClusteringCoefficient.getInstance().calculate(newMatrix);
		// double[] orderedLc = new double[lc.length];
		// for (int i = 0; i < lc.length; i++) {
		// orderedLc[i] = lc[i];
		// }
		// Arrays.sort(orderedLc);
		// maxLinks = (int) (maxLinks * 0.99);
		// while (numTotalLinks > maxLinks) {
		// lblExt: for (int j = 0; j < matrix.length; j++) {
		// lc = LocalClusteringCoefficient.getInstance().calculate(newMatrix);
		// for (int i = 0; i < lc.length; i++) {
		// orderedLc[i] = lc[i];
		// }
		// Arrays.sort(orderedLc);
		// for (int l = 0; l < matrix.length; l++) {
		// if (orderedLc[j] == lc[l] && dm[l][l] > 1) {
		// for (int m = 0; m < matrix.length; m++) {
		// for (int n = 0; n < matrix.length; n++) {
		// if (m != l && orderedLc[m] == lc[n] && dm[n][n] > 1 && dm[l][l] > 1
		// && newMatrix[l][n] == 1) {
		// newMatrix[n][l] = 0;
		// newMatrix[l][n] = 0;
		// dm[n][n]--;
		// dm[l][l]--;
		// numTotalLinks--;
		// if (numTotalLinks <= maxLinks) {
		// break lblExt;
		// }
		// continue lblExt;
		// }
		// }
		// }
		// }
		// }
		// }
		// }
		return newMatrix;
	}

	public Integer[][] transformBest(Integer[][] matrix) {
		int numNodes = matrix.length;
		Integer[][] newMatrix = new Integer[numNodes][numNodes];
		int numTotalLinks = 0;
		double[] distRef = new double[numNodes];
		double[] orderedDistances = new double[numNodes];
		double[] orderedPosto = new double[numNodes];

		// criar array com distancias ordenadas
		double[][] oDistance = new double[distances.length][distances.length];
		for (int i = 0; i < matrix.length; i++) {
			newMatrix[i][i] = 0;
			distRef[i] = computeDistance(geoCoords[i], refPoint, true);
			orderedPosto[i] = posto[i];
			for (int j = 0; j < matrix.length; j++) {
				newMatrix[i][j] = 0;
				newMatrix[j][i] = 0;
				oDistance[i][j] = distances[i][j];
			}
		}
		for (int i = 0; i < matrix.length; i++) {
			orderedDistances[i] = distRef[i];
			Arrays.sort(oDistance[i]);
		}
		Arrays.sort(orderedDistances);
		Arrays.sort(orderedPosto);

		Map<Integer, Integer> mapDistRef = new HashMap<Integer, Integer>();

		Map<Integer, Set<Integer>> mapaIncluidos = new HashMap<Integer, Set<Integer>>();
		Map<Integer, Integer> mapaPosto = new HashMap<Integer, Integer>();
		for (int i = 0; i < numNodes; i++) {
			mapaIncluidos.put(i, new HashSet<Integer>());
			for (int j = 0; j < numNodes; j++) {
				if (orderedDistances[i] == distRef[j]) {
					mapDistRef.put(i, j);
				}
				if (orderedPosto[i] == posto[j]) {
					mapaPosto.put(i, j);
				}
			}
		}

		// la�o para criar um grafo de Gabriel
		double distance, distanceI, distanceJ;
		for (int i = 0; i < matrix.length; i++) {
			newMatrix[i][i] = 0;
			for (int j = i + 1; j < matrix.length; j++) {
				distance = distances[i][j];
				boolean key = false;
				for (int k = 0; k < matrix.length; k++) {
					if (k != j && k != i) {
						distanceI = distances[i][k];
						distanceJ = distances[j][k];
						if (distanceI < distance && distanceJ < distance) {
							key = true;
							break;
						}
					}
				}
				if (!key) {
					newMatrix[i][j] = 1;
					newMatrix[j][i] = 1;
					numTotalLinks++;
				}
			}
		}
		Integer[][] dm = DegreeMatrix.getInstance().transform(newMatrix);
		double sum = 0;
		for (int i = 0; i < dm.length; i++) {
			sum += dm[i][i];
		}
		sum /= dm.length;
		// System.out.println("Grau m�dio de Gabriel = " + sum);
		// System.out.println("Grau solicitado = " + d);
		// la�o para criar enlaces adicionais, at� atingir a densidade desejada
		// int maxLinks = (int) Math.round(0.5 * d * numNodes * (numNodes - 1));
		int maxLinks = (int) Math.round(d * ((numNodes - 1) / 2.0));
		// System.out.println("M�ximo = " + maxLinks);
		// System.out.println("Atual = " + numTotalLinks);
		// maxLinks += numTotalLinks;
		double maxDegreeIt = sum;
		int attempts = 0;
		int maxAttempts = matrix.length * (matrix.length - 1) / 2;

		while (numTotalLinks > maxLinks && attempts < maxAttempts && maxDegreeIt > 1) {
			lblExt: for (int o = matrix.length - 1; o > 0; o--) {
				int i = mapDistRef.get(o);
				if (0.5 > posto[i] / orderedPosto[orderedPosto.length - 1] || dm[i][i] > maxDegreeIt) {
					for (int j = 1; j < matrix.length; j++) {
						for (int l = 0; l < matrix.length; l++) {
							if (oDistance[i][j] == distances[i][l] && l != i && newMatrix[i][l] != 0) {
								attempts++;
								if (traffic[i][l] > P_SKIP * averageTraffic || dm[l][l] < maxDegreeIt || dm[l][l] == 1
										|| dm[i][i] == 1) {
									break;
								}
								dm[i][i]--;
								dm[l][l]--;
								newMatrix[i][l] = 0;
								newMatrix[l][i] = 0;
								numTotalLinks--;
								if (numTotalLinks == maxLinks) {
									break lblExt;
								}
								continue lblExt;
							}
						}
					}
				}

			}
			maxDegreeIt -= 0.15;
		}
		attempts = 0;
		// System.out.println("M�ximo = " + maxLinks);
		// System.out.println("Atual = " + numTotalLinks);
		maxDegreeIt = sum;

		while (numTotalLinks < maxLinks && attempts < maxAttempts) {
			lblExt: for (int o = 0; o < matrix.length; o++) {
				int i = mapDistRef.get(o);
				if (0.5 < posto[i] / orderedPosto[orderedPosto.length - 1] && dm[i][i] <= maxDegreeIt) {
					for (int j = 1; j < matrix.length; j++) {
						for (int l = 0; l < matrix.length; l++) {
							if (oDistance[i][j] == distances[i][l] && l != i && newMatrix[i][l] != 1) {
								attempts++;
								if (traffic[i][l] < P_SKIP * averageTraffic || dm[l][l] > maxDegreeIt) {
									break;
								}
								dm[i][i]++;
								dm[l][l]++;
								newMatrix[i][l] = 1;
								newMatrix[l][i] = 1;
								numTotalLinks++;
								if (numTotalLinks >= maxLinks) {
									break lblExt;
								}
								continue lblExt;
							}
						}
					}
				}

			}
			maxDegreeIt += 0.15;
		}

		return newMatrix;
	}

	public boolean island(int[] degree) {
		double ad = 0;
		for (int d : degree) {
			ad += d;
			if (d == 0) {
				return true;
			}
		}
		// System.out.println("AD = " + ad/degree.length);
		return false;
	}

	public boolean previouslyConnnected(List<List<String>> history, int i, int j, int round) {
		for (int r = round - 1; round >= 0; round--) {
			if (history.get(r).contains(i + ";" + j)) {
				return true;
			}
		}
		return false;
	}

	public Integer[][] transform(Integer[][] matrix) {
		int numNodes = matrix.length;
		Integer[][] newMatrix = new Integer[numNodes][numNodes];
		int numTotalLinks = 0;
		double[] distRef = new double[numNodes];
		double[] orderedDistances = new double[numNodes];
		double[] orderedPosto = new double[numNodes];

		// criar array com distancias ordenadas
		double[][] oDistance = new double[distances.length][distances.length];
		for (int i = 0; i < matrix.length; i++) {
			newMatrix[i][i] = 0;
			distRef[i] = computeDistance(geoCoords[i], refPoint, true);
			orderedPosto[i] = posto[i];
			for (int j = 0; j < matrix.length; j++) {
				newMatrix[i][j] = 0;
				newMatrix[j][i] = 0;
				oDistance[i][j] = distances[i][j];
			}
		}
		for (int i = 0; i < matrix.length; i++) {
			orderedDistances[i] = distRef[i];
			Arrays.sort(oDistance[i]);
		}
		Arrays.sort(orderedDistances);
		Arrays.sort(orderedPosto);

		Map<Integer, Integer> mapDistRef = new HashMap<Integer, Integer>();

		Map<Integer, Set<Integer>> mapaIncluidos = new HashMap<Integer, Set<Integer>>();
		Map<Integer, Integer> mapaPosto = new HashMap<Integer, Integer>();
		for (int i = 0; i < numNodes; i++) {
			mapaIncluidos.put(i, new HashSet<Integer>());
			for (int j = 0; j < numNodes; j++) {
				if (orderedDistances[i] == distRef[j]) {
					mapDistRef.put(i, j);
				}
				if (orderedPosto[i] == posto[j]) {
					mapaPosto.put(i, j);
				}
			}
		}

		int maxLinks = (int) Math.round(d * (numNodes / 2.0));

		double distance, distanceI, distanceJ;
		int[] degree = new int[matrix.length];
		List<List<String>> connectedInRounds = new Vector<List<String>>();
		int round = 0;
//		 System.out.println("Grau m�dio solicitado = " + d);
//		 System.out.println("Número m�ximo de links = " + maxLinks);
//		 System.out.printf("Criando rede com densidade %.2f \n",
//		 maxLinks/(numNodes * (numNodes-1)/2.0));
		int indexO = 0;
		while (numTotalLinks < maxLinks) {
			connectedInRounds.add(new Vector<String>());
			lblExt: for (int o = matrix.length - 1; o >= 0; o--) {//criar um rod�zio
				int i = mapaPosto.get(o);
				newMatrix[i][i] = 0;
				for (int j = 0; j < matrix.length; j++) {
					if (j == i) {
						continue;
					}
					distance = distances[i][j];
					boolean key = false;
					for (int k = 0; k < matrix.length; k++) {
						if (k != j && k != i) {
							distanceI = distances[i][k];
							distanceJ = distances[j][k];
							if (distanceI < distance && distanceJ < distance) {
								if (!(round > 0 && ((distanceI < distance
										&& previouslyConnnected(connectedInRounds, i, k, round))
										|| (distanceJ < distance
												&& previouslyConnnected(connectedInRounds, j, k, round))))) {
									key = true;
									break;
								}
							}
						}
					}
					if (!key && newMatrix[i][j] != 1) {
						newMatrix[i][j] = 1;
						newMatrix[j][i] = 1;
						// System.out.println("Conectou (" + i + ", " + j +
						// ").");
						degree[i]++;
						degree[j]++;
						connectedInRounds.get(round).add(i + ";" + j);
						connectedInRounds.get(round).add(j + ";" + i);
						numTotalLinks++;
						if (numTotalLinks == maxLinks) {
							if (!island(degree)) {
								break lblExt;
							}
						}
						// continue lblExt;
					}
				}
			}
			round++;
			
			indexO = indexO == matrix.length -1 ? 0 : indexO + 1;
			if (round > maxLinks * numNodes) {
				break;
			}
		}
		int attempts = 0;
		int maxAttempts = newMatrix.length * (newMatrix.length - 1) / 2;
		while (numTotalLinks > maxLinks && attempts < maxAttempts) {
			lblExt: for (int o = 0; o < matrix.length - 1; o++) {
				int i = mapaPosto.get(o);
				for (int j = i + 1; j < matrix.length; j++) {
					if (newMatrix[i][j] == 0) {
						continue;
					}
					distance = distances[i][j];
					boolean key = false;
					for (int k = 0; k < matrix.length; k++) {
						if (k != j && k != i && newMatrix[i][k] != 0 && newMatrix[j][k] != 0) {
							distanceI = distances[i][k];
							distanceJ = distances[j][k];
							if (distanceI > distance || distanceJ > distance || degree[i] == 1 || degree[j] == 1) {
								key = true;
								break;
							}
						}
					}
					if (!key) {
						attempts++;
						newMatrix[i][j] = 0;
						newMatrix[j][i] = 0;
						if (AlgebraicConnectivity.getInstance().calculate(newMatrix) < 0.00001) {
							newMatrix[i][j] = 1;
							newMatrix[j][i] = 1;
						} else {
							degree[i]--;
							degree[j]--;
							numTotalLinks--;
							if (numTotalLinks == maxLinks) {
								break lblExt;
							}
						}
					}
				}
			}
		}

		double ad = 0;
		for (double d : degree) {
			ad += d;
		}
		ad /= degree.length;
//		System.out.println("Grau m�dio = " + ad);
//		System.out.println("Número de links = " + numTotalLinks);

		return newMatrix;
	}

	/**
	 * @return o valor do atributo d
	 */
	public double getD() {
		return d;
	}

	/**
	 * Altera o valor do atributo d
	 * 
	 * @param d
	 *            O valor para setar em d
	 */
	public void setD(double d) {
		this.d = d;
	}
}
