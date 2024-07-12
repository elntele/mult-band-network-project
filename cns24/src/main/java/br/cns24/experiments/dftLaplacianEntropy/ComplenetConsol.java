package br.cns24.experiments.dftLaplacianEntropy;

import static br.cns24.experiments.dftLaplacianEntropy.Complenet2013Experiment.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;

public class ComplenetConsol {
	public static void main(String[] args) {
		int numNodes = 400;
		double independentRuns = 100;
		nf.setMinimumFractionDigits(4);
		nf.setMaximumFractionDigits(4);
		try {
//			consolErdos(numNodes, independentRuns);
//			consolBarabasi(numNodes, independentRuns);
			consolWatts(numNodes, independentRuns);
//			consolNewmanWatts(numNodes, independentRuns);
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
		for (double p = 0.01; p <= 1; p += 0.01) {
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
		for (double p = 0.01; p <= 1; p += 0.01) {
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
		for (double p = 0.01; p <= 0.49; p += 0.01) {
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
		for (double p = 0.01; p <= 1; p += 0.01) {
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
		double acm;
		double m1;
		double m2;
		fr = new FileReader(file);

		buffer = new char[(int) file.length()];

		fr.read(buffer);

		content = new String(buffer);

		linhas = content.split("\n");

		dm = 0;
		acm = 0;
		m1 = 0;
		m2 = 0;

		for (int id = 1; id <= independentRuns; id++) {
			linha = linhas[id].split("\t");
			dm += nf.parse(linha[2]).doubleValue();
			acm += nf.parse(linha[3]).doubleValue();
			m1 += nf.parse(linha[4]).doubleValue();
			m2 += nf.parse(linha[5]).doubleValue();
		}
		sb.append(nf.format(p)).append(TAB_SEP);
		sb.append(nf.format(dm / independentRuns)).append(TAB_SEP);
		sb.append(nf.format(acm / independentRuns)).append(TAB_SEP);
		sb.append(nf.format(m1 / independentRuns)).append(TAB_SEP);
		sb.append(nf.format(m2 / independentRuns)).append(TAB_SEP);
		sb.append(NEW_LINE);

		fr.close();
	}
}
