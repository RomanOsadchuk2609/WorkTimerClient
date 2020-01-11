package osadchuk.worktimer.webRequest;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import osadchuk.worktimer.Utils;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class HTTPRequest {

    public static void executeGet(String url, List<NameValuePair> parameters, String JSESSIONID) throws IOException, URISyntaxException {

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        if (parameters != null) {
            URI uri = new URIBuilder(request.getURI()).addParameters(parameters).build();
            request.setURI(uri);
        }
        if (JSESSIONID != null) {
            request.setHeader("Cookie", "JSESSIONID="+ JSESSIONID);
        }
        System.out.println("\n**********************************************************************************");
        Header []header = request.getAllHeaders();
        for (int i = 0; i < header.length; i++) {
            System.out.println(header[i].getName()+": "+header[i].getValue());
        }
        System.out.println("**********************************************************************************");
        HttpResponse response = client.execute(request);
        int responseCode = response.getStatusLine().getStatusCode();
        System.out.println(new Date().toString());
        System.out.println("Sending 'GET' request to URL: " + url);
        System.out.println("Response Code: " + responseCode);
        if (responseCode == 200) {
            System.out.println(EntityUtils.toString(response.getEntity()));
        }
    }

    public static void executeGet(String url, String JSESSIONID) throws IOException, URISyntaxException {

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);

        if (JSESSIONID != null) {
            request.setHeader("Cookie", "JSESSIONID="+ JSESSIONID);
        }
        System.out.println("\n**********************************************************************************");
        Header []header = request.getAllHeaders();
        for (int i = 0; i < header.length; i++) {
            System.out.println(header[i].getName()+": "+header[i].getValue());
        }
        System.out.println("**********************************************************************************");
        HttpResponse response = client.execute(request);
        int responseCode = response.getStatusLine().getStatusCode();
        System.out.println(new Date().toString());
        System.out.println("Sending 'GET' request to URL: " + url);
        System.out.println("Response Code: " + responseCode);
        if (responseCode == 200) {
            System.out.println(EntityUtils.toString(response.getEntity()));
        }
    }

    public static String getResponseFromGet(String url, List<NameValuePair> parameters, String JSESSIONID)throws IOException, URISyntaxException {

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        if (parameters != null) {
            URI uri = new URIBuilder(request.getURI()).addParameters(parameters).build();
            request.setURI(uri);
        }
        if (JSESSIONID != null) {
            request.setHeader("Cookie", "JSESSIONID="+ JSESSIONID);
        }
        System.out.println("\n**********************************************************************************");
        Header []header = request.getAllHeaders();
        for (int i = 0; i < header.length; i++) {
            System.out.println(header[i].getName()+": "+header[i].getValue());
        }
        System.out.println("**********************************************************************************");
        HttpResponse response = client.execute(request);

        int responseCode = response.getStatusLine().getStatusCode();
        System.out.println(new Date().toString());
        System.out.println("Sending 'GET' request to URL: " + url);
        System.out.println("Response Code: " + responseCode);
        if (responseCode == 200) {
            return EntityUtils.toString(response.getEntity());
        }

        else{
            return responseCode+"";
        }
    }

    public static String getResponseFromGet(String url, String JSESSIONID)throws IOException, URISyntaxException {

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        if (JSESSIONID != null) {
            request.setHeader("Cookie", "JSESSIONID="+ JSESSIONID);
        }
        System.out.println("\n**********************************************************************************");
        Header []header = request.getAllHeaders();
        for (int i = 0; i < header.length; i++) {
            System.out.println(header[i].getName()+": "+header[i].getValue());
        }
        System.out.println("**********************************************************************************");

        HttpResponse response = client.execute(request);
        int responseCode = response.getStatusLine().getStatusCode();
        System.out.println(new Date().toString());
        System.out.println("Sending 'GET' request to URL: " + url);
        System.out.println("Response Code: " + responseCode);
        if (responseCode == 200) {
            return EntityUtils.toString(response.getEntity());
        }

        else{
            return responseCode+"";
        }
    }

    public static void executePost(String url, List<NameValuePair> parameters, String JSESSIONID) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(url);
        if (JSESSIONID != null) {
            request.setHeader("Cookie", "JSESSIONID="+ JSESSIONID);
        }
        System.out.println("\n**********************************************************************************");
        Header []header = request.getAllHeaders();
        for (int i = 0; i < header.length; i++) {
            System.out.println(header[i].getName()+": "+header[i].getValue());
        }
        System.out.println(new Date().toString());
        System.out.println("Sending 'POST' request to URL: " + url);
        System.out.println("**********************************************************************************");

        try {
            request.setEntity(new UrlEncodedFormEntity(parameters));
            HttpResponse response = client.execute(request);

            // Print out the response message
            int responseCode = response.getStatusLine().getStatusCode();
           // System.out.println("Sending 'POST' request to URL: " + url);
            System.out.println("Response Code: " + responseCode);
            if (responseCode == 200){
                System.out.println("Output... ");
                System.out.println(EntityUtils.toString(response.getEntity()));
            }
            else{
                System.out.println(response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void executePost(String url, String JSESSIONID) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(url);
        if (JSESSIONID != null) {
            request.setHeader("Cookie", "JSESSIONID="+ JSESSIONID);
        }
        System.out.println("\n**********************************************************************************");
        Header []header = request.getAllHeaders();
        for (int i = 0; i < header.length; i++) {
            System.out.println(header[i].getName()+": "+header[i].getValue());
        }
        System.out.println("**********************************************************************************");

        try {
            HttpResponse response = client.execute(request);
            // Print out the response message
            int responseCode = response.getStatusLine().getStatusCode();
            System.out.println(new Date().toString());
            System.out.println("Sending 'POST' request to URL: " + url);
            System.out.println("Response Code: " + responseCode);
            if (responseCode == 200){
                System.out.println("Output... ");
                System.out.println(EntityUtils.toString(response.getEntity()));
            }
            else{
                System.out.println(response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String getResponseFromPost(String url, String JSESSIONID)throws IOException, URISyntaxException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(url);
        if (JSESSIONID != null) {
            request.setHeader("Cookie", "JSESSIONID="+ JSESSIONID);
        }
        System.out.println("\n**********************************************************************************");
        Header []header = request.getAllHeaders();
        for (int i = 0; i < header.length; i++) {
            System.out.println(header[i].getName()+": "+header[i].getValue());
        }
        System.out.println("**********************************************************************************");

        try {
            HttpResponse response = client.execute(request);
            // Print out the response message
            int responseCode = response.getStatusLine().getStatusCode();
            System.out.println(new Date().toString());
            System.out.println("Sending 'POST' request to URL: " + url);
            System.out.println("Response Code: " + responseCode);
            if (responseCode == 200){
                System.out.println("Output... ");
                return EntityUtils.toString(response.getEntity());
            }
            else{
                System.out.println(response.getStatusLine().getReasonPhrase());
                return responseCode+"";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getResponseFromPost(String url, List<NameValuePair> parameters, String JSESSIONID)throws IOException, URISyntaxException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(url);
        if (JSESSIONID != null) {
            request.setHeader("Cookie", "JSESSIONID="+ JSESSIONID);
        }
        System.out.println("\n**********************************************************************************");
        Header []header = request.getAllHeaders();
        for (int i = 0; i < header.length; i++) {
            System.out.println(header[i].getName()+": "+header[i].getValue());
        }
        System.out.println("**********************************************************************************");

        try {
            request.setEntity(new UrlEncodedFormEntity(parameters));
            HttpResponse response = client.execute(request);
            // Print out the response message
            int responseCode = response.getStatusLine().getStatusCode();
            System.out.println(new Date().toString());
            System.out.println("Sending 'POST' request to URL: " + url);
            System.out.println("Response Code: " + responseCode);
            if (responseCode == 200){
                System.out.println("Output... ");
                return EntityUtils.toString(response.getEntity());

            }
            else{
                System.out.println(response.getStatusLine().getReasonPhrase());
                return responseCode+"";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        //return null;
    }

    public static String login(String url,List<NameValuePair> parameters) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(url);
        try {
            request.setEntity(new UrlEncodedFormEntity(parameters));
            HttpResponse response = client.execute(request);
            int responseCode = response.getStatusLine().getStatusCode();
            System.out.println(new Date().toString());
            System.out.println("Trying to login in: " + url);
            System.out.println("Response Code: " + responseCode);
            if (responseCode == 200){
                System.out.println("Output... ");
                Header[] responseHeaders = response.getHeaders("Set-Cookie");
                List<Header> headerList = Arrays.asList(responseHeaders);
                Header JSID = headerList.stream().filter(h->h.getValue().contains("JSESSIONID")).findFirst().get();
                if (JSID != null) {
                    System.out.println(JSID.getValue());
                    int startIndex, endIndex;
                    startIndex = JSID.getValue().indexOf('=');
                    endIndex = JSID.getValue().indexOf(';');
                    String jsessionid = JSID.getValue().substring(startIndex+1,endIndex);
                    Utils.JSESSIONID = jsessionid;
                }
               return EntityUtils.toString(response.getEntity());

            }
            else if (responseCode == 302){
                return "302";
            }
            else{
                return null;
            }
        } catch (HttpHostConnectException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
