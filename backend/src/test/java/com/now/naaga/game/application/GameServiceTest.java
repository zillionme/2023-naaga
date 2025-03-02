package com.now.naaga.game.application;

import com.now.naaga.common.builder.GameBuilder;
import com.now.naaga.common.builder.GameResultBuilder;
import com.now.naaga.common.builder.PlaceBuilder;
import com.now.naaga.common.builder.PlayerBuilder;
import com.now.naaga.game.application.dto.CreateGameCommand;
import com.now.naaga.game.application.dto.EndGameCommand;
import com.now.naaga.game.application.dto.FindGameByIdCommand;
import com.now.naaga.game.application.dto.FindGameByStatusCommand;
import com.now.naaga.game.domain.Game;
import com.now.naaga.game.domain.GameRecord;
import com.now.naaga.game.domain.GameResult;
import com.now.naaga.game.domain.Statistic;
import com.now.naaga.game.exception.GameException;
import com.now.naaga.place.domain.Place;
import com.now.naaga.player.domain.Player;
import com.now.naaga.player.presentation.dto.PlayerRequest;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static com.now.naaga.common.fixture.PositionFixture.*;
import static com.now.naaga.game.domain.EndType.GIVE_UP;
import static com.now.naaga.game.domain.GameStatus.DONE;
import static com.now.naaga.game.domain.GameStatus.IN_PROGRESS;
import static com.now.naaga.game.domain.ResultType.FAIL;
import static com.now.naaga.game.domain.ResultType.SUCCESS;
import static com.now.naaga.game.exception.GameExceptionType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Sql("/truncate.sql")
@SpringBootTest
class GameServiceTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private GameBuilder gameBuilder;

    @Autowired
    private GameResultBuilder gameResultBuilder;

    @Autowired
    private PlayerBuilder playerBuilder;

    @Autowired
    private PlaceBuilder placeBuilder;

    @Test
    void 게임_id로_게임_결과를_조회한다() {
        // given
        final Player player = playerBuilder.init()
                .build();

        final Place destination = placeBuilder.init()
                .position(잠실_루터회관_정문_좌표)
                .build();

        final Game game = gameBuilder.init()
                .place(destination)
                .player(player)
                .startPosition(잠실역_교보문고_좌표)
                .build();

        final GameResult gameResult = gameResultBuilder.init()
                .resultType(SUCCESS)
                .game(game)
                .build();

        // when
        final Long actual = gameService.findGameResultByGameId(game.getId()).getId();

        // then
        assertThat(gameResult.getId()).isEqualTo(actual);
    }

    @Test
    void 게임_id로_게임_결과를_조회할때_존재하지_않으면_예외를_발생시킨다() {
        // given & when
        final Player player = playerBuilder.init()
                .build();

        final Place destination = placeBuilder.init()
                .position(잠실_루터회관_정문_좌표)
                .build();

        final Game game = gameBuilder.init()
                .place(destination)
                .player(player)
                .startPosition(잠실역_교보문고_좌표)
                .build();

        //then
        GameException gameException = assertThrows(GameException.class, () -> gameService.findGameResultByGameId(game.getId()));
        assertThat(gameException.exceptionType()).isEqualTo(GAME_RESULT_NOT_EXIST);
    }

    @Test
    void 플레이어의_모든_게임결과를_생성순서로_정렬하여_조회한다() {
        // given
        final Player player = playerBuilder.init()
                .build();

        final Place destination = placeBuilder.init()
                .position(잠실_루터회관_정문_좌표)
                .build();

        final Game game1 = gameBuilder.init()
                .place(destination)
                .player(player)
                .startTime(LocalDateTime.of(2023, Month.AUGUST, 13, 15, 30, 0))
                .endTime(LocalDateTime.of(2023, Month.AUGUST, 13, 17, 30, 0))
                .startPosition(잠실역_교보문고_좌표)
                .build();

        final Game game2 = gameBuilder.init()
                .place(destination)
                .player(player)
                .startTime(LocalDateTime.of(2023, Month.AUGUST, 12, 15, 30, 0))
                .endTime(LocalDateTime.of(2023, Month.AUGUST, 12, 17, 30, 0))
                .startPosition(잠실역_교보문고_좌표)
                .build();

        final GameResult gameResult1 = gameResultBuilder.init()
                .resultType(SUCCESS)
                .game(game1)
                .build();

        final GameResult gameResult2 = gameResultBuilder.init()
                .resultType(FAIL)
                .game(game2)
                .build();

        //when
        final List<GameRecord> expected = gameService.findAllGameResult(new PlayerRequest(player.getId()));

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(expected.get(0).getGameResult().getId()).isEqualTo(gameResult2.getId());
            softAssertions.assertThat(expected.get(1).getGameResult().getId()).isEqualTo(gameResult1.getId());
            softAssertions.assertThat(expected.size()).isEqualTo(2);
        });
    }

    @Test
    void 플레이어의_게임과_게임_결과가_없으면_빈리스트를_반환한다() {
        // given
        final Player player = playerBuilder.init()
                .build();

        final Place destination = placeBuilder.init()
                .position(잠실_루터회관_정문_좌표)
                .build();

        //when
        final List<GameRecord> expected = gameService.findAllGameResult(new PlayerRequest(player.getId()));

        // then
        assertThat(expected).isEmpty();
    }

    @Test
    void 플레이어의_통계를_조회한다() {
        // given
        final Player player = playerBuilder.init()
                .build();

        final Place destination = placeBuilder.init()
                .position(잠실_루터회관_정문_좌표)
                .build();

        final Game game1 = gameBuilder.init()
                .place(destination)
                .player(player)
                .gameStatus(DONE)
                .startTime(LocalDateTime.of(2023, Month.AUGUST, 13, 15, 30, 0))
                .endTime(LocalDateTime.of(2023, Month.AUGUST, 13, 17, 30, 0))
                .startPosition(잠실역_교보문고_좌표)
                .build();

        final Game game2 = gameBuilder.init()
                .place(destination)
                .player(player)
                .gameStatus(DONE)
                .startTime(LocalDateTime.of(2023, Month.AUGUST, 12, 15, 30, 0))
                .endTime(LocalDateTime.of(2023, Month.AUGUST, 12, 17, 30, 0))
                .startPosition(잠실역_교보문고_좌표)
                .build();

        final GameResult gameResult1 = gameResultBuilder.init()
                .resultType(SUCCESS)
                .game(game1)
                .build();

        final GameResult gameResult2 = gameResultBuilder.init()
                .resultType(FAIL)
                .game(game2)
                .build();

        // when
        final Statistic expected = gameService.findStatistic(new PlayerRequest(player.getId()));

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(expected.getSuccessGameCount()).isEqualTo(1);
            softAssertions.assertThat(expected.getFailGameCount()).isEqualTo(1);
            softAssertions.assertThat(expected.getGameCount()).isEqualTo(2);
        });
    }

    @Test
    void 플레이어_id와_게임_상태로_게임을_조회한다() {
        // given
        final Player player = playerBuilder.init()
                .build();

        final Place destination = placeBuilder.init()
                .position(잠실_루터회관_정문_좌표)
                .build();

        final Game game1 = gameBuilder.init()
                .place(destination)
                .player(player)
                .gameStatus(DONE)
                .startTime(LocalDateTime.of(2023, Month.AUGUST, 13, 15, 30, 0))
                .endTime(LocalDateTime.of(2023, Month.AUGUST, 13, 17, 30, 0))
                .startPosition(잠실역_교보문고_좌표)
                .build();

        final Game game2 = gameBuilder.init()
                .place(destination)
                .player(player)
                .gameStatus(DONE)
                .startTime(LocalDateTime.of(2023, Month.AUGUST, 12, 15, 30, 0))
                .endTime(LocalDateTime.of(2023, Month.AUGUST, 12, 17, 30, 0))
                .startPosition(잠실역_교보문고_좌표)
                .build();

        // when
        List<Game> expected = gameService.findGamesByStatus(new FindGameByStatusCommand(player.getId(), DONE));

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(expected.size()).isEqualTo(2);
            softAssertions.assertThat(expected.get(0).getId()).isEqualTo(game1.getId());
            softAssertions.assertThat(expected.get(1).getId()).isEqualTo(game2.getId());
        });
    }

    @Test
    void 게임id로_게임을_조회한다() {
        // given
        final Player player = playerBuilder.init()
                .build();

        final Place destination = placeBuilder.init()
                .position(잠실_루터회관_정문_좌표)
                .build();

        final Game game = gameBuilder.init()
                .place(destination)
                .player(player)
                .gameStatus(DONE)
                .startTime(LocalDateTime.of(2023, Month.AUGUST, 13, 15, 30, 0))
                .endTime(LocalDateTime.of(2023, Month.AUGUST, 13, 17, 30, 0))
                .startPosition(잠실역_교보문고_좌표)
                .build();

        // when
        Game expected = gameService.findGameById(new FindGameByIdCommand(game.getId(), player.getId()));

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(expected.getId()).isEqualTo(game.getId());
        });
    }

    @Test
    void 요청_플레이어의_id와_게임의_플레이어와_일치하지_않으면_예외가_발생한다() {
        // given
        final Player player1 = playerBuilder.init()
                .build();

        final Player player2 = playerBuilder.init()
                .nickname("코코닥")
                .build();

        final Place destination = placeBuilder.init()
                .position(잠실_루터회관_정문_좌표)
                .build();

        final Game game = gameBuilder.init()
                .place(destination)
                .player(player2)
                .gameStatus(DONE)
                .startTime(LocalDateTime.of(2023, Month.AUGUST, 13, 15, 30, 0))
                .endTime(LocalDateTime.of(2023, Month.AUGUST, 13, 17, 30, 0))
                .startPosition(잠실역_교보문고_좌표)
                .build();

        // when
        GameException gameException = assertThrows(GameException.class, () -> gameService.findGameById(new FindGameByIdCommand(game.getId(), player1.getId())));

        // then
        assertThat(gameException.exceptionType()).isEqualTo(INACCESSIBLE_AUTHENTICATION);
    }

    @Test
    void 종료요청이_들어오면_게임결과를_저장한다() {
        // given
        final Player player = playerBuilder.init()
                .build();

        final Place destination = placeBuilder.init()
                .position(잠실_루터회관_정문_좌표)
                .build();

        final Game game = gameBuilder.init()
                .place(destination)
                .player(player)
                .gameStatus(IN_PROGRESS)
                .startTime(LocalDateTime.of(2023, Month.AUGUST, 13, 15, 30, 0))
                .endTime(LocalDateTime.of(2023, Month.AUGUST, 13, 17, 30, 0))
                .startPosition(잠실역_교보문고_좌표)
                .build();

        // when
        gameService.endGame(new EndGameCommand(player.getId(), GIVE_UP, 잠실_루터회관_정문_좌표, game.getId()));

        // then
        final GameResult expected = gameService.findGameResultByGameId(game.getId());
        assertThat(expected.getGame().getId()).isEqualTo(game.getId());
    }

    @Test
    void 종료요청이_들어왔을때_GameNotArrivalException이_발생해도_롤백되지않고_게임결과를_저장한다() {
        // given
        final Player player = playerBuilder.init()
                .build();

        final Place destination = placeBuilder.init()
                .position(잠실_루터회관_정문_좌표)
                .build();

        final Game game = gameBuilder.init()
                .place(destination)
                .player(player)
                .gameStatus(IN_PROGRESS)
                .startTime(LocalDateTime.of(2023, Month.AUGUST, 13, 15, 30, 0))
                .endTime(LocalDateTime.of(2023, Month.AUGUST, 13, 17, 30, 0))
                .startPosition(잠실역_교보문고_좌표)
                .build();

        // when
        gameService.endGame(new EndGameCommand(player.getId(), GIVE_UP, 역삼역_좌표, game.getId()));

        // then
        final GameResult expected = gameService.findGameResultByGameId(game.getId());
        assertThat(expected.getGame().getId()).isEqualTo(game.getId());
    }

    @Test
    void 게임임생성_요청이_들어오면_게임을_저장하고_반환한다() {
        // given
        final Player player = playerBuilder.init()
                .build();

        final Place destination = placeBuilder.init()
                .position(잠실_루터회관_정문_근처_좌표)
                .build();

        // when
        final Game expected = gameService.createGame(new CreateGameCommand(player.getId(), 잠실_루터회관_정문_좌표));

        // then
        assertThat(gameService.findGameById(new FindGameByIdCommand(player.getId(), expected.getId())).getGameStatus()).isEqualTo(IN_PROGRESS);
    }

    @Test
    void 게임임생성_요청이_들어왔을때_진행중인_게임이_있다면_예외를_발생시킨다() {
        // given
        final Player player = playerBuilder.init()
                .build();

        final Place destination = placeBuilder.init()
                .position(잠실_루터회관_정문_근처_좌표)
                .build();

        final Game game = gameBuilder.init()
                .place(destination)
                .player(player)
                .gameStatus(IN_PROGRESS)
                .startTime(LocalDateTime.of(2023, Month.AUGUST, 13, 15, 30, 0))
                .endTime(LocalDateTime.of(2023, Month.AUGUST, 13, 17, 30, 0))
                .startPosition(잠실역_교보문고_좌표)
                .build();


        // when
        GameException gameException = assertThrows(GameException.class, () -> gameService.createGame(new CreateGameCommand(player.getId(), 잠실역_교보문고_좌표)));

        // then
        assertThat(gameException.exceptionType()).isEqualTo(ALREADY_IN_PROGRESS);
    }

    @Test
    void 게임임생성_요청이_들어왔을때_추천장소가_없다면_예외를_발생시킨다() {
        // given
        final Player player = playerBuilder.init()
                .build();

        final Place destination = placeBuilder.init()
                .position(역삼역_좌표)
                .build();

        // when
        GameException gameException = assertThrows(GameException.class, () -> gameService.createGame(new CreateGameCommand(player.getId(), 잠실역_교보문고_좌표)));

        // then
        assertThat(gameException.exceptionType()).isEqualTo(CAN_NOT_FIND_PLACE);
    }
}
