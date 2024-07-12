package br.bm.core;

import static java.lang.Math.*;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.random;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

/**
 * Repositorio de formulas - FWM - classe utilitaria. Codigo original obtido a
 * partir de formulasFWM.h, por Daniel Augusto Ribeiro Chaves e Julio Dantas de
 * Andrade.
 * 
 * @author Danilo Araiujo
 */
public class SimonUtil {
	public static final double LAMBDA0 = 1450e-9;

	public static final double D0 = 0; // 0.939 //0 // ps/km.nm
	public static final double S0 = 45; // 72.9 // s/m^3
	public static final double N_2 = 2.3e-20; // m^2/W
	public static final double AEFF = 52e-12; // m^2
	public static final double C = 2.99792458e8; // velocidade da luz

	// CONSTANTES PARA CALCULO DO FATOR DE RUIDO
	public static final int R_ = 1;
	public static final double B0 = 100e9;

	public static final double PLANCK = 6.626068e-34;
	public static final double EPSILON = 0.001;
	public static final int LAMBDA_OTIMIZACAO = 1;
	public static final int LAMBDA_FIRSTFIT = 2;
	public static final double RETURN_G0 = 1e-11;

	public static final int UTILIZAR_DIJ = 50;
	public static final int UTILIZAR_HOP = 51;
	public static final int UTILIZAR_NF = 52;
	public static final int UTILIZAR_ANT = 53;
	public static final int UTILIZAR_REG = 54;

	public static final double lambdaZero = 1528.77e-9;/* 1538.19e-9; *//*
																		 * 1544.53e-9;
																		 */// 1550.0e-9;
	public static final double INF = 1e50;
	// /////// FIBRA TX //////////////////////////////
	public static final double SSMF = S0; // em (s/m^3)

	public static final double DSMF = ((1.0e-6) * D0 + S0 * (lambdaZero - LAMBDA0));
	public static final double LAMBDA_REF_DCF = 1550e-9;

	public static final double SDCF = -1.87e3; // em (s/m^3)

	public static final double DDCF_REF = -110.0e-6; // em (s/m^2)

	/**
	 * Funcao Eficiencia = a*(1+b/c)
	 * 
	 * onde a=(aten^2)/((aten^2)+ (Casamento^2))
	 * b=4*exp(-aten*L)*(sin(casamento*L/2))^2 c=(1-exp(-aten*L))^2
	 * 
	 * 
	 * "(aten^2) (4.exp(-aten.L).sin^2(Casamento*L/2) ) formula"
	 * ----------------------.( 1 + ----------------------------------- )
	 * (aten^2) + (Casamento^2) ( (1-exp(-aten.L))^2 )
	 **/
	public static double eficiencia(double length, double expAlfaL, double casamentoVar, double aten) {
		double parte1, parte2;
		double sen2;

		sen2 = casamentoVar * length / 2;
		sen2 = sin(sen2);
		sen2 *= sen2;

		parte1 = (aten * aten) / (aten * aten + casamentoVar * casamentoVar);
		parte2 = (4 * expAlfaL * sen2) / ((1 - expAlfaL) * (1 - expAlfaL));

		return (parte1 * (1 + parte2));
	}

	/**
	 * Retorna Casamento = a*(Dc+b)
	 * 
	 * @param fi
	 * @param fj
	 * @param fk
	 * @param Pi
	 * @param Pj
	 * @param Pk
	 * @param gamaVar
	 * @param lambda
	 * @return Valor de a*(Dc+b)
	 */
	public static double casamento(double fi, double fj, double fk, double Pi, double Pj, double Pk, double gamaVar,
			double lambda) {
		double parte1, parte2, Dc;
		double lambdaK, frequenciaIK, frequenciaJK;
		double m = 0.0;

		lambdaK = C / fk;
		Dc = D0 + S0 * (lambda - LAMBDA0);
		frequenciaIK = Math.abs(fk - fi);
		frequenciaJK = Math.abs(fj - fk);

		parte1 = ((2 * PI * lambdaK * lambdaK) / C) * frequenciaIK * frequenciaJK;
		parte2 = Dc + ((lambdaK * lambdaK) / (2 * C) * (frequenciaIK + frequenciaJK) * S0);

		return (parte1 * parte2 - m * gamaVar * (Pi + Pj - Pk));
	}

	/**
	 * Calcula Gama=(2*PI*N2)/Lambda*Aeff
	 * 
	 * @param lambda
	 * @return (2*PI*N2)/lambda*Aeff
	 */
	public static double gama(double lambda) {
		return ((2 * PI * N_2) / (lambda * AEFF));
	}

	/**
	 * calculo de Pijk Calculado segundo o paper.
	 * 
	 * n ( (1-exp(-aten.L))^2 ) Formula:
	 * ---.(D^2).(gama^2).Pi.Pj.Pk.exp(-aten*L).(-------------------) 9 ( aten^2 )
	 * 
	 * alt+253= (^2)
	 * 
	 * @param length
	 * @param alfa
	 * @param fi
	 * @param fj
	 * @param fk
	 * @param Pi
	 * @param Pj
	 * @param Pk
	 * @return Pijk
	 */
	public static double getFWMPower(double length, double alfa, double fi, double fj, double fk, double Pi, double Pj,
			double Pk) {
		double parte1, parte2;// , length;
		double expAlfaL, casamento_var, lambda, gama_var, eficiencia_var;
		double D = 3.0;
		// converte o coeficiente de atenuacao de dB/km para m^-1
		double aten = (alfa / 4.343) * 1e-3;
		expAlfaL = exp(-(aten * length));
		lambda = C / (fi + fj - fk);

		gama_var = gama(lambda);
		casamento_var = casamento(fi, fj, fk, Pi, Pj, Pk, gama_var, lambda);
		eficiencia_var = eficiencia(length, expAlfaL, casamento_var, aten);

		parte1 = D * D * gama_var * gama_var * Pi * Pj * Pk * expAlfaL;
		parte2 = ((1 - expAlfaL) * (1 - expAlfaL)) / (aten * aten);

		return (eficiencia_var / 9) * parte1 * parte2;
	}

	/**
	 * CALCULA O FATOR DE RUIDO DO FWM
	 * 
	 * @param length
	 * @param alfa
	 * @param n
	 * @param P
	 * @return
	 */
	public double fatorRuidoFWM(double length, double alfa, int n, double[] P) {
		// indice de P vai de 1 ate n
		int i, j;
		double q = 1.6e-19;
		double Soma_1 = 0.0, Soma_2 = 0.0, resultado = 0.0;
		double aten = (alfa / 4.343) * 1e-3;

		for (i = 0; i < n; i++) {
			Soma_1 = Soma_1 + P[i];
			for (j = i + 1; j <= n; j++)
				Soma_2 = Soma_2 + sqrt(P[i] * P[j]);
		}

		Soma_1 = Soma_1 + P[n];

		resultado = ((Soma_1 + 2 * Soma_2) * (Soma_1 + 2 * Soma_2)) - (P[0] * P[0]);

		return exp(aten * length) * (1 + (R_ / (2 * q * B0 * P[0])) * (resultado));

	}

	/******************************************************************
	 ***** FUNCTION....: exponencial_fnb() DESCRIPTON..: Generates a random number
	 * exponentialy distributed PARAMETERS..: meanRate_par -> mean rate of
	 * distribution or inverse of mean time of distribution probability density
	 * => f(x)=a*exp(-a*x) meanRate_par = a
	 *******************************************************************/
	public static double getRandomExp(double meanRate_par) {
		return (-1 / meanRate_par) * log(random());
	}
	
	public static double getRandomPareto(double a, double xm) {
		return xm/pow(random(), 1/a);	
	}
	
	public static void main(String[] args) {
//		for (int i = 0; i < 10000; i++){
//			System.out.printf("%.4f\n", getRandomPareto(1.0001, 0.01));
//		}
		
		double media = 0;
		double soma = 0;
		double qtde = 0;
		double x = 1;
		double a = 1.644;
		int n = 10000000;
		while (qtde < 10000000 && !(media < 2.5429 && media > 2.542)){
			soma = 0;
			for (int i = 0; i < n; i++) {
				soma += getRandomPareto(a, x);
			}
			a += 0.0001;
			media = soma/n;
			qtde++;
			System.out.printf("media = %.4f; x = %.4f; a = %.4f\n", media, x, a);
		}
		System.out.printf("x = %.4f\n", x);
		System.out.printf("a = %.4f\n", a);
//		for (int i = 0; i < 10000; i++){
//			System.out.printf("%.4f\n", getRandomPareto(2, 0.005));
//		}
//		double media = 0;
//		for (int i = 0; i < 10000; i++){
//			media += getRandomExp(0.4);
//		}
//		media /= 10000;
//		System.out.printf("%.4f\n", media);
	}
}
