package io.github.jhannes.execjardemo;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

public class DemoServer {

    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);

        server.setHandler(new WebAppContext(Resource.newClassPathResource("/demo-webapp"), "/"));

        server.start();
    }

}
