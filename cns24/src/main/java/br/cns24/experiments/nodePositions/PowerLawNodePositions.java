package br.cns24.experiments.nodePositions;

public class PowerLawNodePositions implements NodePositionsGenerator {
	private static final PowerLawNodePositions instance = new PowerLawNodePositions();

	/**
	 * Construtor da classe.
	 */
	private PowerLawNodePositions() {

	}

	@Override
	public double[][] createNodePositions(int numNodes) {
		double[][] positions = new double[numNodes][2];
		double dangle = Math.PI * 2 / numNodes;
		for (int i = 0; i < numNodes; i++) {
			positions[i][0] = 50 + 50 * Math.cos(i * dangle);
			positions[i][1] = 50 + 50 * Math.sin(i * dangle);
		}
		return positions;
	}

	@Override
	public double[][] createNodePositions(int numNodes, Integer[][] degree) {
		double[][] positions = new double[numNodes][2];
		double dangle = Math.PI * 2 / numNodes;
		int maxDegree = 0;
		for (int i = 0; i < numNodes; i++) {
			if (degree[i][i] > maxDegree) {
				maxDegree = degree[i][i];
			}
		}
		for (int i = 0; i < numNodes; i++) {
			positions[i][0] = 50 + 50 * (1 - degree[i][i] / (1.0 * maxDegree)) * Math.cos(i * dangle);
			positions[i][1] = 50 + 50 * (1 - degree[i][i] / (1.0 * maxDegree)) * Math.sin(i * dangle);
		}
		return positions;
	}

	/**
	 * M�todo acessor para obter o valor do atributo instance.
	 * 
	 * @return O atributo instance
	 */
	public static PowerLawNodePositions getInstance() {
		return instance;
	}

}
