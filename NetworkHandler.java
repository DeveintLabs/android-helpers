package deveint.nse.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import deveint.nse.R;

/**
 * Created by myles on 9/26/15.
 */
public class NetworkHandler {
    public boolean isConnected(Activity activity){
        ConnectivityManager connectivityManager=(ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info=connectivityManager.getActiveNetworkInfo();
        if(info != null && info.isConnectedOrConnecting()){
            return true;
        }else{
            return false;
        }
    }
    /*
    * This method pings Google's DNS to make sure the device is actually online
    * */
    public boolean isOnline() {

        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }
    /*
    * makes a post request and returns the result as JSON string
    * Authorization is enabled just change the credentials
    * @param targetURL the url you are calling
    * @param urlParameters the parameters that a encoded with URLEncoder.encode()
    * */
    public static String  excutePost(String targetURL, String urlParameters)
    {
        //Log.v("DATA",urlParameters);
        String authorizationString = "Basic " + Base64.encodeToString(
                ("admin" + ":" + "1234").getBytes(),
                Base64.NO_WRAP); //Base64.NO_WRAP flag
        URL url;
        HttpURLConnection connection = null;
        //System.setProperty("http.keepAlive", "false");
        try {
            //Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
           /* connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length", "" +
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");*/
            connection.setRequestProperty("Authorization",authorizationString);
            connection.setUseCaches (false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes (urlParameters);
            wr.flush ();
            wr.close ();
            //Get Response
            InputStream is ;
            if(connection.getResponseCode()/100 ==2){
                is=connection.getInputStream();
            }else{
                is=connection.getErrorStream();
            }
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        } finally {

            if(connection != null) {
                connection.disconnect();
            }
        }
    }
    /*
   * makes a get request and returns the result as JSON string
   * Authorization is enabled just change the credentials
   * @param url the url you are calling
   * */
    public static String GET(String url) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String authorizationString = "Basic " + Base64.encodeToString(
                ("admin" + ":" + "1234").getBytes(),
                Base64.NO_WRAP); //Base64.NO_WRAP flag
        String responseString = null;
        try {
            HttpGet httpGet=new HttpGet(url);
            httpGet.setHeader("Authorization",authorizationString);
            response = httpclient.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                responseString = out.toString();
                out.close();
            } else{
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
            //TODO Handle problems..
        } catch (IOException e) {
            //TODO Handle problems..
        }
        return responseString;
    }
    /*
    * Makes a put request
    * */
    public String sendPut(String path,String params) throws Exception {
        URL url = new URL(path);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setRequestProperty(
                "Content-Type", "application/x-www-form-urlencoded" );
        httpCon.setDoOutput(true);
        httpCon.setRequestMethod("PUT");
        OutputStreamWriter out = new OutputStreamWriter(
                httpCon.getOutputStream());
        out.write(params);
        out.close();

        /*BufferedReader in = new BufferedReader(
                new InputStreamReader(httpCon.getInputStream()));*/
        //BufferedReader in=new BufferedReader(new InputStreamReader(((HttpURLConnection) (new URL(params)).openConnection()).getInputStream(), Charset.forName("UTF-8")));
        BufferedReader in=new BufferedReader(new InputStreamReader(httpCon.getInputStream(), Charset.forName("UTF-8")));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        //print result
        return response.toString();
    }
    /*
    * Makes a Delete request
    * */
    public String sendDelete(String path) throws Exception {
        URL url = new URL(path);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setDoOutput(true);
        httpCon.setRequestProperty(
                "Content-Type", "application/x-www-form-urlencoded" );
        httpCon.setRequestMethod("DELETE");
        httpCon.connect();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(httpCon.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        //print result
        return response.toString();
    }
    /*
    * A template dialog
    * */
    public void show_dialog(Activity activity,String message){
        AlertDialog.Builder builder=new AlertDialog.Builder(activity);
        builder.setMessage(message)
                .setTitle(activity.getResources().getString(R.string.app_name));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();
    }
}
