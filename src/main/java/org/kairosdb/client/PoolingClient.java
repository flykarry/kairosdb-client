package org.kairosdb.client;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.kairosdb.client.pool.PoolManage;

import java.io.IOException;
import java.net.MalformedURLException;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by gaojun on 2018/2/1.
 */
public class PoolingClient extends AbstractClient {

    private CloseableHttpClient client;
    private int retries = 3;

    /**
     * Creates a client
     *
     * @throws MalformedURLException if url is malformed
     */
    public PoolingClient(String url) throws MalformedURLException {
        super(url);
    }

    @Override
    protected ClientResponse postData(String json, String url) throws IOException {
        StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
        HttpPost postMethod = new HttpPost(url);
        postMethod.setEntity(requestEntity);

        return execute(postMethod);
    }

    @Override
    protected ClientResponse postCompressedData(String json, String url) throws IOException {
        HttpEntity entity = EntityBuilder.create()
                .setText(json)
                .setContentType(ContentType.TEXT_PLAIN)
                .gzipCompress()
                .build();
        HttpPost post = new HttpPost(url);
        post.setEntity(entity);
        post.setHeader("Content-Type", "application/gzip");
        return execute(post);
    }

    @Override
    protected ClientResponse queryData(String url) throws IOException {
        HttpGet getMethod = new HttpGet(url);
        getMethod.addHeader("accept", "application/json");

        return execute(getMethod);
    }

    @Override
    protected ClientResponse delete(String url) throws IOException {
        HttpDelete deleteMethod = new HttpDelete(url);
        deleteMethod.addHeader("accept", "application/json");

        return execute(deleteMethod);
    }

    protected ClientResponse execute(HttpUriRequest request) throws IOException {
        HttpResponse response;

        int tries = retries + 1;
        while (true) {
            tries--;
            try {
                response = client.execute(request);
                break;
            } catch (IOException e) {
                if (tries < 1)
                    throw e;
            }
        }

        return new HttpClientResponse(response);
    }

    @Override
    public void shutdown() throws IOException {
        client.close();
    }

    @Override
    public int getRetryCount() {
        return retries;
    }

    public void setRetryCount(int retries) {
        checkArgument(retries >= 0);
        this.retries = retries;
    }

    protected CloseableHttpClient getClient() {
        return this.client;
    }

    public void setClient(CloseableHttpClient client) {
        this.client = client;
    }

}