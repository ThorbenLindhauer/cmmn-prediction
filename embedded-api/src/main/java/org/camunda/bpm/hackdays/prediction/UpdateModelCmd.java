package org.camunda.bpm.hackdays.prediction;

import org.apache.ibatis.session.SqlSession;

public class UpdateModelCmd implements Command<Void> {

  protected PredictionModel model;
  
  public UpdateModelCmd(PredictionModel model) {
    this.model = model;
  }

  public Void execute(CmmnPredictionService predictionService) {
    SqlSession sqlSession = null;
    
    try {
      sqlSession = predictionService.sqlSessionFactory.openSession();
//      sqlSession.update("PredictionModel.update", model); // model is immutable for now
      
      for (PredictionModelPrior prior : model.getPriors()) {
        sqlSession.update("PredictionModelPrior.update", prior);
      }
      
      sqlSession.commit();
    } finally {
      sqlSession.close();
    }
    
    return null;
  }

}
