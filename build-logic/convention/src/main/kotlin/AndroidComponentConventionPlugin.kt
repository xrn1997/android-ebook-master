import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidComponentConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        //todo 处理login模块
        val isModule = target.findProperty("isModule").toString().toBoolean()
        println(target.name)
        with(target) {
            if (isModule) {
                with(pluginManager) {
                    apply("xrn1997.android.application")
                    apply("therouter")
                }
            } else {
                with(pluginManager) {
                    apply("xrn1997.android.library")
                }
            }
        }
    }

}