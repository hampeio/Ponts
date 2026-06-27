javac -encoding UTF-8 -cp lib/jbox2d-library-2.2.1.1.jar:lib/flatlaf-2.1.jar $(find ponts -name "*.java") &&
java -cp .:lib/jbox2d-library-2.2.1.1.jar:lib/flatlaf-2.1.jar ponts.debug.SmokeTest
