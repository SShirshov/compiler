package edu.montana.csci.csci468.demo;


import edu.montana.csci.csci468.CatscriptTestBase;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;


public class Scratch extends CatscriptTestBase {


    @Test
    void forLoopWorks() {
        assertEquals("5\n2\n1\n8\n", executeProgram("" +
                "var list = [5, 2, 1, 8]\n" +
                "\n" +
                "for(i in list){\n" +
                "    print(i)\n" +
                "}"));
    }
    @Test
    void ifElseWorks() {
        assertEquals("hola\namigo\n", executeProgram("" +
                "var list = [\"hola\",\"amigo\"]\n" +
                "var x = 30\n" +
                "\n" +
                "if(x < 20){\n" +
                "}else{\n" +
                "    for(j in list){\n" +
                "        print(j)\n" +
                "    }\n" +
                "}"));
    }
    @Test
    void arithmeticWorks() {
        assertEquals("2360\n", executeProgram("var peanuts = 120\n" +
                "var cost_per_peanut = 13\n" +
                "var cost_of_bag = 800\n" +
                "var total_cost = peanuts * cost_per_peanut + cost_of_bag \n" +
                "print(total_cost)"));
        assertEquals("5\n", executeProgram("var chips = 100\n" +
                "var chips_per_bag = 20\n" +
                "var bags = chips / chips_per_bag\n" +
                "print(bags)"));
    }

}

