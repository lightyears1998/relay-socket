package net.qfstudio.relay;

import org.junit.Test;


public class AppTest {
    public static void main(String[] args) {
        new AppTest().SimpleTest();
    }

    @Test
    public void SimpleTest() {
        int port = 5354;

        Server server = new Server(port);

        new Thread(){
            @Override
            public void run() {
                server.start();
            }
        }.start();

        new Thread(){
            @Override
            public void run() {
                Peer peer = new Peer("localhost", port, "a1");
                peer.start();
            }
        }.start();

        new Thread(){
            @Override
            public void run() {
                Peer peer = new Peer("localhost", port, "a1");
                peer.start();
            }
        }.start();
    }
}
