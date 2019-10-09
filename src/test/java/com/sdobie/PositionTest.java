package com.sdobie;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PositionTest {

    @Test
    public void shouldFindTheDistanceVetweenTwoPositions() {

        validateDistance(Position.set(0, 0), Position.set(0, 1), 1);
        validateDistance(Position.set(0, 0), Position.set(1, 1), 2);
        validateDistance(Position.set(1, 1), Position.set(0, 0), 2);
        validateDistance(Position.set(5, 7), Position.set(3, 14), 9);

    }

    private void validateDistance(Position position1, Position position2, int expected) {
        int result = position1.distanceTo(position2);

        assertThat(result, is(expected));
    }
}
