package com.open.spring.mvc.person.HttpRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;

public class HttpSender{

    public static Map<String,String> sendRequest(String location,String requestMethod, Map<String,String> requestHeaders){
        try {
            
            URL url = new URL(location);

            try{
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                
                connection.setInstanceFollowRedirects(false); //don't follow redirects

                requestHeaders.forEach((header,value)->{
                    connection.addRequestProperty(header, value);
                });


                StringBuffer content = new StringBuffer();
                int responseCode = connection.getResponseCode();

                if(responseCode <= 299){ //if successful

                    //get content from InputStream Stream and fill StringBuffer
                    BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();
                } else {
                     //get content from ErrorStream Stream and fill StringBuffer
                    BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream()));
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();
                }

                connection.disconnect();

                HashMap<String,String> response = new HashMap<String,String>(2);
                response.put("responseCode",String.valueOf(responseCode));
                response.put("content",content.toString());
                return response;

            } catch(IOException e){
                e.printStackTrace();

                HashMap<String,String> response = new HashMap<String,String>(2);
                response.put("responseCode",null);
                response.put("content",e.toString());
                return response;
            }
            
        } catch (MalformedURLException e) {
            e.printStackTrace();

            HashMap<String,String> response = new HashMap<String,String>(2);
            response.put("responseCode",null);
            response.put("content",e.toString());
            return response;
        }
    }


    public static boolean verifyGithub(String uid){
        String location = "https://api.github.com/users/"+uid;
        HashMap<String,String> contentHeaders = new HashMap<String,String>(0);

        Map<String,String> response = HttpSender.sendRequest(location,"GET",contentHeaders);

        //output information message
        System.out.println("GET request to: "+location + " {");

        response.forEach((header,value)->{
            System.out.println("\""+header+"\":\""+value+"\",");
        });

        System.out.println("}");


        //check response
        if(response.get("responseCode").equals("404")){ //github user does not exist
            return false;
        } else { // github user does exist
            return true;
        }
    }


    ///// an example of how this works:
    public static void main(String[] args) {
        //Arguments: location => url of the place
        String location = "https://gutendex.com/books/51155/"; //this is a dictionary
        //method => type of request (GET, POST, ect.)
        String method = "GET";
        //any request headers, you can put a body and content type header here
        HashMap<String,String> requestHeaders = new HashMap<String,String>(0);

        //sends an http to the given location, and returns the result as a map
        //map contains 2 keys: responseCode and Content
        //"reponse" code is a string representation of the reponse code (200, 404, ect)
        //"content" returns any kind of content it finds (usually the reponse content, but also grabs error messages)
        Map<String,String> response = HttpSender.sendRequest(location,method,requestHeaders);

        //output information message
        System.out.println(method+" request to: "+location + " {");
        response.forEach((header,value)->{
            System.out.println("\""+header+"\":\""+value+"\",");
        });
        System.out.println("}");
    }
};