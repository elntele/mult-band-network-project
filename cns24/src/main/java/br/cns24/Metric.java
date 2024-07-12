package br.cns24;

import br.cns24.experiments.ComplexNetwork;

public interface Metric<T> {
	public abstract double calculate(T[][] matrix);
	
	public abstract double calculate(ComplexNetwork network);
	
	public abstract String name();
}
