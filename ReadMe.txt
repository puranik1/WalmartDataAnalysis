ReadMe:

Technologies used:
-Java SpringBoot (microservice)
-MS SQL SERVER (DB)

The structure of solution is as follows:

- Java Springboot application used to do 2 things:
	1. Injest messages (currently from the sample file provided) and store them into DB
	2. Figure out when the status of register has changed and write the output stream to:
		C:\Walmart\OutputStreamRegisterStatus.txt

- Several DB tables, 2 stored Procedures and 2 SQL jobs used to do following:
	1. Create the backend structre required
	2. Procedures are used to read the stored transactions y the service and create the output streams for department and item sales and write the output stream to :
		C:\Walmart\OutputStreamItem.txt
		C:\Walmart\OutputStreamDept.txt
	3. SQL Jobs are used to run the procedures hourly
- Assumptions made: 
	-SQL server running on localhost
	-Server has a user with following credentials:
		username=admin
		password=admin
	-Server accepts connections from JAVA
	
Build Instructions:

For the DB part, we need to run the script provided so that all objects are created

If running in an editor like IntelliJ, we can direclty use the build option present in IDE
We can also use commands to do so:
-Application is a maven application and can be build using following commands:
	(Run commands through command prompt inside the project folder)
	mvn clean -> cleans old files
	mvn install -> creates a new jar
- Once we have a jar, navigate to the jar folder and run following command:
		java -jar target\WalmartDataAnalysis-1.0-SNAPSHOT.jar

(I have already also provided the jar directly so it can be run, no need to rebuild)		

-After runing the above command for jar, the springboot appplication will start on port 8080 on localhost.
-the RegisterStatus scheduled job will start ru ing but wont return anything since we havent injested any messages yet
-Run the following GET request through browser of any tool like Postman:
	http://localhost:8080/injestmessages
	This wil start reading the input sale.json file and writing it to DB





