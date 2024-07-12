package br.cns24;

public interface Transformation<T> {
	public abstract T[][] transform(T[][] matrix);
}
