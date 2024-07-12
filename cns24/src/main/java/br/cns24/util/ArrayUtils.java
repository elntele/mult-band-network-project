package br.cns24.util;

/**
 * Classe utilit�ria para tratar opera��es com vetores e matrizes.
 * 
 * @author Danilo Ara�jo
 * 
 * @param <T>
 *            Tipo dos valores da matriz ou do vetor
 * @since 20/09/2012
 */
public class ArrayUtils<T> {
	private static final ArrayUtils<Integer> integerInstance = new ArrayUtils<Integer>();

	/**
	 * Exibe os valores da matriz em uma estrutura tabular.
	 * 
	 * @param a
	 *            Matriz de valores do tipo t
	 * @return Representa��o textual da matriz
	 */
	public String print(T[][] a) {
		StringBuffer retorno = new StringBuffer();

		for (T[] vt : a) {
			for (T t : vt) {
				retorno.append(t.toString() + " ");
			}
			retorno.append("\n");
		}

		return retorno.toString();
	}
	
	public int[][] copy(int[][] original){
		int[][] clone = new int[original.length][original[0].length];
		
		for (int i = 0; i < original.length; i++) {
			for (int j = 0; j < original.length; j++) {
				clone[i][j] = original[i][j];
			}
		}
		
		return clone;
	}

	/**
	 * Converte uma matriz de int em uma matriz de Integer
	 * 
	 * @param matrix
	 *            Matriz original
	 * @return Matriz de Integer
	 */
	public Integer[][] box(int[][] matrix) {
		Integer[][] ret = new Integer[matrix.length][matrix[0].length];

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				ret[i][j] = matrix[i][j];
			}
		}

		return ret;
	}

	/**
	 * M�todo acessor para obter o valor do atributo integerinstance.
	 * 
	 * @return O atributo integerinstance
	 */
	public static ArrayUtils<Integer> getIntegerinstance() {
		return integerInstance;
	}
}
