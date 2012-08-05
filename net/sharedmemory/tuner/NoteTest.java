package net.sharedmemory.tuner;
import j2meunit.framework.*;

public class NoteTest extends TestCase {

    public NoteTest() {
    }

    public NoteTest(String testName, TestMethod testMethod) {
        super(testName, testMethod);
    }

    /**
     * Test of findNote method, of class net.sharedmemory.tuner.Note.
     */
    public void testfindNote() {
        System.out.println("findNote");
        net.sharedmemory.tuner.Note instance = new net.sharedmemory.tuner.Note();

        double frequency = 441.123;
        java.lang.String expectedResult = "A4";
        java.lang.String result = instance.findNote(frequency);
        assertEquals(expectedResult, result);
        
        frequency = 523.25;
        expectedResult = "C5";
        result = instance.findNote(frequency);
        assertEquals(expectedResult, result);
        
        frequency = 417.96875;
        expectedResult = "G#4";
        result = instance.findNote(frequency);
        assertEquals(expectedResult, result);
        
        frequency = 1316.40625;
        expectedResult = "B5";
        result = instance.findNote(frequency);
        assertEquals(expectedResult, result);
    }

    public void setUp() {
    }

    public void tearDown() {
    }


    /**
     * Test of tuningDirection method, of class net.sharedmemory.tuner.Note.
     */
    public void testtuningDirection() {
        System.out.println("tuningDirection");
        java.lang.String noteName = "";
        double frequency = 0.0;
        net.sharedmemory.tuner.Note instance = new net.sharedmemory.tuner.Note();
        int expectedResult = 0;
        int result = instance.tuningDirection(noteName,frequency);
        assertEquals(expectedResult, result);
        
        //TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    public Test suite() {
        TestSuite suite = new TestSuite();
        
        suite.addTest(new NoteTest("testfindNote", new TestMethod(){ public void run(TestCase tc) {((NoteTest) tc).testfindNote();}}));
//        suite.addTest(new NoteTest("testtuningDirection", new TestMethod(){ public void run(TestCase tc) {((NoteTest) tc).testtuningDirection();}}));
        return suite;
    }
}
