/**
 * 
 */
package br.cns24.som;

import java.util.List;
import java.util.Vector;

/**
 * .
 * 
 * @author Danilo Ara�jo
 */
public class IRedeKohonen {
	private int dimensaoCamadaEntrada;

	private int dimensaoCamadaSaida;

	private INeuronio[][] camadaSaida;

	private List<IPadraoTreinamento> padroesTreinamento;

	private List<IPadraoTreinamento> padroesTreinamentoOriginal;

	private static final double TX_APREND_MIN = 0.01;

	private static final double VIZ_MIN = 0.99;

	public IRedeKohonen() {
		super();
	}

	public IRedeKohonen(int dimensaoCamadaEntrada, int dimensaoCamadaSaida) {
		this.dimensaoCamadaEntrada = dimensaoCamadaEntrada;
		this.dimensaoCamadaSaida = dimensaoCamadaSaida;
		inicializar();
	}

	public void inicializar() {
		camadaSaida = new INeuronio[dimensaoCamadaSaida][dimensaoCamadaSaida];
		for (int x = 0; x < dimensaoCamadaSaida; x++) {
			for (int y = 0; y < dimensaoCamadaSaida; y++) {
				camadaSaida[x][y] = new INeuronio(this, x, y, dimensaoCamadaEntrada);
			}
		}
	}

	public void iniciarAprendizado(int numeroIteracoes, double taxaVizinhanca, double taxaAprendizado)
			throws RedeException {
		// Validando se o padr�o de treinamento foi carregado
		if (padroesTreinamento == null || padroesTreinamento.size() == 0) {
			throw new RedeException("Padr�o de treinamento n�o carregado");
		}
		double razaoIteracao = 1;
		double valorAbrangencia = taxaVizinhanca * dimensaoCamadaSaida;
		double taxaAprendizadoIteracao = taxaAprendizado;
		int numeroPadroes = padroesTreinamento.size();
		List<IPadraoTreinamento> padroesAprendizado = new Vector<IPadraoTreinamento>(padroesTreinamento.size());
		// Realizando as itera��es
		int iteracaoAtual = 0;

		while (iteracaoAtual < numeroIteracoes && taxaAprendizadoIteracao > TX_APREND_MIN && valorAbrangencia > VIZ_MIN) {
			// Calculando a razao da iteracao
			razaoIteracao = ((double) iteracaoAtual / numeroIteracoes);

			// Realizando uma c�pia dos padroes de treinamento
			padroesAprendizado.clear();
			padroesAprendizado.addAll(padroesTreinamento);

			// Calculando as taxas de vizinhanca e de aprendizado com decaimento
			// a cada iteracao
			taxaAprendizadoIteracao = taxaAprendizado * (1 - razaoIteracao);

			// O valor de abrangencia � baseado no tamanho da area da camada da
			// saida (taxaVizinhanca x dimensaoCamadaSaida) e na razao das
			// interacoes
			// + Nas interacoes iniciais, a abrangencia � toda a taxaVizinhanca
			// x mapa
			// + Nas interacoes finais, a abrangencia � um percentual pequeno da
			// taxaVizinhanca
			valorAbrangencia = taxaVizinhanca * dimensaoCamadaSaida * (1 - razaoIteracao);

			if (taxaAprendizadoIteracao < 0.001) {
				break;
			}

			// Realizando o treinamento da rede com os padroes de aprendizado
			IPadraoTreinamento padraoAtual;
			Integer[] padraoAtualArray;
			for (int i = 0; i < numeroPadroes; i++) {

				// Lendo o padr�o atual de forma aleat�ria
				padraoAtual = padroesAprendizado.get((int) (Math.random() * (numeroPadroes - i)));
				padraoAtualArray = padraoAtual.getCaracteristicasArray();

				// Buscando o melhor neurodio (menor distancia euclidiana) do
				// padrao atual
				INeuronio melhorINeuronio = buscarMelhorINeuronio(padraoAtualArray);

				// Atualizando todos os INeuronios da rede, e calculando a taxa
				// de aprendizado da rede
				// long t3 = System.currentTimeMillis();
				int posXIni, posYIni, posXFim, posYFim;

				// Definindo a �rea de atualiza��o dos INeuronios
				posXIni = (int) Math.ceil(melhorINeuronio.getPosX() - valorAbrangencia);
				if (posXIni < 0)
					posXIni = 0;
				posYIni = (int) Math.ceil(melhorINeuronio.getPosY() - valorAbrangencia);
				if (posYIni < 0)
					posYIni = 0;

				posXFim = (int) Math.floor(melhorINeuronio.getPosX() + valorAbrangencia);
				if (posXFim > dimensaoCamadaSaida)
					posXFim = dimensaoCamadaSaida;
				posYFim = (int) Math.floor(melhorINeuronio.getPosY() + valorAbrangencia);
				if (posYFim > dimensaoCamadaSaida)
					posYFim = dimensaoCamadaSaida;

				for (int x = posXIni; x < posXFim; x++) {
					// if (Math.abs(melhorINeuronio.getPosX() - x) >
					// valorAbrangencia) continue;
					for (int y = posYIni; y < posYFim; y++) {
						// if (Math.abs(melhorINeuronio.getPosY() - y) >
						// valorAbrangencia) continue;
						camadaSaida[x][y].atualizarPesos(padraoAtualArray, melhorINeuronio, taxaAprendizadoIteracao,
								valorAbrangencia);
					}
				}
				// RedeKohonen.t2 += System.currentTimeMillis() - t3;
				// Removendo o padrao lido da lista de padroes de aprendizado
				padroesAprendizado.remove(padraoAtual);
			}
			iteracaoAtual++;
		}
		// System.out.println("Tempo de execu��o (t2): " + t2 + " ms.");
		exibirResultadoFinal();
	}

	private void exibirResultadoFinal() {
		StringBuilder sb = null;
		System.out.println("\n--------------------------------------------------------------------------------");
		System.out.println("\n---------------------------RESULTADOS-------------------------------------------");
		System.out.println("\n--------------------------------------------------------------------------------");
		for (int iPadrao = 0; iPadrao < padroesTreinamento.size(); iPadrao++) {
			sb = new StringBuilder();
			INeuronio melhorINeuronio = buscarMelhorINeuronio(padroesTreinamento.get(iPadrao).getCaracteristicasArray());
			padroesTreinamento.get(iPadrao).setPosX(melhorINeuronio.getPosX());
			padroesTreinamento.get(iPadrao).setPosY(melhorINeuronio.getPosY());
			sb.append(padroesTreinamento.get(iPadrao).getRotulo() + ";" + (int) melhorINeuronio.getPosX() + ";"
					+ (int) melhorINeuronio.getPosY());
			for (Integer valor : padroesTreinamentoOriginal.get(iPadrao).getCaracteristicas()) {
				sb.append(";").append(Util.decimalFormat.format(valor));
			}
			System.out.println(sb.toString());
		}
		System.out.println("\n--------------------------------------------------------------------------------");
		System.out.println("\n---------------------------RESULTADOS-------------------------------------------");
		System.out.println("\n--------------------------------------------------------------------------------");
	}

	public INeuronio buscarMelhorINeuronio(Integer[] padrao) {
		INeuronio melhorINeuronio = camadaSaida[0][0];
		double menorDistanciaEuclidiana, menorSomaQuadratica;
		menorSomaQuadratica = Double.MAX_VALUE;
		menorDistanciaEuclidiana = melhorINeuronio.calcularDistanciaEuclidiana(padrao, menorSomaQuadratica);
		for (int x = 0; x < dimensaoCamadaSaida; x++) {
			for (int y = 0; y < dimensaoCamadaSaida; y++) {
				double distanciaEuclidiana = camadaSaida[x][y].calcularDistanciaEuclidiana(padrao, menorSomaQuadratica);
				if (distanciaEuclidiana < menorDistanciaEuclidiana) {
					menorDistanciaEuclidiana = distanciaEuclidiana;
					menorSomaQuadratica = distanciaEuclidiana * distanciaEuclidiana;
					melhorINeuronio = camadaSaida[x][y];
				}
			}
		}
		return melhorINeuronio;
	}

	public INeuronio buscarMelhorINeuronioCrossover(Integer[] padrao) {
		INeuronio melhorINeuronio = camadaSaida[0][0];
		double menorDistanciaEuclidiana, menorSomaQuadratica;
		menorSomaQuadratica = Double.MAX_VALUE;
		menorDistanciaEuclidiana = melhorINeuronio.calcularDistanciaEuclidiana(padrao, menorSomaQuadratica);
		for (int x = 0; x < dimensaoCamadaSaida; x++) {
			for (int y = 0; y < dimensaoCamadaSaida; y++) {
				double distanciaEuclidiana = camadaSaida[x][y].calcularDistanciaEuclidiana(padrao, menorSomaQuadratica);
				if (distanciaEuclidiana < menorDistanciaEuclidiana) {
					menorDistanciaEuclidiana = distanciaEuclidiana;
					menorSomaQuadratica = distanciaEuclidiana * distanciaEuclidiana;
					melhorINeuronio = camadaSaida[x][y];
				}
			}
		}
		return melhorINeuronio;
	}

	public void normalizarPadroesTreinamento() {
		double[] menorValorEntrada = new double[dimensaoCamadaEntrada];
		double[] maiorValorEntrada = new double[dimensaoCamadaEntrada];

		// Inicializando os menores e maiores valores de cada dimens�o da
		// entrada
		for (int i = 0; i < dimensaoCamadaEntrada; i++) {
			menorValorEntrada[i] = Double.MAX_VALUE;
			maiorValorEntrada[i] = 0.0;
		}

		// Encontrando os menores e maiores valores de cada dimens�o da entrada
		for (int iPadrao = 0; iPadrao < padroesTreinamento.size(); iPadrao++) {
			for (int i = 0; i < dimensaoCamadaEntrada; i++) {
				if (padroesTreinamento.get(iPadrao).getCaracteristicasArray()[i] < menorValorEntrada[i]) {
					menorValorEntrada[i] = padroesTreinamento.get(iPadrao).getCaracteristicasArray()[i];
				}
				if (padroesTreinamento.get(iPadrao).getCaracteristicasArray()[i] > maiorValorEntrada[i]) {
					maiorValorEntrada[i] = padroesTreinamento.get(iPadrao).getCaracteristicasArray()[i];
				}
			}
		}

		// Normalizando os valores de cada dimens�o da entrada
		for (int iPadrao = 0; iPadrao < padroesTreinamento.size(); iPadrao++) {
			for (int i = 0; i < dimensaoCamadaEntrada; i++) {
				padroesTreinamento
						.get(iPadrao)
						.getCaracteristicas()
						.set(
								i,
								(int) Math
										.floor(0.1 + (0.8 * (padroesTreinamento.get(iPadrao).getCaracteristicasArray()[i] - menorValorEntrada[i]) / (maiorValorEntrada[i] - menorValorEntrada[i]))));
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
	 * @param dimensaoCamadaEntrada
	 *            O novo valor de dimensaoCamadaEntrada
	 */
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
	 * @param dimensaoCamadaSaida
	 *            O novo valor de dimensaoCamadaSaida
	 */
	public void setDimensaoCamadaSaida(int dimensaoCamadaSaida) {
		this.dimensaoCamadaSaida = dimensaoCamadaSaida;
	}

	/**
	 * M�todo acessor para obter o valor de camadaSaida
	 * 
	 * @return O valor de camadaSaida
	 */
	public INeuronio[][] getCamadaSaida() {
		return camadaSaida;
	}

	/**
	 * M�todo acessor para modificar o valor de camadaSaida
	 * 
	 * @param camadaSaida
	 *            O novo valor de camadaSaida
	 */
	public void setCamadaSaida(INeuronio[][] camadaSaida) {
		this.camadaSaida = camadaSaida;
	}

	/**
	 * M�todo acessor para obter o valor de padroesTreinamento
	 * 
	 * @return O valor de padroesTreinamento
	 */
	public List<IPadraoTreinamento> getPadroesTreinamento() {
		return padroesTreinamento;
	}

	/**
	 * M�todo acessor para modificar o valor de padroesTreinamento
	 * 
	 * @param padroesTreinamento
	 *            O novo valor de padroesTreinamento
	 */
	public void setPadroesTreinamento(List<IPadraoTreinamento> padroesTreinamento) {
		this.padroesTreinamento = padroesTreinamento;
	}

	/**
	 * M�todo acessor para obter o valor de padroesTreinamentoOriginal
	 * 
	 * @return O valor de padroesTreinamentoOriginal
	 */
	public List<IPadraoTreinamento> getPadroesTreinamentoOriginal() {
		return padroesTreinamentoOriginal;
	}

	/**
	 * M�todo acessor para modificar o valor de padroesTreinamentoOriginal
	 * 
	 * @param padroesTreinamentoOriginal
	 *            O novo valor de padroesTreinamentoOriginal
	 */
	public void setPadroesTreinamentoOriginal(List<IPadraoTreinamento> padroesTreinamentoOriginal) {
		this.padroesTreinamentoOriginal = padroesTreinamentoOriginal;
	}
}