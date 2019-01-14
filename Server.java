import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Server extends JFrame{
	
	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private ServerSocket server;
	private Socket connection;
	
	//constructor/ We create gui here
	public Server()
	{
		super("Instant Messenger");        	//This is title
		userText= new JTextField();			
		userText.setEditable(false);			//Initially empty until connection is setup
		userText.addActionListener(			//What happens on Enter
				new ActionListener(){
					public void actionPerformed(ActionEvent event){
						sendMessage(event.getActionCommand());			//This returns the message typed
						userText.setText("");							//Again empty for next message
					}
				}
			);
		
		add(userText,BorderLayout.NORTH);	//Sits on top of screen
		chatWindow= new JTextArea();
		chatWindow.setEditable(false);
		add(new JScrollPane(chatWindow));	//Chat window is scrollable
		setSize(300,300);
		setVisible(true);
	}
	
	//setting up the server
	public void startServer()
	{
		try{
			server = new ServerSocket(6789,100);		//Port no, Waiting queue
			while(true)
			{
				try{
					//connect & have conversation
					waitForConnection();
					setupStreams();
					whileChatting();
				}catch(EOFException eofexception){
					showMessage("\n Server ended the connection! ");
				}finally{
					closeCrap();
				}
			}
		}catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	
	//wait for connection, then display connection information
	private void waitForConnection() throws IOException
	{
		showMessage(" Waiting for someone to connect... \n");
		connection = server.accept();
		showMessage(" Now connected to " + connection.getInetAddress().getHostName());		//IP address
	}
	
	//get stream to send and receive data
	private void setupStreams() throws IOException 
	{
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
		showMessage("\n Streams are setup! \n");
	}
	
	//during the chat conversation
	public void whileChatting() throws IOException
	{
		String message = "You are now Connected! ";
		sendMessage(message);
		ableToType(true);
		do
		{
			try{
				message =(String) input.readObject();
				showMessage("\n"+ message);
			}catch(ClassNotFoundException classNtFoundException){
				showMessage("\n User sent invalid data!");
			}
		}while(!message.equals("Client - END"));
				
	}
	
	//close streams and socket after you are done chatting
	private void closeCrap() 
	{
		showMessage("\n Closing Connections... \n");
		ableToType(false);
		try{
			output.close();
			input.close();					//close streams
			connection.close();				//close server
		}catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	
	private void sendMessage(String message)
	{
		try{
			output.writeObject("Server - " + message);
			output.flush();
			showMessage("\nServer - " + message);
		}catch(IOException ioException){
			chatWindow.append("\n ERROR: MESSAGE NOT SENT!");
		}
	}
	
	//updates chatWindow
	private void showMessage(final String text)
	{
		SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){
						chatWindow.append(text);
					}
				}
		);
	}
	
	//let the user type their stuff to the box
	private void ableToType(final boolean tof)
	{
		SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){
						userText.setEditable(tof);
					}
				}
		);
	}
}
