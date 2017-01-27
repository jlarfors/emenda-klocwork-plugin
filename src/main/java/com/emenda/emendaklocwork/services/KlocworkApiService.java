package com.emenda.emendaklocwork.services;

import java.util.*;
import java.io.*;
import java.util.regex.*;

import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

public class KlocworkApiService {

	// private KlocworkApiConnection apiConnection = null;
    //
	// private String errorMsg = null;
    //
    // private final String MSG_PREFIX = "[KlocworkApiService] - ";
    //
	// public KlocworkApiService(String host, int port, String user, String ltoken) {
    //     apiConnection = new KlocworkApiConnection(host, port, false, user);
    //     apiConnection.setLtoken(ltoken);
	// }
    //
	// /*
	//  * Function to send request to server
	//  * Supports error handling
	//  */
	// public boolean sendRequest(String request) {
    //     boolean success = apiConnection.sendRequest(request);
	// 	if(success) {
    //         errorMsg = "";
	// 	} else {
    //         // error handling
    //         errorMsg = MSG_PREFIX + "Error: Unsuccessful request made.\n" +
    //             "HTTP Return Code: " + Integer.toString(apiConnection.getHTTPReturnCode()) + "\n" +
    //             "Reponse: " + apiConnection.getLastResponse() + "\n" +
    //             "Request: " + apiConnection.getLastRequest() + "\n" +
    //             apiConnection.getErrorMsg() + "\n";
    //     }
	// 	return success;
	// }
    //
    // public String getLastResponse() { return apiConnection.getLastResponse(); }
    // public JSONArray getJsonResponses() { return apiConnection.getJsonResponses(); }
    //
	// /*
	//  * Function to retrieve error message
	//  */
	// public String getErrorMsg() {
	// 	return errorMsg;
	// }
}
