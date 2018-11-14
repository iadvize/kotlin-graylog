package com.iadvize.kotlingraylog

import android.content.Context
import android.content.SharedPreferences
import com.securepreferences.SecurePreferences
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

/**
 * Created by Yann Coupé on 13/11/2018.
 * Copyright © 2018 iAdvize. All rights reserved.
 */
typealias LogValues = JSONObject

object Graylog {

    private const val LOG_QUEUE_KEY = "GRAYLOG_LOG_QUEUE_KEY"
    // Stock 1000 logs max in the queue.
    private const val LOG_QUEUE_MAX = 1000
    // Send 10 logs max at the same time.
    private const val LOG_SEND_MAX = 10
    // Check the queue every 5mn.
    private const val LOG_INTERVAL = 5 * 60L

    private val jsonHeader = MediaType.parse("application/json; charset=utf-8")
    private val client = OkHttpClient.Builder().build()
    private val scheduler = Executors.newScheduledThreadPool(1) as ScheduledThreadPoolExecutor

    private lateinit var securedPreferences: SharedPreferences
    private lateinit var url: URL

    fun init(context: Context, url: URL) {
        this.securedPreferences = SecurePreferences(context.applicationContext, Graylog::class.java.name, null)
        this.url = url
        start()
    }

    /**
     * Send the error log in the Graylog server.
     *
     * @param attributes: Json which contains all informations about the error.
     */
    fun log(attributes: LogValues) {
        if(this::securedPreferences.isInitialized) {
            GlobalScope.async {
                val body = RequestBody.create(jsonHeader, attributes.toString())
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        save(attributes)
                    }
                    override fun onResponse(call: Call, response: Response) {}
                })
            }
        } else {
            Logger.getLogger(Graylog::class.java.name).warning("${Graylog::class.java.name} is not initialized. You must call the init() method.")
            save(attributes)
        }
    }

    /**
     * Save log in JSON in the log preferences which contains all logs not sent.
     *
     * @param attributes: Json which contains all informations about the error.
     */
    internal fun save(attributes: LogValues) {
        // Get last version of preference.
        val queue = securedPreferences.getStringSet(LOG_QUEUE_KEY, mutableSetOf())
        queue?.let {
            if(it.size < LOG_QUEUE_MAX) {
                it.add(attributes.toString())
                securedPreferences.edit().putStringSet(LOG_QUEUE_KEY, it).apply()
            }
        }
    }

    /**
     * Check if we have log in memory, if yes, try to send it. But only the first 10.
     */
    private fun checkAndSend() {
        val queue = securedPreferences.getStringSet(LOG_QUEUE_KEY, mutableSetOf())
        queue?.let {
            if(it.isNotEmpty()) {
                // Transform set in a list.
                val queueList = it.toList()

                // Get the first logs to send.
                val listToSend = queueList.subList(0, if(queueList.size > LOG_SEND_MAX) LOG_SEND_MAX else it.size)

                // Remove sent logs in queue.
                for(log in listToSend) {
                    it.remove(log)
                    log(LogValues(log))
                }
                securedPreferences.edit().putStringSet(LOG_QUEUE_KEY, it).apply()
            }
        }
    }

    /**
     * Clear queue.
     */
    fun clearQueue() {
        if(this::securedPreferences.isInitialized) securedPreferences.edit().remove(LOG_QUEUE_KEY).apply()
    }

    /**
     * Get queue saved in secured preferences.
     */
    fun getQueue(): MutableSet<String>? =
        if(this::securedPreferences.isInitialized) securedPreferences.getStringSet(LOG_QUEUE_KEY, mutableSetOf())
        else null

    /**
     * Start the scheduler, and a periodic task
     */
    private fun start() {
        val periodicCronTask = Runnable { checkAndSend() }
        scheduler.scheduleAtFixedRate(periodicCronTask, 0, LOG_INTERVAL, TimeUnit.SECONDS)
    }
}