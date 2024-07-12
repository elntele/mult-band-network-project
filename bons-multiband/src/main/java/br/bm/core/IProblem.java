package br.bm.core;

public interface IProblem<V, O> {
	public int getNumberOfObjectives();
	
	public int getNumberOfVariables();
	
	public O getLowerLimitObjectiveAt(int index);
	
	public V getLowerLimitVariableAt(int index);
	
	public O getUpperLimitObjectiveAt(int index);
	
	public V getUpperLimitVariableAt(int index);
	
	public O[] evaluate(V[] variables);
}
