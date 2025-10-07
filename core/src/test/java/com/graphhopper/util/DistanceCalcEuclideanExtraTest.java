package com.graphhopper.util;

import com.github.javafaker.Faker;
import com.graphhopper.util.shapes.GHPoint;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@Tag("extra")
class DistanceCalcEuclideanExtraTest {

    private final DistanceCalcEuclidean dc = new DistanceCalcEuclidean();

    // T1 — Projection AVANT le début : hors segment ; invalid
    @Test
    void projection_before_start_projects_on_infinite_line_and_is_invalid() {
        GHPoint cross = dc.calcCrossingPointToEdge(-5, 0, 0, 0, 10, 10);
        assertEquals(-2.5, cross.getLat(), 1e-12);
        assertEquals(-2.5, cross.getLon(), 1e-12);

        double d2 = dc.calcNormalizedEdgeDistance(-5, 0, 0, 0, 10, 10);
        assertEquals(12.5, d2, 1e-12);
        assertFalse(dc.validEdgeDistance(-5, 0, 0, 0, 10, 10));
    }

    // T2 — Projection APRES la fin : hors segment ; invalid
    @Test
    void projection_after_end_projects_on_infinite_line_and_is_invalid() {
        GHPoint cross = dc.calcCrossingPointToEdge(15, 20, 0, 0, 10, 10);
        assertEquals(17.5, cross.getLat(), 1e-12);
        assertEquals(17.5, cross.getLon(), 1e-12);

        double d2 = dc.calcNormalizedEdgeDistance(15, 20, 0, 0, 10, 10);
        assertEquals(12.5, d2, 1e-12);
        assertFalse(dc.validEdgeDistance(15, 20, 0, 0, 10, 10));
    }

    // T3 — Segment VERTICAL : projection interne ; valid
    @Test
    void vertical_segment_projection_inside() {
        GHPoint cross = dc.calcCrossingPointToEdge(8, 4, 5, 0, 5, 10);
        assertEquals(5.0, cross.getLat(), 1e-12);
        assertEquals(4.0, cross.getLon(), 1e-12);

        double d2 = dc.calcNormalizedEdgeDistance(8, 4, 5, 0, 5, 10);
        assertEquals(9.0, d2, 1e-12);
        assertTrue(dc.validEdgeDistance(8, 4, 5, 0, 5, 10));
    }

    // T4 — Sur-segment + extras: toString, (de)normalized, intermediatePoint, shrinkFactor
    @Test
    void on_segment_distance_is_zero_and_endpoints_are_zero() {
        // point sur segment
        GHPoint cross = dc.calcCrossingPointToEdge(3, 3, 0, 0, 10, 10);
        assertEquals(3.0, cross.getLat(), 1e-12);
        assertEquals(3.0, cross.getLon(), 1e-12);
        assertEquals(0.0, dc.calcNormalizedEdgeDistance(3, 3, 0, 0, 10, 10), 1e-12);
        assertTrue(dc.validEdgeDistance(3, 3, 0, 0, 10, 10));

        // extrémités : zéro
        assertEquals(0.0, dc.calcNormalizedEdgeDistance(0, 0, 0, 0, 10, 10), 1e-12);
        assertEquals(0.0, dc.calcNormalizedEdgeDistance(10, 10, 0, 0, 10, 10), 1e-12);

        // couvre toString + (de)normalized
        assertEquals("2D", dc.toString());
        assertEquals(25.0, dc.calcNormalizedDist(5.0), 1e-12);
        assertEquals(5.0, dc.calcDenormalizedDist(25.0), 1e-12);

        // couvre intermediatePoint + calcShrinkFactor
        GHPoint mid = dc.intermediatePoint(0.5, 0, 0, 10, 10);
        assertEquals(5.0, mid.getLat(), 1e-12);
        assertEquals(5.0, mid.getLon(), 1e-12);
        assertEquals(1.0, dc.calcShrinkFactor(10, 20), 1e-12);
    }

    // T5 — 3D + toutes les méthodes "non supportées" (exception)
    @Test
    void calcDist3D_hypotenuse_3_4_5_and_unsupported_apis() {
        // 2D: (0,0)->(3,4) = 5
        assertEquals(5.0, dc.calcDist(0, 0, 3, 4), 1e-9);

        // 3D: certaines versions jettent UOE; on accepte les deux
        try {
            double d3 = dc.calcDist3D(0, 0, 0, 3, 4, 0);
            assertEquals(5.0, d3, 1e-9);
        } catch (UnsupportedOperationException ex) {
            assertTrue(ex.getMessage() == null || ex.getMessage().contains("2D"));
        }

        // Couvre toutes les méthodes qui lancent UOE
        assertThrows(UnsupportedOperationException.class, () -> dc.calcCircumference(42));
        assertThrows(UnsupportedOperationException.class, () -> dc.isDateLineCrossOver(170, -170));
        assertThrows(UnsupportedOperationException.class, () -> dc.createBBox(0, 0, 100));
        assertThrows(UnsupportedOperationException.class, () -> dc.projectCoordinate(0, 0, 1000, 0));
        assertThrows(UnsupportedOperationException.class, () -> dc.isCrossBoundary(170, -170));
    }

    // T6 — 3D : projection interne (z décalé) + segment dégénéré
    @Test
    void normalized_edge_distance_3d_with_z_offset_and_degenerate() {
        double d2 = dc.calcNormalizedEdgeDistance3D(5, 0, 3, 0, 0, 0, 10, 0, 0);
        assertEquals(9.0, d2, 1e-12);
        assertTrue(dc.validEdgeDistance(5, 0, 0, 0, 10, 0));

        // segment dégénéré (A==B) : juste l'invalidité
        assertFalse(dc.validEdgeDistance(1.0, 2.0, 5.0, 5.0, 5.0, 5.0));
    }

    // T7 — Faker : points aléatoires sur le segment → d²=0 ; valid
    @RepeatedTest(5)
    void faker_points_on_segment_have_zero_distance() {
        Faker faker = new Faker(Locale.ENGLISH, new Random(42)); // seed stable
        double ax = faker.number().numberBetween(-1000, 1000);
        double ay = faker.number().numberBetween(-1000, 1000);
        double bx = ax + faker.number().numberBetween(1, 1000); // dx != 0
        double by = ay + faker.number().numberBetween(1, 1000); // dy != 0

        double t = faker.random().nextDouble();
        double px = ax + t * (bx - ax);
        double py = ay + t * (by - ay);

        double d2 = dc.calcNormalizedEdgeDistance(px, py, ax, ay, bx, by);
        assertEquals(0.0, d2, 1e-9);
        assertTrue(dc.validEdgeDistance(px, py, ax, ay, bx, by));
    }
}