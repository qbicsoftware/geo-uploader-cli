package connection;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CredentialHandler {

  private String url, pw, userID;

  public CredentialHandler(String pathToCredentialProperties) {
    Properties prop = new Properties();
    InputStream input = null;

    try {

      input = new FileInputStream(pathToCredentialProperties);

      // load a properties file
      prop.load(input);

      // get the property value and print it out
      url = prop.getProperty("datasource.url");
      pw = prop.getProperty("datasource.password");
      userID = prop.getProperty("datasource.user");

    } catch (IOException ex) {
      ex.printStackTrace();
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

  }

  public String getUrl() {
    return url;
  }

  public String getPw() {
    return pw;
  }

  public String getUserID() {
    return userID;
  }
}
