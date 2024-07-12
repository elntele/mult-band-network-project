package br.cns24.persistence;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import br.cns24.som.RedeKohonen;

public class RedeKohonenXmlDao implements IRedeKohonenDao {
	private static final RedeKohonenXmlDao instance = new RedeKohonenXmlDao();

	private RedeKohonenXmlDao() {
	}

	@Override
	public void save(String xml, RedeKohonen object) throws IOException {
		try {
			JaxbUtils.getInstance().save(xml, object);
		} catch (JAXBException e) {
			throw new IOException(e);
		}
	}

	@Override
	public RedeKohonen load(String xml) throws IOException {
		RedeKohonen network = null;
		try {
			network = (RedeKohonen) JaxbUtils.getInstance().load(xml,
					RedeKohonen.class);
		} catch (JAXBException e) {
			throw new IOException(e);
		}
		return network;
	}

	public static RedeKohonenXmlDao getInstance() {
		return instance;
	}

}
