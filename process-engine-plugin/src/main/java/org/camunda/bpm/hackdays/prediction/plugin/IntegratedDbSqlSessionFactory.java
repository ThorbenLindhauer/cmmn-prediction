package org.camunda.bpm.hackdays.prediction.plugin;

import org.camunda.bpm.engine.impl.db.sql.DbSqlSession;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.interceptor.Session;

public class IntegratedDbSqlSessionFactory extends DbSqlSessionFactory {

  protected DbSqlSessionFactory wrappedSqlSessionFactory;
  
  public IntegratedDbSqlSessionFactory(DbSqlSessionFactory wrappedSqlSessionFactory) {
    this.wrappedSqlSessionFactory = wrappedSqlSessionFactory;
  }

  @Override
  public Session openSession() {
    return new DbSqlSession(wrappedSqlSessionFactory) {

      // We are participating in the engine's transaction, but do so via a second Mybatis SqlSession.
      // In that case, the engine's own SqlSession may not notice a "dirty" connection when we have only performed
      // updates in our own sql session. DbSqlSession#rollback will therefore not actually roll back,
      // so we force it to.
      
      @Override
      public void rollback() {
        sqlSession.rollback(true);
      }
      
      @Override
      public void commit() {
        sqlSession.commit(true);
      }
    };
  }
  

}
