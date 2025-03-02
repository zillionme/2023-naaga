package com.now.naaga.game.domain.gamescore;

import com.now.naaga.game.domain.Game;
import com.now.naaga.game.domain.ResultType;
import com.now.naaga.score.domain.Score;

import static com.now.naaga.game.domain.ResultType.FAIL;

public class FailGameScorePolicy implements GameScorePolicy {
    
    @Override
    public Score calculate(final Game game) {
        return new Score(0);
    }
    
    @Override
    public boolean hasSameResultType(final ResultType resultType) {
        return resultType == FAIL;
    }
}
