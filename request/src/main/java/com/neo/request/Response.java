package com.neo.request;

import androidx.annotation.Nullable;

abstract class Response {

    static class Failure extends Response {

        private final Exception exception;

        public Failure(Exception exception) {
            this.exception = exception;
        }

        public Exception getException() {
            return exception;
        }
    }

    static class Success extends Response {

        @Nullable
        private final String body;

        @Nullable
        private final String errorBody;

        private final int statusCode;

        public Success(@Nullable String body, @Nullable String errorBody, int statusCode) {
            this.body = body;
            this.errorBody = errorBody;
            this.statusCode = statusCode;
        }

        @Nullable
        public String getBody() {
            return body;
        }

        @Nullable
        public String getErrorBody() {
            return errorBody;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}
