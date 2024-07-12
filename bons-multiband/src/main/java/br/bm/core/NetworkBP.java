package br.bm.core;

public class NetworkBP {
	private double ber;
	private double lambda;
	private double pmd;
	private double dispersion;
	private double meanDist;
	private double total;
	private double stdDevBer;
	
	private int contConv;
	private int contReg;
	private int contRegenerou;

	public NetworkBP() {
		super();
	}

	public NetworkBP(double ber, double lambda, double pmd, double dispersion, double meanDist, double total) {
		super();
		this.ber = ber;
		this.lambda = lambda;
		this.pmd = pmd;
		this.dispersion = dispersion;
		this.meanDist = meanDist;
		this.total = total;
	}
	
	public void clean(){
		this.ber = 0;
		this.lambda = 0;
		this.pmd = 0;
		this.dispersion = 0;
		this.meanDist = 0;
		this.total = 0;
	}

	/**
	 * Método acessor para obter o valor do atributo ber.
	 * 
	 * @return O valor de ber
	 */
	public double getBer() {
		return ber;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo ber.
	 * 
	 * @param ber
	 *            O novo valor de ber
	 */
	public void setBer(double ber) {
		this.ber = ber;
	}

	/**
	 * Método acessor para obter o valor do atributo lambda.
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
	 * Método acessor para obter o valor do atributo pmd.
	 * 
	 * @return O valor de pmd
	 */
	public double getPmd() {
		return pmd;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo pmd.
	 * 
	 * @param pmd
	 *            O novo valor de pmd
	 */
	public void setPmd(double pmd) {
		this.pmd = pmd;
	}

	/**
	 * Método acessor para obter o valor do atributo dispersion.
	 * 
	 * @return O valor de dispersion
	 */
	public double getDispersion() {
		return dispersion;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo dispersion.
	 * 
	 * @param dispersion
	 *            O novo valor de dispersion
	 */
	public void setDispersion(double dispersion) {
		this.dispersion = dispersion;
	}

	/**
	 * Método acessor para obter o valor do atributo meanDist.
	 * 
	 * @return O valor de meanDist
	 */
	public double getMeanDist() {
		return meanDist;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo meanDist.
	 * 
	 * @param meanDist
	 *            O novo valor de meanDist
	 */
	public void setMeanDist(double meanDist) {
		this.meanDist = meanDist;
	}

	/**
	 * Método acessor para obter o valor do atributo total.
	 * 
	 * @return O valor de total
	 */
	public double getTotal() {
		return total;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo total.
	 * 
	 * @param total
	 *            O novo valor de total
	 */
	public void setTotal(double total) {
		this.total = total;
	}

	public int getContConv() {
		return contConv;
	}

	public void setContConv(int contConv) {
		this.contConv = contConv;
	}

	public int getContReg() {
		return contReg;
	}

	public void setContReg(int contReg) {
		this.contReg = contReg;
	}

	public int getContRegenerou() {
		return contRegenerou;
	}

	public void setContRegenerou(int contRegenerou) {
		this.contRegenerou = contRegenerou;
	}

	/**
	 * @return o valor do atributo stdDevBer
	 */
	public double getStdDevBer() {
		return stdDevBer;
	}

	/**
	 * Altera o valor do atributo stdDevBer
	 * @param stdDevBer O valor para setar em stdDevBer
	 */
	public void setStdDevBer(double stdDevBer) {
		this.stdDevBer = stdDevBer;
	}
}
