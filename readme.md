##新浪微博模拟登陆DEMO

详细步骤查看src/main/java/com/unbank/weibo/login/WeiboLoginByHttpClinet.java即可

###第一步：
访问 http://weibo.com/login.php  使得Cookie 里包含login_sid_t ，TC_Ugrow_G0
###第二步：
http://login.sina.com.cn/sso/prelogin.php?entry=weibo&callback=sinaSSOController.preloginCallBack&su=Njc0NjEzNDM4JTQwcXEuY29t&rsakt=mod&checkpin=1&client=ssologin.js(v1.4.18)&_=

获取servertime ，pcid，pubkey，rsakv，nonce
###第三步：
 1 用户名需要 base 64 ecode
 2 密码需要加密
###第四步:
   提交参数进行登陆
   获取 校验 url
###第5步
   访问URL 操作后如果看到自己的uid 说明登陆成功，就可以采集了