package org.example;

import java.io.IOException;
import java.util.Random;

import org.junit.jupiter.api.Test;

class AlgorithmTest {

	@Test
	void test() throws IOException {
		Generator gen = new Generator();
		gen.generateOntology();
		Algorithm alg = new Algorithm();
		alg.EntityLinking();
	}

}