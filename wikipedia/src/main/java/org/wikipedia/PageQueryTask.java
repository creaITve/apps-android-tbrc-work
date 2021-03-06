package org.wikipedia;

import android.text.TextUtils;
import org.json.JSONObject;
import org.mediawiki.api.json.Api;
import org.mediawiki.api.json.ApiResult;
import org.mediawiki.api.json.RequestBuilder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class PageQueryTask<T> extends ApiTask<Map<PageTitle, T>> {
    private final List<PageTitle> titles;
    private final Site site;

    public PageQueryTask(int concurrencyLevel, Api api, Site site, List<PageTitle> titles) {
        super(concurrencyLevel, api);
        this.titles = titles;
        this.site = site;
    }

    @Override
    public RequestBuilder buildRequest(Api api) {
        RequestBuilder builder = api.action("query").param("titles", TextUtils.join("|", titles));
        buildQueryParams(builder);
        return builder;
    }

    @Override
    public Map<PageTitle, T> processResult(ApiResult result) throws Throwable {
        Map<PageTitle, T> map = new HashMap<PageTitle, T>();
        JSONObject data = result.asObject();
        JSONObject query = data.getJSONObject("query");
        JSONObject pages = query.getJSONObject("pages");

        // You would think you could use foreach on an Iterator, but you can't.
        Iterator<String> keys = pages.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            int pageId = Integer.parseInt(key);
            JSONObject pageData = pages.getJSONObject(key);
            String titleString = pageData.getString("title");
            PageTitle pageTitle = new PageTitle(null, titleString, site);
            T pageResult = processPage(pageId, pageTitle, pageData);
            map.put(pageTitle, pageResult);
        }

        return map;
    }

    public abstract void buildQueryParams(RequestBuilder buildQueryParams);

    public abstract T processPage(int pageId, PageTitle pageTitle, JSONObject page) throws Throwable;
}
