package com.yao.wechatpaydemo.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.yao.wechatpaydemo.util.Constants;
import com.yao.wechatpaydemo.MainActivity;
;




public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler{
	
	private static final String TAG = "MicroMsg.SDKSample.WXPayEntryActivity";
	
    private IWXAPI api;
	
   //标识符
 	int iq=0;
 	
 	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_pay);
        
        Log.d("wobuhzibsd","onCreate");
        
        
    	api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);
      	api.registerApp(Constants.APP_ID);
        api.handleIntent(getIntent(), this);
        
    }
    
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
		
	}

	/**
	 * 支付通知
	 * @param resp
     */
	@Override
	public void onResp(BaseResp resp) {
		Log.i("支付通知是多少1111", "支付通知是多"+resp.errCode);
		
		Intent intent = new Intent(WXPayEntryActivity.this,MainActivity.class);
		switch (resp.errCode) {
		case 0:
			Toast.makeText(WXPayEntryActivity.this, "您支付成功", Toast.LENGTH_SHORT).show();
			startActivity(new Intent(getApplicationContext(),MainActivity.class));
//			new MainActivity().onlinePay() ;
			finish();
			break;
		case -1:
			Toast.makeText(WXPayEntryActivity.this, "您已取消支付", Toast.LENGTH_SHORT).show();
			startActivity(intent);
			finish();
			break;
		case -2:
			Toast.makeText(WXPayEntryActivity.this, "您支付失败", Toast.LENGTH_SHORT).show();
			startActivity(intent);
			finish();
			break;
		}
		
	}
}