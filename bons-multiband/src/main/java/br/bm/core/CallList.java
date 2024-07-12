package br.bm.core;

import java.util.List;
import java.util.Vector;

public class CallList {
	private List<Call> listaChamadas = new Vector<Call>();
	private int numEstablishedCalls;
	private double length;

	public CallList() {
		super();
		length = 0.0;
		numEstablishedCalls = 0;
	}

	/**
	 * Add a call in the list of calls
	 * 
	 * @param chamada
	 *            call for putting in the list
	 */
	public void addChamada(Call chamada) {
		listaChamadas.add(0, chamada);
		length += chamada.getLinkLength();
		numEstablishedCalls++;
	}
	

	/**
	 * @return
	 */
	public double getDistanciaMedia() {
		return (length / numEstablishedCalls++);
	}

	/**
	 * Cover all the list and pull away the calls that have finished - funcao
	 * sobrecarregada em rel.acima, adaptada p/ regeneradores
	 * 
	 * @param "tempoacumulado"
	 * @param vectorOfNodes_par
	 */
	public void retirarChamada(double time, Vector<Node> vectorOfNodes_par) {
		List<Call> removidos = new Vector<Call>();
		for (Call chamada : listaChamadas) {
			if (chamada.getFallTime() <= time) {
				chamada.deAllocCall(vectorOfNodes_par);
				removidos.add(chamada);
			}
		}
		listaChamadas.removeAll(removidos);
	}
	

	/**
	 * Cover all the list and rebuild the times
	 * 
	 * @param "tempoacumulado"
	 *            accumulated time received from "roteamento.h" file
	 */
	public void zerachamadas_mpu(double time) {
		for (Call i : listaChamadas) {
			i.setFallTime(i.getFallTime() - time);
		}
	}

	/**
	 * M�todo acessor para obter o valor do atributo listaChamadas.
	 * 
	 * @return O valor de listaChamadas
	 */
	public List<Call> getListaChamadas() {
		return listaChamadas;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo listaChamadas.
	 * 
	 * @param listaChamadas
	 *            O novo valor de listaChamadas
	 */
	public void setListaChamadas(List<Call> listaChamadas) {
		this.listaChamadas = listaChamadas;
	}

	/**
	 * M�todo acessor para obter o valor do atributo numEstablishedCalls.
	 * 
	 * @return O valor de numEstablishedCalls
	 */
	public int getNumEstablishedCalls() {
		return numEstablishedCalls;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo numEstablishedCalls.
	 * 
	 * @param numEstablishedCalls
	 *            O novo valor de numEstablishedCalls
	 */
	public void setNumEstablishedCalls(int numEstablishedCalls) {
		this.numEstablishedCalls = numEstablishedCalls;
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
}
