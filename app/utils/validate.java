package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;

public class validate {
    public static String validateAndReturn(JsonNode json, String param, String msg) {
        System.out.println(json.get(param) + "   >>>>> " + json.get(param).asText() == null);
        Preconditions.checkArgument(json.get(param).asText() !=null && !json.get(param).asText().isEmpty(), msg);
        return json.get(param).asText().trim();
    }
}
