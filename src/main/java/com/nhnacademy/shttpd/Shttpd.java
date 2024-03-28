package com.nhnacademy.shttpd;

import java.net.ServerSocket;
import java.net.Socket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Shttpd extends Thread{
    private String host;
    private int port = 80;

    @Override
    public void run(){
        log.trace("Server Start");
        while(!Thread.currentThread().isInterrupted()){
            try(ServerSocket serverSocket = new ServerSocket(port)){
                Socket socket = serverSocket.accept();
                ShttpdController controller = new ShttpdController(socket);
                log.trace("client {} connected", socket.getLocalAddress());

                controller.start();
            } catch(Exception e){
                System.err.println(e.getMessage());
            }
        }
    }
}
