/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: NormalizedExperiment.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	14/04/2014		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.experiments.dftLaplacianEntropy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * 
 * @author Danilo
 * @since 14/04/2014
 */
public class NormalizedExperiment {
	/**
	 * 
	 */
	private static final double START_DENSITY = 0.02;
	private static final double DENSITY_STEP = 0.02;
	public static final String HEADER_TABLE = "ID\tp\tDensity\tAC\tEf0\tOnmax";
	public static final String START_FILE_NAME_ERDOS = "dftLaplacianEntropy/complenet-erdos-";
	public static final String START_FILE_NAME_BARBASI = "dftLaplacianEntropy/complenet-barabasi-";
	public static final String START_FILE_NAME_WATTS = "dftLaplacianEntropy/complenet-watts-strogatz-";
	public static final String START_FILE_NAME_NWATTS = "dftLaplacianEntropy/complenet-newman-watts-";
	public static final String END_FILENAME = ".txt";
	public static final String NEW_LINE = "\n";
	public static final String TAB_SEP = "\t";
	public static final NumberFormat nf = NumberFormat.getInstance();

	public static void main(String[] args) {
		int numNodes = 100;
		double independentRuns = 30;
		nf.setMinimumFractionDigits(4);
		nf.setMaximumFractionDigits(4);
		try {
			consolErdos(numNodes, independentRuns);
			consolBarabasi(numNodes, independentRuns);
			consolWatts(numNodes, independentRuns);
			// consolNewmanWatts(numNodes, independentRuns);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void consolErdos(int numNodes, double independentRuns) throws Exception {
		File file = null;
		FileWriter fw = null;
		StringBuilder sb = new StringBuilder();

		fw = new FileWriter(new File(START_FILE_NAME_ERDOS + "consol" + "-" + numNodes + END_FILENAME));
		sb.append("p\tdm\tacm\tm1\tm2").append(NEW_LINE);
		for (double p = START_DENSITY; p <= 1; p += DENSITY_STEP) {
			file = new File(START_FILE_NAME_ERDOS + numNodes + "-" + nf.format(p) + END_FILENAME);
			processFile(file, sb, independentRuns, p);
		}
		fw.write(sb.toString());
		fw.close();
	}

	private static void consolBarabasi(int numNodes, double independentRuns) throws Exception {
		File file = null;
		FileWriter fw = null;
		StringBuilder sb = new StringBuilder();
		fw = new FileWriter(new File(START_FILE_NAME_BARBASI + "consol" + "-" + numNodes + END_FILENAME));
		sb.append("p\tdm\tacm\tm1\tm2").append(NEW_LINE);
		for (double p = START_DENSITY; p <= 1; p += DENSITY_STEP) {
			file = new File(START_FILE_NAME_BARBASI + numNodes + "-" + nf.format(p) + END_FILENAME);
			processFile(file, sb, independentRuns, p);
		}
		fw.write(sb.toString());
		fw.close();
	}

	private static void consolWatts(int numNodes, double independentRuns) throws Exception {
		File file = null;
		FileWriter fw = null;
		StringBuilder sb = new StringBuilder();
		fw = new FileWriter(new File(START_FILE_NAME_WATTS + "consol" + "-" + numNodes + END_FILENAME));
		sb.append("p\tdm\tacm\tm1\tm2").append(NEW_LINE);
		for (double p = START_DENSITY; p <= 1.00; p += DENSITY_STEP) {
			file = new File(START_FILE_NAME_WATTS + numNodes + "-" + nf.format(p) + END_FILENAME);
			processFile(file, sb, independentRuns, p);
		}
		fw.write(sb.toString());
		fw.close();
	}

	private static void consolNewmanWatts(int numNodes, double independentRuns) throws Exception {
		File file = null;
		FileWriter fw = null;
		StringBuilder sb = new StringBuilder();
		fw = new FileWriter(new File(START_FILE_NAME_NWATTS + "consol" + "-" + numNodes + END_FILENAME));
		sb.append("p\tdm\tacm\tm1\tm2").append(NEW_LINE);
		for (double p = START_DENSITY; p <= 1; p += DENSITY_STEP) {
			file = new File(START_FILE_NAME_NWATTS + numNodes + "-" + nf.format(p) + END_FILENAME);
			processFile(file, sb, independentRuns, p);
		}
		fw.write(sb.toString());
		fw.close();
	}

	private static void processFile(File file, StringBuilder sb, double independentRuns, double p)
			throws FileNotFoundException, IOException, ParseException {
		String content;
		String[] linhas;
		String[] linha;
		char[] buffer;
		FileReader fr;
		double dm;
		double dft;
		fr = new FileReader(file);

		buffer = new char[(int) file.length()];

		fr.read(buffer);

		content = new String(buffer);

		linhas = content.split("\n");

		dm = 0;
		dft = 0;

		for (int id = 1; id <= independentRuns; id++) {
			linha = linhas[id].split("\t");
			dm += nf.parse(linha[2]).doubleValue();
			dft += nf.parse(linha[3]).doubleValue();
		}
		sb.append(nf.format(p)).append(TAB_SEP);
		sb.append(nf.format(dm / independentRuns)).append(TAB_SEP);
		sb.append(nf.format(dft / independentRuns)).append(TAB_SEP);
		sb.append(NEW_LINE);

		fr.close();
	}
}
