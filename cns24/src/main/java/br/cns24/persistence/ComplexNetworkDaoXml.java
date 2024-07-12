package br.cns24.persistence;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import br.cns24.experiments.ComplexNetwork;

public class ComplexNetworkDaoXml implements IComplexNetworkDao {
	private static final ComplexNetworkDaoXml instance = new ComplexNetworkDaoXml();

	private ComplexNetworkDaoXml() {
	}

	@Override
	public void save(String xml, ComplexNetwork object) throws IOException {
		try {
			JaxbUtils.getInstance().save(xml, object);
		} catch (JAXBException e) {
			throw new IOException(e);
		}
	}

	@Override
	public ComplexNetwork load(String xml) throws IOException {
		ComplexNetwork network = null;
		try {
			network = (ComplexNetwork)JaxbUtils.getInstance().load(xml, ComplexNetwork.class);
			network.evaluate();
		} catch (JAXBException e) {
			throw new IOException(e);
		}
		return network;
	}

	public static ComplexNetworkDaoXml getInstance() {
		return instance;
	}

}
