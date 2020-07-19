package net.qfstudio.relay;

import java.util.HashMap;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;

/**
 * @TODO
 *
 * 1. 抵御恶意连接。
 * 2. 优化并发的同步问题。
 * 3. 继续重构。
 */

/**
 * 充当中介的服务器端
 */
public class Server {
    protected LogUtil util;

    private int port;
    private HashMap<String, Socket> previousSockets;

    private ServerSocket serverSocket;

    public Server(int port) {
        this.util = LogUtil.setupUtilFor(this);
        this.port = port;
        this.previousSockets = new HashMap<String, Socket>();
    }

    public static void main(String[] args) {
        int port = 15267;
        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }

        Server server = new Server(port);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.stop();
            }
        });

        try {
            server.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(this.port);
        util.log(String.format("Server operating at <localhost:%d>", this.port));

        new Thread() {
            @Override
            public void run() {
                mainLoop();
            }
        }.start();
    }

    public void stop() {
        util.log("Shutting down server.");

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        util.log("Shut.");
    }

    protected void mainLoop() {
        while (true) {
            try {
                Socket nextClientSocket = serverSocket.accept();
                util.log(String.format("New socket connection: %s",
                        nextClientSocket.getRemoteSocketAddress().toString()));

                new Thread() {
                    @Override
                    public void run() {
                        handleClientSocket(nextClientSocket);
                    }
                }.start();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    protected void handleClientSocket(Socket clientSocket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String clientInput = in.readLine();
            if (clientInput.startsWith("RelaySocket Protocol v1")) {
                clientInput = in.readLine();

                String label = clientInput.replaceFirst("ClientLabel", "").trim();

                synchronized (this) {
                    Socket previousSocket = previousSockets.get(label);

                    if (previousSocket != null) {
                        previousSockets.remove(label);
                        twistSockets(previousSocket, clientSocket);

                        util.log(String.format("label %s: Peer connection established.", label));
                    } else {
                        out.println("WaitForPeerConnect");
                        previousSockets.put(label, clientSocket);

                        util.log(String.format("label %s: Got first peer.", label));
                    }
                }
            } else {
                util.log("Unrecognized protocol.");
                clientSocket.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    protected void twistSockets(Socket a, Socket b) throws IOException {
        try {
            PrintWriter aOut = new PrintWriter(a.getOutputStream(), true);
            PrintWriter bOut = new PrintWriter(b.getOutputStream(), true);

            aOut.println("PeerConnectionPreceding");
            bOut.println("PeerConnectionPreceding");

            new Thread() {
                @Override
                public void run() {
                    glueSocketStreams(a, b);
                }
            }.start();

            new Thread() {
                @Override
                public void run() {
                    glueSocketStreams(b, a);
                }
            }.start();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    protected void glueSocketStreams(Socket in, Socket out) {
        try {
            InputStream inStream  = in.getInputStream();
            OutputStream outStream = out.getOutputStream();
            inStream.transferTo(outStream);
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
}
