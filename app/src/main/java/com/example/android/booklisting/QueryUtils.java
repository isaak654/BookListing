package com.example.android.booklisting;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


/**
 * Helper methods related to requesting and receiving books data from Google Books.
 */
public final class QueryUtils {

    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Query the Google Books dataset and return a list of {@link Book} objects.
     */
    public static List<Book> fetchBookData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Return the list of {@link Book}s
        return extractBookFromJson(jsonResponse);
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        int connectionReadTimeout = 10000; /* milliseconds */
        int connectionTimeout = 15000; /* milliseconds */

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(connectionReadTimeout);
            urlConnection.setConnectTimeout(connectionTimeout);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link Book} objects that has been built up from
     * parsing the given JSON response.
     */
    private static List<Book> extractBookFromJson(String bookJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(bookJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding books to
        List<Book> books = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(bookJSON);

            // Extract the JSONArray associated with the key called "items",
            // which represents a list of items (or books).
            JSONArray bookArray = baseJsonResponse.getJSONArray("items");

            //For each book in the bookArray, create an {@link Book} object
            for (int i = 0; i < bookArray.length(); i++) {
                //Get a single book and position it within the list of books
                JSONObject currentBook = bookArray.getJSONObject(i);
                JSONObject volumeInfo = currentBook.getJSONObject("volumeInfo");
                // Extract the value for the key called "title"
                String title = volumeInfo.getString("title");
                // Extract the value for the key called "previewLink"
                String url = volumeInfo.getString("previewLink");
                // Extract the value for the key called "authors" if it exists
                String author = "";
                if (volumeInfo.has("authors")) {
                    try {
                        JSONArray authorsArray = volumeInfo.getJSONArray("authors");

                        if (authorsArray.length() > 1) {

                            for (int j = 0; j < authorsArray.length(); j++) {

                                try {
                                    author += volumeInfo.getJSONArray("authors").get(j).toString() + ", ";
                                } catch (JSONException e) {
                                    // If an error is thrown when executing any of the above statements in the "try" block,
                                    // catch the exception here, so the app doesn't crash. Print a log message
                                    // with the message from the exception.
                                    Log.e("QueryUtils", "Problem parsing many authors", e);
                                }
                            }
                        } else {
                            try {
                                author = volumeInfo.getJSONArray("authors").get(0).toString();
                            } catch (JSONException e) {
                                // If an error is thrown when executing any of the above statements in the "try" block,
                                // catch the exception here, so the app doesn't crash. Print a log message
                                // with the message from the exception.
                                Log.e("QueryUtils", "Problem parsing one author", e);

                            }
                        }

                    } catch (JSONException e) {
                        // If an error is thrown when executing any of the above statements in the "try" block,
                        // catch the exception here, so the app doesn't crash. Print a log message
                        // with the message from the exception.
                        Log.e("QueryUtils", "Problem parsing authors", e);
                    }
                } else {
                    author = "Unknown author";
                }

                // Extract the value for the key called "publishedDate" if it exists
                String date;
                if (volumeInfo.has("publishedDate")) {
                    date = volumeInfo.optString("publishedDate");
                } else {
                    date = "No date";
                }

                // Create a new {@link Book} object with the title, author, date,
                // and url from the JSON response.
                Book book = new Book(title, author, url, date);
                books.add(book);
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the JSON results", e);
        }

        // Return the list of books
        return books;
    }
}