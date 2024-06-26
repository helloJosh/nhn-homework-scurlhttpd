package com.nhnacademy.scurl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.nhnacademy.domain.Request;

public class ScurlMain {
    public static final String DEFAULT_VERSION = "HTTP/1.1";
    public static final String CRLF = "\r\n";
    public static void main(String[] args) {
        
        Options options = new Options();
        options.addOption("X", true, "사용할 method를 지정한다. 지정되지 않은 경우, 기본값은 GET");
        options.addOption("v", false, "verbose, 요청, 응답 헤더를 출력한다.");
        options.addOption("H", true, "임의의 헤더를 서버로 전송한다.");
        options.addOption("d", true, "POST, PUT 등에 데이터를 전송한다.");
        options.addOption("L", false,"서버의 응답이 30x 계열이면 다음 응답을 따라 간다.");
        options.addOption("F",true, "multipart/form-data를 구성하여 전송한다. content 부분에 @filename을 사용할 수 있다" );
        CommandLineParser parser = new DefaultParser();
        try {
            String uri = args[args.length-1];
            String[] s = uri.split("/");
            // String host = s[2];
            // String url = s[3];
            String host = "httpbin.org";
            String path = "/post";
            String method ="GET";
            String customHeader="";
            String body="";
            String upload="";
            

            CommandLine cmd = parser.parse(options, args);
            Map<String, String> argMap = new HashMap<>();

            for(Option option : cmd.getOptions()){
                argMap.put(option.getOpt(), option.getValue());
            }

            Scurl scurl = new Scurl(host, 80);

            if(argMap.containsKey("X")){
                method = argMap.get("X");
            } 
            if(argMap.containsKey("v")){
                scurl.setShowHeader(true);
            }
            if(argMap.containsKey("H")){
                customHeader = argMap.get("H");
            }
            if(argMap.containsKey("L")){
                scurl.setRedirect(true);
            }
            if(argMap.containsKey("d")){
                body = argMap.get("d");
                method = "POST";
            }
            if(argMap.containsKey("F")){
                method = "POST";
                upload = argMap.get("F");
            }
            //scurl.requestPostFile("./test.txt");
            scurl.request(method, path, DEFAULT_VERSION , customHeader, body, upload);

        } catch (ParseException e) {
        System.err.println(e.getMessage());
        }
    }
}
