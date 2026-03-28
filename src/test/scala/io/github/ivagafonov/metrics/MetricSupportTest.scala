package io.github.ivagafonov.metrics

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import scala.util.Random

class MetricSupportTest extends AnyFlatSpec with should.Matchers with MetricSupport {

  "Counter" should "increment metric" in {
    counter("test_counter", "label1" -> "value1", "label2" -> "value2").inc()
    counter("test_counter", "label2" -> "value2", "label1" -> "value1").inc()

    toPrometheus should include("unnamed_project_test_counter_total{label1=\"value1\",label2=\"value2\"} 2.0")
  }

  "Gauge" should "keep metric" in {
    gauge("test_gauge", "label1" -> "value1", "label2" -> "value2").set(30)
    gauge("test_gauge", "label2" -> "value2", "label1" -> "value1").inc()
    toPrometheus should include("unnamed_project_test_gauge{label1=\"value1\",label2=\"value2\"} 31.0")
  }

  "GaugeObserve" should "update metric" in {
    gaugeOnScrape("test_gauge_observe", "label1", "label2")(() => Measure(Random.nextDouble(), "value1", "value2"))
    toPrometheus should include("test_gauge_observe{label1=\"value1\",label2=\"value2\"}")
  }

  "Summary" should "calc metric" in {
    summary("test_summary", "label1" -> "value1", "label2" -> "value2").observe(10)
    summary("test_summary", "label2" -> "value2", "label1" -> "value1").observe(10)

    toPrometheus should include("unnamed_project_test_summary{label1=\"value1\",label2=\"value2\",quantile=\"0.1\"} 10.0")
    toPrometheus should include("unnamed_project_test_summary{label1=\"value1\",label2=\"value2\",quantile=\"0.99\"} 10.0")
  }

  "Timer" should "count time" in {
    timer("test_timer", "label1" -> "value1", "label2" -> "value2")(Thread.sleep(100))
    timer("test_timer", "label2" -> "value2", "label1" -> "value1")(Thread.sleep(100))

    toPrometheus should include("unnamed_project_test_timer{label1=\"value1\",label2=\"value2\",quantile=\"0.1\"} 0.1")
    toPrometheus should include("unnamed_project_test_timer{label1=\"value1\",label2=\"value2\",quantile=\"0.99\"} 0.1")
  }

  "Histogram" should "calc metric" in {
    histogram("test_histogram", "label1" -> "value1", "label2" -> "value2").observe(10)
    histogram("test_histogram", "label2" -> "value2", "label1" -> "value1").observe(10)

    toPrometheus should include("unnamed_project_test_histogram_bucket{label1=\"value1\",label2=\"value2\",le=\"8.0\"} 0")
    toPrometheus should include("unnamed_project_test_histogram_bucket{label1=\"value1\",label2=\"value2\",le=\"16.0\"} 2")
  }

}
