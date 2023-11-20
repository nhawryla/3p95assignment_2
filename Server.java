

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
	
	private final String storagePath = "./write/";
	
	private ServerSocket server; 
	private static Socket clientSocket = null;
	
	public void init() throws Exception{
		
		server = new ServerSocket(8080);
		System.out.println("System running");
		
		while(true){
			
			clientSocket = server.accept();
			
			ClientHandler clienthandler = new ClientHandler(clientSocket, storagePath);
			new Thread(clienthandler).start();
		}
	}
	
	public void close(){
		
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
