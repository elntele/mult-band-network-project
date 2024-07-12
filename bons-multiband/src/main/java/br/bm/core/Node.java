package br.bm.core;

import static java.lang.Math.pow;

/**
 * @author drba
 * 
 */
public class Node {
	private int numNode;
	private double switchAtenuation;
	private double laserPower;
	private int numRegenerators;
	private int numFreeRegenerators;
	private int markedRegenerators;
	private double laserSNR;

	public Node() {
		super();
	}
	
	public Node(double switchAtenuationIndB_par, double laserPowerIndBm_par,
			double laserSNRindB_par) {
		setSwitchAtenuation(switchAtenuationIndB_par);
		setLaserPower(laserPowerIndBm_par);
		setLaserSNR(laserSNRindB_par);
		this.numRegenerators = 0;
		this.numFreeRegenerators = 0;
		this.markedRegenerators = 0;
	}

	public Node(double switchAtenuation, double laserPower,  double laserSNR, int numRegenerators) {
		super();
		this.switchAtenuation = switchAtenuation;
		this.laserPower = laserPower;
		this.numRegenerators = numRegenerators;
		this.numFreeRegenerators = numRegenerators;
		this.markedRegenerators = 0;
		this.laserSNR = laserSNR;
	}

	public void markRegenerators() {
		markedRegenerators++;
	}

	public void rescueMarkedRegenerators() {
		numFreeRegenerators += markedRegenerators;
	}
	
	public void resetMarkedRegenerators(){
		markedRegenerators = 0;
	}

	public void setLaserSNR(double snrIndB) {
		laserSNR = pow(10, snrIndB / 10);
	}

	public void setSwitchAtenuation(double atendB) {
		switchAtenuation = pow(10, atendB / 10);
	}

	public void setLaserPower(double powerdBm) {
		laserPower = pow(10, (powerdBm / 10) - 3) * switchAtenuation;
	}

	public void incNumFreeRegenerators() {
		numFreeRegenerators++;
	}

	public void decNumFreeRegenerators() {
		numFreeRegenerators--;
	}

	public void resetNode() {
		numFreeRegenerators = numRegenerators;
	}

	/**
	 * Metodo acessor para obter o valor do atributo numRegenerators.
	 * @return O valor de numRegenerators
	 */
	public int getNumRegenerators() {
		return numRegenerators;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo numRegenerators.
	 * @param numRegenerators O novo valor de numRegenerators
	 */
	public void setNumRegenerators(int numRegenerators) {		
		this.numRegenerators = numRegenerators;
		this.numFreeRegenerators = numRegenerators;
		this.markedRegenerators = 0;
	}

	/**
	 * Metodo acessor para obter o valor do atributo numFreeRegenerators.
	 * @return O valor de numFreeRegenerators
	 */
	public int getNumFreeRegenerators() {
		return numFreeRegenerators;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo numFreeRegenerators.
	 * @param numFreeRegenerators O novo valor de numFreeRegenerators
	 */
	public void setNumFreeRegenerators(int numFreeRegenerators) {
		this.numFreeRegenerators = numFreeRegenerators;
	}

	/**
	 * Metodo acessor para obter o valor do atributo markedRegenerators.
	 * @return O valor de markedRegenerators
	 */
	public int getMarkedRegenerators() {
		return markedRegenerators;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo markedRegenerators.
	 * @param markedRegenerators O novo valor de markedRegenerators
	 */
	public void setMarkedRegenerators(int markedRegenerators) {
		this.markedRegenerators = markedRegenerators;
	}

	/**
	 * Metodo acessor para obter o valor do atributo switchAtenuation.
	 * @return O valor de switchAtenuation
	 */
	public double getSwitchAtenuation() {
		return switchAtenuation;
	}

	/**
	 * Metodo acessor para obter o valor do atributo laserPower.
	 * @return O valor de laserPower
	 */
	public double getLaserPower() {
		return laserPower;
	}

	/**
	 * Metodo acessor para obter o valor do atributo laserSNR.
	 * @return O valor de laserSNR
	 */
	public double getLaserSNR() {
		return laserSNR;
	}

	/**
	 * Metodo acessor para obter o valor do atributo numNode.
	 * @return O valor de numNode
	 */
	public int getNumNode() {
		return numNode;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo numNode.
	 * @param numNode O novo valor de numNode
	 */
	public void setNumNode(int numNode) {
		this.numNode = numNode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + numNode;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (numNode != other.numNode)
			return false;
		return true;
	}

}