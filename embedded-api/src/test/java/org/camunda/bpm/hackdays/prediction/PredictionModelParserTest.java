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
    assertThat(fooVariable.getValues()).containsOnlyKeys(0, 1);
    assertThat(fooVariable.getValues().get(0).getExpression()).isEqualTo("${intVal < 100}");
    assertThat(fooVariable.getValues().get(0).getName()).isEqualTo("fooCat1");
    assertThat(fooVariable.getValues().get(1).getExpression()).isEqualTo("${intVal >= 100}");
    assertThat(fooVariable.getValues().get(1).getName()).isEqualTo("fooCat2");
    
    DiscreteVariable barVariable = model.getVariables().get("bar");
    assertThat(barVariable.getName()).isEqualTo("bar");
    assertThat(barVariable.getValues().get(0).getExpression()).isEqualTo("${intVal < 50}");
    assertThat(barVariable.getValues().get(0).getName()).isEqualTo("barCat1");
    assertThat(barVariable.getValues().get(1).getExpression()).isEqualTo("${intVal >= 50}");
    assertThat(barVariable.getValues().get(1).getName()).isEqualTo("barCat2");
    
    assertThat(model.getDependencies()).containsOnlyKeys("bar");
    assertThat(model.getDependencies().get("bar")).containsExactly("foo");
  }
}
