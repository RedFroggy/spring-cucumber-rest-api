package fr.redfroggy.bdd.scope;

import java.util.HashMap;
import java.util.Map;

/**
 * Scenario scope to store data between steps
 */
public class ScenarioScope {

    //Store http headers
    private Map<String,Object> headers;

    //Store json paths
    private Map<String,Object> jsonPaths;

    public ScenarioScope() {
        headers = new HashMap<>();
        jsonPaths = new HashMap<>();
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public Map<String, Object> getJsonPaths() {
        return jsonPaths;
    }
}
