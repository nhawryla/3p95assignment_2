

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Server{
	
	private final String storagePath = "./write/"; // storage path of where the threads will write to
	
	private ServerSocket server; 
	private static Socket clientSocket = null;
	
	public void init() throws Exception{
		
		server = new ServerSocket(8080); // starts server on port 8080
		System.out.println("System running");
		
		while(true){
			
			clientSocket = server.accept(); // when a new client connects this will catch it and link the client to the client socket variable
			
			ClientHandler clienthandler = new ClientHandler(clientSocket, storagePath); // a new thread is made with the just entered client and it handles it (does all of the file features)
			new Thread(clienthandler).start();
		}
	}
	
	public void close(){ // safely closes the server
		
		if(server != null){
			
			try{
				System.out.println("Closing server");
				server.close();
			}catch (IOException e){
				
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) throws IOException{
		
		Server server = new Server(); 
		
		try{
			
			server.init();
		} catch (Exception e){
			
			server.close();
            clientSocket.close();
		}
	}
}
