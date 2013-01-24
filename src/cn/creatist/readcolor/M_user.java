package cn.creatist.readcolor;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

//user模型
public class M_user {
	private SharedPreferences store;
	private M_api api;
	public final int ID_UNLOGIN = 0;
	
	public String latest_error = null;
	
	public M_user(Context context){
		store = ((Activity) context).getSharedPreferences("user",Context.MODE_PRIVATE);
		api = new M_api(context);
		if(!logined()){
			String email = store.getString("email", "");
			String password = store.getString("password", "");
			if(email != "" && password != ""){
				if(!login(email, password))clear();//登录失败则清除帐号信息
			}
		}
	}
	public String toString(){
		String email = store.getString("email", "");
		String password = store.getString("password", "");
		return "email("+email+"),password("+password+"),token("+api.access_key+"),expires("+api.expiry_time+")";
	}
	//注册或者登录
	private boolean sign(String email,String password,int login_register){
		if(logined())return true;
		boolean ok = api.sign(email, password,login_register);
		if(ok){//成功登录
			update_storage(email,password);
			return true;
		}else{
			latest_error = api.latest_error;
		}
		return false;
	}
	
	public boolean login(String email,String password){
		return sign(email, password, api.LOGIN);
	}
	public boolean register(String email,String password){
		return sign(email, password, api.REGISTER);
	}
	
	private void update_storage(String email,String password){
		SharedPreferences.Editor e = store.edit();
		e.putString("email", email);
		e.putString("password", password);
		e.commit();
	}
	//主动退出登录或者由于错误id强制退出
	public void clear(){
		SharedPreferences.Editor e = store.edit();
		e.remove("email");
		e.remove("password");
		e.commit();
	}
	
	public void logout(){
		clear();
		api.clear();
	}
	
	public boolean logined(){
		return api.user_logined();
	}
}
