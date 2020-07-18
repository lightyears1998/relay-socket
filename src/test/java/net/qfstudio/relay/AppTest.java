package net.qfstudio.relay;

import java.io.IOException;

import org.junit.Test;


public class AppTest {
    public static void main(String[] args) {
        new AppTest().SimpleTest();
    }

    @Test
    public void SimpleTest() {
        int port = 5354;

        Server server = new Server(port);
        try {
            server.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        new Thread("Peer 1") {
            @Override
            public void run() {
                Peer peer = new Peer("localhost", port, "a1");
                peer.start();
            }
        }.start();

        new Thread("Peer 2") {
            @Override
            public void run() {
                Peer peer = new Peer("localhost", port, "a1");
                peer.start();
            }
        }.start();
    }
}
