package ru.urfu.sv.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Duration;
import java.util.Optional;

@Service
@Slf4j
public class WebDriverService {
    @Value("${urfu.user.username}")
    private String urfuUserName;
    @Value("${urfu.user.password}")
    private String urfuUserPassword;
    @Value("${urfu.auth.url}")
    private String urfuAuthUrl;
    @Value("${modeus.url}")
    private String modeusUrl;
    private final ChromeOptions webDriverOptions;


    public WebDriverService(@Value("${path.web.driver}") String pathToDriver) {
        webDriverOptions = new ChromeOptions();
        webDriverOptions.addArguments("--headless=new");
        System.setProperty("webdriver.chrome.driver", pathToDriver);
    }

    public Optional<String> getModeusAuthToken() {
        ChromeDriver webDriver = new ChromeDriver(webDriverOptions);
        webDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(20)).implicitlyWait(Duration.ofSeconds(10));
        webDriver.get(urfuAuthUrl);
        log.info("Страница логина УрФУ загрузилась");

        webDriver.findElement(By.id("userNameInput")).sendKeys(urfuUserName);
        webDriver.findElement(By.id("passwordInput")).sendKeys(urfuUserPassword);
        webDriver.findElement(By.id("submitButton")).click();

        WebDriverWait waitForAuth = new WebDriverWait(webDriver, Duration.ofSeconds(20));
        waitForAuth.until(driver -> driver.getCurrentUrl().contains("auth-ok"));
        if (webDriver.getCurrentUrl().contains("auth-ok")) {
            log.info("Логирование в УрФУ прошло успешно");
        } else {
            log.error("Логирование в УрФУ не прошло, прерываем процесс");
            return Optional.empty();
        }

        webDriver.get(modeusUrl);
        log.info("Страница Модеус загрузилась");

        WebDriverWait waitForToken = new WebDriverWait(webDriver, Duration.ofSeconds(20));
        waitForToken.until(driver -> ((ChromeDriver) driver).getSessionStorage().getItem("id_token") != null);
        String token = webDriver.getSessionStorage().getItem("id_token");
        log.info(token == null ? "Не получилось получить токен аутентификации Модеус" : "Получили токен аутентификации Модеус");

        webDriver.manage().deleteAllCookies();
        webDriver.quit();
        log.info("Веб драйвер успешно закрыт");
        return token == null ? Optional.empty() : Optional.of(token);
    }
}
