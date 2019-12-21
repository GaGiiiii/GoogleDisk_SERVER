import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ServersThreadsForClients extends Thread {

	/* ****** COMMUNICATION SERVER / CLIENT / DB ****** */
	
	private BufferedReader inputFromClient;
	private PrintStream outputForClient;
	private Socket socketForCommunication;
	private Connection connection;

	/* ****** USER DATA ****** */
	
	private int id;
	private String username;
	@SuppressWarnings("unused")
	private String password;
	private boolean prem = false;
	
	
	/* ****** CONSTRUCTOR ****** */

	public ServersThreadsForClients(Socket socketForCommunication, Connection dbConnection) {
		this.socketForCommunication = socketForCommunication;
		this.connection = dbConnection;
	}
	
	/* ****** MENUS ****** */

	private void menu() {
		String choice = null;

		this.outputForClient.println("****** MENU ******"
				+ "\n\n1. Register"
				+ "\n2. Login" 
				+ "\n3. Enter Shared Link"
				+ "\nTo Exit Application Type \"--exit\" At Any Time." + "\n\nYour choice: ");

		try {
			if(!socketForCommunication.isClosed()) {
				choice = inputFromClient.readLine();
			}
			
			if(checkIfInputIsExit(choice)) {
				return;
			}
			
			if(checkIfInputIsCancel(choice)) {
				outputForClient.println();
				menu();
				return;
			}
			
		} catch (IOException e) {
			System.err.println("Client Input Error In Menu.");
		}
		
		if(choice != null) {
			switch (choice) {
				case "1":
					register();
					break;
				case "2":
					login();
					break;
				case "3":
					displayDiskWithSharedLink();
					menu();
					break;
				default:
					this.outputForClient.println("Invalid Choice.\n");
					menu();
			}
		}
	}
	
	private void loggedInMenu() {
		String choice = null;

		if (prem == true) {
			this.outputForClient.println("\nName: \"" + this.username + "\" | Premium: Yes | ID: " + this.id + "\n");
			this.outputForClient.println("****** MENU ******"
					+ "\n\n1. List My Files"
					+ "\n2. Upload File"
					+ "\n3. Manage Folders / Files"
					+ "\n4. Generate Shareable Link"
					+ "\n5. Share Disk With User"
					+ "\n6. Shared Disks"
					+ "\n7. Download File"
					+ "\n8. Logout"
					+ "\nTo Exit Application Type \"--exit\" At Any Time." + "\n\nYour choice: ");
		} else {
			this.outputForClient.println("\nName: \"" + this.username + "\" | Premium: No | ID: " + this.id + "\n");
			this.outputForClient.println("****** MENU ******"
					+ "\n\n1. List My Files"
					+ "\n2. Upload File"
					+ "\n3. Generate Shareable Link"
					+ "\n4. Share Disk With User"
					+ "\n5. Shared Disks"
					+ "\n6. Download File"
					+ "\n7. Logout"
					+ "\nTo Exit Application Type \"--exit\" At Any Time."
					+ "\n\nYour choice: ");
		}
		
		try {
			
			if(socketForCommunication != null &&  !socketForCommunication.isClosed()) {
				choice = inputFromClient.readLine();
			}
			
			if(checkIfInputIsExit(choice)) {
				return;
			}
			
			if(checkIfInputIsCancel(choice)) {
				outputForClient.println();
				loggedInMenu();
				return;
			}
		} catch (IOException e) {
//			e.printStackTrace();
			System.err.println("Client Input Error Logged In Menu.");
		}
		
		if(choice != null) {
			switch (choice) {
				case "1":
					outputForClient.println("\n**********************************************");
					outputForClient.println("\"" + this.username + "\" here are your files:");
					outputForClient.println("**********************************************\n");
					listFiles(this.username, 0, -1);
					outputForClient.println("\n**********************************************");
					outputForClient.println("END");
					outputForClient.println("**********************************************");
					loggedInMenu();
					break;
				case "2":
					uploadFile();
					loggedInMenu();
					break;
				case "3":
					if(prem) {
						fileFolderManagementMenu();
					}else {
						outputForClient.println("\nYour Shareable Link Is: " + generateLink());
					}
					
					loggedInMenu();
					break;
				case "4":
					if(prem) {
						outputForClient.println("\nYour Shareable Link Is: " + generateLink());
					}else {
						shareWithUser();
					}
					
					loggedInMenu();
					break;
				case "5":
					if(prem) {
						shareWithUser();
					}else {
						sharedDisks();
					}
					
					loggedInMenu();
					break;
				case "6":
					if(prem) {
						sharedDisks();
					}else {
						downloadFile();
					}
					
					loggedInMenu();
					break;
				case "7":
					if(prem) {
						downloadFile();
						loggedInMenu();
					}else {
						logout();
					}
	
					break;
				case "8":
					if(prem) {
						logout();
					}else {
						this.outputForClient.println("\nInvalid Choice");
						loggedInMenu();
					}
					
					break;
				case "--exit":
					closeCommunication();
					break;
				default:
					this.outputForClient.println("\nInvalid Choice");
					loggedInMenu();
			}
		}
	}
	
	private void fileFolderManagementMenu() {
		String choice = null;

		this.outputForClient.println("\nName: \"" + this.username + "\" | Premium: Yes | ID: " + this.id + "\n");

		this.outputForClient.println("****** MENU ******"
				+ "\n\n1. List My Files"
				+ "\n2. Create Folder"
				+ "\n3. Rename Folder"
				+ "\n4. Move File / Folder"
				+ "\n5. Delete Folder"
				+ "\n6. Go Back"
				+ "\n7. Logout"
				+ "\nTo Exit Application Type \"--exit\" At Any Time."
				+ "\n\nYour Choice: ");

		try {
			
			if(!socketForCommunication.isClosed()) {
				choice = inputFromClient.readLine();
			}
			
			if(checkIfInputIsExit(choice)) {
				return;
			}
			
			if(checkIfInputIsCancel(choice)) {
				outputForClient.println();
				loggedInMenu();
				return;
			}
			
		} catch (IOException e) {
//			e.printStackTrace();
			System.err.println("Input Error File Managment Menu.");
		}

	if(choice != null) {	
		switch (choice) {
			case "1":
				outputForClient.println("\n**********************************************");
				outputForClient.println("\"" + this.username + "\" here are your files:");
				outputForClient.println("**********************************************\n");
				listFiles(this.username, 0, -1);
				outputForClient.println("\n**********************************************");
				outputForClient.println("END");
				outputForClient.println("**********************************************");
				fileFolderManagementMenu();
				break;
			case "2":
				createFolder();
				fileFolderManagementMenu();
				break;
			case "3":
				renameFolder();
				fileFolderManagementMenu();
				break;
			case "4":
				moveFileFolder();
				fileFolderManagementMenu();
				break;
			case "5":
				deleteFolder();
				fileFolderManagementMenu();
				break;
			case "6":
				return;
			case "7":
				logout();
				break;
			default:
				this.outputForClient.println("Invalid Choice!\n");
				fileFolderManagementMenu();
			}
		}
	}
	
	/* ****** AUTHORIZATION ****** */
	
	private void register() {
		String username;
		String password;
		String isPremium;
		String shareLink;
		boolean isValid = false;
		
		try {
			this.outputForClient.println("\n*** REGISTER ***");

			do {
				this.outputForClient.println("\nEnter Username:");
				username = inputFromClient.readLine();
				
				if(checkIfInputIsExit(username)) {
					return;
				}
				
				if(checkIfInputIsCancel(username)) {
					outputForClient.println();
					menu();
					return;
				}
				
				if (checkIfUserExistsDB(username)) {
					this.outputForClient.println("\nUsername already exist.");
				} else {
					isValid = true;
				}
			} while (!isValid);


			do {
				this.outputForClient.println("\nEnter Password (min 6 chars): ");
				password = inputFromClient.readLine();
				
				if(checkIfInputIsExit(password)) {
					return;
				}
				
				if(checkIfInputIsCancel(password)) {
					outputForClient.println();
					menu();
					return;
				}
				
				if(checkIfInputIsExit(password)) {
					return;
				}
				
			} while (password.length() < 6);
			

			while (true) {
				this.outputForClient.println("\nWould you like to buy premium membership ($10 per month) [Yes / No]: ");
				String premiumChoice = inputFromClient.readLine();
				
				if(checkIfInputIsExit(premiumChoice)) {
					return;
				}
				
				if(checkIfInputIsCancel(premiumChoice)) {
					outputForClient.println();
					menu();
					return;
				}
				

				if (premiumChoice.equalsIgnoreCase("yes")) {
					this.prem = true;
					isPremium = "true";
					break;
				} else if (premiumChoice.equalsIgnoreCase("no")) {
					this.prem = false;
					isPremium = "false";
					break;
				} else {
					this.outputForClient.println("Wrong format, please enter [Yes / No]");
				}
			}
			
			
			this.username = username;
			this.password = password;
			shareLink = generateLink();
			
			this.outputForClient.println(
					"\nREGISTERED SUCCESSFULLY WITH:" + "\nUsername: " + username + " | Password: " + password + " | Premium: " + prem + " | Link: " + shareLink + "\n");
			insertUserDB(username, password, isPremium, shareLink);

			this.id = getUserIdDB(username);

			loggedInMenu();
		} catch (IOException e) {
			System.err.println("Error while getting input from client in register.");
//			e.printStackTrace();
		}
		
	}
	
	private void login() {
		
		String username;
		String password;
		String dbPassword = null;
		String dbPrem = null;
		boolean isValid = false;
		boolean usernameExists = false;

		
		try {
			String sql = "SELECT username, password, isPremium FROM users";
			Statement statement;
			statement = connection.createStatement();
			
			ResultSet result;
			
			this.outputForClient.println("\n*** LOGIN ***");

			do {
				this.outputForClient.println("\nEnter Username:");
				username = inputFromClient.readLine();
				
				if(checkIfInputIsExit(username)) {
					return;
				}
				
				if(checkIfInputIsCancel(username)) {
					outputForClient.println();
					menu();
					return;
				}
				
				usernameExists = false;
				
				result = statement.executeQuery(sql);
				
				while(result.next()) {
					String dbUsername = result.getString(1);
										
					if (dbUsername.equals(username)) {
						usernameExists = true;
						dbPassword = result.getString(2);
						dbPrem = result.getString(3);
						
						break;
					}
				}			

				if (usernameExists) {
					isValid = true;
				} else {
					this.outputForClient.println("\nUsername doesn't exist.");
				}
			} while (!isValid);


			isValid = false;

			do {
				this.outputForClient.println("\nEnter Password:");
				password = inputFromClient.readLine();
				
				if(checkIfInputIsExit(password)) {
					return;
				}
				
				if(checkIfInputIsCancel(password)) {
					outputForClient.println();
					menu();
					return;
				}
				

				if (dbPassword != null && dbPassword.equals(password)) {
					isValid = true;
				} else {
					this.outputForClient.println("\nWrong password.");
				}
			} while (!isValid);

			
			if(dbPrem != null && dbPrem.equals("true")) {
				this.prem = true;
			}else {
				this.prem = false;
			}
						
			this.id = getUserIdDB(username);
			this.username = username;
			this.password = password;

			loggedInMenu();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.err.println("SQL Error | login.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.err.println("Input Error In Login.");
		}
		
	}

	private void logout() {
		this.id = -1;
		this.username = null;
		this.password = null;
		this.prem = false;
		this.outputForClient.println("\nLOGGED OUT SUCCESSFULLY!\n");
		
		menu();
	}

	/* ****** DISK SHARING ****** */

	private void displayDiskWithSharedLink() {
		/* gagiDISK_LINK */
		String username = null;
		String link = null;
		String choice = null;
		boolean isValid = false;
		boolean usernameExists = false;
		
		try {
			do {
				outputForClient.println("\nEnter Link: ");
				link = inputFromClient.readLine();
				
				if(checkIfInputIsExit(link)) {
					return;
				}
				
				if(checkIfInputIsCancel(link)) {
					outputForClient.println();
					menu();
					return;
				}
				 
				if (!link.contains("DISK_LINK")) {
					outputForClient.println("\nWrong Format.");
				} else {
					username = link.substring(0, link.indexOf("DISK_LINK"));
					usernameExists = checkIfUserExistsDB(username);
					
					if(!usernameExists) {
						outputForClient.println("\nUser Doesn't Exist.");
					}else {
						isValid = true;
					}
				}

			} while (!isValid);

			
			outputForClient.println("\n**********************************************");
			outputForClient.println("\"" + username + "'s\"  files:");
			outputForClient.println("**********************************************\n");
			listFiles(username, 0, -1);
			outputForClient.println("\n**********************************************");
			outputForClient.println("END");
			outputForClient.println("**********************************************\n");
			
			outputForClient.println("Would You Like To Download File [ Yes / No ]: ");
			choice = inputFromClient.readLine();
			
			if(checkIfInputIsExit(choice)) {
				return;
			}
			
			if(checkIfInputIsCancel(choice)) {
				outputForClient.println();
				menu();
				
				return;
			}
			
			if(choice != null && choice.equalsIgnoreCase("YES")) {
				downloadFile();
				outputForClient.println();
			}

			outputForClient.println();
		} catch (IOException e) {
//			e.printStackTrace();
			System.err.println("Shared Link Display Error");
		}

	}

	private String generateLink() {
		return this.username + "DISK_LINK";
	}

	private void sharedDisks() {
		try {
			ResultSet allUsers = getUsersWhoSharedDisksToCurrentUserDB();
			String username;
			while(allUsers.next()) {
//				SVAKAKO CE IZLISTATI I AKO JE PRAZAN DISK OD OVOG STO JE SHAREOVAO
				username = getUserUsernameDB(allUsers.getInt(1));
				outputForClient.println("\n**********************************************");
				outputForClient.println("\"" + username + "'s\" Files:");
				outputForClient.println("**********************************************\n");
				listFiles(username, 0, -1);
				outputForClient.println("\n**********************************************");
				outputForClient.println("END");
				outputForClient.println("**********************************************");
			}
			
			// Da li biste zeleli da menjate neciji disk ? Pokupis username i onda novi meni gde moze download ili folder management
			// Sve metode sa folderima ce imati paremetar username za koji ce se raditi sa bazom, download moze samo da se pozove odmah.
			// Lako, ali mnogo posla...
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.err.println("Shared Disks SQL Error.");
		}

	}
	
	private void shareWithUser() {
		String username;
		boolean usernameExists = false;
		boolean isValid = false;
		try {
			do {
				outputForClient.println("\nEnter Username:");
				username = inputFromClient.readLine();
				
				if(checkIfInputIsExit(username)) {
					return;
				}
				
				if(checkIfInputIsCancel(username)) {
					outputForClient.println();
					loggedInMenu();
					return;
				}
				
				if(this.username.equals(username)) {
					outputForClient.println("\nYou Can't Share Disk To Yourself.");
					
					continue;
				}
				
				usernameExists = checkIfUserExistsDB(username);

				if (usernameExists) {
					// STA AKO MU JE VEC SEROVAO .... MOZE LAKO DA SE SREDI ALI IMA POSLA
					int user_id_from = getUserIdDB(this.username);
					int user_id_to =  getUserIdDB(username);
					insertSharesDB(user_id_from, user_id_to);
					isValid = true;
					outputForClient.println("\nSuccessfully Shared Disk With User: \"" + username + "\"");
				} else {
					outputForClient.println("\nUsername Doesn't Exist.");
				}
			} while (!isValid);

		} catch (IOException e) {
//			e.printStackTrace();
			System.err.println("Error While Entering Username To Share Your Disk");
		}
	}
	
	/* ****** FOLDER / FILES MANAGEMENT ****** */
	
	private void listFiles(String user, int level, int folder_id) {	
		
		try {
			
//			UZMI SVE FOLDERE ZA TOG USERA
			
			ResultSet result = userFoldersDB(user, level);

			
//			UZMI SVE FAJLOVE ZA TOG USERA
			
			ResultSet result2 = userFilesDB(user, level);
			
			if(!result.next() && !result2.next()) {
				return;
			}
			
			result.previous();
			result2.previous();

			while(result.next()) {

				if(folder_id == result.getInt(2)) {
					for(int i = 0; i < level; i++) {
						outputForClient.print("\t");
					}
					
					outputForClient.println("[ " + result.getString(1) + " ]");
					listFiles(user, level + 1, result.getInt(4));
				}
					
			}
			
			while(result2.next()) {

				if(folder_id == result2.getInt(2)) {
					for(int i = 0; i < level; i++) {
						outputForClient.print("\t");
					}
					
					outputForClient.println(result2.getString(1));
				}
			}	
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.err.println("DB Error | List files.");
		}		
		
	}

	private void createFolder() {
		String parentFolderName;
		String newFolderName;
		int folder_id;
		int level;
		boolean folderNameExistsV = false;
		boolean global = false;
		
		try {
			do {
				outputForClient.println("\nEnter Folder Name In Which You Want To Create New Folder"
						+ "\nIf You Want To Create File Globaly Type \"--global\"");
				parentFolderName = inputFromClient.readLine();
				
				if(checkIfInputIsExit(parentFolderName)) {
					return;
				}
				
				if(checkIfInputIsCancel(parentFolderName)) {
					outputForClient.println();
					fileFolderManagementMenu();
					return;
				}
				
				if(parentFolderName.equalsIgnoreCase("--global")) {
					global = true;
					
					break;
				}
				
				folderNameExistsV = folderNameExistsDB(parentFolderName);
				
				if(!folderNameExistsV) {
					outputForClient.println("\nFolder Doesn't Exist");
				}
			}while(!folderNameExistsV);
			
			folderNameExistsV = false;
			
			do {
				outputForClient.println("\nEnter New Folder Name: ");
				newFolderName = inputFromClient.readLine();
				
				if(checkIfInputIsExit(newFolderName)) {
					return;
				}
				
				if(checkIfInputIsCancel(newFolderName)) {
					outputForClient.println();
					fileFolderManagementMenu();
					return;
				}
				
				folderNameExistsV = folderNameExistsDB(newFolderName);
				
				if(folderNameExistsV) {
					outputForClient.println("\nFolder Already Exists");
				}
			}while(folderNameExistsV);
			
			if(global) {
				folder_id = -1;
				level = -1;
			}else {
				folder_id = findFolderIdFromNameDB(parentFolderName);
				level = findFolderLevelFromNameDB(parentFolderName);
			}

			
			insertFolderDB(this.id, folder_id, newFolderName, level + 1);
			
			outputForClient.println("\nFolder \"" + newFolderName + "\" Added Successfully.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			 e.printStackTrace();
			System.err.println("Input Error While Creating Folder.");
		}
		
	}
	
	private void renameFolder() {
		String folderToRename;
		String newFolderName;
		int id;
		boolean folderNameExistsV = false;
		
		try {
			do {
				outputForClient.println("\nEnter Folder Name Which You Want To Rename:");
				folderToRename = inputFromClient.readLine();
				
				if(checkIfInputIsExit(folderToRename)) {
					return;
				}
				
				if(checkIfInputIsCancel(folderToRename)) {
					outputForClient.println();
					fileFolderManagementMenu();
					return;
				}
				
				folderNameExistsV = folderNameExistsDB(folderToRename);
				
				if(!folderNameExistsV) {
					outputForClient.println("\nFolder Doesn't Exist");
				}
			}while(!folderNameExistsV);
			
			folderNameExistsV = false;
			
			do {
				outputForClient.println("\nEnter New Name For The Folder \"" + folderToRename + "\":");
				newFolderName = inputFromClient.readLine();
				
				if(checkIfInputIsExit(newFolderName)) {
					return;
				}
				
				if(checkIfInputIsCancel(newFolderName)) {
					outputForClient.println();
					fileFolderManagementMenu();
					return;
				}
				
				folderNameExistsV = folderNameExistsDB(newFolderName);
				
				if(folderNameExistsV) {
					outputForClient.println("\nFolder Already Exists");
				}
			}while(folderNameExistsV);
			
			id = findFolderIdFromNameDB(folderToRename);
			
			renameFolderDB(id, newFolderName);
			outputForClient.println("\nFolder \"" + folderToRename + "\" Renamed Successfully To \"" + newFolderName + "\"");

		} catch (IOException e) {
			// TODO Auto-generated catch block
//			 e.printStackTrace();
			System.err.println("Input Error When Renameing Folder.");
		}
		
	}
	
	private void moveFileFolder() {
		String folderToMove;
		String folderInWhichToMove;
		boolean folderNameExistsV = false;
		boolean isValid = false;
		boolean global = false;
		int newLevelOfMovedFolder;
		int newFolder_idOfMovedFolder;
		int idOfMovingFolder;
		int levelOfMovingFolder = -1;
		int levelOfFolderInWhichYouWantToMove = -1;
		
		try {
			do {
				outputForClient.println("\nEnter Folder Name Which You Want To Move:");
				folderToMove = inputFromClient.readLine();
				
				if(checkIfInputIsExit(folderToMove)) {
					return;
				}
				
				if(checkIfInputIsCancel(folderToMove)) {
					outputForClient.println();
					fileFolderManagementMenu();
					return;
				}
				
				folderNameExistsV = folderNameExistsDB(folderToMove);
				
				if(!folderNameExistsV) {
					outputForClient.println("\nFolder Doesn't Exist");
				}
			}while(!folderNameExistsV);
			
			folderNameExistsV = false;
			
			do {
				outputForClient.println("\nEnter Folder Name In Which You Want To Move Folder \"" + folderToMove + "\""
						+ "\nIf You Want To Move File To Global Level Type \"--global\"");
				folderInWhichToMove = inputFromClient.readLine();
				
				if(checkIfInputIsExit(folderInWhichToMove)) {
					return;
				}
				
				if(checkIfInputIsCancel(folderInWhichToMove)) {
					outputForClient.println();
					fileFolderManagementMenu();
					return;
				}
				
				if(folderInWhichToMove.equalsIgnoreCase("--global")) {
					global = true;
					
					break;
				}
				
				folderNameExistsV = folderNameExistsDB(folderInWhichToMove);
				
				if(!folderNameExistsV) {
					outputForClient.println("\nFolder Doesn't Exist");
					
					continue;
				}
				
				 levelOfMovingFolder = findFolderLevelFromNameDB(folderToMove);
				 levelOfFolderInWhichYouWantToMove = findFolderLevelFromNameDB(folderInWhichToMove);
				
				if(levelOfMovingFolder >= levelOfFolderInWhichYouWantToMove) {
					isValid = true;
				}else {
					if(!isChild(folderInWhichToMove, folderToMove)) {
						isValid = true;
					}else {
						outputForClient.println("\nNot Valid Action");
					}
				}
			}while(!isValid);
			
//			MORAM DA ZNAM KOJI FOLDER HOCU DA MOVEUJEM 
//			I KOD NJEGA MENJAM LEVEL I FOLDER_ID
//			LEVEL CE MI BITI KOD NJEGOVOR NOVOG RODITELJA 1 +
//			FOLDER_ID CE  MI BITI ID OD NOVOG RODITELJA
//			NA KRAJU MORAM DA PREBACIM SVE PODFOLDERE I FAJLOVE ZA MOVEOVAN FOLDER
			
			if(global) {
				newLevelOfMovedFolder = 0;
				newFolder_idOfMovedFolder = -1;
			}else {
				newLevelOfMovedFolder = findFolderLevelFromNameDB(folderInWhichToMove) + 1;
				newFolder_idOfMovedFolder = findFolderIdFromNameDB(folderInWhichToMove);
			}
			

			idOfMovingFolder = findFolderIdFromNameDB(folderToMove);
			
			moveFolderDB(idOfMovingFolder, newLevelOfMovedFolder, newFolder_idOfMovedFolder);
			moveSubfolders(idOfMovingFolder);
			moveSubfiles(idOfMovingFolder);
			outputForClient.println("\nFolder \"" + folderToMove + "\" Move Successfully To \"" + folderInWhichToMove + "\" Folder.");

		} catch (IOException e) {
			// TODO Auto-generated catch block
//			 e.printStackTrace();
			System.err.println("Input Error When Moving Files.");
		}
	}
	
	private boolean isChild(String childName, String parentName) {
		boolean isChild = false;
		
		try {
			int parentID = findFolderIdFromNameDB(parentName);
			ResultSet allFoldersForUser = userFoldersDB(this.username);

			while(allFoldersForUser.next()) {	
				if(allFoldersForUser.getInt(2) == parentID) {					
					if(allFoldersForUser.getString(1).equals(childName)) {
						isChild = true;
						
						break;
					}else {
						isChild = isChild(childName, allFoldersForUser.getString(1));
						
						if(isChild) {
							break;
						}
					}
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.err.println("Error While Getting ISChild.");
		}
		
		return isChild;
	}
	
	private void moveSubfolders(int idOfParentFolder) {
		try {
			ResultSet allFoldersForUser = userFoldersDB(this.username);
			
			while(allFoldersForUser.next()) {
				if(allFoldersForUser.getInt(2) == idOfParentFolder) {
//					ZNACI DA OVAJ FOLDER PRIPADA PARENTU TJ MOVEOVANOM FOLDERU UPDATEUJ MU LEVEL
					int parentLevel = findFolderLevelFromID(idOfParentFolder);
					updateFolderLevelDB(findFolderIdFromNameDB(allFoldersForUser.getString(1)), ++parentLevel);
					moveSubfiles(findFolderIdFromNameDB(allFoldersForUser.getString(1)));
					moveSubfolders(findFolderIdFromNameDB(allFoldersForUser.getString(1)));
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.err.println("SQL Error | moveSubfolders.");
		}
	}
	
	private void moveSubfiles(int idOfParentFolder) {
		try {
			ResultSet allFilesForUser = userFilesDB(this.username);
			
			while(allFilesForUser.next()) {
				if(allFilesForUser.getInt(2) == idOfParentFolder) {
//					ZNACI DA OVAJ FILE PRIPADA PARENTU TJ MOVEOVANOM FOLDERU UPDATEUJ MU LEVEL
					int parentLevel = findFolderLevelFromID(idOfParentFolder);
					updateFileLevelDB(findFileIdFromNameDB(allFilesForUser.getString(1)), ++parentLevel);
//					moveSubfiles(findFolderIdFromName(allFilesForUser.getString(1)));
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.err.println("SQL Error | moveSubfiles.");
		}
	}
	
	private void deleteFolder() {
		String folderToDelete;
		String choice;
		int id;
		boolean folderNameExistsV = false;
		boolean isEmpty = false;
		boolean isValid = false;
		
		try {
			do {
				outputForClient.println("\nEnter Folder Name Which You Want To Delete:");
				folderToDelete = inputFromClient.readLine();
				
				if(checkIfInputIsExit(folderToDelete)) {
					return;
				}
				
				if(checkIfInputIsCancel(folderToDelete)) {
					outputForClient.println();
					fileFolderManagementMenu();
					return;
				}
				
				folderNameExistsV = folderNameExistsDB(folderToDelete);
				
				if(!folderNameExistsV) {
					outputForClient.println("\nFolder Doesn't Exist.");
					
					continue;
				}
				
				id = findFolderIdFromNameDB(folderToDelete);
				isEmpty = isFolderEmptyDB(id);
				
				if(!isEmpty) {
					outputForClient.println("\nFolder Isn't Empty.");
				}
				
				if(isEmpty && folderNameExistsV) {
					isValid = true;
				}
			}while(!isValid);
			
			id = findFolderIdFromNameDB(folderToDelete);
			
			while(true) {
				outputForClient.println("Are You Sure That You Want To Delete Folder \"" + folderToDelete + "\" [ Y / N ] ?");
				choice = inputFromClient.readLine();
				
				if(checkIfInputIsExit(choice)) {
					return;
				}
				
				if(checkIfInputIsCancel(choice)) {
					outputForClient.println();
					fileFolderManagementMenu();
					return;
				}
				
				if(choice.equalsIgnoreCase("Y")) {
					deleteFolderDB(id);
					outputForClient.println("\nFolder \"" + folderToDelete + "\" Deleted Successfully.");
					
					break;
				}else if(choice.equalsIgnoreCase("N")) {
					return;
				}
			}		

		} catch (IOException e) {
			// TODO Auto-generated catch block
//			 e.printStackTrace();
			System.err.println("Error When Deleteing Folders.");
		}
	}

	/* ****** UPLOAD ****** */
	/* MNOGO JE BOLJE DA CUVAMO PUTANJE U BAZI I RADIMO SA FILE SISTEMIMA I CUVAMO FAJL NA SERVERU A U BAZI SAMO PUTANJU !!! DEKI STOJIMIROVIC :D */

	private void uploadFile() { // moze sa istim imenom, srediti da ne moze, par linija koda
		int uploadedFilesNumber = 0;
		InputStream in = null;
		
		if(!this.prem) {
			uploadedFilesNumber = numberOfUploadsDB(this.id);
			if(uploadedFilesNumber > 4) {
				outputForClient.println("\nNon Premium Users Can Upload Up To 5 Files.");
				
				return;
			}
		}
		
		//	UPLOAD FILE HERE
		
		try {
			String path;
			String name;
			String parentFolderName;
			int folder_id;
			int level;
			boolean isValid = false;

			do {
				outputForClient.println("\nEnter Folder Name In Which You Want To Upload File"
						+ "\nIf You Want To Upload File To Global Level Type \"--global\"");
				parentFolderName = inputFromClient.readLine();
				
				if(checkIfInputIsExit(parentFolderName)) {
					return;
				}
				
				if(checkIfInputIsCancel(parentFolderName)) {
					outputForClient.println();
					loggedInMenu();
					
					return;
				}
				
				if(!folderNameExistsDB(parentFolderName) && !parentFolderName.equalsIgnoreCase("--global")) {
					outputForClient.println("\nFolder Doesn't Exist.");
					
					continue;
				}
				
				isValid = true;
			}while(!isValid);
			
			
			outputForClient.println("\nEnter Path To File [ C:\\\\Users\\\\Desktop\\\\fileName.extension ] : ");
			path = inputFromClient.readLine();
			
			if(checkIfInputIsExit(path)) {
				return;
			}
			
			if(checkIfInputIsCancel(path)) {
				outputForClient.println();
				loggedInMenu();
				
				return;
			}
			
			
//			InputStream in = new FileInputStream("C:\\Users\\IRC_client\\Desktop\\mpp.ppt");
			in = new FileInputStream(path);
			
			name = path.substring(path.lastIndexOf('\\') + 1);
			
			if(parentFolderName.equalsIgnoreCase("--global")) {
				folder_id = -1;
				level = 0;
			}else {
				folder_id = findFolderIdFromNameDB(parentFolderName);
				level = findFolderLevelFromNameDB(parentFolderName) + 1;
			}
			
			uploadFileDB(name, in, folder_id, level);
			outputForClient.println("\nFile \"" + name + "\" Successfully Uploaded.");
		} catch (FileNotFoundException e) {
//			e.printStackTrace();
			System.err.println("File Not Found Error In Upload");
			outputForClient.println("\nFile Not Found.");
		} catch (IOException e) {
//			e.printStackTrace();
			System.err.println("Input Error Upload File.");
		}finally {
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
					System.err.println("Error While Closing Input | Upload File.");
				}
			}
		}
			
		if(!this.prem) {
			updateNumberOfUploadedFilesDB(this.id, ++uploadedFilesNumber);
		}

	}
	
	/* ****** DOWNLOAD ****** */
	/* https://stackoverflow.com/questions/39231153/download-blob-data-from-mysql-database-via-java */

	private void downloadFile() {

		String fileNameToDownload;
		boolean fileExists = false;
		
		try {
			
			for(;;) {
				outputForClient.println("\nEnter File Name Which You Want To Download");
				fileNameToDownload = inputFromClient.readLine();
				
				if(checkIfInputIsExit(fileNameToDownload)) {
					return;
				}
				
				if(checkIfInputIsCancel(fileNameToDownload)) {
					outputForClient.println();
					loggedInMenu();
					
					return;
				}
				
				fileExists = fileNameExistsDB(fileNameToDownload);
				
				if(!fileExists) {
					outputForClient.println("\nFile Doesn't Exist.");
				}else {
					break;
				}
			}
			
			
			downloadFileDB(fileNameToDownload);
			outputForClient.println("\nFile \"" + fileNameToDownload + "\" Successfully Downloaded To Desktop.");
	    } catch (IOException e) {
	        System.err.println("Input Error While Downloading File.");
//	        e.printStackTrace();
	    }
		
	}
	
	/* ****** DATABASE ****** */
		
		/* ****** DB USERS ****** */
	
		private String getUserUsernameDB(int id) {
			String username = null;
			
			try {
				String sql = "SELECT username FROM users WHERE id = ?";
	
				PreparedStatement statement = connection.prepareStatement(sql);		
				statement.setInt(1, id);
				
				ResultSet result = statement.executeQuery();
	
				if(result.next()){
					username = result.getString(1);
				}
							
			} catch (SQLException e) {
				System.err.println("DB Error | User Username Search.");
	//			e.printStackTrace();
			}
	
			return username;
		}
		
		private int getUserIdDB(String user) {
			int id = -1;
			
			try {
				String sql = "SELECT id FROM users WHERE BINARY username = ?";

				PreparedStatement statement = connection.prepareStatement(sql);		
				statement.setString(1, user);
				
				ResultSet result = statement.executeQuery();

				if(result.next()){
					id = result.getInt(1);
				}
							
			} catch (SQLException e) {
				System.err.println("DB Error | User Id Search.");
//				e.printStackTrace();
			}

			return id;
		}
		
		private ResultSet userFoldersDB(String user) {
			int id = getUserIdDB(user);
			ResultSet result = null;

			try {
				String sql = "SELECT name, folder_id, level FROM folders WHERE user_id = ?";
				PreparedStatement statement = connection.prepareStatement(sql);
				statement.setInt(1, id);
				result = statement.executeQuery();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
				System.err.println("Error While Getting User Folders.");
			}
			
			return result;
		}
		
		private ResultSet userFilesDB(String user) {
			int id = getUserIdDB(user);
			ResultSet result = null;
			
			try {
				String sql = "SELECT name, folder_id, level FROM files WHERE user_id = ?";
				PreparedStatement statement;
				statement = connection.prepareStatement(sql);
				statement.setInt(1, id);	
				result = statement.executeQuery();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
				System.err.println("Error While Getting User Files.");
			}
			
			return result;
		}
		
		private ResultSet userFoldersDB(String user, int level) {
			int id = getUserIdDB(user);
			ResultSet result = null;

			try {
				String sql = "SELECT name, folder_id, level, id FROM folders WHERE user_id = ? AND level = ?";
				PreparedStatement statement = connection.prepareStatement(sql);
				statement.setInt(1, id);
				statement.setInt(2, level);
				result = statement.executeQuery();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
				System.err.println("Error While Getting User Folders.");
			}
			
			return result;
		}
		
		private ResultSet userFilesDB(String user, int level) {
			int id = getUserIdDB(user);
			ResultSet result = null;
			
			try {
				String sql = "SELECT name, folder_id, level FROM files WHERE user_id = ? AND level = ?";
				PreparedStatement statement;
				statement = connection.prepareStatement(sql);
				statement.setInt(1, id);	
				statement.setInt(2, level);
				result = statement.executeQuery();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
				System.err.println("Error While Getting User Files.");
			}
			
			return result;
		}
		
		private boolean checkIfUserExistsDB(String user) {
			boolean usernameExists = false;
			
			try {
				String sql = "SELECT username FROM users";
				Statement statement = connection.createStatement();
				ResultSet result = statement.executeQuery(sql);

				while(result.next()) {
					String dbUsername = result.getString(1);
										
					if (dbUsername.equals(user)) {
						usernameExists = true;
						
						break;
					}
				}			
				
				
			} catch (SQLException e) {
				System.err.println("DB Error | User Search.");
			}
			
			return usernameExists;
		}
		
		private void insertUserDB(String username, String password, String isPremium, String sharedDiskLink) {
			
			try {
				String sql2 = "INSERT INTO users (username, password, isPremium, number_of_uploads, shareDiskLink) VALUES (?, ?, ?, ?, ?)";
				PreparedStatement statement2;
				
				statement2 = connection.prepareStatement(sql2);
				
				statement2.setString(1, username);
				statement2.setString(2, password);
				statement2.setString(3, isPremium);
				statement2.setInt(4, 0);
				statement2.setString(5, sharedDiskLink);

				int rows = statement2.executeUpdate();
				
				if(rows > 0) {
					System.out.println("User: " + username + " successfully added to database.");
				}else {
					System.out.println("Error, couldn't insert user to database.");
				}
				
			} catch (SQLException e) {
				System.err.println("Database error while inserting user.");
			}
			
		}

		/* ****** DB FOLDERS / FILES ****** */

		private void uploadFileDB(String name, InputStream inputStream, int folder_id, int level) {
		    try {
		    	PreparedStatement statement = connection.prepareStatement("INSERT INTO files (user_id, folder_id, name, level, blobic) VALUES(?, ?, ?, ?, ?)");
				statement.setInt(1, this.id);
				statement.setInt(2, folder_id);
				statement.setString(3,  name);
				statement.setInt(4, level);
				statement.setBlob(5, inputStream);
				
				statement.execute();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
				System.err.println("DB Error | Upload File.");
			}
		    
		    System.out.println("Record Inserted......");
		}

		private void downloadFileDB(String name) {
		FileOutputStream output = null;
		InputStream input = null;
		ResultSet result = null;
		
		try {	
	     	String sql = "SELECT blobic FROM files WHERE id = ?";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setInt(1, findFileIdFromNameDB(name));
			result = statement.executeQuery();
			
//	        output = new FileOutputStream(new File("C:\\Users\\IRC_client\\Desktop\\" + name));
	        String desktopPath = System.getProperty("user.home") + "\\Desktop";
	        System.out.println(desktopPath);
//	        System.out.print(desktopPath.replace("\\", "/"));
	        output = new FileOutputStream(new File(desktopPath + "\\" + name));


	        System.out.println("Getting file please be patient..");
	
	        while (result.next()) {
	            input = result.getBinaryStream("blobic"); //get it from col name
	            int r = 0;
	            while ((r = input.read()) != -1) {
	                output.write(r);
	            }
	        }
	        
	        System.out.println("File writing complete !");
	    } catch (SQLException e) {
	        System.err.println("Connection failed!");   
//	        e.printStackTrace();
	    } catch (FileNotFoundException e) {
	        System.err.println("File not found!");
//	        e.printStackTrace();
	    } catch (IOException e) {
	        System.err.println("File writing error..!");
//	        e.printStackTrace();
	    }finally {
	    	if(result != null && input != null && output != null){
	            try {
	                input.close();
	                output.flush();
	                output.close();
	            } catch (IOException e) {
	                // TODO Auto-generated catch block
//	                e.printStackTrace();
	            }
	    	}
		}
	}
		
		private void updateNumberOfUploadedFilesDB(int user_id, int numberOfUploadedFiles) {
		try {
			String sql2 = "UPDATE users SET number_of_uploads = ? WHERE id = ?";
			PreparedStatement statement2;
			
			statement2 = connection.prepareStatement(sql2);
			
			statement2.setInt(1, numberOfUploadedFiles);
			statement2.setInt(2, user_id);

			int rows = statement2.executeUpdate();
			
			if(rows > 0) {
				System.out.println("Number Of Uploaded Files Successfully Updated.");
			}else {
				System.out.println("Error While Updating Number Of Uploaded Files.");
			}
			
		} catch (SQLException e) {
			System.err.println("Database error while updating number of uploaded files.");
		}
	}
		
		private int numberOfUploadsDB(int user_id) {
		int numberOfUploadedFiles = 0;
		
		try {		
			String sql = "SELECT number_of_uploads FROM users WHERE id = ?";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setInt(1, user_id);
			ResultSet result = statement.executeQuery();

			if(result.next()) {
				numberOfUploadedFiles = result.getInt(1);
			}
			
		} catch (SQLException e) {
			System.err.println("DB Error | User Number Of Files.");
		}
		
		return numberOfUploadedFiles;
	}
	
		private void insertFolderDB(int user_id, int folder_id, String name, int level) {
			
			try {
				String sql2 = "INSERT INTO folders (user_id, folder_id, name, level) VALUES (?, ?, ?, ?)";
				PreparedStatement statement2;
				
				statement2 = connection.prepareStatement(sql2);
				
				statement2.setInt(1, user_id);
				statement2.setInt(2, folder_id);
				statement2.setString(3, name);
				statement2.setInt(4, level);

				int rows = statement2.executeUpdate();
				
				if(rows > 0) {
					System.out.println("Folder: " + name + " successfully added to database.");
				}else {
					System.out.println("Error, couldn't insert folder to database.");
				}
				
			} catch (SQLException e) {
				System.err.println("Database error while inserting folder.");
			}
			
		}

		private void renameFolderDB(int idFolderToRename, String newFolderName) {
			try {
				String sql2 = "UPDATE folders SET name = ? WHERE id = ?";
				PreparedStatement statement2;
				
				statement2 = connection.prepareStatement(sql2);
				
				statement2.setString(1, newFolderName);
				statement2.setInt(2, idFolderToRename);

				int rows = statement2.executeUpdate();
				
				if(rows > 0) {
					System.out.println("Folder successfully renamed.");
				}else {
					System.out.println("Error, couldn't rename folder.");
				}
				
			} catch (SQLException e) {
				System.err.println("Database error while renaming folder.");
			}
		}

		private void moveFolderDB(int idOfMovingFolder, int newLevelOfMovedFolder, int newFolder_idOfMovedFolder) {
			
//			moveFolderDB(idOfMovingFolder, newLevelOfMovedFolder, newFolder_idOfMovedFolder);
			
			try {
				
				String sql2 = "UPDATE folders SET level = ?, folder_id = ? WHERE id = ?";
				PreparedStatement statement2 = connection.prepareStatement(sql2);
				
				statement2.setInt(1, newLevelOfMovedFolder);
				statement2.setInt(2, newFolder_idOfMovedFolder);
				statement2.setInt(3, idOfMovingFolder);

				int rows = statement2.executeUpdate();
				
				if(rows > 0) {
					System.out.println("Folder successfully moved.");
				}else {
					System.out.println("Error, couldn't move folder.");
				}
				
			} catch (SQLException e) {
				System.err.println("Database error while moving folder.");
			}
			
		}

		private void updateFileLevelDB(int id, int level) {
			try {
				
				String sql2 = "UPDATE files SET level = ? WHERE id = ?";
				PreparedStatement statement2 = connection.prepareStatement(sql2);
				
				statement2.setInt(1, level);
				statement2.setInt(2, id);

				int rows = statement2.executeUpdate();
				
				if(rows > 0) {
					System.out.println("File successfully updated level.");
				}else {
					System.out.println("Error, couldn't update file level.");
				}
				
			} catch (SQLException e) {
				System.err.println("Database error while moving file.");
			}
		}
		
		private void updateFolderLevelDB(int id, int level) {
			try {
				
				String sql2 = "UPDATE folders SET level = ? WHERE id = ?";
				PreparedStatement statement2 = connection.prepareStatement(sql2);
				
				statement2.setInt(1, level);
				statement2.setInt(2, id);

				int rows = statement2.executeUpdate();
				
				if(rows > 0) {
					System.out.println("Folder successfully updated level.");
				}else {
					System.out.println("Error, couldn't update folder level.");
				}
				
			} catch (SQLException e) {
				System.err.println("Database error while moving folder.");
			}
		}
		
		private void deleteFolderDB(int id) {
			try {
				String sql2 = "DELETE FROM folders WHERE id = ? LIMIT 1";
				PreparedStatement statement2 = connection.prepareStatement(sql2);
				
				statement2.setInt(1, id);

				int rows = statement2.executeUpdate();
				
				if(rows > 0) {
					System.out.println("Folder successfully deleted.");
				}else {
					System.out.println("Error, couldn't delete folder.");
				}
				
			} catch (SQLException e) {
				System.err.println("Database error while deleting folder.");
			}
		}
		
		private boolean isFolderEmptyDB(int id) {
			boolean isEmpty = true;
			
			// Folder je prazan ako niko od foldera nema folder_id = id
			
			try {
				String sql = "SELECT folder_id FROM folders";
				Statement statement = connection.createStatement();
				ResultSet result = statement.executeQuery(sql);

				while(result.next()) {
					int folder_id = result.getInt(1);
										
					if (folder_id == id) {
						isEmpty = false;
						
						break;
					}
				}			
				
			} catch (SQLException e) {
				System.err.println("DB Error | Folder Empty Search.");
			}
			
			return isEmpty;
		}
	
		private int findFolderIdFromNameDB(String folderName) {
			int id = -1;
			
			try {
				String sql = "SELECT id FROM folders WHERE name = ?";
				PreparedStatement statement = connection.prepareStatement(sql);
				statement.setString(1, folderName);
				ResultSet result = statement.executeQuery();
				
				if(result.next()) {
					id = result.getInt(1);
				}

			} catch (SQLException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
				System.err.println("SQL Error | findFodlerIDFromName.");
			}
			
			return id;
		}
		
		private int findFileIdFromNameDB(String fileName) {
			int id = -1;
			
			try {
				String sql = "SELECT id FROM files WHERE name = ?";
				PreparedStatement statement = connection.prepareStatement(sql);
				statement.setString(1, fileName);
				ResultSet result = statement.executeQuery();
				
				if(result.next()) {
					id = result.getInt(1);
				}

			} catch (SQLException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
				System.err.println("SQL Error | findFileIDFromName.");
			}
			
			return id;
		}
		
		private int findFolderLevelFromNameDB(String folderName) {
			int level = -1;
			
			try {
				String sql = "SELECT level FROM folders WHERE name = ?";
				PreparedStatement statement = connection.prepareStatement(sql);
				statement.setString(1, folderName);
				ResultSet result = statement.executeQuery();
				
				if(result.next()) {
					level = result.getInt(1);
				}

			} catch (SQLException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
				System.err.println("SQL Error | findFodlerLevelFromName.");
			}
			
			return level;
		}
		
		private int findFolderLevelFromID(int id) {
			int level = -1;
			
			try {
				String sql = "SELECT level FROM folders WHERE id = ?";
				PreparedStatement statement = connection.prepareStatement(sql);
				statement.setInt(1, id);
				ResultSet result = statement.executeQuery();
				
				if(result.next()) {
					level = result.getInt(1);
				}

			} catch (SQLException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
				System.err.println("SQL Error | findFileIDFromName.");
			}
			
			return level;
		}
		
		private boolean folderNameExistsDB(String folderName) {		
			boolean folderExists = false;
			
			try {
				String sql = "SELECT name FROM folders";
				Statement statement = connection.createStatement();
				ResultSet result = statement.executeQuery(sql);

				while(result.next()) {
					String dbFolderName = result.getString(1);
										
					if (dbFolderName.equals(folderName)) {
						folderExists = true;
						
						break;
					}
				}			
				
			} catch (SQLException e) {
				System.err.println("DB Error | Folder Search.");
			}
			
			return folderExists;
		}
		
		private boolean fileNameExistsDB(String fileName) {		
			boolean fileExists = false;
			
			try {
				String sql = "SELECT name FROM files";
				Statement statement = connection.createStatement();
				ResultSet result = statement.executeQuery(sql);

				while(result.next()) {
					String dbFileName = result.getString(1);
										
					if (dbFileName.equals(fileName)) {
						fileExists = true;
						
						break;
					}
				}			
				
			} catch (SQLException e) {
				System.err.println("DB Error | File Search.");
			}
			
			return fileExists;
		}
		
		/* ****** DB SHARES ****** */
			
		private void insertSharesDB(int user_id_from, int user_id_to) {
			try {
				String sql2 = "INSERT INTO shares (user_id_from, user_id_to) VALUES (?, ?)";
				PreparedStatement statement2;
				
				statement2 = connection.prepareStatement(sql2);
				
				statement2.setInt(1, user_id_from);
				statement2.setInt(2, user_id_to);
	
				int rows = statement2.executeUpdate();
				
				if(rows > 0) {
					System.out.println("Updated Shares Table.");
				}else {
					System.out.println("Error, Couldn't Update Shares.");
				}
				
			} catch (SQLException e) {
				System.err.println("Database error while inserting shares.");
			}
		}

		private ResultSet getUsersWhoSharedDisksToCurrentUserDB() {
		ResultSet result = null;

		try {
			String sql = "SELECT user_id_from FROM shares WHERE user_id_to = ?";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setInt(1, this.id);
			result = statement.executeQuery();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.err.println("DB Error | Getting Users Who Shared You Disk");
		}
		
		return result;
	}

	
	/* ****** COMMUNICATION METHODS ****** */

	private boolean checkIfInputIsExit(String input) {
		boolean communicationClosed = false;
		
		if(input != null && input.equals("--exit")) {
			closeCommunication();
			communicationClosed = true;
		}
		
		return communicationClosed;
	}
	
	private boolean checkIfInputIsCancel(String input) {
		boolean cancel = false;
		
		if(input != null && input.equals("--cancel")) {
			cancel = true;
		}
		
		return cancel;
	}

	private void closeCommunication() {
		try {
			this.outputForClient.println(">>> Exited successfully!");
			socketForCommunication.close();
			System.out.println("Clients connected: " + --Server.counter);
		} catch (IOException e) {
			System.err.println("Couldn't Close Communication | closeCommunication.");
		}
	}

	/* ****** MAIN / RUN METHOD ****** */

	public void run() {

		try {

			inputFromClient = new BufferedReader(new InputStreamReader(socketForCommunication.getInputStream()));
			outputForClient = new PrintStream(socketForCommunication.getOutputStream());
		
//			System.out.println(isChild("folder1_2", "folder1"));
			menu();
		} catch (Exception e) {
			System.err.println("Lost communication with client.");
			System.out.println("Clients connected: " + --Server.counter);
//			e.printStackTrace();
		}
	}
}

// zatvori konekcije na svaki exception.
// kad kuca nesto da moze da se vrati sa --back

//private void moveSubFoldersAndFilesDB() {
//// Nadji sve foldere i fajlove za taj folder. Onda im nadji level i promeni ga na novi
//try {
//	
//	String sql2 = "UPDATE folders SET level = ?, folder_id = ? WHERE id = ?";
//	PreparedStatement statement2 = connection.prepareStatement(sql2);
//	
//	statement2.setInt(1, newLevelOfMovedFolder);
//	statement2.setInt(2, newFolder_idOfMovedFolder);
//	statement2.setInt(3, idOfMovingFolder);
//
//	int rows = statement2.executeUpdate();
//	
//	if(rows > 0) {
//		System.out.println("Folder successfully moved.");
//	}else {
//		System.out.println("Error, couldn't move folder.");
//	}
//	
//	moveSubFoldersAndFilesDB(idOfMovingFolder, newLevelOfMovedFolder, newFolder_idOfMovedFolder);
//	
//} catch (SQLException e) {
//	System.err.println("Database error while moving folder.");
//}
//}