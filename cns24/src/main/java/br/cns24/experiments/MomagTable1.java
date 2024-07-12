/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: MomagTable1.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	24/03/2014		Vers�o inicial
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
import br.cns24.models.GenerativeProcedure;
import br.cns24.models.KRegular;
import br.cns24.models.TModel;
import br.cns24.models.WattsStrogatzDensity;

/**
 * 
 * @author Danilo
 * @since 24/03/2014
 */
public class MomagTable1 {
	public static void main(String[] args) {
		NumberFormat nf = NumberFormat.getInstance();
		StringBuffer sb1 = new StringBuffer();
		List<TMetric> metrics = TMetric.getDefaults();
		List<ComplexNetwork> cnsER = new Vector<>();
		List<ComplexNetwork> cnsBA = new Vector<>();
		List<ComplexNetwork> cnsWS = new Vector<>();
		List<ComplexNetwork> cnsRegular = new Vector<>();
		int numRedes = 100;
		int numNodes = 50;
		double density = 100.0/((numNodes * (numNodes-1))/2);
		GenerativeProcedure gp = new ErdosRenyiM(density, numNodes);
		int i = 0;
		ComplexNetwork cn = null;
		while (i < numRedes) {
			cn = new ComplexNetwork(0, gp.transform(new Integer[numNodes][numNodes]), CircularNetwork
					.getInstance().createNodePositions(numNodes), TModel.ERDOS_RENYI_N_M, metrics);
			if (cn.getMetricValues().get(TMetric.ALGEBRAIC_CONNECTIVITY).doubleValue() > 0) {
				cnsER.add(cn);
				i++;
			}
		}
		appendMetrics(metrics, cnsER, sb1, nf, 0, "ER");
		double e = 1.00;
		gp = new BarabasiDensity(density, e);
		i = 0;
		while (i < numRedes) {
			cn = new ComplexNetwork(0, gp.transform(new Integer[numNodes][numNodes]), CircularNetwork
					.getInstance().createNodePositions(numNodes), TModel.BARABASI, metrics);
			if (cn.getMetricValues().get(TMetric.ALGEBRAIC_CONNECTIVITY).doubleValue() > 0) {
				cnsBA.add(cn);
				i++;
			}
		}
		appendMetrics(metrics, cnsBA, sb1, nf, e, "BA");
		
		double rp = 0.05;
		gp = new WattsStrogatzDensity(rp, 2, density, true);
		i = 0;
		while (i < numRedes) {
			cn = new ComplexNetwork(0, gp.transform(new Integer[numNodes][numNodes]), CircularNetwork
					.getInstance().createNodePositions(numNodes), TModel.WATTS_STROGATZ, metrics);
			if (cn.getMetricValues().get(TMetric.ALGEBRAIC_CONNECTIVITY).doubleValue() > 0) {
				cnsWS.add(cn);
				i++;
			}
		}
		appendMetrics(metrics, cnsWS, sb1, nf, rp, "WS");

		int k = 2;
		gp = new KRegular(k);
		i = 0;
		while (i < numRedes) {
			cn = new ComplexNetwork(0, gp.transform(new Integer[numNodes][numNodes]), CircularNetwork
					.getInstance().createNodePositions(numNodes), TModel.K_REGULAR, metrics);
			if (cn.getMetricValues().get(TMetric.ALGEBRAIC_CONNECTIVITY).doubleValue() > 0) {
				cnsRegular.add(cn);
				i++;
			}
		}
		appendMetrics(metrics, cnsRegular, sb1, nf, rp, k + "-regular");
		
		System.out.println(metrics.toString());
		System.out.println(sb1.toString());
	}

	private static void appendMetrics(List<TMetric> metrics, List<ComplexNetwork> cns, StringBuffer sb, NumberFormat nf,
			double param, String title) {
		double[] metricValues = new double[metrics.size()];
		for (ComplexNetwork cn : cns) {
			for (int i = 0; i < metrics.size(); i++) {
				metricValues[i] += cn.getMetricValues().get(metrics.get(i));
			}
		}
		sb.append(title + ";");
		for (int i = 0; i < metrics.size(); i++) {
			sb.append(nf.format(metricValues[i] / cns.size()) + ";");
		}
		sb.append("\n");
	}
}
