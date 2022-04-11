## Autumn DI container

### About this project
Spring boot is now days by far the most used Java web framework, and
well, something that caught my eye it's how it magically instantiates and injects
your classes.

### Projects goals
- Get a decent understanding of how java.lang.reflect package can be used to 
create Java libraries
- Build the project solely with what Java's core offers (Done!!!)  

### Features
* Field based injection
* Constructor based injection
* @Qualifer field bean injection
* @Qualifer constructor based injection
* Bean method instantiation
* Circular dependency injection detection
* Multi project build support through @ComponentScan annotation and "Jar in Jar" introspection
* Bean based circular dependency check

### Examples
As this project takes heavy inspiration after Spring boot's DI container, its usage
it's algo based annotations, you can find examples for Java, Kotlin and multi
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
libraries if any, but if you're running it with gradle you'll need to add
mavenLocal() into repositories closure of your `build.gradle` file.
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
    <groupId>com.glaze.autumn</groupId>
    <artifactId>glaze-autumn</artifactId>
    <version>1.3</version>
</dependency>
```

Gradle
```
implementation 'com.glaze.autumn:glaze-autumn:1.3'
```

Gradle kts
```
implementation("com.glaze.autumn:glaze-autumn:1.3")
```