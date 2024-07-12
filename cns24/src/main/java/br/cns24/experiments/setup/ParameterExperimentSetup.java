package br.cns24.experiments.setup;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import br.cns24.experiments.TParameterModel;

@XmlRootElement
public class ParameterExperimentSetup {
	private TParameterModel type;

	private double firstValue;

	private double lastValue;

	private double step;
	
	private boolean variar;

	public ParameterExperimentSetup() {
		super();
	}

	public ParameterExperimentSetup(TParameterModel type, double value) {
		super();
		this.type = type;
		this.firstValue = value;
		this.lastValue = value;
		this.step = 0;
		this.variar = false;
	}

	public ParameterExperimentSetup(TParameterModel type, double ini, double fim, double step) {
		super();
		this.type = type;
		this.firstValue = ini;
		this.lastValue = fim;
		this.step = step;
		this.variar = true;
	}

	public TParameterModel getType() {
		return type;
	}

	@XmlElement
	public void setType(TParameterModel type) {
		this.type = type;
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

	public boolean isVariar() {
		return variar;
	}

	public void setVariar(boolean variar) {
		this.variar = variar;
	}
}
