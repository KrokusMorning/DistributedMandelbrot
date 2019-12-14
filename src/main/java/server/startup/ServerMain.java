package server.startup;

import server.net.MandelbrotServer;

public class ServerMain {

    public static void main(String[] args) {

        MandelbrotServer server = new MandelbrotServer();
        server.serve();
    }
}
