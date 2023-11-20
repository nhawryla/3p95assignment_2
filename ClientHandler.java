import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.Inflater;

public class ClientHandler implements Runnable{

	private final String password = "hello";
	private final Socket client;
	private final String path;
	
	ClientHandler(Socket socket, String path){
		
		this.client = socket;
		this.path = path;
	}
	
	public static boolean integrityCheck(long passedSize, long currentSize) {//checks the checksum value to ensure data was not lost or changed
        
		if (passedSize == currentSize){
			
			return true;
		} else {
			
			return false;
		}
	}

	public static String decompress(byte[] data) throws Exception {//decompresses the byte array into a string
		byte[] result = new byte[data.length * 3];
		Inflater inflater = new Inflater();
		inflater.setInput(data);
		int resultLength = inflater.inflate(result);
		inflater.end();
		return new String(result, 0, resultLength, "UTF-8");
	}

	public static String decryption(String i, int k, String name) {//decrypts the text
		char[] input = i.toCharArray();
		String output = "";
		boolean o1 = false;
		boolean o2 = false;
		boolean o3 = false;

		for (int x = 0; x < input.length; x++) {
			if (input[x] < 127 && input[x] > 32) {
//				if (o1 == false) {
//					o1 = true;
//					System.out.println(name + ": input[x] < 127 && input[x] > 32");
//				}
				if ((input[x] - k) < 33) {
//					if (o2 == false) {
//						o2 = true;
//						System.out.println(name + ": (input[x] - k) < 33");
//					}
					input[x] = (char) (input[x] + 126 - 32 - k);
				}
				else {
//					if (o3 == false) {
//						o3 = true;
//						System.out.println(name + ": (input[x] - k) >= 33");
//					}
					input[x] = (char) (input[x] - k);
				}
			}
			output += input[x];
		}
		return output;
	}
	
	public void run(){


		
		DataInputStream dataInputStream = null;
		DataOutputStream dataOutputStream = null;
		
		try {
			dataInputStream = new DataInputStream(client.getInputStream());
			dataOutputStream = new DataOutputStream(client.getOutputStream());

			long enter = dataInputStream.readLong();
			String name = dataInputStream.readUTF();
			long size = dataInputStream.readLong();
			long fileSize = dataInputStream.readLong();

			System.out.println("printing file : " + name);
			
			byte[] buffer = new byte[(int) fileSize];
			dataInputStream.read(buffer);

			String text = decompress(buffer);
			text = decryption(text, 2, name);
			byte[] sum = text.getBytes();
			Checksum crc32 = new CRC32();
			crc32.update(sum, 0, sum.length);
			long length = crc32.getValue();
			
			if (integrityCheck(size,length)){
				
				System.out.println("File size is correct for file : " + name);
			} else {
				
				System.out.println("File size is incorrect for file : " + name);
			}
			
			FileWriter myWriter = new FileWriter(path+name);
		    myWriter.write(text);
		    myWriter.close();

			long leave = System.currentTimeMillis();

			System.out.println("File data : " + name + " ||  Transfer start time : " + enter + "     Transfer end time : " + leave + "     Total time for completion : " + (leave-enter));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try { 
                if (dataOutputStream != null) { 
                	dataOutputStream.close(); 
                } 
                if (dataInputStream != null) { 
                	dataInputStream.close(); 
                    client.close(); 
                } 
            } 
            catch (IOException e) { 
                e.printStackTrace(); 
            } 
        }
	}
}

