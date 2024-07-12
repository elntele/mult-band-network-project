package br.cns24.som;

import java.util.List;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RedeKohonen {
	//public static long t2 = 0;
	
	private int dimensaoCamadaEntrada;
	
	private int dimensaoCamadaSaida;
	
	private Neuronio[][] camadaSaida;
	
	private List<PadraoTreinamento> padroesTreinamento = new Vector<>();
	
	private List<PadraoTreinamento> padroesTreinamentoOriginal = new Vector<>();
	
	private static final double TX_APREND_MIN = 0.01;
	
	private static final double VIZ_MIN = 0.99;
	
	public RedeKohonen() {
		super();
	}
	
	public RedeKohonen(int dimensaoCamadaEntrada, int dimensaoCamadaSaida) {
		this.dimensaoCamadaEntrada = dimensaoCamadaEntrada;
		this.dimensaoCamadaSaida = dimensaoCamadaSaida;
		inicializar();
	}
	
	public void inicializar() {
		camadaSaida = new Neuronio[dimensaoCamadaSaida][dimensaoCamadaSaida];
		for (int x = 0; x < dimensaoCamadaSaida; x++) {
			for (int y = 0; y < dimensaoCamadaSaida; y++) {
				camadaSaida[x][y] = new Neuronio(this, x, y, dimensaoCamadaEntrada);
			}
		}
	}
	
	public void iniciarAprendizado(int numeroIteracoes, double taxaVizinhanca, double taxaAprendizado) throws RedeException {
		// Validando se o padr�o de treinamento foi carregado
		if (padroesTreinamento == null || padroesTreinamento.size() == 0) {
			throw new RedeException("Padr�o de treinamento n�o carregado");
		}
		double razaoIteracao = 1;
		double valorAbrangencia = taxaVizinhanca * dimensaoCamadaSaida;
		double taxaAprendizadoIteracao = taxaAprendizado;
		int numeroPadroes = padroesTreinamento.size();
		List <PadraoTreinamento> padroesAprendizado = new Vector<PadraoTreinamento>(padroesTreinamento.size());
		// Realizando as itera��es
		int iteracaoAtual = 0;
		
		while (iteracaoAtual < numeroIteracoes && taxaAprendizadoIteracao > TX_APREND_MIN && valorAbrangencia > VIZ_MIN) {
			// Calculando a razao da iteracao
			razaoIteracao = ((double)iteracaoAtual / numeroIteracoes);
			System.out.printf("%.4f\n", (1.0*iteracaoAtual)/numeroIteracoes);
			// Realizando uma c�pia dos padroes de treinamento
			padroesAprendizado.clear();
			padroesAprendizado.addAll(padroesTreinamento);
			
			// Calculando as taxas de vizinhanca e de aprendizado com decaimento a cada iteracao
			taxaAprendizadoIteracao = taxaAprendizado * (1 - razaoIteracao);
			
			// O valor de abrangencia � baseado no tamanho da area da camada da saida (taxaVizinhanca x dimensaoCamadaSaida) e na razao das interacoes
			// + Nas interacoes iniciais, a abrangencia � toda a taxaVizinhanca x mapa
			// + Nas interacoes finais, a abrangencia � um percentual pequeno da taxaVizinhanca
			valorAbrangencia = taxaVizinhanca * dimensaoCamadaSaida * (1 - razaoIteracao);
			
			if (taxaAprendizadoIteracao < 0.001){
				break;
			}
			
			// Realizando o treinamento da rede com os padroes de aprendizado
			PadraoTreinamento padraoAtual;
			Double[] padraoAtualArray;
			for (int i = 0; i < numeroPadroes; i++) {
				
				// Lendo o padr�o atual de forma aleat�ria
				padraoAtual = padroesAprendizado.get((int)(Math.random() * (numeroPadroes - i)));
				padraoAtualArray = padraoAtual.getCaracteristicasArray();
				
				// Buscando o melhor neurodio (menor distancia euclidiana) do padrao atual
				Neuronio melhorNeuronio = buscarMelhorNeuronio(padraoAtualArray);
				
				// Atualizando todos os neuronios da rede, e calculando a taxa de aprendizado da rede
				//long t3 = System.currentTimeMillis();
				int posXIni, posYIni, posXFim, posYFim;
				
				// Definindo a �rea de atualiza��o dos neuronios
				posXIni = (int) Math.ceil(melhorNeuronio.getPosX() - valorAbrangencia);
				if (posXIni < 0) posXIni = 0;
				posYIni = (int) Math.ceil(melhorNeuronio.getPosY() - valorAbrangencia);
				if (posYIni < 0) posYIni = 0;
				
				posXFim = (int) Math.floor(melhorNeuronio.getPosX() + valorAbrangencia);
				if (posXFim > dimensaoCamadaSaida) posXFim = dimensaoCamadaSaida;
				posYFim = (int) Math.floor(melhorNeuronio.getPosY() + valorAbrangencia);
				if (posYFim > dimensaoCamadaSaida) posYFim = dimensaoCamadaSaida;
				
				for (int x = posXIni; x < posXFim; x++) {
					//if (Math.abs(melhorNeuronio.getPosX() - x) > valorAbrangencia) continue;
					for (int y = posYIni; y < posYFim; y++) {
						//if (Math.abs(melhorNeuronio.getPosY() - y) > valorAbrangencia) continue;
						camadaSaida[x][y].atualizarPesos(padraoAtualArray, melhorNeuronio, taxaAprendizadoIteracao, valorAbrangencia);
					}
				}
				//RedeKohonen.t2 += System.currentTimeMillis() - t3;
				// Removendo o padrao lido da lista de padroes de aprendizado
				padroesAprendizado.remove(padraoAtual);
			}
			iteracaoAtual++;
		}
		//System.out.println("Tempo de execu��o (t2): " + t2 + " ms.");
		exibirResultadoFinal();
	}
	
	private void exibirResultadoFinal(){
		StringBuilder sb = null;
		System.out.println("\n--------------------------------------------------------------------------------");
		System.out.println("\n---------------------------RESULTADOS-------------------------------------------");
		System.out.println("\n--------------------------------------------------------------------------------");
		for (int iPadrao = 0; iPadrao < padroesTreinamento.size(); iPadrao++) {
			sb = new StringBuilder();
			Neuronio melhorNeuronio = buscarMelhorNeuronio(padroesTreinamento.get(iPadrao).getCaracteristicasArray());
			padroesTreinamento.get(iPadrao).setPosX(melhorNeuronio.getPosX());
			padroesTreinamento.get(iPadrao).setPosY(melhorNeuronio.getPosY());
			sb.append(padroesTreinamento.get(iPadrao).getRotulo() + ";" + (int)melhorNeuronio.getPosX() + ";"+ (int)melhorNeuronio.getPosY());
			for (Double valor : padroesTreinamentoOriginal.get(iPadrao).getCaracteristicas()){
				sb.append(";").append(Util.decimalFormat.format(valor));
			}
			System.out.println(sb.toString());
		}
		System.out.println("\n--------------------------------------------------------------------------------");
		System.out.println("\n---------------------------RESULTADOS-------------------------------------------");
		System.out.println("\n--------------------------------------------------------------------------------");
	}
	
	public Neuronio buscarMelhorNeuronio(Double[] padrao){
		Neuronio melhorNeuronio = camadaSaida[0][0];
		double menorDistanciaEuclidiana, menorSomaQuadratica;
		menorSomaQuadratica = Double.MAX_VALUE;
		menorDistanciaEuclidiana = melhorNeuronio.calcularDistanciaEuclidiana(padrao, menorSomaQuadratica);
		for (int x = 0; x < dimensaoCamadaSaida; x++) {
			for (int y = 0; y < dimensaoCamadaSaida; y++) {
				double distanciaEuclidiana = camadaSaida[x][y].calcularDistanciaEuclidiana(padrao, menorSomaQuadratica);
				if (distanciaEuclidiana < menorDistanciaEuclidiana) {
					menorDistanciaEuclidiana = distanciaEuclidiana;
					menorSomaQuadratica = distanciaEuclidiana * distanciaEuclidiana;
					melhorNeuronio = camadaSaida[x][y];
				}
			}
		}
		return melhorNeuronio;
	}
	
	public void normalizarPadroesTreinamento() {
		double[] maiorValorEntrada = new double[dimensaoCamadaEntrada];
		
		// Inicializando os menores e maiores valores de cada dimens�o da entrada
		for (int i = 0; i < dimensaoCamadaEntrada; i++) {
			maiorValorEntrada[i] = 0.0;
		}
		
		// Encontrando os menores e maiores valores de cada dimens�o da entrada
		for (int iPadrao = 0; iPadrao < padroesTreinamento.size(); iPadrao++) {
			for (int i = 0; i < dimensaoCamadaEntrada; i++) {
				if (padroesTreinamento.get(iPadrao).getCaracteristicasArray()[i] > maiorValorEntrada[i]) {
					maiorValorEntrada[i] = padroesTreinamento.get(iPadrao).getCaracteristicasArray()[i];
				} 
			}
		}
		
		// Normalizando os valores de cada dimens�o da entrada
		for (int iPadrao = 0; iPadrao < padroesTreinamento.size(); iPadrao++) {
			for (int i = 0; i < dimensaoCamadaEntrada; i++) {
				padroesTreinamento.get(iPadrao).getCaracteristicas().set(i, padroesTreinamento.get(iPadrao).getCaracteristicasArray()[i]/ maiorValorEntrada[i]);
			}
		}
	}
	
	/**
	 * M�todo acessor para obter o valor de dimensaoCamadaEntrada
	 *
	 * @return O valor de dimensaoCamadaEntrada
	 */
	public int getDimensaoCamadaEntrada() {
		return dimensaoCamadaEntrada;
	}

	/**
	 * M�todo acessor para modificar o valor de dimensaoCamadaEntrada
	 *
	 * @param dimensaoCamadaEntrada O novo valor de dimensaoCamadaEntrada
	 */
	@XmlElement
	public void setDimensaoCamadaEntrada(int dimensaoCamadaEntrada) {
		this.dimensaoCamadaEntrada = dimensaoCamadaEntrada;
	}

	/**
	 * M�todo acessor para obter o valor de dimensaoCamadaSaida
	 *
	 * @return O valor de dimensaoCamadaSaida
	 */
	public int getDimensaoCamadaSaida() {
		return dimensaoCamadaSaida;
	}

	/**
	 * M�todo acessor para modificar o valor de dimensaoCamadaSaida
	 *
	 * @param dimensaoCamadaSaida O novo valor de dimensaoCamadaSaida
	 */
	@XmlElement
	public void setDimensaoCamadaSaida(int dimensaoCamadaSaida) {
		this.dimensaoCamadaSaida = dimensaoCamadaSaida;
	}

	/**
	 * M�todo acessor para obter o valor de camadaSaida
	 *
	 * @return O valor de camadaSaida
	 */
	public Neuronio[][] getCamadaSaida() {
		return camadaSaida;
	}

	/**
	 * M�todo acessor para modificar o valor de camadaSaida
	 *
	 * @param camadaSaida O novo valor de camadaSaida
	 */
	@XmlElement
	public void setCamadaSaida(Neuronio[][] camadaSaida) {
		this.camadaSaida = camadaSaida;
	}

	/**
	 * M�todo acessor para obter o valor de padroesTreinamento
	 *
	 * @return O valor de padroesTreinamento
	 */
	public List<PadraoTreinamento> getPadroesTreinamento() {
		return padroesTreinamento;
	}

	/**
	 * M�todo acessor para modificar o valor de padroesTreinamento
	 *
	 * @param padroesTreinamento O novo valor de padroesTreinamento
	 */
	@XmlElement
	public void setPadroesTreinamento(List<PadraoTreinamento> padroesTreinamento) {
		this.padroesTreinamento = padroesTreinamento;
	}

	/**
	 * M�todo acessor para obter o valor de padroesTreinamentoOriginal
	 *
	 * @return O valor de padroesTreinamentoOriginal
	 */
	public List<PadraoTreinamento> getPadroesTreinamentoOriginal() {
		return padroesTreinamentoOriginal;
	}

	/**
	 * M�todo acessor para modificar o valor de padroesTreinamentoOriginal
	 *
	 * @param padroesTreinamentoOriginal O novo valor de padroesTreinamentoOriginal
	 */
	@XmlElement
	public void setPadroesTreinamentoOriginal(
			List<PadraoTreinamento> padroesTreinamentoOriginal) {
		this.padroesTreinamentoOriginal = padroesTreinamentoOriginal;
	}
}