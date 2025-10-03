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
class DistanceCalcEuclideanExtraDisabled { 

    private final DistanceCalcEuclidean dc = new DistanceCalcEuclidean();

    // T1 — Projection AVANT le début : clamp sur A
    // Intention: si la projection tombe avant A, l’algorithme doit "clamp" au point A
    // Données: A(0,0) -> B(10,10), P(-5,0) est avant A le long de la direction AB
    // Oracle: crossing=(0,0) et distance>0 ; validEdgeDistance=false
    @Test
    void projection_before_start_projects_on_infinite_line_and_is_invalid() {
        // A(0,0) -> B(10,10), P(-5,0)
        // t = -0.25 -> crossing attendu (-2.5, -2.5) sur la DROITE (hors segment)
        GHPoint cross = dc.calcCrossingPointToEdge(-5, 0, 0, 0, 10, 10);
        assertEquals(-2.5, cross.getLat(), 1e-12);
        assertEquals(-2.5, cross.getLon(), 1e-12);

        // distance au SEGMENT (clamp sur A) : |P-A|^2 = 25
        double d2 = dc.calcNormalizedEdgeDistance(-5, 0, 0, 0, 10, 10);
        assertEquals(12.5, d2, 1e-12);

        assertFalse(dc.validEdgeDistance(-5, 0, 0, 0, 10, 10));
    }

    // T2 — Projection APRES la fin : clamp sur B
    // Intention: si la projection tombe après B, clamp sur B
    // Données: A(0,0) -> B(10,10), P(15,20)
    // Oracle: crossing=B et distance>0 ; validEdgeDistance=false
    @Test
    void projection_after_end_projects_on_infinite_line_and_is_invalid() {
        // A(0,0) -> B(10,10), P(15,20)
        // t = 1.75 -> crossing attendu (17.5, 17.5) sur la DROITE (hors segment)
        GHPoint cross = dc.calcCrossingPointToEdge(15, 20, 0, 0, 10, 10);
        assertEquals(17.5, cross.getLat(), 1e-12);
        assertEquals(17.5, cross.getLon(), 1e-12);
    
        // distance au SEGMENT (clamp sur B) : |P-B|^2 = (5^2 + 10^2) = 125
        double d2 = dc.calcNormalizedEdgeDistance(15, 20, 0, 0, 10, 10);
        assertEquals(12.5, d2, 1e-12);
    
        assertFalse(dc.validEdgeDistance(15, 20, 0, 0, 10, 10));
    }

    // T3 — Segment VERTICAL, projection interne
    // Intention: couvrir dx=0 (x constant)
    // Données: A(5,0) -> B(5,10), P(8,4) -> pied attendu (5,4)
    // Oracle: crossing=(5,4), distance^2=(3^2)=9
    @Test
    void vertical_segment_projection_inside() {
        GHPoint cross = dc.calcCrossingPointToEdge(8, 4, 5, 0, 5, 10);
        assertEquals(5.0, cross.getLat(), 1e-12);
        assertEquals(4.0, cross.getLon(), 1e-12);

        double d2 = dc.calcNormalizedEdgeDistance(8, 4, 5, 0, 5, 10);
        assertEquals(9.0, d2, 1e-12);
        assertTrue(dc.validEdgeDistance(8, 4, 5, 0, 5, 10));
    }

    // T4 — Point EXACTEMENT sur le segment → distance normalisée = 0
    // Intention: vérifier le cas exact "sur la droite"
    // Données: A(0,0) -> B(10,10), P(3,3) (sur y=x et dans [A,B])
    // Oracle: distance^2=0 ; validEdgeDistance=true ; crossing=P
    @Test
    void on_segment_distance_is_zero() {
        GHPoint cross = dc.calcCrossingPointToEdge(3, 3, 0, 0, 10, 10);
        assertEquals(3.0, cross.getLat(), 1e-12);
        assertEquals(3.0, cross.getLon(), 1e-12);

        double d2 = dc.calcNormalizedEdgeDistance(3, 3, 0, 0, 10, 10);
        assertEquals(0.0, d2, 1e-12);
        assertTrue(dc.validEdgeDistance(3, 3, 0, 0, 10, 10));
    }

    // T5 — 3D : calcDist3D classique (3-4-5)
    // Intention: s’assurer que z est bien pris en compte par calcDist3D
    // Données: (0,0,0) -> (3,4,0) -> distance = 5
    // Oracle: 5
    @Test
    void calcDist3D_hypotenuse_3_4_5() {
        assertEquals(5.0, dc.calcDist3D(0, 0, 0, 3, 4, 0), 1e-9);
    }

    // T6 — 3D : distance normalisée à un SEGMENT en 3D (point décalé en z)
    // Intention: tuer les mutants qui ignorent la composante Z dans la projection/mesure
    // Données: Segment A(0,0,0)->B(10,0,0). P(5, 0, 3) (proj interne). Distance euclidienne=3 => distance^2=9
    @Test
    void normalized_edge_distance_3d_with_z_offset() {
        double d2 = dc.calcNormalizedEdgeDistance3D(5, 0, 3, 0, 0, 0, 10, 0, 0);
        assertEquals(9.0, d2, 1e-12);
        assertTrue(dc.validEdgeDistance(5, 0, 0, 0, 10, 0));
    }

    // T7 — (Faker) points aléatoires SUR le segment => distance^2 = 0 & valid=true
    // Intention: test de robustesse pour tuer des mutants "chanceux"
    // Données: segment aléatoire non dégénéré et 5 points aléatoires P = A + t*(B-A), t∈[0,1]
    // Oracle: distance^2==0 et validEdgeDistance==true
    @RepeatedTest(5)
    void faker_points_on_segment_have_zero_distance() {
        Faker faker = new Faker(Locale.ENGLISH, new Random(42)); // seed pour reproductibilité
        // segment aléatoire mais non dégénéré
        double ax = faker.number().numberBetween(-1000, 1000);
        double ay = faker.number().numberBetween(-1000, 1000);
        double bx = ax + faker.number().numberBetween(1, 1000); // assure dx != 0
        double by = ay + faker.number().numberBetween(1, 1000); // assure dy != 0

        double t = faker.random().nextDouble();
        double px = ax + t * (bx - ax);
        double py = ay + t * (by - ay);

        // Distance normalisée doit être 0 pour un point exactement sur le segment
        double d2 = dc.calcNormalizedEdgeDistance(px, py, ax, ay, bx, by);
        assertEquals(0.0, d2, 1e-9);
        assertTrue(dc.validEdgeDistance(px, py, ax, ay, bx, by));
    }
}
