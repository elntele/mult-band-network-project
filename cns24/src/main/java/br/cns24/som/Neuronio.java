package br.cns24.som;

import java.util.Random;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Neuronio {
	private int posX;
	
	private int posY;
	
	private double[] pesos;
	
	private RedeKohonen rede;
	
	public Neuronio() {
	}
	
	public Neuronio(RedeKohonen rede, int posX, int posY, int qtdeSinapses) {
		this.rede = rede;
		inicializar(posX, posY, qtdeSinapses);
	}
	
	public void inicializar(int posX, int posY, int qtdeSinapses) {
		// Copiando o X,Y para os parametros locais
		this.posX = posX;
		this.posY = posY;
		
		// Inicializando os pesos das sinapses com valores aleatorios pequenos (r/2)
		pesos = new double[qtdeSinapses];
		Random r = new Random();
		for (int i = 0; i < qtdeSinapses; i++) {
			pesos[i] = (r.nextDouble() / 2.0);
		}
	}
	
	public double calcularDistanciaEuclidiana(Double[] padrao) {
		double distancia = 0;
		for (int i = 0; i < rede.getDimensaoCamadaEntrada(); i++) {
			double diferenca = padrao[i] - pesos[i];
			distancia += diferenca * diferenca;
		}
		return Math.sqrt(distancia);
	}
	
	public double calcularDistanciaEuclidiana(Double[] padrao, double menorSomaQuadratica) {
		double diferenca, distancia = 0;
		for (int i = 0; i < rede.getDimensaoCamadaEntrada(); i++) {
			diferenca = padrao[i] - pesos[i];
			distancia += (diferenca * diferenca);
			if (distancia > menorSomaQuadratica) return Double.MAX_VALUE;
		}
		return Math.sqrt(distancia);
	}
	
	public void atualizarPesos(Double[] padrao, Neuronio melhorNeuronio, double taxaAprendizado, double valorAbrangencia) {
		double diferencaX, diferencaY, distanciaHipotenusa, distanciaQuadratica, taxaVizinhanca;
		
		// Calculando a Hipotenusa = Raiz quadrada da Soma do Quadrado dos Catetos
		diferencaX = melhorNeuronio.getPosX() - this.posX;
		diferencaY = melhorNeuronio.getPosY() - this.posY;
		
		// Calculando a distancia pelo valor da abrangencia ao quadrado
		distanciaHipotenusa = Math.sqrt((diferencaX * diferencaX) + (diferencaY * diferencaY));
		
		// Validando se o neuronio se encontra dentro da abrangencia
		if (distanciaHipotenusa <= valorAbrangencia) {
			// Calculando a distancia quadr�tica
			distanciaQuadratica = distanciaHipotenusa / valorAbrangencia;
			distanciaQuadratica *= distanciaQuadratica;
			
			// Calculando a taxa da vizinhan�a baseado na Gaussiana 
			//taxaVizinhanca = Math.exp(-1 * distanciaQuadratica);
			
			// Calculando a taxa da vizinhan�a baseado no Mexican-Hat
			taxaVizinhanca = (1 - distanciaQuadratica) * Math.exp(-1 * distanciaQuadratica);
			
			// Atualizando cada todos os pesos do neur�nio
			for (int i = 0; i < pesos.length; i++) {
				pesos[i] += taxaAprendizado * taxaVizinhanca * (padrao[i] - pesos[i]);
			}
		}
	}

	/**
	 * M�todo acessor para obter o valor de posX
	 *
	 * @return O valor de posX
	 */
	public int getPosX() {
		return posX;
	}

	/**
	 * M�todo acessor para modificar o valor de posX
	 *
	 * @param posX O novo valor de posX
	 */
	@XmlElement
	public void setPosX(int posX) {
		this.posX = posX;
	}

	/**
	 * M�todo acessor para obter o valor de posY
	 *
	 * @return O valor de posY
	 */
	public int getPosY() {
		return posY;
	}

	/**
	 * M�todo acessor para modificar o valor de posY
	 *
	 * @param posY O novo valor de posY
	 */
	public void setY(int posY) {
		this.posY = posY;
	}

	/**
	 * M�todo acessor para obter o valor de pesos
	 *
	 * @return O valor de pesos
	 */
	public double[] getPesos() {
		return pesos;
	}

	/**
	 * M�todo acessor para modificar o valor de pesos
	 *
	 * @param pesos O novo valor de pesos
	 */
	@XmlElement
	public void setPesos(double[] pesos) {
		this.pesos = pesos;
	}

	@XmlElement
	public void setPosY(int posY) {
		this.posY = posY;
	}
	
}