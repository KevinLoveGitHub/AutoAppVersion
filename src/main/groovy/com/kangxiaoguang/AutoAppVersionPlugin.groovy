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
        project.task('hello') {
            doLast {
                println 'Hello from the AutoAppVersionPlugin'
            }
        }
    }
}
