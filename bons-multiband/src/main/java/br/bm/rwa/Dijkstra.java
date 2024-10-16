package br.bm.rwa;

import static br.bm.core.SimonUtil.*;
import java.util.Vector;

import br.bm.core.Link;
import br.bm.core.Node;

public class Dijkstra {

	/**
	 * Description by jorge:
	 * This method fulfill a Vector<Link> input variable called caminho_par.
	 * In the end, this vactor will contain a path in direct way to the
	 * origin node until the last node which is received in input parameters.
	 * Parameters explanation:
	 * matAdj_par[numNodes][numNode] is a matrix of Links objects that follows
	 * the same logic as an adjacency matrix. It is populated in the evaluate
	 * method of the OpticalNetworkMultiBandProblem class.
	 * @param matAdj_par
	 * fonte_par is the origin node
	 * @param fonte_par
	 * dest_par is the last node
	 * @param dest_par
	 * caminho_ar is Vector<Link> to fulfill with Links which form the path
	 * @param caminho_par
	 * vectorOfNodes_par is a vector of object nodes, but in this method it is used only to get node size.
	 * @param vectorOfNodes_par
	 * is a boolean which serves to now if any node is jumped.
	 * @param hops
	 */
	public static void dijkstra_fnb(Link[][] matAdj_par, int fonte_par, int dest_par, Vector<Link> caminho_par,
			Vector<Node> vectorOfNodes_par, boolean hops) {
		/** visitedNodes will contain all nodes for which the path with the lowest noise factor has already been found*/
		Vector<Integer> visitedNodes  = new Vector<Integer>();
		/** notVisitedNodes will contain the nodes for which the paths with the lowest noise factor have not yet been found.*/
		Vector<Integer> notVisitedNodes = new Vector<Integer>();
		/** d - vetor de distancias: contera o valor do menor fator de ruido entre a origem e o no (indice do vetor)*/
		double[] d;
		/** indicate the before node of each node*/
		int[] father; 
		int numeroNos = vectorOfNodes_par.size();
		double menorDist, distEntreNos;
		int vertex, noMenorDisTotal, cont;
		Vector<Integer> track = new Vector<Integer>();

		d = new double[numeroNos];
		father = new int[numeroNos];

		for (vertex = 0; vertex < numeroNos; vertex++) {
			d[vertex] = INF;
			father[vertex] = -1;
			notVisitedNodes.add(vertex);
		}

		d[fonte_par] = 0;

		int[] temp = new int[1];
		while (notVisitedNodes.isEmpty() != true) {
			//3 lists: visitedNodes, isitedNodes , notVisitedNodes and d witch is distances list
			//initially fulfil with INFINITY
			noMenorDisTotal = extraiMinimo_fnb(numeroNos, visitedNodes , notVisitedNodes, d);
			if (noMenorDisTotal != -1) {
				for (cont = 0; cont < numeroNos; cont++) {
					if (hops) {
						distEntreNos = matAdj_par[noMenorDisTotal][cont].getLength() > 0
								&& matAdj_par[noMenorDisTotal][cont].getLength() < INF ? 1
								: matAdj_par[noMenorDisTotal][cont].getLength();
					} else {
						distEntreNos = matAdj_par[noMenorDisTotal][cont].getLength();
					}

					if (cont != noMenorDisTotal && !pertence_fnb(visitedNodes , cont, temp) && distEntreNos != INF) {
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
		track.clear();
		findtrack_fnb(father[dest_par], dest_par, father, track, fonte_par);
		if (!track.isEmpty()) {
			caminho_par.clear();
			int j = track.size();
			// in findtrack_fnb() method, the path to origin node until last node is fulfilled in inverse way,
			// and stored in <Integer> track.  This loop iterate over track in the inverse way to add to caminho_par
			// the links contained in adjacency matrix, matAdj_par, which is a matrix of object Links, then caminho_par will
			// contain the links which track the path to the origin node until the last node
			// in direct way.
			for (int i = 1; i < j; i++) { // seta o vetor de resultado
				caminho_par.add(matAdj_par[track.get(j - i)][track.get(j - i - 1)]);
			}

		} else {
			caminho_par.clear();
		}
		d = null;
		father = null;

	}

	public static void dijkstra_fnb(Link[][] matAdj_par, int fonte_par, int dest_par, Vector<Link> caminho_par,
			Vector<Node> vectorOfNodes_par) {
		dijkstra_fnb(matAdj_par, fonte_par, dest_par, caminho_par, vectorOfNodes_par, false);
	}

	/********************************************************************
	 * DETERMINA��O DE ROTAS ---- DIJKSTRA POR MINIMO FATOR DE RU�DO
	 *********************************************************************/

	/******************************************************************
	 * New description by jorge:
	 * this method verify if an integer, witch represents an index node
	 * in the array of network nodes, belongs to a vector. If belongs
	 * the method returns true and add this integer to an array witch
	 * was received as a parameter. Note that we are talk about indexes.
	 *
	 * Old description by danilo:
	 ***** FUNCTION....: pertence_fnb DESCRIPTION.: Verifies if an element is in a
	 * dynamic vector or not and modifies the parameter "posicao_par" to the
	 * element position. PARAMETERS.: a dynamic vector<int>, a key(int) and an
	 * integer variable
	 *******************************************************************/
	static boolean pertence_fnb(Vector<Integer> vector_par, int key_par, int[] posicao_par) {
		for (int i = 0; i < vector_par.size(); i++) {
			if (vector_par.get(i) == key_par) {
				posicao_par[0] = i;
				return (true);
			}
		}
		return (false);
	}

	/******************************************************************
	 * new description by jorge:
	 * this method fulfill an array witch the first position is the last node
	 * before the node observed, and continue to fulfill until arrive in the
	 * source node in dijkstra table.
	 * explaining of parameters:
	 * menorFtotal_par is the next to last node.
	 * 	cont_par is the last node.
	 * 	father_par is array witch the value is the before node of the index,
	 * 	for example: if in network node 6 has the before node as the node 3
	 * 	(in dijkstra table), in the index 6 of father_par array will be the
	 * 	value 3.
	 * 	track_par if vector to fulfill with the path until the source.
	 * 	source_par is the origin node in dijkstra table
	 *
	 * old description by danilo:
	 ***** FUNCTION....: findtrack_fnb DESCRIPTION.: Updates the vector "track"
	 * received as parameter, for containing in the opposite order all the path
	 * until the current node PARAMETERS.: the next to last node(int), last
	 * node(int), vector father(int)=>indicates the previous node for each node
	 * (index of the vector), vector<int> track=> modified for containing the
	 * path, source of path (int).
	 *******************************************************************/
	static void findtrack_fnb(int menorFtotal_par, int cont_par, int father_par[], Vector<Integer> track_par,
			int source_par) {
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
	 * New description by Jorge:
	 * This method finds the nearest neighbor node from the notVisitedNodes list.
	 * If the node is not present in the `notVisitedNodes` list, the method returns -1.
	 * Otherwise, it returns the node, removes it from the `notVisitedNodes` list, and adds it to the `visitedNodes` list.
	 * If the `notVisitedNodes` list is empty, the method also returns -1.
	 * 1 - Nearest to whom? The reference node is selected in the caller method.

	 *

	 Old description by danilo:
	 ***** FUNCTION....: extraiMinimo_fnb DESCRIPTION.: Searchs the less element of
	 * "vector of distances" that was not calculated the less path to it.
	 * PARAMETERS.: network's size(int), vector<int> visitedNodes => contains all the
	 * nodes for which was found the path with less noise factor , vector<int>
	 * notVisitedNodes=> nodes which was not found the less path, vector d or distance
	 * (long double)=> contains the noise factor between the source and the node
	 * (index of vector).
	 *******************************************************************/
	static int extraiMinimo_fnb(int size_par, Vector<Integer> visitedNodes, Vector<Integer> notVisitedNodes, double d_par[]) {
		// extraiMinimo_fnb(numeroNos, visitedNodes , notVisitedNodes, d);
		int lessNode_loc = 0;
		int[] posicao_loc = new int[1];
		double lessFvalue_loc = INF;
		boolean flag_loc = false;
		for (int i = 0; i < size_par; i++) {
			if (lessFvalue_loc > d_par[i] && pertence_fnb(notVisitedNodes, i, posicao_loc)) {
				lessNode_loc = i;
				lessFvalue_loc = d_par[i];
				flag_loc = true;
			}
		}
		if (flag_loc) {
			visitedNodes.add(lessNode_loc);
			notVisitedNodes.remove(posicao_loc[0]);
			return lessNode_loc;
		} else
			notVisitedNodes.clear();
		return -1;

	}

	int extraiMaximo_fnb(int size_par, Vector<Integer> S_par, Vector<Integer> Q_par, double d_par[]) {
		int lessNoiseNode_loc = 0;
		int[] posicao_loc = new int[1];
		double mostSNRvalue_loc = 0;
		boolean flag_loc = false;
		for (int i = 0; i < size_par; i++) {
			if (mostSNRvalue_loc < d_par[i] && pertence_fnb(Q_par, i, posicao_loc)) {
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
