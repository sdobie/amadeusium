package com.sdobie;


import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RadarTurnTest {

    @Test
    public void shouldFindXPositionInRange() {
        RadarTurn testObject = new RadarTurn();

        int result = testObject.randomLocationX();

        assertThat(result, is(both(greaterThanOrEqualTo(0)).and(lessThan(26))));
    }

    @Test
    public void shouldFindYPositionInRange() {
        RadarTurn testObject = new RadarTurn();

        int result = testObject.randomLocationY();

        assertThat(result, is(both(greaterThanOrEqualTo(0)).and(lessThan(11))));
    }
}
