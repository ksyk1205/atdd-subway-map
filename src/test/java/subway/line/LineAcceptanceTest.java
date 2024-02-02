package subway.line;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import org.springframework.test.annotation.DirtiesContext;
import subway.fixture.LineFixture;
import subway.fixture.StationFixture;
import subway.util.RestAssuredUtil;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("지하철역 노선 기능")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class LineAcceptanceTest {

    private static ExtractableResponse<Response> 신림역;
    private static ExtractableResponse<Response> 보라매역;
    private static ExtractableResponse<Response> 판교역;
    private static ExtractableResponse<Response> 청계산입구역;

    private static String 신림선 = "신림선";
    private static String BLUE = "BLUE";
    private static String 신분당선 = "신분당선";
    private static String RED = "RED";

    @BeforeEach
    void before() {
        신림역 = RestAssuredUtil.sendPost(
                StationFixture.createStationParams("신림역"),
                "/stations");
        보라매역 = RestAssuredUtil.sendPost(
                StationFixture.createStationParams("보라매역"),
                "/stations");
        판교역 = RestAssuredUtil.sendPost(
                StationFixture.createStationParams("판교역"),
                "/stations");
        청계산입구역 = RestAssuredUtil.sendPost(
                StationFixture.createStationParams("청계산입구역"),
                "/stations");
    }

    /**
     * When 지하철 노선을 생성하면
     * Then 지하철 노선 목록 조회 시 생성한 노선을 찾을 수 있다
     */
    @DisplayName("지하철 노선을 생성한다.")
    @Test
    void createLine() {
        //when
        RestAssuredUtil.sendPost(
                LineFixture.createLineParams(신림선, BLUE, 신림역.jsonPath().getLong("id"), 보라매역.jsonPath().getLong("id"), 20L), "/lines");

        //then
        ExtractableResponse<Response> findResponse = RestAssuredUtil.sendGet("/lines");

        assertThat(findResponse.jsonPath().getString("name")).containsAnyOf(신림선);
        assertThat(findResponse.jsonPath().getString("color")).containsAnyOf(BLUE);
    }

    /**
     * Given 2개의 지하철 노선을 생성하고
     * When 지하철 노선 목록을 조회하면
     * Then 지하철 노선 목록 조회 시 2개의 노선을 조회할 수 있다.
     */
    @DisplayName("지하철 노선 목록을 조회한다.")
    @Test
    void findAllLine() {
        //given
        RestAssuredUtil.sendPost(
                LineFixture.createLineParams(신림선, BLUE, 신림역.jsonPath().getLong("id"), 보라매역.jsonPath().getLong("id"), 20L), "/lines");

        RestAssuredUtil.sendPost(
                LineFixture.createLineParams(신분당선, RED, 판교역.jsonPath().getLong("id"), 청계산입구역.jsonPath().getLong("id"), 20L), "/lines");

        //when
        ExtractableResponse<Response> lineList = RestAssuredUtil.sendGet("/lines");

        //then
        assertThat(lineList.jsonPath().getString("name")).containsAnyOf(신림선, 신분당선);
        assertThat(lineList.jsonPath().getString("color")).containsAnyOf(BLUE, RED);
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 조회하면
     * Then 생성한 지하철 노선의 정보를 응답받을 수 있다.
     */
    @DisplayName("지하철 노선을 조회한다.")
    @Test
    void findLine() {
        //given
        ExtractableResponse<Response> createResponse = RestAssuredUtil.sendPost(
                LineFixture.createLineParams(신림선, BLUE, 신림역.jsonPath().getLong("id"), 보라매역.jsonPath().getLong("id"), 20L), "/lines");

        //when
        ExtractableResponse<Response> findResponse = RestAssuredUtil.sendGet("/lines/" + createResponse.jsonPath().getLong("id"));

        //then
        assertThat(findResponse.jsonPath().getString("color")).isEqualTo(BLUE);
        assertThat(findResponse.jsonPath().getString("name")).isEqualTo(신림선);
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 수정하면
     * Then 해당 지하철 노선 정보는 수정된다
     */
    @DisplayName("지하철 노선을 수정한다.")
    @Test
    void updateLine() {
        //given
        ExtractableResponse<Response> createResponse = RestAssuredUtil.sendPost(
                LineFixture.createLineParams(신림선, BLUE, 신림역.jsonPath().getLong("id"), 보라매역.jsonPath().getLong("id"), 20L), "/lines");

        //when
        String 경강선 = "경강선";
        String YELLOW = "YELLOW";
        RestAssuredUtil.sendPut(LineFixture.updateLineParams(경강선, YELLOW), "/lines/" + createResponse.jsonPath().getLong("id"));

        //then
        ExtractableResponse<Response> findResponse = RestAssuredUtil.sendGet("/lines/" + createResponse.jsonPath().getLong("id"));
        assertThat(findResponse.jsonPath().getString("name")).isEqualTo(경강선);
        assertThat(findResponse.jsonPath().getString("color")).isEqualTo(YELLOW);
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 삭제하면
     * Then 해당 지하철 노선 정보는 삭제된다
     */
    @DisplayName("지하철 노선을 삭제한다.")
    @Test
    void deleteLine() {
        //given
        ExtractableResponse<Response> createResponse = RestAssuredUtil.sendPost(
                LineFixture.createLineParams(신림선, BLUE, 신림역.jsonPath().getLong("id"), 보라매역.jsonPath().getLong("id"), 20L), "/lines");

        //when
        ExtractableResponse<Response> deleteResponse = RestAssuredUtil.sendDelete("/lines/" + createResponse.jsonPath().getLong("id"));

        //then
        assertThat(deleteResponse.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());

    }
}