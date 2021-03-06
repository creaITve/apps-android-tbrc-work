package org.wikipedia;

import android.content.Context;
import com.github.kevinsawicki.http.HttpRequest;
import com.squareup.okhttp.HttpResponseCache;
import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

public class OkHttpConnectionFactory implements HttpRequest.ConnectionFactory {
    private static final long HTTP_CACHE_SIZE = 16 * 1024 * 1024;

    private final OkHttpClient client;

    public OkHttpConnectionFactory(Context context) {
        client = new OkHttpClient();
        client.setCookieHandler(((WikipediaApp)context.getApplicationContext()).getCookieManager());

        try {
            client.setResponseCache(new HttpResponseCache(context.getCacheDir(), HTTP_CACHE_SIZE));
        } catch (IOException e) {
            // Shouldn't happen...
            throw new RuntimeException(e);
        }
    }

    public HttpURLConnection create(URL url) throws IOException {
        return client.open(url);
    }

    @Override
    public HttpURLConnection create(URL url, Proxy proxy) throws IOException {
        throw new UnsupportedOperationException(
                "Per-connection proxy is not supported. Use OkHttpClient's setProxy instead.");
    }
}
