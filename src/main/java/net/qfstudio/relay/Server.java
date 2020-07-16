package net.qfstudio.relay;

import java.util.HashMap;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;


/**
 * 充当中介的服务器端
 */
public class Server {
    private String classname;
    private String hashID;

    private int port;
    private HashMap<String, Socket> previous;

    private ServerSocket server;

    public Server(int port) {
        this.classname = this.getClass().getName();
        this.hashID = Integer.toHexString(System.identityHashCode(this));
        this.port = port;
        this.previous = new HashMap<String, Socket>();
    }

    public void start() {
        try {
            server = new ServerSocket(this.port);
            mainLoop();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    protected void mainLoop() {
        while (true) {
            try {
                Socket socket = server.accept();

                new Runnable(){
                    @Override
                    public void run() {
                        try {
                            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()), 1);
                            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                            String clientInput = in.readLine();
                            if (clientInput.startsWith("ClientLabel")) {
                                String label = clientInput.replaceFirst("ClientLabel", "").trim();
                                Socket previousSocket = previous.get(label);
                                if (previousSocket != null) {
                                    out.println("PeerConnectionPreceding");
                                    log(String.format("label %s: Peer connection established.", label));

                                    BufferedReader previousIn = new BufferedReader(new InputStreamReader(previousSocket.getInputStream()), 1);
                                    PrintWriter previousOut = new PrintWriter(previousSocket.getOutputStream(), true);
                                    previousOut.println("PeerConnectionPreceding");

                                    new Thread() {
                                        @Override
                                        public void run() {
                                            try {
                                                String buffer;
                                                while ((buffer = in.readLine()) != null) {
                                                    log(buffer);
                                                    previousOut.println(buffer);
                                                }
                                            } catch (IOException ex) {
                                                ex.printStackTrace();
                                            }
                                        }
                                    }.start();

                                    new Thread() {
                                        @Override
                                        public void run() {
                                            try {
                                                String buffer;
                                                while ((buffer = previousIn.readLine()) != null) {
                                                    log(buffer);
                                                    out.println(buffer);
                                                }
                                            } catch (IOException ex) {
                                                ex.printStackTrace();
                                            }
                                        }
                                    }.start();
                                } else {
                                    out.println("WaitForPeerConnect");
                                    previous.put(label, socket);

                                    log(String.format("label %s: Got first peer.", label));
                                }
                            } else {
                                log("Unrecognized protocol.");
                            }

                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    protected void log(String str) {
        System.out.println(String.format("%s <%s> %s", this.hashID, this.classname, str));
    }
}
