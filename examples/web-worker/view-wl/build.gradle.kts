plugins {
    alias(libs.plugins.kfc.application)
}

dependencies {
    jsMainImplementation(libs.wrappers.browser)
    jsMainImplementation(projects.examples.webWorker.entity)
    jsMainModule(projects.examples.webWorker.workerWl)
}
