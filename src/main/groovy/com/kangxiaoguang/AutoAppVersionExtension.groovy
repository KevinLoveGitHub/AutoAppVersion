package com.kangxiaoguang


import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.process.ExecSpec

import java.util.regex.Matcher
import java.util.regex.Pattern
/**
 * 说明：
 * 作者：Kevin
 * 日期：2019-08-13
 */
class AutoAppVersionExtension {
    String appMajor
    String appMinor
    int versionCode
    int incrementVersionCode
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
        int code = getRevisionNumber() + this.incrementVersionCode
        if (output != null) {
            code += output.versionCode
        }
        code = this.versionCode > 0 ? this.versionCode : code
        return code;
    }

    int getAutoVersionCode() {
        int code = getRevisionNumber() + this.incrementVersionCode
        return code;
    }

    String customVersionName(BaseVariantOutput output) {
        String name = this.versionName != null && !this.versionName.trim().isEmpty() ? this.versionName :
                getCustomVersionName(output)

        if (output != null) {
            name += "_${output.name}"
        }
        println("customVersionName: " + name)
        return name;
    }

    String getAutoVersionName() {
        String name = getCustomVersionName(null)
        name = name + "_" + getCurrentFlavor()
        return name;
    }

    private String getCurrentFlavor() {
        String taskRequestsStr = this.mProject.gradle.startParameter.taskRequests.toString()
        Pattern pattern
        if (taskRequestsStr.contains("assemble")) {
            pattern = Pattern.compile("assemble(\\w+)?(Release|Debug)")
        } else {
            pattern = Pattern.compile("bundle(\\w+)?(Release|Debug)")
        }

        Matcher matcher = pattern.matcher(taskRequestsStr)
        String flavor
        if (matcher.find()) {
            if (matcher.group(1)) {
                flavor = matcher.group(1).toLowerCase() + "-" + matcher.group(2).toLowerCase()
            } else {
                flavor = matcher.group(2).toLowerCase()
            }
        } else {
            println("NO FLAVOR FOUND")
            flavor = ""
        }
        return flavor
    }

    String fileName(BaseVariant base, BaseVariantOutput output) {
        String name = this.versionName != null && !this.versionName.trim().isEmpty() ? this.versionName :
                getCustomVersionName(output) + "_${output.name}"

        name = "${base.getApplicationId()}_${name}.apk"
        return name;
    }

    public String getAutoFileName(BaseVariant base) {
        return "${base.getApplicationId()}_${getAutoVersionName()}.apk"
    }

    public String getAutoFileName(String applicationId) {
        return "${applicationId}_${getAutoVersionName()}.apk"
    }

    private String getCustomVersionName(BaseVariantOutput output) {
        this.appMajor = this.appMajor == null ? '1' : this.appMajor
        this.appMinor = this.appMinor == null ? '1' : this.appMinor
        String version = 'v' + this.appMajor + '.' + this.appMinor + '.' + (customVersionCode(output))
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
