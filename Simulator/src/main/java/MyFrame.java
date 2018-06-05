import javax.swing.*;
import org.json.JSONObject;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

class MyFrame extends JFrame {
    private static final long serialVersionUID = -4078361198228290265L;

    static final int TEXT_FIELD_SIZE = 30;

    private ImageIcon rhinoIcon = new ImageIcon("resources/rhino.png");

    private JTextField serverAddressTextField = new JTextField();
    private JTextField serverPortTextField = new JTextField();

    private JLabel serverAddressLabel = new JLabel("Server address :");
    private JLabel serverPortLabel = new JLabel("Server Port :");
    private JLabel simulationsLabel = new JLabel("Simulations");

    private JTextArea eventsLogTextArea = new JTextArea("Events log", 200, 600);

    private static int VERTICAL_STRUT_LENGTH = 5;

    private String hostIP;
    private int hostPort;

    private ServerConnection serverConnection;


    private Map<String,DefaultValue> caseIdToDefaultValue;

    private static final long startTimeInNano = System.nanoTime();

    class DefaultValue
    {
        String _numberOfEvents;
        String _issueType;
        String _impactedSystems;
        String _hostName;
        String _impactedMarkets;
        String _impactedFlows;
        String _impactedClients;
        String _origin;
        String _flowType;
        String _pnl;

        DefaultValue( String numEvents_, String issueType_, String impactedSystems_, String hostName_,
                      String impactedMarkets_, String impactedFlows_, String impactedClients_,
                        String origin_, String flowType_, String pnl_ )
        {
            _numberOfEvents = numEvents_;
            _issueType = issueType_;
            _impactedMarkets = impactedMarkets_;
            _impactedSystems = impactedSystems_;
            _hostName = hostName_;
            _impactedFlows = impactedFlows_;
            _impactedClients = impactedClients_;
            _origin = origin_;
            _flowType = flowType_;
            _pnl = pnl_;
        }
    }

    private static class Buttons
    {
        private static final JButton case1 = new JButton("Repeated rejections");
        private static final JButton case2 = new JButton("Repeated cancellations");
        private static final JButton case3 = new JButton("High volume");
        private static final JButton case4 = new JButton("Network down");
        private static final JButton case5 = new JButton("Incorrect price range");
        private static final JButton case6 = new JButton("Failover");
        private static final JButton case7 = new JButton("Market data slowness");
    }

    static class EventInfo
    {
        static JTextField numberOfEventsTextField = new JTextField(TEXT_FIELD_SIZE);
        static JTextField issueTypeTextField = new JTextField(TEXT_FIELD_SIZE);
        static JTextField impactedSystemsTextField = new JTextField(TEXT_FIELD_SIZE);
        static JTextField hostNameTextField = new JTextField(TEXT_FIELD_SIZE);
        static JTextField impactedMarketsTextField = new JTextField(TEXT_FIELD_SIZE);
        static JTextField impactedFlowsTextField = new JTextField(TEXT_FIELD_SIZE);
        static JTextField impactedClientsTextField = new JTextField(TEXT_FIELD_SIZE);
        static JTextField originTextField = new JTextField(TEXT_FIELD_SIZE);
        static JTextField flowTypeTextField = new JTextField(TEXT_FIELD_SIZE);
        static JTextField pnlTextField = new JTextField(TEXT_FIELD_SIZE);

        static JLabel numberOfEventsLabel = new JLabel("Number of events: ");
        static JLabel issueTypeLabel = new JLabel("Issue Type :");
        static JLabel impactedSystemsLabel = new JLabel("Impacted Systems :");
        static JLabel hostNameLabel = new JLabel("Host Name: ");
        static JLabel impactedMarketsLabel = new JLabel("Impacted Markets :");
        static JLabel impactedFlowsLabel = new JLabel("Impacted Flows: ");
        static JLabel impactedClientsLabel = new JLabel("Impacted Clients: ");
        static JLabel originLabel = new JLabel("Issue Origin: ");
        static JLabel flowTypeLabel = new JLabel("Flow Type: ");
        static JLabel pnlLabel = new JLabel("PNL: ");
    }

    static class JsonKeys
    {
        static String issueTypeKey = "issue type";
        static String impactedSystemsKey = "impacted systems";
        static String hostnameKey = "hostname";
        static String impactedMarketsKey = "impacted markets";
        static String impactedFlowsKey = "impacted flows";
        static String impactedClientsKey = "impacted clients";
        static String originKey = "origin";
        static String flowTypeKey = "flow type";
        static String pnlKey = "pnl";
        static String timestampKey = "timestamp";
        static String latencyInMs = "LatencyInMs";
    }

    MyFrame()
    {
        setTitle("BNPP RHINOS SIMULATOR");
        setSize(400, 600);
        setLocation(new Point(300, 200));
        setLayout(null);
        setResizable(true);
        caseIdToDefaultValue = new HashMap<>();
        initializeDefaults();
        initComponent();
        initEvent();
    }

    private void initializeDefaults()
    {
        DefaultValue rejectionsDefault = new DefaultValue("500", "Reject", "OMS",
                                                "productionHost", "NSE, BSE", "Cash",
                                                    "VIP", "trading", "agency", "1000000");

        caseIdToDefaultValue.put(Buttons.case1.getText(), rejectionsDefault);

        DefaultValue cancellationsDefault = new DefaultValue("500", "RepeatedCancels", "AlgoEngine",
                                                "bnpphkserver02", "HKEX", "Cash",
                                                    "BNPPInternal", "ETRADING", "ALL", "3830000");

        caseIdToDefaultValue.put(Buttons.case2.getText(), cancellationsDefault);

        DefaultValue executionsDefault = new DefaultValue("500", "Exec", "OMS",
                                                "productionHost", "NSE, BSE", "Cash",
                                                    "VIP", "trading", "agency", "1000000");
        caseIdToDefaultValue.put(Buttons.case3.getText(), executionsDefault);

        DefaultValue networkDownDefault = new DefaultValue("1", "NetworkDown", "FIXGateway",
                                                "bnpphkserver01", "HKEX,SGX", "Cash",
                                                    "BusinessWarrior", "ETRADING", "ALL", "850000");
        caseIdToDefaultValue.put(Buttons.case4.getText(), networkDownDefault);

        DefaultValue incorrectPriceRangeDefault = new DefaultValue("1", "IncorrectPriceRange", "OMS",
                                                    "productionHost", "NSE, BSE", "Cash",
                                                        "VIP", "trading", "agency", "1000000");
        caseIdToDefaultValue.put(Buttons.case5.getText(), incorrectPriceRangeDefault);

        DefaultValue failoverDefault = new DefaultValue("1", "Failover", "OMS",
                                        "productionHost", "NSE, BSE", "Cash",
                                            "VIP", "trading", "agency", "1000000");
        caseIdToDefaultValue.put(Buttons.case6.getText(), failoverDefault);

        DefaultValue marketDataSlownessDefault = new DefaultValue("1", "MarketDataSlowness", "MarketAccess,AlgoEngine",
                                                    "bnpphkserver03", "HKEX", "Cash",
                                                        "BNPPInternal", "ETrading", "Agency", "1750000");
        caseIdToDefaultValue.put(Buttons.case7.getText(), marketDataSlownessDefault);
    }

	private void initComponent()
    {
		Buttons.case1.setBounds(20, 100, 200, 25);
        Buttons.case2.setBounds(20, 130, 200, 25);
        Buttons.case3.setBounds(20, 160, 200, 25);
        Buttons.case4.setBounds(20, 190, 200, 25);
        Buttons.case5.setBounds(20, 220, 200, 25);
        Buttons.case6.setBounds(20, 250, 200, 25);
        Buttons.case7.setBounds(20, 280, 200, 25);


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

		add(Buttons.case1);
		add(Buttons.case2);
		add(Buttons.case3);
		add(Buttons.case4);
		add(Buttons.case5);
		add(Buttons.case6);
		add(Buttons.case7);

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

        Buttons.case1.addActionListener(e -> btnPressedActionHandler(e, Buttons.case1.getText()));

        Buttons.case2.addActionListener(e -> btnPressedActionHandler(e, Buttons.case2.getText()));

        Buttons.case3.addActionListener(e -> btnPressedActionHandler(e, Buttons.case3.getText()));

        Buttons.case4.addActionListener(e -> btnPressedActionHandler(e, Buttons.case4.getText()));

        Buttons.case5.addActionListener(e -> btnPressedActionHandler(e, Buttons.case5.getText()));

        Buttons.case6.addActionListener(e -> btnPressedActionHandler(e, Buttons.case6.getText()));

        Buttons.case7.addActionListener(e -> btnPressedActionHandler(e, Buttons.case7.getText()));
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

	private void showInfoInputDialog(ActionEvent evt, JSONObject incidentJsonObject, AtomicInteger eventCount, DefaultValue defaultValue)
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
	      
	      EventInfo.issueTypeTextField.setText(defaultValue._issueType);
	      EventInfo.numberOfEventsTextField.setText(defaultValue._numberOfEvents);
	      EventInfo.impactedSystemsTextField.setText(defaultValue._impactedSystems);
	      EventInfo.hostNameTextField.setText(defaultValue._hostName);
	      EventInfo.impactedMarketsTextField.setText(defaultValue._impactedMarkets);
	      EventInfo.impactedFlowsTextField.setText(defaultValue._impactedFlows);
	      EventInfo.impactedClientsTextField.setText(defaultValue._impactedClients);
	      EventInfo.originTextField.setText(defaultValue._origin);
	      EventInfo.flowTypeTextField.setText(defaultValue._flowType);
	      EventInfo.pnlTextField.setText(defaultValue._pnl);

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
		    	  long endTimeInNano = System.nanoTime();
		    	  incidentJsonObject.put( JsonKeys.timestampKey, (endTimeInNano-startTimeInNano) );
		    	  if(EventInfo.issueTypeTextField.getText().compareTo("MarketDataSlowness") == 0)
                  {
                      incidentJsonObject.put( JsonKeys.latencyInMs, "500" );
                  }

	    	  }
	    	  catch(Exception ex)
	    	  {
	    		  System.out.println( "Exception: " + ex.getMessage() );
	    	  }
	      }
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
		DefaultValue defaultValue = caseIdToDefaultValue.get(caseID);

		showInfoInputDialog(evt, incidentJsonObject, eventCount, defaultValue);
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