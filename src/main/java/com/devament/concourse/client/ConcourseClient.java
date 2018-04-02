package com.devament.concourse.client;

import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class ConcourseClient {

	private static final String CONCOURSE_AUTH_URL = "concourse_auth_url";
	private static final String UAA_AUTH_URL = "uaa_auth_url";
	private static final String CONCOURSE_ROOT_PATH = "";

	public static void execute(String pipelineName) throws Exception {
		BasicCookieStore cookieStore = new BasicCookieStore();

		HttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
		HttpClientContext context = HttpClientContext.create();
		context.setCookieStore(cookieStore);
		context.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);

		final RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();

		HttpGet httpGet = new HttpGet(CONCOURSE_AUTH_URL);
		httpGet.setConfig(requestConfig);
		httpGet.setHeader("Accept", "application/json");

		HttpResponse response = client.execute(httpGet, context);
		EntityUtils.consume(response.getEntity());

		URI uaaAuthURI = context.getRedirectLocations().get(0);

		httpGet = new HttpGet(UAA_AUTH_URL + "/login");
		httpGet.setConfig(requestConfig);

		response = client.execute(httpGet, context);
		EntityUtils.consume(response.getEntity());
		List<HttpCookie> cookies = HttpCookie.parse(response.getHeaders("Set-Cookie")[0].getValue());

		HttpPost httpPost = new HttpPost(UAA_AUTH_URL + "/login.do");
		httpPost.setConfig(requestConfig);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Referer", UAA_AUTH_URL + "/login");
		httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("username", "UAA_USER"));
		params.add(new BasicNameValuePair("password", "UAA_PASSWORD"));
		params.add(new BasicNameValuePair("X-Uaa-Csrf", cookies.get(0).getValue()));
		httpPost.setEntity(new UrlEncodedFormEntity(params));

		response = client.execute(httpPost, context);

		httpGet = new HttpGet(uaaAuthURI);
		httpGet.setHeader("Accept", "application/json");
		httpGet.setConfig(requestConfig);
		response = client.execute(httpGet, context);
		URI tokenURI = context.getRedirectLocations().get(0);
		System.err.println(tokenURI);

		httpGet = new HttpGet(tokenURI);
		httpGet.setHeader("Accept", "application/json");
		httpGet.setConfig(requestConfig);
		response = client.execute(httpGet, context);
		List<Cookie> cookieList = cookieStore.getCookies();
		String bearerValue = "";
		for (Cookie cookie : cookieList) {
			if ("ATC-Authorization".equals(cookie.getName())) {
				bearerValue = cookie.getValue();
			}

		}
		
		httpGet = new HttpGet(CONCOURSE_ROOT_PATH + "/api/v1/teams/main/pipelines/"+pipelineName+"/jobs/"+CONCOURSE_JOB_NAME);
		httpGet.setHeader("Authorization", bearerValue);
		client = HttpClientBuilder.create().build();
		ResponseHandler<String> handler = new BasicResponseHandler();
		response = client.execute(httpGet);
		String body = handler.handleResponse(response);

		// get last build details
		JsonObject jsonBodyObject = new JsonParser().parse(body).getAsJsonObject();
		String jobName = jsonBodyObject.get("name").getAsString();
		JsonObject jsonBuildObject = new JsonParser().parse(jsonBodyObject.get("next_build").toString())
				.getAsJsonObject();
		String buildId = jsonBuildObject.get("id").getAsString();
		System.out.println("pipeline_name: " + pipelineName);
		System.out.println("job_name: " + jobName);
		System.out.println("build_id: " + buildId);
		
		// set build_id to consul
		HttpPut httpPut = new HttpPut(CONSUL_ROOT_PATH + "/" + pipelineName + "/" + jobName);
		httpPut.setEntity(new StringEntity(issueId+ "#" + buildId));
		client = HttpClientBuilder.create().build();
		response = client.execute(httpPut);
		System.out.println(response.getStatusLine().getStatusCode());
		
	}

}
