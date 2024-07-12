package br.bm.core;

import static br.bm.core.SimonUtil.getRandomPareto;
import static java.lang.Math.random;
import static java.lang.Math.round;

public class CallSchedulerPareto extends CallScheduler {
	private double meanRateBetweenCalls;
	private double meanRateOfCallsDuration;
	private double alphaBetweenCalls;
	private double alphaCallsDuration;
	private double xmBetweenCalls;
	private double xmCallsDuration;
	private int numberOfNodesInNetwork;

	public void resetTime_mpu() {
		setCurrentTime(0.0);
	}

	public CallSchedulerPareto(double meanRateBetweenCalls_par, double meanRateOfCallsDuration_par,
			int numberOfNodesInNetwork_par) {
		super();
//		meanRateBetweenCalls = meanRateBetweenCalls_par;
//		meanRateOfCallsDuration = meanRateOfCallsDuration_par;
//		numberOfNodesInNetwork = numberOfNodesInNetwork_par;
//		xmBetweenCalls = 0.01;
//		xmCallsDuration = 0.01;
//		alphaBetweenCalls = meanRateBetweenCalls/(meanRateBetweenCalls - xmBetweenCalls);
//		alphaCallsDuration = meanRateOfCallsDuration/(meanRateOfCallsDuration - xmCallsDuration);
//		System.out.println("meanRateBetweenCalls_par = " + meanRateBetweenCalls_par);
//		System.out.println("meanRateOfCallsDuration_par = " + meanRateOfCallsDuration_par);
//		System.out.println("xmBetweenCalls = " + xmBetweenCalls);
//		System.out.println("xmCallsDuration = " + xmCallsDuration);
//		System.out.println("alphaBetweenCalls = " + alphaBetweenCalls);
//		System.out.println("alphaCallsDuration = " + alphaCallsDuration);
//		
		//setup para 200 earlangs
//		meanRateBetweenCalls = 200 * 0.01;
//		meanRateOfCallsDuration = 0.01;
//		numberOfNodesInNetwork = numberOfNodesInNetwork_par;
//		xmBetweenCalls = 0.4;
//		xmCallsDuration = 50;
//		alphaBetweenCalls = 5;
//		alphaCallsDuration = 2;
		
		//setup para 80 earlangs
//		meanRateBetweenCalls = 80 * 0.01;
//		meanRateOfCallsDuration = 0.01;
//		numberOfNodesInNetwork = numberOfNodesInNetwork_par;
//		xmBetweenCalls = 0.4;
//		xmCallsDuration = 50;
//		alphaBetweenCalls = 1.4560;
//		alphaCallsDuration = 2;
		
		//setup para 40 earlangs
		meanRateBetweenCalls = 40 * 0.01;
		meanRateOfCallsDuration = 0.01;
		numberOfNodesInNetwork = numberOfNodesInNetwork_par;
		xmBetweenCalls = 1;
		xmCallsDuration = 50;
		alphaBetweenCalls = 1.6443;
		alphaCallsDuration = 2;
	}

	public void generateCallRequisition() {
		// sorteia par fonte destino
		do {
			setNextSourceNode((int) round(random() * (numberOfNodesInNetwork - 1)));
			setNextDestinationNode((int) round(random() * (numberOfNodesInNetwork - 1)));
		} while (getNextSourceNode() == getNextDestinationNode());
		setCurrentTime(getCurrentTime() + getRandomPareto(alphaBetweenCalls, xmBetweenCalls));
		
		setDuration(getRandomPareto(alphaCallsDuration, xmCallsDuration));
//		System.out.printf("%.4f\n", getDuration());
//		System.out.println("currentTime = " + getCurrentTime());
//		System.out.println("duration = " + getDuration());
	}

}
