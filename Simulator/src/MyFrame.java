import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

class MyFrame extends JFrame {

	private static final long serialVersionUID = -4078361198228290265L;

	static final int highVolumeMsgNum = 100000;

	private JButton case1 = new JButton("Repeated rejections");
	private JButton case2 = new JButton("Repeated cancellations");
	private JButton case3 = new JButton("High volume");
	private JButton case4 = new JButton("Network down");
	private JButton case5 = new JButton("Incorrect price range");
	private JButton case6 = new JButton("Failover");

	private JTextField txtA = new JTextField();
	private JTextField txtB = new JTextField();

	private JLabel lblA = new JLabel("Server address :");
	private JLabel lblB = new JLabel("Server Port :");
	private JLabel simulations = new JLabel("Simulations");

	private JTextArea textArea = new JTextArea("Events log", 200, 600);

	private String hostIP;
	private int hostPort;

	private ServerConnection serverConnection;

	public MyFrame() {
		setTitle("Simulator");
		setSize(400, 600);
		setLocation(new Point(300, 200));
		setLayout(null);
		setResizable(true);

		initComponent();
		initEvent();
	}

	private void initComponent() {
		case1.setBounds(20, 100, 200, 25);
		case2.setBounds(20, 130, 200, 25);
		case3.setBounds(20, 160, 200, 25);
		case4.setBounds(20, 190, 200, 25);
		case5.setBounds(20, 220, 200, 25);
		case6.setBounds(20, 250, 200, 25);


		txtA.setBounds(150, 10, 100, 20);
		txtB.setBounds(150, 35, 100, 20);

		lblA.setBounds(20, 10, 150, 20);
		lblA.setFont(new Font(lblA.getFont().getName(), Font.BOLD, 14));
		lblB.setBounds(20, 35, 150, 20);
		lblB.setFont(new Font(lblB.getFont().getName(), Font.BOLD, 14));

		simulations.setBounds(20, 75, 150, 20);
		simulations.setFont(new Font(simulations.getFont().getName(), Font.BOLD, 18));

		textArea.setBounds(20, 300, 350, 200);

		add(textArea);

		add(simulations);

		add(case1);
		add(case2);
		add(case3);
		add(case4);
		add(case5);
		add(case6);

		add(lblA);
		add(lblB);

		add(txtA);
		add(txtB);
	}

	private void initEvent() {

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(1);
			}
		});

		case1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnPressedCase1(e);
			}
		});

		case2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnPressedCase2(e);
			}
		});

		case3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnPressedCase3(e);
			}
		});

		case4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnPressedCase4(e);
			}
		});

		case5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnPressedCase5(e);
			}
		});

		case6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnPressedCase6(e);
			}
		});
	}

	private void extractServerConnectionDetails()
	{
		hostIP = txtA.getText();
		hostPort = Integer.parseInt(txtB.getText());
	}

	private void connectToServer()
	{
		try
		{
			textArea.setText("Connecting to server at host: " + hostIP + " and port: " + hostPort);
			serverConnection = new ServerConnection(hostIP, hostPort);
			serverConnection.connect();
			textArea.setText("Server connection established");
		}
		catch(IOException ex)
		{
			JOptionPane.showMessageDialog(null, "IOException while connecting to server: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	private void btnPressedCase1(ActionEvent evt)
	{
		textArea.setText("");
		textArea.setText("Case: Repeated rejections");
		extractServerConnectionDetails();
		connectToServer();
		if(serverConnection.isConnected())
		{
			textArea.append("\nSending events to server . . .");
			final String str = "{\"name\": \"Rejection\"}";
			for (int i = 0; i < highVolumeMsgNum; ++i)
			{
				try
				{
					serverConnection.writeToOutput(str);
				}
				catch (IOException e)
				{
					JOptionPane.showMessageDialog(null, "IOException while connecting to server: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
		textArea.append("\nEvents successfully sent to server");
		serverConnection.killConnection();
		textArea.append("\nCase completed");
	}

	private void btnPressedCase2(ActionEvent evt)
	{
		textArea.setText("");
		textArea.setText("Case: Repeated cancellations");
		extractServerConnectionDetails();
		connectToServer();
		if(serverConnection.isConnected())
		{
			try
			{
				textArea.append("\nSending events to server . . .");
				String str = "{\"name\": \"Cancellation\"}";
				for( int i = 0; i < highVolumeMsgNum; ++i )
				{
					serverConnection.writeToOutput(str);
				}
				textArea.append("\nEvents successfully sent to server");
				serverConnection.killConnection();
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(null, "IOException while connecting to server: " + e.getMessage());
				e.printStackTrace();
			}
		}
		textArea.append("\nCase completed");
	}

	private void btnPressedCase3(ActionEvent evt)
	{
		textArea.setText("");
		textArea.setText("Case: High volumes");
		extractServerConnectionDetails();
		connectToServer();
		if(serverConnection.isConnected())
		{
			try
			{
				textArea.append("\nSending events to server . . .");
				String str = "{\"name\": \"Order\"}";
				for( int i = 0; i < highVolumeMsgNum; ++i )
				{
					serverConnection.writeToOutput(str);
				}
				textArea.append("\nEvents successfully sent to server");
				serverConnection.killConnection();
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(null, "IOException while connecting to server: " + e.getMessage());
				e.printStackTrace();
			}
		}
		textArea.append("\nCase completed");
	}

	private void btnPressedCase4(ActionEvent evt)
	{
		textArea.setText("");
		textArea.setText("Case: Network down");
		extractServerConnectionDetails();
		connectToServer();
		if(serverConnection.isConnected())
		{
			try
			{
				textArea.append("\nSending event to server . . .");
				String str = "{\"name\": \"Network down\"}";
				for( int i = 0; i < highVolumeMsgNum; ++i )
				{
					serverConnection.writeToOutput(str);
				}
				textArea.append("\nEvent successfully sent to server");
				serverConnection.killConnection();
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(null, "IOException while connecting to server: " + e.getMessage());
				e.printStackTrace();
			}
		}
		textArea.append("\nCase completed");
	}

	private void btnPressedCase5(ActionEvent evt)
	{
		textArea.setText("");
		textArea.setText("Case: Incorrect price range");
		extractServerConnectionDetails();
		connectToServer();
		if(serverConnection.isConnected())
		{
			try
			{
				textArea.append("\nSending events to server . . .");
				String str = "{\"name\": \"Incorrect price range\"}";
				for( int i = 0; i < highVolumeMsgNum; ++i )
				{
					serverConnection.writeToOutput(str);
				}
				textArea.append("\nEvents successfully sent to server");
				serverConnection.killConnection();
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(null, "IOException while connecting to server: " + e.getMessage());
				e.printStackTrace();
			}
		}
		textArea.append("\nCase completed");
	}

	private void btnPressedCase6(ActionEvent evt)
	{
		textArea.setText("");
		textArea.setText("Case: Failover");
		extractServerConnectionDetails();
		connectToServer();
		if(serverConnection.isConnected())
		{
			try
			{
				textArea.append("\nSending event to server . . .");
				String str = "{\"name\": \"Failover\"}";
				for( int i = 0; i < highVolumeMsgNum; ++i )
				{
					serverConnection.writeToOutput(str);
				}
				textArea.append("\nEvents successfully sent to server");
				serverConnection.killConnection();
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(null, "IOException while connecting to server: " + e.getMessage());
				e.printStackTrace();
			}
		}
		textArea.append("\nCase completed");
	}
}