## Autumn DI container 🍁

### About this project
Spring boot is now days by far the most used Java web framework, and
well, something that caught my eye it's how it magically instantiates and injects
your classes.

### Projects goals
- Get a decent understanding of how java.lang.reflect package can be used to 
create Java libraries
- Build the project only with Java's core (Done 🎉) 

### Features
* Field based injection
* Constructor based injection
* @Qualifier field bean injection
* @Qualifier constructor based injection
* Bean method instantiation
* Circular dependency injection detection
* Multi project build support through @ComponentScan annotation and "Jar in Jar" introspection
* Bean based circular dependency check

### Examples
As this project takes heavy inspiration after Spring boot's DI container, its usage
is also based annotations, you can find examples for Java, Kotlin and multi
project build in this repository [this repository](https://github.com/Glazzes/autumnexample)


### Building the project
This library is not intended to be used in production this is only a 
hobbyist project, with that said, run the following command:
```
./gradlew build publishToMavenLocal
```
With this command you've already built the project and published it to a
maven local repository.

### Usage
If you're using Maven this one will recognize by default all local 
libraries if any, if you're using gradle you'll need to add
mavenLocal() into repositories plugin of your `build.gradle` file.
```
repositories {
   mavenLocal() // <- add this
   other repositories...
}
```
Remember to add the respective artifacts for your favorite build tool.

Maven
```
<dependency>
    <groupId>com.glaze</groupId>
    <artifactId>autumn</artifactId>
    <version>0.1</version>
</dependency>
```

Gradle
```
implementation 'com.glaze:autumn:0.1'
```

Gradle kotlin
```
implementation("com.glaze:autumn:0.1")
```
