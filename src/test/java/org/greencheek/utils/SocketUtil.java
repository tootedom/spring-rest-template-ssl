package org.greencheek.utils;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * User: dominictootell
 * Date: 03/08/2013
 * Time: 15:39
 */
public class SocketUtil {
    public static int findUnusedPort() throws IOException {
        ServerSocket server = new ServerSocket(0);
        int port = server.getLocalPort();
        server.close();
        return port;
    }

}
