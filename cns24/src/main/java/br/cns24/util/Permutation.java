package br.cns24.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Permutation<T> {
	public static void main(String args[]) {
		Permutation<Integer> obj = new Permutation<Integer>();
		System.out.println(obj.calculateLength(7));
		Collection<Integer> input = new ArrayList<Integer>();
		input.add(1);
		input.add(2);
		input.add(3);

		Collection<List<Integer>> output = obj.permute(input);
		int k = 0;
		Set<List<Integer>> pnr = null;
		for (int i = 0; i <= input.size(); i++) {
			pnr = new HashSet<List<Integer>>();
			for (List<Integer> integers : output) {
				pnr.add(integers.subList(i, integers.size()));
			}
			k = input.size() - i;
			System.out.println("P(" + input.size() + "," + k + ") :" + "Count (" + pnr.size() + ") :- " + pnr);
		}
	}

	/**
	 * Retorna o Número de permuta��es (n!).
	 * 
	 * @param size
	 *            Tamanho do conjunto original
	 * @return Número de permuta��es.
	 */
	public long calculateLength(int size) {
		int r = size;
		for (int n = size - 1; n > 0; n--) {
			r *= n;
		}
		return r;
	}

	/**
	 * Retorna o Número de permuta��es.
	 * 
	 * @param input
	 *            Conjunto de entrada.
	 * @return Cole��o de listas que representa as permuta��es da lista de
	 *         entrada.
	 */
	public Collection<List<T>> permute(Collection<T> input) {
		Collection<List<T>> output = new ArrayList<List<T>>();
		if (input.isEmpty()) {
			output.add(new ArrayList<T>());
			return output;
		}
		List<T> list = new ArrayList<T>(input);
		T head = list.get(0);
		List<T> rest = list.subList(1, list.size());
		for (List<T> permutations : permute(rest)) {
			List<List<T>> subLists = new ArrayList<List<T>>();
			for (int i = 0; i <= permutations.size(); i++) {
				List<T> subList = new ArrayList<T>();
				subList.addAll(permutations);
				subList.add(i, head);
				subLists.add(subList);
			}
			output.addAll(subLists);
		}
		return output;
	}
}
