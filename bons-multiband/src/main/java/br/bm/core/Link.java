package br.bm.core;

import java.util.Vector;

/**
 * @author drba
 * 
 */
public class Link implements Cloneable {

	private int numFibers;
	private int source;
	private int destination;
	private double length;
	private boolean dynamicGain;
	private Vector<Fiber> fibers = new Vector<Fiber>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Link clone() {
		Link clone = new Link(source, destination, numFibers, this.getFiber(0).getNumUsedLambda(), this.getFiber(0)
				.getMuxDemuxGain(), this.getFiber(0).getBooster().getGain(), this.getFiber(0).getBooster()
				.getNoiseFactor(), this.getFiber(0).getBooster().getSatPower(), length, this.getFiber(0)
				.getCoeficienteAtenuacao(), this.getFiber(0).getPreAmplifier().getGain(), this.getFiber(0)
				.getPreAmplifier().getNoiseFactor(), this.getFiber(0).getPreAmplifier().getSatPower(), this.dynamicGain);
		return clone;
	}

	public Link(int numFibers, int numOfLambdaPerFiber_par, double length, int source, int destination) {

		this.source = source;
		this.destination = destination;
		this.numFibers = numFibers;
		this.length = length;

		for (int i = 1; i <= numFibers; i++)
			fibers.add(new Fiber(numOfLambdaPerFiber_par, length));

	}

	public Link(int numFibers, int numOfLambdaPerFiber_par, double length, int source, int destination,
			double PreGainIndB_par, double MuxDemuxLossIndB_par, double fiberLossIndBPerKm_par,
			double boosterGainIndB_par) {
		this.source = source;
		this.destination = destination;
		this.numFibers = numFibers;
		this.length = length;

		for (int i = 1; i <= numFibers; i++) {
			fibers.add(new Fiber(numOfLambdaPerFiber_par, PreGainIndB_par, MuxDemuxLossIndB_par, length,
					fiberLossIndBPerKm_par, boosterGainIndB_par));
			fibers.get(i - 1).setSourceNode(source); //
			fibers.get(i - 1).setDestinationNode(destination); //
		}
	}

	public Link(int source, int destination, int numFibers, int numOfLambdaPerFiber_par, double MuxDemuxGainIndB_par,
			double boosterGainIndB_par, double boosterNoiseFigure_par, double boosterPSat_par, double length,
			double fiberGainIndBPerKm_par, double preAmpGainIndB_par, double preAmpNoiseFigure_par,
			double preAmprPSat_par, boolean ganhoDinamico_par) {
		this.source = source;
		this.destination = destination;
		this.numFibers = numFibers;
		this.length = length;

		dynamicGain = ganhoDinamico_par;
//TODO jorge, aqui tem algo pra ser feito em relação ao sete. A logica atual fala que um link
// tem um conjunto fibras então tem que ver se é aqui que diferencia as bandas e as fibras.
		for (int i = 1; i <= numFibers; i++) {
			fibers.add(new Fiber(numOfLambdaPerFiber_par, source, destination, MuxDemuxGainIndB_par,
					boosterGainIndB_par, boosterNoiseFigure_par, boosterPSat_par, length, fiberGainIndBPerKm_par,
					preAmpGainIndB_par, preAmpNoiseFigure_par, preAmprPSat_par, dynamicGain));
			fibers.get(i - 1).setSourceNode(source); //
			fibers.get(i - 1).setDestinationNode(destination); //
		}

	}

	int getNumDeLambdasUsados_mpu() {
		return fibers.get(0).getNumUsedLambda();
	}

	public Fiber getFiber(int n) {
		return fibers.get(n);
	}

	public int getAvailableFiber(int lambda) {
		for (int i = 0; i < fibers.size(); i++) {
			if (fibers.get(i).isLambdaAvailable(lambda)) {
				return i;
			}
		}
		return -1;
	}

	public void changeWavelenghtNumber_mpu(int lambda) {
		fibers.get(0).changeLambda(lambda);
	}

	public double getFtotal(int fiberIndex, int lambda_lar) {
		double fTotal_loc = 0;
		fTotal_loc = fibers.get(fiberIndex).getRuidoAditivo(lambda_lar);
		return fTotal_loc;
	}

	public double getGanhoTotal(int fiberIndex) {

		double gTotal_loc = 0;
		gTotal_loc = fibers.get(fiberIndex).getGanhoTotal();

		return gTotal_loc;
	}

	public int getNumUsedLambda() {
		return fibers.get(0).getNumUsedLambda();
	}

	public void erase() {
		for (int i = 0; i < fibers.size(); i++)
			fibers.get(i).erase();
	}

	public void setBoosterGain(double gain, int fiberIndex) {
		fibers.get(fiberIndex).getBooster().setG0Db(gain);
	}

	public void setPreGain(double gain, int fiberIndex) {
		fibers.get(fiberIndex).getPreAmplifier().setG0Db(gain);
	}

	public void setG0PreBooster(double gain, int fiberIndex) {
		fibers.get(fiberIndex).getBooster().setG0Db(gain);
		fibers.get(fiberIndex).getPreAmplifier().setG0Db(gain);
	}

	/**
	 * Metodo acessor para obter o valor do atributo numFibers.
	 * 
	 * @return O valor de numFibers
	 */
	public int getNumFibers() {
		return numFibers;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo numFibers.
	 * 
	 * @param numFibers
	 *            O novo valor de numFibers
	 */
	public void setNumFibers(int numFibers) {
		this.numFibers = numFibers;
	}

	/**
	 * Metodo acessor para obter o valor do atributo source.
	 * 
	 * @return O valor de source
	 */
	public int getSource() {
		return source;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo source.
	 * 
	 * @param source
	 *            O novo valor de source
	 */
	public void setSource(int source) {
		this.source = source;
	}

	/**
	 * Metodo acessor para obter o valor do atributo destination.
	 * 
	 * @return O valor de destination
	 */
	public int getDestination() {
		return destination;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo destination.
	 * 
	 * @param destination
	 *            O novo valor de destination
	 */
	public void setDestination(int destination) {
		this.destination = destination;
	}

	/**
	 * Metodo acessor para obter o valor do atributo length.
	 * 
	 * @return O valor de length
	 */
	public double getLength() {
		return length;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo length.
	 * 
	 * @param length
	 *            O novo valor de length
	 */
	public void setLength(double length) {
		this.length = length;
	}

	/**
	 * Metodo acessor para obter o valor do atributo dynamicGain.
	 * 
	 * @return O valor de dynamicGain
	 */
	public boolean isDynamicGain() {
		return dynamicGain;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo dynamicGain.
	 * 
	 * @param dynamicGain
	 *            O novo valor de dynamicGain
	 */
	public void setDynamicGain(boolean dynamicGain) {
		this.dynamicGain = dynamicGain;
	}

	/**
	 * Metodo acessor para obter o valor do atributo fibers.
	 * 
	 * @return O valor de fibers
	 */
	public Vector<Fiber> getFibers() {
		return fibers;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo fibers.
	 * 
	 * @param fibers
	 *            O novo valor de fibers
	 */
	public void setFibers(Vector<Fiber> fibers) {
		this.fibers = fibers;
	}
}
