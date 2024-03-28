package com.nhnacademy.shttpd;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import com.nhnacademy.domain.Request;
import com.nhnacademy.domain.Response;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShttpdController implements Runnable{
    static final String CRLF = "\r\n";
    Thread thread;
    Socket socket;

    public ShttpdController(Socket socket){
        this.socket = socket;
        thread = new Thread(this);
    }
    public void start(){
        thread.start();
    }
    String getFileList(Path path){
        StringBuilder builder = new StringBuilder();

        try(Stream<Path> stream = Files.list(path)){
            stream.filter(file -> !Files.isDirectory(file))
                    .map(Path::getFileName)
                    .forEach(p -> builder.append(p.toString()).append(CRLF));
        } catch(IOException e){
            System.err.println(e.getMessage());
        }
        return builder.toString();
    }
    String getFile(Path path){
        StringBuilder builder = new StringBuilder();
        try(Stream<String> lines = Files.lines(path)){
            lines.forEach(x -> builder.append(x).append(CRLF));
        }catch(IOException e){
            System.err.println(e.getMessage());
        }
        return builder.toString();
    }
    public Response process(Request request){
        try {
            if (request.getMethod().equals("GET")) {
                Path relativePath = Paths.get("." + request.getPath());

                Response response = new Response(request.getVersion(), 200, "OK");
                StringBuilder contentType = new StringBuilder();
                contentType.append("text");
                if (Files.isDirectory(relativePath)) {
                    contentType.append("; charset=utf-8");

                    response.setBody(getFileList(relativePath).getBytes(StandardCharsets.UTF_8));
                } else if (Files.isRegularFile(relativePath)) {
                    String filename = relativePath.getFileName().toString();
                    if (filename.contains(".")) {
                        throw new IllegalArgumentException();
                    }

                    contentType.append("/")
                            .append(filename.substring(filename.lastIndexOf(".") + 1))
                            .append("; charset=utf-8");

                    response.setBody(getFile(relativePath).getBytes(StandardCharsets.UTF_8));
                }
                response.addField("content-type", contentType.toString());

                return response;
            }

            throw new IllegalArgumentException("invalid status exception");
        } catch (IllegalArgumentException e) {
            return new Response(request.getVersion(), 400);
        }

    }

    @Override
    public void run(){
        log.trace("Start Thread", thread.getId());
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {
            while (!Thread.currentThread().isInterrupted()) {
                String requestLine = in.readLine();
                if(requestLine == null){
                    break;
                }
                String[] fields = requestLine.split("\\s",3);
                if(fields.length != 3)
                    throw new IllegalArgumentException("invalid http request");

                Request request = new Request(fields[0], fields[1], fields[2]);

                String fieldLine;
                while((fieldLine=in.readLine())!=null){
                    if(fieldLine.length() == 0){
                        break;
                    }
                    request.addFeild(fieldLine);
                }

                if(request.hasField(Request.FIELD_CONTENT_LENGTH)){
                    char[] buffer = new char[request.getContentLength()];

                    int bodyLength = in.read(buffer, 0, request.getContentLength());
                    if(bodyLength == request.getContentLength()){
                        request.setBody(buffer);
                    }
                }

                Response response = process(request);
                log.trace("{}",response);
                out.write(response.getBytes());
                out.flush();
            }
        } catch (IOException e){
            System.err.println(e.getMessage());
        }
        log.trace("End Thread",thread.getId());
    }
}
