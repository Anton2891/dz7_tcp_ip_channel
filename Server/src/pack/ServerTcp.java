package pack;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class ServerTcp {
    public static void main(String[] args) {
        new ServerTcp().start();
    }

    final String ADR = "localhost";
    final int PORT = 3030;
    private Selector selector;

    final void start() {
        System.out.println("[SERVER]");
        try (ServerSocketChannel socketChannel = ServerSocketChannel.open()) {
            selector = Selector.open();
            InetSocketAddress socketAddress = new InetSocketAddress(ADR, PORT);
            socketChannel.bind(socketAddress);
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, socketChannel.validOps());
            while (true) {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) {
                        SocketChannel client = socketChannel.accept();
                        client.configureBlocking(false);
                        System.out.println("server> connected: " + client.getRemoteAddress());
                        client.register(selector, SelectionKey.OP_READ);
                        key.interestOps(SelectionKey.OP_ACCEPT);
                    } else if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(256);
                        client.read(buffer);
                        String line = new String(buffer.array()).trim();
                        System.out.println("client>..." + client.getRemoteAddress() + ">: " + line);
                        if (line.equalsIgnoreCase("/q")) {
                            client.close();
                        } else
                            bordMsg("server>..." + client.getRemoteAddress() + ">: " + line);
                    }
                }
                selector.selectedKeys().clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bordMsg(String msg) throws IOException {
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        for (SelectionKey k : selector.keys()) {
            Channel target = k.channel();
            if (target.isOpen() && target instanceof SocketChannel) {
                SocketChannel channel = (SocketChannel) target;
                channel.write(buffer);
                buffer.flip();
            }
        }
    }
}
