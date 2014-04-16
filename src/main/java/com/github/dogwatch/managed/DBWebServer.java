package com.github.dogwatch.managed;

import io.dropwizard.lifecycle.Managed;

import org.h2.tools.Server;

public class DBWebServer implements Managed {
  String port;
  Server server;

  public DBWebServer(String port) {
    this.port = port;
  }

  @Override
  public void start() throws Exception {
    server = Server.createWebServer("-webPort", port).start();

  }

  @Override
  public void stop() throws Exception {
    server.start();
  }

}
