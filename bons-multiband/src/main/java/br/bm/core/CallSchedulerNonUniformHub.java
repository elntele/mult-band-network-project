/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software e confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: CallSchedulerNonUniformHub.java
 * ****************************************************************************
 * Historico de revisoes
 * Nome				Data		Descricao
 * ****************************************************************************
 * Danilo Araujo	02/01/2014		Versao inicial
 * ****************************************************************************
 */
package br.bm.core;

import static br.bm.core.SimonUtil.getRandomExp;

import java.util.Vector;

/**
 * 
 * @author Danilo
 * @since 02/01/2014
 */
public class CallSchedulerNonUniformHub extends CallScheduler { 
	/**
	 * 
	 */
	private static final double MAX_TIME = 1E30;

	private double MEAN_DURATION = 1.0;

	public static final Double[][] TRAFFICMATRIX_NON_UNIFORM_RANDOM = new Double[][]{
		{0.000000, 0.034352, 0.912167, 0.016080, 0.089984, 0.908939, 0.335850, 0.388803, 0.615332, 0.547694, 0.746427, 0.210656, 0.560160, 0.917427 },
		{0.034352, 0.000000, 0.206058, 0.211567, 0.810198, 0.262676, 0.264999, 0.790134, 0.569544, 0.889417, 0.984754, 0.642372, 0.409917, 0.357089 },
		{0.912167, 0.206058, 0.000000, 0.119911, 0.295436, 0.073032, 0.749248, 0.857744, 0.090167, 0.396375, 0.791815, 0.371110, 0.583077, 0.042202 },
		{0.016080, 0.211567, 0.119911, 0.000000, 0.592730, 0.452852, 0.171392, 0.675451, 0.688007, 0.668880, 0.984032, 0.858261, 0.896510, 0.792279 },
		{0.089984, 0.810198, 0.295436, 0.592730, 0.000000, 0.726907, 0.207181, 0.209457, 0.762260, 0.222680, 0.293675, 0.570509, 0.049453, 0.843141 },
		{0.908939, 0.262676, 0.073032, 0.452852, 0.726907, 0.000000, 0.359365, 0.938431, 0.102972, 0.896601, 0.164526, 0.466728, 0.504924, 0.303230 },
		{0.335850, 0.264999, 0.749248, 0.171392, 0.207181, 0.359365, 0.000000, 0.191667, 0.983254, 0.564408, 0.568000, 0.985600, 0.207689, 0.345823 },
		{0.388803, 0.790134, 0.857744, 0.675451, 0.209457, 0.938431, 0.191667, 0.000000, 0.143928, 0.627214, 0.305933, 0.545191, 0.767551, 0.210260 },
		{0.615332, 0.569544, 0.090167, 0.688007, 0.762260, 0.102972, 0.983254, 0.143928, 0.000000, 0.447799, 0.203416, 0.244077, 0.718746, 0.008085 },
		{0.547694, 0.889417, 0.396375, 0.668880, 0.222680, 0.896601, 0.564408, 0.627214, 0.447799, 0.000000, 0.976861, 0.461435, 0.887134, 0.930919 },
		{0.746427, 0.984754, 0.791815, 0.984032, 0.293675, 0.164526, 0.568000, 0.305933, 0.203416, 0.976861, 0.000000, 0.573291, 0.120408, 0.466342 },
		{0.210656, 0.642372, 0.371110, 0.858261, 0.570509, 0.466728, 0.985600, 0.545191, 0.244077, 0.461435, 0.573291, 0.000000, 0.563466, 0.005197 },
		{0.560160, 0.409917, 0.583077, 0.896510, 0.049453, 0.504924, 0.207689, 0.767551, 0.718746, 0.887134, 0.120408, 0.563466, 0.000000, 0.782356 },
		{0.917427, 0.357089, 0.042202, 0.792279, 0.843141, 0.303230, 0.345823, 0.210260, 0.008085, 0.930919, 0.466342, 0.005197, 0.782356, 0.000000 },
	};
	
	public static final Double[][] TRAFFICMATRIX_NSFNET_MBRM96 = new Double[][]{
		{0.00, 1.09, 2.06, 0.14, 0.45, 0.04, 0.43, 1.45, 0.51, 0.10, 0.07, 0.09, 0.00, 0.33 },
		{11.71, 0.00, 8.56, 0.62, 11.12, 7.77, 3.62, 15.79, 3.66, 16.61, 2.03, 37.81, 4.83, 13.19 },
		{0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00 },
		{0.31, 3.41, 13.64, 0.00, 1.90, 0.60, 0.70, 2.88, 2.00, 3.26, 3.07, 6.69, 0.08, 4.01 },
		{0.28, 67.51, 19.02, 3.43, 0.00, 4.03, 10.77, 62.22, 24.02, 17.92, 0.45, 79.03, 9.97, 5.29 },
		{0.00, 5.81, 3.42, 5.52, 3.4, 0.00, 2.61, 2.68, 0.87, 3.87, 0.04, 0.84, 0.06, 2.48 },
		{1.75, 22.02, 102.31, 4.47, 22.03, 7.9, 0.00, 114.10, 19.82, 21.95, 0.78, 71.40, 0.33, 32.84 },
		{2.39, 63.84, 210.30, 8.52, 28.21, 2.66, 97.08, 0.00, 43.95, 33.00, 11.37, 48.63, 5.53, 13.85 },
		{6.45, 18.93, 37.35, 6.00, 24.99, 6.81, 25.06, 61.02, 0.00, 39.62, 14.52, 127.50, 23.34, 0.76 },
		{0.05, 35.29, 10.26, 3.73, 22.34, 9.48, 4.98, 57.08, 6.84, 0.00, 6.3, 17.64, 5.91, 0.76 },
		{0.10, 1.02, 3.13, 1.69, 0.24, 0.06, 0.81, 1.45, 0.58, 7.12, 0.00, 0.84, 0.06, 0.50 },
		{1.28, 26.15, 1.00, 5.94, 24.86, 1.32, 5.49, 40.57, 29.53, 22.37, 10.50, 0.00, 1.01, 0.54 },
		{0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00 },
		{0.73, 29.09, 13.63, 9.89, 35.61, 12.07, 6.44, 28.79, 4.67, 0.00, 3.99, 0.00, 10.75, 0.000000 },
	};
	
	public static final Double[][] TRAFFICMATRIX_NSFNET_BIDIRECTIONAL = new Double[][]{
		{0.00, 6.40, 1.03, 0.23, 0.37, 0.02, 1.09, 1.92, 3.48, 0.08, 0.09, 0.69, 0.00, 0.53, }, 
		{6.40, 0.00, 4.28, 2.02, 39.32, 6.79, 12.82, 39.82, 11.30, 25.95, 1.53, 31.98, 2.42, 21.14, }, 
		{1.03, 4.28, 0.00, 6.82, 9.51, 1.71, 51.16, 105.15, 18.68, 5.13, 1.57, 0.50, 0.00, 6.82, }, 
		{0.23, 2.02, 6.82, 0.00, 2.67, 3.06, 2.59, 5.70, 4.00, 3.50, 2.38, 6.32, 0.04, 6.95, }, 
		{0.37, 39.32, 9.51, 2.67, 0.00, 3.72, 16.40, 45.22, 24.51, 20.13, 0.35, 51.95, 4.99, 20.45, }, 
		{0.02, 6.79, 1.71, 3.06, 3.72, 0.00, 5.26, 2.67, 3.84, 6.68, 0.05, 1.08, 0.03, 7.28, }, 
		{1.09, 12.82, 51.16, 2.59, 16.40, 5.26, 0.00, 105.59, 22.44, 13.47, 0.80, 38.45, 0.17, 19.64, }, 
		{1.92, 39.82, 105.15, 5.70, 45.22, 2.67, 105.59, 0.00, 52.49, 45.04, 6.41, 44.60, 2.77, 21.32, }, 
		{3.48, 11.30, 18.68, 4.00, 24.51, 3.84, 22.44, 52.49, 0.00, 23.23, 7.55, 78.52, 11.67, 2.72, }, 
		{0.08, 25.95, 5.13, 3.50, 20.13, 6.68, 13.47, 45.04, 23.23, 0.00, 6.71, 20.01, 2.96, 0.38, }, 
		{0.09, 1.53, 1.57, 2.38, 0.35, 0.05, 0.80, 6.41, 7.55, 6.71, 0.00, 5.67, 0.03, 2.25, }, 
		{0.69, 31.98, 0.50, 6.32, 51.95, 1.08, 38.45, 44.60, 78.52, 20.01, 5.67, 0.00, 0.51, 0.27, }, 
		{0.00, 2.42, 0.00, 0.04, 4.99, 0.03, 0.17, 2.77, 11.67, 2.96, 0.03, 0.51, 0.00, 5.38, }, 
		{0.53, 21.14, 6.82, 6.95, 20.45, 7.28, 19.64, 21.32, 2.72, 0.38, 2.25, 0.27, 5.38, 0.00, } 

	};
	
	
	public static final Double[][] TRAFFICMATRIX_NSFNET_BIDIRECTIONAL_NORM = new Double[][]{
		{0.000000, 0.060612, 0.009755, 0.002131, 0.003457, 0.000189, 0.010323, 0.018184, 0.032958, 0.000710, 0.000805, 0.006487, 0.000000, 0.005019 }, 
		{0.060612, 0.000000, 0.040534, 0.019083, 0.372336, 0.064305, 0.121413, 0.377072, 0.106970, 0.245762, 0.014443, 0.302870, 0.022871, 0.200208 }, 
		{0.009755, 0.040534, 0.000000, 0.064589, 0.090065, 0.016195, 0.484468, 0.995833, 0.176863, 0.048584, 0.014821, 0.004735, 0.000000, 0.064542 }, 
		{0.002131, 0.019083, 0.064589, 0.000000, 0.025239, 0.028980, 0.024481, 0.053982, 0.037882, 0.033100, 0.022540, 0.059807, 0.000379, 0.065821 }, 
		{0.003457, 0.372336, 0.090065, 0.025239, 0.000000, 0.035183, 0.155318, 0.428213, 0.232077, 0.190643, 0.003267, 0.491950, 0.047211, 0.193674 }, 
		{0.000189, 0.064305, 0.016195, 0.028980, 0.035183, 0.000000, 0.049768, 0.025286, 0.036367, 0.063216, 0.000474, 0.010228, 0.000284, 0.068899 }, 
		{0.010323, 0.121413, 0.484468, 0.024481, 0.155318, 0.049768, 0.000000, 1.000000, 0.212520, 0.127522, 0.007529, 0.364097, 0.001563, 0.186002 }, 
		{0.018184, 0.377072, 0.995833, 0.053982, 0.428213, 0.025286, 1.000000, 0.000000, 0.497064, 0.426556, 0.060707, 0.422388, 0.026186, 0.201913 }, 
		{0.032958, 0.106970, 0.176863, 0.037882, 0.232077, 0.036367, 0.212520, 0.497064, 0.000000, 0.220002, 0.071503, 0.743584, 0.110522, 0.025713 }, 
		{0.000710, 0.245762, 0.048584, 0.033100, 0.190643, 0.063216, 0.127522, 0.426556, 0.220002, 0.000000, 0.063548, 0.189459, 0.027986, 0.003599 }, 
		{0.000805, 0.014443, 0.014821, 0.022540, 0.003267, 0.000474, 0.007529, 0.060707, 0.071503, 0.063548, 0.000000, 0.053698, 0.000284, 0.021261 }, 
		{0.006487, 0.302870, 0.004735, 0.059807, 0.491950, 0.010228, 0.364097, 0.422388, 0.743584, 0.189459, 0.053698, 0.000000, 0.004783, 0.002557 }, 
		{0.000000, 0.022871, 0.000000, 0.000379, 0.047211, 0.000284, 0.001563, 0.026186, 0.110522, 0.027986, 0.000284, 0.004783, 0.000000, 0.050904 }, 
		{0.005019, 0.200208, 0.064542, 0.065821, 0.193674, 0.068899, 0.186002, 0.201913, 0.025713, 0.003599, 0.021261, 0.002557, 0.050904, 0.000000 } 
	};
	
	public static final Double[][] TRAFFICMATRIX_UNIFORM = new Double[][]{
		{0.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000 },
		{1.000000, 0.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000 },
		{1.000000, 1.000000, 0.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000 },
		{1.000000, 1.000000, 1.000000, 0.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000 },
		{1.000000, 1.000000, 1.000000, 1.000000, 0.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000 },
		{1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 0.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000 },
		{1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 0.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000 },
		{1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 0.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000 },
		{1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 0.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000 },
		{1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 0.000000, 1.000000, 1.000000, 1.000000, 1.000000 },
		{1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 0.000000, 1.000000, 1.000000, 1.000000 },
		{1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 0.000000, 1.000000, 1.000000 },
		{1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 0.000000, 1.000000 },
		{1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 0.000000 },
	};

//	public static Double[][] TRAFFICMATRIX = TRAFFICMATRIX_NSFNET_BIDIRECTIONAL_NORM;
	
	public static Double[][] TRAFFICMATRIX = TRAFFICMATRIX_UNIFORM;
	
	static {
//		for (int i = 0; i < 14; i++) {
//			for (int j = i + 1; j < 14; j++) {
//			TRAFFICMATRIX[i][j] = 1.0;
//			TRAFFICMATRIX[j][i] = 1.0;
//			}
//		}
//		System.out.println("Done");
//		double totalLoad = 100;
//		double usedLoad = 0;
//		double percLoadFirstNode = 0.2;
//		for (int i = 0; i < 14; i++) {
//			for (int j = i+1; j < 14; j++) {
//			TRAFFICMATRIX[i][j] = Math.random();
//			TRAFFICMATRIX[j][i] = TRAFFICMATRIX[i][j];
//			}
//		}
//		usedLoad = 0;
//		for (int i = 1; i < 14; i++) {
//			for (int j = i + 1; j < 14; j++) {
//			TRAFFICMATRIX[i][j] = Math.random() * (totalLoad * (1 - percLoadFirstNode) - usedLoad)/(2 * 14 - i);
//			TRAFFICMATRIX[j][i] = TRAFFICMATRIX[i][j];
//			usedLoad += TRAFFICMATRIX[i][j];
//			}
//		}
	}
	
	public static void main(String[] args) {
		double max = 0;
		for (int i = 0; i < TRAFFICMATRIX.length; i++) {
			TRAFFICMATRIX[i][i] = 0.0; 
			for (int j = i+1; j < TRAFFICMATRIX.length; j++) {
				TRAFFICMATRIX[i][j] = (TRAFFICMATRIX_NSFNET_MBRM96[i][j] + TRAFFICMATRIX_NSFNET_MBRM96[j][i])/2;
				TRAFFICMATRIX[j][i] = TRAFFICMATRIX[i][j];
				if (TRAFFICMATRIX[j][i] > max) {
					max = TRAFFICMATRIX[j][i];
				}
			}
		}
		for (int i = 0; i < TRAFFICMATRIX.length; i++) {
			System.out.print("{");
			for (int j = 0; j < TRAFFICMATRIX.length; j++) {
				System.out.print(String.format("%.6f", TRAFFICMATRIX[i][j]/max).replace(",", ".") + ", ");
			}
			System.out.println("}, ");
		}
	}
	
	private Vector<Vector<Double>> trafficMatrix = new Vector<Vector<Double>>();
	// stores the arrivals times between the nodes
	private Vector<Vector<Double>> arrivalTimesMatrix = new Vector<Vector<Double>>();
	private int numberOfNodesInNetwork; // number of nodes in network
	private double totalNetworkLoad; // current total network load

	private void scaleTrafficMatrix(double desiredTotalLoad) {

		double scaleFactor_loc = desiredTotalLoad / totalNetworkLoad;
		for (int i = 0; i < numberOfNodesInNetwork; i++) {
			// atribui valores para a matriz de trafego
			for (int j = 0; j < numberOfNodesInNetwork; j++){
				trafficMatrix.get(i).set(j, trafficMatrix.get(i).get(j) * scaleFactor_loc);
			}
		}
//		System.out.println("Fim scaleTrafficMatrix_mpu");
	}
	
	private void setArrivalTimesMatrix_mpu(int i, int j, double valor) {
		arrivalTimesMatrix.get(i).set(j, valor);
	}

	public void generateCallRequisition() {

		int nextSource = 0;
		int nextDestination = 0;
		double time = MAX_TIME;

		int i, j;
		for (i = 0; i < numberOfNodesInNetwork; i++) {
			// atribui valores para a matriz de trafego
			for (j = 0; j < numberOfNodesInNetwork; j++) {
				if ((arrivalTimesMatrix.get(i).get(j) < time) && (i != j)) {
					nextSource = i;
					nextDestination = j;
					time = arrivalTimesMatrix.get(i).get(j);
				}
			}
		}
		setArrivalTimesMatrix_mpu(nextSource, nextDestination, getCurrentTime() + getRandomExp(trafficMatrix.get(nextSource).get(nextDestination) / MEAN_DURATION));

		setNextSourceNode(nextSource);
		setNextDestinationNode(nextDestination);
		setCurrentTime(time);
		setDuration(getRandomExp(1 / MEAN_DURATION));
//		System.out.printf("call (%d, %d) \n", nextSource, nextDestination);
	}

	public void resetTime_mpu() {

		for (int i = 0; i < numberOfNodesInNetwork; i++) {
			// atribui valores para a matriz de trafego
			for (int j = 0; j < numberOfNodesInNetwork; j++) {
				if (i != j) {
					// arrivalTimesMatrix_ppr[i][j] -= getCurrentTime_mpu();
					setArrivalTimesMatrix_mpu(i, j, arrivalTimesMatrix.get(i).get(j) - this.getCurrentTime());
				}
			}
		}
	}

	public void initThisScheduler_mpu(Vector<Vector<Double>> trafficMatrix_par, int numberOfNodesInNetwork_par) {
		Vector<Double> vectorLine_loc = null;
		Vector<Double> vectorLineA = null;
		numberOfNodesInNetwork = numberOfNodesInNetwork_par;
		// cria matriz com valores nulos
		for (int i = 0; i < numberOfNodesInNetwork_par; i++) {
			for (int j = 0; j < numberOfNodesInNetwork_par; j++) {
				vectorLine_loc = new Vector<Double>();
				vectorLineA = new Vector<Double>();
				for (int k = 0; k < numberOfNodesInNetwork_par; k++) {
					vectorLine_loc.add(0.0);
					vectorLineA.add(0.0);	
				}
				trafficMatrix.add(vectorLine_loc);
				arrivalTimesMatrix.add(vectorLineA);
			}
		}

		for (int i = 0; i < numberOfNodesInNetwork_par; i++) {
			// atribui valores para a matriz de trafego
			for (int j = 0; j < numberOfNodesInNetwork_par; j++) {
				if (i != j){
					trafficMatrix.get(i).set(j, trafficMatrix_par.get(i).get(j));
				} else {
					trafficMatrix.get(i).set(j, 0.0);
				}
			}
		}

		for (int i = 0; i < numberOfNodesInNetwork; i++) {
			// atribui valores para a matriz de chegadas
			for (int j = 0; j < numberOfNodesInNetwork; j++) {
				if (trafficMatrix.get(i).get(j).equals(0.0)) {
					arrivalTimesMatrix.get(i).set(j, MAX_TIME);
				} else {
					arrivalTimesMatrix.get(i).set(j, getRandomExp(trafficMatrix.get(i).get(j) / MEAN_DURATION));
				}
			}
		}
		
		evaluateTotalNetworkLoad_mpr();

	}

	public CallSchedulerNonUniformHub(Vector<Vector<Double>> trafficMatrix_par, int numberOfNodesInNetwork_par) {
		initThisScheduler_mpu(trafficMatrix_par, numberOfNodesInNetwork_par);

	}

	public CallSchedulerNonUniformHub(Vector<Vector<Double>> trafficMatrix_par, int numberOfNodesInNetwork_par,
			double totalNetworkLoad_par) {
		this(trafficMatrix_par, numberOfNodesInNetwork_par);
		scaleTrafficMatrix(totalNetworkLoad_par);
	}

	public CallSchedulerNonUniformHub(int numberOfNodesInNetwork_par, double totalNetworkLoad_par) {
		Vector<Double> vectorLine_loc = null;
		Vector<Vector<Double>> tempMatrix_loc = new Vector<Vector<Double>>();
		// cria matriz com valores nulos
		for (int i = 0; i < numberOfNodesInNetwork_par; i++) {
			for (int j = 0; j < numberOfNodesInNetwork_par; j++) {
				vectorLine_loc = new Vector<Double>();
				for (int k = 0; k < numberOfNodesInNetwork_par; k++) {
					vectorLine_loc.add(0.0);	
				}
				tempMatrix_loc.add(vectorLine_loc);
			}
		}
		// cria matriz de trafego a partir da matriz
		// aleatoria definida acima
		for (int i = 0; i < numberOfNodesInNetwork_par; i++) {
			for (int j = 0; j < numberOfNodesInNetwork_par; j++) {
				if (i != j) {
					tempMatrix_loc.get(i).set(j, TRAFFICMATRIX[i][j]);
				} else {
					tempMatrix_loc.get(i).set(j, 0.0);
				}

			}
		}
		// monta a matriz apartir da ante

		initThisScheduler_mpu(tempMatrix_loc, numberOfNodesInNetwork_par);
		scaleTrafficMatrix(totalNetworkLoad_par);

	}

	void evaluateTotalNetworkLoad_mpr() {
		double soma_loc = 0.0;
		for (int i = 0; i < numberOfNodesInNetwork; i++)
			// atribui valores para a matriz de trafego
			for (int j = 0; j < numberOfNodesInNetwork; j++)
				soma_loc += trafficMatrix.get(i).get(j);

		totalNetworkLoad = soma_loc;
//		System.out.println("LOAD = " + totalNetworkLoad);
	}
}
