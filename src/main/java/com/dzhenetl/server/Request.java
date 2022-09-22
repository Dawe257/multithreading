package com.dzhenetl.server;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class Request {

    private String path;
    private List<NameValuePair> params;

    public Request(String query) {
        this.path = getQueryPath(query);
        this.params = getQueryParams(query);
    }

    private List<NameValuePair> getQueryParams(String request) {
        return URLEncodedUtils.parse(request.split("\\?")[1], StandardCharsets.UTF_8);
    }

    private String getQueryPath(String request) {
        return request.split("\\?")[0];
    }

    public String getPath() {
        return path;
    }

    public List<NameValuePair> getParams() {
        return params;
    }

    public String getQueryParam(String name) {
        Optional<NameValuePair> maybeParam = params.stream().filter(x -> x.getName().equals(name)).findAny();
        if (maybeParam.isPresent()) {
            return maybeParam.get().getValue();
        } else {
            throw new RuntimeException("Отсутствует параметр " + name);
        }
    }
}
