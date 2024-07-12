package br.cns24.models;

import br.cns24.util.RandomUtils;

public class ErdosRenyiM extends GenerativeProcedure {
	private int m;

	public ErdosRenyiM(int m) {
		this.m = m;
	}

	public ErdosRenyiM(double density, double numNodes) {
		this.m = (int) Math.round((density * (numNodes - 1) * numNodes) / 2);
	}

	@Override
	public Integer[][] transform(Integer[][] matrix) {
		Integer[][] newMatrix = new Integer[matrix.length][matrix.length];

		for (int i = 0; i < newMatrix.length; i++){
			for (int j = 0; j < newMatrix.length; j++){
				newMatrix[i][j] = 0;
			}	
		}
		
		int mc = 0;
		while (mc < m) {
			int node1 = RandomUtils.getInstance().nextInt(matrix.length - 1);
			int node2 = RandomUtils.getInstance().nextInt(matrix.length - 1);
			while (node2 == node1 || (newMatrix[node1][node2] != null && newMatrix[node1][node2] == 1)) {
				node1 = RandomUtils.getInstance().nextInt(matrix.length - 1);
				node2 = RandomUtils.getInstance().nextInt(matrix.length - 1);
			}
			newMatrix[node1][node2] = 1;
			newMatrix[node2][node1] = 1;
			mc++;
		}
		return newMatrix;
	}

	@Override
	public String name() {
		return TModel.ERDOS_RENYI_N_M.toString();
	}

	/**
	 * @return o valor do atributo m
	 */
	public int getM() {
		return m;
	}

	/**
	 * Altera o valor do atributo m
	 * @param m O valor para setar em m
	 */
	public void setM(int m) {
		this.m = m;
	}

}
