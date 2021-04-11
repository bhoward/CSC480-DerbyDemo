package edu.depauw.csc480.projectv5;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

/**
 * Demo of starting an embedded Tomcat 10 server. Based in part on
 * <a href="https://zetcode.com/web/embeddedtomcat/">this tutorial</a>. This is
 * a minimal example of using servlets to create a database frontend; almost
 * every decision made here could have been made differently to allow for
 * greater scalability and maintainability, at the cost of greater complexity.
 */
public class Server {

	public static void main(String[] args) throws LifecycleException {
		Tomcat tomcat = new Tomcat();
		tomcat.setBaseDir("temp");

		Connector httpConnector = new Connector();
		httpConnector.setPort(8080);
		tomcat.setConnector(httpConnector);

		// Add a servlet for the University database
		Context ctx = tomcat.addContext("/University", new File("webapps/University").getAbsolutePath());
		Tomcat.addServlet(ctx, "University", new ServletStudentData());
		ctx.addServletMappingDecoded("/StudentData", "University");
		tomcat.initWebappDefaults("/University");

		tomcat.start();
		tomcat.getServer().await();
	}

}
