# Android Studio Configuration
Author: Long Qian

For reminding myself.

## Common stuff
* Make sure you have JAVA SDK installed and `JAVA_HOME, JRE_HOME, PATH` configured.
* Download [__android studio__](https://developer.android.com/sdk/index.html)
* Add `android_studio/bin` to PATH, set alias for `android_studio/bin/studio.sh` for terminal startup. Put android studio binaries somewhere you have full authority.
* Download android SDK and other tools via SDK manager. Set `ANDROID_HOME` and add it to `PATH`.

## Gradle
* Android studio comes with Gradle in `android_studio/bin/gradle/gradle-x.y`. Setup `GRADLE_HOME`, add `$GRADLE_HOME\bin` to `PATH`.
* I prefer local __Gradle__ to __Gradle Wrapper__, and choose the local one in preferences menu.
* I prefer local mavenRepository to jcenter, and use `mavenLocal()` in buildscript.
* Dependency:
```
buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:1.3.0'
    }
}
```

## ARToolKit
[__ARToolKit__](http://artoolkit.org/) is cool!

* Copy ARBaseLib code in the workspace, and in `aRBaseLib\build.gradle` apply android library plugin:
```
apply plugin: 'com.android.library'
```
* Make sure you compile the library prior to the app, add the following to `app\build.gradle`:
```
dependencies {
    compile project(':aRBaseLib')
    compile fileTree(dir: 'libs', include: ['*.jar'])
}
```

## ROS Android
[**ROS Android**](http://wiki.ros.org/android) is awesome!

* Install [**rosjava_core**](http://wiki.ros.org/rosjava/Tutorials/hydro/Installation) first. This will create you a local rosjava maven repository.
* Compiling **android_core** from source is troublesome. An easier way is to clone the [**rosjava_mvn_repo**](https://github.com/rosjava/rosjava_mvn_repo) to your local maven repository, add your local maven repository to your buildscript and you are done! Create ROS Android package as normal android package! Forget about **rosjava_catkin**!
* A sample top level `build.gradle` for ROS Android project:
```
buildscript {
    def rosMavenPath = "$System.env.ROS_MAVEN_PATH".split(':').collect { 'file://' + it }
    repositories {
        rosMavenPath.each { p ->
            maven {
                url p
            }
        }
        mavenLocal()
    }
    dependencies {
        classpath 'org.ros.rosjava_bootstrap:gradle_plugins:0.2.1'
        classpath 'com.android.tools.build:gradle:1.3.0'
    }
}
allprojects {
    group 'org.lcsr.moverio'
}
subprojects {
    apply plugin: 'ros-android'
    apply plugin: 'com.android.application'
}
defaultTasks 'assembleRelease', 'uploadArchives'

```
* Dependencies for a specific application in `app\build.gradle`:
```
dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    compile 'org.ros.rosjava_core:rosjava:0.2.1'
    compile 'org.ros.rosjava_messages:sensor_msgs:1.11.7'
    compile 'org.ros.android_core:android_gingerbread_mr1:0.1.2'
}
```

