/**
 * 
 */
package com.amazonaws.samples;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * @author nirmoho-Mac
 *
 */
public class LoadGenerator {

    public static int connectToURL(String URLtoConnect) throws Exception  {
        
        URL url = new URL(URLtoConnect);
        HttpURLConnection urlConnect = (HttpURLConnection) url.openConnection();
        return urlConnect.getResponseCode();
    }
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception{
        
        String AMI_ID = "ami-7747a30d";
        String INSTANCE_TYPE = "m3.medium";
        AWSInstanceClass instance1 = new AWSInstanceClass(AMI_ID, INSTANCE_TYPE);
       
        String instanceIdLG = instance1.createInstance();
        System.out.println("Created Instance with instance Id -" + instanceIdLG);
      
        String publicDNSLG = instance1.getPUBLIC_DNS();
        System.out.println("Found publicDNS -" + publicDNSLG);
    
        AMI_ID = "ami-247a9e5e";
        INSTANCE_TYPE = "m3.medium";
        AWSInstanceClass instance2 = new AWSInstanceClass(AMI_ID, INSTANCE_TYPE);
       
        String instanceIdWS = instance2.createInstance();
        System.out.println("Created Instance with instance Id -" + instanceIdWS);
       
        String publicDNSWS = instance2.getPUBLIC_DNS();
        System.out.println("Found publicDNS -" + publicDNSWS);
        
        Thread.sleep(120000);
        String andrewId = System.getenv("ANDREW_ID");
        String subPassword = System.getenv("SUBMISSION_PASSWORD");
        String loadGenURL = "http://"+publicDNSLG+"/password?passwd="+subPassword+"&andrewid="+andrewId;

        System.out.println(loadGenURL);   
        
        URL url = new URL(loadGenURL);
        HttpURLConnection urlConnect = (HttpURLConnection) url.openConnection();
        
        if(urlConnect.getResponseCode() == 200)   {
            System.out.println("Successfully Authenticated to LoadGen");
        }   else    {
            System.exit(1);
        }
        
        Thread.sleep(30000);
        
        String submitVM = "http://"+publicDNSLG+"/test/horizontal?dns="+publicDNSWS;
        url = new URL(submitVM);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        System.out.println(urlConnection.getResponseCode());
        
        BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String inputLine; 
        String log_id="";
        while ((inputLine = in.readLine()) != null) {
            log_id = inputLine.substring(92, 114);
            System.out.println(log_id);
        }
            
        in.close();
        
        String logRequest = "http://"+publicDNSLG+"/log?name="+log_id;
        
       
        double rps=0;
        int minuteLaunched = 0;
        int currentMinute = 0;
        while(rps < 4000.0)  {
            
            
            URL uri = new URL(logRequest);
            HttpURLConnection uriCon = (HttpURLConnection) uri.openConnection();
            //System.out.println(uriCon.getResponseCode());
            
            in = new BufferedReader(new InputStreamReader(uriCon.getInputStream()));
            
            if((currentMinute - minuteLaunched) >= 3)   {
                AMI_ID = "ami-247a9e5e";
                INSTANCE_TYPE = "m3.medium";
                AWSInstanceClass additionalInstance = new AWSInstanceClass(AMI_ID, INSTANCE_TYPE);
                String instanceIdVM = additionalInstance.createInstance();
                System.out.println("Created Instance with instance Id -" + instanceIdVM);
              
                String publicDNSVM = additionalInstance.getPUBLIC_DNS();
                System.out.println("Found publicDNS -" + publicDNSVM);
                
                String newVMURL ="http://"+publicDNSLG +"/test/horizontal/add?dns="+publicDNSVM;
                System.out.println(newVMURL);
                url = new URL(newVMURL);
                urlConnect = (HttpURLConnection) url.openConnection();
                System.out.println(urlConnect.getResponseCode());
                while(urlConnect.getResponseCode() != 200) {
                    urlConnect = (HttpURLConnection) url.openConnection(); 
                }
                System.out.println("New VM Added");
                minuteLaunched = currentMinute;
                System.out.println("Current Minute = " + currentMinute);
                System.out.println("Current rps = " + rps);  
               
            }
            
            while ((inputLine = in.readLine()) != null) {
                //System.out.println(inputLine);
                
                if(inputLine.startsWith("[Minute")) {
                    String[] parts = inputLine.split(" ");
                    String Minute = parts[1].substring(0, parts[1].length()-1);
                    currentMinute = Integer.parseInt(Minute);
                }
                
                if(inputLine.startsWith("[Current"))    {
                    String parts[] = inputLine.split("=");
                    String RPS = parts[1].substring(0, parts[1].length()-1);
                    rps = Double.parseDouble(RPS);
                }
            } 
        }
        
        
    }

}
