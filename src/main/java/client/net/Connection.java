package client.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * Class that handles connection with server.
 */
public class Connection implements Runnable{
    private SocketChannel socketChannel;
    private Selector selector;
    private String host;
    private int port;
    private boolean connected = false;
    private final Queue<ByteBuffer> messageOutbox = new ArrayDeque<>();
    private boolean send = false;
    private final ByteBuffer serverMessageBuffer = ByteBuffer.allocateDirect(1000000);
    private Listener listener;

    public void connect (String host, int port) {
        this.host = host;
        this.port = port;
        new Thread(this).start();
    }

    @Override
    public void run() {

        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(host, port));
            connected = true;
            selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
            String completeMessage = "";

            while (connected || !messageOutbox.isEmpty()) {
                if (send) {
                    socketChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
                    send = false;
                }

                selector.select();
                for (SelectionKey key : selector.selectedKeys()) {
                    selector.selectedKeys().remove(key);
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isConnectable()) {
                        socketChannel.finishConnect();
                        messageToView("CONNECTED TO:" + host + ":" + port);
                        key.interestOps(SelectionKey.OP_READ);

                    } else if (key.isReadable()) {
                        serverMessageBuffer.clear();
                        int numOfReadBytes = socketChannel.read(serverMessageBuffer);
                        if (numOfReadBytes == -1) {
                            throw new IOException();
                        }
                        serverMessageBuffer.flip();
                        byte[] bytes = new byte[serverMessageBuffer.remaining()];
                        serverMessageBuffer.get(bytes);
                        String fromServer = new String(bytes);
                        completeMessage = completeMessage + fromServer;
                        if(fromServer.endsWith("END")){
                            completeMessage = completeMessage.substring(0, completeMessage.length()-5)  + ":" + host + ":" + port;
                            messageToView(completeMessage);
                            completeMessage = "";
                        }

                    } else if (key.isWritable()) {
                        ByteBuffer buffer;
                        buffer = messageOutbox.poll();
                        socketChannel.write(buffer);
                        if (buffer.hasRemaining()) {
                            buffer.compact();
                        } else {
                            buffer.clear();
                            key.interestOps(SelectionKey.OP_READ);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void sendMessage(String... message) {
        StringBuilder sb = new StringBuilder();
        for (String aMessage : message) {
            sb.append(aMessage + " ");
        }
        synchronized (messageOutbox) {
            messageOutbox.add(ByteBuffer.wrap(sb.toString().getBytes()));
        }
        send = true;
        selector.wakeup();
    }

    public void disconnect() throws IOException {
        socketChannel.close();
        socketChannel.keyFor(selector).cancel();
        connected = false;
        messageToView("DISCONNECTED:" + host + ":" + port);
    }

    public void dataToServer(String data){
        sendMessage(data);
    }

    private void messageToView(String msg) {
        Executor pool = ForkJoinPool.commonPool();
        pool.execute(() -> listener.handleMsg(msg));
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public boolean isConnected() {
        return connected;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }
}
