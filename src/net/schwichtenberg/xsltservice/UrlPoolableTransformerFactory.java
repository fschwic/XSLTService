package net.schwichtenberg.xsltservice;

import java.io.IOException;
import java.net.URL;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import net.schwichtenberg.http.exceptions.TransformerException;

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.log4j.Logger;

public class UrlPoolableTransformerFactory extends
		BaseKeyedPoolableObjectFactory implements KeyedPoolableObjectFactory {

	private static final Logger LOGGER = Logger
			.getLogger(UrlPoolableTransformerFactory.class);

	private TransformerFactory factory;

	public UrlPoolableTransformerFactory() {
		super();
		this.factory = TransformerFactory.newInstance();
		LOGGER.debug("Created factory.");
	}

	@Override
	public Transformer makeObject(Object key) throws TransformerException {
		LOGGER.debug("Making new Transformer.");
		URL xslt = (URL) key;
		Transformer transformer;
		try {
			transformer = this.factory.newTransformer(new StreamSource(xslt
					.openStream()));
		} catch (IOException e) {
			throw new TransformerException("Can not access " + xslt + ".", e);
		} catch (TransformerConfigurationException e) {
			throw new TransformerException("Can not create transformer for "
					+ xslt + ".", e);
		}
		LOGGER.debug("Made Transformer");
		return transformer;
	}

}
