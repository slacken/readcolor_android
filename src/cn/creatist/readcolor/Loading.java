package cn.creatist.readcolor;


import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.app.Activity;
import android.content.Intent;

public class Loading extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.loading);
	}
	
	public void onStart(){
		super.onStart();
		
		M_user user = new M_user(this);
		Log.v("user",user.toString());
		if(user.logined()){
			new Thread(){
				public void run(){
					Intent pIntent;
					pIntent = new Intent(Loading.this, Post.class);
					//预加载文章
					prepare(pIntent);//传地址
					next(pIntent);
				}
			}.start();
			
		}else{
			new Thread(){
				public void run(){
					try {
						sleep(1000);//  :)
						Intent pIntent;
						pIntent = new Intent(Loading.this,Login.class);
						next(pIntent);
					} catch (InterruptedException e) {}
				}
			}.start();
		}
	}
	
	protected void next(Intent intent){
		startActivity(intent);
		this.finish();
	}
	
	//为post加载数据
	public void prepare(Intent intent){
		M_api api = new M_api(this);
		JSONObject result = api.query("post/random", new ArrayList<NameValuePair>());
		//Log.v("result",result.toString());
		try {
			if(result.getBoolean("succeed")){
				Bundle bundle = new Bundle();
				JSONObject post = result.getJSONObject("post");
				
				//Log.v("post",post.toString());
				bundle.putInt("id", post.getInt("id"));
				bundle.putString("title", post.getString("title"));
				bundle.putString("content", post.getString("content"));
				intent.putExtras(bundle);
				
			}else{
				
			}
		} catch (JSONException e) {}
		
	}

}
