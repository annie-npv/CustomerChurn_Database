CREATE DATABASE churndata;

CREATE TABLE churndata.Customer (
    customerID VARCHAR(20) PRIMARY KEY,
    gender VARCHAR(10),
    SeniorCitizen INT,
    Partner VARCHAR(3),
    Dependents VARCHAR(3),
    tenure INT
);

CREATE TABLE churndata.Service (
    serviceID INT AUTO_INCREMENT PRIMARY KEY,
    customerID VARCHAR(20),
    PhoneService VARCHAR(3),
    MultipleLines VARCHAR(20),
    InternetService VARCHAR(20),
    OnlineSecurity VARCHAR(20),
    OnlineBackup VARCHAR(20),
    DeviceProtection VARCHAR(20),
    TechSupport VARCHAR(20),
    StreamingTV VARCHAR(20),
    StreamingMovies VARCHAR(20),
    PaperlessBilling VARCHAR(3),
    PaymentMethod VARCHAR(50),
    FOREIGN KEY (customerID) REFERENCES Customer(customerID)
);

CREATE TABLE churndata.MonthlyCharges (
    chargeID INT AUTO_INCREMENT PRIMARY KEY,
    customerID VARCHAR(20),
    MonthlyCharges DECIMAL(10,2),
    TotalCharges DECIMAL(10,2),
    Churn VARCHAR(3),
    FOREIGN KEY (customerID) REFERENCES Customer(customerID)
);

CREATE TABLE churndata.SeniorCitizen (
    seniorID INT AUTO_INCREMENT PRIMARY KEY,
    customerID VARCHAR(20),
    SeniorCitizen INT,
    FOREIGN KEY (customerID) REFERENCES Customer(customerID)
);

CREATE TABLE churndata.PartnerDependents (
    partnerDependentID INT AUTO_INCREMENT PRIMARY KEY,
    customerID VARCHAR(20),
    Partner VARCHAR(3),
    Dependents VARCHAR(3),
    FOREIGN KEY (customerID) REFERENCES Customer(customerID)
);

CREATE TABLE churndata.InsightsStatistics (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customerID VARCHAR(255),
    statisticName VARCHAR(255),
    statisticValue VARCHAR(255),
	FOREIGN KEY (customerID) REFERENCES Customer(customerID)
);

SELECT COUNT(*) AS totalCustomers, 
SUM(CASE WHEN Churn = 'Yes' THEN 1 ELSE 0 END) 
AS churnedCustomers 
FROM churndata.monthlycharges;

