package io.github.ivagafonov.metrics.controller

import akka.http.scaladsl.model.{HttpCharsets, HttpEntity, MediaTypes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.github.ivagafonov.metrics.MetricSupport

object PrometheusController extends MetricSupport {

  val routes: Route = metrics ~ jsonMetrics

  private def jsonMetrics: Route = path("api" / "metrics-json") {
    get {
      complete(
        HttpEntity(
          MediaTypes.`application/json`,
          toJsonString
        )
      )
    }
  }

  private def metrics: Route = path("api" / "metrics") {
    get {
        complete(
          HttpEntity(
            MediaTypes.`text/plain` withParams Map("version" -> "0.0.4") withCharset HttpCharsets.`UTF-8`,
            toPrometheus
          )
        )
    }
  }

}
