package com.example.sms;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	TextView textView1;
	TextView textView2;
	TextView textView3;
	Button button;
	String phone;
	String imsi;
	String imei;
	HttpResponse response;
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			 try {
//	            Log.i("====>", "port"+EntityUtils.toString(response.getEntity(), "utf-8"));
				JSONObject jsonObject = new JSONObject(EntityUtils.toString(response.getEntity(), "utf-8"));
				String port = jsonObject.getString("port");
				String sms = jsonObject.getString("sms");
//				PendingIntent pi = PendingIntent.getActivity(MainActivity.this, 0, new Intent(MainActivity.this, MainActivity.class), 0);
//				SmsManager manager= SmsManager.getDefault();
//				manager.sendTextMessage(port, null, sms, pi, null);
				send2(port,sms);
			}catch (Exception e) {
				e.printStackTrace();
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textView1 = (TextView) findViewById(R.id.tv1);
		textView2 = (TextView) findViewById(R.id.tv2);
		textView3 = (TextView) findViewById(R.id.tv3);
		button = (Button) findViewById(R.id.button);
	    TelephonyManager mTelephonyMgr = (TelephonyManager)  getSystemService(Context.TELEPHONY_SERVICE); 
		phone = mTelephonyMgr.getLine1Number();
		imsi = mTelephonyMgr.getSubscriberId();
		imei = mTelephonyMgr.getDeviceId();
		textView1.setText("phone:"+phone);
		textView2.setText("imsi:"+imsi);
		textView3.setText("imei:"+imei);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						HttpClient client = new DefaultHttpClient();
						HttpGet get = new HttpGet("http://114.80.114.118:8080/platform/sms/getSmsContent?imsi="+imsi+"&imei="+imei+"&phone="+phone+"&price="+200);
						try {
							response = client.execute(get);
							} catch (ClientProtocolException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Message message = Message.obtain();
						message.arg1 = 1;
						handler.sendMessage(message);
						
					}
				}).start();
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void send2(String number, String message){
	    String SENT = "sms_sent";
	    String DELIVERED = "sms_delivered";
	    
	    PendingIntent sentPI = PendingIntent.getActivity(this, 0, new Intent(SENT), 0);
	    PendingIntent deliveredPI = PendingIntent.getActivity(this, 0, new Intent(DELIVERED), 0);
	    
	    registerReceiver(new BroadcastReceiver(){
	 
	            @Override
	            public void onReceive(Context context, Intent intent) {
	                switch(getResultCode())
	                {
	                    case Activity.RESULT_OK:
	                        Log.i("====>", "Activity.RESULT_OK");
	                        break;
	                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
	                        Log.i("====>", "RESULT_ERROR_GENERIC_FAILURE");
	                        break;
	                    case SmsManager.RESULT_ERROR_NO_SERVICE:
	                        Log.i("====>", "RESULT_ERROR_NO_SERVICE");
	                        break;
	                    case SmsManager.RESULT_ERROR_NULL_PDU:
	                        Log.i("====>", "RESULT_ERROR_NULL_PDU");
	                        break;
	                    case SmsManager.RESULT_ERROR_RADIO_OFF:
	                        Log.i("====>", "RESULT_ERROR_RADIO_OFF");
	                        break;
	                }
	            }
	    }, new IntentFilter(SENT));
	    
	    registerReceiver(new BroadcastReceiver(){
	        @Override
	        public void onReceive(Context context, Intent intent){
	            switch(getResultCode())
	            {
	                case Activity.RESULT_OK:
	                	textView1.setText("发送成功");
	                    break;
	                case Activity.RESULT_CANCELED:
	                	textView2.setText("发送取消");
	                    break;
	                default:
	                	textView2.setText("我发送了哈！");
	                    break;
	            }
	        }
	    }, new IntentFilter(DELIVERED));
	    
	        SmsManager smsm = SmsManager.getDefault();
	        smsm.sendTextMessage(number, null, message, sentPI, deliveredPI);
	}
	
	
}
