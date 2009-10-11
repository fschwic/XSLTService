package net.schwichtenberg.xsltservice;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class ResetServlet extends HttpServlet {

	private static final Logger LOGGER = Logger.getLogger(ResetServlet.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		StreamTransformer.getInstance().reset();
		LOGGER.info("The current StreamTransformer is reseted.");
		resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}

}
