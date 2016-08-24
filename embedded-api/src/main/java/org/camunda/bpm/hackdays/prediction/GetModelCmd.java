package org.camunda.bpm.hackdays.prediction;

import org.apache.ibatis.session.SqlSession;

public class GetModelCmd implements Command<PredictionModel> {

  protected String name;
  
  public GetModelCmd(String name) {
    this.name = name;
  }

  public PredictionModel execute(CmmnPredictionService service) {
    SqlSession session = null;
    try{
      session = service.sqlSessionFactory.openSession();
      return session.selectOne("PredictionModel.select", name);
    } finally
    {
      if (session != null)
      {
        session.close();
      }
    }
  }

}
