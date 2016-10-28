/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.jetbrains.kotlin.statistics

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import org.jetbrains.kotlin.log.KotlinLogger

object KotlinStatisticsUpdater {
    
    private var updated = false
    
    @Synchronized fun updateStatistics() {
        if (updated) return
        sendRequest()
    }
    
    private fun sendRequest() {
        val url = URL("https://plugins.jetbrains.com/netbeans-plugins/kotlin/last")//this url doesn't exist yet
        val connection = url.openConnection()
        if (connection is HttpURLConnection) {
            try {
                connection.connect()
                updated = true
                KotlinLogger.INSTANCE.logInfo("Statistics updated")
            } catch (e: IOException) {
                KotlinLogger.INSTANCE.logException("Couldn't update statistics", e)
            } finally {
                connection.disconnect()
            }
        }
    }
    
}