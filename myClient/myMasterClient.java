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

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class myMasterClient extends Application {
 
  /**starts up master client gui window **/
  @Override
  public void start(Stage stage) throws Exception {
    Parent root = FXMLLoader.load(getClass().getResource("myMasterClient.fxml"));
      
    Scene scene = new Scene(root);
    
    stage.setScene(scene);
    stage.show();
    
  }
  
  public static void main(String[] args) {
    launch(args);
  }
    
}
