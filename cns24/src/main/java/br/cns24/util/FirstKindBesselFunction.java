package br.cns24.util;

public class FirstKindBesselFunction extends BesselFunction {
	private static final int MAX_M = 10;

	@Override
	public double getJ(int alpha, double x) {
		double j = 0;

		for (int m = 0; m < MAX_M; m++) {
			j += (Math.pow(-1, m) / (factorial(m) * gamma(m + alpha + 1))) * 
					Math.pow(0.5 * x, 2 * m + alpha);
		}

		return j;
	}

	/**
	 * Retorna o valor da fun��o gamma. A fun��o est� considerando apenas um
	 * fatorial deslocado em 1. Falta defini��o para complexos.
	 * 
	 * @param z
	 *            Argumento da fun��o.
	 * @return Valor da fun��o
	 */
	public double gamma(double z) {
		double g = 0;

		g = factorial((long) (z - 1));

		return g;
	}

	/**
	 * Retorna o fatorial do Número dado.
	 * 
	 * @param z
	 *            Argumento da fun��o.
	 * @return Valor da fun��o
	 */
	public long factorial(long n) {
		if (n < 2) {
			return 1;
		}
		return n * (n - 1);
	}
	
	public static void main(String[] args) {
		FirstKindBesselFunction bessel = new FirstKindBesselFunction();
		for (double x = 0; x < 20; x += 0.5){
			System.out.printf("%.4f; %.4f; %.4f \n", x, bessel.getJ(0, x), bessel.getJ(1, x));
		}
	}
}
