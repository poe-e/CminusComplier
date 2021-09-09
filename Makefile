JAVA=java
JAVAC=javac
CLASSPATH=-cp D:\Programs\cup-1.01\lib\java-cup-11b.jar
#CLASSPATH=-cp /usr/share/java/cup.jar:.
JFLEX=jflex
CUP=cup

all: CM.class

CM.class: absyn/*.java parser.java sym.java NodeType.java SemanticAnalyzer.java CodeGenerator.java Lexer.java ShowTreeVisitor.java Scanner.java CM.java

%.class: %.java
	$(JAVAC) $(CLASSPATH) $^

Lexer.java: cm.flex
	$(JFLEX) cm.flex

parser.java: cm.cup
	$(CUP) -expect 3 cm.cup

clean:
	del parser.java Lexer.java sym.java *.class absyn\*.class
