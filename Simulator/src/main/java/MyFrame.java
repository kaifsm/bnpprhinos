import javax.swing.*;
import org.json.JSONObject;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

class MyFrame extends JFrame 
{
	private static final long serialVersionUID = -4078361198228290265L;

	static final int highVolumeMsgNum = 100000;
	
	static final int TEXT_FIELD_SIZE = 30;

	private ImageIcon rhinoIcon = new ImageIcon("resources/rhino.png");
	
	private static final JButton case1 = new JButton("Repeated rejections");
	private static final JButton case2 = new JButton("Repeated cancellations");
	private static final JButton case3 = new JButton("High volume");
	private static final JButton case4 = new JButton("Network down");
	private static final JButton case5 = new JButton("Incorrect price range");
	private static final JButton case6 = new JButton("Failover");
	private static final JButton case7 = new JButton("Market data delay");

	private JTextField serverAddressTextField = new JTextField();
	private JTextField serverPortTextField    = new JTextField();

	private JLabel serverAddressLabel = new JLabel("Server address :");
	private JLabel serverPortLabel    = new JLabel("Server Port :");
	private JLabel simulationsLabel   = new JLabel("Simulations");

	private JTextArea eventsLogTextArea = new JTextArea("Events log", 200, 600);

    private static int VERTICAL_STRUT_LENGTH = 5;
    
	private String hostIP;
	private int hostPort;

	private ServerConnection serverConnection;
	
	static class EventInfo
	{
		static JTextField numberOfEventsTextField 	= new JTextField(TEXT_FIELD_SIZE);
		static JTextField issueTypeTextField 		= new JTextField(TEXT_FIELD_SIZE);
	    static JTextField impactedSystemsTextField 	= new JTextField(TEXT_FIELD_SIZE);
	    static JTextField hostNameTextField 		= new JTextField(TEXT_FIELD_SIZE);
	    static JTextField impactedMarketsTextField 	= new JTextField(TEXT_FIELD_SIZE);
	    static JTextField impactedFlowsTextField 	= new JTextField(TEXT_FIELD_SIZE);
	    static JTextField impactedClientsTextField 	= new JTextField(TEXT_FIELD_SIZE);
	    static JTextField originTextField 			= new JTextField(TEXT_FIELD_SIZE);
	    static JTextField flowTypeTextField 		= new JTextField(TEXT_FIELD_SIZE);
	    static JTextField pnlTextField 				= new JTextField(TEXT_FIELD_SIZE);

	    static JLabel numberOfEventsLabel			= new JLabel("Number of events: ");
		static JLabel issueTypeLabel 				= new JLabel("Issue Type :");
		static JLabel impactedSystemsLabel     		= new JLabel("Impacted Systems :");
		static JLabel hostNameLabel   				= new JLabel("Host Name: ");
		static JLabel impactedMarketsLabel 			= new JLabel("Impacted Markets :");
		static JLabel impactedFlowsLabel 			= new JLabel("Impacted Flows: ");
		static JLabel impactedClientsLabel 			= new JLabel("Impacted Clients: ");
		static JLabel originLabel 					= new JLabel("Issue Origin: ");
		static JLabel flowTypeLabel 				= new JLabel("Flow Type: ");
		static JLabel pnlLabel 						= new JLabel("PNL: ");
	}
	
	static class JsonKeys
	{
		static String issueTypeKey 		 	= new String("issue type"); 
		static String impactedSystemsKey 	= new String("impacted systems");
		static String hostnameKey 		 	= new String("hostname");
		static String impactedMarketsKey 	= new String("impacted markets");
		static String impactedFlowsKey 		= new String("impacted flows");
		static String impactedClientsKey 	= new String("impacted clients");
		static String originKey 			= new String("origin");
		static String flowTypeKey 			= new String("flow type");
		static String pnlKey				= new String("pnl");
		static String timestampKey			= new String("timestamp");
	}
	
	private static final Map<String, String> caseIDtoDefaultText; 
	private static final Map<String, String> caseIDtoDefaultCount; //easier to use String for the count, because it will be put into a text field
	
	static {
		caseIDtoDefaultText = new HashMap<String, String>();
		caseIDtoDefaultText.put(case1.getText(), "Reject"); //for AI to generate RepeatedRejects message
		caseIDtoDefaultText.put(case2.getText(), "Cancel"); //for AI to generate RepeatedCancels message
		caseIDtoDefaultText.put(case3.getText(), "Exec");   //for AI to generate HighVolume message
		caseIDtoDefaultText.put(case4.getText(), "NetworkDisconnection");
		caseIDtoDefaultText.put(case5.getText(), "IncorrectPriceRange");
		caseIDtoDefaultText.put(case6.getText(), "Failover");
		caseIDtoDefaultText.put(case7.getText(), "MarketDataSlowness");
		
		caseIDtoDefaultCount = new HashMap<String, String>();
		caseIDtoDefaultCount.put(case2.getText(), "500");
		caseIDtoDefaultCount.put(case3.getText(), "500");
		caseIDtoDefaultCount.put(case1.getText(), "500");
		caseIDtoDefaultCount.put(case4.getText(), "1");
		caseIDtoDefaultCount.put(case5.getText(), "1");
		caseIDtoDefaultCount.put(case6.getText(), "1");
		caseIDtoDefaultCount.put(case7.getText(), "1");
	};



	
	public MyFrame() 
	{
		setTitle("BNPP RHINOS SIMULATOR");
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
		case7.setBounds(20, 280, 200, 25);


		serverAddressTextField.setBounds(150, 10, 100, 20);
		serverPortTextField.setBounds(150, 35, 100, 20);

		serverAddressLabel.setBounds(20, 10, 150, 20);
		serverAddressLabel.setFont(new Font(serverAddressLabel.getFont().getName(), Font.BOLD, 14));
		serverPortLabel.setBounds(20, 35, 150, 20);
		serverPortLabel.setFont(new Font(serverPortLabel.getFont().getName(), Font.BOLD, 14));

		simulationsLabel.setBounds(20, 75, 150, 20);
		simulationsLabel.setFont(new Font(simulationsLabel.getFont().getName(), Font.BOLD, 18));

		eventsLogTextArea.setBounds(20, 330, 350, 200);

		add(eventsLogTextArea);

		add(simulationsLabel);

		add(case1);
		add(case2);
		add(case3);
		add(case4);
		add(case5);
		add(case6);
		add(case7);

		add(serverAddressLabel);
		add(serverPortLabel);

		add(serverAddressTextField);
		add(serverPortTextField);
	}

	private void initEvent() 
	{
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(1);
			}
		});

		case1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnPressedActionHandler(e, case1.getText());
			}
		});

		case2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnPressedActionHandler(e, case2.getText());
			}
		});

		case3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnPressedActionHandler(e, case3.getText());
			}
		});

		case4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnPressedActionHandler(e, case4.getText());
			}
		});

		case5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnPressedActionHandler(e, case5.getText());
			}
		});

		case6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnPressedActionHandler(e, case6.getText());
			}
		});
		
		case7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnPressedActionHandler(e, case7.getText());
			}
		});
	}

	private void extractServerConnectionDetails()
	{
		hostIP = serverAddressTextField.getText();
		hostPort = Integer.parseInt(serverPortTextField.getText());
	}

	private void connectToServer()
	{
		try
		{
			eventsLogTextArea.setText("Connecting to server at host: " + hostIP + " and port: " + hostPort);
			serverConnection = new ServerConnection(hostIP, hostPort);
			serverConnection.connect();
			eventsLogTextArea.setText("Server connection established");
		}
		catch(IOException ex)
		{
			JOptionPane.showMessageDialog(null, "IOException while connecting to server: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	private void showInfoInputDialog(ActionEvent evt, JSONObject incidentJsonObject, AtomicInteger eventCount, String defaultIssueType, String defaultNoofEvents)
	{
	      JPanel myPanel = new JPanel();
	      myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));
	      myPanel.add(EventInfo.numberOfEventsLabel);
	      myPanel.add(EventInfo.numberOfEventsTextField);
	      myPanel.add(Box.createVerticalStrut(VERTICAL_STRUT_LENGTH));
	      myPanel.add(EventInfo.issueTypeLabel);
	      myPanel.add(EventInfo.issueTypeTextField);
	      myPanel.add(Box.createVerticalStrut(VERTICAL_STRUT_LENGTH));
	      myPanel.add(EventInfo.impactedSystemsLabel);
	      myPanel.add(EventInfo.impactedSystemsTextField);
	      myPanel.add(Box.createVerticalStrut(VERTICAL_STRUT_LENGTH));
	      myPanel.add(EventInfo.hostNameLabel);
	      myPanel.add(EventInfo.hostNameTextField);
	      myPanel.add(Box.createVerticalStrut(VERTICAL_STRUT_LENGTH));
	      myPanel.add(EventInfo.impactedMarketsLabel);
	      myPanel.add(EventInfo.impactedMarketsTextField);
	      myPanel.add(Box.createVerticalStrut(VERTICAL_STRUT_LENGTH));
	      myPanel.add(EventInfo.impactedFlowsLabel);
	      myPanel.add(EventInfo.impactedFlowsTextField);
	      myPanel.add(Box.createVerticalStrut(VERTICAL_STRUT_LENGTH));
	      myPanel.add(EventInfo.impactedClientsLabel);
	      myPanel.add(EventInfo.impactedClientsTextField);	      
	      myPanel.add(Box.createVerticalStrut(VERTICAL_STRUT_LENGTH));
	      myPanel.add(EventInfo.originLabel);
	      myPanel.add(EventInfo.originTextField);
	      myPanel.add(Box.createVerticalStrut(VERTICAL_STRUT_LENGTH));
	      myPanel.add(EventInfo.flowTypeLabel);
	      myPanel.add(EventInfo.flowTypeTextField);
	      myPanel.add(Box.createVerticalStrut(VERTICAL_STRUT_LENGTH));
	      myPanel.add(EventInfo.pnlLabel);
	      myPanel.add(EventInfo.pnlTextField);
	      
	      EventInfo.issueTypeTextField.setText(defaultIssueType);
	      EventInfo.numberOfEventsTextField.setText(defaultNoofEvents);
	      int result = JOptionPane.showConfirmDialog(null, myPanel, 
	               		"Please provide incident details", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, rhinoIcon);
	      
	      if (result == JOptionPane.OK_OPTION) 
	      {
	    	  try
	    	  {
	    		  eventCount.set( Integer.parseInt(EventInfo.numberOfEventsTextField.getText()) );
		    	  incidentJsonObject.put( JsonKeys.issueTypeKey, EventInfo.issueTypeTextField.getText() );
		    	  incidentJsonObject.put( JsonKeys.impactedSystemsKey, EventInfo.impactedSystemsTextField.getText() );
		    	  incidentJsonObject.put( JsonKeys.hostnameKey, EventInfo.hostNameTextField.getText() );
		    	  incidentJsonObject.put( JsonKeys.impactedMarketsKey, EventInfo.impactedMarketsTextField.getText() );
		    	  incidentJsonObject.put( JsonKeys.impactedFlowsKey, EventInfo.impactedFlowsTextField.getText() );
		    	  incidentJsonObject.put( JsonKeys.impactedClientsKey, EventInfo.impactedClientsTextField.getText() );
		    	  incidentJsonObject.put( JsonKeys.originKey, EventInfo.originTextField.getText() );
		    	  incidentJsonObject.put( JsonKeys.flowTypeKey, EventInfo.flowTypeTextField.getText() );
		    	  incidentJsonObject.put( JsonKeys.pnlKey, EventInfo.pnlTextField.getText() );
		    	  incidentJsonObject.put( JsonKeys.timestampKey, new Date().toString() );
	    	  }
	    	  catch(Exception ex)
	    	  {
	    		  System.out.println( "Exception: " + ex.getMessage() );
	    	  }
	      }
	}
	
	static private String getDefaultTextFromCaseID(String caseID)
	{
		Map<String, String> caseIDtoDefaultText = new HashMap<String, String>();
		return caseID;
		
	}
	
	private void btnPressedActionHandler(ActionEvent evt, String caseID)
	{
		eventsLogTextArea.setText("");
		eventsLogTextArea.setText("Case: " + caseID);
		eventsLogTextArea.append("\nPopulating event info input dialog . . .");		
		
		extractServerConnectionDetails();
		connectToServer();		
		
		AtomicInteger eventCount = new AtomicInteger();
		JSONObject incidentJsonObject = new JSONObject();
		showInfoInputDialog(evt, incidentJsonObject, eventCount, caseIDtoDefaultText.get(caseID),caseIDtoDefaultCount.get(caseID));
		eventsLogTextArea.append("\nExtracting event info . . .");
		
		if(serverConnection.isConnected())
		{
			eventsLogTextArea.append( "\nSending events to server . . ." );

			try 
			{
				final int count = eventCount.get();
				for( int i = 0; i < count; ++i )
				{
					serverConnection.writeToOutput( incidentJsonObject.toString() );	
				}				
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				System.out.println( "Exception occured: " + e.getMessage() );
				eventsLogTextArea.append( "\nException occured: " + e.getMessage() );
			}			
		}	
		
		eventsLogTextArea.append("\nEvents successfully sent to server");
		serverConnection.killConnection();
		eventsLogTextArea.append("\nCase completed");
	}
}