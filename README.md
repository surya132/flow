[![Maven Central](https://img.shields.io/maven-central/v/net.tascalate.javaflow/net.tascalate.javaflow.parent.svg)](https://search.maven.org/#artifactdetails%7Cnet.tascalate.javaflow%7Cnet.tascalate.javaflow.parent%7C2.0%7Cpom) [![GitHub release](https://img.shields.io/github/release/vsilaev/tascalate-javaflow.svg)](https://github.com/vsilaev/tascalate-javaflow/releases/tag/2.0) [![license](https://img.shields.io/github/license/vsilaev/tascalate-javaflow.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)
# Continuation support for Java
This project contains libary, tools and examples to develop Java applications using continuations. 

According to Wikipedia "a continuation is an abstract representation of the control state of a computer program; a continuation reifies the program control state, i.e. the continuation is a data structure that represents the computational process at a given point in the process's execution; the created data structure can be accessed by the programming language, instead of being hidden in the runtime environment"

In other words, continuation allows you to capture execution state of the program (local variables, execution stack, program counters etc.) at the certain places and later resume execution from the saved point. Unfortunately, Java has no support for first-class continuations, but it can be added via bytecode instrumentation (like this library did)

The project is based on the completely reworked Apache Jakarta-Commons JavaFlow project (http://commons.apache.org/sandbox/commons-javaflow/). Below is a list of major changes:

1. Original JavaFlow instruments bytecode of each and every method available for tooling to add Continuations support. This adds significant overhead to both code size and execution time. The reworked version requires developer to mark Continuation-aware classes/methods explicitly with annotations.
2. Code is updated to use new bytecode instrumentation tools (ObjectWeb ASM 5.x); support for BCEL is discontinued 
3. Codebase is split to separate modules: run-time API, instrumentation provider SPI, offline instrumentation tools (Maven, Ant, command-line), run-time instrumentation JavaAgent, utilities and examples
4. The library adds support for recent Java features like lambdas and dynamic dispatch

# Maven

You have to add the following configuration to enable build-time instrumentation of classes during Maven build:
```xml
	<dependencies>
		<dependency>
			<groupId>net.tascalate.javaflow</groupId>
			<artifactId>net.tascalate.javaflow.api</artifactId>
			<version>2.0</version>
		</dependency>
		...
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<configuration>
					<source>1.8</source>
					<target>1.8</target>
				</configuration>
			</plugin>
			<plugin>
				<groupId>net.tascalate.javaflow</groupId>
				<artifactId>net.tascalate.javaflow.tools.maven</artifactId>
				<version>2.0</version>
				<executions>
					<execution>
						<phase>process-classes</phase>
						<goals>
							<goal>javaflow-enhance</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>
```
Note that if you are using continuations with Java 1.8 lambdas then you need to add [Tascalate JavaFlow instrumentation agent](https://github.com/vsilaev/tascalate-javaflow/releases/download/2.0/javaflow.instrument-continuations.jar) at run-time as command-line option, while lambda-related classes are generated by JVM on the fly and there is no other way to instrument them.

Please refer to [pom.xml](https://github.com/vsilaev/tascalate-javaflow/blob/master/net.tascalate.javaflow.examples/pom.xml) in examples project for typical Maven configuration

# Ant

There is a separate Ant task for applying JavaFlow instrumentation at build-time. 

It's possibe to instrument compiled Java classes as below:
```xml
    <target name="instrument-classes" description="JavaFlow Instrumentation" depends="compile">
        <taskdef name="javaflow" classname="org.apache.commons.javaflow.ant.AntRewriteTask" 
        classpathref="ant-lib-classpath"/>
        <echo message="JavaFlow instrumentation of compiled classes in ${classes.dir}" />
        <javaflow srcdir="${classes.dir}" destdir="${i-classes.dir}" classpathref="classpath"/>
    </target>
```
... as well as re-write packaged JAR file:
```xml
    <target name="instrument-jar" description="JavaFlow Instrumentation" depends="jar">
        <taskdef name="javaflow" classname="org.apache.commons.javaflow.ant.AntRewriteTask" 
        classpathref="ant-lib-classpath"/>
        <echo message="JavaFlow instrumentation of compiled classes in ${jar.dir}/${ant.project.name}.jar" />
        <javaflow srcdir="${jar.dir}" destdir="${i-jar.dir}" classpathref="classpath"/>
    </target>
```
You may download a complete [examples project setup](https://github.com/vsilaev/tascalate-javaflow/releases/download/2.0/tascalate-javaflow-ant-project-setup1.zip) from [the latest release](https://github.com/vsilaev/tascalate-javaflow/releases/tag/2.0) for complete configuration template. Please pay attention to <code>ant-lib</code> folder with Ant TaskDef and <code>lib</code> folders with compile-/runtime-dependencies.

# Java Instrumentation Agent (Runt-time Instrumentation)
As an alternative to compile-time bytecode instrumentation, you MAY use [Tascalate JavaFlow Instrumentation Agent](https://github.com/vsilaev/tascalate-javaflow/releases/download/2.0/javaflow.instrument-continuations.jar) from [the latest release](https://github.com/vsilaev/tascalate-javaflow/releases/tag/2.0) to enable continuations support at class-loading time. Please note, that if you are using Java 8 and creating continuable lambda functions (either anonymous or/and as method references) you SHOULD use this instrumentation agent always: as long as Java run-time generates implementation of functional interfaces on the fly there is no other option to instrument them. To enable Tascalate JavaFlow Instrumentation Agent please add the following arguments to Java command line:
```bash
    java -javaagent:<path-to-jar>/javaflow.instrument-continuations.jar <rest-of arguments>
```
The agent JAR file includes all necessary dependencies and requires no additional CLASSPATH settings. It's recommended to use this agent in conjunction with either Maven or Ant build tools supplied to minimize the associated overhead of the instrumentation during class-loading process at run-time.

Another useful application of the instrumentation agent is to apply it for debugging code within your IDE of choice. Just specify the "-javaagent" option listed above in your IDE debug/run configuration and you will be able to perform quick "debug-fix" loops without executing full project rebuild. 

# Command-line tools
It's possible to use a stand-alone command-line utility [JavaFlowRewriteJar.jar](https://github.com/vsilaev/tascalate-javaflow/releases/download/2.0/JavaFlowRewriteJar.jar) to instrument JAR archives containing continuable classes. Please use the following command:

```bash
    java -jar JavaFlowRewriteJar.jar src1.jar dst1.jar src2.jar dst2.jar...
```
Note, that the source and the destination should be different files.

# CDI Support
To work correctly in CDI environment continuable methods should be advised only by continuation-aware CDI proxies (interceptors, scope proxies, etc). Obviously, generation of these proxies is out of our control. Plus, major CDI containers (JBoss Weld and Apache OpenWebBeans) generates such proxies dynamically at run-time. Therefore if you plan to use Tascalate JavaFlow continuations with managed beans' methods then it's necessary to instrument CDI-specific proxies with [javaflow.instrument-cdi-proxy.jar](https://github.com/vsilaev/tascalate-javaflow/releases/download/2.0/javaflow.instrument-cdi-proxy.jar) Java Agent:
```bash
    java -javaagent:<path-to-jar>/javaflow.instrument-cdi-proxy.jar <rest-of arguments>
```
Please note, that CDI-specific agent neither requires javaflow.instrument-continuations.jar to operate correctly nor provides class file transformers for continuable methods. So if your project runs with CDI environment AND uses Java 8 lambdas then you have to add 2 Java agents, every serving different purpose:
```bash
    java -javaagent:<path-to-jar>/javaflow.instrument-continuations.jar \
         -javaagent:<path-to-jar>/javaflow.instrument-cdi-proxy.jar \
	 <rest-of arguments>
```
CDI functionality is tested with JBoss Weld 2.x (up to 2.4.2) and Apache OpenWebBeans 1.6.x / 1.7.x (up to 1.7.2). Contribution for other CDI/CDI-like containers (Spring, Google Guice, etc) is welcome.

# More documentation
For additional documentation, tutorials and guidelines please visit my [blog](http://vsilaev.com)
