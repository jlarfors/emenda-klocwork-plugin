package com.emenda.emendaklocwork.services;

import java.util.*;

public class KlocworkResponse {

    private List<String> responses;
    private boolean success;

	public KlocworkResponse(boolean success) {
        this.responses = new ArrayList<String>();
        this.success = success;
	}

    public void addResponse(String response) {
        responses.add(response);
    }

	public List<String> getResponses() { return responses; }
    public String getResponsesAsString() {
        StringBuilder sb = new StringBuilder();
        for (String s : responses) {
            sb.append(s);
            sb.append("\n");
        }
        return sb.toString();
    }
    public boolean isSuccess() { return success; }
}
