package net.schwichtenberg.xsltservice;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ResetServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		StreamTransformer.getInstance().reset();
		getServletContext().log("The current StreamTransformer is reseted.");
		resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}

}
