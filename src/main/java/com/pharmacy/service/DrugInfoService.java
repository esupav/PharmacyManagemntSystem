package com.pharmacy.service;

import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class DrugInfoService {

    private static final String API_BASE_URL = "https://api.fda.gov/drug/label.json";
    private static final String API_KEY = "F4hvzNyS18CMz6gi8yik8uR6JyIdF2JjBD5Xcnwg"; // Your new API key

    /**
     * Asynchronously fetches information about a specific drug.
     *
     * @param drugName The name of the drug to search for.
     * @return A CompletableFuture that will contain the JSON response as a String.
     */
    public CompletableFuture<String> findDrugInfo(String drugName) {
        HttpClient client = HttpClient.newHttpClient();
        
        // Construct the request URI with the search query and API key
        String searchUri = API_BASE_URL + "?search=openfda.brand_name:\"" + drugName + "\"&api_key=" + API_KEY;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(searchUri))
                .header("Accept", "application/json")
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    /**
     * Asynchronously fetches the description for a specific drug.
     *
     * @param drugName The name of the drug.
     * @return A CompletableFuture that will contain the drug's description.
     */
    public CompletableFuture<String> getDrugDescription(String drugName) {
        return findDrugInfo(drugName)
                .thenApply(jsonResponse -> parseFieldFromJson(jsonResponse, "description"));
    }

    /**
     * A simple parser for the JSON response to extract a specific field.
     * This is a basic example; a real application would use a more robust JSON parsing library.
     *
     * @param jsonResponse The JSON string returned from the API.
     * @param fieldName The name of the field to extract from the first result.
     * @return The value of the field, or "Not Found" if it doesn't exist.
     */
    public String parseFieldFromJson(String jsonResponse, String fieldName) {
        try {
            JSONObject root = new JSONObject(jsonResponse);
            if (root.has("results") && root.getJSONArray("results").length() > 0) {
                JSONObject firstResult = root.getJSONArray("results").getJSONObject(0);
                if (firstResult.has(fieldName)) {
                    return firstResult.getJSONArray(fieldName).getString(0);
                }
            }
        } catch (Exception e) {
            // In a real app, you'd want more specific error handling
            e.printStackTrace();
        }
        return "Not Found";
    }
}
