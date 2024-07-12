package br.cns24.util;

public class RandomUtils {
	private static RandomUtils instance = new RandomUtils();
	
	private RandomUtils(){
	}
	
	/**
	 * Retorna o valor de uma vari�vel aleat�ria de uma distribui��o uniforme no intervalo (min, max). 
	 * @param min Valor m�nimo.
	 * @param max Valor m�ximo.
	 * @return Valor da vari�vel aleat�ria.
	 */
	public int nextInt(int min, int max){
		return (int)(Math.round(Math.random() * (max - min) + min));
	}
	
	/**
	 * Retorna o valor de uma vari�vel aleat�ria de uma distribui��o uniforme no intervalo (0, max). 
	 * @param max Valor m�ximo.
	 * @return Valor da vari�vel aleat�ria.
	 */
	public int nextInt(int max){
		return nextInt(0, max);
	}

	public static RandomUtils getInstance() {
		return instance;
	}
	
}
