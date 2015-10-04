package ProxyServer.ndsu;

import java.net.*;
import java.io.*;
import java.util.*;

public class ProxyThread extends Thread {
    private Socket socket = null;
    private static final int BUFFER_SIZE = 32768;
    public ProxyThread(Socket socket) {
        super("ProxyThread");
        this.socket = socket;
    }

    public void run() {
        //get input from user
        //send request to server
        //get response from server
        //send response to user

        try {
            DataOutputStream out =
                    new DataOutputStream(socket.getOutputStream());
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            String inputLine, outputLine;
            int cnt = 0;
            String urlToCall = "";
            ///////////////////////////////////
            //begin get request from client
            while ((inputLine = in.readLine()) != null) {
                try {
                    StringTokenizer tok = new StringTokenizer(inputLine);
                    tok.nextToken();
                } catch (Exception e) {
                    break;
                }
                //parse the first line of the request to find the url
                System.out.println(inputLine);
                if (cnt == 0) {
                    String[] tokens = inputLine.split(" ");
                    urlToCall = tokens[1];
                    //can redirect this to output log
                    System.out.println("Request for : " + urlToCall);
                }
                cnt++;
            }

            //end get request from client
            ///////////////////////////////////


            BufferedReader rd = null;
            try {
                //System.out.println("sending request
                //to real server for url: "
                //        + urlToCall);
                ///////////////////////////////////
                //begin send request to server, get response from server
                System.out.println("url to call: " + urlToCall);
                URL url = new URL(urlToCall);
                URLConnection conn = url.openConnection();
                conn.setDoInput(true);
                //not doing HTTP posts
                conn.setDoOutput(false);
                System.out.println("content length: " + conn.getContentLength());
                System.out.println("allowed user interaction: " + conn.getAllowUserInteraction());
                System.out.println("content encoding: " + conn.getContentEncoding());
                System.out.println("content type: " + conn.getContentType());

                // Get the response
                System.out.println("2");
                InputStream is = null;
                HttpURLConnection huc = (HttpURLConnection)conn;
                System.out.println(conn.getContentLength());
                if (conn.getContentLength() > 0) {
                    try {
                        is = conn.getInputStream();
                        rd = new BufferedReader(new InputStreamReader(is));
                    } catch (IOException ioe) {
                         System.out.println(
                                "********* IO EXCEPTION **********: " + ioe);
                    }
                }
                else{
                    System.out.println("content length failed");
                }
                //end send request to server, get response from server
                ///////////////////////////////////

                System.out.println("3");
                ///////////////////////////////////
                //begin send response to client
                byte by[] = new byte[ BUFFER_SIZE ];
                System.out.println("4");
                int index = is.read( by, 0, BUFFER_SIZE );
                System.out.println("5");
                while ( index != -1 )
                {
                    out.write( by, 0, index );
                    index = is.read( by, 0, BUFFER_SIZE );
                    System.out.println("6");
                }
                System.out.println("7");
                out.flush();
                System.out.println("8");

                //end send response to client
                ///////////////////////////////////
            } catch (Exception e) {
                //can redirect this to error log
                System.err.println("Encountered exception: " + e);
                //encountered error - just send nothing back, so
                //processing can continue
                out.writeBytes("failed");
            }

            //close out all resources
            if (rd != null) {
                rd.close();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null) {
                socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
