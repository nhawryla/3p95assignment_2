

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

public class Client {

	private final static String loadPath = "./load/"; // File path where the files are loaded from
	private final String password = "hello"; // encryption key
	
	private Socket socket;
	private static DataOutputStream dataOutputStream = null;
    private static DataInputStream dataInputStream = null;
	
	public void init() throws Exception{
		
		socket = new Socket("127.0.0.1", 8080); // connect to host on local port
		
		dataInputStream = new DataInputStream(socket.getInputStream()); // sets up output and input connections to server
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
		
		System.out.println("Connected to server");
	}
	
	public void sendFile(String filePath) throws Exception{ // main method for sending files
		
		dataOutputStream.writeLong(System.currentTimeMillis()); // writes to the server the time of launching the method

		File file = new File(filePath); // gets file and file path 
		Path path = Paths.get(filePath);
		
		System.out.println("Printing file " + file.getName());

		//String contents = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
		byte[] contents = Files.readAllBytes(path); // creates a byte array containing all bytes of the given file
		
		String text = new String(contents, StandardCharsets.UTF_8); // turns the byte array into its string form
		byte[] sum = text.getBytes();
		Checksum crc32 = new CRC32();
		crc32.update(sum, 0, sum.length);//also important
		long length = crc32.getValue();//THIS LINE
		
		dataOutputStream.writeUTF(file.getName()); // send file name to server
		dataOutputStream.writeLong(length); 
		dataOutputStream.writeLong(file.length()); // sends file length
		text = encryption(text, 2, file.getName()); // ecnrypts text
		contents = compress(text); //compresses text
		//encrypt compress text
		dataOutputStream.write(contents); // writes the contents to the server
		
		//dataOutputStream.write((encrypt(text,password)).getBytes());
		
		//String encrypted = encrypt(text,password);
		//dataOutputStream.write(encrypted.getBytes());
		
		//System.out.println(encrypted);
		
	}
	
	public void close(){ // when the client runs into an error safely closes down the ports
		
		if(socket != null){
			
			try{
				socket.close();
			}catch (IOException e){
				
				e.printStackTrace();
			}
		}
	}

	public static String encryption(String i, int k, String name) {//encrypts the data, has a purposeful bug commented out for question 2 testing
		char[] input = i.toCharArray();
		String output = "";
		boolean o1 = false;
		boolean o2 = false;
		boolean o3 = false;
		boolean o4 = false;

		for (int x = 0; x < input.length; x++) {

			if (input[x] == 'X') {//this is a bug for question 2, for now it operates as usual but for getting the error, swap the input[x] assignment with the commented one
//				if (o1 == false) {
//					o1 = true;
//					System.out.println(name + ": input[x] == X");
//				}
//				//input[x] = (char)(input[x] - 1);
				input[x] = (char) (input[x] + k);
			}
			else if (input[x] < 127-k && input[x] > 32) {
//				if (o2 == false) {
//					o2 = true;
//					System.out.println(name + ": input[x] < 127-k && input[x] > 32");
//				}
				input[x] = (char) (input[x] + k);
			}
			else if (input[x] < 127 && input[x] > 32) {
//				if (o3 == false) {
//					o3 = true;
//					System.out.println(name + ": input[x] < 127 && input[x] > 32");
//				}
				input[x] = (char) (input[x] - 126 + 32 + k);
			}
			else {
//				if (o4 == false) {
//					o4 = true;
//					System.out.println(name + ": input[x] >= 127 || input[x] <= 32");
//				}
			}
			output += input[x];
		}
		return output;
	}

	public static byte[] compress(String in) {//compresses the data into a byte array
		byte[] input = in.getBytes();
		byte[] output = new byte[input.length];

		Deflater deflater = new Deflater();
		deflater.setInput(input);
		deflater.finish();
		deflater.deflate(output);

		return output;
	}

	public static void main(String[] args) throws IOException{
		
		File path = new File(loadPath); // takes in all of the files in the specified path
		File[] files = path.listFiles();

		for (int z = 0; z < files.length; z++){ // loops through for each file a new client is made and connected that way threads can handle the files seperately 

			Client client = new Client();
			
			try {

				client.init(); // initiates the new client and sends the next file in line to the server
				client.sendFile(loadPath + files[z].getName());
				
			} catch (Exception e){
			
				client.close();
			}
		}
		
		dataInputStream.close();
        dataInputStream.close();
	}
}
