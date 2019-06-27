/*
 * Copyright 2018, Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hipstershop;


import hipstershop.Demo.Ad;
import hipstershop.Demo.AdRequest;
import hipstershop.Demo.AdResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.opencensus.common.Duration;
import io.opencensus.common.Scope;
import io.opencensus.contrib.grpc.metrics.RpcViews;
import io.opencensus.contrib.grpc.util.StatusConverter;
import io.opencensus.exporter.stats.stackdriver.StackdriverStatsConfiguration;
import io.opencensus.exporter.stats.stackdriver.StackdriverStatsExporter;
import io.opencensus.exporter.trace.stackdriver.StackdriverTraceConfiguration;
import io.opencensus.exporter.trace.stackdriver.StackdriverTraceExporter;
import io.opencensus.trace.Span;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.samplers.Samplers;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.prometheus.client.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import static hipstershop.AdServiceClient.ExampleServlet.inprogressRequests;

/** A simple client that requests ads from the Ads Service. */
public class AdServiceClient {

  static {
    //HACK Avoids duplicate metrics registration in case of Spring Boot dev-tools restarts
    CollectorRegistry.defaultRegistry.clear();
  }
  static class ExampleServlet extends HttpServlet {
    static final Gauge inprogressRequests = Gauge.build()
            .name("inprogress_requests").help("Inprogress requests.").register();
    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
      resp.getWriter().println("Hello World!");
      // Increment the number of requests.
      //inprogressRequests.inc();
    }
  }


  private static final Logger logger = LogManager.getLogger(AdServiceClient.class);
  private static final Tracer tracer = Tracing.getTracer();

  private final ManagedChannel channel;
  private final hipstershop.AdServiceGrpc.AdServiceBlockingStub blockingStub;

  /** Construct client connecting to Ad Service at {@code host:port}. */
  public AdServiceClient(String host, int port) {
    this(
        ManagedChannelBuilder.forAddress(host, port)
            // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
            // needing certificates.
            .usePlaintext(true)
            .build());
  }

  /** Construct client for accessing RouteGuide server using the existing channel. */
  private AdServiceClient(ManagedChannel channel) {
    this.channel = channel;
    blockingStub = hipstershop.AdServiceGrpc.newBlockingStub(channel);
  }

  private void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  /** Get Ads from Server. */
  public void getAds(String contextKey) {
    inprogressRequests.inc();
    logger.info("Get Ads with context " + contextKey + " ...");
    AdRequest request = AdRequest.newBuilder().addContextKeys(contextKey).build();
    AdResponse response;

    Span span =
        tracer
            .spanBuilder("AdsClient")
            .setRecordEvents(true)
            .setSampler(Samplers.alwaysSample())
            .startSpan();
    try (Scope scope = tracer.withSpan(span)) {
      tracer.getCurrentSpan().addAnnotation("Getting Ads");
      response = blockingStub.getAds(request);
      tracer.getCurrentSpan().addAnnotation("Received response from Ads Service.");
    } catch (StatusRuntimeException e) {
      tracer.getCurrentSpan().setStatus(StatusConverter.fromGrpcStatus(e.getStatus()));
      logger.log(Level.WARN, "RPC failed: " + e.getStatus());
      return;
    } finally {
      span.end();
    }
    for (Ad ads : response.getAdsList()) {
      logger.info("Ads: " + ads.getText());
    }
    inprogressRequests.dec();
  }

  private static int getPortOrDefaultFromArgs(String[] args, int index, int defaultPort) {
    int portNumber = defaultPort;
    if (index < args.length) {
      try {
        portNumber = Integer.parseInt(args[index]);
      } catch (NumberFormatException e) {
        logger.warn(
            String.format("Port %s is invalid, use default port %d.", args[index], defaultPort));
      }
    }
    return portNumber;
  }

  private static String getStringOrDefaultFromArgs(
      String[] args, int index, @Nullable String defaultString) {
    String s = defaultString;
    if (index < args.length) {
      s = args[index];
    }
    return s;
  }

  /**
   * Ads Service Client main. If provided, the first element of {@code args} is the context key to
   * get the ads from the Ads Service
   */
  public static void main(String[] args) throws InterruptedException {


    final String contextKeys = getStringOrDefaultFromArgs(args, 0, "camera");
    final String host = getStringOrDefaultFromArgs(args, 1, "localhost");
    final int serverPort = getPortOrDefaultFromArgs(args, 2, 9555);


    org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server(serverPort);
    ServletContextHandler context = new ServletContextHandler();
    context.setContextPath("/");
    server.setHandler(context);
    // Expose our example servlet.
    context.addServlet(new ServletHolder(new ExampleServlet()), "/");
    // Expose Promtheus metrics.
    context.addServlet(new ServletHolder(new MetricsServlet()), "/metrics");
    // Add metrics about CPU, JVM memory etc.
    DefaultExports.initialize();

    // Start the webserver.
    try {
      server.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
//    server.join();



    // Add final keyword to pass checkStyle.


    // Registers all RPC views.
    RpcViews.registerAllGrpcViews();

    // Registers Stackdriver exporters.
    long sleepTime = 10; /* seconds */
    int maxAttempts = 3;

    for (int i = 0; i < maxAttempts; i++) {
      try {
        StackdriverTraceExporter.createAndRegister(StackdriverTraceConfiguration.builder().build());
        StackdriverStatsExporter.createAndRegister(
            StackdriverStatsConfiguration.builder()
                .setExportInterval(Duration.create(15, 0))
                .build());
      } catch (Exception e) {
        if (i == (maxAttempts - 1)) {
          logger.log(
              Level.WARN,
              "Failed to register Stackdriver Exporter."
                  + " Tracing and Stats data will not reported to Stackdriver. Error message: "
                  + e.toString());
        } else {
          logger.info("Attempt to register Stackdriver Exporter in " + sleepTime + " seconds");
          try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(sleepTime));
          } catch (Exception se) {
            logger.log(Level.WARN, "Exception while sleeping" + e.toString());
          }
        }
      }
    }

    // Register Prometheus exporters and export metrics to a Prometheus HTTPServer.
    // PrometheusStatsCollector.createAndRegister();

    AdServiceClient client = new AdServiceClient(host, serverPort);
    for(int i=0;i<100;i++){
      try {
        client.getAds(contextKeys);
      } finally {
        client.shutdown();
      }
      Thread.sleep(2000);
    }


    logger.info("Exiting AdServiceClient...");
  }
}
