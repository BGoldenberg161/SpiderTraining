import java.net.*;
import java.io.*;
import javax.net.ssl.*;
import java.util.*;

public class HomeSpider {
    public static void main(String[] args) throws Exception {

        String server = "www.siliconmtn.com";
        int port = 443;
        ArrayList<String> unCheckedLinks = new ArrayList<> ();
        unCheckedLinks.add("/");
        ArrayList<String> checkedLinks = new ArrayList<> ();
        
        try {
            while(!unCheckedLinks.isEmpty()){
            SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket)factory.createSocket(server, port);
            
            socket.startHandshake();
            
            PrintWriter out = new PrintWriter(
                new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())));
                    
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
                        
                String currentLink = unCheckedLinks.get(0);

                out.print("GET " + currentLink + " HTTP/1.1\r\n");
                out.print( "Host: " + server + "\r\n" );
                out.print( "User-Agent: bgold/1.2\r\n" );
                out.print("\r\n");
                out.flush();

                if (out.checkError()){
                    System.out.println("SSLSocketClient:  java.io.PrintWriter error");
                }

                String fileName = "HTML" + currentLink.replace("/","-") + ".txt";
                
                makeFile(fileName);
                
                FileWriter myWriter = new FileWriter(fileName);
                String inputLine;
                while ((inputLine = in.readLine()) != null){
                    myWriter.write(inputLine + "\r\n");
                    myWriter.flush();
                    if(inputLine.contains("<a ")){
                        String newLink = parseHTML(inputLine);
                        if(!unCheckedLinks.contains(newLink) && !checkedLinks.contains(newLink) && newLink != null){
                            unCheckedLinks.add(newLink);
                        }
                    }
                }
                myWriter.close();

                checkedLinks.add(currentLink);
                unCheckedLinks.remove(currentLink);
                
                System.out.println("checked links list: " + checkedLinks);
                System.out.println("unchecked links list: " + unCheckedLinks);
                
                in.close();
                out.close();
                socket.close();
            }
            
        } catch (Exception e) {
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
    
    public static String parseHTML(String line){
        int hrefStart = line.indexOf("href='/");
        int hrefStart2 = line.indexOf("href=\"/");
        if(hrefStart == -1 && hrefStart2 == -1){
            return null;
        }
        if(hrefStart == -1){
            int linkStart = line.indexOf("\"", (hrefStart2 + 1)) + 1;
            int linkEnd = line.indexOf("\"", linkStart);
            String link = line.substring(linkStart, linkEnd);

            return link;
        } else {
            int linkStart = line.indexOf("'", hrefStart) + 1;
            int linkEnd = line.indexOf("'", linkStart);
            String link = line.substring(linkStart, linkEnd);

            return link;
        }
        
    }
}   
