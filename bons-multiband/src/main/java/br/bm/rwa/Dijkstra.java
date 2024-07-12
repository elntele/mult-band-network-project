package br.bm.rwa;

import static br.bm.core.SimonUtil.*;
import java.util.Vector;

import br.bm.core.Link;
import br.bm.core.Node;

public class Dijkstra {
	public static void dijkstra_fnb(Link[][] matAdj_par, int fonte_par, int dest_par, Vector<Link> caminho_par,
			Vector<Node> vectorOfNodes_par, boolean hops) {
		Vector<Integer> S = new Vector<Integer>(); // contera todos os nos para
													// os quais ja se foi
		// encontrado o caminho com menor fator de ruido
		Vector<Integer> Q = new Vector<Integer>(); // contera os nos para os
													// quais ainda nao foram
		// encontrados os caminhos com menor fator de ruido
		double[] d; // vetor de distancias: contera o valor do menor fator de
		// ruido entre a origem e o no (indice do vetor)
		int[] father; // indica o no anterior de cada no
		int numeroNos = vectorOfNodes_par.size();
		double menorDist, distEntreNos;
		int vertex, noMenorDisTotal, cont;
		Vector<Integer> track = new Vector<Integer>();

		d = new double[numeroNos];
		father = new int[numeroNos];

		for (vertex = 0; vertex < numeroNos; vertex++) {
			d[vertex] = INF;
			father[vertex] = -1;
			Q.add(vertex);
		}

		d[fonte_par] = 0;

		int[] temp = new int[1];
		while (Q.isEmpty() != true) {
			noMenorDisTotal = extraiMinimo_fnb(numeroNos, S, Q, d);
			if (noMenorDisTotal != -1) {
				for (cont = 0; cont < numeroNos; cont++) {
					if (hops) {
						distEntreNos = matAdj_par[noMenorDisTotal][cont].getLength() > 0
								&& matAdj_par[noMenorDisTotal][cont].getLength() < INF ? 1
								: matAdj_par[noMenorDisTotal][cont].getLength();
					} else {
						distEntreNos = matAdj_par[noMenorDisTotal][cont].getLength();
					}

					if (cont != noMenorDisTotal && !pertence_fnb(S, cont, temp) && distEntreNos != INF) {
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
			if (lessFvalue_loc > d_par[i] && pertence_fnb(Q_par, i, posicao_loc)) {
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
