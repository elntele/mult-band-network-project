/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: SimtonNetwork.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	17/10/2013		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.gui;

/**
 * 
 * @author Danilo
 * @since 17/10/2013
 */
public class PlaningResult {
	private Integer[][] var;
	
	private Double[][] pf;
	
	private Double[] isolation;
	
	private Integer[] w;

	/**
	 * Construtor da classe.
	 * @param var
	 * @param pf
	 */
	public PlaningResult(Integer[][] var, Double[][] pf) {
		super();
		this.var = var;
		this.pf = pf;
	}

	/**
	 * @return o valor do atributo var
	 */
	public Integer[][] getVar() {
		return var;
	}

	/**
	 * Altera o valor do atributo var
	 * @param var O valor para setar em var
	 */
	public void setVar(Integer[][] var) {
		this.var = var;
	}

	/**
	 * @return o valor do atributo pf
	 */
	public Double[][] getPf() {
		return pf;
	}

	/**
	 * Altera o valor do atributo pf
	 * @param pf O valor para setar em pf
	 */
	public void setPf(Double[][] pf) {
		this.pf = pf;
	}

	/**
	 * @return o valor do atributo isolation
	 */
	public Double[] getIsolation() {
		return isolation;
	}

	/**
	 * Altera o valor do atributo isolation
	 * @param isolation O valor para setar em isolation
	 */
	public void setIsolation(Double[] isolation) {
		this.isolation = isolation;
	}

	/**
	 * @return o valor do atributo w
	 */
	public Integer[] getW() {
		return w;
	}

	/**
	 * Altera o valor do atributo w
	 * @param w O valor para setar em w
	 */
	public void setW(Integer[] w) {
		this.w = w;
	}
}
