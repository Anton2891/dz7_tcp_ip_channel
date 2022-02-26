package pack;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class ClientTcp_1 {
    public static void main(String[] args) {
        new ClientTcp_1().start();
    }

    final String ADR = "localhost";
    final int PORT = 3030;

    final void start(){
        System.out.println("[CLIENT]");
        InetSocketAddress address = new InetSocketAddress(ADR, PORT);
        try (SocketChannel socet = SocketChannel.open(address)) {
            Selector selector = Selector.open();
            socet.configureBlocking(false);
            Scanner scanner = new Scanner(System.in);
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            String line;
            socet.register(selector, SelectionKey.OP_READ);
            new Thread(() -> {
                SocketChannel client = null;
                try {
                    while (true) {
                        selector.select();
                        Set<SelectionKey> set = selector.selectedKeys();
                        Iterator<SelectionKey> iterator = set.iterator();
                        while (iterator.hasNext()){
                            SelectionKey key = iterator.next();
                            iterator.remove();
                            if (key.isReadable()){
                                client = (SocketChannel) key.channel();
                                buffer.clear();
                                client.read(buffer);
                                buffer.flip();
                                System.out.println(new String(buffer.array(), buffer.position(), buffer.limit()));
                            }
                        }
                        set.clear();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            do {
                System.out.println("client_1>: ");
                line = scanner.nextLine();
                buffer.clear();
                buffer.put(line.getBytes(StandardCharsets.UTF_8));
                buffer.flip();
                socet.write(buffer);
            } while (!line.equalsIgnoreCase("/q"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
