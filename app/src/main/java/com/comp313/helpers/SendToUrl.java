package com.comp313.helpers;
/*
 * By: SHAFIQ-UR-REHMAN
 * Purpose: Send "POST" request to API hosted in Azure, and receive response from any given URL in background.
 * This class is used by "DbAdapter" which itself is AsyncTask, which in turn is used by Login, Bookings_All, BookingDetails, & NewUserRegister classes to send POST calls to API.
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SendToUrl
{
    public String sendToUrl(String myUrl, String formData) throws IOException
    {
        String responseData = "";
        OutputStream connOutputStream = null;
        InputStream inputStream = null;
        HttpURLConnection conn=null;

        try {
            URL url = new URL(myUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type","application/json");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setConnectTimeout(1000);

            //Write form-data to conn's request-body
            connOutputStream = conn.getOutputStream();
            OutputStreamWriter outputWriter = new OutputStreamWriter(connOutputStream, "UTF-8");
            BufferedWriter writer = new BufferedWriter(outputWriter);
            //
            writer.write(formData);
            writer.flush();
            writer.close();
            connOutputStream.close();
            //
            conn.connect();
            //
            //
            //
            inputStream = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuffer sb = new StringBuffer();

            //read ea line one by one
            String line = "";
            while((line = br.readLine()) != null)
            {
                sb.append(line);
            }
            //aft all lines r read, close buffer
            responseData = sb.toString();
            br.close();

        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            conn.disconnect();
        }
        return responseData;
    }
}
