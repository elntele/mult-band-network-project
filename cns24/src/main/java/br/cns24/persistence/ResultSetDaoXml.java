package br.cns24.persistence;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import br.cns24.experiments.ComplexNetwork;
import br.cns24.experiments.ResultSet;

public class ResultSetDaoXml implements IResultSet {
	private static final ResultSetDaoXml instance = new ResultSetDaoXml();

	private ResultSetDaoXml() {
	}

	@Override
	public void save(String xml, ResultSet object) throws IOException {
		try {
			JaxbUtils.getInstance().save(xml, object);
		} catch (JAXBException e) {
			throw new IOException(e);
		}
	}

	@Override
	public ResultSet load(String xml) throws IOException {
		ResultSet network = null;
		try {
			network = (ResultSet)JaxbUtils.getInstance().load(xml, ComplexNetwork.class);
		} catch (JAXBException e) {
			throw new IOException(e);
		}
		return network;
	}

	public static ResultSetDaoXml getInstance() {
		return instance;
	}

}
