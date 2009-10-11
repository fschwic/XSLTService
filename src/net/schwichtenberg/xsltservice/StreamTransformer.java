package net.schwichtenberg.xsltservice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

import net.schwichtenberg.http.exceptions.TransformerException;

/**
 * A singleton holding transformers for specific XSLTs. Transformation is
 * streamed.
 * 
 * @author Frank Schwichtenberg <http://frank.schwichtenberg.net>
 * 
 */
public class StreamTransformer implements
		net.schwichtenberg.xsltservice.Transformer {

	private static final Logger LOGGER = Logger
			.getLogger(StreamTransformer.class);

	private static StreamTransformer instance;

	private TransformerPool transformerPool;

	private StreamTransformer() {
		super();
		this.transformerPool = new TransformerPool();
	}

	public static StreamTransformer getInstance() {
		if (instance == null) {
			instance = new StreamTransformer();
		}

		return instance;
	}

	/**
	 * Get an instance of StreamTransformer that has a transformer for given
	 * XSLT. May be an advantage if encoding and mime-type of transforming
	 * result must be known before first transformation.
	 * 
	 * @throws TransformerException
	 * 
	 * @deprecated
	 */
	public static StreamTransformer getInstance(URL xslt)
			throws TransformerException {
		if (instance == null) {
			instance = new StreamTransformer();
		}

		instance.createTransformer(xslt);

		return instance;
	}

	/**
	 * Returns the encoding of the transformation result as defined in the XSLT
	 * document.
	 * 
	 * @throws TransformerException
	 */
	public String getOutputEncoding(URL xslt) throws TransformerException {

		String enc = null;

		try {
			Transformer t = (Transformer) this.transformerPool
					.borrowObject(xslt);
			enc = t.getOutputProperty("encoding");
			this.transformerPool.returnObject(xslt, t);
		} catch (Exception e) {
			if (e instanceof TransformerException) {
				TransformerException te = (TransformerException) e;
				throw te;
			} else {
				throw new TransformerException(
						"TransformerPool throws exception.", e);
			}
		}

		return enc;
	}

	/**
	 * Returns the mime-type of the transformation result as defined in the XSLT
	 * document.
	 * 
	 * @throws TransformerException
	 */
	public String getOutputMimeType(URL xslt) throws TransformerException {

		String method = null;

		try {
			Transformer t = (Transformer) this.transformerPool
					.borrowObject(xslt);
			method = t.getOutputProperty("method");
			this.transformerPool.returnObject(xslt, t);
		} catch (Exception e) {
			if (e instanceof TransformerException) {
				TransformerException te = (TransformerException) e;
				throw te;
			} else {
				throw new TransformerException(
						"TransformerPool throws exception.", e);
			}
		}

		if (method.equalsIgnoreCase("text")) {
			method = "plain";
		}
		return "text/" + method;
	}

	/**
	 * Transform with given parameters.
	 * 
	 * @throws TransformerException
	 */
	public void transform(OutputStream out, InputStream xml, URL xslt,
			Map<String, String> xsltParams) throws TransformerException {

		try {

			Transformer t = (Transformer) this.transformerPool
					.borrowObject(xslt);

			t.clearParameters();
			if (xsltParams != null) {
				Iterator<String> paramsIterator = xsltParams.keySet()
						.iterator();
				while (paramsIterator.hasNext()) {
					String paramKey = paramsIterator.next();
					t.setParameter(paramKey, xsltParams.get(paramKey));
				}
			}
			t.transform(new StreamSource(xml), new StreamResult(out));

			this.transformerPool.returnObject(xslt, t);

		} catch (Exception e) {
			if (e instanceof TransformerException) {
				TransformerException te = (TransformerException) e;
				throw te;
			} else {
				throw new TransformerException(
						"TransformerPool throws exception.", e);
			}
		}
	}

	/**
	 * Transform without parameters.
	 * 
	 * @throws TransformerException
	 */
	public void transform(OutputStream out, InputStream xml, URL xslt)
			throws TransformerException {
		this.transform(out, xml, xslt, null);
	}

	/**
	 * Creates a new transformer for the given transformation instruction URL
	 * and stores it the internal list of transformers. Unless a tranformer for
	 * this URL already exists. An appropriate re-cache mechanism must be
	 * implemented, soon.
	 * 
	 * @param xslt
	 *            The URL pointing to transformation instruction document.
	 * @throws TransformerException
	 * 
	 * @deprecated
	 */
	public void createTransformer(URL xslt) throws TransformerException {

		try {
			Transformer t = (Transformer) this.transformerPool
					.borrowObject(xslt);
			this.transformerPool.returnObject(xslt, t);
		} catch (Exception e) {
			if (e instanceof TransformerException) {
				TransformerException te = (TransformerException) e;
				throw te;
			} else {
				throw new TransformerException(
						"TransformerPool throws exception.", e);
			}
		}
	}

	public void removeTransformer(URL xslt) {
		this.transformerPool.clear(xslt);
	}

	public void reset() {
		this.transformerPool.clear();
		// instance = null;
	}

}
