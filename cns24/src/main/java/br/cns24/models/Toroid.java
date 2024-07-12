/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: Toroid.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	16/10/2013		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.models;

import br.cns24.transformations.DegreeMatrix;
import br.cns24.util.RandomUtils;

/**
 * 
 * @author Danilo
 * @since 16/10/2013
 */
public class Toroid extends GenerativeProcedure {
	private int m;
	
	private double numNodes;

	public Toroid(int m) {
		this.m = m;
	}

	public Toroid(double density, double numNodes) {
		this.numNodes = numNodes;
		this.m = (int) Math.round((density * (numNodes - 1) * numNodes) / 2);
	}
	
	public void changeDensity(double density){
		this.m = (int) Math.round((density * (numNodes - 1) * numNodes) / 2);
	}

	@Override
	public Integer[][] transform(Integer[][] matrix) {
		int nInputNodes = matrix.length;
		int source;
		int dest;
		Integer[][] newMatrix = new Integer[nInputNodes][nInputNodes];
		int numLinks = 0;
		int sqrt_nNodes = (int) Math.sqrt(nInputNodes)+1; // length of a side
		int n = sqrt_nNodes * sqrt_nNodes; // Perfect square
		Integer[][] newMatrixToroid = new Integer[n][n];
		for (int i = 0; i < newMatrixToroid.length; i++){
			for (int j = 0; j < newMatrixToroid.length; j++){
				newMatrixToroid[i][j] = 0;
			}	
		}
		for (int row = 0; row < n; row += sqrt_nNodes) {
			for (int col = 0; col < sqrt_nNodes; col++) { // Links
				int i = row + col;
				int j = col + 1;
				if (j >= sqrt_nNodes)
					j -= sqrt_nNodes; // Wrap horizontal
				j += row; // Add next node link
				newMatrixToroid[i][j] = 1;// Add horizontal link
				newMatrixToroid[j][i] = 1;
				j = i + sqrt_nNodes;
				if (j >= n)
					j -= n; // Wrap vertical
				newMatrixToroid[i][j] = 1;// Add vertical link
				newMatrixToroid[j][i] = 1;
			}
		}
		
		for (int i = 0; i < nInputNodes; i++){
			for (int j = 0; j < nInputNodes; j++){
				newMatrix[i][j] = newMatrixToroid[i][j];
				if (newMatrix[i][j] == 1){
					numLinks++;
				}
			}	
		}
		numLinks /=2;
		
		Integer[][] degree = DegreeMatrix.getInstance().transform(newMatrix);
		while (numLinks > m){
			source = RandomUtils.getInstance().nextInt(nInputNodes - 1);
			dest = RandomUtils.getInstance().nextInt(nInputNodes - 1);
//			while (source == dest || newMatrix[source][dest] == 0 || degree[source][source] < 2 || degree[dest][dest] < 2) {
			while (source == dest || newMatrix[source][dest] == 0) {
				source = RandomUtils.getInstance().nextInt(nInputNodes - 1);
				dest = RandomUtils.getInstance().nextInt(nInputNodes - 1);
			}
			newMatrix[source][dest] = 0;
			newMatrix[dest][source] = 0;
			degree[source][source] -= 1;
			degree[dest][dest] -= 1;
			numLinks-=1;
		}
		while (numLinks < m){
			source = RandomUtils.getInstance().nextInt(nInputNodes - 1);
			dest = RandomUtils.getInstance().nextInt(nInputNodes - 1);
			while (source == dest || newMatrix[source][dest] == 1) {
				source = RandomUtils.getInstance().nextInt(nInputNodes - 1);
				dest = RandomUtils.getInstance().nextInt(nInputNodes - 1);
			}
			newMatrix[source][dest] = 1;
			newMatrix[dest][source] = 1;
			numLinks+=1;
		}

		return newMatrix;
	}

	@Override
	public String name() {
		return TModel.TOROID.toString();
	}

}
