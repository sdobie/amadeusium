package com.sdobie;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class GridTest {

    @Test
    public void shouldFindOre() {
        Grid testObject = new Grid();
        testObject.addCell(new Cell(Position.set(1, 1), "?"));
        testObject.addCell(new Cell(Position.set(2, 2), "1"));

        Cell result = testObject.oreLocation();

        assertThat(result.position, is(Position.set(2, 2)));
    }

    @Test
    public void shouldFindNoCellWhenNoOre() {
        Grid testObject = new Grid();
        testObject.addCell(new Cell(Position.set(1, 1), "?"));
        testObject.addCell(new Cell(Position.set(2, 2), "?"));

        Cell result = testObject.oreLocation();

        assertThat(result, is(Cell.NO_CELL));
    }
}
