## Autumn DI container

### About this project
Spring boot is by far the most used Java framework today and
something that caught my eye was the dependency injection
container and how it "magically" injects the right instances 
into your classes.

The main goal of this project is to get a decent understanding
of how java.lang.reflect package can be used to manipulate bytecode
at execution time to create new libraries.

### Features
* Field based injection
* Constructor based injection
* @Qualifer field bean injection
* @Qualifer constructor based injection
* Bean method instantiation
* Circular dependency injection detection
* Bean based circular dependency check (TODO)

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
mavenLocal() into repositories declaration of your `build.gradle` file.
```
repositories {
   mavenLocal() // <- add this
   other repositories...
}
```
As the description says, this project is based on Spring boot's DI
container, therefore it's usage is very similar, you can find an autumn example
in [this repository](https://github.com/Glazzes/autumnexample) both for Java and
Kotlin.

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