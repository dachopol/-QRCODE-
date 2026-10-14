package com.aistudio.qrgenerator.kmpzqr

import com.aistudio.qrgenerator.kmpzqr.util.GalleryBitmapDecoder
import org.junit.Assert.assertEquals
import org.junit.Test

class GalleryBitmapDecoderTest {
    @Test
    fun sampleSize_staysOneForImagesWithinGuard() {
        assertEquals(1, GalleryBitmapDecoder.calculateInSampleSize(1080, 1920))
        assertEquals(1, GalleryBitmapDecoder.calculateInSampleSize(2048, 2048))
    }

    @Test
    fun sampleSize_isPowerOfTwoAndBoundsLargeImages() {
        val sample = GalleryBitmapDecoder.calculateInSampleSize(8192, 6144)
        assertEquals(4, sample)
        assertEquals(2048, 8192 / sample)
        assertEquals(1536, 6144 / sample)
    }
}
