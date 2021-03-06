package com.ssafy.api.controller;

import com.ssafy.api.request.ConferenceCategoryPostReq;
import com.ssafy.api.response.*;
import com.ssafy.api.service.ConferenceService;
import com.ssafy.common.auth.SsafyUserDetails;
import com.ssafy.common.model.response.BaseResponseBody;
import com.ssafy.db.entity.Conference;
import com.ssafy.db.entity.ConferenceCategory;
import com.ssafy.db.entity.UserConference;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;
import org.springframework.security.core.Authentication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 방 관련 API 요청 처리를 위한 컨트롤러 정의.
 */
@Api(value = "방 API", tags = {"Conference"})
@Controller
@RequestMapping("/api/v1/")
public class ConferenceController {
    @Autowired
    ConferenceService conferenceService;

    @PostMapping("conferences")
    @ApiOperation(value = "방 생성", notes = "방을 생성 한다.")
    @ApiResponses({
            @ApiResponse(code = 201, message = "성공"),
    })
    public ResponseEntity<ConferenceCreatePostRes> createConference(
            @RequestParam("description") String description, @RequestParam("title") String title, @RequestParam("conferenceCategoryId") Long conferenceCategoryId, @RequestParam("thumbnail") MultipartFile thumbnail,
            @RequestParam(required = false) String conferenceDay, @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date conferenceTime, @RequestParam(required = false) Integer price,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date applyEndTime, @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date applyStartTime,
            @ApiIgnore Authentication authentication
    ) throws IOException {
        SsafyUserDetails userDetails = (SsafyUserDetails) authentication.getDetails();
        String userId = userDetails.getUsername();

        Conference conference = conferenceService.createConference(userId, description, title, conferenceCategoryId, saveThumbnail(thumbnail), conferenceDay, conferenceTime, applyEndTime, applyStartTime, price);    // createInfo,
        joinConference(authentication, conference.getId());
        return ResponseEntity.status(201).body(ConferenceCreatePostRes.of(201, "success.", conference));
    }

    @PostMapping("conferences/join")
    @ApiOperation(value = "방 참가", notes = "방에 참가다.")
    @ApiResponses({
            @ApiResponse(code = 201, message = "성공"),
    })
    public ResponseEntity<? extends BaseResponseBody> joinConference(
            @ApiIgnore Authentication authentication, @RequestParam("conferenceId") Long conferenceId) {
        SsafyUserDetails userDetails = (SsafyUserDetails) authentication.getDetails();
        String userId = userDetails.getUsername();

        conferenceService.joinConference(userId, conferenceId);
        return ResponseEntity.status(201).body(BaseResponseBody.of(201, "success."));
    }

    @GetMapping(value = "conferences/{conference_id}")
    @ApiOperation(value = "방 상세정보 조회", notes = "방 ID를 가지고 상세 정보를 조회한다")
    @ApiResponses({
            @ApiResponse(code = 200, message = "성공"),
    })
    public ResponseEntity<ConferenceDetailRes> getConferenceDetail(
            @PathVariable Long conference_id) {
        Conference conference = conferenceService.getConferenceByConferenceId(conference_id);
        Optional<List<UserConference>> userConference = conferenceService.getUserConferenceByConferenceId(conference_id);
        return ResponseEntity.status(200).body(ConferenceDetailRes.of(conference, userConference));
    }

    @PatchMapping(value = "conferences/{conference_id}")
    @ApiOperation(value = "방 정보 수정", notes = "방 ID를 가지고 정보를 수정한다")
    @ApiResponses({
            @ApiResponse(code = 201, message = "성공"),
    })
    public ResponseEntity<? extends BaseResponseBody> patchConferenceInfo(
            @PathVariable Long conference_id, @RequestParam String description, @RequestParam String title, @RequestParam Long conferenceCategoryId,
            @RequestParam MultipartFile thumbnail, @RequestParam String conferenceDay, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date conferenceTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date applyEndTime, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date applyStartTime,
            @RequestParam Boolean isActive, @RequestParam Integer price) throws IOException {
        conferenceService.patchConferenceInfo(description, title, conferenceCategoryId, saveThumbnail(thumbnail), conferenceDay, conferenceTime, applyEndTime, applyStartTime, isActive, price, conference_id);
        return ResponseEntity.status(201).body(BaseResponseBody.of(201, "Success"));
    }

    @PatchMapping(value = "conferences/onBoarding")
    @ApiOperation(value = "방송 상태 변경", notes = "방송상태를 변경한다")
    @ApiResponses({
            @ApiResponse(code = 201, message = "성공"),
    })
    public ResponseEntity<ConferenceOnboardStatusRes> changeOnboardStates(
            @RequestParam Long conferenceId) {

        Conference conference = conferenceService.changeOnboardStates(conferenceId);
        return ResponseEntity.status(201).body(ConferenceOnboardStatusRes.of(201, "Success",conference));
    }

    @GetMapping(value = "conferences")
    @ApiOperation(value = "방 목록 조회", notes = "방 목록 리스트를 검색한다")
    @ApiResponses({
            @ApiResponse(code = 200, message = "성공"),
    })
    public ResponseEntity<ConferenceListPostRes> getConferenceList(
            @RequestParam(required = false) String title, @RequestParam(required = false) @ApiParam(value = "call_start_time,asc") String sort, @RequestParam(required = false) String userName,
            @RequestParam(required = false) Integer size, @RequestParam(required = false) String conferenceCategory) {
        Optional<List<Conference>> conferences = conferenceService.getConferences(title, sort, size, conferenceCategory, userName);
        return ResponseEntity.status(200).body(ConferenceListPostRes.of(conferences));
    }

    @DeleteMapping(value = "conferences")
    @ApiOperation(value = "방 정보 삭제", notes = "방 정보를 삭제한다")
    @ApiResponses({
            @ApiResponse(code = 200, message = "성공"),
    })
    public ResponseEntity<? extends BaseResponseBody> deleteConference(
            @RequestParam(required = true) Long conference_id) {
        conferenceService.deleteConference(conference_id);
        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "Success"));
    }

    @PostMapping(value = "conference-categories")
    @ApiOperation(value = "방 카테고리 생성", notes = "방 카테고리를 생성한다")
    @ApiResponses({
            @ApiResponse(code = 201, message = "성공"),
            @ApiResponse(code = 409, message = "카테고리 중복"),
    })
    public ResponseEntity<? extends BaseResponseBody> createConferenceCategory(
            @RequestBody @ApiParam(value = "카테고리 정보", required = true) ConferenceCategoryPostReq categoryInfo) {
        Optional<ConferenceCategory> conferenceCategory = conferenceService.getConferenceCategoryByName(categoryInfo.getName());
        if (conferenceCategory.isPresent()) {
            return ResponseEntity.status(409).body(BaseResponseBody.of(409, "duplicate category"));
        } else {
            conferenceService.createConferenceCategory(categoryInfo);
            return ResponseEntity.status(201).body(BaseResponseBody.of(201, "Success"));
        }
    }

    @DeleteMapping(value = "conference-categories")
    @ApiOperation(value = "방 카테고리 삭제", notes = "방 카테고리를 삭제한다")
    @ApiResponses({
            @ApiResponse(code = 200, message = "성공"),
            @ApiResponse(code = 404, message = "사용자 없음"),
    })
    public ResponseEntity<? extends BaseResponseBody> deleteConferenceCategory(
            @RequestBody @ApiParam(value = "카테고리 정보", required = true) ConferenceCategoryPostReq categoryInfo) {
        Optional<ConferenceCategory> conferenceCategory = conferenceService.getConferenceCategoryByName(categoryInfo.getName());
        if (conferenceCategory.isPresent()) {
            conferenceService.deleteConferenceCategory(conferenceCategory.get().getId());
            return ResponseEntity.status(200).body(BaseResponseBody.of(200, "Success"));
        } else {
            return ResponseEntity.status(404).body(BaseResponseBody.of(404, "non-existent category"));
        }
    }

    @GetMapping("conference-categories")
    @ApiOperation(value = "방 카테고리 조회", notes = "방 카테고리들을 조회한다")
    @ApiResponses({
            @ApiResponse(code = 200, message = "성공"),
    })
    public ResponseEntity<ConferenceCategoryRes> getCategories() {
        Optional<List<ConferenceCategory>> categories = conferenceService.getCategories();
        return ResponseEntity.status(200).body(ConferenceCategoryRes.of(categories));
    }

    private String saveThumbnail(MultipartFile thumbnail) throws IOException {
        String path = "images/";
        File file = new File(path);
        if (!file.exists())
            file.mkdirs();

        String url = "";
        if (thumbnail != null) {
            String originalFileExtension = thumbnail.getOriginalFilename().substring(thumbnail.getOriginalFilename().lastIndexOf("."));
            String new_file_name = Long.toString(System.nanoTime()) + originalFileExtension;

            url = "images" + File.separator + new_file_name;
            Path pathabs = Paths.get(url).toAbsolutePath();
            thumbnail.transferTo(pathabs.toFile());
        }
        return url;
    }
}
