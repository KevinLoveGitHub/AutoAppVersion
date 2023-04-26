# AutoAppVersion

## example

在项目中配置主版本和子版本，如果没有配置默认都为`1`

项目根目录 `build.gradle` 配置：

```groovy
 repositories {
    google()
    mavenCentral()
}

dependencies {
    classpath 'com.kangxiaoguang.gradle.tools:auto-app-version:2.0.7'
}
```

app modules `app/build.gradle` 配置：

```groovy
apply plugin: 'com.kangxiaoguang.auto-app-version'

// 可选配置
appVersion {
    appMajor  "2"
    appMinor  "1"
    // 指定versionCode
    versionCode 110
    // 指定versionName
    versionName "app"
    addCommitCount  false
    isDebug  false
}
```

由于`gradle 6.5.0`以后一些接口被限制，使用此版本以后的项目需要增加配置如下:

```groovy
android.applicationVariants.all {
    variant ->
        variant.outputs.each { output ->
            output.versionNameOverride = appVersion.customVersionName()
            output.versionCodeOverride = appVersion.customVersionCode()
            output.outputFileName = appVersion.fileName(variant, output)
        }
}
```

## 版本名说明

生成的版本号格式如：`v1.2.100.200610.53b1f2c_master_modify_Shine-debug`

* `1.2.100`：100为程序`versionCode`加上git commit次数
* `200610`：编译日期
* `53b1f2c`：commit id
* `master`：分支名
* `modify`：如果编译该apk的代码未提交会出现该标识
* `Shine-debug`：build variant

## 版本号说明

如果定义`addCommitCount`，且值为`true`，版本号为程序`versionCode`加上git commit次数，否则为程序`versionCode`。该字段默认为`true`
