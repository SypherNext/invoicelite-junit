package com.invoicelite;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Prints the order in which JUnit calls each part of a test class.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LifecycleDemoTest {

    private static int instancesCreated = 0;
    private final int instance;

    LifecycleDemoTest() {
        instance = ++instancesCreated;
        System.out.println("constructor: instance " + instance);
    }

    @BeforeAll
    static void beforeAll() {
        System.out.println("@BeforeAll: once, before any test");
    }

    @BeforeEach
    void beforeEach(TestInfo test) {
        System.out.println("@BeforeEach: instance " + instance + ", before " + test.getDisplayName());
    }

    @Test
    @Order(1)
    void firstTest() {
        System.out.println("@Test: firstTest");
    }

    @Test
    @Order(2)
    void secondTest() {
        System.out.println("@Test: secondTest");
    }

    @AfterEach
    void afterEach() {
        System.out.println("@AfterEach: instance " + instance);
    }

    @AfterAll
    static void afterAll() {
        System.out.println("@AfterAll: once, after every test");
    }
}
