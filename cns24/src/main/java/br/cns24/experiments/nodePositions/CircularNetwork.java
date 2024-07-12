package br.cns24.experiments.nodePositions;

public class CircularNetwork implements NodePositionsGenerator {
	private static final CircularNetwork instance = new CircularNetwork();

	/**
	 * Construtor da classe.
	 */
	private CircularNetwork() {

	}

	
	public double[][] createNodePositionsWheel(int numNodes) {
		double[][] positions = new double[numNodes][2];
		double dangle = Math.PI * 2 / (numNodes-1);
		for (int i = 1; i < numNodes; i++) {
			positions[i][0] = 50 + 50 * Math.cos(i * dangle);
			positions[i][1] = 50 + 50 * Math.sin(i * dangle);
		}
		positions[0][0] = 50;
		positions[0][1] = 50;
		return positions;
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

	/**
	 * M�todo acessor para obter o valor do atributo instance.
	 * 
	 * @return O atributo instance
	 */
	public static CircularNetwork getInstance() {
		return instance;
	}

	@Override
	public double[][] createNodePositions(int numNodes, Integer[][] degree) {
		return createNodePositions(numNodes);
	}

}
