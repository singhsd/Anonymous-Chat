/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import java.net.*;
import java.io.*;
import java.util.*;
import java.net.InetAddress;

/**
 *
 * @author S D Singh
 */
public class ServerObject implements Runnable{
    
    ServerSocket serverSock;
    ArrayList threads;
    ArrayList clientOutputStreams;
    
    ServerObject(ServerSocket sock, int portNum)
    {
        serverSock = sock;
        threads = new ArrayList();
        clientOutputStreams = new ArrayList();
    }
    
    public void run()
    {
        try{
            while(true)
            {
                Socket sock = serverSock.accept();
                InputStreamReader isr = new InputStreamReader(sock.getInputStream());
                PrintWriter writer = new PrintWriter(sock.getOutputStream());
                clientOutputStreams.add(writer);
                
                Runnable runnableForServerInput = new ServerInput(isr);
                Thread serverInput = new Thread(runnableForServerInput);
                threads.add(serverInput);
                serverInput.start();

            }
        } catch (Exception e) {
            System.out.println("Error in server: "+e);
            return;
        }
    }
    
    class ServerInput implements Runnable
    {
      BufferedReader reader;
      ServerInput(InputStreamReader isr)
      {
        reader = new BufferedReader(isr);
      }
      void tellEveryone(String message)
      {
        Iterator it = clientOutputStreams.iterator();
        while(it.hasNext())
        {
          try{
            PrintWriter writer = (PrintWriter) it.next();
            writer.println(message);
            writer.flush();
          }catch(Exception e){e.printStackTrace();}
        }
      }
      public synchronized void run()
      {
        try{
          String name = reader.readLine();
          while(true)
          {
            tellEveryone(name);
            name = reader.readLine();
          }
        }catch(Exception e){
            e.printStackTrace();
            cleanUp();
        }
      }
    }
    
    public void cleanUp()
    {
        String message = "Connection closed by host";
        Iterator it = clientOutputStreams.iterator();
        while(it.hasNext())
        {
          try{
            PrintWriter writer = (PrintWriter) it.next();
            writer.println(message);
            writer.flush();
            writer.close();
          }catch(Exception e){e.printStackTrace();}
        }
        
        Iterator it2 = threads.iterator();
        while(it.hasNext())
        {
          try{
            Thread th = (Thread) it2.next();
            th.stop();
          }catch(Exception e){e.printStackTrace();}
        }
    }
}
