package com.nhnacademy.domain;

import java.io.File;
import java.io.FileInputStream;
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
    Map<String, String> fieldMap;

    public Request(String method, String path, String version){
        this.method = method;
        this.path = path;
        this.version = version;
        this.fieldMap = new HashMap<>();
    }

    public void addFeild(String line){
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(String.format("%s %s %s%s", getMethod(), getPath(), getVersion(), CRLF));
        fieldMap.forEach((k, v) -> builder.append(String.format("%s: %s%s", k, v, CRLF)));

        return builder.toString();
    }


}
