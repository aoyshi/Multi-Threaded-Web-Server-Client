/** Arunika Oyshi
 * 
 * REFERENCES:
 * 1: JAVA MULTI-THREADED SERVER + SOCKET PROGG: https://www.utc.edu/center-information-security-assurance/pdfs/multi.threaded.web.server.pdf
 * 2: SERVER GUI: http://www.codejava.net/java-se/swing/redirect-standard-output-streams-to-jtextarea
 * 3: HTTP E-TAGS: https://www.ntu.edu.sg/home/ehchua/programming/webprogramming/HTTP_Basics.html
 * 4: HTTP MESSAGE SYNTAX: https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/PUT
 * 5: HTTP STATUS CODES: https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
 * 6: JAVAFX DIALOGUES: http://code.makery.ch/blog/javafx-dialogs-official/
 *.
 */

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

//subclass to redirect sys.out.print to gui
class CustomOutputStream extends OutputStream {
    private JTextArea textArea;
    
    //constructor
    public CustomOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }
     
    //writes to textarea with input "b" (char as int) and refocuses textarea to bottom 
    @Override
    public void write(int b) throws IOException {
        // redirects data to the text area
        textArea.append(String.valueOf((char)b));
        // scrolls the text area to the end of data
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
}

//GUI class
public class myServer extends JFrame {
    /**
     * The text area which is used for displaying logging information.
     */
    private JTextArea textArea;
    private JButton buttonClear = new JButton("Clear"); 
    private PrintStream standardOut;
  
//constructor    
  public myServer() {
        super("Server Log"); //window title
         
        textArea = new JTextArea(50, 10);
        textArea.setEditable(false);
        PrintStream printStream = new PrintStream(new CustomOutputStream(textArea));
         
        // keeps reference of standard output stream
        standardOut = System.out;
         
        // re-assigns standard output stream and error output stream
        System.setOut(printStream);
        System.setErr(printStream);
 
        /**creates the GUI**/
        
        //add clear button
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(10, 10, 10, 10);
        constraints.anchor = GridBagConstraints.WEST;         
        constraints.gridx = 1;
        add(buttonClear, constraints); 
        
        //create text area
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;        
        add(new JScrollPane(textArea), constraints);       
        
        // adds event handler for button Clear
        buttonClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                // clears the text area
                try {
                    textArea.getDocument().remove(0,
                            textArea.getDocument().getLength());
                    standardOut.println("Text area cleared");
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        });
      
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);    // centers on screen
    }

  private static ArrayList<String> clientList = new ArrayList<String>(); //stores list of connected clients
  private static String path = "";
  
  public static void main(String[] args) throws Exception { 
     /**
     * Runs the gui program.
     */
        myServer mainFrame = new myServer();
     
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                mainFrame.setVisible(true);
            }
        });
        
        boolean isValidPath = false;
        
        while(!isValidPath)
        {
            //ask user for server directory path
            path = (String) JOptionPane.showInputDialog(
                mainFrame,
                "Enter Server Directory Path (enter -1 to exit Server): \n",
                "Input Dialog",
                JOptionPane.PLAIN_MESSAGE,
                null, //no icon
                null, //no drop down
                null  //no prompt text
            );
            
            //If a string was entered by user
            if ((path != null) && (path.length() > 0)) 
            {    
              if(path.equals("-1"))
              {
                System.exit(0);
              }
                
              isValidPath = checkPath(path); //check if path valid
              if(!isValidPath) //display error msg if invalid
              {
                System.out.println(" !! \"" + path + "\" is not a valid directory path. Try Again.");    
              }
            }
            else //user didnt enter anything 
            {
               System.out.println(" !! You must enter a file path. Try Again.");    
            }
        }
        
        System.out.println("\nCurrent Server Directory: " + path);
        System.out.println("--------------------------------------------------------------------------");

        int port=6789;//Define port number
        ServerSocket serverSocket = null; //create socket to connect to client
        boolean serverConnected = false; //boolean checks if server connected successfully

       //Establish server socket listening to port 6789
        try 
        {
          serverSocket = new ServerSocket(port);
          serverConnected = true; //server connected successfully
        }
        catch(Exception e)
        {
          System.out.println("Error! Couldn't set up server.\n");
        }

       //enter loop to process request only if server connected successfully
       if(serverConnected)
       {
          System.out.println("Server connected. Listening for requests at port " + port + "...\n");
          //Process HTTP service requests in infinite loop
          while(true)
          {
            //Listen through socket for client TCP connection request, accept if found
            Socket clientSocket = serverSocket.accept();  
            // Construct an object to process the HTTP request message. 
            HttpRequest request = new HttpRequest(clientSocket, clientList, path);
            // Create a new thread to process the request. 
            Thread thread = new Thread(request);
            thread.start();  
          }
        }        
 
    }//end main
  
    //takes file path as input and returns true if path leads to valid directory
  private static boolean checkPath(String path) {
      boolean isValid = false;   
      try 
      {
        File test = new File(path); //throws exception if invalid path        
        if(test.isFile()) //false if it's file, not folder
          isValid = false;        
        else if(test.isDirectory()) //return true only if path leads to valid directory
          isValid = true;
      }    
      catch(Exception e)
      {
        isValid = false;
        e.printStackTrace();
      }   
      return isValid;
  }

}//end myServer class

//This class handles client requests as individual thread
final class HttpRequest implements Runnable 
{
  final String CRLF = "\r\n"; //to terminate output stream (http)
  Socket socket;  //reference of clientSocket connection with server
  private String client; //stores client name for current thread
  private ArrayList<String> clientList; //copy of global client list
  private String path;
    
  //Constructor
  public HttpRequest(Socket socket, ArrayList<String> clientList, String path) throws Exception 
  {
    this.socket = socket; 
    this.clientList = clientList;
    this.path = path;
  }
  
  //Implement run method of Runnable Interface
  @Override
  public void run() 
  {
    try 
    {       
      processRequest(); //this method handles client request
    }
    catch(Exception e) //run cant throw exception so catch any errors processRequest throws
    {
      System.out.println(e);
    }
  }
  
  //This method analyzes client request and gives appropriate response
  private void processRequest() throws Exception 
  {
    //Get reference to client socket output stream (to write to client)
    DataOutputStream os = new DataOutputStream(socket.getOutputStream()); 

    //Wrap input stream with filters (to read from client)
    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    
    String clientReq = ""; //reads client requests
    while(!(clientReq=br.readLine()).equals("</DISCONNECT>")) //loop to serve client forever until client disconnects
    {
        //System.out.println(clientReq); 
        String requestLine = clientReq;//get client request line
        
        String[] tokens = clientReq.split(" "); //split at whiteaspaces
        String requestType = tokens[0]; //GET or POST
        String requestedItem = tokens[1]; //NAME OF FILE (or filelist)
        
        //collect client headerlines
        String headerLine = ""; String temp = "";
        while((temp = br.readLine()).length()!= 0)
        {    
          //System.out.println(headerLine); 
            headerLine += temp + "\n";
        } 
        //System.out.println();
        
        switch(requestType)
        {
            case "GET": if(requestedItem.equals("/File-List")) //get file list and send to client
                        {
                            System.out.println("\n******* "+ this.client + " REQUESTED TO VIEW FILE LIST *******");
                            System.out.println(requestLine);
                            System.out.println(headerLine);
                            sendFileList(os);
                        } 
                        else //CLIENT requests a file 
                        {
                            System.out.println("\n******* "+ this.client + " REQUESTED TO DOWNLOAD A FILE *******");
                            System.out.println(requestLine);
                            System.out.println(headerLine);
                            sendFile(os,requestedItem);
                        }
                        break;
                        
            case "POST":  if(requestedItem.equals("/<SUBMIT-USERNAME>"))//register current client unique name    
                          {
                             saveUsername(br,os);
                             System.out.println("\n******* WELCOME NEW CLIENT: "+ this.client + " *******");
                             System.out.println(requestLine);
                             System.out.println(headerLine);
                                 
                            //DISPLAY list of currently connected clients
                            System.out.println("-----------------------------------------------------------------");
                            System.out.println(">TIME NOW: "+ getHTTPTime()); 
                            System.out.println(">LIST OF CURRENT CLIENTS: " + clientList.toString());
                            System.out.println("-----------------------------------------------------------------");
                          }                
                          else //download file FROM client
                          {
                            System.out.println("\n******* "+ this.client + " REQUESTED TO UPLOAD A FILE *******\n");
                            int success = saveFile(os, requestedItem);
                            System.out.println(requestLine);
                            System.out.println(headerLine);
                            if(success==1)
                             System.out.println("File successfully received from client " + this.client);
                            else
                             System.out.println("Failed to receive file from client.");
                          } 
                          break;                     
        }

    } 
    System.out.println("-----------------------------------------------------------------");
    System.out.println(this.client + " DISOCNNECTED "); //user has disconnected
    clientList.remove(this.client);
    //DISPLAY updated list of currently connected clients
    System.out.println("\nTIME NOW: "+ getHTTPTime()); 
    System.out.println("LIST OF CURRENT CLIENTS: " + clientList.toString());
    System.out.println("-----------------------------------------------------------------");
    
    //Close i/o streams and sockets
    os.close();
    br.close();
    socket.close();
    
  } //end http class
  
  //takes request filename as input and sends file via a sister socket to client
  private void sendFile(DataOutputStream os, String filename) throws Exception{
        
    //Prepend chosen server directory path to filename
    filename = path + "\\" + filename;   
    FileInputStream fis = null;
    boolean fileExists = true;
    try //check if file exists
    {
      fis = new FileInputStream(filename);
    }
    catch (Exception e) 
    {
      fileExists = false;
    }
    
    /*---construct status line---*/
    String statusLine = null;
    double contentLength = 0; //stores type of file (extension type)
    
    if(fileExists)
    {
      statusLine = "HTTP/1.1 200 OK" + CRLF;
      contentLength = fis.getChannel().size();
    }
    else 
    {
      statusLine = "HTTP/1.1 404 Not Found" + CRLF;
    }
    /*---construct header lines---*/
    String headerLines = "Server: AO:6879" + CRLF +
                         "User-Agent: NetBeans IDE/8.1" + CRLF +
                         "Content-Type: " + contentType(filename) + CRLF +
                         "Content-Length: " + contentLength + CRLF +
                         "Date: " + getHTTPTime() + CRLF + CRLF;
    //send response message to client
    os.writeBytes(statusLine);
    os.writeBytes(headerLines);   
     
    if(fileExists) {
        /*  SEND FILE TO CLIENT  THROUGH SECONDARY SOCKET  */
        int serverPORT = 7000; 
        ServerSocket sisterSocket = new ServerSocket(serverPORT);
        Socket sisterClientSocket = sisterSocket.accept();
        DataOutputStream sisterOS = new DataOutputStream(sisterClientSocket.getOutputStream()); 

        byte[] buffer = new byte[1024]; //buffer to hold bytes as they are put into ouput stream
        int bytesRead = 0; //amount of data read into buffer
        while ((bytesRead = fis.read(buffer)) != -1) //loop till end of file, read data from file to buffer
        {
          sisterOS.write(buffer, 0, bytesRead); //write file data from buffer into output stream to client
        }

        //close all streams & secondary sockets
        fis.close();
        sisterOS.close();
        sisterClientSocket.close();
        sisterSocket.close();

        System.out.println("File successfully sent to client " + this.client);       
    }
    
  }
  
  //registers username posted by client and saves it in global arraylist clientList
  private void saveUsername(BufferedReader br, DataOutputStream os) throws Exception{
    String username = "";
    while((username=br.readLine()).length()!=0)
    {    
      this.client = username; //set current client name as username in msg body
    } 
    //save username on list of connected clients
    clientList.add(this.client);
   
    String statusLine = "HTTP/1.1 201 Created" + CRLF;
    /*---construct header lines---*/
    String headerLines = "Server: AO:6879" + CRLF +
                         "User-Agent: NetBeans IDE/8.1" + CRLF +
                         "Content-Type: text/plain" + CRLF +
                         "Content-Length: " + client.length() + CRLF +
                         "Date: " + getHTTPTime() + CRLF + CRLF;
    //send server response message
    os.writeBytes(statusLine);
    os.writeBytes(headerLines);
  }
  
  //This method return file type by parsing the provided string filename
  private static String contentType(String filename)
  {
    if(filename.endsWith(".htm")||filename.endsWith(".html"))
      return "text/html";
    else if(filename.endsWith(".jpeg")||filename.endsWith(".jpg"))
      return "image/jpeg";
    else if(filename.endsWith(".pdf"))
      return "application/pdf";
    else if(filename.endsWith(".gif"))
      return "image/gif";
    else if(filename.endsWith(".png"))
      return "image/png";
    else if(filename.endsWith(".doc")||filename.endsWith(".docx"))
      return "application/msword";
    else if(filename.endsWith(".txt"))
      return "text/plain";
    
    return "aplication/octet-stream"; //default (if no ext matches above)
  }
  
  //this method returns a string containing all available filenames in the main server directory
  private void sendFileList(DataOutputStream os) throws Exception{

      File serverDir = new File(path); //get server folder from path to server directory
      File[] listOfFiles = serverDir.listFiles(); //get list of files in dir
      String fileList = "Available files at Server (subfolders are in parentheses): \n";
      
      for (File file : listOfFiles)  //loop through list of files
      {
        if (file.isFile()) //if file, simple print filename
           fileList += file.getName()+"  ";        
        
        else if (file.isDirectory()) //if directory, wrap in parentheses first
           fileList += "("+file.getName()+")  ";        
      }      
      
      fileList += "\n";
      
      //statusLine
      String status = "HTTP/1.1 200 OK"+CRLF;
      //header     
      String header =  "Content-Type: text/plain" + CRLF +
                       "Content-Length: " + fileList.length() + CRLF +
                       "Server: AO:6789" + CRLF +
                       "Date: " + getHTTPTime() + CRLF +
                       "User-Agent: NetBeans 8.1" + CRLF + CRLF;
      
      os.writeBytes(status);
      os.writeBytes(header);    
      os.writeBytes(fileList);//send file list as entity body
      os.writeBytes(CRLF); //terminate message body
  }
  
  //takes filename as input, sends requested file to client via sister socket; returns 1 for successful upload to client, 0 for failed
  private int saveFile(DataOutputStream os,String filename) throws Exception
  {
    int success = 0; //assume upload failed
    
    //extracted filename is like /example.txt: cut out the backslash
    filename = filename.substring(1);    
    try 
    {
        //open up new connection on the side, downloads file, closes secondary socket
        String serverIP = "127.0.0.1";
        int serverPORT = 7000; 
        
        /* GET FILE FROM CLIENT VIA SECONDARY SOCKET */
        Socket sisterSocket = new Socket(serverIP, serverPORT);
        File file = new File(path + "\\" + filename); //create + open new file requested by client 
        FileOutputStream fos = new FileOutputStream(file); //to write to file
  
        //get server input stream reference
        DataInputStream dis = new DataInputStream(sisterSocket.getInputStream());
        int bytesRead; //amount of data put into buffer
        byte[] buffer = new byte[1024]; // to store incoming data       
        while ((bytesRead = dis.read(buffer,0,buffer.length))>0) //get input stream data & store in buffer
        {
          fos.write(buffer, 0, bytesRead); //write to file from buffer
        }   
        //close all streams & sister sockets
        sisterSocket.close();
        fos.close();        

        /* send response to client */
        String statusLine = "HTTP/1.1 201 Created" + CRLF;
        /*---construct header lines---*/
        String headerLines = "Server: AO:6879" + CRLF +
                             "User-Agent: NetBeans IDE/8.1" + CRLF +
                             "Content-Type: " + contentType(filename) + CRLF +
                             "Date: " + getHTTPTime() + CRLF + CRLF;
        //send status message
        os.writeBytes(statusLine);
        os.writeBytes(headerLines);
        
        success = 1; //change upload to successful
    }
    catch(Exception e)
    {
        //construct error message
        String statusLine = "HTTP/1.1 204 No Content" + CRLF;
        /*---construct header lines---*/
        String headerLines = "Server: AO:6879" + CRLF +
                             "User-Agent: NetBeans IDE/8.1" + CRLF +
                             "Content-Type: " + contentType(filename) + CRLF +
                             "Date: " + getHTTPTime() + CRLF + CRLF;
        //send status message
        os.writeBytes(statusLine);
        os.writeBytes(headerLines);
    }
    return success; //1 for successful upload, 0 for failed.
  }
  
  //returns time as string in http-format
  private static String getHTTPTime() {
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat dateFormat = new SimpleDateFormat(
        "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    return dateFormat.format(calendar.getTime());
  } 

}//end HttpReq Class


