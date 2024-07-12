/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: BackboneExperiment.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	11/10/2013		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.experiments;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import br.cns24.TMetric;
import br.cns24.experiments.nodePositions.CircularNetwork;
import br.cns24.models.BarabasiDensity;
import br.cns24.models.ErdosRenyiM;
import br.cns24.models.Finland;
import br.cns24.models.GenerativeProcedure;
import br.cns24.models.NsfNet;
import br.cns24.models.PacificBell;
import br.cns24.models.TModel;
import br.cns24.models.WattsStrogatzDensity;

/**
 * 
 * @author Danilo
 * @since 11/10/2013
 */
public class BackboneExperiment {
	private static final NumberFormat nf = NumberFormat.getInstance(new Locale("en-EL"));

	public static void main(String[] args) {
		nf.setMinimumFractionDigits(4);
		List<TMetric> metrics = TMetric.getDefaults();
		ComplexNetwork cn = null;
		List<ComplexNetwork> cns = null;
		int numRedes = 100;

		int numNodes = 12;
		GenerativeProcedure gp = new Finland();
		cn = buildResults(metrics, gp, numNodes);
		double density = cn.getMetricValues().get(TMetric.DENSITY).doubleValue();

		generateER(metrics, cn, numRedes, numNodes, density);
		generateWS(metrics, cn, numRedes, numNodes, density);
		generateBA(metrics, cn, numRedes, numNodes, density);

		numNodes = 14;
		gp = new NsfNet();
		cn = buildResults(metrics, gp, numNodes);
		density = cn.getMetricValues().get(TMetric.DENSITY).doubleValue();

		generateER(metrics, cn, numRedes, numNodes, density);
		generateWS(metrics, cn, numRedes, numNodes, density);
		generateBA(metrics, cn, numRedes, numNodes, density);

		numNodes = 15;
		gp = new PacificBell();
		cn = buildResults(metrics, gp, numNodes);
		density = cn.getMetricValues().get(TMetric.DENSITY).doubleValue();

		generateER(metrics, cn, numRedes, numNodes, density);
		generateWS(metrics, cn, numRedes, numNodes, density);
		generateBA(metrics, cn, numRedes, numNodes, density);
	}

	/**
	 * @param metrics
	 * @param cn
	 * @param numRedes
	 * @param numNodes
	 * @param density
	 */
	private static void generateER(List<TMetric> metrics, ComplexNetwork cn, int numRedes, int numNodes, double density) {
		List<ComplexNetwork> cns;
		GenerativeProcedure gp;
		cns = new Vector<>();
		gp = new ErdosRenyiM(density, numNodes);
		for (int i = 0; i < numRedes; i++){
			cns.add(new ComplexNetwork(0, gp.transform(new Integer[numNodes][numNodes]), CircularNetwork
				.getInstance().createNodePositions(numNodes), TModel.ERDOS_RENYI_N_M, metrics));
		}
		showResults(metrics, cn, cns);
	}

	/**
	 * @param metrics
	 * @param cn
	 * @param numRedes
	 * @param numNodes
	 * @param density
	 */
	private static void generateBA(List<TMetric> metrics, ComplexNetwork cn, int numRedes, int numNodes, double density) {
		List<ComplexNetwork> cns;
		GenerativeProcedure gp;
		double td;
		cns = new Vector<>();
		double e = 0.60;
		td = Double.MAX_VALUE;
		gp = new BarabasiDensity(density, e);
		while (cn.getTopologicalDistance(new ComplexNetwork(0, gp.transform(new Integer[numNodes][numNodes]), CircularNetwork
				.getInstance().createNodePositions(numNodes), TModel.BARABASI, metrics)) < td){
			td = cn.getTopologicalDistance(new ComplexNetwork(0, gp.transform(new Integer[numNodes][numNodes]), CircularNetwork
					.getInstance().createNodePositions(numNodes), TModel.BARABASI, metrics));
			e += .05;
			((BarabasiDensity)gp).setExponent(e);
		}
		System.out.printf("\nParâmetro BA e = %.4f\n", e);
		
		for (int i = 0; i < numRedes; i++){
			cns.add(new ComplexNetwork(0, gp.transform(new Integer[numNodes][numNodes]), CircularNetwork
				.getInstance().createNodePositions(numNodes), TModel.BARABASI, metrics));
		}
		showResults(metrics, cn, cns);
	}

	/**
	 * @param metrics
	 * @param cn
	 * @param numRedes
	 * @param numNodes
	 * @param density
	 */
	private static void generateWS(List<TMetric> metrics, ComplexNetwork cn, int numRedes, int numNodes, double density) {
		List<ComplexNetwork> cns;
		GenerativeProcedure gp;
		cns = new Vector<>();
		double rp = 0.02;
		double td = Double.MAX_VALUE;
		gp = new WattsStrogatzDensity(rp, getK(density, numNodes)-1, density, true);
		while (cn.getTopologicalDistance(new ComplexNetwork(0, gp.transform(new Integer[numNodes][numNodes]), CircularNetwork
				.getInstance().createNodePositions(numNodes), TModel.WATTS_STROGATZ, metrics)) < td){
			td = cn.getTopologicalDistance(new ComplexNetwork(0, gp.transform(new Integer[numNodes][numNodes]), CircularNetwork
					.getInstance().createNodePositions(numNodes), TModel.WATTS_STROGATZ, metrics));
			rp += .005;
			((WattsStrogatzDensity)gp).setP(rp);
		}
		System.out.printf("\nParâmetro RP = %.4f\n", rp);
		for (int i = 0; i < numRedes; i++){
			cns.add(new ComplexNetwork(0, gp.transform(new Integer[numNodes][numNodes]), CircularNetwork
				.getInstance().createNodePositions(numNodes), TModel.WATTS_STROGATZ, metrics));
		}
		showResults(metrics, cn, cns);
	}

	/**
	 * @param metrics
	 * @param cn
	 * @param cns
	 */
	private static void showResults(List<TMetric> metrics, ComplexNetwork cn, List<ComplexNetwork> cns) {
		buildResults(metrics, cns);
		
		System.out.printf("Topological distance = %.4f \n\n", cn.getTopologicalDistance(cns.get(0)));
	}

	private static int getK(double density, int n) {
		int k = Math.max((int) Math.floor((density * (n - 1)) / 2.0) + 1, 1);
		return k;
	}

	/**
	 * @param metrics
	 * @param gp
	 */
	private static ComplexNetwork buildResults(List<TMetric> metrics, GenerativeProcedure gp, int numNodes) {
		ComplexNetwork cn = new ComplexNetwork(0, gp.transform(new Integer[numNodes][numNodes]), CircularNetwork
				.getInstance().createNodePositions(numNodes), TModel.CUSTOM, metrics);
		System.out.println("Sum�rio da rede " + gp.name() + ":");
		for (TMetric metric : metrics) {
			System.out.printf("%s %s\n", metric.toString(), nf.format(cn.getMetricValues().get(metric).doubleValue()));
		}
		System.out.println("Distribui��o de grau dos nós:");
		cn.calculateDegreeSequenceDistribution();
		for (int i = 0; i < cn.getSequenceDegreeDistribution().length; i++) {
			System.out.print(nf.format(cn.getSequenceDegreeDistribution()[i]) + " ");
		}
		System.out.println("\n");

		return cn;
	}

	/**
	 * @param metrics
	 * @param gp
	 */
	private static void buildResults(List<TMetric> metrics, List<ComplexNetwork> cns) {
		double[] metricValues = new double[metrics.size()];
		double[] dds = new double[cns.get(0).getAdjacencyMatrix().length];
		for (ComplexNetwork cn : cns) {
			cn.calculateDegreeSequenceDistribution();
			for (int i = 0; i < metrics.size(); i++) {
				metricValues[i] += cn.getMetricValues().get(metrics.get(i));
			}
			for (int i = 0; i < cn.getSequenceDegreeDistribution().length; i++) {
				dds[i] += cn.getSequenceDegreeDistribution()[i];
			}
		}

		System.out.println("Sum�rio da rede " + cns.get(0).getModel() + ":");
		int i = 0;
		for (TMetric metric : metrics) {
			System.out.printf("%s %s\n", metric.toString(), nf.format(metricValues[i] / cns.size()));
			i++;
		}
		System.out.println("Distribui��o de grau dos nós:");
		for (double p : dds) {
			System.out.print(nf.format(p/ cns.size()) + " ");
		}
		System.out.println("\n");
	}
}
