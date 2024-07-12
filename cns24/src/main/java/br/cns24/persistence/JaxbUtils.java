package br.cns24.persistence;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class JaxbUtils {
	private static final JaxbUtils instance = new JaxbUtils();

	private JaxbUtils() {

	}

	/**
	 * Salva o objeto passado em um arquivo xml
	 * 
	 * @param xml
	 *            Path do arquivo xml a ser salvo
	 * @param object
	 *            Objeto com nota��es JAXB
	 * @throws JAXBException
	 *             Caso ocorra algum erro no processo de serializa��o
	 */
	public void save(String xml, Object object) throws JAXBException {
		File file = new File(xml);
		JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		jaxbMarshaller.marshal(object, file);
	}

	/**
	 * Carrega um objeto da classe especificada.
	 * 
	 * @param xml
	 *            Path do arquivo xml a ser salvo
	 * @param cls
	 *            Classe a ser usada
	 * @return Objeto correspondente ao XML.
	 * @throws JAXBException
	 *             Caso ocorra algum erro no processo de deserializa��o
	 */
	public Object load(String xml, Class cls) throws JAXBException {
		File file = new File(xml);
		JAXBContext jaxbContext = JAXBContext.newInstance(cls);

		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

		return jaxbUnmarshaller.unmarshal(file);
	}

	public static JaxbUtils getInstance() {
		return instance;
	}
}
