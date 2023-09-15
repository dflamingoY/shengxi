package org.xiaoxingqi.shengxi.core.http;

import com.alibaba.fastjson.JSON;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * 把json字符串解析成相应的字典
 * Created by Onlydyf on 2015/8/10.
 */
public class FastMapRequest extends Request<Map<String,Object>> {

	private Listener<Map<String,Object>> listener;
	private Map<String,String> params;
	public FastMapRequest(String url, Map<String,String> params, Listener<Map<String,Object>> listener, ErrorListener errorListener) {
		super(Method.POST,url, errorListener);
		this.listener=listener;
		this.params=params;
	}

	protected Response<Map<String,Object>> parseNetworkResponse(NetworkResponse response) {
		Map<String,Object> map=null;
		try {
			String json=new String(response.data,"utf-8");
			if (!json.isEmpty()&&!json.matches("^\\[null\\]$")) {
				map=(Map<String, Object>) JSON.parse(json);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			map=new HashMap<>();
		}
		return Response.success(map, HttpHeaderParser.parseCacheHeaders(response));
	}

	protected void deliverResponse(Map<String,Object> response) {
		listener.onResponse(response);
	}

	protected Map<String, String> getParams() throws AuthFailureError {
		return params;
	}

	@Override
	public void deliverError(VolleyError error) {
		super.deliverError(error);
	}

	public RetryPolicy getRetryPolicy() {
		RetryPolicy retryPolicy = new DefaultRetryPolicy(3000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
		return retryPolicy;
	}
}

