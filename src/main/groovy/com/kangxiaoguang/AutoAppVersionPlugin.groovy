package com.kangxiaoguang

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 说明：
 * 作者：Kevin
 * 日期：2019-08-13
 */
class AutoAppVersionPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def extension = project.extensions.create('version', AutoAppVersionExtension)
        project.task('hello') {
            doLast {
                def versionName = getVersionName(extension)
                project.ext.versionName = versionName
                println("版本号：${ versionName}")
            }
        }
    }

    private static String getVersionName(AutoAppVersionExtension extension) {
        String version = 'v' + extension.appMajor +
                '.' + extension.appMinor +
                '.' + getRevisionNumber()
        String today = new Date().format('yyMMdd')
        String time = new Date().format('HHmmss')
        if (extension.isDebug) {
            return version + ".$today." + getRevisionDescription() + '.debug'
        }
        return version + ".$today." + getRevisionDescription()
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
}
