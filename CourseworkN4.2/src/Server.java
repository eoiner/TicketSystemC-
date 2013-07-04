import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.sql.*;

public class Server extends JFrame implements ActionListener {
	private int port = 5050;
	private boolean foundRec = false;
	private String url;
	private String messageTokens[] = new String[16],
			addrRecord[][] = {
					{ "Eoin Butler", "Mary Smith", "Jammed Disk Tray", "5", "SOLVED" },
					 };
	private ServerSocket serverSocket;
	private BufferedReader input;
	private PrintWriter output;
	private Container c;
	private JTextArea display;
	private JButton cancel, send, exit;
	private JPanel buttonPanel;
	private StringTokenizer tokens;
	private File aFile;
	private RandomAccessFile file;
	private String pData[][] = new String[300][11];
	private Connection connect;
	private String query;
	private boolean myDebug = true;
	private Statement statement;


	public Server() {
		super("Server");

		setup();

		run();
	}

	// Initialize the db
	public void setup() {
		c = getContentPane();

		exit = new JButton("Exit");
		exit.setBackground(Color.red);
		exit.setForeground(Color.white);
		buttonPanel = new JPanel();
		buttonPanel.add(exit);
		c.add(buttonPanel, BorderLayout.SOUTH);

		exit.addActionListener(this);

		display = new JTextArea();
		display.setEditable(false);
		addWindowListener(new WindowHandler(this));
		c.add(new JScrollPane(display), BorderLayout.CENTER);

		setSize(400, 700);
		setLocation(10, 20);
		show();
	}

	public void InitRecord() {

		ResultSet rs;
		sysPrint("\nIn InitRecord() method.");

		try {

			url = "jdbc:odbc:IssueTracker1";

			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			connect = DriverManager.getConnection(url);
			sysPrint("\nThe value of connect is " + connect);
			statement = connect.createStatement();

			if (connect == null) {
				display.append("Connection... was not successful\n");

				JOptionPane.showMessageDialog(null,
						"The IssueTracker1 was not found. \n",
						"Database not found", JOptionPane.INFORMATION_MESSAGE);
			} else {
				sysPrint("\nReady to initialize database.");
				statement = connect.createStatement();

				query = "SELECT * FROM trackerRecords ";
				rs = statement.executeQuery(query);
				if (!rs.next()) {
					for (int ii = 0; ii < addrRecord.length; ii++) {

						query = "INSERT INTO trackerRecords ("
								+ "Techname, Custname, IssueDetails, Urgency, "
								+ "Status"
								+ ") VALUES ('" + addrRecord[ii][0] + "', '"
								+ addrRecord[ii][1] + "', '"
								+ addrRecord[ii][2] + "', '"
								+ addrRecord[ii][3] + "', '"
								+ addrRecord[ii][4] + "', '"
								+ addrRecord[ii][5] + "')";

						int result = statement.executeUpdate(query);
						if (result == 1)
							display.append("\nThe record was successful in init().");

						display.append("\nThe init record written was "
								+ addrRecord[ii][0] + addrRecord[ii][1]
								+ addrRecord[ii][2] + addrRecord[ii][3]
								+ addrRecord[ii][4] + addrRecord[ii][5]);

					} 
				} 
			} 
			statement.close();
		} catch (ClassNotFoundException cnfex) {
			// process ClassNotFoundExceptions here
			cnfex.printStackTrace();
			display.append("Connection unsuccessful\n" + cnfex.toString());
		} catch (SQLException sqlex) {
			// process SQLExceptions here
			sqlex.printStackTrace();
			display.append("Connection unsuccessful\n" + sqlex.toString());
		} catch (Exception ex) {
			// process remaining Exceptions here
			ex.printStackTrace();
			display.append(ex.toString());
		}

	}


	// Method reads and writes data to the server
	public void run() {
		int i3 = 0;

		try {

			serverSocket = new ServerSocket(5050, 100,
					InetAddress.getByName("127.0.0.1"));
			display.setText("Server waiting for client on port "
					+ serverSocket.getLocalPort() + "\n");

			// server infinite loop
			while (true) {
				Socket socket = serverSocket.accept();
				display.append("New connection accepted "
						+ socket.getInetAddress() + ":" + socket.getPort()
						+ "\n");
				input = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				output = new PrintWriter(socket.getOutputStream(), true);

				InitRecord();

				String message = "";

				// print received data
				try {
					while (!message.toUpperCase().equals("QUIT")) {
						message = (String) input.readLine();
						tokens = new StringTokenizer(message);

						if (tokens.countTokens() >= 1) {
							int ii = 0;
							while (tokens.hasMoreTokens()) {
								messageTokens[ii] = tokens.nextToken()
										.toString();
								display.append("\n" + messageTokens[ii]);
								ii++;
							}

							display.append("\nThe value of messageTokens[ 0 ] is "
									+ messageTokens[0] + "\n");
							if (messageTokens[0].toUpperCase().equals("FIND")) {
								;
							} else if (messageTokens[0].toUpperCase().equals(
									"LISTALL")) {
								display.append("\nCurrrently in run() method and if LISTALL construct.");
								listNames();
							} else if (messageTokens[0].toUpperCase().equals(
									"ADD;;")) {
								display.append("\nCurrrently in run() method and if add construct.");
								addName(message);
							} else if (messageTokens[0].toUpperCase().equals(
									"UPDATE;;")) {
								display.append("\nCurrrently in run() method and if update construct.");
								updateName(message);
							} else if (messageTokens[0].toUpperCase().equals(
									"DELETE;;")) {
								display.append("\nCurrrently in run() method and if delete construct.");
								deleteName();
							}
							ii = 0;
						} else {
							display.append(message);
							message = null; 
											
							break;
						}
					}
					sendData("FROM SERVER==> QUIT");
				} catch (IOException e) {
					display.append("\n" + e);
				}

				// connection closed by client
				try {
					socket.close();
					display.append("\n Connection closed by client");
				} catch (IOException e) {
					display.append("\n" + e);
				}
			}
		} catch (IOException e) {
			display.append("\n" + e);
		}
	}


	// Exit Button action
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == exit)
			closeConnection();
	}


	// Closes the Socket
	private void closeConnection() {
		try {
			serverSocket.close();
			input.close();
			System.exit(0);
		} catch (IOException e) {
			display.append("\n" + e);
			System.exit(0);
		}

		try {
			connect.close();
		} catch (SQLException sqlex) {
			System.err.println("Unable to disconnect");
			sqlex.printStackTrace();
		}
	}

	// Print Notes
	public void sysPrint(String str) {
		if (myDebug) {
			System.out.println(str);
		}
	}


	// Add a name to the db
	public void addName(String message) {

		int ii = 0;

		display.append("\nCurrrently in addName() method.");
		tokens = new StringTokenizer(message, ";;");

		try {
			statement = connect.createStatement();

			if (tokens.countTokens() >= 1) {

				while (tokens.hasMoreTokens()) {
					messageTokens[ii] = tokens.nextToken().trim().toString();
					display.append("\n" + messageTokens[ii]);
					ii++;
				}
			}

			display.append("\nThe size of the recID is "
					+ messageTokens[1].length());
			String query = "INSERT INTO trackerRecords ("
					+ "Techname, Custname, IssueDetails, Urgency, "
					+ "Status" + ") VALUES ('"
					+ messageTokens[1] + "', '" + messageTokens[2] + "', '"
					+ messageTokens[3] + "', '" + messageTokens[4] + "', '"
					+ messageTokens[5] + "')";

			int result = statement.executeUpdate(query);
			if (result == 1)
				display.append("\nThe record was successful in addName().");

			statement.close();
		} catch (SQLException sqlex) {
			sqlex.printStackTrace();
			display.append(sqlex.toString());
		}
		display.append("\nThe record to be added is " + messageTokens[0] + " "
				+ messageTokens[1] + " " + messageTokens[2] + " "
				+ messageTokens[3] + " " + messageTokens[4] + " "
				+ messageTokens[5]);

	}


	// Method lists names in the db
	public void listNames() {
		int ii = 0, iii = 0, recLength = addrRecord.length, numEntries = 0;
		double loopLimit = 0;
		String str = "";
		Vector rows = new Vector();
		ResultSet rs;
		ResultSetMetaData rsmd;

		foundRec = false;
		sysPrint("In listNames() method.");

		sendData("ListAll;;");

		query = "SELECT * FROM trackerRecords ";
		try {

			sysPrint("The value of connect is " + connect);

			Statement statement = connect.createStatement();
			rs = statement.executeQuery(query);
			while (rs.next()) {
				pData[iii][0] = String.valueOf(rs.getInt(1));
				pData[iii][1] = rs.getString(2);
				pData[iii][2] = rs.getString(3);
				pData[iii][3] = rs.getString(4);
				pData[iii][4] = rs.getString(5);
				pData[iii][5] = rs.getString(6);

				display.append("\n" + pData[iii][0] + " " + pData[iii][1] + " "
						+ pData[iii][2] + " " + pData[iii][3] + " "
						+ pData[iii][4] + " " + pData[iii][5]);

				// Begin at 0(iii) and pack data in locations
				str = "" + (pData[iii][0]).trim() + ";; "
						+ (pData[iii][1]).trim() + ";; "
						+ (pData[iii][2]).trim() + ";; "
						+ (pData[iii][3]).trim() + ";; "
						+ (pData[iii][4]).trim() + ";; "
						+ (pData[iii][5]).trim() + ";; " + ";;;";
				sendData(str);
				display.append("\nsendData " + str); // setting up to send the
														// entire file
				str = "";

				iii++;
				ii++;
			}

			statement.close();
		} catch (SQLException sqlex) {
			sqlex.printStackTrace();
			display.append(sqlex.toString());
		}

		sendData("GetAllDone;;");
	}

	// Update a name in the db
	public void updateName(String message) {

		int ii = 0;

		display.append("\nCurrrently in updateName() method.");
		tokens = new StringTokenizer(message, ";;");

		try {
			statement = connect.createStatement();

			if (tokens.countTokens() >= 1) {

				while (tokens.hasMoreTokens()) {
					messageTokens[ii] = tokens.nextToken().trim().toString();
					display.append("\n" + messageTokens[ii]);
					ii++;
				}
			}

			display.append("\nThe size of the recID is "
					+ messageTokens[1].length());
			String query = "UPDATE addresses SET " + "Techname='"
					+ messageTokens[2] + "', Custname='" + messageTokens[3]
					+ "', IssueDetails='" + messageTokens[4] + "', Urgency='"
					+ messageTokens[5] + "', Status='" + messageTokens[6]
					+ "' WHERE id=" + messageTokens[1];

			int result = statement.executeUpdate(query);
			if (result == 1)
				display.append("\nThe record was successful in updateName().");

			statement.close();
		} catch (SQLException sqlex) {
			sqlex.printStackTrace();
			display.append(sqlex.toString());
		}
		display.append("\nThe record to be updated is " + messageTokens[1]
				+ " " + messageTokens[2] + " " + messageTokens[3] + " "
				+ messageTokens[4] + " " + messageTokens[5] + " "
				+ messageTokens[6]);

	}

	// Delete a name in the db
	public void deleteName() {
		int ii = 0, recLength = addrRecord.length;

		display.append("\nCurrrently in deleteName() method.");

		ii = messageTokens[1].indexOf(";");
		messageTokens[1] = (messageTokens[1]).substring(0, ii);

		try {
			statement = connect.createStatement();
			String query = "DELETE FROM trackerRecords WHERE id=" + messageTokens[1];
			int result = statement.executeUpdate(query);

			if (result == 1)
				display.append("\nThe record was successful in deleteName().");

			statement.close();
		} catch (SQLException sqlex) {
			sqlex.printStackTrace();
			display.append(sqlex.toString());
		}

		display.append("\nThe record to be deleted was " + messageTokens[1]);

	}

	private void sendData(String str) {
		output.println(str);
		output.flush();
	}

	public static void main(String args[]) {
		final Server server = new Server();
		server.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				server.closeConnection();
				System.exit(0);
			}
		});
	}

	// Closing the Socket
	class WindowHandler extends WindowAdapter {
		Server tcpS;

		public WindowHandler(Server t) {
			tcpS = t;
		}

		public void windowClosing(WindowEvent e) {
			tcpS.closeConnection();
		}
	}

}
