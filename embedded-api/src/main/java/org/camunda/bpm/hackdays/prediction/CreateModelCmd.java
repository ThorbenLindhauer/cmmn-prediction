package org.camunda.bpm.hackdays.prediction;

import org.apache.ibatis.session.SqlSession;

public class CreateModelCmd implements Command<Void> {

  protected PredictionModel model;
  
  public CreateModelCmd(PredictionModel model) {
    this.model = model;
  }

  public Void execute(CmmnPredictionService predictionService) {
    SqlSession sqlSession = null;
    
    try {
      sqlSession = predictionService.sqlSessionFactory.openSession();
      sqlSession.insert("PredictionModel.insert", model);
    } finally {
      sqlSession.close();
    }
    
    return null;
    
    
  }

}
