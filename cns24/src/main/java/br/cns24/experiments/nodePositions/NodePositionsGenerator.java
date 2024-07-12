package br.cns24.experiments.nodePositions;

/**
 * Cria posi��es dos nós.
 * 
 * @author Danilo Ara�jo
 * 
 * @since 17/08/2012
 */
public interface NodePositionsGenerator {
	/**
	 * Cria posi��o para os nós.
	 * 
	 * @return Array de posi��o dos nós.
	 */
	public double[][] createNodePositions(int numNodes);
	
	/**
	 * Cria posi��o para os nós.
	 * 
	 * @return Array de posi��o dos nós.
	 */
	public double[][] createNodePositions(int numNodes, Integer[][] degree);
}
