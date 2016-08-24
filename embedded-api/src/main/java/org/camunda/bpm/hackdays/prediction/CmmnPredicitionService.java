package org.camunda.bpm.hackdays.prediction;

import java.sql.Connection;

public class CmmnPredicitionService {

	public void createDbTables(Connection dbConnection) {
		new CreateTablesCmd(dbConnection).execute();
	}
}
