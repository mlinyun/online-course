package com.mlinyun.onlinecourse.data.utils;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

import java.io.IOException;

/**
 * 代理对象实例化类
 */
public class HibernateProxyTypeAdapter extends TypeAdapter<HibernateProxy> {

    @Schema(description = "GSON数据")
    private final Gson context;

    @Override
    public HibernateProxy read(JsonReader reader) {
        throw new UnsupportedOperationException("UnsupportedOperationException");
    }

    public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
        @Override
        @SuppressWarnings("unchecked")
        public <T> TypeAdapter<T> create(Gson gsonObj, TypeToken<T> type) {
            if (HibernateProxy.class.isAssignableFrom(type.getRawType())) {
                return (TypeAdapter<T>) new HibernateProxyTypeAdapter(gsonObj);
            }
            return null;
        }
    };

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void write(JsonWriter jsonWriter, HibernateProxy proxy) throws IOException {
        if (proxy == null) {
            jsonWriter.nullValue();
            return;
        }
        Class<?> baseType = Hibernate.getClass(proxy);
        TypeAdapter delegate = context.getAdapter(TypeToken.get(baseType));
        Object unProxiedValue = ((HibernateProxy) proxy).getHibernateLazyInitializer().getImplementation();
        delegate.write(jsonWriter, unProxiedValue);
    }

    private HibernateProxyTypeAdapter(Gson gsonContext) {
        this.context = gsonContext;
    }

}
