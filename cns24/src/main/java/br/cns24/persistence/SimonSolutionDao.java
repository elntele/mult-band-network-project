package br.cns24.persistence;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Vector;

public class SimonSolutionDao {
	private static final int NUM_NODES = 14;

	private static final SimonSolutionDao instance = new SimonSolutionDao();

	private SimonSolutionDao() {

	}

	public List<int[][]> readNetworksFromFile(String file) throws FileNotFoundException, IOException {
		List<int[][]> list = new Vector<>();
		int[][] matrix = null;
		String[] values = null;
		int value;
		FileReader reader = new FileReader(file);
		LineNumberReader lnr = new LineNumberReader(reader);
		String content = lnr.readLine();
		while (content != null) {
			values = content.split("	");
			matrix = new int[NUM_NODES][NUM_NODES];
			for (int i = 0; i < matrix.length; i++) {
				for (int j = i + 1; j < matrix.length; j++) {
					value = Integer.valueOf(values[indiceVetor(j, i)]);
					if (value != 0) {
						matrix[i][j] = 1;
						matrix[j][i] = 1;
					} else {
						matrix[i][j] = 0;
						matrix[j][i] = 0;
					}
				}
			}
			list.add(matrix);
			content = lnr.readLine();
		}
		return list;
	}
	
	public List<Integer[][]> readNetworksFromFileObject(String file) throws FileNotFoundException, IOException {
		List<Integer[][]> list = new Vector<>();
		Integer[][] matrix = null;
		String[] values = null;
		int value;
		FileReader reader = new FileReader(file);
		LineNumberReader lnr = new LineNumberReader(reader);
		String content = lnr.readLine();
		while (content != null) {
			values = content.split("	");
			matrix = new Integer[NUM_NODES][NUM_NODES];
			for (int i = 0; i < matrix.length; i++) {
				for (int j = i + 1; j < matrix.length; j++) {
					value = Integer.valueOf(values[indiceVetor(j, i)]);
					if (value != 0) {
						matrix[i][j] = 1;
						matrix[j][i] = 1;
					} else {
						matrix[i][j] = 0;
						matrix[j][i] = 0;
					}
				}
			}
			list.add(matrix);
			content = lnr.readLine();
		}
		return list;
	}

	public List<double[]> readObjectivesFromFile(String file) throws FileNotFoundException, IOException, ParseException {
		NumberFormat nf = NumberFormat.getInstance();
		List<double[]> list = new Vector<>();
		String[] values = null;
		FileReader reader = new FileReader(file);
		LineNumberReader lnr = new LineNumberReader(reader);
		String content = lnr.readLine();
		while (content != null) {
			values = content.split(" ");
			list.add(new double[] { nf.parse(values[0]).doubleValue(), nf.parse(values[1]).doubleValue() });
			content = lnr.readLine();
		}
		lnr.close();
		reader.close();
		return list;
	}

	private int indiceVetor(int j, int i) {
		return (j + (NUM_NODES - 1) * i - i * (i + 1) / 2);
	}

	public static SimonSolutionDao getInstance() {
		return instance;
	}
}
