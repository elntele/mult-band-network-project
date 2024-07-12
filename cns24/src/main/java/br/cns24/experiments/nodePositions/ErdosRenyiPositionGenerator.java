package br.cns24.experiments.nodePositions;

public class ErdosRenyiPositionGenerator implements NodePositionsGenerator {
	private static final ErdosRenyiPositionGenerator instance = new ErdosRenyiPositionGenerator();

	/**
	 * Construtor da classe.
	 */
	private ErdosRenyiPositionGenerator() {
	}

	@Override
	public double[][] createNodePositions(int numNodes) {
		double[][] positions = new double[numNodes][2];
		double dangle = Math.PI * 2 / numNodes;
		for (int i = 0; i < numNodes; i++) {
			positions[i][0] = 50 + 50 * (1 - Math.random() * Math.cos(i * dangle));
			positions[i][1] = 50 + 50 * (1 - Math.random()  * Math.sin(i * dangle));
		}
		return positions;
	}

	@Override
	public double[][] createNodePositions(int numNodes, Integer[][] degree) {
		return createNodePositions(numNodes);
	}

	/**
	 * M�todo acessor para obter o valor do atributo instance.
	 * 
	 * @return O atributo instance
	 */
	public static ErdosRenyiPositionGenerator getInstance() {
		return instance;
	}

}
