package com.aistudio.qrgenerator.kmpzqr

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aistudio.qrgenerator.kmpzqr.data.AppDatabase
import com.aistudio.qrgenerator.kmpzqr.data.QrItemEntity
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryWindowInstrumentedTest {

    private lateinit var application: Application
    private lateinit var database: AppDatabase

    @Before
    fun setUp() = runBlocking {
        application = InstrumentationRegistry.getInstrumentation()
            .targetContext.applicationContext as Application
        database = AppDatabase.getDatabase(application)
        database.qrDao().clearHistory(isScan = false)
        database.qrDao().clearHistory(isScan = true)

        repeat(150) { index ->
            database.qrDao().insertQrItem(
                QrItemEntity(
                    type = "TEXT",
                    title = "History $index",
                    subtitle = "Bounded history test",
                    rawContent = "payload-$index",
                    timestamp = index.toLong(),
                    isScan = false
                )
            )
        }
    }

    @After
    fun tearDown() = runBlocking {
        database.qrDao().clearHistory(isScan = false)
        database.qrDao().clearHistory(isScan = true)
    }

    @Test
    fun historyWindow_loads100ThenExpandsWithoutDeletingStoredRows() = runBlocking {
        val viewModel = MainViewModel(application)

        val total = withTimeout(5_000) {
            viewModel.historyTotalCount.filter { it == 150 }.first()
        }
        assertEquals(150, total)

        val firstWindow = withTimeout(5_000) {
            viewModel.historyItems.filter { it.size == 100 }.first()
        }
        assertEquals(100, firstWindow.size)
        assertEquals("payload-149", firstWindow.first().rawContent)

        viewModel.loadMoreHistory()

        val expanded = withTimeout(5_000) {
            viewModel.historyItems.filter { it.size == 150 }.first()
        }
        assertEquals(150, expanded.size)
        assertEquals("payload-0", expanded.last().rawContent)

        val storedCount = withTimeout(5_000) {
            viewModel.historyTotalCount.filter { it == 150 }.first()
        }
        assertEquals(150, storedCount)
    }
}
