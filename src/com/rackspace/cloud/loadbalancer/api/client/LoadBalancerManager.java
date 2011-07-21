/**
 * 
 */
package com.rackspace.cloud.loadbalancer.api.client;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.protocol.RequestExpectContinue;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.util.Log;

import com.rackspace.cloud.files.api.client.CustomHttpClient;
import com.rackspace.cloud.loadbalancer.api.client.http.LoadBalancersException;
import com.rackspace.cloud.loadbalancer.api.parsers.CloudLoadBalancersFaultXMLParser;
import com.rackspace.cloud.loadbalancer.api.parsers.LoadBalancersXmlParser;
import com.rackspace.cloud.servers.api.client.Account;
import com.rackspace.cloud.servers.api.client.CloudServersException;
import com.rackspace.cloud.servers.api.client.http.HttpBundle;

public class LoadBalancerManager extends EntityManager {
	private Context context;

	public LoadBalancerManager(Context context) {
		this.context = context;
	}

	public LoadBalancer getLoadBalancerById(long id) throws LoadBalancersException {
		LoadBalancer loadBalancer = null;
		//First try DFW
		try{
			loadBalancer = getLoadBalancerById(id, Account.getAccount().getLoadBalancerDFWUrl());
			loadBalancer.setRegion("DFW");
		} catch(LoadBalancersException lbe){
			//Didn't work

		}

		//Then try ORD
		if(loadBalancer == null){
			try{
				loadBalancer = getLoadBalancerById(id, Account.getAccount().getLoadBalancerORDUrl());
				loadBalancer.setRegion("ORD");
			}
			catch(LoadBalancersException lbe){
				throw lbe;
			}
		}
		return loadBalancer;
	}

	private LoadBalancer getLoadBalancerById(long id, String url) throws LoadBalancersException {
		CustomHttpClient httpclient = new CustomHttpClient(context);
		HttpGet get = new HttpGet(url + Account.getAccount().getAccountId() + "/loadbalancers/" + id);
		LoadBalancer loadBalancer = new LoadBalancer();

		get.addHeader("X-Auth-Token", Account.getAccount().getAuthToken());
		get.addHeader("Accept", "application/xml");

		try {			
			HttpResponse resp = httpclient.execute(get);		    
			BasicResponseHandler responseHandler = new BasicResponseHandler();
			String body = responseHandler.handleResponse(resp);
			
			Log.d("info", "the xml body is " + body);
			
			if (resp.getStatusLine().getStatusCode() == 200 || resp.getStatusLine().getStatusCode() == 202) {		    	
				LoadBalancersXmlParser loadBalancersXMLParser = new LoadBalancersXmlParser();
				SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
				XMLReader xmlReader = saxParser.getXMLReader();
				xmlReader.setContentHandler(loadBalancersXMLParser);
				xmlReader.parse(new InputSource(new StringReader(body)));		    	
				loadBalancer = loadBalancersXMLParser.getLoadBalancer();		    	
			} else {
				CloudLoadBalancersFaultXMLParser parser = new CloudLoadBalancersFaultXMLParser();
				SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
				XMLReader xmlReader = saxParser.getXMLReader();
				xmlReader.setContentHandler(parser);
				xmlReader.parse(new InputSource(new StringReader(body)));		    	
				LoadBalancersException cse = parser.getException();		    	
				throw cse;
			}
		} catch (ClientProtocolException e) {
			LoadBalancersException cse = new LoadBalancersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (IOException e) {
			LoadBalancersException cse = new LoadBalancersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (ParserConfigurationException e) {
			LoadBalancersException cse = new LoadBalancersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (SAXException e) {
			LoadBalancersException cse = new LoadBalancersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (FactoryConfigurationError e) {
			LoadBalancersException cse = new LoadBalancersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		}
		return loadBalancer;
	}

	public ArrayList<LoadBalancer> createList() throws LoadBalancersException{
		ArrayList<LoadBalancer> loadBalancers = createSublist(Account.getAccount().getLoadBalancerORDUrl());
		for(LoadBalancer loadBalancer: loadBalancers){
			loadBalancer.setRegion("ORD");
		}
		ArrayList<LoadBalancer> DFWloadBalancers = createSublist(Account.getAccount().getLoadBalancerDFWUrl());
		for(LoadBalancer loadBalancer: DFWloadBalancers){
			loadBalancer.setRegion("DFW");
		}
		loadBalancers.addAll(DFWloadBalancers);
		return loadBalancers;
	}

	public ArrayList<LoadBalancer> createSublist(String regionUrl) throws LoadBalancersException {
		//TODO:grab from ord and combine list
		CustomHttpClient httpclient = new CustomHttpClient(context);
		//TODO:check for uk or us
		HttpGet get = new HttpGet(regionUrl + Account.getAccount().getAccountId() + "/loadbalancers");
		ArrayList<LoadBalancer> loadBalancers = new ArrayList<LoadBalancer>();

		get.addHeader("X-Auth-Token", Account.getAccount().getAuthToken());
		get.addHeader("Accept", "application/xml");

		try {			
			HttpResponse resp = httpclient.execute(get);		    
			BasicResponseHandler responseHandler = new BasicResponseHandler();
			String body = responseHandler.handleResponse(resp);
			if (resp.getStatusLine().getStatusCode() == 200 || resp.getStatusLine().getStatusCode() == 202) {		    	
				LoadBalancersXmlParser loadBalancersXMLParser = new LoadBalancersXmlParser();
				SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
				XMLReader xmlReader = saxParser.getXMLReader();
				xmlReader.setContentHandler(loadBalancersXMLParser);
				xmlReader.parse(new InputSource(new StringReader(body)));		    	
				loadBalancers = loadBalancersXMLParser.getLoadBalancers();		    	
			} else {
				CloudLoadBalancersFaultXMLParser parser = new CloudLoadBalancersFaultXMLParser();
				SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
				XMLReader xmlReader = saxParser.getXMLReader();
				xmlReader.setContentHandler(parser);
				xmlReader.parse(new InputSource(new StringReader(body)));		    	
				LoadBalancersException cse = parser.getException();		    	
				throw cse;
			}
		} catch (ClientProtocolException e) {
			LoadBalancersException cse = new LoadBalancersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (IOException e) {
			LoadBalancersException cse = new LoadBalancersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (ParserConfigurationException e) {
			LoadBalancersException cse = new LoadBalancersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (SAXException e) {
			LoadBalancersException cse = new LoadBalancersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (FactoryConfigurationError e) {
			LoadBalancersException cse = new LoadBalancersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		}
		return loadBalancers;
	}

	public HttpBundle create(LoadBalancer entity, String regionUrl) throws CloudServersException {
		HttpResponse resp = null;
		CustomHttpClient httpclient = new CustomHttpClient(context);

		HttpPost post = new HttpPost(regionUrl + Account.getAccount().getAccountId() + "/loadbalancers");
		post.addHeader("Content-Type", "application/xml");

		StringEntity tmp = null;
		try {
			tmp = new StringEntity(entity.toDetailedXML());
		} catch (UnsupportedEncodingException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		}

		post.setEntity(tmp);

		post.addHeader("X-Auth-Token", Account.getAccount().getAuthToken());
		httpclient.removeRequestInterceptorByClass(RequestExpectContinue.class);

		HttpBundle bundle = new HttpBundle();
		bundle.setCurlRequest(post);
		try {
			resp = httpclient.execute(post);
			bundle.setHttpResponse(resp);
		} catch (ClientProtocolException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (IOException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (FactoryConfigurationError e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		}
		return bundle;
	}

	public HttpBundle delete(LoadBalancer loadBalancer) throws CloudServersException {
		HttpResponse resp = null;
		CustomHttpClient httpclient = new CustomHttpClient(context);

		HttpDelete delete = new HttpDelete(LoadBalancer.getRegionUrl(loadBalancer.getRegion()) + Account.getAccount().getAccountId() 
				+ "/loadbalancers/" + loadBalancer.getId());				
		delete.addHeader("X-Auth-Token", Account.getAccount().getAuthToken());
		delete.addHeader("Content-Type", "application/xml");
		httpclient.removeRequestInterceptorByClass(RequestExpectContinue.class);

		HttpBundle bundle = new HttpBundle();
		bundle.setCurlRequest(delete);

		try {			
			resp = httpclient.execute(delete);
			bundle.setHttpResponse(resp);
		} catch (ClientProtocolException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (IOException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (FactoryConfigurationError e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		}	
		return bundle;
	}

	public HttpBundle update(LoadBalancer loadBalancer, String name, String algorithm, String protocol, String port) throws CloudServersException {
		HttpResponse resp = null;
		CustomHttpClient httpclient = new CustomHttpClient(context);

		HttpPut put = new HttpPut(LoadBalancer.getRegionUrl(loadBalancer.getRegion()) + Account.getAccount().getAccountId() + "/loadbalancers/" + loadBalancer.getId());				

		put.addHeader("X-Auth-Token", Account.getAccount().getAuthToken());
		put.addHeader("Content-Type", "application/xml");

		String xml = "<loadBalancer xmlns=\"http://docs.openstack.org/loadbalancers/api/v1.0\" " + 
		"name=\"" + name + "\" " + 
		"algorithm=\"" + algorithm.toUpperCase() + "\" " + 
		"protocol=\"" + protocol.toUpperCase() + "\" " + 
		"port=\"" + port + "\" />";

		StringEntity tmp = null;
		try {
			tmp = new StringEntity(xml);
		} catch (UnsupportedEncodingException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		}

		put.setEntity(tmp);
		httpclient.removeRequestInterceptorByClass(RequestExpectContinue.class);

		HttpBundle bundle = new HttpBundle();
		bundle.setCurlRequest(put);

		try {			
			resp = httpclient.execute(put);
			bundle.setHttpResponse(resp);
		} catch (ClientProtocolException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (IOException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (FactoryConfigurationError e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		}	
		return bundle;
	}
	
	
}
