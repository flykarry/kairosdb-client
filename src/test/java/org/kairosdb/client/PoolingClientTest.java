package org.kairosdb.client;

import org.junit.Test;
import org.kairosdb.client.builder.QueryBuilder;
import org.kairosdb.client.builder.TimeUnit;
import org.kairosdb.client.pool.PoolManage;
import org.kairosdb.client.response.QueryResponse;

/**
 * Created by gaojun on 2018/2/1.
 */
public class PoolingClientTest {


    @Test
    public void queryTest() throws Exception {
        PoolingClient poolingClient = new PoolingClient("http://192.168.13.110:8080");
        poolingClient.setClient(PoolManage.getInstance().getHttpClient());

        QueryBuilder builder = QueryBuilder.getInstance();
        builder.setStart(3, TimeUnit.MONTHS)
                .addMetric("energybasedata").addTag("guidmeasureparamid","2d9e8dbf-a883-4d64-8701-5056aeda942a");
        QueryResponse response = poolingClient.query(builder);

        System.out.println(response.getBody());
    }
}
