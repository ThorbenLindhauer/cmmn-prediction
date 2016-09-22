package org.camunda.bpm.hackdays.prediction;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class IoUtil {

  public static byte[] readInputStream(InputStream inputStream) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    byte[] buffer = new byte[16*1024];
    try {
      int bytesRead = inputStream.read(buffer);
      while (bytesRead!=-1) {
        outputStream.write(buffer, 0, bytesRead);
        bytesRead = inputStream.read(buffer);
      }
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    return outputStream.toByteArray();
  }
}
