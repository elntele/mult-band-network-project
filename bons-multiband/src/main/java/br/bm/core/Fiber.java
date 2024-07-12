package br.bm.core;
import static br.bm.core.SimonUtil.C;
import static br.bm.core.SimonUtil.getFWMPower;
import static java.lang.Math.pow;

import java.util.Set;
import java.util.Vector;

public class Fiber {
	public static final double LAMBDA_INICIAL = 1528.77e-9;
	public static final double FREQUENCIA_FINAL = (C) / (LAMBDA_INICIAL);
	public static final double ESPACAMENTO_FREQUENCIA = 100e9;

	private int lambda;
	private int sourceNode;
	private int destinationNode;
	private boolean dynamicGain;
	private double muxDemuxGain;
	private double length;
	private double gain;
	private double coeficienteAtenuacao;
	private double sumPowerA;
	private double sumPowerB;
	private double sumPowerC;
	private double sumPowerD;
	private double sumPowerE;
	private double sumPowerF;
	private Vector<Double> powerA = new Vector<Double>();
	private Vector<Double> powerB = new Vector<Double>();
	private Vector<Double> powerC = new Vector<Double>();
	private Vector<Double> powerD = new Vector<Double>();
	private Vector<Double> powerE = new Vector<Double>();
	private Vector<Double> powerF = new Vector<Double>();
	private Vector<Double> fwmNoise = new Vector<Double>();
	private Vector<Double> frequencies = new Vector<Double>();
	private OpticalAmplifier booster;
	private OpticalAmplifier preAmplifier;
	private double time;
	
	public Fiber() {
		super();
	}

	public Fiber(int lambda, double length) {
		this.length = length;

		initialize(lambda);
	}

	public Fiber(int lambda, double ganhoPreDb, double perdaMuxDemuxDb, double length, double perdaFibraDbKm,
			double boosterGainDB) {
		this.length = length;

		initialize(lambda);
	}

	public Fiber(int lambda, int sourceNode, int destinationNode, double MuxDemuxGainIndB_par,
			double boosterGainIndB_par, double boosterNoiseFigure_par, double boosterPsat_par, double length,
			double fiberGainDbKm, double preAmpGainIndB_par, double preAmpNoiseFigure_par, double preAmpPsat_par,
			boolean dynamicGain) {

		// creates new boster and pre amp objects
		booster = new OpticalAmplifier(boosterGainIndB_par, boosterNoiseFigure_par, boosterPsat_par);
		preAmplifier = new OpticalAmplifier(preAmpGainIndB_par, preAmpNoiseFigure_par, preAmpPsat_par);
		this.length = length;
		this.sourceNode = sourceNode;
		this.destinationNode = destinationNode;
		this.dynamicGain = dynamicGain;
		setFiberGainDbKm(fiberGainDbKm);
		setCoeficienteAtenuacao(-fiberGainDbKm);
		setGainMuxDemuxDb(MuxDemuxGainIndB_par);

		initialize(lambda);
		time = 0.0;
	}

	public double getRuidoAditivo(int lambda) {
		// TODO: estava sem return na versao original C++
		return 0.0;
	}

	public double getGanhoTotal() {
		double G2 = booster.getGain(sumPowerB);
		double G3 = gain;
		double G4 = preAmplifier.getGain(sumPowerD);

		return G2 * G3 * G4;
	}

	public void setGainMuxDemuxDb(double pDeMux) {
		muxDemuxGain = pow(10, (pDeMux / 10));
	}

	public void setFiberGainDbKm(double fiberGainDbKm) {
		gain = pow(10, (fiberGainDbKm * length) / 10);
	}

	public int getNumUsedLambda() {
		int usedLambda = 0;
		for (int i = 0; i < lambda; i++)
			if (!isLambdaAvailable(i))
				usedLambda++;

		return usedLambda;
	}

	public void addTime(double duracao_ppr) {
		time += duracao_ppr;
	}

	public boolean isLambdaAvailable(int lambda) {
		return !powerA.isEmpty() && powerA.get(lambda) == 0.0 ;
	}

	public void changeLambda(int lambda) {
		powerA.clear();
		powerB.clear();
		powerC.clear();
		powerD.clear();
		powerE.clear();
		powerF.clear();
		frequencies.clear();
		fwmNoise.clear();

		for (int i = 1; i <= lambda; i++) {
			powerA.add(0.0);
			powerB.add(0.0);
			powerC.add(0.0);
			powerD.add(0.0);
			powerE.add(0.0);
			powerF.add(0.0);
			frequencies.add(FREQUENCIA_FINAL - (i * ESPACAMENTO_FREQUENCIA));

			fwmNoise.add(0.0);
		}
		sumPowerA = 0.0;
		sumPowerB = 0.0;
		sumPowerC = 0.0;
		sumPowerD = 0.0;
		sumPowerE = 0.0;
		sumPowerF = 0.0;

		this.lambda = lambda;
	}

	public void initialize(int lambda) {
		this.lambda = lambda;
		for (int i = 1; i <= lambda; i++) {
			powerA.add(0.0);
			powerB.add(0.0);
			powerC.add(0.0);
			powerD.add(0.0);
			powerE.add(0.0);
			powerF.add(0.0);
			frequencies.add(FREQUENCIA_FINAL - (i * ESPACAMENTO_FREQUENCIA));

			fwmNoise.add(0.0);
		}
		sumPowerA = 0.0;
		sumPowerB = 0.0;
		sumPowerC = 0.0;
		sumPowerD = 0.0;
		sumPowerE = 0.0;
		sumPowerF = 0.0;
	}

	public void deallocate(int lambda) {
		deallocateSumsPower(lambda);
		powerA.set(lambda, 0.0);
		powerB.set(lambda, 0.0);
		powerC.set(lambda, 0.0);
		powerD.set(lambda, 0.0);
		powerE.set(lambda, 0.0);
		powerF.set(lambda, 0.0);
	}

	public void allocate(int lambda_par, double potInicialEmWatt_par) {
		// o parametro de potencia inicial tem q vir j� com a a perda do
		// switch
		double ganhoBooster_loc, ganhoPre_loc;
		setPowerA(potInicialEmWatt_par, lambda_par);
		setPowerB(powerA.get(lambda_par) * muxDemuxGain, lambda_par);
		if (dynamicGain) {
			ganhoBooster_loc = booster.getGain(sumPowerB);
		} else {
			ganhoBooster_loc = booster.getG0();
		}
		setPowerC(powerB.get(lambda_par) * ganhoBooster_loc, lambda_par);
		setPowerD(powerC.get(lambda_par) * gain, lambda_par);
		if (dynamicGain) {
			ganhoPre_loc = preAmplifier.getGain(sumPowerD);
		} else {
			// Se ganho n�o for din�mico, getGain_mpu retorna G0
			ganhoPre_loc = preAmplifier.getG0();
		}
		setPowerE(powerD.get(lambda_par) * ganhoPre_loc, lambda_par);
		setPowerF(powerE.get(lambda_par) * muxDemuxGain, lambda_par);
	}

	public void inicializarVetor(double[] vetor, int tamanho) {
		for (int i = 0; i < tamanho; i++)
			vetor[i] = 0.0;
	}

	public void copiarVetor(double[] vetorDestino, double[] vetorOrigem) {
		for (int i = 0; i < vetorOrigem.length; i++)
			vetorDestino[i] = vetorOrigem[i];
	}

	public double getSumPowerFWMNew(int usarLambda_par, Vector<Node> vectorOfNodes_par) {
		int count, tamanho;
		double[] listaPotencias, potTemp; // potencia de FWM
		double sumPower;

		tamanho = 100;
		count = 0;
		sumPower = 0.0;
		listaPotencias = new double[tamanho];
		inicializarVetor(listaPotencias, tamanho);
		// procuraram as combina��es de canais que geram o FWM
		
		Set<FWMCombination> combinations = FWMUtil.getInstance().getCombinations(usarLambda_par);
		
		for (FWMCombination combination : combinations){
			if (combination.i >= this.getLambda() || combination.j >= this.getLambda() || combination.k >= this.getLambda()){
				continue;
			}
			// se a combincao existe e os canais foram alocados
			if ((powerC.get(combination.i) != 0) && (powerC.get(combination.j) != 0) && (powerC.get(combination.k) != 0)) {
				// verifica o tamanho do vetor e gerencia este
				if (count == tamanho) {
					potTemp = new double[tamanho];
					copiarVetor(potTemp, listaPotencias);

					tamanho += 100;
					listaPotencias = new double[tamanho];
					inicializarVetor(listaPotencias, tamanho);

					copiarVetor(listaPotencias, potTemp);
					potTemp = null;
				}
				listaPotencias[count] = getFWMPower((this.getLength() * 1000), this.getCoeficienteAtenuacao(),
						frequencies.get(combination.i), frequencies.get(combination.j), frequencies.get(combination.k),
						powerC.get(combination.i), powerC.get(combination.j), powerC.get(combination.k));
				sumPower += listaPotencias[count];
				count++;
			}
		}

		listaPotencias = null;

		return sumPower;
	}
	
	public double getSumPowerFWM(int usarLambda_par, Vector<Node> vectorOfNodes_par) {
		int nLambda; // variaveis relacionadas aos comprimentos de onda
		int count, tamanho;
		double[] listaPotencias, potTemp; // potencia de FWM
		double sumPower;

		nLambda = this.getLambda();
		tamanho = 100;
		count = 0;
		sumPower = 0.0;
		listaPotencias = new double[tamanho];
		inicializarVetor(listaPotencias, tamanho);
		// procuraram as combinacoees de canais que geram o FWM
		
		for (int i = 0; i < nLambda; i++)
			for (int j = 0; j < nLambda; j++)
				for (int k = 0; k < nLambda; k++){
					if ((i + j - k == usarLambda_par) && (i != k) && (j != k)) {
						// se a combincao existe e os canais foram alocados
						if ((powerC.get(i) != 0) && (powerC.get(j) != 0) && (powerC.get(k) != 0)) {
							// verifica o tamanho do vetor e gerencia este
							if (count == tamanho) {
								potTemp = new double[tamanho];
								copiarVetor(potTemp, listaPotencias);

								tamanho += 100;
								listaPotencias = new double[tamanho];
								inicializarVetor(listaPotencias, tamanho);

								copiarVetor(listaPotencias, potTemp);
								potTemp = null;
							}
							listaPotencias[count] = getFWMPower((this.getLength() * 1000), this
									.getCoeficienteAtenuacao(), frequencies.get(i), frequencies.get(j), frequencies
									.get(k), powerC.get(i), powerC.get(j), powerC.get(k));
							sumPower += listaPotencias[count];
							count++;
						}
					}
				}
					
		listaPotencias = null;

		return sumPower;
	}

	public double getG0PreAmp(double power) {
		if (dynamicGain)
			return preAmplifier.getGain(power);
		else
			return preAmplifier.getG0();
	}

	public double getG0Booster(double power) {
		if (dynamicGain)
			return booster.getGain(power);
		else
			return booster.getG0();
	}

	public double getBoosterF(double inputPower_par) {
		return (booster.getNoiseFactor());
	}

	public double getFrequency(int lambda) {
		return (frequencies.get(lambda));
	}

	public double getPreF(double inputPower_par) {
		return (preAmplifier.getNoiseFactor());
	}

	public void deallocateSumsPower(int lambda) {
		sumPowerA -= powerA.get(lambda);
		sumPowerB -= powerB.get(lambda);
		sumPowerC -= powerC.get(lambda);
		sumPowerD -= powerD.get(lambda);
		sumPowerE -= powerE.get(lambda);
		sumPowerF -= powerF.get(lambda);
	}

	public double getPowerA(int lambda) {
		return powerA.get(lambda);
	}

	public double getPowerB(int lambda) {
		return powerB.get(lambda);
	}

	public double getPowerC(int lambda) {
		return powerC.get(lambda);
	}

	public double getPowerD(int lambda) {
		return powerD.get(lambda);
	}

	public double getPowerE(int lambda) {
		return powerE.get(lambda);
	}

	public double getPowerF(int lambda) {
		return powerF.get(lambda);
	}

	public void setPowerA(double power, int lambda) {
		sumPowerA -= powerA.get(lambda);
		sumPowerA += power;
		powerA.set(lambda, power);
	}

	public void setPowerB(double power, int lambda) {
		sumPowerB -= powerB.get(lambda);
		sumPowerB += power;
		powerB.set(lambda, power);
	}

	public void setPowerC(double power, int lambda) {
		sumPowerC -= powerC.get(lambda);
		sumPowerC += power;
		powerC.set(lambda, power);
	}

	public void setPowerD(double power, int lambda) {
		sumPowerD -= powerD.get(lambda);
		sumPowerD += power;
		powerD.set(lambda, power);
	}

	public void setPowerE(double power, int lambda) {
		sumPowerE -= powerE.get(lambda);
		sumPowerE += power;
		powerE.set(lambda, power);
	}

	public void setPowerF(double power, int lambda) {
		sumPowerF -= powerF.get(lambda);
		sumPowerF += power;
		powerF.set(lambda, power);
	}

	public void erase() {
		sumPowerA = 0.0;
		sumPowerB = 0.0;
		sumPowerC = 0.0;
		sumPowerD = 0.0;
		sumPowerE = 0.0;
		sumPowerF = 0.0;

		for (int i = 0; i < powerA.size(); i++) {

			powerA.set(i, 0.0);
			powerB.set(i, 0.0);
			powerC.set(i, 0.0);
			powerD.set(i, 0.0);
			powerE.set(i, 0.0);
			powerF.set(i, 0.0);
		}

		time = 0.0;
	}

	/**
	 * Metodo acessor para obter o valor do atributo lambda.
	 * 
	 * @return O valor de lambda
	 */
	public int getLambda() {
		return lambda;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo lambda.
	 * 
	 * @param lambda
	 *            O novo valor de lambda
	 */
	public void setLambda(int lambda) {
		this.lambda = lambda;
	}

	/**
	 * M�todo acessor para obter o valor do atributo sourceNode.
	 * 
	 * @return O valor de sourceNode
	 */
	public int getSourceNode() {
		return sourceNode;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo sourceNode.
	 * 
	 * @param sourceNode
	 *            O novo valor de sourceNode
	 */
	public void setSourceNode(int sourceNode) {
		this.sourceNode = sourceNode;
	}

	/**
	 * M�todo acessor para obter o valor do atributo destinationNode.
	 * 
	 * @return O valor de destinationNode
	 */
	public int getDestinationNode() {
		return destinationNode;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo destinationNode.
	 * 
	 * @param destinationNode
	 *            O novo valor de destinationNode
	 */
	public void setDestinationNode(int destinationNode) {
		this.destinationNode = destinationNode;
	}

	/**
	 * M�todo acessor para obter o valor do atributo dynamicGain.
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
	 * M�todo acessor para obter o valor do atributo muxDemuxGain.
	 * 
	 * @return O valor de muxDemuxGain
	 */
	public double getMuxDemuxGain() {
		return muxDemuxGain;
	}

	/**
	 * M�todo acessor para obter o valor do atributo length.
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
	 * M�todo acessor para obter o valor do atributo gain.
	 * 
	 * @return O valor de gain
	 */
	public double getGain() {
		return gain;
	}

	/**
	 * M�todo acessor para obter o valor do atributo coeficienteAtenuacao.
	 * 
	 * @return O valor de coeficienteAtenuacao
	 */
	public double getCoeficienteAtenuacao() {
		return coeficienteAtenuacao;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo coeficienteAtenuacao.
	 * 
	 * @param coeficienteAtenuacao
	 *            O novo valor de coeficienteAtenuacao
	 */
	public void setCoeficienteAtenuacao(double coeficienteAtenuacao) {
		this.coeficienteAtenuacao = coeficienteAtenuacao;
	}

	/**
	 * M�todo acessor para obter o valor do atributo sumPowerA.
	 * 
	 * @return O valor de sumPowerA
	 */
	public double getSumPowerA() {
		return sumPowerA;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo sumPowerA.
	 * 
	 * @param sumPowerA
	 *            O novo valor de sumPowerA
	 */
	public void setSumPowerA(double sumPowerA) {
		this.sumPowerA = sumPowerA;
	}

	/**
	 * Metodo acessor para obter o valor do atributo sumPowerB.
	 * 
	 * @return O valor de sumPowerB
	 */
	public double getSumPowerB() {
		return sumPowerB;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo sumPowerB.
	 * 
	 * @param sumPowerB
	 *            O novo valor de sumPowerB
	 */
	public void setSumPowerB(double sumPowerB) {
		this.sumPowerB = sumPowerB;
	}

	/**
	 * M�todo acessor para obter o valor do atributo sumPowerC.
	 * 
	 * @return O valor de sumPowerC
	 */
	public double getSumPowerC() {
		return sumPowerC;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo sumPowerC.
	 * 
	 * @param sumPowerC
	 *            O novo valor de sumPowerC
	 */
	public void setSumPowerC(double sumPowerC) {
		this.sumPowerC = sumPowerC;
	}

	/**
	 * M�todo acessor para obter o valor do atributo sumPowerD.
	 * 
	 * @return O valor de sumPowerD
	 */
	public double getSumPowerD() {
		return sumPowerD;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo sumPowerD.
	 * 
	 * @param sumPowerD
	 *            O novo valor de sumPowerD
	 */
	public void setSumPowerD(double sumPowerD) {
		this.sumPowerD = sumPowerD;
	}

	/**
	 * M�todo acessor para obter o valor do atributo sumPowerE.
	 * 
	 * @return O valor de sumPowerE
	 */
	public double getSumPowerE() {
		return sumPowerE;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo sumPowerE.
	 * 
	 * @param sumPowerE
	 *            O novo valor de sumPowerE
	 */
	public void setSumPowerE(double sumPowerE) {
		this.sumPowerE = sumPowerE;
	}

	/**
	 * M�todo acessor para obter o valor do atributo sumPowerF.
	 * 
	 * @return O valor de sumPowerF
	 */
	public double getSumPowerF() {
		return sumPowerF;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo sumPowerF.
	 * 
	 * @param sumPowerF
	 *            O novo valor de sumPowerF
	 */
	public void setSumPowerF(double sumPowerF) {
		this.sumPowerF = sumPowerF;
	}

	/**
	 * Metodo acessor para obter o valor do atributo powerA.
	 * 
	 * @return O valor de powerA
	 */
	public Vector<Double> getPowerA() {
		return powerA;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo powerA.
	 * 
	 * @param powerA
	 *            O novo valor de powerA
	 */
	public void setPowerA(Vector<Double> powerA) {
		this.powerA = powerA;
	}

	/**
	 * Metodo acessor para obter o valor do atributo powerB.
	 * 
	 * @return O valor de powerB
	 */
	public Vector<Double> getPowerB() {
		return powerB;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo powerB.
	 * 
	 * @param powerB
	 *            O novo valor de powerB
	 */
	public void setPowerB(Vector<Double> powerB) {
		this.powerB = powerB;
	}

	/**
	 * Metodo acessor para obter o valor do atributo powerC.
	 * 
	 * @return O valor de powerC
	 */
	public Vector<Double> getPowerC() {
		return powerC;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo powerC.
	 * 
	 * @param powerC
	 *            O novo valor de powerC
	 */
	public void setPowerC(Vector<Double> powerC) {
		this.powerC = powerC;
	}

	/**
	 * Metodo acessor para obter o valor do atributo powerD.
	 * 
	 * @return O valor de powerD
	 */
	public Vector<Double> getPowerD() {
		return powerD;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo powerD.
	 * 
	 * @param powerD
	 *            O novo valor de powerD
	 */
	public void setPowerD(Vector<Double> powerD) {
		this.powerD = powerD;
	}

	/**
	 * M�todo acessor para obter o valor do atributo powerE.
	 * 
	 * @return O valor de powerE
	 */
	public Vector<Double> getPowerE() {
		return powerE;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo powerE.
	 * 
	 * @param powerE
	 *            O novo valor de powerE
	 */
	public void setPowerE(Vector<Double> powerE) {
		this.powerE = powerE;
	}

	/**
	 * Metodo acessor para obter o valor do atributo powerF.
	 * 
	 * @return O valor de powerF
	 */
	public Vector<Double> getPowerF() {
		return powerF;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo powerF.
	 * 
	 * @param powerF
	 *            O novo valor de powerF
	 */
	public void setPowerF(Vector<Double> powerF) {
		this.powerF = powerF;
	}

	/**
	 * Metodo acessor para obter o valor do atributo fwmNoise.
	 * 
	 * @return O valor de fwmNoise
	 */
	public Vector<Double> getFwmNoise() {
		return fwmNoise;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo fwmNoise.
	 * 
	 * @param fwmNoise
	 *            O novo valor de fwmNoise
	 */
	public void setFwmNoise(Vector<Double> fwmNoise) {
		this.fwmNoise = fwmNoise;
	}

	/**
	 * Metodo acessor para obter o valor do atributo frequencies.
	 * 
	 * @return O valor de frequencies
	 */
	public Vector<Double> getFrequencies() {
		return frequencies;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo frequencies.
	 * 
	 * @param frequencies
	 *            O novo valor de frequencies
	 */
	public void setFrequencies(Vector<Double> frequencies) {
		this.frequencies = frequencies;
	}

	/**
	 * Metodo acessor para obter o valor do atributo booster.
	 * 
	 * @return O valor de booster
	 */
	public OpticalAmplifier getBooster() {
		return booster;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo booster.
	 * 
	 * @param booster
	 *            O novo valor de booster
	 */
	public void setBooster(OpticalAmplifier booster) {
		this.booster = booster;
	}

	/**
	 * Metodo acessor para obter o valor do atributo preAmplifier.
	 * 
	 * @return O valor de preAmplifier
	 */
	public OpticalAmplifier getPreAmplifier() {
		return preAmplifier;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo preAmplifier.
	 * 
	 * @param preAmplifier
	 *            O novo valor de preAmplifier
	 */
	public void setPreAmplifier(OpticalAmplifier preAmplifier) {
		this.preAmplifier = preAmplifier;
	}

	/**
	 * Metodo acessor para obter o valor do atributo time.
	 * 
	 * @return O valor de time
	 */
	public double getTime() {
		return time;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo time.
	 * 
	 * @param time
	 *            O novo valor de time
	 */
	public void setTime(double time) {
		this.time = time;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + destinationNode;
		result = prime * result + lambda;
		long temp;
		temp = Double.doubleToLongBits(length);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + sourceNode;
		return result;
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
		Fiber other = (Fiber) obj;
		if (destinationNode != other.destinationNode)
			return false;
		if (lambda != other.lambda)
			return false;
		if (Double.doubleToLongBits(length) != Double.doubleToLongBits(other.length))
			return false;
		if (sourceNode != other.sourceNode)
			return false;
		return true;
	}
}
