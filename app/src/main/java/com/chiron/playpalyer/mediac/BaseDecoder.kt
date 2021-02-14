package com.chiron.playpalyer.mediac

import android.media.MediaCodec
import android.media.MediaFormat
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.lang.Exception
import java.nio.ByteBuffer

abstract class BaseDecoder(private val mFilePath:String):IDecoder{
    private val TAG ="BaseDecoder"
    //----------线程相关数据定义----------------
    /**
     * 解码器是否在工作
     */
    private var mIsRunning = true

    /**
     * 线程等待锁
     */
    private val mLock = Object()

    /**
     * 是否可以进行解码
     */
    private var mReadyForDecode=false
    //------------状态相关数据定义---------------
    /**
     * 音视频解码器
     */
    private var mCodec:MediaCodec?=null

    /**
     * 音视频数据读取
     */
    private var mExtractor:IExtractor?=null

    /**
     * 解码输入缓冲区
     */
    private var mInputBuffers:Array<ByteBuffer>?=null

    /**
     * 解码输出缓冲区
     */
    private var mOutputBuffers:Array<ByteBuffer>?=null

    /**
     * 解码数据信息
     */
    private var mBufferInfo=MediaCodec.BufferInfo()
    private var mState=DecodeState.STOP
    private var mStateListener:IDecoderStateListener?=null
    /**
     * 流数据是否结束
     */
    private var mIsEos=false
    protected var mVideoWidth=0
    protected var mVideoHeight=0
    private var mDuration: Long = 0
    private var mStartPos: Long = 0
    private var mEndPos: Long = 0

    /**
     * 解码流程定义
     */
    override fun run() {
        mState=DecodeState.START
        mStateListener?.let { it.decoderPrepare(this) }

        //【解码步骤：1，初始化，并且启动解码器】
        if(!init()){
            return
        }

        while (mIsRunning){//解码器在工作时做如下操作

            if (mState != DecodeState.START &&
                mState != DecodeState.DECODING &&
                mState != DecodeState.SEEKING
            ) {
                waitDecode()
            }

            //解码器没有在运行或者当前解码器状态已经为停止状态则结束当前的while
            if(!mIsRunning || mState==DecodeState.STOP){
                mIsRunning=false
                break
            }

            //如果数据没有解码完毕，将数据推入解码器解码
            if(!mIsEos){
                //【解码步骤：2，将数据压入解码器的输入缓冲区】
                mIsEos=pushBufferToDecoder()
            }

            //【解码步骤:3，将解码完成的数据从输出缓冲区拉出来】
            val index=pullBufferFromDecoder()
            if(index>=0){
                //【解码步骤:4，渲染】
                render(mOutputBuffer!![index],mBufferInfo)
                //【解码步骤：5，释放缓冲区】
                // 注：第二个参数，是个boolean，命名为render，
                // 这个参数在视频解码时，用于决定是否要将这一帧数据显示出来。
                mCodec!!.releaseOutputBuffer(index,true)
                if(mState==DecodeState.START){
                    mState=DecodeState.PAUSE
                }
            }
            //【解码步骤：6，判断解码是否完成】
            //当sampleSize < 0 时，压入了一个结束标记吗
            //
            //当接收到这个标志后，解码器就知道所有数据已经接收完毕，在所有数据解码完成以后，
            // 会在最后一帧数据加上结束标记信息
            if (mBufferInfo.flags==MediaCodec.BUFFER_FLAG_END_OF_STREAM){
                mState=DecodeState.FINISH
                mStateListener?.decodeFinish(this)
            }
        }
        doneDecode()
        //【解码步骤：7，释放解码器】
        release()
    }

    /**
     * 解码线程进入等待（这里算是等待通知模型的一种实现）
     */
    private fun waitDecode(){
        try{
            if(mState==DecodeState.PAUSE){
                mStateListener?.decoderPause(this)
            }
            synchronized(mLock){
                mLock.wait()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    /**
     * 通知解码线程继续执行
     */
    private fun notifyDecode(){
        synchronized(mLock){
            mLock.notifyAll()
        }
        if(mState==DecodeState.DECODING){
            mStateListener?.decoderRunning(this)
        }
    }

    /**
     * 渲染
     */
    abstract fun render(outputBuffer: ByteBuffer,bufferInfo:MediaCodec.BufferInfo)

    /**
     * 结束解码
     */
    abstract fun doneDecode()

    private fun init():Boolean{
        //1、验证参数是否完整
        if(mFilePath.isEmpty() || !File(mFilePath).exists()){
            Log.w(TAG,"文件路径为空！")
            mStateListener?.decodeError(this,"文件路径为空")
            return false;
        }
        //检查子类参数是否完整
        if(!check()){
            return false
        }
        //2、初始化数据提取器
        mExtractor=initExtractor(mFilePath)
        if(mExtractor==null || mExtractor!!.getFormat()==null){
            return false
        }
        //3、初始化参数
        if(!initParams()){
            return false
        }
        //4、初始化渲染器
        if(!initRender()){
            return false
        }
        //5、初始化解码器
        if(!initDecodec()){
            return false
        }
        return true
    }

    private fun initParams():Boolean{
        try{
            //获取视频的一些参数
            val format=mExtractor!!.getFormat()!!
            mDuration=format.getLong(MediaFormat.KEY_DURATION)/1000
            if(mEndPos==0L){
                mEndPos=mDuration
            }
            initSpaceParams(mExtractor!!.getFormat()!!)
        }catch (e:Exception){
            return false
        }
        return true
    }

    private fun initDecodec():Boolean{
        try{
            //1、根据音视频编码格式初始化解码器
            val type=mExtractor!!.getFormat()!!.getString(MediaFormat.KEY_MIME)
            mCodec= MediaCodec.createDecoderByType(type!!)
            //2、配置解码器
            if(!configDecodec(mCodec!!,mExtractor!!.getFormat()!!)){
                waitDecode()
            }
            //3、启动解码器
            mCodec!!.start()
            //4、获取解码器缓冲区
            mInputBuffers=mCodec?.inputBuffers
            mOutputBuffers=mCodec?.outputBuffers
        }catch (e:Exception){
            return false
        }
        return true
    }

    /**
     * 检查子类参数
     */
    abstract fun check():Boolean

    /**
     * 初始化数据提取器
     */
    abstract fun initExtractor(path:String):IExtractor

    /**
     * 初始化子类特有的参数
     */
    abstract fun initSpaceParams(format:MediaFormat)

    /**
     * 初始化渲染器
     */
    abstract fun initRender():Boolean

    /**
     * 配置解码器
     */
    abstract fun configDecodec(decodec:MediaCodec,format:MediaFormat):Boolean

    private fun pushBufferToDecoder():Boolean{
        //查询是否有可用的缓冲区，返回索引（2000表示等待2000ms，-1表示无限等待）
        var inputBufferIndex = mCodec!!.dequeueInputBuffer(2000)
        var isEndOfStream=false
        if(inputBufferIndex>=0){
            //通过缓冲索引 inputBufferIndex 获取可用的缓冲区，并使用Extractor提取待解码数据，填充到缓冲区中。
            val inputBuffer = mInputBuffers!![inputBufferIndex]
            val sampleSize=mExtractor!!.readBuffer(inputBuffer)
            /**
             * 注意：如果SampleSize返回-1，说明没有更多的数据了。
             * 这个时候，queueInputBuffer的最后一个参数要传入结束标记MediaCodec.BUFFER_FLAG_END_OF_STREAM。
             */
            if(sampleSize<0){
                mCodec!!.queueInputBuffer(inputBufferIndex,0,0,0,MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                isEndOfStream=true
            }else{
                //调用queueInputBuffer将数据压入解码器(第四个参数代表当前buffer的时间戳)
                mCodec!!.queueInputBuffer(inputBufferIndex,0,sampleSize,mExtractor!!.getCurrentTimeStamp(),0)
            }
        }
        return isEndOfStream
    }

    private fun pullBufferFromDecoder():Int{
        //查询是否有解码完成的数据，index>=0的时候，表示数据有效，并且index为缓冲区索引
        //mBufferInfo用于获取数据帧信息，1000为等待时间（-1为无限等待）
        var index=mCodec!!.dequeueOutputBuffer(mBufferInfo,1000)
        when(index){
            //输出格式改变了
            MediaCodec.INFO_OUTPUT_FORMAT_CHANGED->{}
            //没有缓冲数据，等会儿再来
            MediaCodec.INFO_TRY_AGAIN_LATER->{}
            //输出缓冲改变了
            MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED->{
                mOutputBuffers=mCodec!!.outputBuffers
            }
            else->{
                return index
            }
        }
        return -1
    }

    private fun release(){
        try{
            Log.i(TAG,"解码停止，释放解码器")
            mState=DecodeState.STOP
            mIsEos=true
            mExtractor?.stop()
            mCodec?.stop()
            mCodec?.release()
            mStateListener?.decodeDestroy(this)
        }catch (e:Exception){

        }
    }
}