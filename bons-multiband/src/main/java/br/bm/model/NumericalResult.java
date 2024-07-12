package br.bm.model;

public class NumericalResult implements IEvaluationResult<TNetworkIndicator, Double>{
	private TNetworkIndicator indicator;
	
	private Double value;
	
	public NumericalResult(TNetworkIndicator indicator, Double value) {
		super();
		this.indicator = indicator;
		this.value = value;
	}

	@Override
	public TNetworkIndicator getType() {
		return indicator;
	}

	@Override
	public Double getValue() {
		return value;
	}

}
