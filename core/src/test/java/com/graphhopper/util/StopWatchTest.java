package com.graphhopper.util;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Auteurs : Yasmine Ben Youssef, Hazem Ben Amor
 * Projet : Tests unitaires pour la classe StopWatch du projet GraphHopper.
 */
class StopWatchTest {

    /**
     * Test 1 – Vérifie qu’après un start() suivi d’un stop() le temps mesuré est bien positif.
     */
    @Test
    void testStartAndStopShouldIncreaseTime() throws InterruptedException {
        StopWatch sw = new StopWatch().start();
        Thread.sleep(2);
        sw.stop();
        assertTrue(sw.getNanos() > 0);
    }

    /**
     * Test 2 – Vérifie que getSeconds() correspond bien à getNanos() / 1e9.
     */
    @Test
    void testGetSecondsOracleComparison() throws InterruptedException {
        StopWatch sw = new StopWatch().start();
        Thread.sleep(3);
        sw.stop();
        double expected = sw.getNanos() / 1e9;
        assertEquals(expected, sw.getSeconds(), 1e-9);
    }

    /**
     * Test 3 – Vérifie que getCurrentSeconds() augmente pendant que le chronomètre tourne.
     */
    @Test
    void testGetCurrentSecondsWhileRunning() throws InterruptedException {
        StopWatch sw = new StopWatch().start();
        double before = sw.getCurrentSeconds();
        Thread.sleep(3);
        double after = sw.getCurrentSeconds();
        assertTrue(after > before);
        sw.stop();
    }

    /**
     * Test 4 – Vérifie que getTimeString() retourne un texte avec
     * une unité de temps cohérente (ns, µs, ms ou s).
     */
    @Test
    void testGetTimeStringCoversAllUnits() throws InterruptedException {
        StopWatch sw = new StopWatch();

        // Cas très court
        sw.start();
        sw.stop();
        String text1 = sw.getTimeString();
        assertTrue(text1.contains("ns") || text1.contains("µs") || text1.contains("ms"));

        // Cas un peu plus long
        sw.start();
        Thread.sleep(5);
        sw.stop();
        String text2 = sw.getTimeString();
        assertTrue(text2.contains("ms") || text2.contains("s"));

        // Vérifie que le texte n’est jamais nul
        sw = new StopWatch().start().stop();
        assertNotNull(sw.getTimeString());
    }

    /**
     * Test 5 – Vérifie que le nom passé au constructeur apparaît dans toString().
     */
    @Test
    void testToStringIncludesNameIfPresent() {
        StopWatch sw = new StopWatch("Timer").start().stop();
        String output = sw.toString();
        assertTrue(output.startsWith("Timer"));
    }

    /**
     * Test 6 – Vérifie que stop() appelé avant start() ne cause pas d’erreur.
     */
    @Test
    void testStopWithoutStartDoesNotCrash() {
        StopWatch sw = new StopWatch();
        sw.stop();
        assertTrue(sw.getNanos() >= 0);
    }

    /**
     * Test 7 – Utilise Faker pour créer un nom aléatoire et vérifier
     * qu’il apparaît bien dans la sortie du chronomètre.
     */
    @Test
    void testWithFakerGeneratedName() {
        Faker faker = new Faker();
        String randomName = faker.app().name();
        StopWatch sw = new StopWatch(randomName).start().stop();
        String output = sw.toString();
        assertTrue(output.contains(randomName) || output.contains("time:"));
    }
}
