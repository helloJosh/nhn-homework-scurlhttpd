package com.nhnacademy.scurl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.stream.Stream;

import com.nhnacademy.domain.Request;

import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class Scurl {
    public static final String DEFAULT_VERSION = "HTTP/1.1";
    public static final String CRLF = "\r\n";
    String host;
    int port;
    boolean showHeader;
    boolean isRedirect;
    
    public Scurl(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void request(String method, String path, String version, String customHeader,String data, String filePath){
        try(Socket socket = new Socket(getHost(), getPort())){
            BufferedWriter print = new BufferedWriter(new OutputStreamWriter(System.out));
            BufferedReader from = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter to = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            Request request = new Request(method, path, version);
            request.addFeild("Host: httpbin.org");

            if(method.equals("GET")){
                if(!customHeader.equals("")){
                    request.addFeild(customHeader);
                }


                if(isShowHeader())
                    System.out.println(printHeader(request));
                
                to.write(request.toString());
                to.flush();
                String line="";
                StringBuilder response = new StringBuilder();
                
                while ((line = from.readLine()) != null) {
                    if(line.isEmpty()){
                        response.append("< \n");
                        break;
                    }  
                    if(isShowHeader()){
                        response.append("< ").append(line).append("\n");
                    }
                }
                
                while ((line = from.readLine()) != null) {
                    response.append(line).append("\n");
                    if(line.equals("}"))
                        break;
                }

                System.out.println(response.toString());
            } 
            else if(method.equals("POST")){
                if(!data.equals("")){
                    request.addFeild(customHeader);
                    request.addFeild("content-length: "+data.length());
                    request.setBody(data.toCharArray());
                    if(isShowHeader())
                        System.out.println(printHeader(request));
                    
                    to.write(request.toString());

                    to.flush();
                }
                else if(!filePath.equals("")){
                    String[] s = filePath.split("@");
                    String relativePath = "./"+s[1];
                    request.addFeild("Content-Type: multipart/form-data; boundary=boundary");
                    request.addFeild("Content-Length: " + getFileSize(filePath));
                    request.addFeild("Connection: close");
                    byte[] requestByte = request.getBytes(relativePath);
                    
                    System.out.println(new String(requestByte));

                    if(isShowHeader())
                        System.out.println(printHeader(request));
                    
                    socket.getOutputStream().write(requestByte);
                    socket.getOutputStream().flush();
                }

                String line="";
                StringBuilder response = new StringBuilder();
                
                while ((line = from.readLine()) != null) {
                    if(line.isEmpty()){
                        response.append("< \n");
                        break;
                    }  
                    if(isShowHeader()){
                        response.append("< ").append(line).append("\n");
                    }
                }
                
                while ((line = from.readLine()) != null) {
                    response.append(line).append("\n");
                    if(line.equals("}"))
                        break;
                }

                System.out.println(response.toString());
            }
            else if(method.equals("DELETE")){

            }

            to.close();
            print.close();
            from.close();

        } catch(IOException e){
            System.err.println(e.getMessage());
        }
    }
    public static long getFileSize(String filePath){
        File file = new File(filePath);
        return file.length();
    }
    
    public String printHeader(Request request){
        StringBuilder builder = new StringBuilder();
        builder.append("> ").append(String.format("%s %s %s%s",  request.getMethod(), request.getPath(), request.getVersion(), CRLF));
        request.getFieldMap().forEach((k,v)->builder.append(String.format("> %s: %s%s", k, v, CRLF)));
        builder.append("> ").append(CRLF);
        return builder.toString();
    }
}