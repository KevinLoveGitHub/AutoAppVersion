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
 * 日期：2019-08-13
 */
class AutoAppVersionPlugin implements Plugin<Project> {
    private AutoAppVersionExtension extension
    private Logger logger
    private Project project
    private String versionName
    private String revisionNumber
    private int versionCode

    @Override
    void apply(Project project) {
        this.project = project
        logger = Logger.getLogger("")
        if (!project.plugins.hasPlugin("com.android.application")) {
            throw new InvalidPluginException("'com.android.application' plugin must be applied", null)
        }
        extension = project.extensions.create('appVersion', AutoAppVersionExtension)
        project.afterEvaluate {
            project.android.applicationVariants.all { BaseVariant variant ->
                addTasks(variant)
            }
        }
    }

    void addTasks(BaseVariant variant) {
        revisionNumber = getRevisionNumber()
        variant.outputs.each { output ->
            versionName = getVersionName(this.extension, output.versionCode) + "_${output.name}"
            versionCode = revisionNumber + output.versionCode
            println("versionName: ${versionName} versionCode: ${versionCode} revisionNumber: ${revisionNumber}")
            output.versionNameOverride = versionName
            output.versionCodeOverride = versionCode
        }

        variant.outputs.all { output ->
            outputFileName = "${variant.getApplicationId()}_${versionName}.apk"
        }
    }

    private String getVersionName(AutoAppVersionExtension extension, int versionCode) {
        extension.appMajor = extension.appMajor == null ? '1' : extension.appMajor
        extension.appMinor = extension.appMinor == null ? '1' : extension.appMinor
        String version = 'v' + extension.appMajor +
                '.' + extension.appMinor +
                '.' + (getRevisionNumber() + versionCode)
        String today = new Date().format('yyMMdd')
        String time = new Date().format('HHmmss')
        if (extension.isDebug) {
            return version + ".$today." + getRevisionDescription() + '_' + getBranchName() + '.debug'
        }
        return version + ".$today." + getRevisionDescription() + '_' + getBranchName()
    }

    private String getRevisionDescription() {
        String desc = 'git describe --always'.execute().getText().trim()
        def result = getExecResult("git", "describe", "--always")
        return (result == null || result.size() == 0) ? new Date().format("yyMMdd") : result.substring(result.size() - 6)
    }

    private int getRevisionNumber() {
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
