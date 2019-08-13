package com.kangxiaoguang

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test
/**
 * 说明：
 * 作者：Kevin
 * 日期：2019-08-13
 */
class AutoAppVersionPluginTest {
    @Test
    public void greeterPluginAddsGreetingTaskToProject() {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'com.kangxiaoguang.auto-app-version'
        def task = project.tasks.hello
        task.getActions().forEach({ action -> action.execute(task) })
    }
}
