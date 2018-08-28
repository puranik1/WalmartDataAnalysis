	--==================================================================================
	--Create Database TestDb
	--==================================================================================
	IF NOT EXISTS (SELECT name FROM master.sys.databases WHERE name = N'TestDb')
	BEGIN
		CREATE DATABASE TestDb
	END
	GO

	--==================================================================================
	--Create required tables
	--==================================================================================
	USE TestDb
	DROP TABLE IF EXISTS Transactions;
	DROP TABLE IF EXISTS PollerLog;
	DROP TABLE IF EXISTS CurrentRegisterStatus;
	DROP TABLE IF EXISTS DepartmentSales;

	CREATE TABLE dbo.Transactions
	(
		transactionId	BIGINT IDENTITY(1,1),
		id VARCHAR(1000),	
		orderId VARCHAR(1000),
		orderTime BIGINT,
		storeNumber INT,
		department VARCHAR(1000),
		register VARCHAR(1000),
		amount REAL,
		upc VARCHAR(1000),
		name VARCHAR(1000),
		description VARCHAR(1000),
		insertedDate DATETIME
	)

	CREATE TABLE PollerLog(
		id BIGINT IDENTITY(1,1),
		lastTransactionIdProcessed BIGINT

	);

	CREATE TABLE CurrentRegisterStatus(
		id BIGINT IDENTITY(1,1),
		store INT,
		register VARCHAR(1000),
		lastStatus VARCHAR(1000),
		currentStatus VARCHAR(1000),
		lastTransactionDate BIGINT,
		processId uniqueidentifier,
		isStatusChangeReported INT

	);

	CREATE TABLE DepartmentSales(
		id  BIGINT IDENTITY(1,1),
		storeNumber INT,
		department VARCHAR(1000),
		totalSales REAL,
		hr INT
	);
	GO

	--==================================================================================
	--Create required Stored Procedures
	--==================================================================================
		USE TestDb
	
	EXEC sp_configure 'show advanced options', 1
	GO	
	RECONFIGURE
	GO	-- Enabling xp_cmdshell
	EXEC sp_configure 'xp_cmdshell', 1
	GO	
	RECONFIGURE
	GO
		
	DROP PROCEDURE IF EXISTS usp_GetItemWiseSaleDetails
	GO
	CREATE PROCEDURE usp_GetItemWiseSaleDetails
	AS

	--**************************************************************************************************
	--**************************************************************************************************
	--Code Logic: 
	--Determine what transactions happened in the last seven hours (Get a sum of total amount by hour and store in temp table)
	--Determine average of transactions (amount) in last second to seventh hour
	--Compare againt total transactions(amount) in last one hour
	--Percentage increase  = 100*(New -old)/old

	--**************************************************************************************************
	--**************************************************************************************************
	BEGIN TRY

		PRINT('Started executing usp_GetItemWiseSaleDetails : ' + CAST(GETDATE() AS VARCHAR))

	   --Temp table for getting dept/item wise sales
	   IF OBJECT_ID('tempdb..#DeptItemSalesInLastSevenHours') IS NOT NULL
				DROP TABLE #DeptItemSalesInLastSevenHours 
	   CREATE TABLE #DeptItemSalesInLastSevenHours
	   (
		   id INT IDENTITY(1,1),
		   storeNumber INT,
		   department VARCHAR(1000),
		   upc VARCHAR(1000),
		   name VARCHAR(1000),
		   hr INT,		  
		   totalSales REAL
	   )	   

	   INSERT INTO #DeptItemSalesInLastSevenHours
	   (
			storeNumber,
			department,
			upc,
			name,	
			hr,		
			totalSales
		)
		SELECT
			storeNumber,
			department,
			upc,
			name,
			CONVERT (BIGINT, DATEDIFF(HH, '01-01-1970 00:00:00' , insertedDate)),			
			SUM(amount)
		FROM TestDB.dbo.Transactions
		WHERE DATEDIFF(MINUTE, insertedDate,GETDATE()) < 420 --Transactions in last seven hours
		GROUP BY storeNumber,
			department,
			upc,
			name,
			CONVERT (BIGINT, DATEDIFF(HH, '01-01-1970 00:00:00' , insertedDate))			

		PRINT(CAST(@@RowCount AS VARCHAR) + ' transactions inserted into #DeptItemSalesInLastSevenHours')

		DECLARE @mxHr INT = (SELECT ISNULL(MAX(hr),0) FROM #DeptItemSalesInLastSevenHours)

		--DECLARE @CurrentHr INT = 7
		DECLARE @CurrentHr INT = @mxHr
		
	
		--Temp table for getting avg sales per item in last 6 hours
		IF OBJECT_ID('tempdb..#LstSixHrAvg') IS NOT NULL
				DROP TABLE #LstSixHrAvg 
			CREATE TABLE #LstSixHrAvg 
			(
				storeNumber INT,
				department VARCHAR (1000),
				upc VARCHAR (1000),
				name VARCHAR(1000),
				AvgSalesForLastSixHours REAL
			)
			INSERT INTO #LstSixHrAvg
			(
				storeNumber,
				department,
				upc,
				name,
				AvgSalesForLastSixHours
			)
			Select
				storenumber,
				department,
				upc,
				name,
				AVG(totalSales) AvgSalesForlastSixHours
			FROM #DeptItemSalesInLastSevenHours
			WHERE hr >= @CurrentHr - 6
				AND hr < @CurrentHr
			GROUP BY storeNumber,
				department,
				upc,
				name

		PRINT(CAST(@@RowCount AS VARCHAR) + ' records inserted into #LstSixHrAvg')

		--Temp table for getting sales in current hour
		IF OBJECT_ID('tempdb..#CurrentHrSales') IS NOT NULL
			DROP TABLE #CurrentHrSales
		CREATE TABLE #CurrentHrSales
		(
			storeNumber INT,
			department VARCHAR(1000),
			upc VARCHAR (1000),
			name VARCHAR(1000),
			CurrentHourSales REAL
		)
		INSERT INTO #CurrentHrSales
		(
			storeNumber,
			department,
			upc,
			name,
			CurrentHourSales
		)
		SELECT storeNumber,
			department,
			upc,
			name,
			totalSales AS CurrentHourSales
		FROM #DeptItemSalesInLastSevenHours
		WHERE hr = @CurrentHR

		PRINT(CAST(@@RowCount AS VARCHAR) + ' records inserted into #CurrentHrSales')

		--Temp table for getting percentage increase in sales
		IF OBJECT_ID('tempdb..#PercentageIncreaseSales') IS NOT NULL
			DROP TABLE #PercentageIncreaseSales
		CREATE TABLE #PercentageIncreaseSales
		(
			storeNumber INT,
			department VARCHAR(1000),
			upc VARCHAR (1000),
			name VARCHAR(1000),
			PercentIncreaseInSales REAL
		)

		INSERT INTO #PercentageIncreaseSales
		(
			storeNumber,
			department,
			upc,
			name,
			PercentIncreaseInSales
		)
		SELECT a.storeNumber,
			a.department,
			a.upc,
			a.name,
			100*((a.CurrentHourSales-ISNULL(b.AvgSalesForLastSixHours,0))/(CASE WHEN ISNULL(b.AvgSalesForLastSixHours,0)=0 THEN CASE WHEN ISNULL(a.CurrentHourSales,0)=0 THEN 1 ELSE a.CurrentHourSales END ELSE b.AvgSalesForLastSixHours END))  AS PercentIncreaseInSales
		FROM #CurrentHrSales a
		LEFT JOIN #LstSixHrAvg b
			ON a.storeNumber   =  b.storeNumber
			AND a.department =  b.department
			AND a.upc   =  b.upc
			AND a.name  =  b.name
		
		PRINT(CAST(@@RowCount AS VARCHAR) + ' records inserted into #PercentageIncreaseSales')			
		

		--Temp table fpr getting highest percentage increase in sales
		IF OBJECT_ID('tempdb..##RankedPercentageIncrease') IS NOT NULL
		   DROP TABLE ##RankedPercentageIncrease
		CREATE TABLE ##RankedPercentageIncrease
		(
			 storeNumber INT,
			 department VARCHAR(1000),
			 upc VARCHAR(1000),
			 name VARCHAR(1000),
			 PercentIncreaseInSales REAL,
			 Rnk INT
		)
		INSERT INTO ##RankedPercentageIncrease
		(
				storenumber,
				department,
				upc,
				name,
				PercentIncreaseInSales,
				Rnk
		)
		SELECT 
			  storeNumber,
			  department,
			  upc,
			  name,
			  PercentIncreaseInSales,
			  DENSE_RANK() OVER ( PARTITION BY storeNumber, department
								   ORDER BY PercentIncreaseInSales DESC) AS Rnk
		FROM #PercentageIncreaseSales

		--Final Output
		SELECT * FROM ##RankedPercentageIncrease
		WHERE Rnk = 1 AND PercentIncreaseInSales > 0

		--We only write to stream if there is a positive increase in percentage sales
		IF((SELECT COUNT(*) FROM ##RankedPercentageIncrease WHERE Rnk = 1 AND PercentIncreaseInSales > 0) > 0)
		BEGIN
			DECLARE @TempFilePath VARCHAR(1000) = 'C:\Walmart\itemdetails.txt'  
			DECLARE @FinalFilePath VARCHAR(1000) = 'C:\Walmart\OutputStreamItem.txt'  --file path for output
			DECLARE @Cmd VARCHAR (1000)
			SET @Cmd = 'bcp "SELECT * FROM ##RankedPercentageIncrease WHERE Rnk = 1 AND PercentIncreaseInSales > 0" QUERYOUT ' + '"' + @TempFilePath + '" -c -T -S' + @@SERVERNAME + ' && type ' + '"' + @TempFilePath +'" >> '+@FinalFilePath
			PRINT(@Cmd)
			EXEC master..xp_cmdshell @Cmd

			--Delete temp files
			DECLARE @FileExists INT
			SET @Cmd = 'del ' + @TempFilePath
			EXEC master..xp_FileExist @TempFilePath, @FileExists OUT
			IF @FileExists = 1
			EXEC master..xp_cmdShell @Cmd

		END
		
		PRINT('Completed executing usp_GetItemWiseSaleDetails : ' + CAST(GETDATE() AS VARCHAR))

	 END TRY
	 BEGIN CATCH
		   SELECT ERROR_MESSAGE()
	END CATCH
	GO

	USE TestDb
	GO
	EXEC sp_configure 'show advanced options', 1
	GO	
	RECONFIGURE
	GO	-- Enabling xp_cmdshell
	EXEC sp_configure 'xp_cmdshell', 1
	GO	
	RECONFIGURE
	GO

	DROP PROCEDURE IF EXISTS usp_GetDeptSalesDetails
	GO
	CREATE PROCEDURE usp_GetDeptSalesDetails
	AS
	--**************************************************************************************************
	--**************************************************************************************************
	--Code Logic: 
	--Determine what transactions happened in the last three hours (total sales by department and hour)
	--Determine ranking for each hour per store for all departments
	--Pivot the table to have a better view and easy underestanding of the data
	--determine all departemtns whose ranks have dereased consistantly in last 3 hours

	--**************************************************************************************************
	--**************************************************************************************************
	BEGIN TRY
		
		PRINT('Started executing usp_GetDeptSalesDetails : ' + CAST(GETDATE() AS VARCHAR))

		--temp table for getting dept wise sales
		IF OBJECT_ID('tempdb..#DeptSales') IS NOT NULL
				DROP TABLE #DeptSales 
		CREATE TABLE #DeptSales
		(
			id INT IDENTITY (1,1),
			storeNumber INT,
			department varchar(1000),
			hr INT,
			totalSales REAL,
			rnk INT
		)		

		INSERT INTO #DeptSales
		(
			storeNumber ,
			department,
			hr,
			totalSales
		)
		SELECT
			storeNumber,
			department,
			CONVERT (BIGINT, DATEDIFF(HH, '01-01-1970 00:00:00' , insertedDate)),
			SUM (amount)
		 FROM TestDb.dbo.Transactions
		 WHERE DATEDIFF(MINUTE, insertedDate,GETDATE()) < 180 --Transactions in last three hours
		 GROUP BY storeNumber,
				department,
				CONVERT (BIGINT, DATEDIFF(HH, '01-01-1970 00:00:00' , insertedDate))

		PRINT(CAST(@@RowCount AS VARCHAR) + ' transactions inserted into #DeptSales')

		DECLARE @mxhr INT = (SELECT ISNULL(MAX(hr),0) FROM #DeptSales)

		--Get the rank per store for all departments based on sales figures
		UPDATE a
		SET a.rnk = b.rnk
		FROM #DeptSales a
		INNER JOIN
		(
		   SELECT 
				storeNumber,
				department,
				hr,
				DENSE_RANK () OVER (PARTITION BY storeNumber, hr ORDER BY totalSales DESC) AS rnk
				FROM #DeptSales
		)b
		ON a.storeNumber = b.storeNumber
		AND a.department = b.department
		AND a.hr = b.hr


		--Pivot table to get all ranks in a row
		DECLARE @SQL VARCHAR(MAX)
		SET @SQL =
		'IF OBJECT_ID(''tempdb..##PivotRanks'') IS NOT NULL
			DROP TABLE ##PivotRanks 
		IF OBJECT_ID(''tempdb..##OutputStream'') IS NOT NULL
			DROP TABLE ##OutputStream 
		SELECT storeNumber, department, ISNULL(['+CAST(@mxHr-2 AS VARCHAR)+'],0) AS ['+CAST(@mxHr-2 AS VARCHAR)+']
		,ISNULL(['+CAST(@mxHr-1 AS VARCHAR)+'],0) AS ['+CAST(@mxHr-1 AS VARCHAR)+']
		,ISNULL(['+CAST(@mxHr AS VARCHAR)+'],0) AS ['+CAST(@mxHr AS VARCHAR)+']
		INTO ##PivotRanks
		FROM
		(

			SELECT
					storeNumber,
					department,
					hr,
					ISNULL(rnk,0) AS rnk
			FROM #DeptSales
		) AS SourceTable
		PIVOT
		(
			MIN(rnk)
			FOR hr IN (['+CAST(@mxHr-2 AS VARCHAR)+'],['+CAST(@mxHr-1 AS VARCHAR)+'],['+CAST(@mxHr AS VARCHAR)+'])
		)AS Pivottable


		SELECT *
		INTO ##OutputStream 
		FROM ##PivotRanks		
		WHERE ['+CAST(@mxHr-2 AS VARCHAR)+'] > ['+CAST(@mxHr-1 AS VARCHAR)+']
		AND ['+CAST(@mxHr-1 AS VARCHAR)+'] > ['+CAST(@mxHr AS VARCHAR)+']

		'
		PRINT (@SQL)
		EXEC (@SQL)

		DECLARE @TempFilePath VARCHAR(1000) = 'C:\Walmart\deptdetails.txt'  
		DECLARE @FinalFilePath VARCHAR(1000) = 'C:\Walmart\OutputStreamDept.txt'  --file path for output


		DECLARE @Cmd VARCHAR(1000)
		SET @Cmd = 'bcp "SELECT * FROM ##OutputStream" QUERYOUT ' + '"' + @TempFilePath + '" -c -T -S ' + @@SERVERNAME + ' && type ' + '"' + @TempFilePath +'" >> '+@FinalFilePath
		EXEC master..xp_cmdshell @Cmd
		
		--Delete temp files
		DECLARE @FileExists INT
		SET @Cmd = 'del ' + @TempFilePath
		EXEC master..xp_FileExist @TempFilePath, @FileExists OUT
		IF @FileExists = 1
		EXEC master..xp_cmdShell @Cmd

		PRINT('Completed executing usp_GetDeptSalesDetails : ' + CAST(GETDATE() AS VARCHAR))

		
	END TRY
	BEGIN CATCH
		SELECT ERROR_MESSAGE()
	END CATCH 
	GO

	--==================================================================================
	--Create SQL Jobs
	--==================================================================================
	USE [msdb]
	GO

	/****** Object:  Job [DepartmentItemWiseHourlyAnalysis]    Script Date: 8/27/2018 10:21:29 PM ******/
	BEGIN TRANSACTION
	DECLARE @ReturnCode INT
	SELECT @ReturnCode = 0
	/****** Object:  JobCategory [[Uncategorized (Local)]]    Script Date: 8/27/2018 10:21:29 PM ******/
	IF NOT EXISTS (SELECT name FROM msdb.dbo.syscategories WHERE name=N'[Uncategorized (Local)]' AND category_class=1)
	BEGIN
	EXEC @ReturnCode = msdb.dbo.sp_add_category @class=N'JOB', @type=N'LOCAL', @name=N'[Uncategorized (Local)]'
	IF (@@ERROR <> 0 OR @ReturnCode <> 0) GOTO QuitWithRollback

	END

	DECLARE @jobId BINARY(16)
	EXEC @ReturnCode =  msdb.dbo.sp_add_job @job_name=N'DepartmentItemWiseHourlyAnalysis', 
			@enabled=1, 
			@notify_level_eventlog=0, 
			@notify_level_email=0, 
			@notify_level_netsend=0, 
			@notify_level_page=0, 
			@delete_level=0, 
			@description=N'No description available.', 
			@category_name=N'[Uncategorized (Local)]', 
			@owner_login_name=N'admin', @job_id = @jobId OUTPUT
	IF (@@ERROR <> 0 OR @ReturnCode <> 0) GOTO QuitWithRollback
	/****** Object:  Step [Run Procedure for analysis]    Script Date: 8/27/2018 10:21:29 PM ******/
	EXEC @ReturnCode = msdb.dbo.sp_add_jobstep @job_id=@jobId, @step_name=N'Run Procedure for analysis', 
			@step_id=1, 
			@cmdexec_success_code=0, 
			@on_success_action=1, 
			@on_success_step_id=0, 
			@on_fail_action=2, 
			@on_fail_step_id=0, 
			@retry_attempts=0, 
			@retry_interval=0, 
			@os_run_priority=0, @subsystem=N'TSQL', 
			@command=N'EXEC TestDb.dbo.usp_GetItemWiseSaleDetails', 
			@database_name=N'master', 
			@flags=0
	IF (@@ERROR <> 0 OR @ReturnCode <> 0) GOTO QuitWithRollback
	EXEC @ReturnCode = msdb.dbo.sp_update_job @job_id = @jobId, @start_step_id = 1
	IF (@@ERROR <> 0 OR @ReturnCode <> 0) GOTO QuitWithRollback
	EXEC @ReturnCode = msdb.dbo.sp_add_jobschedule @job_id=@jobId, @name=N'HourlyItemAnalysisSchedule', 
			@enabled=1, 
			@freq_type=4, 
			@freq_interval=1, 
			@freq_subday_type=8, 
			@freq_subday_interval=1, 
			@freq_relative_interval=0, 
			@freq_recurrence_factor=0, 
			@active_start_date=20180827, 
			@active_end_date=99991231, 
			@active_start_time=0, 
			@active_end_time=235959, 
			@schedule_uid=N'216fd3cd-f039-4825-bda9-22d9016652df'
	IF (@@ERROR <> 0 OR @ReturnCode <> 0) GOTO QuitWithRollback
	EXEC @ReturnCode = msdb.dbo.sp_add_jobserver @job_id = @jobId, @server_name = N'(local)'
	IF (@@ERROR <> 0 OR @ReturnCode <> 0) GOTO QuitWithRollback
	COMMIT TRANSACTION
	GOTO EndSave
	QuitWithRollback:
		IF (@@TRANCOUNT > 0) ROLLBACK TRANSACTION
	EndSave:
	GO

	USE [msdb]
	GO

	/****** Object:  Job [DepartmentWiseHourlyAnalysis]    Script Date: 8/27/2018 10:21:22 PM ******/
	BEGIN TRANSACTION
	DECLARE @ReturnCode INT
	SELECT @ReturnCode = 0
	/****** Object:  JobCategory [[Uncategorized (Local)]]    Script Date: 8/27/2018 10:21:22 PM ******/
	IF NOT EXISTS (SELECT name FROM msdb.dbo.syscategories WHERE name=N'[Uncategorized (Local)]' AND category_class=1)
	BEGIN
	EXEC @ReturnCode = msdb.dbo.sp_add_category @class=N'JOB', @type=N'LOCAL', @name=N'[Uncategorized (Local)]'
	IF (@@ERROR <> 0 OR @ReturnCode <> 0) GOTO QuitWithRollback

	END

	DECLARE @jobId BINARY(16)
	EXEC @ReturnCode =  msdb.dbo.sp_add_job @job_name=N'DepartmentWiseHourlyAnalysis', 
			@enabled=1, 
			@notify_level_eventlog=0, 
			@notify_level_email=0, 
			@notify_level_netsend=0, 
			@notify_level_page=0, 
			@delete_level=0, 
			@description=N'No description available.', 
			@category_name=N'[Uncategorized (Local)]', 
			@owner_login_name=N'admin', @job_id = @jobId OUTPUT
	IF (@@ERROR <> 0 OR @ReturnCode <> 0) GOTO QuitWithRollback
	/****** Object:  Step [Run Procedure for DeptWise Analysis]    Script Date: 8/27/2018 10:21:22 PM ******/
	EXEC @ReturnCode = msdb.dbo.sp_add_jobstep @job_id=@jobId, @step_name=N'Run Procedure for DeptWise Analysis', 
			@step_id=1, 
			@cmdexec_success_code=0, 
			@on_success_action=1, 
			@on_success_step_id=0, 
			@on_fail_action=2, 
			@on_fail_step_id=0, 
			@retry_attempts=0, 
			@retry_interval=0, 
			@os_run_priority=0, @subsystem=N'TSQL', 
			@command=N'EXEC TestDb.dbo.usp_GetDeptSalesDetails', 
			@database_name=N'master', 
			@flags=0
	IF (@@ERROR <> 0 OR @ReturnCode <> 0) GOTO QuitWithRollback
	EXEC @ReturnCode = msdb.dbo.sp_update_job @job_id = @jobId, @start_step_id = 1
	IF (@@ERROR <> 0 OR @ReturnCode <> 0) GOTO QuitWithRollback
	EXEC @ReturnCode = msdb.dbo.sp_add_jobschedule @job_id=@jobId, @name=N'DepartmentWiseHourlyAnalysis', 
			@enabled=1, 
			@freq_type=4, 
			@freq_interval=1, 
			@freq_subday_type=8, 
			@freq_subday_interval=1, 
			@freq_relative_interval=0, 
			@freq_recurrence_factor=0, 
			@active_start_date=20180827, 
			@active_end_date=99991231, 
			@active_start_time=0, 
			@active_end_time=235959, 
			@schedule_uid=N'831bb9d9-d60d-470f-97a7-fc7e739e57f5'
	IF (@@ERROR <> 0 OR @ReturnCode <> 0) GOTO QuitWithRollback
	EXEC @ReturnCode = msdb.dbo.sp_add_jobserver @job_id = @jobId, @server_name = N'(local)'
	IF (@@ERROR <> 0 OR @ReturnCode <> 0) GOTO QuitWithRollback
	COMMIT TRANSACTION
	GOTO EndSave
	QuitWithRollback:
		IF (@@TRANCOUNT > 0) ROLLBACK TRANSACTION
	EndSave:
	GO