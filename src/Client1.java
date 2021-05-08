
import java.net.*;
import java.io.*;
import java.util.*;

public class Client1 {
	
		// main method to run client and connect to server on port 5056 
	    public static void main(String[] abc) throws Exception {
	    	
	        Socket client = new Socket("localhost",5056);
	        Client1 client1 = new Client1(client);
	        client1.displayMenu();
	        
	    }
	    
		String directory = "";
		//reads text from character input stream
		BufferedReader bufferReader;
		// Allows to read data(primitive type) from socket input stream
	    DataInputStream in; 
	    //Allows to send data(primitive type) to socket output stream
	    DataOutputStream out; 
	    // socket for connection
	    Socket soc;
	    int securityFlag;
	    
	    // constructor to create socket object and initialize streams
	    public Client1(Socket sockett) {
	        
	    	try {
	            this.soc = sockett;
	            this.in = new DataInputStream(soc.getInputStream());
	            this.out = new DataOutputStream(soc.getOutputStream());
	            
	            //inputstreamReader reads from byte stream and converts them into character streams.
	            bufferReader = new BufferedReader(new InputStreamReader(System.in));
	        }
	    	
	        catch(Exception ex) {
	        	System.out.println();
	        }        
	    } // constructor end
	    
	    
	 // method to display menu and login
	    public void displayMenu() throws Exception {
	        
	    	while(true) {
	    		// read user name entered by client for authentication
	    		System.out.println("Enter username for authentication: ");
	    		String user = bufferReader.readLine();
	    		out.writeUTF(user); //write using modern utf-8
	    		
	    		// read password entered by client for authentication
	    		System.out.println("Enter password: ");
	    		String pd = bufferReader.readLine();
	    		out.writeUTF(pd);
	    		
	    		String reply = in.readUTF();
	    		System.out.println(reply);
	    		
	    		// verified when user confirms
	    		if(reply.equalsIgnoreCase("Authentication successful, start transferring files")) 
	    			break;
	    	}
	    	
	    	while(true) {   
	    		// display menu options to client on console
	            System.out.println("pwd: "+ directory +"\n----MENU----");
	            System.out.println("1. Send File \n2. Receive File \n3. Browse \n4. Back \n5. Create Directory"
	            		+ "\n6. Delete File \n7. Exit");
	            System.out.print("\nEnter Choice : ");
	            
	            // read user's choice from console
	            int opt = Integer.parseInt(bufferReader.readLine());
	            
	            if(opt == 1)
	            {
	                out.writeUTF("upload");
	                Send();
	            }
	            
	            else if(opt == 2) {
	                out.writeUTF("download");
	                Receive();
	            }
	            
	            else if(opt == 3) {
	                out.writeUTF("browsing list");
	                Browse();
	            }
	            
	            else if(opt == 4) {
	                out.writeUTF("browsing list");
	                Back();
	            }
	            
	            else if(opt == 5) {
	            	out.writeUTF("create directory");
	            	CreateDir();
	            }
	            
	            else if(opt == 6) {
	            	out.writeUTF("delete");
	            	Delete();	
	            }
	            
	            else {
	                out.writeUTF("exit");
	                // display closed connection message when client write exit
	                System.out.println("\n-----Connection closed with server-----\n");
	                System.exit(1);
	            }
	        } //end while
	    } // end method displayMenu
	   
	    
	    // method to send files
	    void Send() throws Exception {        
	        
	        String fname, newfname;
	        fname = selectFile();
	        
	        if(securityFlag==1) {
	        	return ;
	        }
	        
	        
	        File file = new File(fname);
	        
	        // don't crash if file does not exist just display a message and return
	        if(!file.exists())
	        {
	            System.out.println("File does not exist at client side");
	            out.writeUTF("File not found");
	            return;
	        }
	        
	        System.out.print("To save file enter filename along with path and extension: "); 
	        newfname = bufferReader.readLine();
	        out.writeUTF(newfname);
	        
	        // can't send if already exists
	        String msg = in.readUTF();
	        if(msg.equalsIgnoreCase("This file already exists on server"))
	        {
	            System.out.println(msg);
	            return;
	        }
	        
	        // tell client that file is sending
	        System.out.println("Sending file, Please wait!");
	        FileInputStream fileInput = new FileInputStream(file);
	        int data;
	        do
	        {
	            data = fileInput.read();
	            out.writeUTF(String.valueOf(data));
	        }
	        
	        //reading file
	        while(data != -1);
	        fileInput.close();
	        System.out.println(in.readUTF());
	        
	    } // end of send method
	    
	    
	    
	    //select file method
	    String selectFile() throws Exception { 
	    	
	       securityFlag = 0;
	       String currentdir,directory = "";
		   System.out.println("Enter the path: ");
		   currentdir = bufferReader.readLine();
		 
		   //securing directory so that system files are not transferred
		   if(currentdir.contains("C") || currentdir.contains("c")) {
				System.out.println("System files directory access not allowed"); //for system securities
				currentdir = ""; 
				securityFlag = 1;
				return "";
		   } // end if
		   
		   while(true){
			 
			   if(directory.equalsIgnoreCase(""))
				   directory = currentdir;
			   else 
				   directory = directory+currentdir+"\\";
		       
			   File folder = new File(directory);
			   
			   /*<> will help compiler while verifying instead of run time
			     File storing string values of Array list*/
		       ArrayList<String> listfiles = new ArrayList<String>(); 
		       ArrayList<String> listdirectories = new ArrayList<String>();
				
		       /*folder has path of the directory, for file operations it should be of File type
		       list of files is storing files and directory */
		       File[] listOfFiles = folder.listFiles();

			    for (int i = 0; i < listOfFiles.length; i++) {
			      if (listOfFiles[i].isFile()) {
			        listfiles.add(listOfFiles[i].getName());
			      } 
			      else if (listOfFiles[i].isDirectory()) {
			    	 listdirectories.add(listOfFiles[i].getName());
			      }
			    } // end for
			    
			    System.out.println("Directories:");
			    for(int i=0;i<listdirectories.size();i++){
			    	//for array list we have to use get() to get an element at specified index
			    	System.out.println("" + (i+1) + ":"+ listdirectories.get(i));
			    	
			    } //end for
			    
			    System.out.println("\n\nFiles");
			    for(int i=0;i<listfiles.size();i++){
			    	System.out.println(""+ (i+1) + ":" + listfiles.get(i));
			    } // end for
			    
			    System.out.println("\n\n\n1.Select file\n2.Enter Subdirectory");
			    int choice;
	            choice=Integer.parseInt(bufferReader.readLine());
	            
	            if(choice == 1)  System.out.print("File: ");
	            else  System.out.print("Subdirectory: ");
	            
	            currentdir = bufferReader.readLine();
	            
	            if(choice == 1){
	            	directory=directory+currentdir;
	            	break;
	            }
		   } // end while
		   return directory;
		   
	   } // end select file method 
	   
	    
	    
	    // method for receiving files 
	    void Receive() throws Exception {
	    	
	        String fname;
	        System.out.print("Enter the name of file to receive:");
	        fname = bufferReader.readLine();
	        out.writeUTF(directory+fname);
	        
	        String msg = in.readUTF();
	        // can't receive if not available
	        if(msg.equalsIgnoreCase("This file is not available on server")) {
	            System.out.println(msg);
	            return;
	        }
	        
	        else if(msg.equalsIgnoreCase("Sending file, Please wait!")) {
	        	// get path where file should be received
	            System.out.println("Enter the path where you want to save file: ");
	            String path = bufferReader.readLine();
	            path = path+"\\"+fname;
	        	
	            File file = new File(path);
	            if(file.exists()) {
	            	System.out.println("File already exists at client side");
	            	return;
	            }
	            System.out.println("Receiving file, Please wait!");
	            
	            FileOutputStream fileoutput = new FileOutputStream(file);
	            int data;
	            String temp;
	            do {
	                temp = in.readUTF();
	                data = Integer.parseInt(temp);
	                if(data!= -1) {
	                    fileoutput.write(data);                    
	                }
	            }
	            while(data!= -1);
	            fileoutput.close();
	            System.out.println(in.readUTF());     
	        }
	    } // end of receive method
	    
	    // method to browse directories from a list
	    void Browse() throws Exception {
	        
	    	String dire;
	    	// get name of directory to browse
	        System.out.print("Enter name of directory to browse: ");
	        dire = bufferReader.readLine();
	        
	        if(dire.contains("C") || dire.contains("c")) {
	        	
	        	//securing directory so that system files are not transferred
	        	System.out.println("System directory access is not allowed"); 
	        	directory = dire;
	        	Back();
	        	return;
	        }
	        
	        out.writeUTF(directory+dire);
	        String msg = in.readUTF();
	        
	        if(msg.equalsIgnoreCase("directory doesn't exist"))  {
	            System.out.println(msg);
	            return;
	        }
	        
	        else {
	        	if(directory.equals(""))
	        		directory = dire;
	        	else 
	        		directory = directory+ dire+"\\";
	        	System.out.println(msg);
	        }
	    } // end of browsing method 
	    
	    
	    // method to go back to initial point in directory 
	    void Back() throws Exception {
	    	
	    	if(directory.endsWith(":\\")) {
	    		directory = "";
	    		out.writeUTF("Back");
	    	}
	    	
	    	else if(directory.endsWith("")) {
	    		directory = "";
	    		out.writeUTF("Back");
	    	}
	    	
	    	else {
	    		int sI = directory.lastIndexOf('\\');
	    		System.out.println("ind: "+sI);
	    		
	            char arr[]= new char[1500];
	            
	            //copies characters from a given string into destination character array
	            directory.getChars(0,directory.length(), arr, 0);
	            arr[sI] = '?';
	            String str = new String(arr,0,sI);
	            sI = str.lastIndexOf('\\');
	            System.out.println("ind: "+sI);
	            
	            str.getChars(0, sI-1, arr, 0);
	            directory = new String(arr, 0, sI+1);
	            System.out.println(directory);
	            
	            out.writeUTF(directory);
	            
	    	}
	    	
	    	String msg = in.readUTF();
	    	// can't go back if directory not found
	    	if(msg.equalsIgnoreCase("File not found")) {
	    		System.out.println("File not found on server");
	    		return;
	    	}
	            
	    	else {	
	    		System.out.println(msg);
	    	}
	    } //end of back directory method
	    
	    
	    
	    // method for creating directory
	    void CreateDir() throws Exception {
	    	
	    	String dir = "";
	    	// get path from client where new directory should come
	    	System.out.println("Enter path: ");
	    	dir = bufferReader.readLine();
	        out.writeUTF(dir);
	        
	        String msg = in.readUTF();
	        
	        if(msg.equalsIgnoreCase("There was a problem creating this directory")) {
	        	System.out.println(msg);
	        	return;
	        }
	        // can't create if already exists
	        else if(msg.equalsIgnoreCase("This directory already exists here")) {
	        	System.out.println(msg);
	        	return;
	        }
	        // on successful creation
	        else {
	        	System.out.println("Directory has been created");
	        }
	    } //end of create directory method
	    
	    // method for deleting a file
	    void Delete() throws Exception{
	    	 
	    	 String fname; 
	    	 // get name of file to be deleted
	    	 fname = selectFile();
	    	 
	    	 if(securityFlag == 1) {
	         	return ;
	    	 }
	    	 // give name to server
	    	 out.writeUTF(fname);
	    	 
	    	 String msg = in.readUTF();
	    	 // can't delete if it does not exist
	    	 if(msg.equalsIgnoreCase("File does not exist")) {
	    		System.out.println(msg);
	         	return;
	    	 }
	    	 // if problem deleting
	    	 else if (msg.equalsIgnoreCase("There was a problem while deleting this file")) {
	    		 System.out.println(msg);
	    		 return;
	    	 }
	    	 // successful deletion 
	    	 else {
	    		 System.out.println("File has been deleted");
	    	 }     	
	    } //end of delete method
	 
} // Client1 class end


