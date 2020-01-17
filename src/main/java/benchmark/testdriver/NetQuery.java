package benchmark.testdriver;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.io.*;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.auth.DigestSchemeFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


public class NetQuery {
  
  static Map<String, CloseableHttpClient> clientcache = Maps.newHashMap();
  
  CloseableHttpClient httpclient;
  HttpRequestBase request;
	Long start;
	Long end;
	String queryString;
	String username;
	String password;
	
	protected NetQuery(String serviceURL, String query, byte queryType, String defaultGraph, int timeout, String userpass) {
	  
		HttpClientBuilder builder = HttpClients.custom().setConnectionTimeToLive(timeout, TimeUnit.SECONDS);  
	  
  	  if(userpass!=null){
  	    
  	    username = userpass.split(":")[0];
        password = userpass.split(":")[1]; 
        
        
  	    if(clientcache.containsKey(username)){
  	      httpclient  = clientcache.get(username);
  	    }else{
  	      
  	      CredentialsProvider prov = new BasicCredentialsProvider();
          prov.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username,password));
    
          httpclient =  builder.setDefaultCredentialsProvider(prov).build();
         
  	    }

      }else{
        httpclient = builder.build();
      }
  	  
  
	  
		String urlString = null;
		try {
			queryString = query;
			char delim=serviceURL.indexOf('?')==-1?'?':'&';
			if(queryType==Query.UPDATE_TYPE)
				urlString = serviceURL;
			else {
				urlString = serviceURL + delim + "query=" + URLEncoder.encode(query, "UTF-8");
				delim = '&';
	                        if(defaultGraph!=null)
	                                urlString +=  delim + "default-graph-uri=" + defaultGraph;
			}
			
			 if(queryType==Query.UPDATE_TYPE){
			   request = new HttpPost(urlString);
			 } else {
			   request = new HttpGet(urlString);
			 }


			 if(queryType==Query.DESCRIBE_TYPE || queryType==Query.CONSTRUCT_TYPE){
			   request.addHeader(new BasicHeader("Accept", "application/rdf+xml"));
			 }else{
			   request.addHeader(new BasicHeader("Accept", "application/sparql-results+xml"));
			 } 
		   if(queryType==Query.UPDATE_TYPE) {
		      HttpPost post = new HttpPost(urlString);
		      post.addHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
		      
		      ByteArrayOutputStream out = new ByteArrayOutputStream();
		      String queryParamName = TestDriver.sparqlUpdateQueryParameter + "="; 
		      out.write(queryParamName.getBytes());
		      out.write(URLEncoder.encode(query, "UTF-8").getBytes());
		      if(defaultGraph!=null) {
		        out.write("&default-graph-uri=".getBytes());
		        out.write(defaultGraph.getBytes());
		      }
		      
		      post.setEntity(new ByteArrayEntity(out.toByteArray()));
		      out.close();
		    }
			
		
			
		} catch(UnsupportedEncodingException e) {
			System.err.println(e.toString());
			e.printStackTrace();
			System.exit(-1);
		} catch(MalformedURLException e) {
			System.err.println(e.toString() + " for URL: " + urlString);
			System.err.println(serviceURL);
			e.printStackTrace();
			System.exit(-1);
		} catch(IOException e) {
			System.err.println(e.toString());
			e.printStackTrace();
			System.exit(-1);
		}
	}


	protected InputStream exec() {
	  CloseableHttpResponse response = null;
		try {
		  start = System.nanoTime();
			response =  httpclient.execute(request);

		} catch(IOException e) {
			System.err.println("Could not connect to SPARQL Service.");
			e.printStackTrace();
			System.exit(-1);
		}
		try {
			int rc = response.getStatusLine().getStatusCode();
			if(rc < 200 || rc >= 300) {
				System.err.println("Query execution: Received error code " + rc + " from server");
				System.err.println("Error message: " + response.getStatusLine().getReasonPhrase() + "\n\nFor query: \n");
				System.err.println(queryString + "\n");
			}
			return response.getEntity().getContent();
		} catch(SocketTimeoutException e) {
			return null;
		} catch(IOException e) {
			System.err.println("Query execution error:");
			e.printStackTrace();
			System.exit(-1);
			return null;
		}

	}
	
	protected double getExecutionTimeInSeconds() {
		end = System.nanoTime();
		Long interval = end-start;
		Thread.yield();
		return interval.doubleValue()/1000000000;
	}
	
	protected void close() {
		request.abort();
		try {
      httpclient.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
		request = null;
		httpclient = null;
	}
}
