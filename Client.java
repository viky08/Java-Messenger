import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Client extends JFrame{
	
	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private String message = "";
	private String serverIP;
	private Socket connection;
	
	//constructor/ We create gui here
	public Client(String host)
	{
		super("Instant Messenger - Client");        	//This is title
		serverIP=host;
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
	
	//connect to server
	public void startRunning()
	{
		try{
			connectToServer();
			setupStreams();
			whileChatting();
		}catch(EOFException eofException){
			showMessage("\n Client terminated connection");
		}catch(IOException ioException){
			ioException.printStackTrace();
		}finally{
			closeCrap();
		}
	}
	
	//connect to server
	private void connectToServer() throws IOException
	{
		showMessage("Attempting connection... \n");
		connection = new Socket(InetAddress.getByName(serverIP),6789);
		showMessage("Conneced to: "+ connection.getInetAddress().getHostName());
	}
	
	//setup Streams to send and receive messages 
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
		ableToType(true);
		do
		{
			try{
				message =(String) input.readObject();
				showMessage("\n"+ message);
			}catch(ClassNotFoundException classNtFoundException){
				showMessage("\n User sent invalid data!");
			}
		}while(!message.equals("Server - END"));
					
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
			output.writeObject("Client - " + message);
			output.flush();
			showMessage("Client - " + message);
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
