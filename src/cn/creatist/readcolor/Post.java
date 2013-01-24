package cn.creatist.readcolor;
import java.util.ArrayList;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
/*
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
*/
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.Editable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;


public class Post extends Activity{

	private ViewFlipper viewFlipper = null;
	private LayoutInflater inflater = null;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		//如果未登录则转到登录页面
		M_user user = new M_user(this);
		if(!user.logined()){
			Intent intent = new Intent(this,Login.class);
			startActivity(intent);
			finish();
			return;
		}
		
		setContentView(R.layout.flipper);
		
		viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper); 
		viewFlipper.setKeepScreenOn(true);
		//viewFlipper.setAutoStart(true);
        //viewFlipper.setFlipInterval(3000);
		
		inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		Bundle extras = this.getIntent().getExtras();
		if(!extras.isEmpty()){
			int id = extras.getInt("id");
			String title = extras.getString("title");
			String content = extras.getString("content");
			add_view(id,title, content);
		}
	}
	
	
	//增加下一篇文章
	public boolean new_post(boolean change){
		return record_post("random", 0);
	}
	public boolean like_post(int id){
		return record_post("like", id);
	}
	public boolean dislike_post(int id){
		return record_post("dislike", id);
	}
	public boolean next_post(int id){
		return record_post("next", id);
	}
	
	private boolean post_task = false;
	
	private boolean record_post(String action,int id){
		boolean succeed = false;
		if(!post_task){
			post_task = true;
			
			M_api api = new M_api(Post.this);
			
			ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();
			//
			if(action == "next" || action == "like" || action == "dislike"){
				param.add(new BasicNameValuePair("id", ""+id));
			}
			JSONObject result = api.query("post/"+action, param);
			try {
				if(result.getBoolean("succeed")){
					JSONObject post = result.getJSONObject("post");
					
					add_view(post.getInt("id"),post.getString("title"), post.getString("content"));
					viewFlipper.showNext();
					succeed = true;
				}
			}catch (JSONException e) {}
			Log.v("action",action);
			
			post_task = false;
		}
		return succeed;
	}
	
	//增加一个文章页面的视图，并绑定各种事件
	private void add_view(final int id,String title,String content){
		View v = inflater.inflate(R.layout.post,null);
		TextView contentView = (TextView) v.findViewById(R.id.post_content);
		TextView titleView = (TextView) v.findViewById(R.id.post_title);
		
		titleView.setText(title);
		contentView.setText(Html.fromHtml(content,new HtmlImage(),new HtmlImage()));
		
		ImageView setting = (ImageView) v.findViewById(R.id.post_setting);
		ImageView like = (ImageView) v.findViewById(R.id.post_like);
		LinearLayout next = (LinearLayout) v.findViewById(R.id.post_next);
		ImageView dislike = (ImageView) v.findViewById(R.id.post_dislike);
		
		setting.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				show_dialog("提示","还没实现啊");
			}
		});
		like.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				like_post(id);
			}
		});
		next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				next_post(id);
			}
		});
		dislike.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dislike_post(id);
			}
		});
		viewFlipper.addView(v);
	}
	
	private void show_dialog(String title,String content){
		Dialog dialog = new AlertDialog.Builder(this)
								.setTitle(title)
								.setMessage(content)
								.setPositiveButton("确定", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								}).create();
		dialog.show();
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY) {
		if (e2.getX() - e1.getX() > 20){
			Animation rInAnim = AnimationUtils.loadAnimation(this, R.anim.push_right_in);
            Animation rOutAnim = AnimationUtils.loadAnimation(this, R.anim.push_right_out);
  
            viewFlipper.setInAnimation(rInAnim);  
            viewFlipper.setOutAnimation(rOutAnim);
            viewFlipper.showPrevious();
            return true;
		}
		else if(e2.getX() - e1.getX() < -20){
			Animation lInAnim = AnimationUtils.loadAnimation(this, R.anim.push_left_in);
            Animation lOutAnim = AnimationUtils.loadAnimation(this, R.anim.push_left_out);
  
            viewFlipper.setInAnimation(lInAnim);  
            viewFlipper.setOutAnimation(lOutAnim);  
            viewFlipper.showNext();
            return true;
		}
		return false;
	}
	
	public void onPause(){
		super.onPause();
		//然后生成通知按钮
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.launch)
		        .setContentTitle("进入读彩")
		        .setContentText("开始阅读推荐文章。")
		        .setAutoCancel(true);
		Intent intent = this.getIntent();
		NotificationManager notice_manger = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
		mBuilder.setContentIntent(contentIntent);
		notice_manger.notify(0, mBuilder.build());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item){
		int item_id = item.getItemId();
		switch (item_id) {
		case R.id.logout:
			M_user user = new M_user(Post.this);
			user.logout();
			Intent intent = new Intent(Post.this,Login.class);
			startActivity(intent);
			finish();
			break;
		case R.id.exit_app:
			Post.this.finish();
			break;
		default:
			break;
		}
		return true;
	}
	
	protected class HtmlImage implements Html.ImageGetter ,Html.TagHandler{
		
		public Drawable getDrawable(String source) {
//			HttpGet request = new HttpGet(source);
//			HttpClient http = new DefaultHttpClient();
//			try {
//				HttpResponse response = http.execute(request);
//				if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
//					InputStream img = response.getEntity().getContent();
//					return Drawable.createFromStream(img, source);
//				}
//			} catch (ClientProtocolException e) {
//			} catch (IOException e) {}
			return null;
		}

		@Override
		public void handleTag(boolean opening, String tag, Editable output,XMLReader xmlReader) {
			
		}
	}

}
