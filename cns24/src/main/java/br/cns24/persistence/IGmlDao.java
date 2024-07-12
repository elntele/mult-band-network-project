package br.cns24.persistence;

import br.cns24.experiments.ComplexNetwork;

public interface IGmlDao {
	public void save(ComplexNetwork network, String path);
	
	public ComplexNetwork load(String path);
}
