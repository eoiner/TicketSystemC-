import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.util.*;
import java.net.*;


public class Client extends JFrame implements ActionListener {
	private int port = 5050;
	private String server = "localhost";
	private Socket socket = null;
	private BufferedReader input;
	private PrintWriter output;
	private int ERROR = 1, iii = 0, numEntries = 0;
	private Container c;
	private JTextArea display;
	private JButton clear, status, refresh, add, update, delete, exit;
	private JPanel buttonPanel, textPanel, deckPanel, displayPanel, listPanel,
			addCard, updateCard, deleteCard, findCard;
	private CardLayout cardManager;
	private BorderLayout borderManager;
	private JTextField enterBox;
	private JLabel enterLabel;
	private StringTokenizer tokens;
	private String messageTokens[] = new String[16];
	private String pData[][] = new String[300][11];
	private boolean myDebug = true, initPass = true;
	private JTable table;
	private String columnNames[] = { "Record ID", "TechName", "CustName",
			"IssueDetails", "Urgency", "Status" };
	private StatusDisp statusDisp;
	private UpdateRec updateRec;
	private NewRec newRec;
	private DeleteRec deleteRec;
	Client tcpC;
	private JScrollPane scrollpane;

	public Client() {
		super("Client");

		setUp();

		connect();

		run();

		closeConnection();
	}

	// Initialise the application
	private void setUp() {

		c = getContentPane();

		status = new JButton("Status");
		refresh = new JButton("Refresh");
		add = new JButton("Add");
		update = new JButton("Update");
		delete = new JButton("Delete");
		;
		exit = new JButton("Exit");
		refresh.setBackground(Color.blue);
		refresh.setForeground(Color.white);
		status.setBackground(Color.blue);
		status.setForeground(Color.white);
		add.setBackground(Color.blue);
		add.setForeground(Color.white);
		update.setBackground(Color.blue);
		update.setForeground(Color.white);
		delete.setBackground(Color.blue);
		delete.setForeground(Color.white);
		exit.setBackground(Color.red);
		exit.setForeground(Color.white);
		buttonPanel = new JPanel();
		buttonPanel.add(refresh);
		buttonPanel.add(status);
		buttonPanel.add(add);
		buttonPanel.add(update);
		buttonPanel.add(delete);
		buttonPanel.add(exit);
		c.add(buttonPanel, BorderLayout.SOUTH);

		enterLabel = new JLabel(
				"Enter a last name below and then press a button.");
		enterLabel.setFont(new Font("Serif", Font.BOLD, 14));
		enterLabel.setForeground(Color.black);
		enterBox = new JTextField(100);
		enterBox.setEditable(true);
		textPanel = new JPanel();
		textPanel.setLayout(new GridLayout(2, 1));

		status.addActionListener(this);
		refresh.addActionListener(this);
		add.addActionListener(this);
		update.addActionListener(this);
		delete.addActionListener(this);
		exit.addActionListener(this);



		statusDisp = new StatusDisp(tcpC);


		listPanel = new JPanel();
		listPanel.setLayout(borderManager);
		table = new JTable(pData, columnNames);
		table.setEnabled(false);
		scrollpane = JTable.createScrollPaneForTable(table);
		c.add(scrollpane);

		addWindowListener(new WindowHandler(this));
		setSize(900, 700);
		setLocation(420, 20);
		show();

	}

	private void connect() {
		// connect to server
		try {
			System.out.println("Connecting to server " + server + " " + port);
			socket = new Socket(server, port);
			statusDisp.display.setText("Connected with server "
					+ socket.getInetAddress() + ":" + socket.getPort());
		} catch (UnknownHostException e) {
			statusDisp.display.setText("" + e);
			System.exit(ERROR);
		} catch (IOException e) {
			statusDisp.display.setText("\n" + e);
			System.out.println("\n" + e);
			System.exit(ERROR);
		}
	}

	private void sendData(String str) {
		output.println(str);
	}

	public void sysPrint(String str) {
		if (myDebug) {
			System.out.println(str);
		}
	}

	public String[][] getPData() {
		return pData;
	}

	public int getEntries() {
		return numEntries;
	}

	public void setEntries(int ent) {
		numEntries = ent;
	}

	public int getNumEntries() {
		return numEntries;
	}

	private void run() {
		try {
			input = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			output = new PrintWriter(socket.getOutputStream(), true);
			enterBox.requestFocus();

			while (true) {

				statusDisp.display
						.append("\nThe names in the issue tracker records are: ");
				sendData("LISTALL NAMES");

				String message = input.readLine();
				
				while (!message.toUpperCase().equals("FROM SERVER==> QUIT")) {

					tokens = new StringTokenizer(message, ";");
					if (tokens.countTokens() >= 1) {
						int ii = 0;
						while (tokens.hasMoreTokens()) {
							messageTokens[ii] = tokens.nextToken().toString();

							ii++;
						}

						if (messageTokens[0].toUpperCase()
								.equals("RECORDFOUND")) {
							showName();
						} else if (messageTokens[0].toUpperCase().equals(
								"LISTRECORD")) {
							listNames();
						} else if (messageTokens[0].toUpperCase().equals(
								"LISTALL")) {
							sysPrint("\nProcessing ListAll data.");
							message = (String) input.readLine();
							iii = 0;
							while (!message.equals("GetAllDone;;")) {
								tokens = new StringTokenizer(message, ";;");
								pData[iii][0] = tokens.nextToken().toString()
										.trim();
								sysPrint("\nThe value of pData [iii][ 0 ] is: "
										+ pData[iii][0]);
								ii = 1;
								while (tokens.hasMoreTokens() && ii < 11) {
									pData[iii][ii] = tokens.nextToken()
											.toString();

									ii++;
								}

								sysPrint("\n" + message);
								message = (String) input.readLine();

								statusDisp.display.append("\nListAll record "
										+ pData[iii][0] + " " + pData[iii][1]
										+ " " + pData[iii][2] + " "
										+ pData[iii][3] + " " + pData[iii][4]
										+ " " + pData[iii][5] + " "
										+ pData[iii][6] + " " + pData[iii][7]
										+ " " + pData[iii][8]);

								iii++;
								listAllNames();
							} 

							numEntries = iii - 1;
							sysPrint("\n" + message);
							initPass = false;

							table = new JTable(pData, columnNames);
							this.repaint();
						} else if (messageTokens[0].toUpperCase().equals(
								"RECORDDELETED")) {
							statusDisp.display.append("\n" + message);
						} else if (messageTokens[0].toUpperCase().equals(
								"NOTFOUND")) {
							statusDisp.display.append("\n" + message);
						}
					}
					message = input.readLine();
				} 
			}
		} catch (IOException e) {
			statusDisp.display.append("\n" + e);
		}

	}


	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == exit) {
			closeConnection();
		}

		else if (e.getSource() == refresh) {
			table = new JTable(pData, columnNames);
			table.repaint();
		} else if (e.getSource() == clear) {
			enterBox.setText("");
		}

		else if (e.getSource() == status) {
			statusDisp.setVisible(true);
		} else if (e.getSource() == add) {
			JOptionPane.showMessageDialog(null,
					"1: Enter a unique Record ID. \n"
							+ "2: Technician Name. \n" + "3: Enter  \n"
							+ "- Customer Name \n" + "- Issue Details \n"
							+ "- Urgency \n" + "- Status \n"
							+ "4: Then press enter.\n", "Add Record",
					JOptionPane.INFORMATION_MESSAGE);
			newRec = new NewRec(tcpC, table, pData);
			newRec.setVisible(true);
		} else if (e.getSource() == update) {
			JOptionPane.showMessageDialog(null,
					"1: Enter the Record ID that you want to update. "
							+ "\n2: Then press enter.", "Update Record",
					JOptionPane.INFORMATION_MESSAGE);
			updateRec = new UpdateRec(tcpC, pData, iii);
		} else if (e.getSource() == delete) {
			deleteRec = new DeleteRec(tcpC, table, pData);
			deleteRec.show(true);
		}
	}

	// Show name found - from server
	private void showName() {
		statusDisp.display.append("\n Name:         " + messageTokens[1] + " "
				+ messageTokens[2]);
		statusDisp.display.append("\n :     " + messageTokens[3]);
		statusDisp.display.append("\n : " + messageTokens[4]);
	}

	private void listNames() {
		if (!messageTokens[1].equals("") && !messageTokens[1].equals(" ")) {
			statusDisp.display.append("\n Name: " + messageTokens[1]);
		}
	}

	private void listAllNames() {

	}

	// Closing the Socket to the server
	private void closeConnection() {
		sendData("QUIT");
		try {
			socket.close();
			input.close();
			output.close();
		} catch (IOException e) {
			statusDisp.display.append("\n" + e);
		}

		setVisible(false);
		System.exit(0);
	}

	public void paintComponent(Graphics g) {
		super.paintComponents(g);

		this.remove(table);

		table = new JTable(pData, columnNames);
		this.add(table);

	}

	// Entry point - JVM Calls
	public static void main(String[] args) {
		final Client client = new Client();

		client.tcpC = client;

		client.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				client.closeConnection();
			}
		});
	}

	// Closing the socket connect when application finishes
	class WindowHandler extends WindowAdapter {
		Client tcpC;

		public WindowHandler(Client t) {
			tcpC = t;
		}

		public void windowClosing(WindowEvent e) {
			tcpC.closeConnection();
		}
	}

	class StatusDisp extends Dialog implements ActionListener {

		private JButton exit;
		private int theRecID, ii;
		private String pData[][];
		private Client tclient;
		JTextArea display;

		// Status Display constructor
		public StatusDisp(Client t_client) {

			super(new Frame(), "Status Display", true);

			tclient = t_client;

			setup();
		}

		// Initialise the application
		public void setup() {

			display = new JTextArea();
			display.setEditable(false);
			add(new JScrollPane(display), BorderLayout.CENTER);

			setSize(400, 280);

			exit = new JButton("Exit");

			exit.addActionListener(this);

			add(exit, BorderLayout.SOUTH);
		}

		public void actionPerformed(ActionEvent e) {

			if (e.getSource() == exit) {
				clear();
			}
		}

		// Clears the status display
		private void clear() {
			setVisible(false);

		}
	}

	class UpdateRec extends Dialog implements ActionListener {
		private JTextField recID, TechName, CustName, IssueDetails, Urgency,
				Status;
		private JLabel recIDLabel, TechNameLabel, CustNameLabel,
				IssueDetailsLabel, UrgencyLabel, StatusLabel;
		private JButton cancel, save;
		private int theRecID, ii, toCont;
		private String pData[][];
		private Client tclient;

		// Update records constructor
		public UpdateRec(Client t_client, String p_Data[][], int iiPassed) {

			super(new Frame(), "Update Record", true);

			pData = p_Data;
			ii = iiPassed;
			tclient = t_client;

			setup();

			setVisible(true);
		}

		// Initializing the application
		public void setup() {

			setSize(400, 280);
			setLayout(new GridLayout(12, 2));

			setLayout(new GridLayout(14, 2));

			recID = new JTextField(10);
			TechName = new JTextField(10);
			CustName = new JTextField(10);
			IssueDetails = new JTextField(10);
			Urgency = new JTextField(10);
			Status = new JTextField(10);

			recIDLabel = new JLabel("Record ID");
			TechNameLabel = new JLabel("Technician Name");
			CustNameLabel = new JLabel("Customer Name");
			IssueDetailsLabel = new JLabel("Issue Details");
			UrgencyLabel = new JLabel("Urgency");
			StatusLabel = new JLabel("Status");
			save = new JButton("Save Changes");
			cancel = new JButton("Cancel");

			recID.addActionListener(this);
			save.addActionListener(this);
			cancel.addActionListener(this);

			add(recIDLabel);
			add(recID);
			add(TechNameLabel);
			add(TechName);
			add(CustNameLabel);
			add(CustName);
			add(IssueDetailsLabel);
			add(IssueDetails);
			add(UrgencyLabel);
			add(Urgency);
			add(StatusLabel);
			add(Status);
			add(save);
			add(cancel);
		}

		// Data entry method
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == recID) {
				theRecID = Integer.parseInt(recID.getText());

				sysPrint("The value of theRecID is " + theRecID);
				if (theRecID > 0) {
					for (int i = 0; i <= getNumEntries(); i++) {
						if (!(pData[i][0] == null)) {
							sysPrint("The value of  pData[ i ] [ 0 ] is "
									+ Integer.parseInt(pData[i][0]));
						} else {
							sysPrint("The value of  pData[ i ] [ 0 ] is "
									+ pData[i][0]);
						}

						if (!(pData[i][0]).equals("")
								&& Integer.parseInt(pData[i][0]) == theRecID) {
							theRecID = i;
							break;
						}
					}

					recID.setText(pData[theRecID][0]);
					TechName.setText(pData[theRecID][1]);
					CustName.setText(pData[theRecID][2]);
					IssueDetails.setText(pData[theRecID][3]);
					Urgency.setText(pData[theRecID][4]);
					Status.setText(pData[theRecID][5]);
				} else
					recID.setText("This record " + theRecID + " does not exist");
			} else if (e.getSource() == save) {

				pData[theRecID][0] = recID.getText();
				pData[theRecID][1] = TechName.getText().trim();
				pData[theRecID][2] = CustName.getText().trim();
				pData[theRecID][3] = IssueDetails.getText().trim();
				pData[theRecID][4] = Urgency.getText().trim();
				pData[theRecID][5] = Status.getText();

				for (int iii = 0; iii < pData.length; iii++) {
					if ((pData[iii][0]).equals(recID.getText())) {
						theRecID = iii;
						break;
					}
				}

				table = new JTable(pData, columnNames);
				table.repaint();

				sendData("Update;; " + pData[theRecID][0] + ";; "
						+ pData[theRecID][1] + ";; " + pData[theRecID][2]
						+ ";; " + pData[theRecID][3] + ";; "
						+ pData[theRecID][4] + ";; " + pData[theRecID][5]
						+ ";; ");

				toCont = JOptionPane.showConfirmDialog(null,
						"Do you want to add another record? \nChoose one",
						"Choose one", JOptionPane.YES_NO_OPTION);

				if (toCont == JOptionPane.YES_OPTION) {
					recID.setText("");
					TechName.setText("");
					CustName.setText("");
					IssueDetails.setText("");
					Urgency.setText("");
					Status.setText("");
				} else {
					clear();
				}
			} else if (e.getSource() == cancel) {

				clear();
			}
		}

		// Resets the fields
		private void clear() {
			recID.setText("");
			TechName.setText("");
			CustName.setText("");
			IssueDetails.setText("");
			Urgency.setText("");
			Status.setText("");
			setVisible(false);
		}
	}

	class NewRec extends Dialog implements ActionListener {
		private JTextField recID, TechName, CustName, IssueDetails, Urgency,
				Status;
		private JLabel recIDLabel, TechNameLabel, CustNameLabel,
				IssueDetailsLabel, UrgencyLabel, StatusLabel;
		private JButton cancel, save;

		private int recIDNum, toCont;
		private JTable table;
		private JPanel addressPanel;
		private String pData[][];
		private boolean recExists = false;
		private Client tclient;

		// NewRec constructor
		public NewRec(Client t_client, JTable tab, String p_Data[][]) {
			super(new Frame(), "New Record", true);

			table = tab;
			pData = p_Data;
			tclient = t_client;
			setup();

			setSize(400, 250);
		}

		// Initializing the application
		public void setup() {

			setLayout(new GridLayout(14, 2));

			recID = new JTextField(10);
			recID.setEnabled(false);
			TechName = new JTextField(10);
			CustName = new JTextField(10);
			IssueDetails = new JTextField(10);
			Urgency = new JTextField(10);
			Status = new JTextField(10);

			recIDLabel = new JLabel("Record ID");
			TechNameLabel = new JLabel("Technician Name");
			CustNameLabel = new JLabel("Customer Name");
			IssueDetailsLabel = new JLabel("Issue Details");
			UrgencyLabel = new JLabel("Urgency Level (1-5)");
			StatusLabel = new JLabel("Status (SOLVED/UNSOLVED)");
			save = new JButton("Save Changes");
			cancel = new JButton("Cancel");

			recID.addActionListener(this);
			save.addActionListener(this);
			cancel.addActionListener(this);

			add(recIDLabel);
			add(recID);
			add(TechNameLabel);
			add(TechName);
			add(CustNameLabel);
			add(CustName);
			add(IssueDetailsLabel);
			add(IssueDetails);
			add(UrgencyLabel);
			add(Urgency);
			add(StatusLabel);
			add(Status);
			add(save);
			add(cancel);

		}

		// Cancel/Save button action listner
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == cancel) {
				clear();
			} else if (e.getSource() == recID) {
				if (recID.getText().equals(null) || recIDNum <= 0
						|| recIDNum > 300) {
					JOptionPane
							.showMessageDialog(
									null,
									"A Record ID entered was:  null or blank, or not between 0 and 300. which is invalid.\n"
											+ "Please enter a number greater than 0 and less than 300.",
									"RecID Entered", JOptionPane.ERROR_MESSAGE);
				} else {
					for (int i = 0; i <= getNumEntries(); i++) {
						if (Integer.parseInt(pData[i][0]) == recIDNum) {
							recIDNum = i;
							recExists = true;
							break;
						}
					}
					if (recExists) {
						JOptionPane.showMessageDialog(null, "A recID entered "
								+ recID.getText() + " already exists.",
								"RecID Exists", JOptionPane.ERROR_MESSAGE);
						recExists = false;
					}
				}
			} else if (e.getSource() == save) {
				sysPrint("\n1a: Currrently in add() class actionPerformed() method and save construct.");

				if ((TechName.getText() != "") && (CustName.getText() != "")) {
					sysPrint("\n1b: Currrently in add() class - checking for duplicate recID.");

					sysPrint("\n1d: Currrently in add() class actionPerformed() method - getting data for add.");
					recIDNum = getNumEntries() + 1;
					pData[recIDNum][0] = recID.getText();
					sysPrint("A new record is being added at "
							+ pData[recIDNum][0]);

					pData[recIDNum][1] = TechName.getText().trim();
					pData[recIDNum][2] = CustName.getText().trim();
					pData[recIDNum][3] = IssueDetails.getText().trim();
					pData[recIDNum][4] = Urgency.getText().trim();
					pData[recIDNum][5] = Status.getText().trim();

					table = new JTable(pData, columnNames);
					this.repaint();
					setEntries(getEntries() + 1);
					sendData("Add;; " // + pData[ recIDNum ] [ 0 ] + ";; "
							+ pData[recIDNum][1] + ";; "
							+ pData[recIDNum][2]
							+ ";; " + pData[recIDNum][3]
							+ ";; "
							+ pData[recIDNum][4] + ";; "
							+ pData[recIDNum][5] + ";;;");

					toCont = JOptionPane.showConfirmDialog(null,
							"Do you want to add another record? \nChoose one",
							"Choose one", JOptionPane.YES_NO_OPTION);

					if (toCont == JOptionPane.YES_OPTION) {
						recID.setText("");
						TechName.setText("");
						CustName.setText("");
						IssueDetails.setText("");
						Urgency.setText("");
						Status.setText("");
					} else {
						clear();
					}
					
				}

			}

		}

		// Clears the NewRec dialog
		private void clear() {
			sysPrint("\n1e: Currrently in add() class clear() method.");
			setVisible(false);
		}
	}

	class DeleteRec extends Dialog implements ActionListener {
		private JTextField recID;
		private JLabel recIDLabel;
		private JButton cancel, delete;
		// private Record data;
		private int partNum, iii = 0;
		private int theRecID = -1, toCont;
		private JTable table;
		private String pData[][];
		private Client tclient;

		// DeleteRec constructor
		public DeleteRec(Client t_client, JTable tab, String p_Data[][]) {
			super(new Frame(), "Delete Record", true);
			setSize(400, 150);
			setLayout(new GridLayout(2, 2));

			table = tab;
			pData = p_Data;
			tclient = t_client;

			recIDLabel = new JLabel("Record ID");
			recID = new JTextField(10);
			delete = new JButton("Delete Record");
			cancel = new JButton("Cancel");

			cancel.addActionListener(this);
			delete.addActionListener(this);
			recID.addActionListener(this);

			add(recIDLabel);
			add(recID);
			add(delete);
			add(cancel);
		}

		// Cancel/Save button action
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == recID) {
				theRecID = Integer.parseInt(recID.getText());
			} else if (e.getSource() == delete) {
				theRecID = Integer.parseInt(recID.getText());
				sysPrint(" The record id to be deleted is " + theRecID);

				sendData("Delete;; " + (theRecID) + ";; ");
				setEntries(getEntries() - 1);

				for (int iii = 0; iii < pData.length; iii++) {
					if ((pData[iii][0]).equals(recID.getText())) {
						theRecID = iii;
						break;
					}
				}

				pData[theRecID][0] = Integer.toString(theRecID);
				pData[theRecID][1] = "Deleted";
				pData[theRecID][2] = " ";
				pData[theRecID][3] = " ";
				pData[theRecID][4] = " ";
				pData[theRecID][5] = " ";

				table = new JTable(pData, columnNames);
				table.repaint();

				toCont = JOptionPane.showConfirmDialog(null,
						"Do you want to add another record? \nChoose one",
						"Choose one", JOptionPane.YES_NO_OPTION);

				if (toCont == JOptionPane.YES_OPTION) {
					recID.setText("");
				} else {
					clear();
				}
			} else if (e.getSource() == cancel) {
				clear();
			}
		}


		// Method makes the DeleteRec dialogue invisible
		private void clear() {
			recID.setText("");
			setVisible(false);
		}
	}
}