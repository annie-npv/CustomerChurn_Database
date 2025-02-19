import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * ChurnCustomerApplication class represents a graphical user interface for displaying customer
 * data and performing various analyses on the data.
 * @author annienguyen
 */
public class ChurnCustomerApplication extends JFrame {
	private JTextField searchField;
	private JTextField filterValueField;
	private JTextArea resultArea;
	private Connection connection;
	private JPanel searchPanel;
	private PreparedStatement preparedStatement;
	private JButton filterButton;
	private JPanel filterPanel;
	private double averageMonthlyCharges = 0;
	private int tenure = 0;
	String churnAnalysisResult = null;
	private int senior = 0;

	// Initialize tableComboBox
	String[] tableOptions = { "Customer", "Service", "MonthlyCharges", "SeniorCitizen", "PartnerDependents",
			"InsightsStatistics" };
	JComboBox tableComboBox = new JComboBox<>(tableOptions);

	/**
	 * Constructs a new instance of TheGUI.
	 */
	public ChurnCustomerApplication() {
		super("Customer Data GUI");
		setSize(800, 400); // Increased width to accommodate additional stats panel
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		// Initialize searchPanel
		searchPanel = new JPanel(new FlowLayout());
		JLabel searchLabel = new JLabel("Search by Customer ID (Optional):");
		searchField = new JTextField(15);
		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchCustomer();
			}
		});
		searchPanel.add(searchLabel);
		searchPanel.add(searchField);
		searchPanel.add(searchButton);

		// Initialize filterPanel
		filterPanel = new JPanel(new FlowLayout());
		String[] filterOptions = { "Senior Citizen", "Tenure", "Paperless Billing" };
		JComboBox<String> filterComboBox = new JComboBox<>(filterOptions);
		filterValueField = new JTextField(10);
		filterButton = new JButton("Apply Filter");
		filterButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applyFilter((String) filterComboBox.getSelectedItem());
			}
		});
		filterPanel.add(filterComboBox);
		filterPanel.add(filterValueField);
		filterPanel.add(filterButton);

		// Add components to the frame
		add(searchPanel, BorderLayout.NORTH);
		add(tableComboBox, BorderLayout.WEST); // Add tableComboBox to the left side
		add(filterPanel, BorderLayout.SOUTH);

		// Result Panel
		resultArea = new JTextArea(10, 30);
		resultArea.setEditable(false);
		JScrollPane resultScrollPane = new JScrollPane(resultArea);
		resultScrollPane.setPreferredSize(new Dimension(380, 300)); // Adjust the size as needed
		add(resultScrollPane);

		// Connect to the database
		connectToDatabase();
	}

	/**
	 * Establishes a connection to the MySQL database.
	 */
	private void connectToDatabase() {
		try {
			String url = "jdbc:mysql://localhost/churndata?";
			String username = "root";
			String password = "";
			connection = DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			showErrorDialog("Failed to connect to the database.");
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Searches for customer data based on the entered customer ID.
	 */
	private void searchCustomer() {
		String searchTerm = searchField.getText().trim();

		// Clear the resultArea before displaying the new result
		resultArea.setText("");

		// Calculate additional statistics regardless of the selected table
		calculateAdditionalStatistics(searchTerm);

		try {
			// Retrieve additional information from related tables based on user selection
			String selectedTable = (String) tableComboBox.getSelectedItem();
			switch (selectedTable) {
			case "Service":
				displayServiceInfo(searchTerm);
				break;
			case "MonthlyCharges":
				displayMonthlyChargesInfo(searchTerm);
				break;
			case "SeniorCitizen":
				displaySeniorCitizenInfo(searchTerm);
				break;
			case "PartnerDependents":
				displayPartnerDependentsInfo(searchTerm);
				break;
			case "InsightsStatistics":
				displayInsightsStatistics(searchTerm);
				break;
			default:
				displayCustomerInfo(searchTerm);
			}
		} catch (SQLException e) {
			showErrorDialog("Failed to execute the query.");
			e.printStackTrace();
		}
	}

	/**
	 * Calculates additional statistics for the given customer.
	 * 
	 * @param searchTerm The customer ID for which statistics are calculated.
	 */
	private void calculateAdditionalStatistics(String searchTerm) {
		// Calculate additional statistics for the given customer
		calculateCLV(searchTerm); // Assuming averageMonthlyCharges and tenure are available
		isDiscountEligible(searchTerm); // Assuming tenure is available
		calculateNumberOfServices(searchTerm);
		calculateChurnPercentage();
	}

	/**
	 * Displays customer information based on the provided customer ID.
	 * 
	 * @param searchTerm The customer ID for which information is displayed.
	 * @throws SQLException If an SQL exception occurs.
	 */
	private void displayCustomerInfo(String searchTerm) throws SQLException {
		StringBuilder resultBuilder = new StringBuilder("Customer Details:\n");
		String query = "SELECT * FROM Customer";
		if (searchTerm.isEmpty()) {
			PreparedStatement customerStatement = connection.prepareStatement(query);
		}
		if (!searchTerm.isEmpty()) {
			query += " WHERE customerID = ?";
		}
		try (PreparedStatement customerStatement = connection.prepareStatement(query)) {
			if (!searchTerm.isEmpty()) {
				customerStatement.setString(1, searchTerm);
			}
			try (ResultSet customerResultSet = customerStatement.executeQuery()) {
				while (customerResultSet.next()) {
					resultBuilder.append("Customer ID: ").append(customerResultSet.getString("customerID"))
							.append("\n");
					resultBuilder.append("Gender: ").append(customerResultSet.getString("gender")).append("\n");
					resultBuilder.append("Senior Citizen: ").append(customerResultSet.getInt("SeniorCitizen"))
							.append("\n");
					resultBuilder.append("Partner: ").append(customerResultSet.getString("Partner")).append("\n");
					resultBuilder.append("Dependents: ").append(customerResultSet.getString("Dependents")).append("\n");
					resultBuilder.append("Tenure: ").append(customerResultSet.getInt("tenure")).append("\n\n");
					tenure = customerResultSet.getInt("tenure");
				}
			}
		}
		resultArea.append(resultBuilder.toString());
	}

	/**
	 * Displays service information based on the provided customer ID.
	 * 
	 * @param searchTerm The customer ID for which information is displayed.
	 * @throws SQLException If an SQL exception occurs.
	 */
	private void displayServiceInfo(String searchTerm) throws SQLException {
		StringBuilder resultBuilder = new StringBuilder("Service Details:\n");
		String query = "SELECT * FROM Service";
		if (searchTerm.isEmpty()) {
			PreparedStatement serviceStatement = connection.prepareStatement(query);
		}
		if (!searchTerm.isEmpty()) {
			query += " WHERE customerID = ?";
		}
		try (PreparedStatement serviceStatement = connection.prepareStatement(query)) {
			if (!searchTerm.isEmpty()) {
				serviceStatement.setString(1, searchTerm);
			}
			try (ResultSet serviceResultSet = serviceStatement.executeQuery()) {
				while (serviceResultSet.next()) {
					resultBuilder.append("Service ID: ").append(serviceResultSet.getInt("serviceID")).append("\n");
					resultBuilder.append("Phone Service: ").append(serviceResultSet.getString("PhoneService"))
							.append("\n");
					resultBuilder.append("Multiple Lines: ").append(serviceResultSet.getString("MultipleLines"))
							.append("\n");
					resultBuilder.append("Internet Service: ").append(serviceResultSet.getString("InternetService"))
							.append("\n");
					resultBuilder.append("Online Security: ").append(serviceResultSet.getString("OnlineSecurity"))
							.append("\n");
					resultBuilder.append("Online Backup: ").append(serviceResultSet.getString("OnlineBackup"))
							.append("\n");
					resultBuilder.append("Device Protection: ").append(serviceResultSet.getString("DeviceProtection"))
							.append("\n");
					resultBuilder.append("Tech Support: ").append(serviceResultSet.getString("TechSupport"))
							.append("\n");
					resultBuilder.append("Streaming TV: ").append(serviceResultSet.getString("StreamingTV"))
							.append("\n");
					resultBuilder.append("Streaming Movies: ").append(serviceResultSet.getString("StreamingMovies"))
							.append("\n");
					resultBuilder.append("Paperless Billing: ").append(serviceResultSet.getString("PaperlessBilling"))
							.append("\n");
					resultBuilder.append("Payment Method: ").append(serviceResultSet.getString("PaymentMethod"))
							.append("\n\n");
					// Add more service details as needed
				}
			}
		}
		resultArea.append(resultBuilder.toString());
	}

	/**
	 * Displays monthly charges information based on the provided customer ID.
	 * 
	 * @param searchTerm The customer ID for which information is displayed.
	 * @throws SQLException If an SQL exception occurs.
	 */
	private void displayMonthlyChargesInfo(String searchTerm) throws SQLException {
		StringBuilder resultBuilder = new StringBuilder("Monthly Charges:\n");
		String query = "SELECT * FROM MonthlyCharges";
		if (searchTerm.isEmpty()) {
			PreparedStatement chargesStatement = connection.prepareStatement(query);
		}
		if (!searchTerm.isEmpty()) {
			query += " WHERE customerID = ?";
		}
		try (PreparedStatement chargesStatement = connection.prepareStatement(query)) {
			if (!searchTerm.isEmpty()) {
				chargesStatement.setString(1, searchTerm);
			}
			try (ResultSet chargesResultSet = chargesStatement.executeQuery()) {
				while (chargesResultSet.next()) {
					resultBuilder.append("Charge ID: ").append(chargesResultSet.getInt("chargeID")).append("\n");
					resultBuilder.append("Monthly Charges: ")
							.append(String.valueOf(chargesResultSet.getDouble("MonthlyCharges"))).append("\n");
					resultBuilder.append("Total Charges: ")
							.append(String.valueOf(chargesResultSet.getDouble("TotalCharges"))).append("\n");
					resultBuilder.append("Churn: ").append(chargesResultSet.getString("Churn")).append("\n\n");
				}
			}
		}
		resultArea.append(resultBuilder.toString());
	}

	/**
	 * Displays senior citizen status information based on the provided customer ID.
	 * 
	 * @param searchTerm The customer ID for which information is displayed.
	 * @throws SQLException If an SQL exception occurs.
	 */
	private void displaySeniorCitizenInfo(String searchTerm) throws SQLException {
		StringBuilder resultBuilder = new StringBuilder("Senior Citizen Information:\n");
		String query = "SELECT * FROM SeniorCitizen";
		if (searchTerm.isEmpty()) {
			PreparedStatement seniorStatement = connection.prepareStatement(query);
		}
		if (!searchTerm.isEmpty()) {
			query += " WHERE customerID = ?";
		}
		try (PreparedStatement seniorStatement = connection.prepareStatement(query)) {
			if (!searchTerm.isEmpty()) {
				seniorStatement.setString(1, searchTerm);
			}
			try (ResultSet seniorResultSet = seniorStatement.executeQuery()) {
				while (seniorResultSet.next()) {
					resultBuilder.append("Senior ID: ").append(seniorResultSet.getInt("seniorID")).append("\n");
					resultBuilder.append("Senior Citizen: ").append(seniorResultSet.getInt("SeniorCitizen"))
							.append("\n\n");
					senior = seniorResultSet.getInt("SeniorCitizen");

				}
			}
		}
		resultArea.append(resultBuilder.toString());
	}

	/**
	 * Displays partner and dependents information based on the provided customer
	 * ID.
	 * 
	 * @param searchTerm The customer ID for which information is displayed.
	 * @throws SQLException If an SQL exception occurs.
	 */
	private void displayPartnerDependentsInfo(String searchTerm) throws SQLException {
		StringBuilder resultBuilder = new StringBuilder("Partner and Dependents Information:\n");
		String query = "SELECT * FROM PartnerDependents";
		if (searchTerm.isEmpty()) {
			PreparedStatement partnerDependentsStatement = connection.prepareStatement(query);
		}
		if (!searchTerm.isEmpty()) {
			query += " WHERE customerID = ?";
		}
		try (PreparedStatement partnerDependentsStatement = connection.prepareStatement(query)) {
			if (!searchTerm.isEmpty()) {
				partnerDependentsStatement.setString(1, searchTerm);
			}
			try (ResultSet partnerDependentsResultSet = partnerDependentsStatement.executeQuery()) {
				while (partnerDependentsResultSet.next()) {
					resultBuilder.append("Partner Dependent ID: ")
							.append(partnerDependentsResultSet.getInt("partnerDependentID")).append("\n");
					resultBuilder.append("Partner: ").append(partnerDependentsResultSet.getString("Partner"))
							.append("\n");
					resultBuilder.append("Dependents: ").append(partnerDependentsResultSet.getString("Dependents"))
							.append("\n\n");
				}
			}
		}
		resultArea.append(resultBuilder.toString());
	}

	/**
	 * Applies a filter to the database based on the given filter type and value.
	 * 
	 * @param filterType The type of filter to apply (e.g., "Senior Citizen",
	 *                   "Tenure", "Paperless Billing").
	 */
	private void applyFilter(String filterType) {
		String filterValue = filterValueField.getText().trim();
		String query = "";
		try {
			switch (filterType) {
			case "Senior Citizen":
				query = "SELECT * FROM SeniorCitizen WHERE SeniorCitizen = ?";
				break;
			case "Tenure":
				query = "SELECT * FROM Customer WHERE tenure >= ?";
				break;
			case "Paperless Billing":
				query = "SELECT * FROM Service WHERE PaperlessBilling = ?";
				break;
			default:
				showErrorDialog("Invalid filter type.");
				return;
			}

			PreparedStatement preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, filterValue);
			ResultSet resultSet = preparedStatement.executeQuery();
			displayFilteredResults(resultSet);
		} catch (SQLException ex) {
			showErrorDialog("Failed to apply filter.");
			ex.printStackTrace();
		}
	}

	/**
	 * Displays the filtered results in the result area of the GUI.
	 * 
	 * @param resultSet The result set containing the filtered data.
	 */
	private void displayFilteredResults(ResultSet resultSet) {
		StringBuilder resultBuilder = new StringBuilder();
		try {
			while (resultSet.next()) {
				resultBuilder.append("Customer ID: ").append(resultSet.getString("customerID")).append("\n");
				// Append more columns as needed
				resultBuilder.append("\n"); // Add a newline between rows
			}
			resultArea.setText(resultBuilder.toString());
		} catch (SQLException ex) {
			showErrorDialog("Failed to display filtered results.");
			ex.printStackTrace();
		}
	}

	/**
	 * Displays an error dialog with the given message.
	 * 
	 * @param message The error message to display.
	 */
	private void showErrorDialog(String message) {
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	// Individual statistics
	// Method to calculate customer lifetime value
	private void calculateCLV(String searchTerm) {
	    // Check if searchTerm is empty, if so, return without executing the method
	    if (searchTerm.isEmpty()) {
	        return;
	    }

	    // Fetch MonthlyCharges from 'service' table and tenure from 'customer' table
	    String query_charge = "SELECT MonthlyCharges FROM MonthlyCharges WHERE customerID = ?";
	    String query_tenure = "SELECT tenure FROM Customer WHERE customerID = ?";
	    double averageMonthlyCharges = 0;
	    int tenure = 0;

	    try (PreparedStatement serviceStatement = connection.prepareStatement(query_charge)) {
	        serviceStatement.setString(1, searchTerm);
	        try (ResultSet serviceResultSet = serviceStatement.executeQuery()) {
	            if (serviceResultSet.next()) {
	                averageMonthlyCharges = serviceResultSet.getDouble("MonthlyCharges");
	            }
	        }
	    } catch (SQLException e) {
	        showErrorDialog("Failed to fetch MonthlyCharges.");
	        e.printStackTrace();
	    }

	    try (PreparedStatement customerStatement = connection.prepareStatement(query_tenure)) {
	        customerStatement.setString(1, searchTerm);
	        try (ResultSet customerResultSet = customerStatement.executeQuery()) {
	            if (customerResultSet.next()) {
	                tenure = customerResultSet.getInt("tenure");
	            }
	        }
	    } catch (SQLException e) {
	        showErrorDialog("Failed to fetch tenure.");
	        e.printStackTrace();
	    }

	    // Insert CLV into the InsightsStatistics table if not already present
	    if (!isStatisticAlreadyPresent(searchTerm, "Customer Lifetime Value")) {
	        String insertCLVQuery = "INSERT INTO InsightsStatistics (customerID, statisticName, statisticValue) VALUES (?, ?, ?)";
	        try (PreparedStatement preparedStatement = connection.prepareStatement(insertCLVQuery)) {
	            preparedStatement.setString(1, searchTerm);
	            preparedStatement.setString(2, "Customer Lifetime Value");
	            preparedStatement.setDouble(3, averageMonthlyCharges * tenure);
	            preparedStatement.executeUpdate();
	        } catch (SQLException e) {
	            showErrorDialog("Failed to insert CLV into the InsightsStatistics table.");
	            e.printStackTrace();
	        }
	    }
	}

	// Method to determine discount eligibility
	private void isDiscountEligible(String searchTerm) {
	    // Check if searchTerm is empty, if so, return without executing the method
	    if (searchTerm.isEmpty()) {
	        return;
	    }

	    // Fetch tenure from 'customer' table
	    int tenure = 0;
	    try (PreparedStatement customerStatement = connection.prepareStatement("SELECT tenure FROM Customer WHERE customerID = ?")) {
	        customerStatement.setString(1, searchTerm);
	        try (ResultSet customerResultSet = customerStatement.executeQuery()) {
	            if (customerResultSet.next()) {
	                tenure = customerResultSet.getInt("tenure");
	            }
	        }
	    } catch (SQLException e) {
	        showErrorDialog("Failed to fetch tenure.");
	        e.printStackTrace();
	    }

	    // Determine discount eligibility based on tenure
	    boolean eligible = tenure >= 12;
	    String discountEligibility = eligible ? "Yes" : "No";

	    // Insert discount eligibility into the InsightsStatistics table if not already present
	    if (!isStatisticAlreadyPresent(searchTerm, "Discount Eligibility")) {
	        String insertDiscountQuery = "INSERT INTO InsightsStatistics (customerID, statisticName, statisticValue) VALUES (?, ?, ?)";
	        try (PreparedStatement preparedStatement = connection.prepareStatement(insertDiscountQuery)) {
	            preparedStatement.setString(1, searchTerm);
	            preparedStatement.setString(2, "Discount Eligibility");
	            preparedStatement.setString(3, discountEligibility);
	            preparedStatement.executeUpdate();
	        } catch (SQLException e) {
	            showErrorDialog("Failed to insert discount eligibility into the InsightsStatistics table.");
	            e.printStackTrace();
	        }
	    }
	}

	/**
	 * Calculates the number of services subscribed by the customer and inserts it
	 * into the InsightsStatistics table if not already present.
	 * 
	 * @param searchTerm The ID of the customer to retrieve services for.
	 */
	private void calculateNumberOfServices(String searchTerm) {
	    // Check if searchTerm is empty, if so, return without executing the method
	    if (searchTerm.isEmpty()) {
	        return;
	    }

	    // Fetch services from 'service' table
	    int numberOfServices = 0;
	    try (PreparedStatement serviceStatement = connection.prepareStatement(
	            "SELECT PhoneService, MultipleLines, InternetService, OnlineSecurity, OnlineBackup, DeviceProtection, TechSupport, StreamingTV, StreamingMovies FROM Service WHERE customerID = ?")) {
	        serviceStatement.setString(1, searchTerm);
	        try (ResultSet serviceResultSet = serviceStatement.executeQuery()) {
	            while (serviceResultSet.next()) {
	                String[] services = { serviceResultSet.getString("PhoneService"),
	                        serviceResultSet.getString("MultipleLines"), serviceResultSet.getString("InternetService"),
	                        serviceResultSet.getString("OnlineSecurity"), serviceResultSet.getString("OnlineBackup"),
	                        serviceResultSet.getString("DeviceProtection"), serviceResultSet.getString("TechSupport"),
	                        serviceResultSet.getString("StreamingTV"), serviceResultSet.getString("StreamingMovies") };
	                numberOfServices += calculateNumberOfNonNoServices(services);
	            }
	        }
	    } catch (SQLException e) {
	        showErrorDialog("Failed to fetch services.");
	        e.printStackTrace();
	    }

	    // Insert the number of services into the InsightsStatistics table if not already present
	    if (!isStatisticAlreadyPresent(searchTerm, "Number of Services")) {
	        String insertNumberOfServicesQuery = "INSERT INTO InsightsStatistics (customerID, statisticName, statisticValue) VALUES (?, ?, ?)";
	        try (PreparedStatement preparedStatement = connection.prepareStatement(insertNumberOfServicesQuery)) {
	            preparedStatement.setString(1, searchTerm);
	            preparedStatement.setString(2, "Number of Services");
	            preparedStatement.setString(3, String.valueOf(numberOfServices));
	            preparedStatement.executeUpdate();
	        } catch (SQLException e) {
	            showErrorDialog("Failed to insert the number of services into the InsightsStatistics table.");
	            e.printStackTrace();
	        }
	    }
	}

	/**
	 * Calculates the number of non-'No' services subscribed by the customer.
	 * 
	 * @param services An array containing the service subscriptions.
	 * @return The number of non-'No' services.
	 */
	private int calculateNumberOfNonNoServices(String[] services) {
		int numberOfNonNoServices = 0;
		for (String service : services) {
			if (!service.equalsIgnoreCase("No") && !service.equalsIgnoreCase("No phone service")) {
				numberOfNonNoServices++;
			}
		}
		return numberOfNonNoServices;
	}

	// Churn Analysis
	/**
	 * Calculates the percentage of customers who churned.
	 */
	private void calculateChurnPercentage() {
		double churnPercentage = 0;
		try {
			String query = "SELECT COUNT(*) AS totalCustomers, SUM(CASE WHEN Churn = 'Yes' THEN 1 ELSE 0 END) AS churnedCustomers FROM MonthlyCharges";
			try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.next()) {
						int totalCustomers = resultSet.getInt("totalCustomers");
						int churnedCustomers = resultSet.getInt("churnedCustomers");
						churnPercentage = (double) churnedCustomers / totalCustomers * 100;
						churnAnalysisResult = "Churned Customer (%): \n" + churnPercentage + "%\n";
					}
				}
			}
		} catch (SQLException e) {
			showErrorDialog("Failed to calculate churn percentage.");
			e.printStackTrace();
		}

	}

	/**
	 * Displays insights statistics for the given customer.
	 * 
	 * @param searchTerm The customer ID for which insights statistics are
	 *                   displayed.
	 */
	private void displayInsightsStatistics(String searchTerm) {
		StringBuilder resultBuilder = new StringBuilder("Insights Statistics:\n");
		// Check if searchTerm is empty, if so, return without executing the method
		if (searchTerm.isEmpty()) {
			return;
		}

		// Query to retrieve insights statistics for the given customer
		String query = "SELECT * FROM InsightsStatistics WHERE customerID = ?";

		try (PreparedStatement insightStatement = connection.prepareStatement(query)) {
			insightStatement.setString(1, searchTerm);
			try (ResultSet statsResultSet = insightStatement.executeQuery()) {
				while (statsResultSet.next()) {
					resultBuilder.append("Customer ID: ").append(statsResultSet.getString("customerID")).append("\n");
					resultBuilder.append("Statistics Name: ").append(statsResultSet.getString("statisticName"))
							.append("\n");
					resultBuilder.append("Statistics Value: ").append(statsResultSet.getString("statisticValue"))
							.append("\n\n");
				}
			}
			resultBuilder.append(churnAnalysisResult);
		} catch (SQLException e) {
			showErrorDialog("Failed to fetch insights statistics.");
			e.printStackTrace();
		}
		resultArea.append(resultBuilder.toString());
	}
	
	/**
	 * Checks if a statistic with the given name already exists for the specified customer.
	 * 
	 * @param customerID     The ID of the customer.
	 * @param statisticName  The name of the statistic to check.
	 * @return               True if the statistic already exists, otherwise false.
	 */
	private boolean isStatisticAlreadyPresent(String customerID, String statisticName) {
	    String query = "SELECT COUNT(*) AS count FROM InsightsStatistics WHERE customerID = ? AND statisticName = ?";
	    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
	        preparedStatement.setString(1, customerID);
	        preparedStatement.setString(2, statisticName);
	        try (ResultSet resultSet = preparedStatement.executeQuery()) {
	            if (resultSet.next()) {
	                int count = resultSet.getInt("count");
	                return count > 0;
	            }
	        }
	    } catch (SQLException e) {
	        showErrorDialog("Failed to check if statistic already exists.");
	        e.printStackTrace();
	    }
	    return false;
	}

	/**
	 * Main method
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			ChurnCustomerApplication gui = new ChurnCustomerApplication();
			gui.setVisible(true);
		});
	}
}
