/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: AnchoredErdosRenyi.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	10/10/2013		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.models;

import br.cns24.util.RandomUtils;

/**
 * 
 * @author Danilo
 * @since 10/10/2013
 */
public class AnchoredErdosRenyi extends GenerativeProcedure {
	private int m;

	public AnchoredErdosRenyi(int m) {
		this.m = m;
	}

	public AnchoredErdosRenyi(double density, double numNodes) {
		this.m = (int) Math.round((density * (numNodes - 1) * numNodes) / 2);
	}

	@Override
	public Integer[][] transform(Integer[][] matrix) {
		Integer[][] newMatrix = new Integer[matrix.length][matrix.length];
		KRegular kr = null;
		int mc = 0;
		if (m >= matrix.length - 1){
			kr = new KRegular(1);
			newMatrix = kr.transform(newMatrix);
			int node1 = RandomUtils.getInstance().nextInt(matrix.length - 1);
			int node2 = RandomUtils.getInstance().nextInt(matrix.length - 1);
			while (node2 == node1) {
				node1 = RandomUtils.getInstance().nextInt(matrix.length - 1);
				node2 = RandomUtils.getInstance().nextInt(matrix.length - 1);
			}
			newMatrix[node1][node2] = 0;
			newMatrix[node2][node1] = 0;
			mc = matrix.length - 1;
		}
		
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
		return TModel.ANCHORED_ERDOS_RENYI_N_M.toString();
	}

}
