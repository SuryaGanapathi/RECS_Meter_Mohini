/**
 *
 */
package com.recsmeterreading.connections;

import android.content.Context;
import android.util.Log;


import com.google.gson.Gson;
import com.recsmeterreading.Utils.AppLogs;
import com.recsmeterreading.Utils.FileUtil;
import com.recsmeterreading.Utils.Network;
import com.recsmeterreading.Utils.PrintLog;
import com.recsmeterreading.exceptions.BadRequestException;
import com.recsmeterreading.exceptions.InternalServerError;
import com.recsmeterreading.exceptions.JavaLangException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author Ravinder
 */
public class Connection {

    public static final int STATUS_FOUND = 200;
    public static final int STATUS_204 = 204;
    public static final int STATUS_400 = 400;
    public static final int STATUS_401 = 401;
    public static final int STATUS_403 = 403;
    public static final int STATUS_404 = 404;
    public static final int STATUS_412 = 412;
    public static final int STATUS_423 = 423;
    public static final int STATUS_ERROR = 500;
    private static int TIMEOUT = 30 * 1000;

    private static final String TAG = "Connection";



    public static final String map_view_http = "";

    public static final String base_url_uat_https = "";

    //Production Url
    public static final String base_url_prod = "";

    //Production Url
    public static final String base_url_prod_https = "";

    //Production Url


    private static Object HTTPRequestPost(Context context, String url, String aContentType, String aBody) {

        HttpURLConnection connection;
        url = url.replaceAll(" ", "%20");

        AppLogs.log(TAG, "****** request url : == " + url);
        AppLogs.log(TAG, "****** request body : == " + aBody);
        String status = "0";// return 0 if failed.
        //String accessToken = SharedPreferenceUtil.getAccessToken(context);

        try {
            HttpURLConnection.setFollowRedirects(false);
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("Content-Type", aContentType);
            //connection.setRequestProperty("Authorization", "bearer " + accessToken);
            connection.setConnectTimeout(TIMEOUT);
            if (aBody != null) {
                connection.setRequestProperty("Content-Length", "" + aBody.getBytes().length);
                // Send request as data output stream
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                //wr.writeBytes(aBody);
                wr.write(aBody.getBytes(StandardCharsets.UTF_8));
                wr.flush();
                wr.close();
            } else {
                connection.setRequestProperty("Content-Length", "" + 0);
            }
            connection.connect();

            int responseCode = connection.getResponseCode();
            AppLogs.log(TAG, "****** Response code: " + connection.getResponseCode() + ", Message: " + connection.getResponseMessage());
            if (responseCode == STATUS_FOUND) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                AppLogs.log(TAG, "****** response Message: " + response.toString());

                if (!response.equals("")) {
                    return response.toString();
                } else {
                    return new BadRequestException("Unable Process Your Request Please try Again later");
                }

            } else if (responseCode == STATUS_204) {
                return new BadRequestException("Invalid Login credentials");
            } else if (responseCode == STATUS_400 || responseCode != STATUS_FOUND) {
                return new BadRequestException("Invalid Credentials or bad request. Please try again.");
            } else if (responseCode == STATUS_ERROR) {
                return new InternalServerError("Internal Server Error. Please try again later.");
            } else {
                String errorMessage = new String(FileUtil.toByteArray(connection.getErrorStream()));
                PrintLog.print(TAG, "****** Error Message: " + errorMessage);
                return new JavaLangException("Unable to fetch data from. Please try again later.");
            }
        } catch (Exception e) {
            PrintLog.print(TAG, "****** Error Message: " + e);
            AppLogs.log(TAG, "****** exception : == " +e);
            return e.toString();
//            return new InternalServerError("Unable to connect to server. Please try again later.");
        }
    }


    public static Object callHttpPostRequestsV2(Context context, String url, Object requestBody) {
        if (Network.isAvailable(context)) {
            String requestString = new Gson().toJson(requestBody);
            String AccessToken = new Gson().toJson(requestBody);
            if (requestBody == null) {
                return HTTPRequestPost(context, url, "application/json", null);
            } else {
                return HTTPRequestPost(context, url, "application/json", requestString.replace("\\\\", ""));
            }
        } else {
            return new BadRequestException("No Internet Connection Please Connect to Internet");
        }
    }

    public static Object callHttpPostRequestsV2Jobj(Context context, String url, String requestBody) {
        if (Network.isAvailable(context)) {
            if (requestBody == null) {
                return HTTPRequestPost(context, url, "application/json", null);
            } else {
                return HTTPRequestPost(context, url, "application/json", requestBody);
            }
        } else {
            return new BadRequestException("No Internet Connection Please Connect to Internet");
        }
    }


    public static Object callHttpGetRequestsV2(Context aContext, String s, String url) {
        if (Network.isAvailable(aContext)) {

            HttpURLConnection connection;

            // url = url.replaceAll(" ", "%20");
            url = s.replaceAll(" ", "%20");
            PrintLog.print(TAG, url);
            // String accessToken = SharedPreferenceUtil.getAccessToken(aContext);

            try {
                connection = (HttpURLConnection) new URL(url).openConnection();
                // connection.setDoOutput(true);
                connection.setConnectTimeout(1000000);
                connection.setReadTimeout(1000000);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                // connection.setRequestProperty("Authorization", "bearer " + accessToken);
                connection.connect();
//                int responseCode = connection.getResponseCode();

//                if (responseCode == STATUS_FOUND) {
                    PrintLog.print(TAG, "Success....");
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    PrintLog.print(TAG, response.toString());
                    return response.toString();

//                } else if (responseCode > 201 && responseCode <300) {
//                    return new JavaLangException("Sorry No data found");
//
//                } else {
//                    return new JavaLangException("Unable to fetch data from . Please try again later.");
//                }
            } catch (Exception e) {
                Log.e("Connection.java:",""+e.toString());
                return new InternalServerError("Unable to connect to server. Please try again later.");
            }
        } else {
            return new BadRequestException("No Internet Connection Please Connect to Internet");
        }
    }
}
