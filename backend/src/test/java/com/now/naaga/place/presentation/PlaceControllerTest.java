package com.now.naaga.place.presentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.now.naaga.auth.domain.AuthToken;
import com.now.naaga.auth.infrastructure.AuthType;
import com.now.naaga.auth.infrastructure.jwt.AuthTokenGenerator;
import com.now.naaga.common.CommonControllerTest;
import com.now.naaga.common.builder.PlaceBuilder;
import com.now.naaga.common.builder.PlayerBuilder;
import com.now.naaga.common.exception.ExceptionResponse;
import com.now.naaga.common.infrastructure.FileManager;
import com.now.naaga.place.domain.Place;
import com.now.naaga.place.exception.PlaceExceptionType;
import com.now.naaga.place.presentation.dto.PlaceResponse;
import com.now.naaga.player.domain.Player;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.now.naaga.common.fixture.PlaceFixture.PLACE;
import static com.now.naaga.common.fixture.PositionFixture.서울_좌표;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(ReplaceUnderscores.class)
public class PlaceControllerTest extends CommonControllerTest {

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private PlayerBuilder playerBuilder;

    @Autowired
    private PlaceBuilder placeBuilder;

    @MockBean
    private FileManager<MultipartFile> fileManager;

    @BeforeEach
    protected void setUp() {
        super.setUp();
    }

    @Test
    void 장소_추가_요청을_받으면_추가된_장소_정보를_응답한다() throws FileNotFoundException {
        //given
        final Player player = playerBuilder.init()
                                           .build();

        final Place place = PLACE();

        when(fileManager.save(any())).thenReturn(new File("/임시경로", "이미지.png"));

        final AuthToken generate = authTokenGenerator.generate(player.getMember(), 1L, AuthType.KAKAO);
        final String accessToken = generate.getAccessToken();

        //when
        final ExtractableResponse<Response> extract = given()
                .log().all()
                .multiPart(new MultiPartSpecBuilder(place.getName()).controlName("name").charset(StandardCharsets.UTF_8).build())
                .multiPart(new MultiPartSpecBuilder(place.getDescription()).controlName("description").charset(StandardCharsets.UTF_8).build())
                .multiPart(
                        new MultiPartSpecBuilder(place.getPosition().getLatitude().setScale(6, RoundingMode.HALF_DOWN).doubleValue()).controlName(
                                "latitude").charset(StandardCharsets.UTF_8).build())
                .multiPart(
                        new MultiPartSpecBuilder(place.getPosition().getLongitude().setScale(6, RoundingMode.HALF_DOWN).doubleValue()).controlName(
                                "longitude").charset(StandardCharsets.UTF_8).build())
                .multiPart(new MultiPartSpecBuilder(new FileInputStream(new File("src/test/java/com/now/naaga/place/fixture/루터회관.png"))).controlName(
                                                                                                                                                "imageFile").charset(StandardCharsets.UTF_8)
                                                                                                                                        .fileName(
                                                                                                                                                "src/test/java/com/now/naaga/place/fixture/루터회관.png")
                                                                                                                                        .mimeType(
                                                                                                                                                "image/png")
                                                                                                                                        .build())
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .post("/places")
                .then()
                .log().all()
                .extract();
        final int statusCode = extract.statusCode();
        final String location = extract.header("Location");
        final PlaceResponse actual = extract.as(PlaceResponse.class);
        final PlaceResponse expected = PlaceResponse.from(place);

        //then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(statusCode).isEqualTo(HttpStatus.CREATED.value());
            softAssertions.assertThat(location).isEqualTo("/places/" + actual.id());
            softAssertions.assertThat(expected)
                          .usingRecursiveComparison()
                          .ignoringFields("id", "imageUrl")
                          .isEqualTo(actual);
        });

    }

    @Test
    void 장소_추가_요청_시_주변_20미터_내에_이미_등록된_목적지가_있다면_예외를_응답한다() throws FileNotFoundException {
        //given
        final Player player = playerBuilder.init()
                                           .build();

        placeBuilder.init()
                    .registeredPlayer(player)
                    .position(서울_좌표)
                    .build();

        final Place seoulPlace = PLACE();

        final AuthToken generate = authTokenGenerator.generate(player.getMember(), 1L, AuthType.KAKAO);
        final String accessToken = generate.getAccessToken();

        //when
        final ExtractableResponse<Response> extract = given()
                .log().all()
                .multiPart(new MultiPartSpecBuilder(seoulPlace.getName()).controlName("name").charset(StandardCharsets.UTF_8).build())
                .multiPart(new MultiPartSpecBuilder(seoulPlace.getDescription()).controlName("description").charset(StandardCharsets.UTF_8).build())
                .multiPart(
                        new MultiPartSpecBuilder(seoulPlace.getPosition().getLatitude().setScale(6, RoundingMode.HALF_DOWN).doubleValue()).controlName("latitude").charset(StandardCharsets.UTF_8)
                                                                                                                                          .build())
                .multiPart(
                        new MultiPartSpecBuilder(seoulPlace.getPosition().getLongitude().setScale(6, RoundingMode.HALF_DOWN).doubleValue()).controlName("longitude").charset(StandardCharsets.UTF_8)
                                                                                                                                           .build())
                .multiPart(new MultiPartSpecBuilder(new FileInputStream(new File("src/test/java/com/now/naaga/place/fixture/루터회관.png"))).controlName("imageFile").charset(StandardCharsets.UTF_8)
                                                                                                                                        .fileName("src/test/java/com/now/naaga/place/fixture/루터회관.png")
                                                                                                                                        .mimeType("image/png").build())
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .post("/places")
                .then()
                .log().all()
                .extract();
        final int statusCode = extract.statusCode();
        final ExceptionResponse actual = extract.as(ExceptionResponse.class);
        PlaceExceptionType exceptionType = PlaceExceptionType.ALREADY_EXIST_NEARBY;
        ExceptionResponse expected = new ExceptionResponse(exceptionType.errorCode(), exceptionType.errorMessage());

        //then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(statusCode).isEqualTo(HttpStatus.BAD_REQUEST.value());
            softAssertions.assertThat(expected)
                          .usingRecursiveComparison()
                          .isEqualTo(actual);
        });
    }

    @Test
    void 장소를_아이디로_조회한다() {
        //given
        final Place place = placeBuilder.init()
                                        .build();

        final AuthToken generate = authTokenGenerator.generate(place.getRegisteredPlayer().getMember(), 1L, AuthType.KAKAO);
        final String accessToken = generate.getAccessToken();
        //when
        final ExtractableResponse<Response> extract = given()
                .log().all()
                .pathParam("placeId", place.getId())
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/places/{placeId}")
                .then()
                .log().all()
                .extract();
        final int statusCode = extract.statusCode();
        final PlaceResponse actual = extract.as(PlaceResponse.class);
        final PlaceResponse expected = PlaceResponse.from(place);
        //then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(statusCode).isEqualTo(HttpStatus.OK.value());
            softAssertions.assertThat(expected)
                          .usingRecursiveComparison()
                          .isEqualTo(actual);
        });
    }

    @Test
    void 회원이_등록한_장소를_조회한다() throws JsonProcessingException {
        //given
        final Place place = placeBuilder.init()
                                        .build();

        final AuthToken generate = authTokenGenerator.generate(place.getRegisteredPlayer().getMember(), 1L, AuthType.KAKAO);
        final String accessToken = generate.getAccessToken();

        //when
        final ExtractableResponse<Response> extract = given()
                .log().all()
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("sort-by", "time")
                .queryParam("order", "descending")
                .when()
                .get("/places")
                .then()
                .log().all()
                .extract();
        final int statusCode = extract.statusCode();
        final String jsonResponse = extract.body().asString();
        final ObjectMapper objectMapper = new ObjectMapper();
        final List<PlaceResponse> actual = objectMapper.readValue(jsonResponse, new TypeReference<List<PlaceResponse>>() {
        });
        final List<PlaceResponse> expected = PlaceResponse.convertToPlaceResponses(List.of(place));
        //then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(statusCode).isEqualTo(HttpStatus.OK.value());
            softAssertions.assertThat(expected)
                          .usingRecursiveComparison()
                          .isEqualTo(actual);
        });
    }
}
