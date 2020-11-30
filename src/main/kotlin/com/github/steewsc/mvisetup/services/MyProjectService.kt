package com.github.steewsc.mvisetup.services

import com.intellij.openapi.project.Project
import com.github.steewsc.mvisetup.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
