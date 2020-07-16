package net.qfstudio.relay;

import java.net.Socket;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * 可以相互通信的对等端
 */
public class Peer {
    private String classname;
    private String hashID;

    private String serverHost;
    private int serverPort;
    private String label;

    private Socket socket;

    public Peer(String serverHost, int serverPort, String peerLabel) {
        this.classname = this.getClass().getName();
        this.hashID = Integer.toHexString(System.identityHashCode(this));
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.label = peerLabel;
    }

    public void start() {
        try {
            socket = new Socket(serverHost, serverPort);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true) ;
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()), 1);

            out.println(String.format("ClientLabel %s", this.label));

            String response;
            while ((response = in.readLine()) != null) {
                log(response);
                if (response.equals("PeerConnectionPreceding")) {
                    break;
                }
            }

            log("I'm going to say something.");

            for (int i = 0; i < 10 * Math.random() ; ++i) {
                log("I said " + Integer.toString(i));
                out.println(String.format("%s says %d.", this.hashID, i));
            }

            while ((response = in.readLine()) != null) {
                log(response);
            }

            socket.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    protected void log(String str) {
        System.out.println(String.format("%s <%s> %s", this.hashID, this.classname, str));
    }
}
