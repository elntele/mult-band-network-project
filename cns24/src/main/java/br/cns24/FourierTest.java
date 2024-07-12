/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: FourierTest.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	19/07/2015		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import br.cns24.transformations.Laplacian;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

/**
 * 
 * @author Danilo Araujo
 * @since 19/07/2015
 */
public class FourierTest {

	/**
	 * Construtor da classe.
	 */
	public FourierTest() {
	}

	public static void main2(String[] args) {
		double d = (Math.PI)/2;
		System.out.println(Math.cos(d));
		System.out.println(Math.sin(d));
	}
	
	public static void main(String[] args) {
		double fs = 200;
		double ts = 1 / fs;

		double[] x = new double[] { 980, 988, 1160, 1080};//, 928, 1068};//, 1156, 1152, 1176, 1264 };
		// for (int i = 0; i < x.length; i++) {
		// x[i] = Math.cos(2 * Math.PI * 20 * i * ts + Math.PI / 4) + 3
		// * Math.cos(2 * Math.PI * 40 * i * ts - 2 * Math.PI / 5) + 2
		// * Math.cos(2 * Math.PI * 60 * i * ts + Math.PI / 8);
		// System.out.printf("%.2f ", x[i]);
		// }
		// System.out.println();
		DoubleFFT_1D fft = new DoubleFFT_1D(2*x.length);
		double[] fftValues = new double[2*x.length];
		for (int i = 0; i < x.length; i++) {
			fftValues[i] = x[i];
		}
		fft.realForward(fftValues);
		for (int i = 0; i < fftValues.length; i++) {
			System.out.printf("%.2f ", fftValues[i]);
		}
		System.out.println();
		double[][] fft1 = tdf(x);
		for (int i = 0; i < fft1.length; i++) {
			System.out.printf("%.2f %.2f ", fft1[i][0], fft1[i][1]);
		}
		System.out.println();
	}
	
	public static double[][] tdf(double[] x) {
		double[][] tdf = new double[x.length][2];
		for (int k = 0; k < x.length; k++) {
			for (int n = 0; n < x.length; n++) {
				tdf[k][0] += x[n] * Math.cos((Math.PI * k * n)/x.length); 
				tdf[k][1] += -x[n] * Math.sin((Math.PI * k * n)/x.length); 
			}
		}
		return tdf;
	}

	public static void main1(String[] args) {
		double fs = 200;
		double ts = 1 / fs;

		double[] x = new double[100];
		for (int i = 0; i < x.length; i++) {
			x[i] = Math.cos(2 * Math.PI * 20 * i * ts + Math.PI / 4) + 3
					* Math.cos(2 * Math.PI * 40 * i * ts - 2 * Math.PI / 5) + 2
					* Math.cos(2 * Math.PI * 60 * i * ts + Math.PI / 8);
			System.out.printf("%.2f ", x[i]);
		}
		System.out.println();
		DoubleFFT_1D fft = new DoubleFFT_1D(x.length);
		double[] fftValues = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			fftValues[i] = x[i];
		}
		fft.realForward(fftValues);
		for (int i = 2; i < fftValues.length-1; i += 2) {
			System.out.printf("%.2f ", Math.sqrt(Math.abs(fftValues[i] * fftValues[i] + fftValues[i + 1] + fftValues[i + 1])));
		}
		System.out.println();
	}
}
