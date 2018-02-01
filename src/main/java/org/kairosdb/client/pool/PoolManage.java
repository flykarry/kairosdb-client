package org.kairosdb.client.pool;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * 线程池管理
 * Created by gaojun on 2018/2/1.
 */
public class PoolManage {

    //不耗时，且必使用，不用懒加载
    private static PoolManage poolManage = new PoolManage();
    private HttpClientBuilder clientBuilder = HttpClientBuilder.create();
    private PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();


    private PoolManage() {
        clientBuilder.setConnectionManager(connectionManager);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                connectionManager.closeExpiredConnections();
                connectionManager.closeIdleConnections(2, TimeUnit.MINUTES);
            }
        }, 1000 * 60, 1000 * 60);
    }

    public static PoolManage getInstance() {
        return poolManage;
    }


    public CloseableHttpClient getHttpClient() {
        return clientBuilder.build();
    }

    public void setMaxTotal(int max) {
        connectionManager.setMaxTotal(max);
    }

    public void setDefaultMaxPerRoute(int max) {
        connectionManager.setDefaultMaxPerRoute(max);
    }
}
