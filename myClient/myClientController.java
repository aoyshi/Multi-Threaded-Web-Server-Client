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

package myMasterClient;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Thread.sleep;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TimeZone;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

public class myClientController implements Initializable {

    /**get references to fxml objects**/
    @FXML private TextArea logDisplay;
    @FXML private TextArea serverLog;
    @FXML private Label clientLabel;
    @FXML private Button connect;
    @FXML private Button disconnect;
    @FXML private Button viewBtn;
    @FXML private Button uploadBtn;
    @FXML private Button downloadBtn;
    @FXML private Button showBtn;
    @FXML private Button hideBtn;
    @FXML private Button closeBtn;
    @FXML private Label serverLogLabel;
    
    private final String CRLF = "\r\n"; //terminate output stream (http)
    private final byte[] STOP = "\rSTOP".getBytes(); //to terminate end of file transfer without closing socket
    private String clientName = ""; //stores name of current thread client
    private String directoryPath = ""; //store directory of client entered my user
    private boolean connected = false; //server-connected status
    
    String serverAddress = "127.0.0.1"; //local-host ip address
    int port = 6789; //also specified in server class

    Socket connectionSocket = null; //for connecting client to server
    DataOutputStream out = null;  //for sending messages to server
    BufferedReader in = null; //for reading messages from server 
 
    //javafx required default method
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }    
  
  //takes file path as input and returns true if path leads to valid directory
  private boolean checkPath(String path) {
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
  
//if press connect button, connect to server socket-port  
  @FXML
  private void connectEvent(ActionEvent event) {
    //dialog popup asks user for directory file path
      TextInputDialog dialog = new TextInputDialog();
      dialog.setTitle("User Input Dialog");
      dialog.setHeaderText("To get path of a folder: \n1. Press Shift+Right click on desired folder\n2. Click \"Copy as path\" from the drop-down menu\n3. Click on input field below and Ctrl+V to paste path into field\n4. Delete quotation marks from the file path before clicking OK");
      dialog.setContentText("Enter path of client directory: ");
    //Traditional way to get the response value.
      Optional<String> result = dialog.showAndWait();
      boolean userEnteredInput = result.isPresent(); //returns true if value entered 
      boolean isValidPath = false;
      if (userEnteredInput) //user entered something
      {
        directoryPath = result.get(); //get user entry
        if(directoryPath.length() == 0) //check if empty string 
           logDisplay.appendText("> No input detected. Click Connect to try again\n");
         else  //invalid path, display error msg
         {             
            isValidPath = checkPath(directoryPath); //check if path valid
            if(!isValidPath) //display error msg if invalid
               logDisplay.appendText("> "+ directoryPath +" is not a valid directory path. Click Connect to try again\n");           
         } 
      }
      //continue only if entered file path leads to valid directory  
      if(isValidPath) 
      {
        logDisplay.appendText("> Directory Path: "+directoryPath+"\n"); //display chosen path 
        logDisplay.appendText("----------------------------------------------\n");
        try   
        { //connect to server 
          connectionSocket = new Socket(serverAddress,port); 
          //get reference to socket output stream
          out = new DataOutputStream(connectionSocket.getOutputStream());
          //wrap reference to socket input stream with filters
          in = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
          this.clientName = generateRandomName(); //give random name to current client

          //create and display success message on dialogue display
          logDisplay.appendText("> Successfully connected to server.\n> Your username is "+clientName+".\n");
          clientLabel.setText("Client: " + clientName); //change label of window to given name

          connect.setDisable(true); //disable connect button so that same client cant connect again

          //tell server your username//
          logDisplay.appendText("\n********** USERNAME REGISTRATION **********\n");
          sendClientRequest("POST", "<SUBMIT-USERNAME>", out); //send client request to server
          out.writeBytes(clientName); //send username in message body 
          out.writeBytes(CRLF); //denotes end of data to server
          out.writeBytes(CRLF);

          serverLog.appendText("> Server Response for USERNAME REGISTRATION: \n");          
          int code = getServerCode(in); //get server response msg
          serverLog.appendText("--------------------------------------------\n");
                    
          if(code==201)
          {
            logDisplay.appendText("> Username successfully registered at server.\n");
            connected = true;
          }
          else if(code==204)
          {
            logDisplay.appendText("> Error registering username at server.\n");                 
          }

          //enable functional buttons
          disconnect.setDisable(false);
          viewBtn.setDisable(false);
          uploadBtn.setDisable(false);
          downloadBtn.setDisable(false);
        } 
        catch (Exception e) //connection not successful
        {
          //create and display success message on dialogue display
          logDisplay.appendText("> Could not connect to server.\n");
        }  
      }
    }
    
  //create a random username for the current client
  private String generateRandomName() {
        String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int random1 = (int)(Math.random()*52); //generates random number between 0 and 51
        int random2 = (int) (Math.random()*52);
        int random3 = (int) (Math.random()*52);
        int random4 = (int) (Math.random()*100); //random number between 0 and 99
        //three letters & a numerical at the end
        String username = "" + letters.charAt(random1)+letters.charAt(random2)+letters.charAt(random3)+random4;
        return username;
    }
 
  //if view button pressed, send client request to view files, et and display server file list
  @FXML
  private void onViewEvent(ActionEvent event) throws Exception 
  {
        logDisplay.appendText("\n********** VIEW FILES **********");  
        sendClientRequest("GET", "File-List", out); //send http-get request to server 
                
        serverLog.appendText("> Server Response for VIEW FILE REQUEST: \n");   
        int statusCode = getServerCode(in); //reads/prints server status+headers and returns integer server status code
        serverLog.appendText("--------------------------------------------\n");
        
        if(statusCode==200) //request OK
        {
          String body = "\n> "; //concat temp & to display on log
          String temp = ""; //temporarily hold incoming data
          while((temp = in.readLine()).length()!=0)  //read message body
          {
            body += temp + "\n"; //cosntruct string containing file list
          } 
          body += "\n";
          logDisplay.appendText(body); //display file list on gui
        }
  }
   
  //if upload button pressed, send client post message and upload file, get & display server response
  @FXML
  private void onUploadEvent(ActionEvent event) throws Exception
    {         
        logDisplay.appendText("\n********** FILE UPLOAD **********\n");
        String clientFileList = getClientFileList()+"\n"; //get client file list
        logDisplay.appendText(clientFileList);
        String filenameUpload = "";
        //dialog popup asks user for filename 
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Text Input Dialog");
        dialog.setContentText("Enter name of file to upload (with extensions)");
        dialog.setHeaderText(clientFileList); 
        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        boolean userEnteredInput = result.isPresent(); //returns true if value entered 
        if (userEnteredInput) //user entered something
        {
          filenameUpload = result.get(); //get & store value entered
          
          if(filenameUpload.length() != 0) //check if input if empty
          {//check if file exists in client (append ./ for filePATH)
            logDisplay.appendText(">You requested to upload file '"+filenameUpload+"'\n");
            boolean fileExists = checkFileExists(filenameUpload);
            if(fileExists)
            {
              //-----below lines executed only if file exists
              sendClientRequest("POST",filenameUpload,out); //send http POST request to server                           
              /*---send file to server in mesage body---*/
              sendFile(filenameUpload,out);
              
              serverLog.appendText("> Server Response for UPLOAD REQUEST: \n");               
              int code = getServerCode(in); //read server response msg & extract status code
              serverLog.appendText("--------------------------------------------\n");
              
              if(code==201) //content created successfully
              {
                logDisplay.appendText("> Upload Complete!\n");
              }
              else if(code==204) //server failed to create content
              {
               logDisplay.appendText("> Error uploading file to server.\n");

              }     
            }
            else {
                logDisplay.appendText("> File '"+ filenameUpload + "' does not exist in client. Try again\n");
            }
          }
          else 
          {
            logDisplay.appendText("> No filename entered.\n"); //user exited input dialog            
          }
        }
        
    }
  
//auxiliary method to print most-recent server file list to client gui when client selects download-option  
  private String getServerFileList(DataOutputStream out) throws Exception{
      String fileList = "";
      sendClientRequest("GET", "File-List", out); //silently request server for current file list
      
      String temp = "";//temporarily hold incoming data
      while((temp = in.readLine()).length()!= 0) 
      {
         //do nothing
         //read past server status line & header lines to get to file list
      } 
      //now read/store file list data
      temp="";
      while((temp = in.readLine()).length()!=0)  //read message body
      {
        fileList += temp + "\n"; //cosntruct string containing file list
      } 
      fileList += "\n";
      
      return fileList;
  }
  
  //checks if given input string "filename" exists, returns true if file exists, false otherwise
  private boolean checkFileExists(String filename) {
        boolean fileExists = true;
        filename = directoryPath + "\\" +filename; //path to file in specific dir
        FileInputStream fis = null;
        try //check if file exists
        {
          fis = new FileInputStream(filename); //throws exception if file DNE
        }
        catch (Exception e) 
        {
          fileExists = false;
        }
        return fileExists;     
    }
 
  //upon pressing download button, get most recent server file list, send client get request, download file, print server response.
  @FXML
  private void onDownloadEvent(ActionEvent event) throws Exception {
        String serverFileList = getServerFileList(out);
        logDisplay.appendText("\n********** FILE DOWNLOAD **********\n");
        logDisplay.appendText(serverFileList);
        String filename = "";
        //dialog popup asks user for filename 
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Text Input Dialog");
        dialog.setContentText("Enter name of file to download (with extensions)");
        dialog.setHeaderText(serverFileList);
        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        boolean userEnteredInput = result.isPresent(); //returns true if value entered 
        if (userEnteredInput) //user entered something
        {
            filename = result.get();
            if(filename.length() != 0) //user input isnt empty
            {
                logDisplay.appendText("> You requested to download file '"+filename+"'\n");
                sendClientRequest("GET",filename, out); //send http request for file
                
                serverLog.appendText("> Server Response for DOWNLOAD REQUEST: \n");               
                int serverStatusCode = getServerCode(in); //gets & displays server response message 
                serverLog.appendText("--------------------------------------------\n");
     
                if(serverStatusCode==200) //if file exists, download it
                {
                   logDisplay.appendText("> File found on server. Downloading...\n");
                   getFile(filename); 
                   logDisplay.appendText("> Download complete!\n");
                } 
                else if(serverStatusCode==404) //file not found
                {
                  logDisplay.appendText("> The requested file '"+filename+"' does not exist on the server.\n");             
                }      
            }       
            else 
            {
               logDisplay.appendText("> No filename entered.\n"); //user exited input dialog            
            }
        }
   }
  
  //opens up new connection on the side, downloads file, closes secondary socket
  private void getFile(String filename) throws Exception {
        String serverIP = "127.0.0.1";
        int serverPORT = 7000; 
        
        Socket sisterSocket = new Socket(serverIP, serverPORT);
        File file = new File(directoryPath + "\\" + filename); //create + open new file requested by client in desried folder
        FileOutputStream fos = new FileOutputStream(file); //to write to file
  
        //get server input stream reference
        DataInputStream dis = new DataInputStream(sisterSocket.getInputStream());
        BufferedReader in = new BufferedReader(new InputStreamReader(sisterSocket.getInputStream()));
        int bytesRead; //amount of data put into buffer
        byte[] buffer = new byte[1024]; // to store incoming data       
        while ((bytesRead = dis.read(buffer,0,buffer.length))>0) //get input stream data & store in buffer
        {
          fos.write(buffer, 0, bytesRead); //write to file from buffer
        }   
        //close all streams & sister sockets
        sisterSocket.close();
        fos.close();        
  } 
  
  /* make server message log visible if click show */
  @FXML
  private void onShowEvent(ActionEvent event){ 
      serverLog.setVisible(true);
      serverLogLabel.setVisible(false);
      showBtn.setDisable(true);
      hideBtn.setDisable(false);
  }
  
  /* hide server message log if click hide */
  @FXML
  private void onHideEvent(ActionEvent event){ 
      serverLog.setVisible(false);
      serverLogLabel.setVisible(true);
      showBtn.setDisable(false);
      hideBtn.setDisable(true);
  }
  
  //disables buttons, closes streams/sockets if user chooses to disconnect
  @FXML
  private void disconnectEvent(ActionEvent event){      
        try {
           //notify server about disconnect
           out.writeBytes("</DISCONNECT>");
           out.flush();
           sleep(500); //give server some time to say bye (& not throw exception)
           //close all streams and socket
           out.close();
           in.close();
           connectionSocket.close(); 
                     
           //disable functional buttons          
            disconnect.setDisable(true);    
            viewBtn.setDisable(true);
            uploadBtn.setDisable(true);
            downloadBtn.setDisable(true);
         
            connect.setDisable(false); //enable connect button again
                        
            //clear out dialogue box and server details box
            logDisplay.setText("");
            serverLog.setText("");
            
            //reset generic client label on top
            clientLabel.setText("Client");
        }
         catch(Exception e) {
           //create error message on dialogue display
           logDisplay.appendText("\n> Could not disconnect.\n");
         }
        finally
        {
            connected = false; //no longer connected to server
        }
    }
    
  //this method returns a string containing all available filenames in the main client directory
  private String getClientFileList() {
      File clientDir = new File(directoryPath); //get path to client directory
      File[] listOfFiles = clientDir.listFiles(); //get list of files in dir
      String fileList = "Available files (subfolders are in parentheses): \n";
      
      for (File file : listOfFiles)  //loop through list of files
      {
        if (file.isFile()) //if file, simple print filename
           fileList += file.getName()+"  ";        
        
        else if (file.isDirectory()) //if directory, wrap in parentheses first
           fileList += "("+file.getName()+")  ";        
      }      
      fileList +="\n"; //new line for cleanliness
      
      return fileList; 
  }
  
//sends http client requests to server via outputstream, tailored to specific methodType(GET,POST) and filename("View-Files" in case of file list retrieval)  
  private void sendClientRequest(String methodType, String filename, DataOutputStream out) throws Exception{
      //encode request in HTTP
      String requestLine = methodType + " /"+filename+" HTTP/1.1" +CRLF;
      String contentType = "";
      
      if(filename.equals("File-List")) //if user requesting server file list
        contentType = "text/plain";
      else
        contentType = getContentType(filename); //get file type based on extension
    
      String contentLength = "";
      if(methodType.equals("POST")) //put contentlength in headers only if method is POST
      {
        if(filename.equals("<SUBMIT-USERNAME>")) //user is uploading username
        {
           contentLength = "Content-Length: " + clientName.length() + CRLF;  
        }
        else //user is uploading file
        {
           File file = new File(directoryPath + "\\" + filename);  
           contentLength = "Content-Length: " + file.length() + CRLF; //get file size in bytes
        }       
      }
      //construct e-tags
      String headerLines = "Client-Name: " + this.clientName + CRLF +
                           "Host: AO:6879" + CRLF +
                           "User-Agent:  NetBeans IDE/8.1" + CRLF +
                           "Content-Type: " + contentType + CRLF +
                           contentLength + //"Content-Length: " + contentLength + CRLF + : include this line if method is post
                           "Date: " + getHTTPTime() + CRLF + CRLF;
      //send request to server
      out.writeBytes(requestLine);
      out.writeBytes(headerLines);
  }
  
  //prints server status+headers and returns integer server status code
  private int getServerCode(BufferedReader in) throws Exception {
      String statusLine = in.readLine() + "\n";             
      String statusHeaderLines = "";
      String temp = "";
      while((temp = in.readLine()).length()!= 0) //read server response msg headers
      {
        statusHeaderLines += temp + "\n";
      } 
      serverLog.appendText(statusLine); //display server status msg
      serverLog.appendText(statusHeaderLines); //display header lines
      
      String[] parts = statusLine.split(" "); //split status message along whitespaces
      String statusCode = parts[1]; //numerical status code
      int code=0;
      try 
      {
        code = Integer.parseInt(statusCode);
      }
      catch(Exception e)
      {
        logDisplay.appendText("> Error reading server response code.\n");
      }
      return code;
    }  
  
  //This method return file type by parsing the provided filename
  private static String getContentType(String filename)
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
  
  //returns time as string in http-format
  private static String getHTTPTime() {
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat dateFormat = new SimpleDateFormat(
        "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    return dateFormat.format(calendar.getTime());
  } 
  
  //takes filename as input, opens sister sockets and sends that requested filename to server for upload
  private void sendFile(String filename, DataOutputStream os) throws Exception {
    //Prepend a directoryPath and \ so that file path is within the selected directory
    filename = directoryPath + "\\" + filename; 
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
    
    //send file if exists
    if (fileExists) {
       try 
       {    /*  SEND FILE TO SERVER  */
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
        }
        catch (IOException e) 
        {
          logDisplay.appendText("> Error uploading file to client.\n");
        }
        finally
        {
           if(fis!=null)
             fis.close();
        }
    } 
    else
    {
        logDisplay.appendText("> File does not exist in client directory.\n");
    }
  }
  
  //if user wants to close client window, do housekeeping (close streams, sockets, notify server) and close
  @FXML
  private void onCloseEvent(ActionEvent event) throws Exception {
      if(connected)  //if user didnt disconnect, safely disconnect befoe closing window
      {
          //notify server about disconnect
           out.writeBytes("</DISCONNECT>");
           out.flush();
           sleep(500); //give server some time to say bye (& not throw exception)
           //close all streams and socket
           out.close();
           in.close();
           connectionSocket.close();      
      }
      // get a handle to the stage & close window
      Stage stage = (Stage) closeBtn.getScene().getWindow();
      stage.close();     
  }
}//end main class

 
