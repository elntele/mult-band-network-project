/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE e Padtec
 * ****************************************************************************
 * Projeto: Planejador de Redes �pticas Padtec � M�dulo Kernel
 * Arquivo: DistanceLaplacianMatrix.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo		20/01/2013	Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.transformations;

import br.cns24.Transformation;

/**
 * @author Danilo
 * 
 * @since 20/01/2013
 */
public class DistanceLaplacianMatrix implements Transformation<Integer> {
	private double[][] distance;
	
	public DistanceLaplacianMatrix(double[][] distance){
		this.distance = distance;
	}
	
	@Override
	public Integer[][] transform(Integer[][] matrix) {
		Integer[][] degree = DegreeMatrix.getInstance().transform(matrix);
		Integer[][] laplacian = new Integer[matrix.length][matrix.length];

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
//				laplacian[i][j] = degree[i][j] - matrix[i][j] * distance[i][k];
			}
		}

		return laplacian;
	}

}
