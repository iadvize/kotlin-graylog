Kotlin Graylog [![CircleCI](https://circleci.com/gh/iadvize/kotlin-graylog.svg?style=svg)](https://circleci.com/gh/iadvize/kotlin-graylog)
==================

Client-side Android logging library for Graylog.

Kotlin Graylog supports versions from API 16.

## Examples

First of all, you have to initialize the library with a context and your Graylog endpoint: 

```kotlin
Graylog.init(context, URL("yourGraylogEndpoint"))
```

After that, you can send log to your Graylog server by calling: 

```kotlin
val values = LogValues()
values.put("name", "value")
Graylog.log(values)
```

## Install

Link your project with the Kotlin Graylog dependency, add this line to your app's `build.gradle`:
```gradle
implementation 'com.iadvize:kotlin-graylog:1.0.0'
```

## Contribute

Look at contribution guidelines here : [CONTRIBUTING.md](CONTRIBUTING.md)
