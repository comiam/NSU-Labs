package me.bolshim

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

inline fun <reified T : Any> logger(): Logger = LogManager.getLogger(T::class.java)

