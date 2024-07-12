/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: Dijkstra.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	27/12/2013		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * 
 * @author Danilo
 * @since 27/12/2013
 */
public class Dijkstra {
	private static final Double INF = 1.0E50;
	public static Map<String, List<Integer>> calculateAll(Integer[][] adjacencyMatrix) {
		int n = adjacencyMatrix.length;
		Double[][] am = new Double[n][n];
		for (int source = 0; source < n; source++) {
			am[source][source] = 0.0;
			for (int destination = source + 1; destination < n; destination++) {
				am[source][destination] = adjacencyMatrix[source][destination].doubleValue();
				am[destination][source] = am[source][destination];
			}
		}

		return calculateAll(am);
	}

	public static Map<String, List<Integer>> calculateAll(Double[][] input) {
		Map<String, List<Integer>> ret = new HashMap<String, List<Integer>>();
		int n = input.length;
		Double[][] adjacencyMatrix = new Double[n][n]; 
		
		for (int source = 0; source < n; source++) {
			adjacencyMatrix[source][source] = INF;
			for (int destination = source + 1; destination < n; destination++) {
				if (input[source][destination] == 0) {
					adjacencyMatrix[source][destination] = INF;
				} else {
					adjacencyMatrix[source][destination] = input[source][destination];
				}
				adjacencyMatrix[destination][source] = adjacencyMatrix[source][destination];
			}
		}
		
		for (int source = 0; source < n; source++) {
			for (int destination = source + 1; destination < n; destination++) {
				List<Integer> route = calculate(adjacencyMatrix, source, destination);
				ret.put(source + "-" + destination, route);
				ret.put(destination + "-" + source, route);
			}
		}

		return ret;
	}

	public static List<Integer> calculate(Double[][] adjacencyMatrix, int source, int destination) {
		Vector<Integer> S = new Vector<Integer>(); // conter� todos os nós para
													// os quais j� se foi
		// encontrado o caminho com menor fator de ru�do
		Vector<Integer> Q = new Vector<Integer>(); // conter� os nós para os
													// quais ainda n�o foram
		// encontrados os caminhos com menor fator de ru�do
		double[] d; // vetor de distancias: conter� o valor do menor fator de
		// ru�do entre a origem e o n� (�ndice do vetor)
		int[] father; // indica o n� anterior de cada n�
		int numeroNos = adjacencyMatrix.length;
		double menorDist, distEntreNos;
		int vertex, noMenorDisTotal, cont;
		Vector<Integer> retorno = new Vector<Integer>();

		d = new double[numeroNos];
		father = new int[numeroNos];

		for (vertex = 0; vertex < numeroNos; vertex++) {
			d[vertex] = INF;
			father[vertex] = -1;
			Q.add(vertex);
		}

		d[source] = 0;

		int[] temp = new int[1];
		while (Q.isEmpty() != true) {
			noMenorDisTotal = extraiMinimo_fnb(numeroNos, S, Q, d);
			if (noMenorDisTotal != -1) {
				for (cont = 0; cont < numeroNos; cont++) {
					distEntreNos = adjacencyMatrix[noMenorDisTotal][cont];

					if (cont != noMenorDisTotal && !pertence(S, cont, temp) && distEntreNos < INF) {
						menorDist = d[noMenorDisTotal] + distEntreNos;
						// RELAX
						if (d[cont] > menorDist) {
							d[cont] = menorDist;
							father[cont] = noMenorDisTotal;
						}
					}
				}
			}

		}
		findRoute(father[destination], destination, father, retorno, source);
		return retorno;
	}

	/********************************************************************
	 * DETERMINA��O DE ROTAS ---- DIJKSTRA POR MINIMO FATOR DE RU�DO
	 *********************************************************************/

	/******************************************************************
	 ***** FUNCTION....: pertence_fnb DESCRIPTION.: Verifies if an element is in a
	 * dynamic vector or not and modifies the parameter "posicao_par" to the
	 * element position. PARAMETERS.: a dynamic vector<int>, a key(int) and an
	 * integer variable
	 *******************************************************************/
	static boolean pertence(Vector<Integer> vector_par, int key_par, int[] posicao_par) {
		for (int i = 0; i < vector_par.size(); i++) {
			if (vector_par.get(i) == key_par) {
				posicao_par[0] = i;
				return (true);
			}
		}
		return (false);
	}

	/******************************************************************
	 ***** FUNCTION....: findtrack_fnb DESCRIPTION.: Updates the vector "track"
	 * received as parameter, for containing in the opposite order all the path
	 * until the current node PARAMETERS.: the next to last node(int), last
	 * node(int), vector father(int)=>indicates the previous node for each node
	 * (index of the vector), vector<int> track=> modified for containing the
	 * path, source of path (int).
	 *******************************************************************/
	static void findRoute(int menorFtotal_par, int cont_par, int father_par[], Vector<Integer> track_par, int source_par) {
		int previous_loc;
		track_par.clear();
		track_par.add(cont_par);
		if (menorFtotal_par != -1) {
			track_par.add(menorFtotal_par);
			previous_loc = menorFtotal_par;
			while (previous_loc != source_par) {
				if (father_par[previous_loc] != -1) {
					track_par.add(father_par[previous_loc]);
					previous_loc = father_par[previous_loc];
				} else {
					track_par.clear();
					break;
				}
			}
		} else
			track_par.clear();
	}

	/******************************************************************
	 ***** FUNCTION....: extraiMinimo_fnb DESCRIPTION.: Searchs the less element of
	 * "vector of distances" that was not calculated the less path to it.
	 * PARAMETERS.: network's size(int), vector<int> S_par => contains all the
	 * nodes for which was found the path with less noise factor , vector<int>
	 * Q_par=> nodes which was not found the less path, vector d or distance
	 * (long double)=> contains the noise factor between the source and the node
	 * (index of vector).
	 *******************************************************************/
	static int extraiMinimo_fnb(int size_par, Vector<Integer> S_par, Vector<Integer> Q_par, double d_par[]) {
		int lessNode_loc = 0;
		int[] posicao_loc = new int[1];
		double lessFvalue_loc = INF;
		boolean flag_loc = false;
		for (int i = 0; i < size_par; i++) {
			if (lessFvalue_loc > d_par[i] && pertence(Q_par, i, posicao_loc)) {
				lessNode_loc = i;
				lessFvalue_loc = d_par[i];
				flag_loc = true;
			}
		}
		if (flag_loc) {
			S_par.add(lessNode_loc);
			Q_par.remove(posicao_loc[0]);
			return lessNode_loc;
		} else
			Q_par.clear();
		return -1;

	}

	int extraiMaximo_fnb(int size_par, Vector<Integer> S_par, Vector<Integer> Q_par, double d_par[]) {
		int lessNoiseNode_loc = 0;
		int[] posicao_loc = new int[1];
		double mostSNRvalue_loc = 0;
		boolean flag_loc = false;
		for (int i = 0; i < size_par; i++) {
			if (mostSNRvalue_loc < d_par[i] && pertence(Q_par, i, posicao_loc)) {
				lessNoiseNode_loc = i;
				mostSNRvalue_loc = d_par[i];
				flag_loc = true;
			}
		}
		if (flag_loc) {
			S_par.add(lessNoiseNode_loc);
			Q_par.remove(posicao_loc);
			return lessNoiseNode_loc;
		} else
			Q_par.clear();
		return -1;

	}

}
