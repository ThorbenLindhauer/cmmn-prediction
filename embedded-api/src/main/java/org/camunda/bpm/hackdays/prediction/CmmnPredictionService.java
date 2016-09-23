package org.camunda.bpm.hackdays.prediction;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel;

public class CmmnPredictionService {
  
  protected DataSource dataSource;
  protected SqlSessionFactory sqlSessionFactory;
  protected TransactionFactory txFactory;
  
  protected CmmnPredictionService() {
  }
  
	public void createDbTables() {
	  inTransactionDo(new ConnectionFunction() {

      public void call(Connection connection) {
        new CreateTablesCmd(connection).execute(CmmnPredictionService.this);
      }
    });
	}
	
	protected void inTransactionDo(ConnectionFunction callable) {
	  Transaction transaction = null;
    
    try {
      transaction = txFactory.newTransaction(dataSource, null, false);
      callable.call(transaction.getConnection());
      
      transaction.commit();
    } catch (Exception e) {
      if (transaction != null) {
        try {
          transaction.rollback();
        } catch (SQLException e1) {
          throw new RuntimeException("Could not rollback tx", e);
        }
      }
    }
    finally {
      if (transaction != null) {
        try {
          transaction.close();
        } catch (SQLException e) {
          throw new RuntimeException("Could not close tx", e);
        }
      }
    }
	}
	
	protected static interface ConnectionFunction {
	  void call(Connection connection);
	}
	
	public void dropDbTables() {
	  inTransactionDo(new ConnectionFunction() {

      public void call(Connection connection) {
        new DropTablesCmd(connection).execute(CmmnPredictionService.this);
      }
    });
	}
	
	public PredictionModel getModel(String name) {
	  return new GetModelCmd(name).execute(this);
	}
	
	public void insertModel(PredictionModel model) {
	  new CreateModelCmd(model).execute(this);
	}
	
	public void updateModel(PredictionModel model) {
	  new UpdateModelCmd(model).execute(this);
	}
	
	public ParsedPredictionModel parseModel(PredictionModel model) {
	  return new ParseModelCmd(model).execute(this);
	}
	
	/**
	 * For the given variable, estimate the marginal probability.
	 * 
	 * @return
	 */
	public Map<String, Double> estimate(PredictionModel model, String variableName, Map<String, Integer> variableAssignments, Map<String, Object> expressionContext) {
	  return new EstimateDistributionCmd(model, variableName, variableAssignments, expressionContext).execute(this);
	}
	
	public static CmmnPredictionService buildStandalone(DataSource dataSource) {
	  return buildWithTxFactory(dataSource, new JdbcTransactionFactory());
	}
	
	public static CmmnPredictionService buildWithTxFactory(DataSource dataSource, TransactionFactory txFactory) {
	  InputStream config = CmmnPredictionService.class.getClassLoader().getResourceAsStream("mybatis/mybatis-config.xml");
    SqlSessionFactory sqlSessionFactory = createMyBatisSqlSessionFactory(config, dataSource, txFactory);
    
    CmmnPredictionService service = new CmmnPredictionService();
    service.sqlSessionFactory = sqlSessionFactory;
    service.dataSource = dataSource;
    service.txFactory = txFactory;
    return service;
	}
	
	protected static SqlSessionFactory createMyBatisSqlSessionFactory(InputStream config, DataSource dataSource, TransactionFactory txFactory) {

    Environment environment = new Environment("cmmn-prediction", txFactory, dataSource);

    XMLConfigBuilder parser = new XMLConfigBuilder(new InputStreamReader(config));
    
    Configuration configuration = parser.getConfiguration();
    configuration.setEnvironment(environment);
    configuration = parser.parse();

    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    return sqlSessionFactory;
	}
}
