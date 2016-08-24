package org.camunda.bpm.hackdays.prediction;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;

import org.camunda.bpm.engine.impl.util.IoUtil;

public class CreateTablesCmd implements Command {

	public static final String DDL_CLASSPATH_LOCATION = "sql/create.h2.sql";
	
	protected Connection dbConnection;

	public CreateTablesCmd(Connection dbConnection) {
		this.dbConnection = dbConnection;
	}

	public void execute() {
		
		InputStream sqlStream = CreateTablesCmd.class.getClassLoader().getResourceAsStream(DDL_CLASSPATH_LOCATION);
		byte[] sqlBytes = IoUtil.readInputStream(sqlStream, "classpath://" + DDL_CLASSPATH_LOCATION);
		
		String sqlString = new String(sqlBytes, StandardCharsets.UTF_8);
		
		try {
			dbConnection.createStatement().execute(sqlString);
		} catch (SQLException e) {
			throw new CmmnPredictionException("Could not create tables", e);
		}
	}
}
