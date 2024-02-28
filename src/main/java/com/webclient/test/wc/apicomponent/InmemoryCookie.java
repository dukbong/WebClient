package com.webclient.test.wc.apicomponent;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InmemoryCookie implements CookieStore {

	private Map<URI, List<HttpCookie>> cookieStore = new HashMap<>();
	
	@Override
	public void add(URI uri, HttpCookie cookie) {
        cookieStore.putIfAbsent(uri, new ArrayList<>());
        cookieStore.get(uri).add(cookie);
	}

	@Override
	public List<HttpCookie> get(URI uri) {
		return cookieStore.getOrDefault(uri, new ArrayList<>());
	}

	@Override
	public List<HttpCookie> getCookies() {
        List<HttpCookie> cookies = new ArrayList<>();
        cookieStore.values().forEach(cookies::addAll);
        return cookies;
	}

	@Override
	public List<URI> getURIs() {
        return new ArrayList<>(cookieStore.keySet());
	}

	@Override
	public boolean remove(URI uri, HttpCookie cookie) {
        List<HttpCookie> cookies = cookieStore.get(uri);
        if (cookies != null) {
            return cookies.remove(cookie);
        }
        return false;
	}

	@Override
	public boolean removeAll() {
        cookieStore.clear();
        return true;
	}
	
}

