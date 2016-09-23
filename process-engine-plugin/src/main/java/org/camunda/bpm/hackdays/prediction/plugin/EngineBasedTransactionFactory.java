package org.camunda.bpm.hackdays.prediction.plugin;

import java.sql.Connection;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransaction;
import org.apache.ibatis.transaction.managed.ManagedTransaction;
import org.camunda.bpm.engine.impl.context.Context;

public class EngineBasedTransactionFactory implements TransactionFactory {

  public void setProperties(Properties props) {
  }

  public Transaction newTransaction(Connection conn) {
    if (isInContextOfEngine()) {
      return new ManagedTransaction(conn, false);
    } else {
      return new JdbcTransaction(conn);
    }
  }

  public Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
    if (isInContextOfEngine()) {
      // this is a hack since it disregards the data source and just participates in the engine's db connection
      Connection engineConnection = Context.getCommandContext().getDbSqlSession().getSqlSession().getConnection();
      return new ManagedTransaction(engineConnection, false);
    } else {
      return new JdbcTransaction(dataSource, level, autoCommit);
    }
  }
  
  protected boolean isInContextOfEngine()
  {
    return Context.getCommandContext() != null;
  }
}
