package org.camunda.bpm.hackdays.prediction;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CreateTablesTest {

  protected Connection connection;
  
  @Before
  public void setUp() throws Exception {
    Class.forName("org.h2.Driver");
    connection = DriverManager.getConnection("jdbc:h2:mem:foo");
  }
  
  @After
  public void tearDown() throws Exception
  {
    if (connection != null) {
      connection.close();
    }
  }

  @Test
  public void shouldCreateTables() throws Exception {
    // given
    CmmnPredicitionService service = new CmmnPredicitionService();
    
    // when
    service.createDbTables(connection);
    
    // then
    ResultSet resultSet = connection.createStatement().executeQuery("SHOW TABLES");
    int rows = 0;
    Set<String> tableNames = new HashSet<String>();
    
    while (resultSet.next())
    {
      rows++;
      tableNames.add(resultSet.getString(1));
    }
    
    assertThat(rows).isEqualTo(1);
    assertThat(tableNames).containsExactly("PREDICTION_MODEL");
  }
}
