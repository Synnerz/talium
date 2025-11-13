package com.github.synnerz.talium

import net.fabricmc.api.ClientModInitializer
import org.slf4j.LoggerFactory

object Talium : ClientModInitializer {
    private val logger = LoggerFactory.getLogger("talium")

	override fun onInitializeClient() {
		logger.info("Initialized Synnerz/Talium library")
	}
}