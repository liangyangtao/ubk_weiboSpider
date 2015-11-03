package com.unbank.weibo.login;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WeiboLoginByWebDriver {
	private static Log logger = LogFactory.getLog(WeiboLoginByWebDriver.class);

	public void console(int i) {
		System.setProperty("webdriver.chrome.driver",
				"phantomjs-1.9.7-windows/chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		// 登陆成功以后 ，去取博主发表的微博
		boolean isLogin = false;
		// 如果没有登陆成功，一直登陆
		while (isLogin == false) {
			try {
				isLogin = loginSina("674613438@qq.com", "", driver);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}

		System.out.println(driver.getPageSource());
		driver.quit();

	}

	// 登录新浪微博
	public boolean loginSina(String username, String password, WebDriver driver) {
		driver.get("http://weibo.com/login.php");
		waitForPageLoaded(driver);
		WebElement userElement = driver.findElement(By
				.cssSelector(" div.username > div > input"));
		WebElement passwordElement = driver.findElement(By
				.cssSelector(" div.password > div > input"));
		WebElement buttonElement = driver.findElement(By
				.cssSelector("a.W_btn_a.btn_40px"));
		userElement.sendKeys(username);
		passwordElement.sendKeys(password);
		buttonElement.click();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (driver.getCurrentUrl().contains("weibo.com/signup/signup.php")
				|| driver.getPageSource().contains("下次自动登录")) {
			logger.info("登陆失败");
			return false;
		}
		return true;
	}

	public static void waitForPageLoaded(WebDriver driver) {
		ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				return ((JavascriptExecutor) driver).executeScript(
						"return document.readyState").equals("complete");
			}
		};
		Wait<WebDriver> wait = new WebDriverWait(driver, 5000);
		try {
			wait.until(expectation);
		} catch (Throwable error) {
			logger.info(error);
		}
	}

}
