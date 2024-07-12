/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: HubDegree.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	05/09/2013		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.metrics;

import br.cns24.Metric;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;
import br.cns24.transformations.DegreeMatrix;

/**
 * 
 * @author Danilo
 * @since 05/09/2013
 */
public class HubDegree implements Metric<Integer> {

	private static final HubDegree instance = new HubDegree();

	private HubDegree() {
	}

	public static HubDegree getInstance() {
		return instance;
	}
	
	public double calculate(ComplexNetwork cn) {
		Integer[][] degree = cn.getDegreeMatrix();
		if (cn.getDegreeMatrix() == null) {
			degree = DegreeMatrix.getInstance().transform(cn.getAdjacencyMatrix());
			cn.setDegreeMatrix(degree);
		}
		double maior = 0;

		for (int i = 0; i < degree.length; i++) {
			if (degree[i][i] > maior) {
				maior = degree[i][i];
			}
		}

		return maior;
	}

	@Override
	public double calculate(Integer[][] matrix) {
		Integer[][] degree = DegreeMatrix.getInstance().transform(matrix);
		double maior = 0;

		for (int i = 0; i < degree.length; i++) {
			if (degree[i][i] > maior) {
				maior = degree[i][i];
			}
		}

		return maior;
	}

	@Override
	public String name() {
		return TMetric.HUB_DEGREE.toString();
	}

}
