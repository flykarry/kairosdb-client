/*
 * Copyright 2013 Proofpoint Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.kairosdb.client.builder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.kairosdb.client.builder.aggregator.CustomAggregator;
import org.kairosdb.client.builder.grouper.CustomGrouper;
import org.kairosdb.client.serializer.CustomAggregatorSerializer;
import org.kairosdb.client.serializer.CustomGrouperSerializer;
import org.kairosdb.client.serializer.DataPointSerializer;
import org.kairosdb.client.util.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;

/**
 * Builder used to create the JSON to push metrics to KairosDB.
 */
public class MetricBuilder {
    private List<Metric> metrics = new ArrayList<Metric>();
    private boolean useCompression = false;

    private MetricBuilder() {
    }

    /**
     * Returns a new metric builder.
     *
     * @return metric builder
     */
    public static MetricBuilder getInstance() {
        return new MetricBuilder();
    }

    /**
     * Adds a metric to the builder.
     *
     * @param metricName metric name
     * @return the new metric
     */
    public Metric addMetric(String metricName) {
        Metric metric = new Metric(metricName);
        metrics.add(metric);
        return metric;
    }

    /**
     * Adds a metric to the builder with a customer type.
     *
     * @param metricName     metric name
     * @param registeredType type used to deserialize the json on the server
     * @return the new metric
     */
    public Metric addMetric(String metricName, String registeredType) {
        Metric metric = new Metric(metricName, registeredType);
        metrics.add(metric);
        return metric;
    }

    /**
     * Returns a list of metrics added to the builder.
     *
     * @return list of metrics
     */
    public List<Metric> getMetrics() {
        return metrics;
    }

    /**
     * Sets the compression flag for http post.
     *
     * @param compressionEnabled
     */
    public void setCompression(boolean compressionEnabled) {
        useCompression = compressionEnabled;
        return;
    }

    /**
     * Returns the compression flag
     *
     * @return
     */
    public boolean isCompressionEnabled() {
        return useCompression;
    }

    /**
     * Returns the JSON string built by the builder. This is the JSON that can be used by the client add metrics.
     *
     * @return JSON
     * @throws IOException if metrics cannot be converted to JSON
     */
    public String build() throws IOException {
        for (Metric metric : metrics) {
            // verify that there is at least one tag for each metric
            checkState(metric.getTags().size() > 0, metric.getName() + " must contain at least one tag.");
        }
        return JsonUtils.toJson(metrics);
    }
}
