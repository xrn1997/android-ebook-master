import com.android.build.gradle.TestedExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidComponentConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val isModule = target.findProperty("isModule")?.toString()?.toBoolean() ?: false
        with(target) {
            with(pluginManager) {
                if (isModule) {
                    apply("xrn1997.android.application")
                    apply("therouter")
                } else {
                    apply("xrn1997.android.library")
                }
            }
            extensions.configure<TestedExtension> {
                sourceSets.named("main") {
                    jniLibs.srcDirs("src/main/jniLibs")
                    if (isModule) {
                        manifest.srcFile("src/main/module/AndroidManifest.xml")
                    } else {
                        manifest.srcFile("src/main/AndroidManifest.xml")
                        java.exclude("debug/**")
                    }
                }
            }
        }
    }
}