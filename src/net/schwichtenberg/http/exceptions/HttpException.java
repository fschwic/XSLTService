package net.schwichtenberg.http.exceptions;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class HttpException extends Exception {

	private static final long serialVersionUID = 1L;

	private static final int STATUS_CODE = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

	private static final String STATUS_MESSAGE = "Internal Server Error";

	public HttpException() {
		super();
	}

	public HttpException(String message, Throwable cause) {
		super(message, cause);
	}

	public HttpException(String message) {
		super(message);
	}

	public HttpException(Throwable cause) {
		super(cause);
	}

	public int getHttpStatusCode() {
		return STATUS_CODE;
	}

	public String getHttpStatusMessage() {
		return STATUS_MESSAGE;
	}

	public void send(HttpServletResponse response) throws ServletException {
		try {
			response.setStatus(this.getHttpStatusCode());
			response.setHeader("X-Exception-Type", this.getClass().getName());
			response.setContentType("text/xml");
			XMLOutputFactory of = XMLOutputFactory.newInstance();
			XMLStreamWriter writer = of.createXMLStreamWriter(response
					.getOutputStream());
			this.toXml(writer);
		} catch (IOException ioe) {
			throw new ServletException("While trying to handle exception: "
					+ this.getClass().getSimpleName() + ": "
					+ this.getMessage(), ioe);
		} catch (XMLStreamException e) {
			throw new ServletException(e);
		}
	}

	private void toXml(XMLStreamWriter writer) throws ServletException {
		StringWriter sw = new StringWriter();
		this.printStackTrace(new PrintWriter(sw));

		String namespaceURI = HttpException.class.getCanonicalName();
		try {
			writer.setPrefix("ex", namespaceURI);
			writer.writeStartElement(namespaceURI, "exception");
			writer.writeNamespace("ex", namespaceURI);
			writer.writeStartElement(namespaceURI, "name");
			writer.writeCharacters(this.getClass().getSimpleName());
			writer.writeEndElement();
			writer.writeStartElement(namespaceURI, "status-code");
			writer.writeCharacters(Integer.toString(this.getHttpStatusCode()));
			writer.writeEndElement();
			writer.writeStartElement(namespaceURI, "status-message");
			writer.writeCharacters(this.getHttpStatusMessage());
			writer.writeEndElement();
			writer.writeStartElement(namespaceURI, "type");
			writer.writeCharacters(this.getClass().getCanonicalName());
			writer.writeEndElement();
			writer.writeStartElement(namespaceURI, "message");
			writer.writeCharacters(this.getMessage());
			writer.writeEndElement();
			writer.writeStartElement(namespaceURI, "stack-trace");
			writer.writeCData(sw.toString());
			writer.writeEndElement();
			writer.writeEndElement();
			writer.close();
		} catch (XMLStreamException e) {
			throw new ServletException(e);
		}
	};

}
