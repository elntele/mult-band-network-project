package br.bm.core;

public class NetworkCost {
	private double opticalSwitch;
	private double lambda;
	private double deployment;
	private double amplifier;
	private double sSMFFiber;
	private double dCFFiber;
	private double totals;

	public NetworkCost(double opticalSwitch, double lambda, double deployment, double amplifier, double sSMFFiber,
			double dCFFiber, double totals) {
		super();
		this.opticalSwitch = opticalSwitch;
		this.lambda = lambda;
		this.deployment = deployment;
		this.amplifier = amplifier;
		this.sSMFFiber = sSMFFiber;
		this.dCFFiber = dCFFiber;
		this.totals = totals;
	}

	public NetworkCost() {
		super();
	}


	/**
	 * Metodo acessor para obter o valor do atributo opticalSwitch.
	 * 
	 * @return O valor de opticalSwitch
	 */
	public double getOpticalSwitch() {
		return opticalSwitch;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo opticalSwitch.
	 * 
	 * @param opticalSwitch
	 *            O novo valor de opticalSwitch
	 */
	public void setOpticalSwitch(double opticalSwitch) {
		this.opticalSwitch = opticalSwitch;
	}

	/**
	 * Metodo acessor para obter o valor do atributo lambda.
	 * 
	 * @return O valor de lambda
	 */
	public double getLambda() {
		return lambda;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo lambda.
	 * 
	 * @param lambda
	 *            O novo valor de lambda
	 */
	public void setLambda(double lambda) {
		this.lambda = lambda;
	}

	/**
	 * Metodo acessor para obter o valor do atributo deployment.
	 * 
	 * @return O valor de deployment
	 */
	public double getDeployment() {
		return deployment;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo deployment.
	 * 
	 * @param deployment
	 *            O novo valor de deployment
	 */
	public void setDeployment(double deployment) {
		this.deployment = deployment;
	}

	/**
	 * Metodo acessor para obter o valor do atributo amplifier.
	 * 
	 * @return O valor de amplifier
	 */
	public double getAmplifier() {
		return amplifier;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo amplifier.
	 * 
	 * @param amplifier
	 *            O novo valor de amplifier
	 */
	public void setAmplifier(double amplifier) {
		this.amplifier = amplifier;
	}

	/**
	 * Metodo acessor para obter o valor do atributo sSMFFiber.
	 * 
	 * @return O valor de sSMFFiber
	 */
	public double getsSMFFiber() {
		return sSMFFiber;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo sSMFFiber.
	 * 
	 * @param sSMFFiber
	 *            O novo valor de sSMFFiber
	 */
	public void setsSMFFiber(double sSMFFiber) {
		this.sSMFFiber = sSMFFiber;
	}

	/**
	 * Metodo acessor para obter o valor do atributo dCFFiber.
	 * 
	 * @return O valor de dCFFiber
	 */
	public double getdCFFiber() {
		return dCFFiber;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo dCFFiber.
	 * 
	 * @param dCFFiber
	 *            O novo valor de dCFFiber
	 */
	public void setdCFFiber(double dCFFiber) {
		this.dCFFiber = dCFFiber;
	}

	/**
	 * Metodo acessor para obter o valor do atributo totals.
	 * 
	 * @return O valor de totals
	 */
	public double getTotals() {
		return totals;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo totals.
	 * 
	 * @param totals
	 *            O novo valor de totals
	 */
	public void setTotals(double totals) {
		this.totals = totals;
	}
}
