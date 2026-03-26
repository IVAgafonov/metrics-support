package io.github.ivagafonov.metrics

import io.prometheus.metrics.config.EscapingScheme
import io.prometheus.metrics.core.datapoints.{CounterDataPoint, DistributionDataPoint, GaugeDataPoint}
import io.prometheus.metrics.core.metrics.{Counter, Gauge, Histogram, Metric, Summary}
import io.prometheus.metrics.expositionformats.PrometheusTextFormatWriter
import io.prometheus.metrics.model.registry.PrometheusRegistry
import io.prometheus.metrics.model.snapshots.CounterSnapshot.CounterDataPointSnapshot
import io.prometheus.metrics.model.snapshots.{DataPointSnapshot, DistributionDataPointSnapshot, SummarySnapshot}
import io.prometheus.metrics.model.snapshots.GaugeSnapshot.GaugeDataPointSnapshot
import io.prometheus.metrics.model.snapshots.HistogramSnapshot.HistogramDataPointSnapshot
import io.prometheus.metrics.model.snapshots.SummarySnapshot.SummaryDataPointSnapshot
import spray.json.{JsNumber, JsObject, JsValue}

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters.IteratorHasAsScala
import scala.util.{Failure, Success, Try}

trait MetricSupport {

  private val prefix = sys.env.getOrElse("PROJECT_NAME", "unnamed_project").toLowerCase().replace(" .,", "_")

  private val metricsCache: ConcurrentHashMap[String, Metric] = new ConcurrentHashMap[String, Metric]

  def counter(name: String, labels: (String, String)*): CounterDataPoint = {
    val sortedMetricLabels = labels.sortWith(_._1 > _._1)

    val metricName = "counter_" + prefix + "_" + name + "_" + sortedMetricLabels.map(_._1).mkString("_")

    val counterMetric = metricsCache.computeIfAbsent(metricName, _ => {

      def getCounter(name: String, labelNames: Seq[String]): Counter = {
        Counter.builder()
          .name(prefix + "_" + name)
          .labelNames(labelNames: _*)
          .register(PrometheusRegistry.defaultRegistry)
      }

      Try {
        getCounter(name, sortedMetricLabels.map(_._1))
      } match {
        case Success(value) => value
        case Failure(_) =>
          getCounter(name + "_1", sortedMetricLabels.map(_._1))
      }

    }).asInstanceOf[Counter]

    counterMetric.labelValues(sortedMetricLabels.map(_._2): _*)
  }

  def summary(name: String, quantiles: Seq[Double], labels: (String, String)*): DistributionDataPoint = {
    val sortedMetricLabels = labels.sortWith(_._1 > _._1)

    val metricName = "summary_" + prefix + "_" + name + "_" + sortedMetricLabels.map(_._1).mkString("_")

    val summaryMetric = metricsCache.computeIfAbsent(metricName, _ => {

      def getSummary(name: String, labelNames: Seq[String]): Summary = {
        val builder = Summary.builder()
          .name(prefix + "_" + name)
          .labelNames(labelNames: _*)

          quantiles.foreach(builder.quantile)

          builder.register(PrometheusRegistry.defaultRegistry)
      }

      Try {
        getSummary(name, sortedMetricLabels.map(_._1))
      } match {
        case Success(value) => value
        case Failure(_) =>
          getSummary(name + "_1", sortedMetricLabels.map(_._1))
      }

    }).asInstanceOf[Summary]

    summaryMetric.labelValues(sortedMetricLabels.map(_._2): _*)
  }

  def summary(name: String, labels: (String, String)*): DistributionDataPoint = {
    summary(name, Seq(0.99, 0.9, 0.5, 0.1), labels: _*)
  }

  def timer[T](name: String, labels: (String, String)*)(f: => T): T = {
    val now = System.currentTimeMillis()
    val res = f
    summary(name, labels: _*).observe((System.currentTimeMillis() - now).toDouble / 1000)
    res
  }

  def gauge(name: String, labels: (String, String)*): GaugeDataPoint = {
    val sortedMetricLabels = labels.sortWith(_._1 > _._1)

    val metricName = "gauge_" + prefix + "_" + name + "_" + sortedMetricLabels.map(_._1).mkString("_")

    val gaugeMetric = metricsCache.computeIfAbsent(metricName, _ => {

      def getGauge(name: String, labelNames: Seq[String]): Gauge = {
        Gauge.builder()
          .name(prefix + "_" + name)
          .labelNames(labelNames: _*)
          .register(PrometheusRegistry.defaultRegistry)
      }

      Try {
        getGauge(name, sortedMetricLabels.map(_._1))
      } match {
        case Success(value) => value
        case Failure(_) =>
          getGauge(name + "_1", sortedMetricLabels.map(_._1))
      }

    }).asInstanceOf[Gauge]

    gaugeMetric.labelValues(sortedMetricLabels.map(_._2): _*)
  }

  def histogram(name: String, labels: (String, String)*): DistributionDataPoint = {
    histogram(name, 1, 2, 10, labels: _*)
  }

  def histogram(name: String, start: Int, factor: Int, count: Int, labels: (String, String)*): DistributionDataPoint = {
    val sortedMetricLabels = labels.sortWith(_._1 > _._1)

    val metricName = "histogram_" + prefix + "_" + name + "_" + sortedMetricLabels.map(_._1).mkString("_")

    val histogramMetric = metricsCache.computeIfAbsent(metricName, _ => {

      def getHistogram(name: String, labelNames: Seq[String]): Histogram = {
        Histogram.builder()
          .name(prefix + "_" + name)
          .labelNames(labelNames: _*)
          .classicExponentialUpperBounds(start, factor, count)
          .register(PrometheusRegistry.defaultRegistry)
      }

      Try {
        getHistogram(name, sortedMetricLabels.map(_._1))
      } match {
        case Success(value) => value
        case Failure(_) =>
          getHistogram(name + "_1", sortedMetricLabels.map(_._1))
      }

    }).asInstanceOf[Histogram]

    histogramMetric.labelValues(sortedMetricLabels.map(_._2): _*)
  }

  def toPrometheus: String = {
    val metricsOutput = new ByteArrayOutputStream()
    PrometheusTextFormatWriter.create()
      .write(metricsOutput, PrometheusRegistry.defaultRegistry.scrape(), EscapingScheme.UNDERSCORE_ESCAPING)

    metricsOutput.toString(StandardCharsets.UTF_8)
  }


  def toJsonString: String = {
    val json = JsObject(Map(
      "counter" -> JsObject(Map.empty[String, JsValue]),
      "summary" -> JsObject(Map.empty[String, JsValue]),
      "histogram" -> JsObject(Map.empty[String, JsValue]),
      "gauge" -> JsObject(Map.empty[String, JsValue]),
    ))

    val res = PrometheusRegistry.defaultRegistry.scrape().iterator().asScala
      .foldLeft(json)((js, m) => {
        m.getDataPoints.iterator().asScala.foldLeft(js)((acc: JsObject, dp: DataPointSnapshot) => {
          val metricName = m.getMetadata.getName + dp.getLabels.iterator().asScala.map(l =>  "[" + l.getName + ":" + l.getValue + "]").mkString
          var histogramAcc: Long = 0
          dp match {
            case v: CounterDataPointSnapshot =>
              JsObject(acc.fields + ("counter" -> JsObject(acc.fields("counter").asJsObject.fields + (metricName -> JsNumber(v.getValue)))))
            case v: SummaryDataPointSnapshot =>
              JsObject(acc.fields + ("summary" -> JsObject(acc.fields("summary").asJsObject.fields ++ v.getQuantiles.iterator().asScala.zipWithIndex.map(q => metricName + "[q_" + (q._2 + 97).toChar + ":" + q._1.getQuantile + "]"-> JsNumber(q._1.getValue)).concat(
                Seq(
                  metricName + "[sum]" -> JsNumber(v.getSum),
                  metricName + "[count]" -> JsNumber(v.getCount),
                )
              ))))
            case v: HistogramDataPointSnapshot =>
              JsObject(acc.fields + ("histogram" -> JsObject(acc.fields("histogram").asJsObject.fields ++ v.getClassicBuckets.iterator().asScala.zipWithIndex.map(q => metricName + "[b_" + (q._2 + 97).toChar + "_" + q._1.getUpperBound + "]" -> JsNumber({
                histogramAcc += q._1.getCount
                histogramAcc
              })).concat(
                Seq(
                  metricName + "[sum]" -> JsNumber(v.getSum),
                  metricName + "[count]" -> JsNumber(v.getCount),
                )
              ))))
            case v: GaugeDataPointSnapshot =>
              JsObject(acc.fields + ("gauge" -> JsObject(acc.fields("gauge").asJsObject.fields + (metricName -> JsNumber(v.getValue)))))
            case _ => js
          }
        })
      })

    res.sortedPrint
  }

}
