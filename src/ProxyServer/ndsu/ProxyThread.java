package ProxyServer.ndsu;

import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
//^^^Implements log locking and synchronous handling automatically

public class ProxyThread extends Thread {
    Logger logger = Logger.getLogger("Access");
    FileHandler fh;
    private Socket socket = null;
    private SocketAddress clientAddress = null;
    private static final int BUFFER_SIZE = 32768;
    public ProxyThread(Socket socket, SocketAddress address) {
        super("ProxyThread");
        this.socket = socket;
        this.clientAddress = address;
    }

    public void run() {
        try {
        	System.out.println(clientAddress);
//            DataOutputStream out =
//                    new DataOutputStream(socket.getOutputStream());
//            BufferedReader in = new BufferedReader(
//                    new InputStreamReader(socket.getInputStream()));
            //set up input and output stream
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String inputLine;
            int cnt = 0;
            String urlToCall = "";
            int contentSize = 0;

            //read input until empty
            while ((inputLine = in.readLine()) != null) {
                try {
                    StringTokenizer tok = new StringTokenizer(inputLine);
                    tok.nextToken();
                } catch (Exception e) {
                    break;
                }
                //parse the first line fof input for url
                if (cnt == 0) {
                    String[] tokens = inputLine.split(" ");
                    urlToCall = tokens[1];
                    //can redirect this to output log
                    System.out.println("Request for : " + urlToCall);
                }
                cnt++;
            }

            BufferedReader rd = null;

            //check for cached copy
            if (new File(urlToCall).exists())
            {
                System.out.println("Cached!");
            }
            //otherwise fetch it from remote server
            else {

                try {
                    //send request to server
                    System.out.println("sending request to real server for url: " + urlToCall);
                    URL url = new URL(urlToCall);
                    URLConnection conn = url.openConnection();
                    conn.setDoInput(true);
                    //not doing HTTP posts
                    conn.setDoOutput(false);
                    contentSize = conn.getContentLength();

                    //get response from server
                    InputStream is = null;
                    try {
                        is = conn.getInputStream();
                        rd = new BufferedReader(new InputStreamReader(is));
                    } catch (IOException ioe) {
                        System.out.println("********* IO EXCEPTION **********: " + ioe);
                    }

                    //send response to client
                    byte by[] = new byte[BUFFER_SIZE];
                    int index = is.read(by, 0, BUFFER_SIZE);
                    while (index != -1) {
                        out.write(by, 0, index);
                        index = is.read(by, 0, BUFFER_SIZE);
                    }
                    out.flush();
                } catch (Exception e) {
                    //can redirect this to error log
                    System.err.println("Encountered exception: " + e);
                    //encountered error - just send nothing back, so
                    //processing can continue (error appears in telnet response)
                    out.writeBytes("");
                }

                //log result to logfile
                try {

                    // This block configure the logger with handler and formatter
                    fh = new FileHandler("Access.log");
                    logger.addHandler(fh);

                    // the following statement is used to log any messages
                    String timestamp = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss").format(Calendar.getInstance().getTime());
                    logger.info("Date: " + timestamp + ", Client Address: " + "ipaddress" + ", URL: " + urlToCall + ", Content Size: " + contentSize);

                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                //close out all resources
                if (rd != null) {
                    rd.close();
                }
                if (socket != null) {
                    socket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
