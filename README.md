# Multi-Threaded-Web-Server-Client
A multithreaded java HTTP web server allowing file uploads and downloads to/from multiple clients.

Server: always-on multithreaded server, with a Java Swing GUI to display client request messages and currently connected clients. The Clear button simply clears the text area without affecting any connection states. User will be prompted to select the location of the server folder by entering a file path into the popup input dialog.

![Server GUI SnapShot](https://github.com/aoyshi/Multi-Threaded-Web-Server-Client/blob/master/serverScreenshot.png)

Client: simple JavaFX GUI that spawns fully-functioning, independent, identical client GUI windows, each as its own thread. This spawned client GUI first asks the user for a client directory path via an input dialog popup, then enables functions such as connect, view server file list, download/upload files from/to server (files will be taken from and downloaded to the above-mentioned client directory path specified by the user at the beginning), and disconnect/close the window anytime. A space in the bottom part of the GUI is reserved for optional inspection of server http response messages via a textArea that can be expanded and hidden anytime with the "Show" or "Hide" button.

![Client GUI SnapShot](https://github.com/aoyshi/Multi-Threaded-Web-Server-Client/blob/master/clientGUIScreenshot.png)

------------------------------------------------------------------------------

![Client GUI SnapShot](https://github.com/aoyshi/Multi-Threaded-Web-Server-Client/blob/master/clientInputDialogPic.png)

Connection: The socket connection established is keep-alive, so the user may download and upload as many times as desired until the client is explicitly disconnected.

# System/Tools Used: 

OS: Windows 10

IDE: NetBeans 8.2

Language: Java 8.0

GUI: JavaFX 8.0, Swing

# Limitations:

1. The usual [X] windows-default close button at the top right corner of the client window has been intentionally disabled via code, so that the user cannot exit out of the client while he is still connected to the server, without closing all the streams first. The only way to close the client window is through the Close button.
2. The client and server utilize TWO different ports, one for general communication of response and request (port 6789), and another for sending and receiving files (port 7000). The two port numbers are hardcoded into the program and the user cannot change it from the GUI.
3. The program runs locally only as of now. The localhost ip address ("127.0.0.1") is hardcoded into the client program for connecting to the server.


# Future work:
1. Make the client and server run over a real network with actual IP addresses, and not just locally. This would allow the programs to run on separate end systems. A possible implementation would be to ask for the server IP address from the client before setting up a connection. Consequently, the server - upon connecting - should display its IP address so the user can input that information into the client program for connection setup.
2. Make port numbers flexible so user can select it via the GUI.

# How to run:
1. Create packages named myServer and myMasterClient on NetBeans (8.1 or higher), and copy paste the .java and .fxml files into respective packages.
OR
2. Run the .jar files as standalone GUIs.
