package com.ssafy.api.response;

import com.ssafy.common.model.response.BaseResponseBody;
import com.ssafy.db.entity.Conference;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 방 생성 API ([POST] /api/v1/auth) 요청에 대한 응답값 정의.
 */
@Getter
@Setter
@ApiModel("ConferenceCreatePostRes")
public class ConferenceCreatePostRes extends BaseResponseBody{
	@ApiModelProperty(name="방 생성")
	long conferenceId;
	String url;

	public static ConferenceCreatePostRes of(Integer statusCode, String message, Conference conference) {
		ConferenceCreatePostRes res = new ConferenceCreatePostRes();
		res.setStatusCode(statusCode);
		res.setMessage(message);
		res.setConferenceId(conference.getId());
		res.setUrl(conference.getThumbnail());
		return res;
	}
}