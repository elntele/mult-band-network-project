/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: GmlEdge.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	12/10/2013		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.model;

import java.util.*;

/**
 * 
 * @author Danilo
 * @since 12/10/2013
 */
public class GmlEdge {
	private GmlNode source;
	
	private GmlNode target;
	
	private String label;

	private Bands bands;
	
	private Map<String, String> informations = new HashMap<String, String>();

	/**
	 * Construtor da classe.
	 */
	public GmlEdge() {
		super();
		source = new GmlNode(-1);
		target = new GmlNode(-1);
	}

	/**
	 * Construtor da classe.
	 * @param source
	 * @param target
	 * @param label
	 */
	public GmlEdge(GmlNode source, GmlNode target, String label, HashMap<String, String> informations) {
		super();
		this.source = source;
		this.target = target;
		this.label = label;
		this.informations = informations;
	}

	/**
	 * @return o valor do atributo source
	 */
	public GmlNode getSource() {
		return source;
	}

	/**
	 * Altera o valor do atributo source
	 * @param source O valor para setar em source
	 */
	public void setSource(GmlNode source) {
		this.source = source;
	}

	/**
	 * @return o valor do atributo target
	 */
	public GmlNode getTarget() {
		return target;
	}

	/**
	 * Altera o valor do atributo target
	 * @param target O valor para setar em target
	 */
	public void setTarget(GmlNode target) {
		this.target = target;
	}

	/**
	 * @return o valor do atributo label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Altera o valor do atributo label
	 * @param label O valor para setar em label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		return result;
	}

	public Bands getBand() {
		return bands;
	}

	public void setBand(Bands bands) {
		this.bands = bands;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GmlEdge other = (GmlEdge) obj;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}

	/**
	 * @return o valor do atributo informations
	 */
	public Map<String, String> getInformations() {
		return informations;
	}

	/**
	 * Altera o valor do atributo informations
	 * @param informations O valor para setar em informations
	 */
	public void setInformations(Map<String, String> informations) {
		this.informations = informations;
	}
}
