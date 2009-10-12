package net.schwichtenberg.xsltservice;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.schwichtenberg.http.exceptions.HttpException;
import net.schwichtenberg.http.exceptions.InvalidParameterException;

import org.apache.log4j.Logger;

public class XSLTServlet extends HttpServlet {

	private static final Logger LOGGER = Logger.getLogger(XSLTServlet.class);

	/**
	 * Delay in seconds an undesirable request must wait.
	 */
	private static final int UNDESIRABLE_REQUEST_DELAY = 5;

	private static final StreamTransformer TRANSFORMER = StreamTransformer
			.getInstance();

	private List<String> allowedHostsXml = new Vector<String>();

	private List<String> allowedHostsXslt = new Vector<String>();

	public XSLTServlet() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {

		try {
			// get params
			URL xml;
			URL xslt;
			try {
				xml = new URL(req.getParameter("xml"));
				xslt = new URL(req.getParameter("xslt"));
			} catch (MalformedURLException e) {
				throw new InvalidParameterException(
						"Both the value of xml and xslt must be a valid URL.",
						e);
			}
			Map<String, String> params = new HashMap<String, String>();
			Iterator paramIt = req.getParameterMap().keySet().iterator();
			while (paramIt.hasNext()) {
				String key = (String) paramIt.next();
				params.put(key, req.getParameter(key));
			}

			// check if desirable request, otherwise wait UNDESIRABLE_USER_DELAY
			if (!allowedHostsXml.contains(xml.getHost())
					|| !allowedHostsXslt.contains(xslt.getHost())) {
				try {
					LOGGER.info("Delaying request. xml[" + xml.getHost()
							+ "] xslt[" + xslt.getHost() + "]");
					Thread.sleep(UNDESIRABLE_REQUEST_DELAY * 1000);
				} catch (InterruptedException e) {
					throw new ServletException(e);
				}
			}

			// prepare response and transform
			TRANSFORMER.createTransformer(xslt);
			resp.setCharacterEncoding(TRANSFORMER.getOutputEncoding(xslt));
			resp.setContentType(TRANSFORMER.getOutputMimeType(xslt));

			try {
				TRANSFORMER.transform(resp.getOutputStream(), xml.openStream(),
						xslt, params);
			} catch (IOException e) {
				// TODO specify
				throw new HttpException(e);
			}
		} catch (HttpException e) {
			e.send(resp);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.doGet(req, resp);
	}

	@Override
	public void init() throws ServletException {
		super.init();
		allowedHostsXml.add("localhost");
		allowedHostsXml.add("frank.schwichtenberg.net");
		allowedHostsXml.add("antonia.schwichtenberg.net");
		allowedHostsXml.add("www.java.de");
		allowedHostsXml.add("www.tagesschau.de");
		allowedHostsXml.add("www.ionio.gr");
		allowedHostsXml.add("www.klack.de");
		allowedHostsXml.add("fugu.de");
		allowedHostsXslt.add("localhost");
		allowedHostsXslt.add("frank.schwichtenberg.net");
		allowedHostsXslt.add("antonia.schwichtenberg.net");
		allowedHostsXslt.add("www.java.de");
	}
}
