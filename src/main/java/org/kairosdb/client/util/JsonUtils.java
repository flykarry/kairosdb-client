package org.kairosdb.client.util;

import com.google.common.collect.ListMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.kairosdb.client.DataPointTypeRegistry;
import org.kairosdb.client.builder.DataPoint;
import org.kairosdb.client.builder.QueryMetric;
import org.kairosdb.client.builder.aggregator.CustomAggregator;
import org.kairosdb.client.builder.grouper.CustomGrouper;
import org.kairosdb.client.deserializer.GroupByDeserializer;
import org.kairosdb.client.deserializer.ResultsDeserializer;
import org.kairosdb.client.response.GroupResult;
import org.kairosdb.client.response.Result;
import org.kairosdb.client.serializer.*;

import java.io.Reader;
import java.lang.reflect.Type;
import java.util.TimeZone;

/**
 * Created by gaojun on 2018/2/1.
 */
public class JsonUtils {
    private static Gson mapper;


    static {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(CustomAggregator.class, new CustomAggregatorSerializer());
        builder.registerTypeAdapter(CustomGrouper.class, new CustomGrouperSerializer());
        builder.registerTypeAdapter(DataPoint.class, new DataPointSerializer());
        builder.registerTypeAdapter(ListMultimap.class, new ListMultiMapSerializer());
        builder.registerTypeAdapter(QueryMetric.Order.class, new OrderSerializer());
        builder.registerTypeAdapter(TimeZone.class, new TimeZoneSerializer());

        builder.registerTypeAdapter(GroupResult.class, new GroupByDeserializer());
        builder.registerTypeAdapter(Result.class, new ResultsDeserializer(new DataPointTypeRegistry()));
        mapper = builder.create();
    }

    public static String toJson(Object src) {
        return mapper.toJson(src);
    }


    public static <T> T fromJson(Reader json, Type typeOfT) {
        return mapper.fromJson(json, typeOfT);
    }

    public static <T> T fromJson(String json, Type typeOfT) {
        return mapper.fromJson(json, typeOfT);
    }
}
