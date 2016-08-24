package org.camunda.bpm.hackdays.prediction;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel;

public class CmmnPredictionService {
  
  protected SqlSessionFactory sqlSessionFactory;

  // TODO: perhaps this should internally get a connection from datasource
  //   to be consistent with other APIs
	public void createDbTables(Connection dbConnection) {
		new CreateTablesCmd(dbConnection).execute(this);
	}
	
	public PredictionModel getModel(String name) {
	  return new GetModelCmd(name).execute(this);
	}
	
	public void insertModel(PredictionModel model) {
	  new CreateModelCmd(model).execute(this);
	}
	
	public ParsedPredictionModel parseModel(PredictionModel model) {
	  return new ParseModelCmd(model).execute(this);
	}
	
	public static CmmnPredictionService build(DataSource dataSource) {
	  InputStream config = CmmnPredictionService.class.getClassLoader().getResourceAsStream("mybatis/mybatis-config.xml");
	  SqlSessionFactory sqlSessionFactory = createMyBatisSqlSessionFactory(config, dataSource);
	  
	  CmmnPredictionService service = new CmmnPredictionService();
	  service.sqlSessionFactory = sqlSessionFactory;
	  return service;
	}
	
	protected static SqlSessionFactory createMyBatisSqlSessionFactory(InputStream config, DataSource dataSource) {
    // use this transaction factory if you work in a non transactional
    // environment
    // TransactionFactory transactionFactory = new JdbcTransactionFactory();

    // use ManagedTransactionFactory if you work in a transactional
    // environment (e.g. called within the engine or using JTA)
	  
    TransactionFactory transactionFactory = new ManagedTransactionFactory();

    Environment environment = new Environment("cmmn-prediction", transactionFactory, dataSource);

    XMLConfigBuilder parser = new XMLConfigBuilder(new InputStreamReader(config));
    
    Configuration configuration = parser.getConfiguration();
    configuration.setEnvironment(environment);
    configuration = parser.parse();

    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    return sqlSessionFactory;
	}
}
