package com.ssafy.db.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

/**
 * 컨퍼런스 모델 정의.
 */
@Entity
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Conference extends BaseEntity{
    @ManyToOne(fetch = FetchType.LAZY)
    private ConferenceCategory conferenceCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    String thumbnail;
    String title;
    String description;
    Boolean is_active;
    Boolean is_free;

    String conference_day;

    Date conference_time;
    Integer price;

}
