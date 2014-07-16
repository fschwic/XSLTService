package net.schwichtenberg.xsltservice;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.schwichtenberg.http.exceptions.HttpException;
import net.schwichtenberg.http.exceptions.InvalidParameterException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XSLTServlet extends HttpServlet {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(XSLTServlet.class);

	/**
	 * Delay in seconds an undesirable request must wait.
	 */
	private int undesirable_request_delay = 5;

	private Set<String> allowedHostsXml = new HashSet<String>();

	private Set<String> allowedHostsXslt = new HashSet<String>();

	public XSLTServlet() {
		super();
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// allow all cross site requests
		resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
		resp.setHeader("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		super.service(req, resp);

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

			// check if desirable request, otherwise wait
			// undesirable_request_delay
			if (!allowedHostsXml.contains(xml.getHost())
					|| !allowedHostsXslt.contains(xslt.getHost())) {
				try {
					LOGGER.info("Delaying request. xml[" + xml.getHost()
							+ "] xslt[" + xslt.getHost() + "]");
					// TODO which one is better
					// this.wait(undesirable_request_delay * 1000);
					Thread.sleep(undesirable_request_delay * 1000);
				} catch (InterruptedException e) {
					throw new ServletException(e);
				}
			}

			// prepare response and transform
			// StreamTransformer.getInstance().createTransformer(xslt);
			resp.setCharacterEncoding(StreamTransformer.getInstance()
					.getOutputEncoding(xslt));
			resp.setContentType(StreamTransformer.getInstance()
					.getOutputMimeType(xslt));

			try {
				StreamTransformer.getInstance().transform(
						resp.getOutputStream(), xml.openStream(), xslt, params);
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
		LOGGER.debug("Initialising");
		super.init();

		String configFileName = getServletConfig().getInitParameter("config");
		Properties config = new Properties();
		try {
			Properties props = System.getProperties();
			String configFile = (String) props.get("catalina.home") + "/conf/"
					+ configFileName;

			Reader configReader = new FileReader(configFile);
			config.load(configReader);
		} catch (IOException e) {
			LOGGER.error("Could not load configuration. " + configFileName);
			LOGGER.warn("No configuration read. Using default values.");
		}

		// override default delay from config
		if (config.getProperty("undesirable.request.delay") != null) {
			undesirable_request_delay = Integer.parseInt(config
					.getProperty("undesirable.request.delay"));
		}

		// load trusted host for xml from config
		if (config.getProperty("xml.url.allowed.hostname") != null) {
			String[] hostsXml = config.getProperty("xml.url.allowed.hostname")
					.split(",");
			for (String host : hostsXml) {
				LOGGER.info("Adding host XML: " + host);
				allowedHostsXml.add(host);
			}
		}

		// load trusted host for xsl from config
		if (config.getProperty("xslt.url.allowed.hostname") != null) {
			String[] hostsXslt = config
					.getProperty("xslt.url.allowed.hostname").split(",");
			for (String host : hostsXslt) {
				LOGGER.info("Adding host XSL: " + host);
				allowedHostsXslt.add(host);
			}
		}
	}

	@Override
	public void destroy() {
		LOGGER.debug("Destroing.");
		super.destroy();
	}
}
