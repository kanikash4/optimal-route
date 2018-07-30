package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;

public class validate {
    public static String validateAndReturn(JsonNode json, String param, String msg) {
       if(!json.get(param).isNull())  {
           return json.get(param).asText().trim();
       }
        return msg;
    }
}
