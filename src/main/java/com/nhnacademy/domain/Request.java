package com.nhnacademy.domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class Request {
    public static final String DEFAULT_VERSION = "HTTP/1.1";
    public static final String FIELD_CONTENT_LENGTH = "content-length";
    public static final String CRLF = "\r\n";
    
    private String method;
    private String path;
    private String version = DEFAULT_VERSION;
    private char[] body;
    private byte[] fileBody;
    Map<String, String> fieldMap;

    public Request(String method, String path, String version){
        this.method = method;
        this.path = path;
        this.version = version;
        this.fieldMap = new HashMap<>();
    }

    public void addFeild(String line){
        if(line.contains("@")){
            String[] fields = line.split("@",2);
            fieldMap.put(fields[0].trim(), fields[1].trim());
            return;
        }
        String[] fields = line.split(":",2);
        if(fields.length != 2){
            throw new IllegalArgumentException("invalid http request");
        }

        addFelid(fields[0].trim(), fields[1].trim());
    }
    public void addFelid(String key, String value){
        if(key.equalsIgnoreCase(FIELD_CONTENT_LENGTH)){
            Integer.parseInt(value);
        }
        fieldMap.put(key.toLowerCase(),value);
    }
    public String getField(String key){
        return fieldMap.get(key);
    }
    public boolean hasField(String key){
        return fieldMap.containsKey(key);
    }
    public int getContentLength() {
        return Integer.parseInt(getField(FIELD_CONTENT_LENGTH));
    }
    public byte[] getBytes(String filePath) throws IOException{
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%s %s %s%s", getMethod(), getPath(), getVersion(), CRLF));
        fieldMap.forEach((k,v) -> builder.append(String.format("%s: %s%s", k,v,CRLF)));
        builder.append(CRLF);
        builder.append("--boundary\r\n");
        builder.append("Content-Disposition: form-data; name=\"upload\"; filename=\"test\"\r\n");
        builder.append("Content-Type: application/octet-stream\r\n\r\n\r\n");

        String header = builder.toString();
        String footer = "\r\n\r\n--boundary--\r\n";
        
        FileInputStream fileInputStream = new FileInputStream(filePath);
        byte[] buffer = new byte[(int)getFileSize(filePath)];
        fileInputStream.read(buffer,0,(int)getFileSize(filePath));
        byte[] payload = new byte[header.getBytes().length +footer.length()+(int)getFileSize(filePath)];
        System.arraycopy(header.getBytes(), 0, payload, 0, header.getBytes().length);
        System.arraycopy(buffer, 0, payload, header.getBytes().length, buffer.length);
        System.arraycopy(footer.getBytes(),0, payload,header.getBytes().length+buffer.length, footer.length());

        return payload;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(String.format("%s %s %s%s", getMethod(), getPath(), getVersion(), CRLF));
        fieldMap.forEach((k, v) -> builder.append(String.format("%s: %s%s", k, v, CRLF)));
        builder.append(CRLF);

        if(body != null){
            builder.append(getBody());
            builder.append(CRLF);
        }
        if(fileBody != null){
            builder.append(getBody());
            builder.append(CRLF);

        }

        return builder.toString();
    }
    public static long getFileSize(String filePath){
        File file = new File(filePath);
        return file.length();
    }

}
