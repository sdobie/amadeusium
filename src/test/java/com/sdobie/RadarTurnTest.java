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
    public void shouldFindXPositionOf5ForTurnLessThan9() {
        RadarTurn testObject = new RadarTurn(new Grid().atTurnCount(8));

        int result = testObject.randomLocationX();

        assertThat(result, is(7));
    }

    @Test
    public void shouldGetAValueFromInitialPositions() {
        RadarTurn testObject = new RadarTurn(new Grid());

        Bot result = testObject.updateBot(new Bot(1, Position.NO_POSITION, ItemType.RADAR).withRole(Role.RADAR));

        assertThat(result.destination, is(Position.set(7, 5)));
    }

    @Test
    public void shouldFindXPositionOf14ForTurns10To20() {
        RadarTurn testObject = new RadarTurn(new Grid().atTurnCount(20));

        int result = testObject.randomLocationX();

        assertThat(result, is(16));
    }

    @Test
    public void shouldFindXPositionOf23ForTurns21To40() {
        RadarTurn testObject = new RadarTurn(new Grid().atTurnCount(40));

        int result = testObject.randomLocationX();

        assertThat(result, is(25));
    }

    @Test
    public void shouldFindYPositionInRange() {
        RadarTurn testObject = new RadarTurn(new Grid().atTurnCount(200));

        int result = testObject.randomLocationY();

        assertThat(result, is(both(greaterThanOrEqualTo(0)).and(lessThan(11))));
    }
}
