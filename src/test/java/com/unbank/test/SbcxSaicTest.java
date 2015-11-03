package com.unbank.test;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JOptionPane;

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
import org.junit.Test;

import com.unbank.featch.HttpClientBuilder;

public class SbcxSaicTest {

	public static RequestConfig requestConfig = RequestConfig.custom()
			.setSocketTimeout(30000).setConnectTimeout(30000).build();
	public static BasicCookieStore cookieStore = new BasicCookieStore();

	public static CloseableHttpClient httpClient;
	public static void main(String[] args) {
		PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
		HttpClientBuilder httpClientBuilder = new HttpClientBuilder(false,
				poolingHttpClientConnectionManager, cookieStore);
		httpClient = httpClientBuilder.getHttpClient();
//		new SbcxSaicTest().test();
		new SbcxSaicTest()
		.image();

	}

	private void image() {
		// TODO Auto-generated method stub
		String code3Url = "http://ecx.images-amazon.com/images/I/41Gq3P4SnHL.jpg";
		System.out.println(code3Url);
		InputStream inputStream = getImage(httpClient, code3Url,
				getCookiesString());
		fetchImage(inputStream);
	}
	
    @Test
	private void test() {
		// TODO Auto-generated method stub
		String codeUrl = "http://sbcx.saic.gov.cn:9080/tmois/tmois/tmzhcx/input_code.jsp";
		String html = getHtml(httpClient, codeUrl, getCookiesString());
		String code2Url = "http://sbcx.saic.gov.cn:9080/tmois/wszhcx_getCodeImage.xhtml";
		html = getHtml(httpClient, code2Url, getCookiesString());
		Long dateTime = new Date().getTime();
		String code3Url = "http://sbcx.saic.gov.cn:9080/tmois/wszhcx_getCodeImage.xhtml?d="
				+ dateTime;
		System.out.println(code3Url);
		// 获取验证码图片
		InputStream inputStream = getImage(httpClient, code3Url,
				getCookiesString());
		// 保存xialai
		fetchImage(inputStream);
		// 
		String code = null;
		try {
			// BufferedImage image = ImageIO.read(inputStream);
			code = JOptionPane.showInputDialog("daim");
		} catch (Exception e) {
			e.printStackTrace();
		}
		String url = "http://sbcx.saic.gov.cn:9080/tmois/wszhcx_getImage.xhtml?inputCode="
				+ code;
		html = post(httpClient, url, null, "utf-8", getCookiesString());

		String detailurl = "http://sbcx.saic.gov.cn:9080/tmois/wszhcx_getDetailByRegInt.xhtml?regNum=10110&intcls=11&seriaNum=1&codeShow="
				+ code;
		html = getHtml(httpClient, detailurl, getCookiesString());
		System.out.println(html);
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

	public InputStream getImage(CloseableHttpClient httpClient, String url,
			String cookie) {
		HttpClientContext context = HttpClientContext.create();
		HttpGet httpGet = new HttpGet(url);
		fillHeaderWithCookie(url, httpGet, cookie);
		httpGet.setConfig(requestConfig);
		InputStream inputStream = null;
		try {
			CloseableHttpResponse response = httpClient.execute(httpGet,
					context);
			try {
				HttpEntity entity = response.getEntity();
				inputStream = new ByteArrayInputStream(
						EntityUtils.toByteArray(entity));

			} finally {
				response.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return inputStream;
	}

	public void fetchImage(InputStream is) {
		String imageName = "C:\\Users\\Administrator\\Desktop\\aaa.jpg";
		BufferedInputStream bis = null;
		ImageInputStream iis = null;
		try {

			bis = new BufferedInputStream(is);
			iis = ImageIO.createImageInputStream(is);
			Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
			if (iter.hasNext()) {
				ImageReader reader = iter.next();
				reader.setInput(iis);
				ImageReadParam irp = reader.getDefaultReadParam();
				BufferedImage bi = reader.read(0, irp);
				ImageIO.write(bi, "jpg", new File(imageName));
			}
		} catch (Exception e) {
			// logger.info("保存图片失败", e);
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// logger.info("关闭输入流失败", e);
				}
			}

			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
				}
			}

			if (iis != null) {
				try {
					iis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
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
		httpGet.setHeader(
				"User-Agent",
				"Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.154 Safari/537.36");
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
		httpPost.setHeader(
				"User-Agent",
				"Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.154 Safari/537.36");
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
