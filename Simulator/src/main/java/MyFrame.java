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
	
	private static final JButton repeatedRejections = new JButton("Repeated rejections");
	private static final JButton repeatedCancellations = new JButton("Repeated cancellations");
	private static final JButton highVolume = new JButton("High volume");
	private static final JButton networkDown = new JButton("Network down");
	private static final JButton incorrectPriceRange = new JButton("Incorrect price range");
	private static final JButton failover = new JButton("Failover");
	private static final JButton marketDataDelay = new JButton("Market data delay");

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
	
	private static final Map<String, String> caseIDtoissueType; 
	private static final Map<String, String> caseIDtoNoOfEvents; //easier to use String for the count, because it will be put into a text field
	private static final Map<String, String> caseIDtoimpactedSystems;
	private static final Map<String, String> caseIDtoHostname;
	private static final Map<String, String> caseIDtoImpactedMarkets;
	private static final Map<String, String> caseIDtoImpactedFlows;
	private static final Map<String, String> caseIDtoImpactedClients;
	private static final Map<String, String> caseIDtoOriginText;
	private static final Map<String, String> caseIDtoFlowType;
	private static final Map<String, String> caseIDtoPnL;
	
	static {
		caseIDtoissueType = new HashMap<String, String>();
		caseIDtoissueType.put(repeatedRejections.getText(), "Reject"); //for AI to generate RepeatedRejects message
		caseIDtoissueType.put(repeatedCancellations.getText(), "Cancel"); //for AI to generate RepeatedCancels message
		caseIDtoissueType.put(highVolume.getText(), "Exec");   //for AI to generate HighVolume message
		caseIDtoissueType.put(networkDown.getText(), "NetworkDown");
		caseIDtoissueType.put(incorrectPriceRange.getText(), "IncorrectPriceRange");
		caseIDtoissueType.put(failover.getText(), "Failover");
		caseIDtoissueType.put(marketDataDelay.getText(), "MarketDataSlowness");
		
		caseIDtoNoOfEvents = new HashMap<String, String>();
		caseIDtoNoOfEvents.put(repeatedRejections.getText(), "500");
		caseIDtoNoOfEvents.put(repeatedCancellations.getText(), "500");
		caseIDtoNoOfEvents.put(highVolume.getText(), "500");
		caseIDtoNoOfEvents.put(networkDown.getText(), "1");
		caseIDtoNoOfEvents.put(incorrectPriceRange.getText(), "1");
		caseIDtoNoOfEvents.put(failover.getText(), "1");
		caseIDtoNoOfEvents.put(marketDataDelay.getText(), "1");
		
		caseIDtoimpactedSystems = new HashMap<String, String>();
		caseIDtoimpactedSystems.put(repeatedCancellations.getText(), "AlgoEngine");
		caseIDtoimpactedSystems.put(networkDown.getText(), "FIXGateway");
		caseIDtoimpactedSystems.put(marketDataDelay.getText(), "MarketAccess");

		caseIDtoHostname = new HashMap<String, String>();
		caseIDtoHostname.put(repeatedCancellations.getText(), "bnpphkserver02");
		caseIDtoHostname.put(networkDown.getText(), "bnpphkserver01");
		caseIDtoHostname.put(marketDataDelay.getText(), "bnpphkserver03");
		
		caseIDtoImpactedClients = new HashMap<String, String>();
		caseIDtoImpactedClients.put(repeatedCancellations.getText(), "BNPPInternal");
		caseIDtoImpactedClients.put(networkDown.getText(), "BNPPInternal");
		caseIDtoImpactedClients.put(marketDataDelay.getText(), "BNPPInternal");
		
		caseIDtoImpactedMarkets = new HashMap<String, String>();
		caseIDtoImpactedMarkets.put(repeatedCancellations.getText(), "HKEX");
		caseIDtoImpactedMarkets.put(marketDataDelay.getText(), "HKEX");
		
		caseIDtoPnL = new HashMap<String, String>();
		caseIDtoPnL.put(repeatedCancellations.getText(), "3830000");
		caseIDtoPnL.put(networkDown.getText(), "850000");
		caseIDtoPnL.put(marketDataDelay.getText(), "1750000");
		
		caseIDtoImpactedFlows = new HashMap<String, String>(); 
		caseIDtoImpactedFlows.put(networkDown.getText(), "Cash");
		caseIDtoImpactedFlows.put(marketDataDelay.getText(), "Cash");
		
		caseIDtoOriginText = new HashMap<String, String>(); 
		
		caseIDtoFlowType = new HashMap<String, String>(); 
		
//	      caseIDtoimpactedSystems.get(caseID));
//	      EventInfo.hostNameTextField.setText(caseIDtoHostname.get(caseID));
//	      EventInfo.impactedMarketsTextField.setText(caseIDtoImpactedMarkets.get(caseID));
//	      EventInfo.impactedFlowsTextField.setText(caseIDtoImpactedFlows.get(caseID));
//	      EventInfo.impactedClientsTextField.setText(caseIDtoImpactedClients.get(caseID));
//	      EventInfo.originTextField.setText(caseIDtoOriginText.get(caseID));
//	      EventInfo.flowTypeTextField.setText(caseIDtoFlowType.get(caseID));
//	      EventInfo.pnlTextField.setText(caseIDtoPnL.get(caseID));
		
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
		repeatedRejections.setBounds(20, 100, 200, 25);
		repeatedCancellations.setBounds(20, 130, 200, 25);
		highVolume.setBounds(20, 160, 200, 25);
		networkDown.setBounds(20, 190, 200, 25);
		incorrectPriceRange.setBounds(20, 220, 200, 25);
		failover.setBounds(20, 250, 200, 25);
		marketDataDelay.setBounds(20, 280, 200, 25);


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

		add(repeatedRejections);
		add(repeatedCancellations);
		add(highVolume);
		add(networkDown);
		add(incorrectPriceRange);
		add(failover);
		add(marketDataDelay);

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

		repeatedRejections.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnPressedActionHandler(e, repeatedRejections.getText());
			}
		});

		repeatedCancellations.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnPressedActionHandler(e, repeatedCancellations.getText());
			}
		});

		highVolume.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnPressedActionHandler(e, highVolume.getText());
			}
		});

		networkDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnPressedActionHandler(e, networkDown.getText());
			}
		});

		incorrectPriceRange.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnPressedActionHandler(e, incorrectPriceRange.getText());
			}
		});

		failover.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnPressedActionHandler(e, failover.getText());
			}
		});
		
		marketDataDelay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnPressedActionHandler(e, marketDataDelay.getText());
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

	private void showInfoInputDialog(ActionEvent evt, JSONObject incidentJsonObject, AtomicInteger eventCount, String caseID)
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
	      
	      
	      EventInfo.numberOfEventsTextField.setText(caseIDtoNoOfEvents.get(caseID));
	      EventInfo.issueTypeTextField.setText(caseIDtoissueType.get(caseID));
	      EventInfo.impactedSystemsTextField.setText(caseIDtoimpactedSystems.get(caseID));
	      EventInfo.hostNameTextField.setText(caseIDtoHostname.get(caseID));
	      EventInfo.impactedMarketsTextField.setText(caseIDtoImpactedMarkets.get(caseID));
	      EventInfo.impactedFlowsTextField.setText(caseIDtoImpactedFlows.get(caseID));
	      EventInfo.impactedClientsTextField.setText(caseIDtoImpactedClients.get(caseID));
	      EventInfo.originTextField.setText(caseIDtoOriginText.get(caseID));
	      EventInfo.flowTypeTextField.setText(caseIDtoFlowType.get(caseID));
	      EventInfo.pnlTextField.setText(caseIDtoPnL.get(caseID));
	      
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
		showInfoInputDialog(evt, incidentJsonObject, eventCount, caseID);
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