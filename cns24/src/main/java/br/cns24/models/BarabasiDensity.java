package br.cns24.models;

import br.cns24.transformations.DegreeMatrix;

public class BarabasiDensity extends GenerativeProcedure {
	private double density;

	private double[] cdf;
	
	private double exponent = 1.0;

	public BarabasiDensity(double density) {
		this.density = density;
	}

	public BarabasiDensity(double density, double exponent) {
		this.density = density;
		this.exponent = exponent;
	}

	@Override
	public Integer[][] transform(Integer[][] matrix) {
		return grow(matrix, matrix.length);
	}

	@Override
	public Integer[][] grow(Integer[][] matrix, int numNodes) {
		Integer[][] nMatrix = new Integer[numNodes][numNodes];
		int m0 = 3;
		for (int i = 0; i < numNodes; i++) {
			nMatrix[i][i] = 0;
			for (int j = i + 1; j < numNodes; j++) {
				nMatrix[i][j] = 0;
				nMatrix[j][i] = 0;
				if (j < m0 && i < m0) {
					nMatrix[i][j] = 1;
					nMatrix[j][i] = 1;
				}
			}
		}
		double r;
		cdf = new double[numNodes];
		int numCreatedLinks = (m0 * (m0 - 1)) / 2;
		Integer[][] degree = null;
		int deltaM = 2;
		degree = DegreeMatrix.getInstance().transform(nMatrix);
		double sumToCdf = 0; 
		
		lblExt: for (int n = 3; n < numNodes; n++) {
			if (numCreatedLinks >= (numNodes * (numNodes-1)/2.0) * density){
				break;
			}
			sumToCdf = 0;
			for (int i = 0;  i < degree.length; i++){
				sumToCdf += Math.pow(degree[i][i], exponent);
			}
			cdf[0] = Math.pow(degree[0][0], exponent) / sumToCdf;
			deltaM = (int) ((((n) * (n + 1)) / 2) * density) - numCreatedLinks;
			if (deltaM < 1) {
				deltaM = 1;
			}
			for (int i = 1; i < n; i++) {
				cdf[i] = cdf[i - 1] + Math.pow(degree[i][i], exponent) / sumToCdf;
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
						degree[n][n]++;
						degree[j][j]++;
						numCreatedLinks++;
						linked = true;
						break;
					}
				}
				if (!linked) {
					for (int j = 0; j < n; j++) {
						if (nMatrix[n][j] != 1) {
							nMatrix[n][j] = 1;
							nMatrix[j][n] = 1;
							degree[n][n]++;
							degree[j][j]++;
							numCreatedLinks++;
							break;
						}
					}
				}
			}

		}
		return nMatrix;
	}

	@Override
	public String name() {
		return TModel.BARABASI_DENSITY.toString();
	}

	/**
	 * @return o valor do atributo density
	 */
	public double getDensity() {
		return density;
	}

	/**
	 * Altera o valor do atributo density
	 * @param density O valor para setar em density
	 */
	public void setDensity(double density) {
		this.density = density;
	}

	/**
	 * @return o valor do atributo exponent
	 */
	public double getExponent() {
		return exponent;
	}

	/**
	 * Altera o valor do atributo exponent
	 * @param exponent O valor para setar em exponent
	 */
	public void setExponent(double exponent) {
		this.exponent = exponent;
	}

}
