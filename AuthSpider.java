import java.net.*;
import java.io.*;
import javax.net.ssl.*;
import java.util.*;

public class AuthSpider {
    public static void main(String[] args) throws Exception {

        String server = "www.siliconmtn.com";
        int port = 443;

        ArrayList<String> unCheckedLinks = new ArrayList<> ();
        ArrayList<String> checkedLinks = new ArrayList<> ();
        addLinks(unCheckedLinks);

        String params = "requestType=" + URLEncoder.encode("reqBuild", "UTF-8");
        params += "&" + URLEncoder.encode("pmid", "UTF-8") + "=" + URLEncoder.encode("ADMIN_LOGIN", "UTF-8");
        params += "&" + URLEncoder.encode("emailAddress", "UTF-8") + "=" + URLEncoder.encode("branden.goldenberg@siliconmtn.com", "UTF-8");
        params += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode("SMTRul3s!", "UTF-8");
    
        String jid = "";
        
        
        SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();

        try {
            while(!unCheckedLinks.isEmpty()){
            SSLSocket socket = (SSLSocket)factory.createSocket(server, port);
            
            socket.startHandshake();
            
            PrintWriter out = new PrintWriter(
            new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream())));

            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

                
                String currentLink = unCheckedLinks.get(0);
                if (currentLink.equals("/sb/admintool")){
                    login(currentLink, server, params, out);
                } else {
                    get(currentLink, server, jid, out);
                }

                String fileName = "HTML" + currentLink.replace("/","-") + ".txt";
                makeFile(fileName);
                
                try{
                    FileWriter myWriter = new FileWriter(fileName);
                    String inputLine;
    
                    while ((inputLine = in.readLine()) != null){
                        myWriter.write(inputLine + "\r\n");
                        System.out.println(inputLine);
    
                        if(inputLine.contains("JSESSIONID") && currentLink.equals("/sb/admintool")){
                            String newCookie = parseCookie(inputLine, "JSESSIONID");
                            jid = newCookie;
                        }
                    }
                    myWriter.close();
                }catch (Exception e) {
                    e.printStackTrace();
                }
                
                checkedLinks.add(currentLink);
                unCheckedLinks.remove(currentLink);    
                
                System.out.println("checked links list: " + checkedLinks);
                System.out.println("unchecked links list: " + unCheckedLinks);

                in.close();
                out.close();
                socket.close();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void makeFile(String filename) {
        try {
          File myFile = new File(filename);
          if (myFile.createNewFile()) {
            System.out.println("File created: " + myFile.getName());
          } else {
            System.out.println("File already exists.");
          }
        } catch (IOException e) {
          System.out.println("An error occurred.");
          e.printStackTrace();
        }
      }
    
    public static String parseCookie(String line, String key){
        int cookieStart = line.indexOf(key) + 1;
        if(cookieStart == -1){
            return null;
        }
        int valueStart = cookieStart + key.length();
        int valueEnd = line.indexOf(";", cookieStart);
        
        return line.substring(valueStart, valueEnd);
    }

    public static void login(String link, String server, String params, PrintWriter out){
        out.print("POST " + link + " HTTP/1.1\r\n");
        out.print("Host: " + server + "\r\n");
        out.print("Content-Type: application/x-www-form-urlencoded\r\n");
        out.print("Content-Length: "+params.length()+"\r\n");
        out.print("User-Agent: bgold/1.2\r\n");
        out.print("\r\n");
        //auth form values
        out.print(params + "\r\n");
        out.print("\r\n");
        out.flush();
    }

    public static void get(String link, String server, String jid, PrintWriter out){
        out.print("GET " + link + " HTTP/1.1\r\n");
        out.print("Host: " + server + "\r\n");
        out.print("Cookie: JSESSIONID=" + jid + "\r\n");
        out.print("User-Agent: bgold/1.2\r\n");
        out.print("\r\n");
        out.flush();
    }

    public static void addLinks(ArrayList unCheckedLinks){
        //login page
        unCheckedLinks.add("/sb/admintool");
        //redirect
        unCheckedLinks.add("/admintool");
        //links
        unCheckedLinks.add("/sb/admintool?cPage=index&actionId=FLUSH_CACHE");
        unCheckedLinks.add("/sb/admintool?cPage=stats&actionId=FLUSH_CACHE");
        unCheckedLinks.add("/sb/admintool?cPage=index&actionId=SCHEDULE_JOB_INSTANCE&organizationId=SMT");
        unCheckedLinks.add("/sb/admintool?cPage=index&actionId=WEB_SOCKET&organizationId=SMT");
        unCheckedLinks.add("/sb/admintool?cPage=index&actionId=ERROR_LOG&organizationId=SMT");
    }

}   
