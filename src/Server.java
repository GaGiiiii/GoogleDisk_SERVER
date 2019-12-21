import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Server {
	
	public static int counter = 0;
	
	public static void main(String[] args) {
		
		String jdbcURL = "jdbc:mysql://remotemysql.com:3306/ZtXJnzLCul";
		String username = "ZtXJnzLCul";
		String password = "HLeKu4znve";
		Connection dbConnection = null;
		
//		String jdbcURL = "jdbc:mysql://localhost:3306/rmt	";
//		String username = "root";
//		String password = "";
//		Connection dbConnection = null;
		
		try {
			dbConnection = DriverManager.getConnection(jdbcURL, username, password);
		
			if(dbConnection != null) {						
				System.out.println("Database connected successfully.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Database connection failed.");
		}
		
		try {
			@SuppressWarnings("resource")
			ServerSocket serverSocket = new ServerSocket(6666);
			Socket socketForCommunication = null;
			
			while(true) {
				System.out.println("Waiting for connection...");
				socketForCommunication = serverSocket.accept();
				
				System.out.println("Clients connected: " + ++counter);
				
				ServersThreadsForClients newThreadForClient = new ServersThreadsForClients(socketForCommunication, dbConnection);
				newThreadForClient.start();
			}
		} catch (IOException e) {
			System.out.println("Socket Error | Server Class.");
		}
	}
}