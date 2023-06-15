package com.news.producer.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Post(
  String title
  , String summary
  , @JsonSerialize(using = LocalDateSerializer.class) LocalDate date
  , String location
  , String imageSrc
) {
}
