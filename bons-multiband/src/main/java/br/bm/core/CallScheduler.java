/**
 * 
 */
package br.bm.core;

/**
 * .
 * 
 * @author Danilo Araujo
 */
public class CallScheduler {
	private double currentTime; // stores the current simulation time
	private int nextSourceNode; // the generated source node
	private int nextDestinationNode; // the generated Destination node
	private double duration; // duration of the call

	public CallScheduler() {
		currentTime = 0;
		nextSourceNode = -1;
		nextDestinationNode = -1;
		duration = 0;
	}
	
	public void resetTime_mpu(){
		
	}
	
	public void generateCallRequisition() {
		
	}

	/**
	 * Metodo acessor para obter o valor do atributo currentTime.
	 * 
	 * @return O valor de currentTime
	 */
	public double getCurrentTime() {
		return currentTime;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo currentTime.
	 * 
	 * @param currentTime
	 *            O novo valor de currentTime
	 */
	public void setCurrentTime(double currentTime) {
		this.currentTime = currentTime;
	}

	/**
	 * Metodo acessor para obter o valor do atributo nextSourceNode.
	 * 
	 * @return O valor de nextSourceNode
	 */
	public int getNextSourceNode() {
		return nextSourceNode;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo nextSourceNode.
	 * 
	 * @param nextSourceNode
	 *            O novo valor de nextSourceNode
	 */
	public void setNextSourceNode(int nextSourceNode) {
		this.nextSourceNode = nextSourceNode;
	}

	/**
	 * Metodo acessor para obter o valor do atributo nextDestinationNode.
	 * 
	 * @return O valor de nextDestinationNode
	 */
	public int getNextDestinationNode() {
		return nextDestinationNode;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo nextDestinationNode.
	 * 
	 * @param nextDestinationNode
	 *            O novo valor de nextDestinationNode
	 */
	public void setNextDestinationNode(int nextDestinationNode) {
		this.nextDestinationNode = nextDestinationNode;
	}

	/**
	 * Metodo acessor para obter o valor do atributo duration.
	 * 
	 * @return O valor de duration
	 */
	public double getDuration() {
		return duration;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo duration.
	 * 
	 * @param duration
	 *            O novo valor de duration
	 */
	public void setDuration(double duration) {
		this.duration = duration;
	}

}
