package com.yao.wechatpaydemo;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;

import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.yao.wechatpaydemo.util.Constants;
import com.yao.wechatpaydemo.util.MD5;
import com.yao.wechatpaydemo.util.Util;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WxPayActivity extends AppCompatActivity {

    String c="0.1";
    private PayReq req;
    private final IWXAPI msgApi = WXAPIFactory.createWXAPI(this, null);
    private Map<String, String> resultunifiedorder;
    private StringBuffer sb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        req = new PayReq();
        sb = new StringBuffer();
        msgApi.registerApp(Constants.APP_ID);
        GetPrepayIdTask getPrepayId = new GetPrepayIdTask();
        getPrepayId.execute();

    }

    private class GetPrepayIdTask extends
            AsyncTask<Void, Void, Map<String, String>> {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(WxPayActivity.this,
                    getString(R.string.app_tip),
                    getString(R.string.getting_prepayid));
        }

        @Override
        protected void onPostExecute(Map<String, String> result) {
            if (dialog != null) {
                dialog.dismiss();
            }
            sb.append("prepay_id\n" + result.get("prepay_id") + "\n\n");
            Log.d("", "onPostExecute: " + sb.toString());

            resultunifiedorder = result;
            System.out.println("----------" + result);
            // 生成签名
            genPayReq();
        }

        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub
            super.onCancelled();
        }

        @Override
        protected Map<String, String> doInBackground(Void... params) {
            String url = String
                    .format("https://api.mch.weixin.qq.com/pay/unifiedorder");
            String entity = genProductArgs();

            Log.e("orion", entity);

            byte[] buf = Util.httpPost(url, entity);

            String content = new String(buf);
            Log.e("orion", content);
            Map<String, String> xml = decodeXml(content);

            return xml;
        }

    }

    public Map<String, String> decodeXml(String content) {
        try {
            Map<String, String> xml = new HashMap<String, String>();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(content));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {

                String nodeName = parser.getName();
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:

                        break;
                    case XmlPullParser.START_TAG:

                        if ("xml".equals(nodeName) == false) {
                            // 实例化student对象
                            xml.put(nodeName, parser.nextText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                event = parser.next();
            }

            return xml;
        } catch (Exception e) {
            Log.e("orion6", e.toString());
        }
        return null;
    }

    private String genProductArgs() {
        StringBuffer xml = new StringBuffer();

        try {
            String nonceStr = genNonceStr();

            xml.append("</xml>");
            List<NameValuePair> packageParams = new LinkedList<>();
            packageParams
                    .add(new BasicNameValuePair("appid", Constants.APP_ID));// 应用ID
            Log.d("dayin1", "genProductArgs: "+Constants.APP_ID);
            packageParams.add(new BasicNameValuePair("body", "ceshi"));// 商品描述
            packageParams
                    .add(new BasicNameValuePair("mch_id", Constants.MCH_ID));// 商户号
            Log.d("dayin2", "genProductArgs: "+Constants.MCH_ID);
            packageParams.add(new BasicNameValuePair("nonce_str", nonceStr));// 随机字符串
            Log.d("dayin3", "genProductArgs: "+nonceStr);
            packageParams.add(new BasicNameValuePair("notify_url",
                    "http://www.weixin.qq.com/wxpay/pay.php"));// 通知地址
            packageParams.add(new BasicNameValuePair("out_trade_no",
                    genOutTradNo()));// 商户订单号
            Log.d("dayin4", "genProductArgs: "+genOutTradNo());
            packageParams.add(new BasicNameValuePair("spbill_create_ip",
                    "192.168.0.108"));// 终端IP
            packageParams.add(new BasicNameValuePair("total_fee","20"));// 总金额
            packageParams.add(new BasicNameValuePair("trade_type", "APP"));// 交易类型

            String sign = genPackageSign(packageParams);
            packageParams.add(new BasicNameValuePair("sign", sign));// 签名

            Log.d("dayin5", "genProductArgs: "+sign);
            String xmlstring = toXml(packageParams);

            return new String(xmlstring.toString().getBytes(), "ISO-8859-1");
            // return xmlstring;

        } catch (Exception e) {
            return null;
        }
    }

    private String toXml(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>");
        for (int i = 0; i < params.size(); i++) {
            sb.append("<" + params.get(i).getName() + ">");
            sb.append(params.get(i).getValue());
            sb.append("</" + params.get(i).getName() + ">");
        }
        sb.append("</xml>");

        Log.e("orion", sb.toString());
        return sb.toString();
    }

    private String genNonceStr() {
        Random random = new Random();
        return MD5.getMessageDigest(String.valueOf(random.nextInt(10000))
                .getBytes());
    }

    private long genTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }

    private String genOutTradNo() {
        Random random = new Random();
        return MD5.getMessageDigest(String.valueOf(random.nextInt(10000))
                .getBytes());
    }

    //生成签名参数
    private void genPayReq() {
        req.appId = Constants.APP_ID;
        req.partnerId = Constants.MCH_ID;
        req.prepayId = resultunifiedorder.get("prepay_id");
        req.packageValue = "prepay_id=" + resultunifiedorder.get("prepay_id");
        req.nonceStr = genNonceStr();
        req.timeStamp = String.valueOf(genTimeStamp());

        List<NameValuePair> signParams = new LinkedList<NameValuePair>();
        signParams.add(new BasicNameValuePair("appid", req.appId));
        signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
        signParams.add(new BasicNameValuePair("package", req.packageValue));
        signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
        signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
        signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));

        req.sign = genAppSign(signParams);

        sb.append("sign\n" + req.sign + "\n\n");

        sendPayReq();
    }

    /**
     * 生成签名
     */
    private String genPackageSign(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).getName());
            sb.append('=');
            sb.append(params.get(i).getValue());
            sb.append('&');
        }
        sb.append("key=");
        sb.append(Constants.API_KEY);
        Log.d("", "genPackageSign: " + sb.toString());

        String packageSign = MD5.getMessageDigest(sb.toString().getBytes())
                .toUpperCase();
        Log.e("orion", packageSign);
        return packageSign;
    }

    private String genAppSign(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).getName());
            sb.append('=');
            sb.append(params.get(i).getValue());
            sb.append('&');
        }
        sb.append("key=");
        sb.append(Constants.API_KEY);

        this.sb.append("sign str\n" + sb.toString() + "\n\n");
        String appSign = MD5.getMessageDigest(sb.toString().getBytes())
                .toUpperCase();
        Log.e("orion", appSign);
        return appSign;
    }

    private void sendPayReq() {

        msgApi.registerApp(Constants.APP_ID);
        msgApi.sendReq(req);

    }
}
