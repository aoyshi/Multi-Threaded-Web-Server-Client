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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class myMasterClientController {
  
  /** spawns client gui as individual thread **/
  @FXML
  private void onStartNewClient(ActionEvent event) {
    
      try 
      {
        Parent root = FXMLLoader.load(getClass().getResource("myClient.fxml")); 
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
        
        //dont let user close window without disconnecting cleanly
        stage.setOnCloseRequest(e -> 
        {
            Alert alert = new Alert(AlertType.INFORMATION); //alert window popup
            alert.setTitle("Important Instruction");
            alert.setContentText("Press the \"Close\" button inside Client GUI to close client window");
            alert.showAndWait();
            e.consume(); 
        });
        
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      
      
    
  }
}
