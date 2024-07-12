package br.cns24.models;

public class ErdosRenyiP extends GenerativeProcedure {
	private double p;

	public double getP() {
		return p;
	}
	@Override
	public Integer[][] transform(Integer[][] matrix) {
		Integer[][] newMatrix = new Integer[matrix.length][matrix.length];

		for (int i = 0; i < matrix.length; i++) {
			newMatrix[i][i] = 0;
			for (int j = i + 1; j < matrix.length; j++) {
				if (Math.random() < p) {
					newMatrix[i][j] = 1;
					newMatrix[j][i] = 1;
				} else {
					newMatrix[i][j] = 0;
					newMatrix[j][i] = 0;
				}
			}
		}
		return newMatrix;
	}

	public void setP(double p) {
		this.p = p;
	}

	public ErdosRenyiP(double p) {
		this.p = p;
	}


	@Override
	public String name() {
		return TModel.ERDOS_RENYI_N_M.toString();
	}

}
