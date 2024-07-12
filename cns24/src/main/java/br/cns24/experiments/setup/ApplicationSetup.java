package br.cns24.experiments.setup;

import java.util.List;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import br.cns24.TMetric;
import br.cns24.experiments.TParameterModel;
import br.cns24.models.TModel;

/**
 * Representa a configuração b�sica de um experimento no simulador.
 * 
 * @author Danilo
 * 
 */
@XmlRootElement
public class ApplicationSetup {
	private int defaultNumNodes;

	private TModel defaultModel;

	private List<TModel> algorithms;

	private List<MetricExperimentSetup> metrics = new Vector<>();

	private List<ParameterExperimentSetup> parameters = new Vector<>();
	
	private SomSetup somSetup = new SomSetup();

	public ApplicationSetup() {
		defaultNumNodes = 6;
		defaultModel = TModel.ERDOS_RENYI_N_P;
		ParameterExperimentSetup p = new ParameterExperimentSetup(TParameterModel.PROBABILITY_ERDOS, 0.4, 0.6, 0.001);
		MetricExperimentSetup m1 = new MetricExperimentSetup(TMetric.DENSITY);
		MetricExperimentSetup m2 = new MetricExperimentSetup(TMetric.CONCENTRATION_ROUTES);
		MetricExperimentSetup m3 = new MetricExperimentSetup(TMetric.BETWENNESS_CENTRALIZATION);

		metrics.add(m1);
		metrics.add(m2);
		metrics.add(m3);
		
		parameters.add(p);
	}

	public List<TModel> getAlgorithms() {
		return algorithms;
	}

	@XmlElement
	public void setAlgorithms(List<TModel> algorithms) {
		this.algorithms = algorithms;
	}

	public List<MetricExperimentSetup> getMetrics() {
		return metrics;
	}

	@XmlElement
	public void setMetrics(List<MetricExperimentSetup> metrics) {
		this.metrics = metrics;
	}

	public List<ParameterExperimentSetup> getParameters() {
		return parameters;
	}

	@XmlElement
	public void setParameters(List<ParameterExperimentSetup> parameters) {
		this.parameters = parameters;
	}

	public int getDefaultNumNodes() {
		return defaultNumNodes;
	}

	@XmlElement
	public void setDefaultNumNodes(int defaultNumNodes) {
		this.defaultNumNodes = defaultNumNodes;
	}

	public TModel getDefaultModel() {
		return defaultModel;
	}

	@XmlElement
	public void setDefaultModel(TModel defaultModel) {
		this.defaultModel = defaultModel;
	}

	public SomSetup getSomSetup() {
		return somSetup;
	}

	@XmlElement
	public void setSomSetup(SomSetup somSetup) {
		this.somSetup = somSetup;
	}
}
