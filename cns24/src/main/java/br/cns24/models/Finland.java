/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: Finland.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	11/10/2013		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.models;

/**
 * 
 * @author Danilo
 * @since 11/10/2013
 */
public class Finland extends GenerativeProcedure {
	public Finland() {
	}
	
	@Override
	public Integer[][] transform(Integer[][] matrix) {
		return new Integer[][]{
				{0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0},
				{1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0},
				{0, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0},
				{0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0},
				{0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 0},
				{0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1},
				{0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 1},
				{0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 1},
				{0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 1},
				{0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0}};
	}

	@Override
	public String name() {
		return TModel.FINLAND.toString();
	}

}
