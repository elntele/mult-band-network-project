/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: PACustom.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	11/01/2014		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.models;

/**
 * 
 * @author Danilo
 * @since 11/01/2014
 */
public class PACustom extends GenerativeProcedure {
	private double density;

	private double g;

	private double a;

	private Double[][] mainPA;

	private Double[][] secundaryPA;

	public PACustom(double density, Double[][] mainPA, Double[][] secundaryPA, double a) {
		this.density = density;
		this.g = 1;
		this.a = a;
		this.mainPA = mainPA;
		this.secundaryPA = secundaryPA;
	}

	public void fill(Integer[][] m) {
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[i].length; j++) {
				if (m[i][j] == null) {
					m[i][j] = 0;
				}
			}
		}
	}

	@Override
	public Integer[][] transform(Integer[][] matrix) {
		return null;
	}

	@Override
	public Integer[][] grow(Integer[][] matrix, int numNodes) {
		double[] cdf = new double[numNodes];
		double[] createdMainPA = new double[mainPA.length];
		double[] createdSecPA = new double[secundaryPA.length];

		Integer[][] nMatrix = new Integer[numNodes][numNodes];
		int m0 = 2;

		int firstIndex = -1;
		int secondIndex = -1;
		double firstMax = 0;
		double secondMax = 0;
		double aux = 0;

		for (int i = 0; i < numNodes; i++) {
			aux = 0;
			for (int j = i + 1; j < numNodes; j++) {
				aux += mainPA[i][j];
			}
			if (aux > firstMax) {
				firstIndex = i;
				firstMax = aux;
			}
		}
		for (int i = 0; i < numNodes; i++) {
			aux = 0;
			for (int j = i + 1; j < numNodes; j++) {
				aux += mainPA[i][j];
			}
			if (aux > secondMax && i != firstIndex) {
				secondIndex = i;
				secondMax = aux;
			}
		}

		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++) {
				nMatrix[i][j] = 0;
			}
		}
		nMatrix[firstIndex][secondIndex] = 1;
		nMatrix[secondIndex][firstIndex] = 1;

		double r;
		cdf = new double[numNodes];
		int numCreatedLinks = (m0 * (m0 - 1)) / 2;
		int deltaM = 2;

		createdMainPA[firstIndex] = mainPA[firstIndex][secondIndex];
		createdMainPA[secondIndex] = mainPA[firstIndex][secondIndex];

		createdSecPA[firstIndex] = secundaryPA[firstIndex][secondIndex];
		createdSecPA[secondIndex] = secundaryPA[firstIndex][secondIndex];

		double sumMainPA = mainPA[firstIndex][secondIndex];
		double sumSecPA = secundaryPA[firstIndex][secondIndex];

		lblExt: for (int n = 2; n < numNodes; n++) {
			deltaM = (int) ((((n) * (n + 1)) / 2) * density) - numCreatedLinks;
			if (deltaM < 2) {
				deltaM = 2;
			}
			cdf[0] = (createdMainPA[0] + a * secundaryPA[0][n]) / ((2.0 * sumMainPA) + a * n * secundaryPA[0][n]);
			for (int i = 1; i < n; i++) {
				cdf[i] = cdf[i - 1] + (createdMainPA[i] + a * secundaryPA[i][n]) / ((2.0 * sumMainPA) + a * n * secundaryPA[i][n]);
			}
			int numLinks = Math.min(deltaM, n - 1);
			boolean linked = false;
			for (int i = 0; i < numLinks; i++) {
				if (n == numNodes && numCreatedLinks / ((numNodes * (numNodes - 1)) / 2.0) >= density) {
					break lblExt;
				}
				linked = false;
				r = Math.random();
				for (int j = 0; j < n; j++) {
					if (((j == 0 && r < cdf[0]) || (r < cdf[j] && r >= cdf[j - 1])) && nMatrix[n][j] != 1) {
						nMatrix[n][j] = 1;
						nMatrix[j][n] = 1;
						createdMainPA[n] += mainPA[n][j];
						createdMainPA[j] += mainPA[n][j];

						createdSecPA[n] += secundaryPA[n][j];
						createdSecPA[j] += secundaryPA[n][j];

						sumMainPA += mainPA[n][j];
						sumSecPA += secundaryPA[n][j];

						numCreatedLinks++;
						linked = true;
						break;
					}
				}
				if (!linked) {
					if (nMatrix[n][firstIndex] != 1) {
						nMatrix[n][firstIndex] = 1;
						nMatrix[firstIndex][n] = 1;
						createdMainPA[n] += mainPA[n][firstIndex];
						createdMainPA[firstIndex] += mainPA[n][firstIndex];

						createdSecPA[n] += secundaryPA[n][firstIndex];
						createdSecPA[firstIndex] += secundaryPA[n][firstIndex];

						sumMainPA += mainPA[n][firstIndex];
						sumSecPA += secundaryPA[n][firstIndex];

						numCreatedLinks++;
						linked = true;
					}
					if (!linked && nMatrix[n][secondIndex] != 1) {
						nMatrix[n][secondIndex] = 1;
						nMatrix[secondIndex][n] = 1;
						createdMainPA[n] += mainPA[n][secondIndex];
						createdMainPA[secondIndex] += mainPA[n][secondIndex];

						createdSecPA[n] += secundaryPA[n][secondIndex];
						createdSecPA[secondIndex] += secundaryPA[n][secondIndex];

						sumMainPA += mainPA[n][secondIndex];
						sumSecPA += secundaryPA[n][secondIndex];

						numCreatedLinks++;
						linked = true;
					}
					if (!linked) {
						for (int j = 0; j < n; j++) {
							if (nMatrix[n][j] != 1) {
								nMatrix[n][j] = 1;
								nMatrix[j][n] = 1;
								createdMainPA[n] += mainPA[n][j];
								createdMainPA[j] += mainPA[n][j];

								createdSecPA[n] += secundaryPA[n][j];
								createdSecPA[j] += secundaryPA[n][j];

								sumMainPA += mainPA[n][j];
								sumSecPA += secundaryPA[n][j];

								numCreatedLinks++;
								break;
							}
						}
					}
				}
			}

		}
		return nMatrix;
	}

	@Override
	public String name() {
		return TModel.CUSTOM_POWER_LAW.toString();
	}

	/**
	 * @return o valor do atributo density
	 */
	public double getDensity() {
		return density;
	}

	/**
	 * Altera o valor do atributo density
	 * 
	 * @param density
	 *            O valor para setar em density
	 */
	public void setDensity(double density) {
		this.density = density;
	}

	public static void main(String[] args) {
		Double[][] traffic = new Double[][] {
				{ 0.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 5.000000, 1.000000, 1.000000, 1.000000,
						1.000000, 1.000000, 1.000000, 1.000000 },
				{ 1.000000, 0.000000, 1.000000, 1.000000, 1.000000, 1.000000, 5.000000, 1.000000, 1.000000, 1.000000,
						1.000000, 1.000000, 1.000000, 1.000000 },
				{ 1.000000, 1.000000, 0.000000, 1.000000, 1.000000, 1.000000, 5.000000, 1.000000, 1.000000, 1.000000,
						1.000000, 1.000000, 1.000000, 1.000000 },
				{ 1.000000, 1.000000, 1.000000, 0.000000, 1.000000, 1.000000, 5.000000, 1.000000, 1.000000, 1.000000,
						1.000000, 1.000000, 1.000000, 1.000000 },
				{ 1.000000, 1.000000, 1.000000, 1.000000, 0.000000, 1.000000, 5.000000, 1.000000, 1.000000, 1.000000,
						1.000000, 1.000000, 1.000000, 1.000000 },
				{ 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 0.000000, 5.000000, 1.000000, 1.000000, 1.000000,
						1.000000, 1.000000, 1.000000, 1.000000 },
				{ 5.000000, 5.000000, 5.000000, 5.000000, 5.000000, 5.000000, 0.000000, 5.000000, 5.000000, 5.000000,
						5.000000, 5.000000, 5.000000, 5.000000 },
				{ 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 5.000000, 0.000000, 1.000000, 1.000000,
						1.000000, 1.000000, 1.000000, 1.000000 },
				{ 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 5.000000, 1.000000, 0.000000, 1.000000,
						1.000000, 1.000000, 1.000000, 1.000000 },
				{ 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 5.000000, 1.000000, 1.000000, 0.000000,
						1.000000, 1.000000, 1.000000, 1.000000 },
				{ 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 5.000000, 1.000000, 1.000000, 1.000000,
						0.000000, 1.000000, 1.000000, 1.000000 },
				{ 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 5.000000, 1.000000, 1.000000, 1.000000,
						1.000000, 0.000000, 1.000000, 1.000000 },
				{ 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 5.000000, 1.000000, 1.000000, 1.000000,
						1.000000, 1.000000, 0.000000, 1.000000 },
				{ 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 5.000000, 1.000000, 1.000000, 1.000000,
						1.000000, 1.000000, 1.000000, 0.000000 }, };
		Double[][] distance = new Double[][] {
				{ 0.0, 46.35552358139237, 25.820791127104993, 37.3165466977359, 57.690265301485454, 95.53682668135056,
						86.94918415862664, 114.59863721081453, 142.40864767032957, 128.23709302420707,
						130.8548675787726, 155.19911513694063, 160.30337277776252, 152.58102204767798 },
				{ 46.35552358139237, 0.0, 68.44957944001386, 47.75650307767402, 67.51542960001957, 119.73616133098511,
						92.11372850027071, 120.00112536248776, 146.52425406730995, 142.25145920115045,
						132.42738486661494, 156.44278717221172, 164.29360055332594, 158.304852 },
				{ 25.820791127104993, 68.44957944001386, 0.0, 38.56316588376758, 50.61506807305524, 74.64190541246386,
						77.38283528995096, 102.47113397743456, 129.55459852434188, 110.91215493603035,
						119.86738907639193, 143.36214472600165, 146.88269634985676, 138.52728438328467 },
				{ 37.3165466977359, 47.75650307767402, 38.56316588376758, 0.0, 22.38807067569655, 72.0184386077821,
						51.191602377114, 79.5579933237796, 107.33018403777629, 97.42490672018076, 94.89710722131983,
						119.40332987664812, 125.36479175982579, 118.16985627208685 },
				{ 57.690265301485454, 67.51542960001957, 50.61506807305524, 22.38807067569655, 0.0, 54.030900557691545,
						29.293807920143944, 57.372100319968766, 85.20016881432008, 75.41473744441542,
						73.20835120943184, 97.62309482309104, 103.20821063300149, 95.87931883425797 },
				{ 95.53682668135056, 119.73616133098511, 74.64190541246386, 72.0184386077821, 54.030900557691545, 0.0,
						46.36421883179139, 49.09896128876862, 68.00084215324407, 40.3693081493602, 65.30019504007197,
						82.76367893975831, 82.078183829328, 72.95403784605384 },
				{ 86.94918415862664, 92.11372850027071, 77.38283528995096, 51.191602377114, 29.293807920143944,
						46.36421883179139, 0.0, 28.50329399142099, 56.163426980389865, 51.40160852954935,
						43.916049920147415, 68.33720566768577, 74.20545514772628, 67.22317818062315 },
				{ 114.59863721081453, 120.00112536248776, 102.47113397743456, 79.5579933237796, 57.372100319968766,
						49.09896128876862, 28.50329399142099, 0.0, 27.841135273424182, 30.07853539527083,
						17.861121950458994, 40.943468625708995, 45.836113054760666, 38.7212910651157 },
				{ 142.40864767032957, 146.52425406730995, 129.55459852434188, 107.33018403777629, 85.20016881432008,
						68.00084215324407, 56.163426980389865, 27.841135273424182, 0.0, 31.82993023215879,
						15.0564511576241, 14.790239944839843, 18.04312372498244, 12.2772787298873, },
				{ 128.23709302420707, 142.25145920115045, 110.91215493603035, 97.42490672018076, 75.41473744441542,
						40.3693081493602, 51.40160852954935, 30.07853539527083, 31.82993023215879, 0.0,
						36.507687336371234, 45.58163588514251, 42.6091103434268, 33.49524299998697 },
				{ 130.8548675787726, 132.42738486661494, 119.86738907639193, 94.89710722131983, 73.20835120943184,
						65.30019504007197, 43.916049920147415, 17.861121950458994, 15.0564511576241,
						36.507687336371234, 0.0, 24.523648237468425, 31.97407930167761, 27.32734467522142 },
				{ 155.19911513694063, 156.44278717221172, 143.36214472600165, 119.40332987664812, 97.62309482309104,
						82.76367893975831, 68.33720566768577, 40.943468625708995, 14.790239944839843,
						45.58163588514251, 24.523648237468425, 0.0, 11.485785864927832, 15.088123054355037 },
				{ 160.30337277776252, 164.29360055332594, 146.88269634985676, 125.36479175982579, 103.20821063300149,
						82.078183829328, 74.20545514772628, 45.836113054760666, 18.04312372498244, 42.6091103434268,
						31.97407930167761, 11.485785864927832, 0.0, 9.147529306550922 },
				{ 152.58102204767798, 158.304852, 138.52728438328467, 118.16985627208685, 95.87931883425797,
						72.95403784605384, 67.22317818062315, 38.7212910651157, 12.27727872988734, 33.49524299998697,
						27.32734467522142, 15.088123054355037, 9.147529306550922, 0.0 } };

		for (int i = 0; i < 14; i++) {
			for (int j = 0; j < 14; j++) {
				distance[i][j] = 0.0;
			}
		}

		PACustom pl = new PACustom(0.2, traffic, distance, 1);
		Integer[][] am = pl.grow(null, 14);

		for (int i = 0; i < am.length; i++) {
			for (int j = 0; j < am.length; j++) {
				System.out.print(am[i][j] + " ");
			}
			System.out.println("");
		}
	}

}
