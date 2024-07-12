package br.cns24.util;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class DftUtils {
	public static double[] getReal(double[] data) {
		double[] real = new double[data.length];
		double[] fftValues = null;

		DoubleFFT_1D fft = new DoubleFFT_1D(data.length);
		fftValues = new double[2 * data.length];
		for (int i = 0; i < data.length; i++) {
			fftValues[i] = data[i];
		}
		fft.realForwardFull(fftValues);

		for (int i = 0; i < 2 * data.length; i += 2) {
			real[i / 2] = fftValues[i];
		}

		return real;
	}

	public static double[] getIm(double[] data) {
		double[] im = new double[data.length];
		double[] fftValues = null;

		DoubleFFT_1D fft = new DoubleFFT_1D(data.length);
		fftValues = new double[2 * data.length];
		for (int i = 0; i < data.length; i++) {
			fftValues[i] = data[i];
		}
		fft.realForwardFull(fftValues);

		for (int i = 0; i < 2 * data.length; i += 2) {
			im[i / 2] = fftValues[i + 1];
		}

		return im;
	}

	public static double[] getMagnitude(double[] data) {
		double[] magnitude = new double[data.length];
		double[] fftValues = null;

		DoubleFFT_1D fft = new DoubleFFT_1D(data.length);
		fftValues = new double[2 * data.length];
		for (int i = 0; i < data.length; i++) {
			fftValues[i] = data[i];
		}
		fft.realForwardFull(fftValues);

		for (int i = 0; i < 2 * data.length; i += 2) {
			magnitude[i / 2] = Math.sqrt(fftValues[i] * fftValues[i]
					+ fftValues[i + 1] * fftValues[i + 1]);
		}

		return magnitude;
	}

	public static double[] getPhase(double[] data) {
		double[] phase = new double[data.length];
		double[] fftValues = null;

		DoubleFFT_1D fft = new DoubleFFT_1D(data.length);
		fftValues = new double[2 * data.length];
		for (int i = 0; i < data.length; i++) {
			fftValues[i] = data[i];
		}
		fft.realForwardFull(fftValues);

		for (int i = 0; i < 2 * data.length; i += 2) {
			if (fftValues[i] == 0) {
				if (fftValues[i + 1] > 0) {
					phase[i / 2] = Math.PI / 2;
				} else {
					phase[i / 2] = -Math.PI / 2;
				}
			} else if (fftValues[i + 1] == 0) {
				if (fftValues[i] > 0) {
					phase[i / 2] = 0;
				} else {
					phase[i / 2] = -Math.PI;
				}
			} else {
				phase[i / 2] = Math.atan(fftValues[i + 1] / fftValues[i]);
			}
		}

		return phase;
	}

	public static void main(String[] args) {
		double[] data = new double[] { 2, 3, -1, 1 };
		double[] real = getReal(data);
		double[] im = getIm(data);
		double[] magnitude = getMagnitude(data);
		double[] phase = getPhase(data);
		System.out.println("Pontos da DFT:");
		for (int i = 0; i < real.length; i++) {
			System.out.printf("%.2f %s%.2fj; ", real[i], im[i] < 0 ? "" : "+",
					im[i]);
		}
		System.out.println();
		System.out.println("Magnitude da DFT:");
		for (int i = 0; i < real.length; i++) {
			System.out.printf("%.2f ", magnitude[i]);
		}
		System.out.println();
		System.out.println("Fase da DFT:");
		for (int i = 0; i < real.length; i++) {
			System.out.printf("%.2f ", phase[i]);
		}
	}
}
