package com.iadvize.kotlingraylog

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.net.URL

/**
 * Created by Yann Coupé on 18/04/2018.
 * Copyright © 2017 iAdvize. All rights reserved.
 */

@RunWith(RobolectricTestRunner::class)
class GraylogTest {

    @Before
    fun init() {
        Graylog.init(ApplicationProvider.getApplicationContext<Context>(), URL("https://www.fake-endpoint.com"))
        Graylog.clearQueue()
    }

    /**
     * Make sure that when you save a log and the GraylogManager was initialized, it is saved.
     */
    @Test
    fun save_whenInitialized() {
        var queue = Graylog.getQueue()
        assertNotNull(queue)
        assertEquals(queue?.size, 0)

        val json = LogValues()
        json.put("name", "value")
        Graylog.save(json)

        queue = Graylog.getQueue()
        assertNotNull(queue)
        queue?.let {
            assertEquals(queue.size, 1)
        }
    }

    /**
     * Make sure that when you clear queue, it is cleared.
     */
    @Test
    fun clearQueue_whenInitialized() {
        save_whenInitialized()
        var queue = Graylog.getQueue()
        assertNotNull(queue)
        queue?.let {
            assertEquals(it.size, 1)
        }

        Graylog.clearQueue()

        queue = Graylog.getQueue()
        assertNotNull(queue)
        assertEquals(queue?.size, 0)
    }
}