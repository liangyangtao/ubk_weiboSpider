package com.unbank.weibo.login;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.util.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.unbank.featch.HttpClientBuilder;

/***
 * 
 * @author 梁杨桃
 * 
 *         发现网上的大部分教程都失效了ssologin.js都是老版本了。 已经不是最新的了 用的是(v1.4.18)
 * 
 * 
 * 
 */
public class WeiboLoginByHttpClinet {

	public static RequestConfig requestConfig = RequestConfig.custom()
			.setSocketTimeout(30000).setConnectTimeout(30000).build();
	public static BasicCookieStore cookieStore = new BasicCookieStore();

	public static CloseableHttpClient httpClient;
	public static String loginUrl = "http://weibo.com/login.php";
	public static String callUrl = "http://login.sina.com.cn/sso/prelogin.php?entry=weibo&callback=sinaSSOController.preloginCallBack&su=&rsakt=mod&client=ssologin.js(v1.4.18)&_=";
	public static String ssologinUrl = "http://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.4.18)";

	public static void main(String[] args) {
		PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
		HttpClientBuilder httpClientBuilder = new HttpClientBuilder(false,
				poolingHttpClientConnectionManager, cookieStore);
		httpClient = httpClientBuilder.getHttpClient();
		String username = "";
		String password = "";
		WeiboLoginByHttpClinet weiboLoginByHttpClinet = new WeiboLoginByHttpClinet();

		weiboLoginByHttpClinet.login(username, password);
		// weiboLoginByHttpClinet.spiderByPeple("http://weibo.com/u/2396658275");
		// weiboLoginByHttpClinet.spiderByKeyword("银行");

	}

	/**
	 * 登陆后根据关键词搜索
	 * 
	 * 
	 * 
	 */
	private void spiderByKeyword(String keyword) {
		try {
			keyword = URLEncoder.encode(keyword, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(keyword);
		String url = "http://s.weibo.com/weibo/" + keyword
				+ "?topnav=1&wvr=6&b=1";
		String html = getHtml(httpClient, url, null);
		// System.out.println(html);
		Document document = Jsoup.parse(html);
		Elements scriptElements = document.select("script");
		Element contentElement = null;
		for (Element element : scriptElements) {
			if (element.toString().contains("\"pid\":\"pl_weibo_direct\"")) {
				contentElement = element;
				break;
			}
		}
		System.out.println(contentElement);
		String myContent = StringUtils.substringBetween(
				contentElement.toString(),
				"<script>STK && STK.pageletM && STK.pageletM.view(",
				")</script>");
		JSONObject jsonObject = JSONObject.fromObject(myContent);
		String content = jsonObject.getString("html");
		Document homeFeedDocument = Jsoup.parse(content);
		// WB_cardwrap WB_notes
		Elements weiboList = homeFeedDocument.select("div.WB_cardwrap");
		for (Element element : weiboList) {
			try {
				System.out.println(element.text());
			} catch (Exception e) {
				continue;
			}
		}
	}

	/**
	 * 
	 * 登陆后解析一个人的微博 ，记得先模拟登陆
	 * 
	 * 新浪的内容在javascript中，可以解析Json字符串
	 * 
	 * 
	 * 
	 * 
	 * */
	private void spiderByPeple(String url) {
		String html = getHtml(httpClient, url, getCookiesString());
		Document document = Jsoup.parse(html);
		Elements scriptElements = document.select("script");
		Element contentElement = null;
		for (Element element : scriptElements) {
			if (element.toString().contains("pl.content.homeFeed.index")
					&& element.toString().contains("Pl_Official_MyProfileFeed")) {
				contentElement = element;
				break;
			}
		}

		String myContent = StringUtils.substringBetween(
				contentElement.toString(), "<script>FM.view(", ")</script>");
		JSONObject jsonObject = JSONObject.fromObject(myContent);
		String content = jsonObject.getString("html");
		Document homeFeedDocument = Jsoup.parse(content);
		Elements weiboList = homeFeedDocument.select("div.WB_cardwrap");
		for (Element element : weiboList) {
			try {
				System.out.println(element.text());
			} catch (Exception e) {
				continue;
			}
		}

	}

	/**
	 * 登陆
	 * 
	 * */

	public boolean login(String username, String password) {
		/**
		 * 第一步：访问 http://weibo.com/login.php
		 * 
		 * 
		 * 使得Cookie 里包含login_sid_t ，TC_Ugrow_G0
		 * 
		 * 
		 * 
		 * */
		String html = getHtml(httpClient, loginUrl, getCookiesString());

		// String login_sid_t = StringUtils.substringBetween(cookies,
		// "login_sid_t=   ", ";");
		// System.out.println("login_sid_t    :  " + login_sid_t);
		//
		// String TC_Ugrow_G0 = StringUtils.substringBetween(cookies,
		// "TC-Ugrow-G0=", ";");
		// System.out.println("TC_Ugrow_G0     " + TC_Ugrow_G0);

		long DateTime = new Date().getTime();
		// System.out.println(DateTime);
		callUrl = callUrl + DateTime;

		/**
		 * 第二步：http://login.sina.com.cn/sso/prelogin.php?entry=weibo&callback=
		 * sinaSSOController
		 * .preloginCallBack&su=Njc0NjEzNDM4JTQwcXEuY29t&rsakt=mod
		 * &checkpin=1&client=ssologin.js(v1.4.18)&_=
		 * 
		 * 获取servertime ，pcid，pubkey，rsakv，nonce
		 * 
		 * */
		html = getHtml(httpClient, callUrl, getCookiesString());
		// sinaSSOController.preloginCallBack({"retcode":0,"servertime":1440403744,"pcid":"xd-29af00e904fa0c7e2736ea8cc6b7fcd99717","nonce":"ULSRFO","pubkey":"EB2A38568661887FA180BDDB5CABD5F21C7BFD59C090CB2D245A87AC253062882729293E5506350508E7F9AA3BB77F4333231490F915F6D63C55FE2F08A49B353F444AD3993CACC02DB784ABBB8E42A9B1BBFFFB38BE18D78E87A0E41B9B8F73A928EE0CCEE1F6739884B9777E4FE9E88A1BBE495927AC4A799B3181D6442443","rsakv":"1330428213","showpin":0,"exectime":10})
		String servertime = StringUtils.substringBetween(html, "servertime\":",
				",");
		// System.out.println("servertime        " + servertime);
		String pcid = StringUtils.substringBetween(html, "pcid\":\"", "\",\"");
		// System.out.println("pcid    " + pcid);
		String pubkey = StringUtils.substringBetween(html, "pubkey\":\"",
				"\",\"");
		// System.out.println("pubkey     " + pubkey);
		String rsakv = StringUtils.substringBetween(html, "rsakv\":\"", "\"");
		// System.out.println("rsakv       " + rsakv);
		String nonce = StringUtils.substringBetween(html, "nonce\":\"", "\"");
		// System.out.println("nonce        " + nonce);
		/**
		 * 
		 * 第三步： 1 用户名需要 base 64 ecode
		 * 
		 * 2 密码需要加密
		 * 
		 * 
		 * 
		 * */
		// 1
		String su = encodeAccount(username);
		// System.out.println("____" + su + "____dfdsfds");
		// 2
		// String messageg = servertime + "\t" + nonce + "\n" + password;
		String sp = ssorskPassword(pubkey, servertime, nonce, password);
		// System.out.println(sp.trim());
		/**
		 * 第四步 ：提交参数进行登陆
		 * 
		 * 获取 校验 url
		 * 
		 * */
		Map<String, String> params = new HashMap<String, String>();
		params.put("entry", "weibo");
		params.put("gateway", "1");
		params.put("from", "");
		params.put("savestate", "7");
		params.put("useticket", "1");
		params.put("pagerefer", "");
		params.put("vsnf", "1");
		params.put("su", su.trim());
		params.put("service", "miniblog");
		params.put("servertime", servertime);
		params.put("nonce", nonce);
		params.put("pwencode", "rsa2");
		params.put("rsakv", rsakv);
		params.put("sp", sp.trim());
		params.put("sr", "1920*1080");
		params.put("encoding", "UTF-8");
		params.put("prelt", "35");
		params.put(
				"url",
				"http://weibo.com/ajaxlogin.php?framelogin=1&callback=parent.sinaSSOController.feedBackUrlCallBack");
		params.put("returntype", "META");
		html = post(httpClient, ssologinUrl, params, "utf-8",
				getCookiesString());
		// System.out.println(html);
		/**
		 * 获取到结果 <html> <head> <title>Sina Passport</title> <meta
		 * http-equiv="Content-Type" content="text/html; charset=GBK" />
		 * 
		 * <script charset="utf-8"
		 * src="http://i.sso.sina.com.cn/js/ssologin.js"></script> </head>
		 * <body> Signing in ... <script>
		 * try{sinaSSOController.setCrossDomainUrlList({"retcode":0,"arrURL":[
		 * "http:\/\/crosdom.weicaifu.com\/sso\/crosdom?action=login&savestate=1477987419","http:\/\/passport.97973.com\/sso\/crossdomain?action=login&savestate=1477987419","http:\/\/passport.weibo.cn\/sso\/crossdomain?action=login&savestate=1"]});}catch(e){}try{sinaSSOController.crossDomainAction('login',function(){location.replace('http://passport.weibo.com/wbsso/login?ssosavestate=1477987419&url=http%3A%2F%2Fweibo.com%2Fajaxlogin.php%3Fframelogin%3D1%26callback%3Dparent.sinaSSOController.feedBackUrlCallBack&ticket=ST-MjQ3ODk5MzQ2NQ==-1446451419-xd
		 * - 4 9 B B 0 2 8 C A F C 0 3 9 E 1 2 E B 9 9 9 C A A 6 0 3 F A 4 A & r
		 * e t c o d e = 0 ' ) ; } ) ; } c a t c h ( e ) { } </script> </body>
		 * </html>
		 * 
		 * */

		String url = StringUtils.substringBetween(html, "location.replace('",
				"');");
		// System.out.println(url);
		// url =
		// "http://weibo.com/ajaxlogin.php?framelogin=1&callback=parent.sinaSSOController.feedBackUrlCallBack&sudaref=weibo.com";
		/**
		 * 第5步： 访问URL 操作后如果看到自己的uid 说明登陆成功，就可以采集了
		 */
		html = getHtml(httpClient, url, getCookiesString());
		System.out.println(html);
		System.out.println(getCookiesString());
		return false;

	}

	/**
	 * 加密密码
	 * 
	 * 
	 * */
	public String ssorskPassword(String pubkey, String serverTime,
			String nonce, String password) {
		return PasswordUtil4Sina.getPassEncoding(pubkey, serverTime, nonce,
				password);
	}

	/**
	 * base64 ecode 用户名
	 * 
	 * @author
	 * */
	public String encodeAccount(String account) {
		String userName = "";
		try {
			userName = Base64.encodeBase64String(URLEncoder.encode(account,
					"UTF-8").getBytes());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return userName;
	}

	public String post(CloseableHttpClient httpClient, String url,
			Map<String, String> params, String charset, String cookie) {
		HttpClientContext context = HttpClientContext.create();
		context.setCookieStore(cookieStore);
		String useCharset = charset;
		if (charset == null) {
			useCharset = "utf-8";
		}
		String result = null;
		try {
			HttpPost httpPost = new HttpPost(url);
			fillHeader(url, httpPost, cookie);
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			if (params != null) {
				for (String key : params.keySet()) {
					nvps.add(new BasicNameValuePair(key, params.get(key)));
				}
				httpPost.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
			}
			httpPost.setConfig(requestConfig);
			CloseableHttpResponse response = httpClient.execute(httpPost,
					context);
			try {
				HttpEntity entity = response.getEntity();
				result = EntityUtils.toString(entity, useCharset);
			} finally {
				response.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public String getHtml(CloseableHttpClient httpClient, String url,
			String cookie) {
		HttpClientContext context = HttpClientContext.create();
		HttpGet httpGet = new HttpGet(url);
		fillHeaderWithCookie(url, httpGet, cookie);
		httpGet.setConfig(requestConfig);
		String chartset = null;
		String result = null;
		try {
			CloseableHttpResponse response = httpClient.execute(httpGet,
					context);
			try {
				Header heads[] = response.getAllHeaders();
				for (Header header : heads) {
					if (header.getValue().toLowerCase().contains("charset")) {
						Pattern pattern = Pattern
								.compile("charset=[^\\w]?([-\\w]+)");
						Matcher matcher = pattern.matcher(header.getValue());
						if (matcher.find()) {
							chartset = matcher.group(1);
						}
					}
				}
				if (chartset == null) {
					chartset = "utf-8";
				} else {
					if (chartset.equals("gbk2312")) {
						chartset = "gbk";
					}
				}
				InputStream inputStream = response.getEntity().getContent();
				result = inputStream2String(inputStream, chartset);
			} finally {
				response.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public String getCookiesString() {
		List<Cookie> cookies = cookieStore.getCookies();
		StringBuffer sb = new StringBuffer();
		if (cookies != null) {
			for (Cookie c : cookies) {
				sb.append(c.getName() + "=" + c.getValue() + ";");
			}
		}
		return sb.toString();
	}

	public void fillHeaderWithCookie(String url, HttpGet httpGet, String cookie) {
		httpGet.setHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:41.0) Gecko/20100101 Firefox/41.0");
		httpGet.setHeader("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		httpGet.setHeader("Accept-Language",
				"zh-CN,zh;q=0.8,en-us;q=0.8,en;q=0.6");
		httpGet.setHeader("Accept-Encoding", "gzip, deflate,sdch");
		httpGet.setHeader("Connection", "keep-alive");
		httpGet.setHeader("Cache-Control", "max-age=0");
		httpGet.setHeader("Cookie", cookie);
	}

	private void fillHeader(String url, HttpPost httpPost, String cookie) {
		httpPost.setHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:41.0) Gecko/20100101 Firefox/41.0");
		httpPost.setHeader("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		httpPost.setHeader("Accept-Language",
				"zh-CN,zh;q=0.8,en-us;q=0.8,en;q=0.6");
		httpPost.setHeader("Accept-Encoding", "gzip, deflate,sdch");
		httpPost.setHeader("Connection", "keep-alive");
		httpPost.setHeader("Cache-Control", "max-age=0");
		httpPost.setHeader("Cookie", cookie);

	}

	public String inputStream2String(InputStream is, String charset) {
		String temp = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			int i = -1;
			while ((i = is.read()) != -1) {
				baos.write(i);
			}
			temp = baos.toString(charset);
			if (temp.contains("???") || temp.contains("�")) {
				Pattern pattern = Pattern
						.compile("<meta[\\s\\S]*?charset=\"{0,1}(\\S+?)\"\\s{0,10}/{0,1}>");
				// .compile("<meta\\s+http-equiv=\"content-type\"\\s+content=\"[\\s\\S]*?charset=(\\S+?)\"\\s+/>");
				Matcher matcher = pattern.matcher(temp.toLowerCase());
				if (matcher.find()) {
					charset = matcher.group(1);
				} else {
					charset = "gbk";
				}
				temp = baos.toString(charset);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				baos.close();
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return temp;

	}

}
