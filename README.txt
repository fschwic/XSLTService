== XSLT Service ==
You may send the URLs of an XML and an XSLT file to this service
in order to get the XML transformed by the XSLT as response.

See index page for usage information.

=== Configuration ===
Configuration is read from catlina home conf directory. Default 
config file name is xsltservice.properties. 
See xsltservice.example.properties.

Every request send to XSLTervice where the domain of XML or XSLT
document is not known by the service is considered as undesirable
and is delayed. To avoid the delay configure the hostnames you 
want to load XML or XSLT documents from.

=== Logging ===
XSLTService uses SLF4J for logging. If the used runtime container
does not provide SLF4J the slf4j-api.jar and a logger binder should
be put in the runtime containers classpath.
