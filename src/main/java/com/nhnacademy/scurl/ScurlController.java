// package com.nhnacademy.scurl;

// import java.io.BufferedReader;
// import java.io.BufferedWriter;
// import java.io.File;
// import java.io.FileInputStream;
// import java.io.IOException;
// import java.io.InputStreamReader;
// import java.io.OutputStream;
// import java.io.OutputStreamWriter;
// import java.net.Socket;

// import com.nhnacademy.domain.Request;

// import lombok.Getter;
// import lombok.Setter;

// @Getter@Setter
// public class Scurl {
//     private String host;
//     private int port;
//     private String url;
//     private String method;
//     private boolean showHeader =false;
//     private String postBody;
//     private boolean hasRedirect = false;


//     private String customHeader;
//     private String filePath;
    
//     public Scurl(String host, int port) {
//         this.host = host;
//         this.port = port;
//     }
    

//     @Override
//     public String toString() {
//         return "Scurl [host=" + host + ", port=" + port + ", url=" + url + ", method=" + method + ", showHeader="
//                 + showHeader + ", postBody=" + postBody + ", customHeader=" + customHeader + ", filePath=" + filePath + "]";
//     }
//     public void requestRedirect(){
//         String targetUrl = "/status/302";
//         while(true){
//             try(Socket socket = new Socket(getHost(), getPort())){
//                 BufferedWriter print = new BufferedWriter(new OutputStreamWriter(System.out));
//                 BufferedReader from = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                 BufferedWriter to = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

//                 Request request = new Request();
//                 request.setMethod("GET");
//                 request.setUrl(targetUrl);
//                 request.setHost(getHost());
//                 request.setUserAgent("scurl/0.0.1");
//                 request.setAccept("*/*");
//                 request.setConnection("close");

//                 if(isShowHeader()){
//                     StringBuilder requestBuilder = new StringBuilder();
//                     String[] words = request.generateRequest().split("\r\n");
//                     for(String s:words){
//                         requestBuilder.append("> ").append(s).append("\n");
//                     }
//                     requestBuilder.append("> \n");
//                     print.write(requestBuilder.toString());
//                     print.flush();
//                 }
    
//                 to.write(request.generateRequest());
//                 to.flush();

//                 String line;
//                 StringBuilder response = new StringBuilder();
//                 boolean redirected = false;
//                 while ((line = from.readLine()) != null) {
//                     if (line.startsWith("location:") || line.startsWith("Location:")) {
//                         String[] splitUrl = line.split(":");
//                         String redirectUrl = splitUrl[1].trim();
//                         targetUrl = redirectUrl;
//                         redirected = true;
//                     } 
//                     if(line.isEmpty()){
//                         response.append("< \n");
//                         break;
//                     }  
//                     if(isShowHeader()){
//                         response.append("< ").append(line).append("\n");
//                     }
//                 }
                
                
//                 while ((line = from.readLine()) != null) {
//                     response.append(line).append("\n");
//                 }


//                 print.write(response.toString());
//                 print.flush();
//                 to.flush();


//                 if (!redirected) {
//                     System.out.println("?");
//                     break;
//                 }

//                 to.close();
//                 //print.close(); --> 이거 넣으면 프로그램이 죽음
//                 from.close();
            
//             } catch (IOException e){
//                 System.out.println(e.getMessage());
//             }
//         }
//     }



//     public void requestHttp(){
//         try(Socket socket = new Socket(getHost(), getPort())){
//             BufferedWriter print = new BufferedWriter(new OutputStreamWriter(System.out));
//             BufferedReader from = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//             BufferedWriter to = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

//             Request request = new Request();
//             request.setMethod(getMethod());
//             request.setUrl(getUrl());
//             request.setHost(getHost());
//             request.setUserAgent("scurl/0.0.1");
//             request.setConnection("close");
//             request.setAccept("*/*");
//             if(getCustomHeader() != null){
//                 request.setCustomHeader(getCustomHeader());
//             }
//             if(getPostBody() != null){
//                 request.setData(getPostBody());
//                 request.setDataLength();
//             }

//             if(isShowHeader()){
//                 StringBuilder requestBuilder = new StringBuilder();
//                 String[] words = request.generateRequest().split("\r\n");
//                 for(String s:words){
//                     requestBuilder.append("> ").append(s).append("\n");
//                 }
//                 requestBuilder.append("> \n");
//                 print.write(requestBuilder.toString());
//                 print.flush();
//             }

//             to.write(request.generateRequest());
//             to.flush();

            
//             String line="";
//             StringBuilder response = new StringBuilder();
            
//             while ((line = from.readLine()) != null) {
//                 if(line.isEmpty()){
//                     response.append("< \n");
//                     break;
//                 }  
//                 if(isShowHeader()){
//                     response.append("< ").append(line).append("\n");
//                 }
//             }
            
//             while ((line = from.readLine()) != null) {
//                 response.append(line).append("\n");
//             }

//             print.write(response.toString());
//             print.flush();

//             to.close();
//             print.close();
//             from.close();
            
//         } catch(IOException e){
//             System.out.println(e.getMessage());
//         }

//     }

//     private static long getFileSize(String filePath) {
//         File file = new File(filePath);
//         return file.length();
//     }

//     public void requestPostFile(String filePath){
//         try (Socket socket = new Socket(host, port)) {
//             OutputStream outputStream = socket.getOutputStream();
//             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outputStream));
//             BufferedWriter clientOut = new BufferedWriter(new OutputStreamWriter(System.out));
//             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

//             String requestBody = "--boundary\r\n" +
//                                   "Content-Disposition: form-data; name=\"upload\"; filename=\"test\"\r\n" +
//                                   "Content-Type: application/octet-stream\r\n\r\n";

//             String request = "POST /post HTTP/1.1\r\n" +
//                                 "Host: httpbin.org\r\n" +
//                                 "Content-Type: multipart/form-data; boundary=boundary\r\n" +
//                                 "Connection: close\r\n" +
//                                 "Content-Length: " + (requestBody.length() + 14 + getFileSize("./test.txt")) +"\r\n\r\n";

//             out.write(request);                    
//             out.write(requestBody);
//             out.flush();

//             FileInputStream fileInputStream = new FileInputStream("./test.txt");
//             byte[] buffer = new byte[1024];
//             int bytesRead;
//             while ((bytesRead = fileInputStream.read(buffer)) != -1) {
//                 outputStream.write(buffer, 0, bytesRead);
//             }
//             fileInputStream.close();

//             out.write("\r\n--boundary--\r\n");
//             out.flush();

//             String line;
//             while ((line = in.readLine()) != null) {
//                 System.out.println(line);
//             }

//             out.close();
//             clientOut.close();
//             in.close();

//         } catch (IOException e) {
//             e.printStackTrace();
//         }
//     }
// }
