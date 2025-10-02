package com.dddpeter.app.rainweather.common;

import android.util.Log;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class OKHttpClientBuilder {
    public static OkHttpClient.Builder buildOKHttpClient() {
        try {
            TrustManager[] trustAllCerts = buildTrustManagers();
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);
            
            // 设置超时时间
            builder.connectTimeout(15, TimeUnit.SECONDS);      // 连接超时
            builder.readTimeout(30, TimeUnit.SECONDS);         // 读取超时
            builder.writeTimeout(30, TimeUnit.SECONDS);        // 写入超时
            
            // 设置重试机制
            builder.retryOnConnectionFailure(true);
            
            return builder;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            Log.w("OkHttp", "Exception: ", e);
            // 即使SSL配置失败，也要设置超时时间
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(15, TimeUnit.SECONDS);
            builder.readTimeout(30, TimeUnit.SECONDS);
            builder.writeTimeout(30, TimeUnit.SECONDS);
            builder.retryOnConnectionFailure(true);
            return builder;
        }
    }

    private static TrustManager[] buildTrustManagers() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };
    }
}
