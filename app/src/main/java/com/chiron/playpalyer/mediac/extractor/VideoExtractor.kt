package com.chiron.playpalyer.mediac.extractor

import android.media.MediaFormat
import com.chiron.playpalyer.mediac.IExtractor
import com.chiron.playpalyer.mediac.MMExtractor
import java.nio.ByteBuffer

/**
 * 视频提取器
 */
class VideoExtractor(path:String):IExtractor {
    private val mMediaExtractor=MMExtractor(path)

    override fun getFormat(): MediaFormat? {
        return mMediaExtractor.getVideoFormat()
    }

    override fun readBuffer(byteBuffer: ByteBuffer): Int {
        return mMediaExtractor.readBuffer(byteBuffer)
    }

    override fun getCurrentTimeStamp(): Long {
        return mMediaExtractor.getCurrentTimeStamp()
    }

    override fun getSampleFlag(): Int {
        return mMediaExtractor.getSampleFlag()
    }

    override fun seek(pos: Long): Long {
        return mMediaExtractor.seek(pos)
    }

    override fun setStartPos(pos: Long) {
        return mMediaExtractor.setStartPos(pos)
    }

    override fun stop() {
        mMediaExtractor.stop()
    }
}