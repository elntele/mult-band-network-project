/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: GmlNode.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	12/10/2013		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.model;

import java.util.HashMap;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * 
 * @author Danilo
 * @since 12/10/2013
 */
public class GmlNode {
	private int id;

	private String label;

	private String country;

	private double longitude;

	private double latitude;

	private int internal;

	private long population;

	private String shortName;

	private double idh;

	private double gine;

	private double pib;

	private Map<String, String> informations = new HashMap<String, String>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "id = " + id + ", latitude = " + latitude + ", longitude = " + longitude;
	}

	/**
	 * Construtor da classe.
	 * 
	 * @param id
	 * @param label
	 * @param country
	 * @param longitude
	 * @param latitude
	 * @param internal
	 */
//	@JsonCreator
	public GmlNode(@JsonProperty("id")int id, @JsonProperty("label")String label,@JsonProperty("country") String country,@JsonProperty("longitude") double longitude, @JsonProperty("latitude")double latitude,@JsonProperty("internal")int internal,
			@JsonProperty("population") long population) {
		super();
		this.id = id;
		this.label = label;
		this.country = country;
		this.longitude = longitude;
		this.latitude = latitude;
		this.population = population;
		this.internal = internal;
	}

	/**
	 * Construtor da classe.
	 * 
	 * @param id
	 */

	public GmlNode(int id) {
		super();
		this.id = id;
		this.latitude = 181;
		this.longitude = 181;
	}

//	/**
//	 * construtor vazio colocado apenas por causa do mapeamento do json Jackson para
//	 * a parte de paralelistmo, antes isso n�o existia no projeto, n�o use para
//	 * outra coisa
//	 */
//	public GmlNode() {
//
//	}

	public boolean visible() {
		return getLongitude() < 181;
	}

	/**
	 * @return o valor do atributo id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Altera o valor do atributo id
	 * 
	 * @param id O valor para setar em id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return o valor do atributo label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Altera o valor do atributo label
	 * 
	 * @param label O valor para setar em label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return o valor do atributo country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * Altera o valor do atributo country
	 * 
	 * @param country O valor para setar em country
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * @return o valor do atributo longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * Altera o valor do atributo longitude
	 * 
	 * @param longitude O valor para setar em longitude
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * @return o valor do atributo latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * Altera o valor do atributo latitude
	 * 
	 * @param latitude O valor para setar em latitude
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return o valor do atributo internal
	 */
	public int getInternal() {
		return internal;
	}

	/**
	 * Altera o valor do atributo internal
	 * 
	 * @param internal O valor para setar em internal
	 */
	public void setInternal(int internal) {
		this.internal = internal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
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
		GmlNode other = (GmlNode) obj;
		if (id != other.id)
			return false;
		return true;
	}

	/**
	 * @return o valor do atributo population
	 */
	public long getPopulation() {
		return population;
	}

	/**
	 * Altera o valor do atributo population
	 * 
	 * @param population O valor para setar em population
	 */
	public void setPopulation(long population) {
		this.population = population;
	}

	/**
	 * @return o valor do atributo shortName
	 */
	public String getShortName() {
		return shortName;
	}

	/**
	 * Altera o valor do atributo shortName
	 * 
	 * @param shortName O valor para setar em shortName
	 */
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	/**
	 * @return o valor do atributo informations
	 */
	public Map<String, String> getInformations() {
		return informations;
	}

	/**
	 * Altera o valor do atributo informations
	 * 
	 * @param informations O valor para setar em informations
	 */
	public void setInformations(Map<String, String> informations) {
		this.informations = informations;
	}

	public double getIdh() {
		return idh;
	}

	public void setIdh(double idh) {
		this.idh = idh;
	}

	public double getGine() {
		return gine;
	}

	public void setGine(double gine) {
		this.gine = gine;
	}

	public double getPib() {
		return pib;
	}

	public void setPib(double pib) {
		this.pib = pib;
	}

}
