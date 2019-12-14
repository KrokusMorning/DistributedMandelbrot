package server.net;

import server.model.Mandelbrot;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ForkJoinPool;


public class ClientHandler implements Runnable{

    private final SocketChannel socketChannel;
    private final ByteBuffer clientMessageBuffer = ByteBuffer.allocateDirect(1000000);
    private final Queue<String> messageInbox = new ArrayDeque<>();
    private Mandelbrot mandelbrot;

    ClientHandler(SocketChannel socketChannel, Mandelbrot mandelbrot) {
        this.socketChannel = socketChannel;
        this.mandelbrot = mandelbrot;
    }

    @Override
    public void run() {
        

        while (!messageInbox.isEmpty()) {
            String params[] = messageInbox.poll().split("/");
                if(params[0].equals("GET ")){
                    if(params[1].equals("mandelbrot")){
                        this.mandelbrot = new Mandelbrot(Double.parseDouble(params[2]), Double.parseDouble(params[3]), Double.parseDouble(params[4]), Double.parseDouble(params[5]),
                                Integer.parseInt(params[6]), Integer.parseInt(params[7]), Integer.parseInt(params[8]));
                        String mb = mandelbrot.generateMandelbrot();
                        mb = "PGMPART:" + mb;
                        divideandSend(mb);
                    }
                }
        }
    }

    void sendMessage(ByteBuffer msg) {
        try {
            socketChannel.write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    public void messageIn() {
        clientMessageBuffer.clear();
        try {
            socketChannel.read(clientMessageBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientMessageBuffer.flip();
        byte[] bytes = new byte[clientMessageBuffer.remaining()];
        clientMessageBuffer.get(bytes);
        String command = new String(bytes);
        messageInbox.add(command);
        ForkJoinPool.commonPool().execute(this);
    }

    /**
     * Divides messages into smaller pieces since not doing so resulted in incomplete messages reaching the client.
     * A 10ms delay is added to avoid lost data.
     * @param message, message to be sent to client.
     */
   private void  divideandSend(String message){
       int divisions = 0;
       String messagePart;
       if(message.length() > 500000){
           for(int i = message.length(); true; i /= 2){
               divisions++;
               if(i < 500000){
                   divisions = (int) Math.pow(2, divisions);
                   break;
               }
           }
           for (int i = 0; i <= divisions; i++){
               if(i == divisions){
                   messagePart = message.substring(((message.length() /divisions)) * i);
               }
               else{
                   messagePart = message.substring(((message.length() /divisions)) * i, ((message.length() /divisions)) * (i+1));
               }
               sendMessage(ByteBuffer.wrap((messagePart).getBytes()));

               try {
                   Thread.sleep(10);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }
       }
       else{
           sendMessage(ByteBuffer.wrap((message).getBytes()));

       }
       sendMessage(ByteBuffer.wrap((":END").getBytes()));
   }
}
