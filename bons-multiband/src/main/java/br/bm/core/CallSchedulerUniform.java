package br.bm.core;


import static br.bm.core.SimonUtil.getRandomExp;
import static java.lang.Math.random;
import static java.lang.Math.round;

public class CallSchedulerUniform extends CallScheduler {
	private double meanRateBetweenCalls;
	private double meanRateOfCallsDuration;
	private int numberOfNodesInNetwork;

	public void resetTime_mpu() {
		setCurrentTime(0.0);
	}

	public CallSchedulerUniform(double meanRateBetweenCalls_par, double meanRateOfCallsDuration_par,
			int numberOfNodesInNetwork_par) {
		super();
		meanRateBetweenCalls = meanRateBetweenCalls_par;
		meanRateOfCallsDuration = meanRateOfCallsDuration_par;
		numberOfNodesInNetwork = numberOfNodesInNetwork_par;

	}

	public void generateCallRequisition() {
		// sorteia par fonte destino
		do {
			setNextSourceNode((int) round(random() * (numberOfNodesInNetwork - 1)));
			setNextDestinationNode((int) round(random() * (numberOfNodesInNetwork - 1)));
		} while (getNextSourceNode() == getNextDestinationNode());
		setCurrentTime(getCurrentTime() + getRandomExp(meanRateBetweenCalls));
		setDuration(getRandomExp(meanRateOfCallsDuration));
		
//		System.out.println("currentTime = " + getCurrentTime());
//		System.out.println("duration = " + getDuration());
//		System.out.printf("%.4f\n", getDuration());
	}

	/**
	 * Metodo acessor para obter o valor do atributo meanRateBetweenCalls.
	 * 
	 * @return O valor de meanRateBetweenCalls
	 */
	public double getMeanRateBetweenCalls() {
		return meanRateBetweenCalls;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo meanRateBetweenCalls.
	 * 
	 * @param meanRateBetweenCalls
	 *            O novo valor de meanRateBetweenCalls
	 */
	public void setMeanRateBetweenCalls(double meanRateBetweenCalls) {
		this.meanRateBetweenCalls = meanRateBetweenCalls;
	}

	/**
	 * M�todo acessor para obter o valor do atributo meanRateOfCallsDuration.
	 * 
	 * @return O valor de meanRateOfCallsDuration
	 */
	public double getMeanRateOfCallsDuration() {
		return meanRateOfCallsDuration;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo meanRateOfCallsDuration.
	 * 
	 * @param meanRateOfCallsDuration
	 *            O novo valor de meanRateOfCallsDuration
	 */
	public void setMeanRateOfCallsDuration(double meanRateOfCallsDuration) {
		this.meanRateOfCallsDuration = meanRateOfCallsDuration;
	}

	/**
	 * Metodo acessor para obter o valor do atributo numberOfNodesInNetwork.
	 * 
	 * @return O valor de numberOfNodesInNetwork
	 */
	public int getNumberOfNodesInNetwork() {
		return numberOfNodesInNetwork;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo numberOfNodesInNetwork.
	 * 
	 * @param numberOfNodesInNetwork
	 *            O novo valor de numberOfNodesInNetwork
	 */
	public void setNumberOfNodesInNetwork(int numberOfNodesInNetwork) {
		this.numberOfNodesInNetwork = numberOfNodesInNetwork;
	}

}
