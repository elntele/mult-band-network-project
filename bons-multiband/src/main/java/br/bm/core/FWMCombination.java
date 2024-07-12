/*
 * *****************************************************************************
 * Copyright (c) 2005
 * Propriedade do Laboratorio de P&D da Unicap-Itautec
 * Todos os direitos reservados, com base nas leis brasileiras de 
 * copyright
 * Este software e confidencial e de propriedade intelectual do
 * Laboratorio de P&D da Unicap-Itautec
 * ****************************************************************************
 * Projeto: SIAC - Sistema Itautec de Automaco Comercial
 * Arquivo: FWMCombination.java
 * ****************************************************************************
 * Historico de revisoes
 * CR		Nome				Data		Descricao
 * ****************************************************************************
 * 064813-P	Danilo Araujo		29/04/2011		Versao inicial
 * ****************************************************************************
 */
package br.bm.core;


/**
 * Estrutura para encapsular 3 comprimentos de onda para avaliacao de FWM.
 * 
 * @author Danilo Araujo
 * @since 29/04/2011
 */
public class FWMCombination {
	protected int i;

	protected int j;

	protected int k;

	public FWMCombination() {
	}

	public FWMCombination(int i, int j, int k) {
		super();
		this.i = i;
		this.j = j;
		this.k = k;
	}

	/**
	 * M�todo acessor para obter o valor do atributo i.
	 *
	 * @return Retorna o atributo i.
	 */
	public int getI() {
		return i;
	}

	/**
	 * Metodo acessor para modificar o valor do atributo i.
	 *
	 * @param i O valor de i para setar.
	 */
	public void setI(int i) {
		this.i = i;
	}

	/**
	 * Metodo acessor para obter o valor do atributo j.
	 *
	 * @return Retorna o atributo j.
	 */
	public int getJ() {
		return j;
	}

	/**
	 * Metodo acessor para modificar o valor do atributo j.
	 *
	 * @param j O valor de j para setar.
	 */
	public void setJ(int j) {
		this.j = j;
	}

	/**
	 * Metodo acessor para obter o valor do atributo k.
	 *
	 * @return Retorna o atributo k.
	 */
	public int getK() {
		return k;
	}

	/**
	 * Metodo acessor para modificar o valor do atributo k.
	 *
	 * @param k O valor de k para setar.
	 */
	public void setK(int k) {
		this.k = k;
	}

}
