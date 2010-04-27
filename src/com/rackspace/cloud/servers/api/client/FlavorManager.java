/**
 * 
 */
package com.rackspace.cloud.servers.api.client;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.rackspace.cloud.servers.api.client.parsers.FlavorsXMLParser;

/**
 * @author mike
 *
 */
public class FlavorManager extends EntityManager {

	public ArrayList<Flavor> createList(boolean detail) {
		
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet get = new HttpGet(Account.getServerUrl() + "/flavors/detail.xml?now=cache_time2");
		ArrayList<Flavor> flavors = new ArrayList<Flavor>();
		
		get.addHeader("X-Auth-Token", Account.getAuthToken());
		
		try {			
			HttpResponse resp = httpclient.execute(get);
		    System.out.println(resp.getStatusLine().toString());
		    //System.out.println("body:\n\n" + getResponseBody(resp));
		    
		    BasicResponseHandler responseHandler = new BasicResponseHandler();
		    String body = responseHandler.handleResponse(resp);
		    System.out.println("body:\n\n" + body);
		    
		    if (resp.getStatusLine().getStatusCode() == 200 || resp.getStatusLine().getStatusCode() == 203) {		    	
		    	
		    	FlavorsXMLParser flavorsXMLParser = new FlavorsXMLParser();
		    	SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		    	XMLReader xmlReader = saxParser.getXMLReader();
		    	xmlReader.setContentHandler(flavorsXMLParser);
		    	xmlReader.parse(new InputSource(new StringReader(body)));		    	
		    	flavors = flavorsXMLParser.getFlavors();		    	
		    }
		} catch (ClientProtocolException cpe) {
			// TODO Auto-generated catch block
			cpe.printStackTrace();
			//return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//return false;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return flavors;
	}

}