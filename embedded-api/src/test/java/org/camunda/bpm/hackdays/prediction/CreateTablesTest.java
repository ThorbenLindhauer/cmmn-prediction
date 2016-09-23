package org.camunda.bpm.hackdays.prediction;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CreateTablesTest {

  protected DataSource dataSource;
  
  @Before
  public void setUp() throws Exception {
//    Class.forName("org.h2.Driver");
    dataSource = new PooledDataSource("org.h2.Driver", "jdbc:h2:mem:foo", null);
//    connection = DriverManager.getConnection("jdbc:h2:mem:foo");
  }
  
  @Test
  public void shouldCreateTables() throws Exception {
    // given
    CmmnPredictionService service = CmmnPredictionService.buildStandalone(dataSource);
    
    // when
    service.createDbTables();
    
    // then
    ResultSet resultSet = dataSource.getConnection().createStatement().executeQuery("SHOW TABLES");
    Set<String> tableNames = new HashSet<String>();
    
    while (resultSet.next())
    {
      tableNames.add(resultSet.getString(1));
    }
    
    assertThat(tableNames).hasSize(2);
    assertThat(tableNames).contains("PREDICTION_MODEL", "PREDICTION_PRIOR");
  }
  
}
