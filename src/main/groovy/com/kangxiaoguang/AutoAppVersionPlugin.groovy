package com.kangxiaoguang

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.InvalidPluginException
import org.gradle.process.ExecSpec

import java.util.logging.Logger

/**
 * 说明：
 * 作者：Kevin
 * 日期：2019-08-13*/
class AutoAppVersionPlugin implements Plugin<Project> {
    private String assembleRegex = "^(:.*:)*assemble.*"
    private AutoAppVersionExtension extension
    private Logger logger
    private Project project
    private String customVersionName
    private String revisionNumber
    private int versionCode

    @Override
    void apply(Project project) {
        this.project = project
        logger = Logger.getLogger("")
        if (!project.plugins.hasPlugin("com.android.application")) {
            throw new InvalidPluginException("'com.android.application' plugin must be applied", null)
        }
        this.extension = project.extensions.create('appVersion', AutoAppVersionExtension, project)

        def version = project.gradle.gradleVersion
        println("current gradle version: " + version)

        def split = version.split("\\.")
        if (split == null || split.length < 2) {
            return
        }

        def GradleMajorVersion = Integer.valueOf(split[0])
        if ((GradleMajorVersion >= 6 && Integer.valueOf(split[1]) >= 5) || GradleMajorVersion >= 7) {
            println("gradle version more than 6.5.0")
            return
        }

        project.afterEvaluate {
            project.tasks.configureEach { task ->
                if (task.name ==~ assembleRegex) {
                    String variantName = task.name.replace("assemble", "").toLowerCase()
                    project.android.applicationVariants.configureEach { BaseVariant variant ->
                        if (variantName == variant.name.toLowerCase()) {
                            addTasks(variant)
                        }
                    }
                }
            }
        }
    }

    void addTasks(BaseVariant variant) {
        revisionNumber = getRevisionNumber()
        variant.outputs.each { output ->
            customVersionName = getVersionName(this.extension, output.versionCode) + "_${output.name}"
            versionCode = revisionNumber + output.versionCode
            println("versionName: ${customVersionName} versionCode: ${versionCode} revisionNumber: ${revisionNumber}")
            output.versionNameOverride = customVersionName
            output.versionCodeOverride = versionCode
        }

        variant.outputs.all { output -> outputFileName = "${variant.getApplicationId()}_${customVersionName}.apk"
        }
    }

    private String getVersionName(AutoAppVersionExtension extension, int versionCode) {
        extension.appMajor = extension.appMajor == null ? '1' : extension.appMajor
        extension.appMinor = extension.appMinor == null ? '1' : extension.appMinor
        String version = 'v' + extension.appMajor + '.' + extension.appMinor + '.' + (getRevisionNumber() + versionCode)
        String today = new Date().format('yyMMdd')
        String time = new Date().format('HHmmss')
        if (extension.isDebug) {
            return version + ".$today." + getRevisionDescription() + '_' + getBranchName() + getStatus() + '.debug'
        }
        return version + ".$today." + getRevisionDescription() + '_' + getBranchName() + getStatus()
    }

    private String getRevisionDescription() {
        def result = getExecResult("git", "rev-parse", "--short", "HEAD")
        return (result == null || result.size() == 0) ? new Date().format("yyMMdd") : result
    }

    private int getRevisionNumber() {
        if (!this.extension.addCommitCount) {
            return 0
        }
        def result = getExecResult("git", "rev-list", "--count", "HEAD")
        if (result == null) {
            return 0
        }
        return result.toInteger()
    }

    private String getBranchName() {
        def result = getExecResult("git", "symbolic-ref", "--short", "-q", "HEAD")
        if (result == null) {
            return 0
        }
        return result
    }

    private String getStatus() {
        def result = getExecResult("git", "status", "--short")
        if (result == null || result.size() == 0) {
            return ''
        }
        return '_modify'
    }

    private String getExecResult(String... args) {
        def count = new ByteArrayOutputStream()
        def error = new ByteArrayOutputStream()
        def action = new Action<ExecSpec>() {
            @Override
            void execute(ExecSpec execSpec) {
                execSpec.workingDir("./")
                execSpec.commandLine(args)
                execSpec.setStandardOutput(count)
                execSpec.setErrorOutput(error)
            }
        }
        try {
            def exec = this.project.exec(action)
            if (exec.exitValue != 0) {
                return null
            }
            return count.toString('UTF-8').trim()
        } catch (Exception e) {
            this.project.logger.error(e.getMessage())
        }
        return null
    }
}
