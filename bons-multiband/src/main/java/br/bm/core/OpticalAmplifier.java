package br.bm.core;

import static java.lang.Math.pow;

public class OpticalAmplifier {
	private double g0;
	private double satPower;
	private double gain;
	private double noiseFactor;

	public OpticalAmplifier() {
		super();
	}

	public OpticalAmplifier(double g0, double noiseFactor, double satPower) {
		super();
		setG0Db(g0);
		setSatPowerDb(satPower);
		setNoiseFactorDb(noiseFactor);
	}

	public double getGain(double inputPower) {
		return g0 / (1 + (g0 * inputPower / satPower));
	}

	public void setG0Db(double g0IndB) {
		g0 = pow(10, g0IndB / 10);
	}

	public void setNoiseFactorDb(double noiseFigure) {
		noiseFactor = pow(10, noiseFigure / 10);
	}

	public void setSatPowerDb(double pSat) {
		satPower = pow(10, (pSat / 10) - 3);
	}

	/**
	 * Metodo acessor para obter o valor do atributo g0.
	 * 
	 * @return O valor de g0
	 */
	public double getG0() {
		return g0;
	}

	/**
	 * Metodo acessor para obter o valor do atributo gain.
	 * 
	 * @return O valor de gain
	 */
	public double getGain() {
		return gain;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo gain.
	 * 
	 * @param gain
	 *            O novo valor de gain
	 */
	public void setGain(double gain) {
		this.gain = gain;
	}

	/**
	 * Metodo acessor para obter o valor do atributo noiseFactor.
	 * 
	 * @return O valor de noiseFactor
	 */
	public double getNoiseFactor() {
		return noiseFactor;
	}

	/**
	 * Metodo acessor para obter o valor do atributo satPower.
	 * 
	 * @return O valor de satPower
	 */
	public double getSatPower() {
		return satPower;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(g0);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(noiseFactor);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(satPower);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		OpticalAmplifier other = (OpticalAmplifier) obj;
		if (Double.doubleToLongBits(g0) != Double.doubleToLongBits(other.g0))
			return false;
		if (Double.doubleToLongBits(noiseFactor) != Double.doubleToLongBits(other.noiseFactor))
			return false;
		if (Double.doubleToLongBits(satPower) != Double.doubleToLongBits(other.satPower))
			return false;
		return true;
	}

}
