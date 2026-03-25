package io.github.ivagafonov.metrics.controller

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import akka.http.scaladsl.testkit.ScalatestRouteTest
import io.github.ivagafonov.metrics.MetricsSupport

class PrometheusControllerTest extends AnyFlatSpec with MetricsSupport with should.Matchers with ScalatestRouteTest {

  "Prometheus controller" should "have metrics to export" in {

    counter("test_counter_1").inc()

    Get("/api/metrics") ~> PrometheusController.routes ~> check {
      responseAs[String] should include ("unnamed_project_test_counter_1_total 1.0")
    }

    Get("/api/metrics-json") ~> PrometheusController.routes ~> check {
      responseAs[String] should include ("\"unnamed_project_test_counter_1\": 1.0")
    }

  }

}
