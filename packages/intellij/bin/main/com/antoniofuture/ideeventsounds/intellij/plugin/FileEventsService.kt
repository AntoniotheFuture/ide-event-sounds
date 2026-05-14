package com.antoniofuture.ideeventsounds.intellij.plugin

import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.diagnostic.logger

class FileEventsService : ApplicationComponent {
    private val logger = logger<FileEventsService>()

    override fun initComponent() {
        logger.info("FileEventsService.initComponent called")
        // 暂时移除 VirtualFileListener，避免 IDE 卡死
        logger.info("FileEventsService: VirtualFileListener disabled to prevent IDE freeze")
    }

    override fun disposeComponent() {
        logger.info("FileEventsService.disposeComponent called")
    }
}