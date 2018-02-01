package org.kairosdb.client;

import static org.kairosdb.client.util.Preconditions.checkNotNullOrEmpty;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.kairosdb.client.builder.MetricBuilder;
import org.kairosdb.client.builder.QueryBuilder;
import org.kairosdb.client.builder.QueryTagBuilder;
import org.kairosdb.client.response.ErrorResponse;
import org.kairosdb.client.response.GetResponse;
import org.kairosdb.client.response.QueryResponse;
import org.kairosdb.client.response.QueryTagResponse;
import org.kairosdb.client.response.Response;

import com.google.gson.stream.JsonReader;
import org.kairosdb.client.util.JsonUtils;

/**
 * Base code used to send metrics to Kairos or query Kairos.
 */
public abstract class AbstractClient implements Client {
    private String url;
    private DataPointTypeRegistry typeRegistry;

    /**
     * Creates a client
     *
     * @param url url to the KairosDB server
     * @throws MalformedURLException if url is malformed
     */
    protected AbstractClient(String url) {
        this.url = checkNotNullOrEmpty(url);
//        new URL(url); // validate url  //去掉校验url

    }

    @Override
    public GetResponse getMetricNames() throws IOException {
        return get(url + "/api/v1/metricnames");
    }

    @Override
    public GetResponse getTagNames() throws IOException {
        return get(url + "/api/v1/tagnames");
    }

    @Override
    public GetResponse getTagValues() throws IOException {
        return get(url + "/api/v1/tagvalues");
    }

    @Override
    public GetResponse getStatus() throws IOException {
        return get(url + "/api/v1/health/status");
    }

    @Override
    public QueryTagResponse queryTag(QueryTagBuilder builder) throws URISyntaxException, IOException {
        ClientResponse clientResponse = postData(builder.build(), url + "/api/v1/datapoints/query/tags");
        int responseCode = clientResponse.getStatusCode();

        InputStream stream = clientResponse.getContentStream();
        return new QueryTagResponse(responseCode, stream);
    }

    @Override
    public QueryResponse query(QueryBuilder builder) throws URISyntaxException, IOException {
        ClientResponse clientResponse = postData(builder.build(), url + "/api/v1/datapoints/query");
        int responseCode = clientResponse.getStatusCode();

        InputStream stream = clientResponse.getContentStream();
        return new QueryResponse(responseCode, stream);
    }

    @Override
    public Response pushMetrics(MetricBuilder builder) throws URISyntaxException, IOException {
        checkNotNull(builder);
        if (builder.isCompressionEnabled()) {
            ClientResponse clientResponse = postCompressedData(builder.build(), url + "/api/v1/datapoints");
            return getResponse(clientResponse);
        } else {
            ClientResponse clientResponse = postData(builder.build(), url + "/api/v1/datapoints");
            return getResponse(clientResponse);
        }
    }

    @Override
    public Response deleteMetric(String name) throws IOException {
        checkNotNullOrEmpty(name);

        ClientResponse response = delete(url + "/api/v1/metric/" + name);
        return getResponse(response);
    }

    @Override
    public Response delete(QueryBuilder builder) throws URISyntaxException, IOException {
        checkNotNull(builder);
        ClientResponse clientResponse = postData(builder.build(), url + "/api/v1/datapoints/delete");

        return getResponse(clientResponse);
    }

    @Override
    public void registerCustomDataType(String groupType, Class dataPointClass) {
        typeRegistry.registerCustomDataType(groupType, dataPointClass);
    }

    @Override
    public Class getDataPointValueClass(String groupType) {
        return typeRegistry.getDataPointValueClass(groupType);
    }

    private Response getResponse(ClientResponse clientResponse) throws IOException {
        Response response = new Response(clientResponse.getStatusCode());
        InputStream stream = clientResponse.getContentStream();
        if (stream != null) {
            InputStreamReader reader = new InputStreamReader(stream);
            try {
                ErrorResponse errorResponse = JsonUtils.fromJson(reader, ErrorResponse.class);
                response.addErrors(errorResponse.getErrors());
            } finally {
                reader.close();
            }
        }
        return response;
    }

    private GetResponse get(String url) throws IOException {
        ClientResponse clientResponse = queryData(url);
        int responseCode = clientResponse.getStatusCode();

        if (responseCode == 200) {
            if (url.contains("health/status")) {
                return new GetResponse(responseCode);
            }
        }

        if (responseCode >= 400) {
            return new GetResponse(responseCode);
        } else {
            InputStream stream = clientResponse.getContentStream();
            if (stream == null) {
                throw new IOException("Could not get content stream.");
            }

            return new GetResponse(responseCode, readNameQueryResponse(stream));
        }
    }

    private List<String> readNameQueryResponse(InputStream stream) throws IOException {
        List<String> list = new ArrayList<String>();
        JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));

        try {
            reader.beginObject();
            String container = reader.nextName();
            if (container.equals("results")) {
                reader.beginArray();
                while (reader.hasNext()) {
                    list.add(reader.nextString());
                }
                reader.endArray();
                reader.endObject();
            }
        } finally {
            reader.close();
        }

        return list;
    }

    protected abstract ClientResponse postData(String json, String url) throws IOException;

    protected abstract ClientResponse postCompressedData(String json, String url) throws IOException;

    protected abstract ClientResponse queryData(String url) throws IOException;

    protected abstract ClientResponse delete(String url) throws IOException;
}
