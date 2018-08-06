package com.imooc.miaosha;

import org.apache.commons.lang3.RandomUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EpollClient implements Runnable {

    public static void main(String[] args) {

        ExecutorService pool = new ThreadPoolExecutor(3, 3,
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        for (int i = 0; i < 3; i++) {
            pool.execute(new EpollClient());
        }
        pool.shutdown();

        System.out.println(pool.isTerminated());

    }

    @Override
    public void run() {
        createThreads();
    }

    private void createThreads() {
        try (SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.connect(new InetSocketAddress("127.0.0.1", 8000));

            ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
            ByteBuffer readBuffer = ByteBuffer.allocate(1024);

            writeBuffer.put((Thread.currentThread().getName() +
                    Double.toString(RandomUtils.nextDouble())).getBytes());
            writeBuffer.flip();

            for (int i = 0; i < 3; i++) {
                writeBuffer.rewind();
                socketChannel.write(writeBuffer);

                Thread.sleep(5000);

                readBuffer.clear();
                socketChannel.read(readBuffer);
            }
//            while (true) {
//                writeBuffer.rewind();
//                socketChannel.write(writeBuffer);
//
//                Thread.sleep(1000);
//
//                readBuffer.clear();
//                socketChannel.read(readBuffer);
//            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
