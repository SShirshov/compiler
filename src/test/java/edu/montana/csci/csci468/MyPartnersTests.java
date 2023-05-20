package edu.montana.csci.csci468;

import edu.montana.csci.csci468.CatscriptTestBase;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MyPartnersTests extends CatscriptTestBase {
    @Test
    void nestedForLoopWorks() {
        assertEquals("1\nhi\nbye\n2\nhi\nbye\n3\nhi\nbye\n4\nhi\nbye\n", executeProgram("" +
                "var list = [1, 2, 3, 4]\n" +
                "var list2 = [\"hi\",\"bye\"]\n" +
                "\n" +
                "for(i in list){\n" +
                "    print(i)\n" +
                "    for (j in list2){\n" +
                "        print(j)\n" +
                "    }\n" +
                "}"));
    }
    @Test
    void ifAndIfElseStatementWithNestedForLoopWorks() {
        assertEquals("word1\nword2\n", executeProgram("" +
                "var list2 = [\"word1\",\"word2\"]\n" +
                "var x = 50\n" +
                "\n" +
                "if(x > 25){\n" +
                "    for (j in list2){\n" +
                "        print(j)\n" +
                "    }\n" +
                "}"));
        assertEquals("go\nbobcats\n", executeProgram("" +
                "var list = [\"go\",\"bobcats\"]\n" +
                "var x = 10\n" +
                "\n" +
                "if(x > 25){\n" +
                "}else{\n" +
                "    for(j in list){\n" +
                "        print(j)\n" +
                "    }\n" +
                "}"));
    }
    @Test
    void multiVariableMathWorks() {
        assertEquals("2500\n", executeProgram("" +
                "var base = 50\n" +
                "\n" +
                "var height = 100\n" +
                "\n" +
                "var areaOfTriangle = 0\n" +
                "\n" +
                "areaOfTriangle = (base*height)/2\n" +
                "\n" +
                "print(areaOfTriangle)"));
        assertEquals("600\n", executeProgram("" +
                "var base1 = 10\n" +
                "\n" +
                "var base2 = 50\n" +
                "\n" +
                "var height = 20\n" +
                "\n" +
                "var areaOfTrapezoid = 0\n" +
                "\n" +
                "areaOfTrapezoid = ((base1+base2)/2)*20\n" +
                "\n" +
                "print(areaOfTrapezoid)"));
    }
}
