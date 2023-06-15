package com.news.producer.component;

import com.news.producer.config.RabbitConfig;
import com.news.producer.model.Post;
import lombok.AllArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

@AllArgsConstructor
@EnableScheduling
@Component
public class NewsProducer {

  private static final Logger LOGGER = Logger.getLogger(NewsProducer.class.getName());

  private final RabbitConfig rabbitConfig;
  private final RabbitTemplate rabbitTemplate;

  @Scheduled(fixedRateString = "${scheduler.interval}")
  public void fetchAndSendNews() {
    WebDriver driver = new FirefoxDriver();
    try {
      driver.get("https://www.reuters.com/world/europe/");
      List<WebElement> elements = driver.findElements(By.className("story-collection__default__G33_I"));
      for (WebElement el : elements) {
        String title = getElementText(el, By.className("media-story-card__heading__eqhp9"));
        String date = getElementText(el, By.className("media-story-card__section__SyzYF"));
        String summary = getElementText(el, By.className("body__body__VgU9Q"))!=null ? getElementText(el, By.className("body__body__VgU9Q")) : getElementHref(el, By.className("media-story-card__heading__eqhp9"));
        String location = getElementText(el, By.className("link__underline_on_hover__2zGL4"));
        String imageSrc = getElementSrc(el, By.className("styles__image-container__skIG1"));
        Post post = new Post(title!=null?title:"TBA", summary, parseDate(date), location, imageSrc);
        LOGGER.info("Sending: " + post);
        rabbitTemplate.convertAndSend(rabbitConfig.getTopicExchangeName(), rabbitConfig.getRoutingKey(), post);
      }
    } finally {
      driver.close();
    }
  }

  private String getElementText(WebElement parent, By by) {
    WebElement element = getElementSafe(parent, by);
    if (element != null) {
      return element.getText();
    }
    return null;
  }

  private String getElementSrc(WebElement parent, By by) {
    WebElement element = getElementSafe(parent, by);
    if (element != null) {
      return element.getAttribute("srcset");
    }
    return null;
  }

  private String getElementHref(WebElement parent, By by) {
    WebElement element = getElementSafe(parent, by);
    if (element != null) {
      return element.getAttribute("href");
    }
    return null;
  }

  private WebElement getElementSafe(WebElement parent, By by) {
    try {
      return parent.findElement(by);
    } catch (NoSuchElementException ignored) {
      return null;
    }
  }

  private LocalDate parseDate(String date) {
    LocalDate datePart = LocalDate.now();
    if (date == null || date.trim().isEmpty()){
      return datePart;
    }
    String[] dateFormatted = date.split("·");

    if (dateFormatted.length<3) {
      return datePart;
    }
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    // Parse the date string using the formatter
    LocalDate dateTime = LocalDate.parse(dateFormatted[1].trim(), formatter);

    return dateTime;
  }
}
