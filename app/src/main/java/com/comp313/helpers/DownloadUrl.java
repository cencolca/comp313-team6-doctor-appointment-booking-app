package com.comp313.helpers;
/*
 * By: SHAFIQ-UR-REHMAN
 * Purpose: Send "GET" request to and receive response from any given URL in background
 * This class is used by "GetNearbyPlacesData" which itself is AsyncTask
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadUrl
{

    ////JSON string returned from API
    public String readUrl(String myUrl) throws IOException
    {
        String data = "";
        InputStream inputStream = null;
        HttpURLConnection urlConnection=null;

        try {
            URL url = new URL(myUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            //read data from URL
            inputStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer sb = new StringBuffer();

            //read ea line one by one
            String line = "";
            while((line = br.readLine()) != null)
            {
                sb.append(line);
            }
            //aft all lines r read, close buffer
            data = sb.toString();
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
            inputStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}
