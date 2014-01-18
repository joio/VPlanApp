package com.inf1315.vertretungsplan.api;

import java.io.*;
import java.net.*;
import java.util.*;

import org.json.*;

import android.util.Log;

public class API
{
    /**
     * The standard url: {@value #STANDARD_URL}
     */
    public static final String STANDARD_URL = "http://skirising.no-ip.org/VPlanApp/api/";
    
    public static API STANDARD_API;
    public static API LOCAL_DEBUG_API;
    
    private URL url;
    
    /**
     * Creates a new API with the {@link #STANDARD_URL} ({@value #STANDARD_URL})
     * @throws MalformedURLException
     * @see API#API(String)
     * @see API#API(URL)
     */
    public API() throws MalformedURLException
    {
	this(STANDARD_URL);
    }

    /**
     * Creates a new API with for the specified URL
     * @param url The URL
     * @throws MalformedURLException
     * @see API#API()
     * @see API#API(URL)
     */
    public API(String url) throws MalformedURLException
    {
	this(new URL(url));
    }

    /**
     * Creates a new API with for the specified URL
     * @param url The URL
     * @throws MalformedURLException
     * @see API#API()
     * @see API#API(String)
     */
    public API(URL url)
    {
	this.url = url;
    }
    
    /**
     * Make a new API-request
     * @param action The action to be performed
     * @param params The parameters to the action as strings with format <i>"key=value"</i>
     * @return The Response from the server
     * @throws IOException
     * @see {@link API#request(ApiAction, Map)}
     */
    public ApiResponse request(ApiAction action, String...params) throws IOException
    {
	Map<String, String> paramsMap = new HashMap<String, String>();
	for(String param : params)
	{
	    String[] sp = param.split("=");
	    if(sp.length != 2)
		throw new IllegalArgumentException("Params need to be key=value");
	    paramsMap.put(sp[0], sp[1]);
	}
	return request(action, paramsMap);
    }
    

    /**
     * Make a new API-request
     * @param action The action to be performed
     * @param params The parameters to the action
     * @return The Response from the server
     * @throws IOException
     * @see {@link API#request(ApiAction, String...)}
     */
    public ApiResponse request(ApiAction action, Map<String, String> params)
    {
	ApiResponse r;
	JSONObject obj = null;
	try
	{
	    params.put("a", action.toString().toLowerCase());
	    obj = getJSONfromURL(url, "GET", params);
	    if(!actionToClassMap.containsKey(action))
		throw new RuntimeException("invalid action \"" + action + "\"");
	    r = new ApiResponse(obj, actionToClassMap.get(action), actionIsArrayMap.get(action));
	} catch (Exception e)
	{
	    e.printStackTrace();
	    Log.i("API Params", params.toString());
	    if(obj != null)
		try
		{
		    Log.i("API Response", obj.toString(4));
		} catch (JSONException e1)
		{
		}
	    r = new ApiResponse(e);
	}
	return r;
    }
    
    /**
     * Method for getting a JSONObject from an url
     * @param url The HTTP-URL
     * @param requestMethod The HTTP request method (GET/POST)
     * @param params Additional params for GET/POST
     * @return A JSONObject parsed from the specified url
     */
    public static JSONObject getJSONfromURL(URL url, String requestMethod, Map<String,String> params) throws IOException, JSONException
    {
	requestMethod = requestMethod.toUpperCase();
	StringBuilder get = new StringBuilder();
	if(params != null)
	{
	    for (String key : params.keySet())
		get.append(get.length() == 0 ? "" : "&").
		append(URLEncoder.encode(key, "UTF-8")).
		append('=').
		append(URLEncoder.encode(params.get(key), "UTF-8"));
	}
	if(requestMethod.equals("GET"))
	    url = new URL(url.toExternalForm() + "?" + get);
	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	conn.setRequestMethod(requestMethod);
	conn.connect();
	if(requestMethod.equals("POST"))
	    conn.getOutputStream().write(get.toString().getBytes());
	BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	StringBuilder sb = new StringBuilder();
	String line;
	while ((line = reader.readLine()) != null)
	    sb.append(line).append('\n');
	reader.close();
	conn.disconnect();
	return new JSONObject(sb.toString());
    }
    
    private static Map<ApiAction, Class<? extends ApiResult>> actionToClassMap;
    private static Map<ApiAction, Boolean> actionIsArrayMap;
    
    static
    {
	try
	{
	    STANDARD_API = new API();
	    LOCAL_DEBUG_API = new API("http://192.168.0.36/VPlanApp/api.php");
	} catch (MalformedURLException e){}
	
	actionToClassMap = new HashMap<ApiAction, Class<? extends ApiResult>>();
	actionIsArrayMap = new HashMap<ApiAction, Boolean>();
	
	actionToClassMap.put(ApiAction.USER, UserInfo.class);
	actionIsArrayMap.put(ApiAction.USER, false);
	
	actionToClassMap.put(ApiAction.PLAN, PlanObject.class);
	actionIsArrayMap.put(ApiAction.PLAN, false);
	
	actionToClassMap.put(ApiAction.TICKER, TickerObject.class);
	actionIsArrayMap.put(ApiAction.TICKER, true);
    }
}
