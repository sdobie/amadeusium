package com.sdobie;


import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RadarTurnTest {

    @Test
    public void shouldFindXPositionInRange() {
        RadarTurn testObject = new RadarTurn(new Grid().atTurnCount(200));

        int result = testObject.randomLocationX();

        assertThat(result, is(both(greaterThanOrEqualTo(0)).and(lessThan(26))));
    }

    @Test
    public void shouldFindXPositionOf5ForTurnLessThan11() {
        RadarTurn testObject = new RadarTurn(new Grid().atTurnCount(10));

        int result = testObject.randomLocationX();

        assertThat(result, is(5));
    }

    @Test
    public void shouldFindYPositionInRange() {
        RadarTurn testObject = new RadarTurn(new Grid().atTurnCount(200));

        int result = testObject.randomLocationY();

        assertThat(result, is(both(greaterThanOrEqualTo(0)).and(lessThan(11))));
    }
}
