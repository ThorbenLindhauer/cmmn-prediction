package org.camunda.bpm.hackdays.prediction;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;

import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel;
import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel.DiscreteVariable;
import org.camunda.bpm.hackdays.prediction.model.PredictionModelParser;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PredictionModelParserTest {

  
  @Test
  public void shouldParsePredictionModel() throws Exception {
    // given
    PredictionModelParser parser = new PredictionModelParser(new ObjectMapper());
    InputStream inputStream = PredictionModelParserTest.class.getClassLoader().getResourceAsStream("model.json");
    
    // when
    ParsedPredictionModel model = parser.parse(inputStream);
    
    // then
    assertThat(model.getVariables()).containsOnlyKeys("foo", "bar");
    
    DiscreteVariable fooVariable = model.getVariables().get("foo");
    assertThat(fooVariable.getName()).isEqualTo("foo");
    assertThat(fooVariable.getValueExpressions()).containsOnlyKeys("fooCat1", "fooCat2");
    assertThat(fooVariable.getValueExpressions().get("fooCat1")).isEqualTo("${intVal < 100}");
    assertThat(fooVariable.getValueExpressions().get("fooCat2")).isEqualTo("${intVal >= 100}");
    
    DiscreteVariable barVariable = model.getVariables().get("bar");
    assertThat(barVariable.getName()).isEqualTo("bar");
    assertThat(barVariable.getValueExpressions()).containsOnlyKeys("barCat1", "barCat2");
    assertThat(barVariable.getValueExpressions().get("barCat1")).isEqualTo("${intVal < 50}");
    assertThat(barVariable.getValueExpressions().get("barCat2")).isEqualTo("${intVal >= 50}");
    
    assertThat(model.getDependencies()).containsOnlyKeys("bar");
    assertThat(model.getDependencies().get("bar")).containsExactly("foo");
  }
}
