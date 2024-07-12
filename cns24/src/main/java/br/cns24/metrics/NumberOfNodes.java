/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: NumberOfNodes.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	21/06/2015		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.metrics;

import br.cns24.Metric;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;

/**
 * 
 * @author Danilo Araujo
 * @since 21/06/2015
 */
public class NumberOfNodes  implements Metric<Integer> {
	/**
	 * 
	 */
	private static final NumberOfNodes instance = new NumberOfNodes();

	private NumberOfNodes() {
	}

	public static NumberOfNodes getInstance() {
		return instance;
	}

	public double calculate(ComplexNetwork cn) {
		return cn.getAdjacencyMatrix().length;
	}

	public double calculate(Integer[][] matrix) {
		return matrix.length;
	}

	@Override
	public String name() {
		return TMetric.NUMBER_OF_NODES.toString();
	}
}
