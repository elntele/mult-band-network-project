package br.cns24.experiments;

import java.util.List;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import br.cns24.experiments.setup.ApplicationSetup;

@XmlRootElement
public class ResultSet {
	private List<ComplexNetwork> networks = new Vector<>();
	
	private ApplicationSetup setup;
	
	public void evaluateAll(){
		for (ComplexNetwork network : networks){
			network.evaluate();
		}
	}

	public List<ComplexNetwork> getNetworks() {
		return networks;
	}
	
	@XmlElement
	public void setNetworks(List<ComplexNetwork> networks) {
		this.networks = networks;
	}
	
	public ApplicationSetup getSetup() {
		return setup;
	}

	@XmlElement
	public void setSetup(ApplicationSetup setup) {
		this.setup = setup;
	}
	
	
}
