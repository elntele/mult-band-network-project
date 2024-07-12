/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE e Padtec
 * ****************************************************************************
 * Projeto: Planejador de Redes �pticas Padtec � M�dulo Kernel
 * Arquivo: ACMNTExperiment.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo		20/01/2013	Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.experiments;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import br.cns24.TMetric;
import br.cns24.models.TModel;
import br.cns24.persistence.SimonSolutionDao;
import br.cns24.transformations.DegreeMatrix;
import br.cns24.util.FormatUtils;

/**
 * @author Danilo
 * 
 * @since 20/01/2013
 */
public class ACMNTExperiment {
	private static final double[][] position = { { -85.692672, 14.728428 }, { -86.51664, -31.619772 },
			{ -69.213312, 34.606656 }, { -51.189012, 0.51498 }, { -29.044872, 3.810852 }, { 4.531824, 46.142208 },
			{ 0, 0 }, { 28.3239, 3.192876 }, { 56.13282, 1.853928 }, { 41.610384, 30.177828 },
			{ 43.464312, -6.282756 }, { 67.97736, -7.003728 }, { 74.15712, 2.677896 }, { 66.741408, 8.033688 } };

	public static double[][] transform(Integer[][] matrix, double[][] distance) {
		Integer[][] degree = DegreeMatrix.getInstance().transform(matrix);
		double[][] laplacian = new double[matrix.length][matrix.length];

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				laplacian[i][j] = degree[i][j] - matrix[i][j] * distance[i][j];
			}
		}

		return laplacian;
	}

	public static double calculate(Integer[][] matrix, double[][] distances) {
		double[][] laplacian = transform(matrix, distances);
		double[][] realValues = new double[matrix.length][matrix.length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				realValues[i][j] = laplacian[i][j];
			}
		}
		try {
			RealMatrix rm = new Array2DRowRealMatrix(realValues);
			EigenDecomposition solver = new EigenDecomposition(rm, 0);
			double[] autovalores = solver.getRealEigenvalues();
			double aux;
			for (int i = 0; i < autovalores.length; i++) {
				for (int j = i; j < autovalores.length; j++) {
					if (autovalores[j] < autovalores[i]) {
						aux = autovalores[i];
						autovalores[i] = autovalores[j];
						autovalores[j] = aux;
					}
				}
			}
			return autovalores[1];
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static void main(String[] args) {
		showResults();
	}

	private static void showResults() {
		double[][] distances = new double[14][14];
		double maior = 0;
		for (int i = 0; i < distances.length; i++) {
			for (int j = 0; j < distances.length; j++) {
				distances[i][j] = Math.sqrt((position[i][0] - position[j][0]) * (position[i][0] - position[j][0])
						+ (position[i][1] - position[j][1]) * (position[i][1] - position[j][1]));
				if (distances[i][j] > maior) {
					maior = distances[i][j];
				}
			}
		}
		for (int i = 0; i < distances.length; i++) {
			for (int j = 0; j < distances.length; j++) {
				distances[i][j] /= maior;
			}
		}

		try {
			List<TMetric> metricas = new Vector<>();
			metricas.add(TMetric.ALGEBRAIC_CONNECTIVITY);
			metricas.add(TMetric.AVERAGE_DEGREE);
			metricas.add(TMetric.AVERAGE_PATH_LENGTH);
			metricas.add(TMetric.DENSITY);
			metricas.add(TMetric.NATURAL_CONNECTIVITY);

			List<Integer[][]> list = SimonSolutionDao.getInstance().readNetworksFromFileObject(
					"C:\\Temp\\exp_20120814\\_nsgaii_C2_M10_50_1,0000_0,0600_0.400_var.txt");
			List<double[]> listObj = SimonSolutionDao.getInstance().readObjectivesFromFile(
					"C:\\Temp\\exp_20120814\\_nsgaii_C2_M10_50_1,0000_0,0600_0.400_pf.txt");
			ComplexNetwork network;
			int i = 0;
			int redesDistintas = 0;
			double[] meanMetrics = new double[TMetric.values().length];
			for (Integer[][] matrix : list) {
				if (i > 0 && listObj.get(i)[0] == listObj.get(i - 1)[0] && listObj.get(i)[1] == listObj.get(i - 1)[1]) {
					i++;
					continue;
				}
				network = new ComplexNetwork(i, matrix, new double[14][2], TModel.CUSTOM);
				network.evaluate();

				System.out.print("REDE " + redesDistintas + ";");
				int j = 0;
				System.out.printf("%.6f;", listObj.get(i)[0]);
				System.out.printf("%.2f;", listObj.get(i)[1]);
				System.out.printf("%.2f;", calculate(matrix, distances));
				for (TMetric metric : metricas) {
					System.out.print(FormatUtils.getInstance().getValue(network.getMetricValues().get(metric)) + ";");
					meanMetrics[j] += network.getMetricValues().get(metric);
					j++;
				}
				System.out.println();
				i++;
				redesDistintas++;
			}
			int k = 0;
			System.out.println("**************************************************");
			System.out.println("VALOR M�DIO DAS métricaS");
			System.out.println("**************************************************");
			network = new ComplexNetwork(i, list.get(0), new double[14][2], TModel.CUSTOM);
			network.evaluate();
			for (TMetric metric : metricas) {
				meanMetrics[k] /= list.size();
				System.out.println(metric.toString() + " = " + FormatUtils.getInstance().getValue(meanMetrics[k]));
				k++;
			}
			assertNotNull(list);
		} catch (FileNotFoundException e) {
			fail("Arquivo n�o encontrado");
		} catch (Exception e) {
			fail("Erro de I/O");
		}
	}

	private static void showResults1() {
		double[][] distances = new double[14][14];
		double maior = 0;
		for (int i = 0; i < distances.length; i++) {
			for (int j = 0; j < distances.length; j++) {
				distances[i][j] = Math.sqrt((position[i][0] - position[j][0]) * (position[i][0] - position[j][0])
						+ (position[i][1] - position[j][1]) * (position[i][1] - position[j][1]));
				if (distances[i][j] > maior) {
					maior = distances[i][j];
				}
			}
		}
		for (int i = 0; i < distances.length; i++) {
			for (int j = 0; j < distances.length; j++) {
				distances[i][j] /= maior;
			}
		}

		try {
			List<TMetric> metricas = new Vector<>();
			metricas.add(TMetric.ALGEBRAIC_CONNECTIVITY);
			metricas.add(TMetric.AVERAGE_DEGREE);
			metricas.add(TMetric.AVERAGE_PATH_LENGTH);
			metricas.add(TMetric.DENSITY);
			metricas.add(TMetric.NATURAL_CONNECTIVITY);

			List<Integer[][]> list = SimonSolutionDao.getInstance().readNetworksFromFileObject(
					"C:\\Temp\\exp_20120815\\_nsgaii_C2_M10_50_1,0000_0,0600_0.400_var.txt");
			List<double[]> listObj = SimonSolutionDao.getInstance().readObjectivesFromFile(
					"C:\\Temp\\exp_20120815\\_nsgaii_C2_M10_50_1,0000_0,0600_0.400_pf.txt");
			ComplexNetwork network;
			int i = 0;
			int redesDistintas = 0;
			double[] meanMetrics = new double[TMetric.values().length];
			for (Integer[][] matrix : list) {
				if (i > 0 && listObj.get(i)[0] == listObj.get(i - 1)[0] && listObj.get(i)[1] == listObj.get(i - 1)[1]) {
					i++;
					continue;
				}
				network = new ComplexNetwork(i, matrix, new double[14][2], TModel.CUSTOM);
				network.evaluate();

				System.out.println("**************************************************");
				System.out.println("REDE " + redesDistintas + ":");
				System.out.println("**************************************************");
				int j = 0;
				System.out.printf("BP = %.6f\n", listObj.get(i)[0]);
				System.out.printf("Custo = %.2f\n", listObj.get(i)[1]);
				System.out.printf("AC Distance = %.2f\n", calculate(matrix, distances));
				for (TMetric metric : metricas) {
					System.out.println(metric.toString() + " = "
							+ FormatUtils.getInstance().getValue(network.getMetricValues().get(metric)));
					meanMetrics[j] += network.getMetricValues().get(metric);
					j++;
				}
				i++;
				redesDistintas++;
			}
			int k = 0;
			System.out.println("**************************************************");
			System.out.println("VALOR M�DIO DAS métricaS");
			System.out.println("**************************************************");
			network = new ComplexNetwork(i, list.get(0), new double[14][2], TModel.CUSTOM);
			network.evaluate();
			for (TMetric metric : metricas) {
				meanMetrics[k] /= list.size();
				System.out.println(metric.toString() + " = " + FormatUtils.getInstance().getValue(meanMetrics[k]));
				k++;
			}
			assertNotNull(list);
		} catch (FileNotFoundException e) {
			fail("Arquivo n�o encontrado");
		} catch (Exception e) {
			fail("Erro de I/O");
		}
	}
}
