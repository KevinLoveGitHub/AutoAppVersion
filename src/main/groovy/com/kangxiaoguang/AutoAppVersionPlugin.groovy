package com.kangxiaoguang

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.InvalidPluginException

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
        def versionName = getVersionName(this.extension)
        def revisionNumber = getRevisionNumber()
        println('versionName: ' + versionName + ' revisionNumber: ' + revisionNumber)
        variant.outputs.each { output ->
            output.versionNameOverride = versionName
            output.versionCodeOverride = revisionNumber
        }

        variant.outputs.all { output ->
            outputFileName = "${variant.getApplicationId()}_${versionName}.apk"
        }
    }

    private static String getVersionName(AutoAppVersionExtension extension) {
        extension.appMajor = extension.appMajor == null ? '1' : extension.appMajor
        extension.appMinor = extension.appMinor == null ? '1' : extension.appMinor
        String version = 'v' + extension.appMajor +
                '.' + extension.appMinor +
                '.' + getRevisionNumber()
        String today = new Date().format('yyMMdd')
        String time = new Date().format('HHmmss')
        if (extension.isDebug) {
            return version + ".$today." + getRevisionDescription() + '_' + getBranchName() + '.debug'
        }
        return version + ".$today." + getRevisionDescription() + '_' + getBranchName()
    }

    private static String getRevisionDescription() {
        String desc = 'git describe --always'.execute().getText().trim()
        return (desc == null || desc.size() == 0) ? new Date().format("yyMMdd") : desc.substring(desc.size() - 6)
    }

    private static int getRevisionNumber() {
        Process process = "git rev-list --count HEAD".execute()
        process.waitFor()
        return process.getText().toInteger()
    }

    private static String getBranchName() {
        return "git symbolic-ref --short -q HEAD".execute().getText().trim()
    }
}
