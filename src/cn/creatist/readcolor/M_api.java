package cn.creatist.readcolor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;




//API模型
public class M_api {
	private final String auth_base = "http://twenti.sinaapp.com/authorize/";
	private final String api_base = "http://twenti.sinaapp.com/api/";
	public final int LOGIN = 1;
	public final int REGISTER = 2;
	public final String DEFAULT_KEY = "public";//默认的access_key，适用于未登录用户
	protected String access_key;//存取密钥，有效期存储在服务器上
	protected long expiry_time;
	
	public String latest_error;
	
	private SharedPreferences store;//用来存取access_key
	
	public M_api(Context context){
		store = ((Activity) context).getSharedPreferences("user",Context.MODE_PRIVATE);
		try {
			access_key = URLEncoder.encode(store.getString("access_key", "default"),"utf8");
			expiry_time = store.getLong("expiry_time", 0);
			if(!user_logined() && expiry_time!=0 && access_key!="default")clear();
		} catch (UnsupportedEncodingException e) {}
	}
	
	public boolean user_logined(){
		return expiry_time > new Date().getTime()/1000;
	}
	
	private JSONObject post_request(String url,List<NameValuePair> params){
		JSONObject obj = new JSONObject();
		try {
			String result_str = "";
			HttpPost http = new HttpPost(url + "?token=" + access_key);
			if(!params.isEmpty()){
				http.setEntity(new UrlEncodedFormEntity(params,"utf8"));
				//Log.v("参数",params.toString());
			}
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(http);
			
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				result_str = EntityUtils.toString(response.getEntity());
				//Log.v("result string",result_str);//服务器返回的内容
				
				obj = new JSONObject(result_str);
				try{
					int error = obj.getInt("error");
					if(error < 5)clear();
				}catch(JSONException e){}
				
			}else{
				obj.put("succeed", false);
				obj.put("error_info", "网络错误:"+response.getStatusLine().getStatusCode());
				Log.v("网络错误:",""+response.getStatusLine().getStatusCode());
			}
		} catch (IOException e) {
			Log.v("http error",e.getMessage());
			try {
				obj.put("succeed", false);
				obj.put("error_info", "与服务的连接连接出错了");
			} catch (JSONException e1) {}
		} catch (JSONException e) {
			obj = new JSONObject();
			try {
				obj.put("succeed", false);
				obj.put("error_info", "服务器返回数据格式有误");
			}catch (JSONException e1) {}
		} catch(Exception e){
			obj = new JSONObject();
			try {
				obj.put("succeed", false);
				obj.put("error_info", e.getMessage());//其他错误
			}catch (JSONException e1) {}
		}
		return obj;
	}
	
	public void clear(){
		SharedPreferences.Editor e = store.edit();
		e.remove("access_key");
		e.remove("expiry_time");
		e.commit();
	}
	
	public void test(){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("test", "测试参数值！"));
		Log.v("Json Object",post_request("http://twenty.app/api/", params).toString());
	}
	
	//登录或注册
	public boolean sign(String email,String password,int login_register){
		String http_url = auth_base + (login_register == LOGIN?"login":"register");
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("email", email));
		params.add(new BasicNameValuePair("password", password));
		
		JSONObject result = post_request(http_url, params);
		try {
			//Log.v("返回结果",result.toString());
			if(result.getBoolean("succeed")){
				//然后是更新access_key
				String token = result.getString("token");
				long expires = result.getLong("expires");
				
				//Log.v("expires",""+expires);
				
				access_key = token;
				expiry_time = expires + new Date().getTime()/1000;
				
				SharedPreferences.Editor e = store.edit();
				e.putString("access_key", access_key);
				e.putLong("expiry_time", expiry_time);
				e.commit();
				//
				return true;
			}else{
				latest_error = result.getString("error_info");
			}
		}catch (JSONException e) {}
		//返回用户id
		return false;
	}
	
	public JSONObject query(String action,List<NameValuePair> params){
		String url = api_base + action;
		return post_request(url, params);
	}
}
