/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: GenerativeAlgortithm.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	07/10/2013		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.model;

/**
 * 
 * @author Danilo
 * @since 07/10/2013
 */
public interface GenerativeAlgortithm {
	public Network generate(int n, double ... parameters);
}
