import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

//Server class 
public class Server {
	

	// thread count to limit number of clients at a time by fixing thread pool
	static int maxThreads = 5;
    static ExecutorService pool = Executors.newFixedThreadPool(maxThreads);
    
	public static void main(String[] abc) throws Exception
    {
		
		// to open server for receiving connections on port 5056
        ServerSocket server = new ServerSocket(5056);
        System.out.println("\n-----Server is ready to receive client requests on port 5056-----\n");
        
        // to keep a number for each client request 
     	int clientNo = 0;
        
        // while loop for the server awaiting client requests when started
     	while (true) { 
     		Socket soc = null; 
     			
     		try { 
	     		// socket object to receive incoming client requests 
	     		soc = server.accept(); 
	     				
	     		// update client number after each successful connection is established
	     		clientNo++;
	      
	     		// display message at server console that a client is connected
	     		System.out.println("Client number "+clientNo+" is connected from "+soc.getInetAddress()+
	     				" using port "+soc.getPort());
	     				
	     		// ServerFTP object to assign a new thread connection to the client
	     		ServerFTP thread = new ServerFTP(soc); 
	
	     		// Invoking the ServerFTP class methods to start communication 
	     		pool.execute(thread); 
     				
     		} 
     		
     		// close resources if error or malfunction
     		catch (Exception e){ 
     			soc.close(); 
				server.close();
     		}
     	} // end while
    } // end main
} // end server class


// ServerFTP class
// runnable interface implemented to create threads and rewrite run() method 
class ServerFTP implements Runnable
{

	// declaring variables for input output streams, socket and pool 
	Socket cs;
    DataInputStream in;
    DataOutputStream out;
    ExecutorService pool;
    
    // Constructor method to connect client socket to server socket
    public ServerFTP(Socket skt) {
    	
        try {
        	// attach socket to server's socket
            this.cs = skt; 
            // obtaining input and output streams
            this.in = new DataInputStream(cs.getInputStream());
            this.out = new DataOutputStream(cs.getOutputStream());
        }
        
        catch(Exception o) {
        	System.out.println();
        }        
    } // constructor end
    
    
    /* run method overridden for threaded client server communication 
    after successful authentication*/
    public void run() { 
    	
    	/* code inside while loop is responsible for client authentication 
    	and avoid attacks on server from forbidden clients */
    	while(true) {
    		
    		try {
    			
				String user = in.readUTF();
				String pwd = in.readUTF();
				
				// users is a text file that contains user details for allowed users
				File file = new File("F:\\users.txt");
				FileInputStream fin = new FileInputStream(file);
				DataInputStream din = new DataInputStream(fin);
				
				String str1 = "";
				boolean check = false;
				str1 = din.readLine();
				
				// match user details with the users who are allowed to access 
				while(true) {
					if(str1 == "") 
						break;
					
					String str2 = din.readLine();
					
					if(str1.equals(user) && str2.equals(pwd)) {
						check = true;
						break;
					}
					// break if authentication failed
					else if(!str1.equals(user) || !str2.equals(pwd)) {
						break;
					}
					str1 = din.readLine();
				}
				
				// display message if authentication completed and continue 
				if(check == true){
					out.writeUTF("Authentication successful, start transferring files");
					break;
				}
				
				// display message if authentication failed
				else {
					out.writeUTF("Authentication failed, Try again");
				}
			} // end try
    		
    		catch (IOException p) {
				// Auto-generated catch block
				p.printStackTrace();
			}
    	} // end while
    	
    	// run client commands after successful authentication
    	while(true) {
    		
            try {
            	// reads menu option entered by client to perform actions accordingly 
	            String opt = in.readUTF();
	            
	            // sent from client
	            if(opt.equalsIgnoreCase("upload")) {
	                Receive();
	                continue;
	            }
	            
	            // received from client
	            else if(opt.equalsIgnoreCase("download")) {             
	            	Send();
	                continue;
	            }
	            
	            else if(opt.equalsIgnoreCase("browsing list")) {           
	                Browse();
	                continue;
	            }
	            
	            else if(opt.equalsIgnoreCase("Create Directory")) {
	            	CreateDirectory();
	            	continue;
	            }
	            
	            else if(opt.equalsIgnoreCase("Delete")) {
	            	Delete();
	            	continue; 	
	            }
	            
	            else if(opt.equalsIgnoreCase("Exit")) {
	            	System.out.println("\n-----Closing connection of client using port "+cs.getPort()+"-----\n");
	            	break;
	            }
            }
            
            catch(Exception x) {
            	System.out.println();
            }
        } // end while
    } // end of run() method override  
    
    
    // method for sending files to client
    void Send() throws Exception { 
    	
    	String path = in.readUTF();
    	// creating file type variable of the path given by user
        File file = new File(path);
        
        // display a message if file does not exist
        if(!file.exists()) {
            out.writeUTF("This file is not available on server");
            return;
        }
        
        // send file if it exists
        else {
            out.writeUTF("Sending file, Please wait!");
            FileInputStream finp = new FileInputStream(file);
            
            int data;
            // read data from file and send it using data output stream
            do {
                data = finp.read();
                out.writeUTF(String.valueOf(data));
            }
            while(data!= -1); 
            
            // close resource and display a message when file sent 
            finp.close();    
            out.writeUTF("File has been sent successfully to client");                            
        }
    } // end of send method
    
    
    // method for receiving files at client side
    void Receive() throws Exception {
 
        String path = in.readUTF();
        
        // return if file not found
        if(path.equalsIgnoreCase("File not found")) {
            return;
        }
        // creating file type variable of the path given by user
        File file = new File(path);
        
        // do not receive file if it exists on the server
        if(file.exists()) {
            out.writeUTF("This file already exists on server");
            return;
        }
        
        // receive file if it does not already exist
        else {
            out.writeUTF("Receiving file, Please wait!");
            FileOutputStream fout = new FileOutputStream(file);
            
            int data;
            String temp;
            // read data from file and send it using data output stream
            do {
            	temp = in.readUTF();
            	data = Integer.parseInt(temp);
            	
            	if(data!= -1) {
            		fout.write(data);                    
            	}
            }
            while(data!= -1);
            
            // close resource and display a message when file received
            fout.close();
            out.writeUTF("File has been received successfully from client"); 
        }     
    } // end of receive method
    
    
    // method for browsing files from server directory 
    void Browse() throws Exception {
    	
        String path = in.readUTF();
        
        if(path.equalsIgnoreCase("back")) {
        	 out.writeUTF("Initial Point");
        	 return;
        }
        
        // creating file type variable of the path given by user
        File file = new File(path);
        
        if(!file.exists()) {
        	out.writeUTF("directory doesn't exist");
        	return;
        }
        
        // list of all files
        ArrayList<String> files = new ArrayList<String>();
        // list of all directories
		ArrayList<String> directories = new ArrayList<String>();
		
		File[] folder = file.listFiles();

	    for (int x = 0; x<folder.length; x++) {
	    	
	      if (folder[x].isFile()) {
	        files.add(folder[x].getName());
	      }
	      
	      else if (folder[x].isDirectory()) {
	    	  directories.add(folder[x].getName());
	      }
	    } // end for
	    
	    String text = "Directories:\n";
	    for(int y = 0; y<directories.size(); y++) {
	    	text = text + "" + (y+1) + ":" + directories.get(y) + "\n";
	    } // end for
	    
	    text = text+"\n\nFiles\n";
	    for(int z = 0; z<files.size() ;z++) {
	    	text = text + "" + (z+1) + ":" + files.get(z) + "\n";
	    } // end for
	    
	    out.writeUTF(text);
	    return;
    } // end of browse method
    
    
    // method for creating directory at client side
    void CreateDirectory() throws Exception {
    	
    	String path = in.readUTF();
    	
    	// creating file type variable of the path given by user
    	File file = new File(path);

    	// to check if the directory exists
 	   	if(!file.exists()) {
 	   		
	    	// make directory if it does not already exist
 	   		if(file.mkdir()) {
	 	   		out.writeUTF("Directory has been created");
	 	   	}
 	   		// display a message if directory creation fails
	 	   	else {
	 	   		out.writeUTF("There was a problem creating this directory");
	 	   	}
 	   	}
 	   	
 	   	// display a message if directory already exits 
 	   	else {
 	   		out.writeUTF("This directory already exists here");
 	   	}
 	   	return;
    
    } // end of directory creation method
    
    
    // method for deleting a file from client side
    void Delete() throws Exception {
    	
    	String path = in.readUTF();
    	// creating file type variable of the path given by user
    	File file = new File(path);
    	
    	// to check if the file exists
    	if(file.exists()) {
    		
    		// deleting the file if it exists
    		if(file.delete()) {
    			out.writeUTF("File has been deleted");
    		}
    		
    		// display a message if the file deletion fails
    		else {
    			out.writeUTF("There was a problem while deleting this file");
    		}
    	}
    	// display a message if the file does not exist
    	else {
    		out.writeUTF("File does not exist");
    	}
    	return;   	
    } // end of delete method  
} // end of ServerFTP class



