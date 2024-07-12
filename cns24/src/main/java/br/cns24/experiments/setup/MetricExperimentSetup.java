package br.cns24.experiments.setup;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import br.cns24.TMetric;

/**
 * Representa a configuração de uma métrica para ser usada em um experimento.
 * 
 * @author Danilo
 * 
 */
@XmlRootElement
public class MetricExperimentSetup {
	private TMetric metric;

	private double firstValue;

	private double lastValue;

	private double step;

	public TMetric getMetric() {
		return metric;
	}

	public MetricExperimentSetup(TMetric metric) {
		super();
		this.metric = metric;
	}

	public MetricExperimentSetup() {
		super();
	}

	@XmlElement
	public void setMetric(TMetric metric) {
		this.metric = metric;
	}

	public double getFirstValue() {
		return firstValue;
	}

	@XmlElement
	public void setFirstValue(double firstValue) {
		this.firstValue = firstValue;
	}

	public double getLastValue() {
		return lastValue;
	}

	@XmlElement
	public void setLastValue(double lastValue) {
		this.lastValue = lastValue;
	}

	public double getStep() {
		return step;
	}

	@XmlElement
	public void setStep(double step) {
		this.step = step;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((metric == null) ? 0 : metric.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MetricExperimentSetup other = (MetricExperimentSetup) obj;
		if (metric != other.metric)
			return false;
		return true;
	}

}
