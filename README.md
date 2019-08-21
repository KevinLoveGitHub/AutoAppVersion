# AutoAppVersion

## example
在项目中配置主版本和子版本，如果没有配置默认都为`1`

项目根目录 `build.gradle` 配置：

```groovy
dependencies {
    classpath 'com.kangxiaoguang.gradle.tools:auto-app-version:1.0'
}
```

app modules `app/build.gradle` 配置：

```
apply plugin: 'com.kangxiaoguang.auto-app-version'

appVersion {
    appMajor  "2"
    appMinor  "1"
    isDebug  false
}
```
