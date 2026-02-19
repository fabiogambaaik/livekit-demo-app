package com.geg.livekitstreamingdemo.manager;

import android.content.Context;
import android.util.Log;

import com.geg.livekitstreamingdemo.utils.AppConstants;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TokenRequester {

    private static final String ENDPOINT = "https://cloud-api.livekit.io/api/sandbox/connection-details";

    private static final String SANDBOX_ID = "ultra-topology-13lkd8"; // SET SANDBOX_ID HERE

    private LiveKitManager liveKitManager;

    public TokenRequester(Context context) {
        liveKitManager = new LiveKitManager(context);
    }

    public static class TokenResponse {
        public String serverUrl;
        public String participantToken;
        public String roomName;
        public String participantName;

        @Override
        public String toString() {
            return "TokenResponse{" +
                    "serverUrl='" + serverUrl + '\'' +
                    ", participantToken='" + participantToken + '\'' +
                    ", roomName='" + roomName + '\'' +
                    ", participantName='" + participantName + '\'' +
                    '}';
        }
    }

    // request tokens for Room 1001-1006
    public void requestTokens() {
        new Thread(() -> {
            for (int room = 1001; room <= 1006; room++) {
                String roomName = String.valueOf(room);
                try {
                    TokenResponse response = sendRequest(roomName);
                    liveKitManager.launchConnectToRoom(response);

                } catch (Exception e) {
                    Log.e(AppConstants.LOGS.LOG_TAG, "Error while requesting token: ", e);
                }
            }
        }).start();
    }

    // send token request
    private TokenResponse sendRequest(String roomName) throws Exception {

        URL url = new URL(ENDPOINT);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("X-Sandbox-ID", SANDBOX_ID);
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        // JSON Body
        JSONObject body = new JSONObject();
        body.put("room_name", roomName);
        body.put("participant_name", "app-demo");

        OutputStream os = conn.getOutputStream();
        os.write(body.toString().getBytes("UTF-8"));
        os.close();

        int responseCode = conn.getResponseCode();

        InputStream is;
        if (responseCode >= 200 && responseCode < 300) {
            is = conn.getInputStream();
        } else {
            is = conn.getErrorStream();
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder responseBuilder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            responseBuilder.append(line);
        }

        reader.close();
        conn.disconnect();

        String jsonResponse = responseBuilder.toString();
        Log.d(AppConstants.LOGS.LOG_TAG, "Response token for room " + roomName + ": " + jsonResponse);

        return parseResponse(jsonResponse);
    }

    // parse response
    private TokenResponse parseResponse(String json) throws Exception {
        JSONObject obj = new JSONObject(json);

        TokenResponse response = new TokenResponse();
        response.serverUrl = obj.getString("serverUrl");
        response.participantToken = obj.getString("participantToken");
        response.roomName = obj.getString("roomName");
        response.participantName = obj.getString("participantName");

        return response;
    }
}

