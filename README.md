James
=====

James is an extensive Java framework for optimization using local search metaheuristics. The framework is mainly focused on subset selection, which has many applications in various areas, though other types of problems can also easily be plugged in.

Modules
-------

The James framework consists of several modules:
 
 - [James Core Module][1]: as its name suggests, this module contains the core components of the framework. It includes general concepts modeling problems, objectives, constraints, algorithms, neighbourhoods, etc.
 
   A number of generic local search algorithms are provided out-of-the-box, including random descent, steepest descent, tabu search, variable neighbourhood search, parallel tempering, etc. Exhaustive search is also available, which is of course only applicable to problems with a reasonable small, finite search space.
   
   Moreover, the core module contains implementations of components for subset selection, as well as some specific subset sampling algorithms.
   
 - [James Extensions Module][2]: this module extends the core with components for advanced subset selection as well as other types of problems (e.g. permutation problems). 

Dependencies
============

James requires Java 7 or later. To perform logging, James depends on the [Simple Logging Facade for Java (SLF4J)][5],
which is a general logging API that provides bindings for several popular logging frameworks including log4j, JDK 1.4 logging and logback. To use such logging framework, include the appropriate binding on your classpath as described in
the [SLF4J user manual][6]. If no binding is found, a warning message

```
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
```

is printed to the console and all log messages are silently discarded.

Download and install
====================

James is currently under development and no stable releases have yet been published.

However, development snapshots are frequently deployed at [Sonatype OSSRH][3] (OSS Repository Hosting Service).
Artifacts can be automatically retrieved using Maven, or you can manually put the necessary jar files on your
classpath (see below).

Remember that snapshots are no stable releases and should therefore be used with care. For example, their API might
change without notice, and they might contain incomplete or insufficiently tested components.

### Maven

If you are using Maven, snapshots can be automatically retrieved by adding the Sonatype snapshots repository

```
<repositories>
  <repository>
    <id>snapshots-repo</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
  </repository>
</repositories>
```

to your project's POM file and specifying the appropriate dependencies, e.g.

```
<dependency>
  <groupId>org.jamesframework</groupId>
  <artifactId>james-core</artifactId>
  <version>0.1-SNAPSHOT</version>
</dependency>
```

for the core module or

```
<dependency>
  <groupId>org.jamesframework</groupId>
  <artifactId>james-extensions</artifactId>
  <version>0.1-SNAPSHOT</version>
</dependency>
```

for the extensions. This will automatically retrieve the latest snapshot of the specified module version (here 0.1) and any dependencies. Upcoming stable releases will be published at the Maven Central Repository, so that they can easily
be added as dependencies to a Maven project without having to specify a custom repository.

### Plain-old JARs

Alternatively, you can manually grab the necessary jar files and put them on your classpath:

 - [james-core.jar](https://oss.sonatype.org/index.html#nexus-search;quick~james-core)
 - [slf4j-api.jar](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.slf4j%22%20slf4j-api)
 - [optional] [james-extensions.jar](https://oss.sonatype.org/index.html#nexus-search;quick~james-extensions)

The extensions are optional and should only be included if you are using any of its components.

Building from source code
=========================

James is built using [Maven][4], so compiling the source code should be as easy as running

```
mvn install
```

from inside the `james` root directory

```
|-- james
  |-- james-core
  |-- james-extensions
  |-- pom.xml
```

assuming that Maven has been installed on your computer. This will compile the code, create jar packages and install them in your local Maven repository, so that they can be added as dependencies in any other Maven project. After building James, you can also grab the created jar packages from the `target` directory inside each module.


Documentation
=============

More information, user documentation and examples of how to use the framework are provided at the James website www.jamesframework.org. In addition, developer documentation is posted on the [wiki](http://github.com/hdbeukel/james/wiki).

Contact
=======

The James framework is developed and maintained by

 - Herman De Beukelaer (Herman.DeBeukelaer@UGent.be)
 
 
 
[1]: https://github.com/hdbeukel/james/tree/master/james/james-core
[2]: https://github.com/hdbeukel/james/tree/master/james/james-extensions
[3]: https://oss.sonatype.org/index.html#welcome
[4]: http://maven.apache.org/download.cgi
[5]: http://www.slf4j.org
[6]: http://www.slf4j.org/manual.html


