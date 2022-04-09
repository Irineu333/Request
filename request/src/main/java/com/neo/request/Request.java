package com.neo.request;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Request {

    private final Method method;
    private final String url;
    private final List<Query> queries;
    private final Body body;
    private final Auth auth;

    public Request(Method method, String url, List<Query> queries, Body body, Auth auth) {
        this.method = method;
        this.url = url;
        this.queries = queries;
        this.body = body;
        this.auth = auth;
    }

    public Response execute() {
        HttpURLConnection urlConnection = null;

        try {
            final URL url;

            if (queries.isEmpty()) {
                url = new URL(this.url);
            } else {
                url = new URL(putQueries(this.url));
            }

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(method.name());

            if (auth != null) {
                putAuth(urlConnection);
            }

            if (body != null) {
                putBody(urlConnection);
            } else {
                urlConnection.connect();
            }

            int statusCode = urlConnection.getResponseCode();

            String body = getBody(urlConnection);
            String errorBody = getErrorBody(urlConnection);

            return new Response.Success(
                    body,
                    errorBody,
                    statusCode
            );
        } catch (IOException e) {
            e.printStackTrace();
            return new Response.Failure(e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private void putAuth(HttpURLConnection urlConnection) {
        urlConnection.setRequestProperty("Authorization", auth.getData());
    }

    private void putBody(HttpURLConnection urlConnection) throws IOException {

        urlConnection.setRequestProperty("Content-Type", body.getType());
        urlConnection.setRequestProperty("Content-Length", String.valueOf(body.getData().length()));

        if (!Stream.put(urlConnection.getOutputStream(), body.getData())) {
            throw new IOException("could not put body");
        }
    }

    private String putQueries(String url) {

        StringBuilder builder = new StringBuilder(url);

        builder.append("?");

        for (int i = 0; i < queries.size(); i++) {
            Query query = queries.get(i);

            if (i > 0) {
                builder.append("&");
            }

            builder.append(query.key)
                    .append("=")
                    .append(query.value);
        }

        return builder.toString();
    }

    private String getErrorBody(HttpURLConnection urlConnection) {
        try {
            InputStream errorStream = urlConnection.getErrorStream();

            if (errorStream == null) return null;

            return Stream.read(errorStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getBody(HttpURLConnection urlConnection) {
        try {
            InputStream inputStream = urlConnection.getInputStream();

            if (inputStream == null) return null;

            return Stream.read(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void then(
            final OnResultListener result
    ) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        final Response response = execute();

                        new Handler(Looper.getMainLooper())
                                .post(new Runnable() {
                                    @Override
                                    public void run() {
                                        result.onResult(response);
                                    }
                                });
                    }
                }
        ).start();
    }

    public enum Method {
        GET,
        POST,
        HEAD,
        OPTIONS,
        PUT,
        DELETE,
        TRACE
    }

    static class Query {
        private final String key;
        private final String value;

        public Query(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    static class Builder {

        private final Method method;
        private final String url;
        private final List<Query> queries = new ArrayList<>();
        private Body body;
        public Auth auth;

        public Builder(Method method, String url) {
            this.method = method;
            this.url = url;
        }

        public Builder queries(Query... queries) {
            this.queries.addAll(Arrays.asList(queries));
            return this;
        }

        public Builder query(Query query) {
            this.queries.add(query);
            return this;
        }

        public Builder body(Request.Body body) {
            this.body = body;
            return this;
        }

        public Builder auth(Request.Auth auth) {
            this.auth = auth;
            return this;
        }

        public Request build() {
            return new Request(
                    method,
                    url,
                    queries,
                    body,
                    auth
            );
        }
    }

    static abstract class Body {

        private final String type;
        private final String data;

        public Body(String type, String data) {
            this.type = type;
            this.data = data;
        }

        public String getType() {
            return type;
        }

        public String getData() {
            return data;
        }

        public static class Raw extends Body {
            public Raw(String raw) {
                super("application/json", raw);
            }
        }
    }

    static abstract class Auth {

        private final String data;

        public Auth(String data) {
            this.data = data;
        }

        public String getData() {
            return data;
        }

        static class BearerToken extends Auth {

            public BearerToken(String token) {
                super("Bearer " + token);
            }
        }
    }
}
