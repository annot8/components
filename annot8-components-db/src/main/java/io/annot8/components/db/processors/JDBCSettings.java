/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.db.processors;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.annot8.core.settings.Settings;

public class JDBCSettings {

  private final String jdbcUrl;
  private final String user;
  private final String password;

  public JDBCSettings(String jdbcUrl) {
    this(jdbcUrl, null, null);
  }

  public JDBCSettings(String jdbcUrl, String user, String password) {
    this.jdbcUrl = jdbcUrl;
    this.user = user;
    this.password = password;
  }

  public String getJdbcUrl() {
    return jdbcUrl;
  }

  public String getPassword() {
    return password;
  }

  public String getUser() {
    return user;
  }
}
