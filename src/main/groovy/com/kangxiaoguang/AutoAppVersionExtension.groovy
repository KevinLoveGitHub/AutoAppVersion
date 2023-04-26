package com.kangxiaoguang

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.process.ExecSpec
/**
 * 说明：
 * 作者：Kevin
 * 日期：2019-08-13
 */
class AutoAppVersionExtension {
    String appMajor
    String appMinor
    int versionCode
    String versionName
    boolean addCommitCount = true
    boolean isDebug
    private Project mProject

    AutoAppVersionExtension(Project project) {
        this.mProject = project
    }

    int customVersionCode(BaseVariantOutput output) {
        if (!this.addCommitCount && output != null) {
            return output.versionCode
        }
        int code = this.versionCode > 0 ? this.versionCode : getRevisionNumber() + output.versionCode
        return code;
    }

    String customVersionName(BaseVariantOutput output) {
        String name = this.versionName !=null && !this.versionName.trim().isEmpty() ? this.versionName :
                getCustomVersionName(output)

        if (output != null) {
            name += "_${output.name}"
        }
        println("customVersionName: " + name)
        return name;
    }

    String fileName(BaseVariant base, BaseVariantOutput output) {
        String name = this.versionName !=null && !this.versionName.trim().isEmpty() ? this.versionName :
                getCustomVersionName(output) + "_${output.name}"

        name = "${base.getApplicationId()}_${name}.apk"
        return name;
    }

    private String getCustomVersionName(BaseVariantOutput output) {
        this.appMajor = this.appMajor == null ? '1' : this.appMajor
        this.appMinor = this.appMinor == null ? '1' : this.appMinor
        String version = 'v' + this.appMajor +
                '.' + this.appMinor +
                '.' + (customVersionCode(output))
        String today = new Date().format('yyMMdd')
        String time = new Date().format('HHmmss')
        if (this.isDebug) {
            return version + ".$today." + getRevisionDescription() + '_' + getBranchName() + getStatus() + '.debug'
        }
        return version + ".$today." + getRevisionDescription() + '_' + getBranchName() + getStatus()
    }

    private String getRevisionDescription() {
        def result = getExecResult("git", "rev-parse", "--short", "HEAD")
        return (result == null || result.size() == 0) ? new Date().format("yyMMdd") : result
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

    private int getRevisionNumber() {
        if (!this.addCommitCount) {
            return 0
        }
        def result = getExecResult("git", "rev-list", "--count", "HEAD")
        if (result == null) {
            return 0
        }
        return result.toInteger()
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
            def exec = this.mProject.exec(action)
            if (exec.exitValue != 0) {
                return null
            }
            return count.toString('UTF-8').trim()
        } catch (Exception e) {
            this.mProject.logger.error(e.getMessage())
        }
        return null
    }
}
