package other.mviSetup.src.app_package

import com.android.tools.idea.wizard.template.ProjectTemplateData

fun someActivity(
        packageName: String,
        entityName: String,
        layoutName: String,
        projectData: ProjectTemplateData
) = """package $packageName

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import ${projectData.applicationPackage}.R;

class ${entityName}sActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.${layoutName.toLowerCase()})
    }
}
"""
fun someActivityLayout(
        packageName: String,
        entityName: String) = """<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="${packageName}.${entityName}sActivity">

</androidx.constraintlayout.widget.ConstraintLayout>
"""