# AutoAppVersion
[![Download](https://api.bintray.com/packages/kevinlive/maven/auto-app-version/images/download.svg) ](https://bintray.com/kevinlive/maven/auto-app-version/_latestVersion)
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
    addCommitCount  false
    isDebug  false
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
