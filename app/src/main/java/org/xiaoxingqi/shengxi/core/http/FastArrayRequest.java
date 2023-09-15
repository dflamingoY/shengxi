package org.xiaoxingqi.shengxi.core.http;

import com.alibaba.fastjson.JSON;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 把json字符串解析成相应的集合
 * Created by Onlydyf on 2015/8/10.
 */
public class FastArrayRequest extends Request<List> {

	private Listener<List> listener;
	private Map<String,String> params;
	private Class clazz;
	public FastArrayRequest(String url, Map<String,String> params, Class clazz, Listener<List> listener, ErrorListener errorListener) {
		super(Method.POST,url, errorListener);
		this.listener=listener;
		this.params=params;
		this.clazz=clazz;

	}

	protected Response<List> parseNetworkResponse(NetworkResponse response) {
		List list=null;
		try {
			String json=new String(response.data,"utf-8");
			if (!json.isEmpty()&&!json.matches("^\\[null\\]$")) {
				list=(List) JSON.parseArray(json, clazz);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			list=new ArrayList();
		}
		return Response.success(list, HttpHeaderParser.parseCacheHeaders(response));
	}

	protected void deliverResponse(List response) {
		listener.onResponse(response);
	}

	@Override
	public void deliverError(VolleyError error) {
		super.deliverError(error);
	}

	protected Map<String, String> getParams() throws AuthFailureError {
		return params;
	}
}
