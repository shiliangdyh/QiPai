package com.sl.qipai

import android.app.Application
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

/**
 *
 * @author shiliang
 * @version 1.0
 * @date 2019-10-14
 */
object LogUtil {
    val TO_CONSOLE = 0x1
    val TO_SCREEN = 0x10
    val TO_FILE = 0x100
    val FROM_LOGCAT = 0x1000

    private val LOG_MAXSIZE = 2 * 1024 * 1024

    private val LOG_TEMP_FILE = "log.temp"
    private val LOG_LAST_FILE = "log_last.txt"

    private val LOG_LEVEL = Log.VERBOSE

    val DEBUG_ALL = TO_CONSOLE or TO_FILE

    var DEBUG = true
    var mContext:Application? = null

    private val lockObj = Any()
    internal var mDate = Calendar.getInstance()
    internal var mBuffer = StringBuffer()
    internal var mLogStream: OutputStream? = null
    internal var mFileSize: Long = 0

    fun d(tag: String, msg: String) {
        log(tag, msg, DEBUG_ALL, Log.DEBUG)
    }

    private fun logToScreen(tag: String, msg: String, level: Int) {

    }

    private fun logToFile(tag: String, msg: String, level: Int) {
        // 没有android.permission-group.STORAGE权限，则不写文件
        synchronized(lockObj) {
            val outStream = openLogFileOutStream()
            if (outStream != null) {
                try {
                    val d = getLogStr(tag, msg).toByteArray(charset("utf-8"))

                    if (mFileSize < LOG_MAXSIZE) {
                        outStream.write(d)
                        outStream.write("\r\n".toByteArray(charset("utf-8")))
                        outStream.flush()
                        mFileSize += d.size.toLong()
                    } else {
                        closeLogFileOutStream()
                        if (renameLogFile()) {
                            logToFile(tag, msg, level)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }

    private fun getLogStr(tag: String, msg: String): String {
        mDate.setTimeInMillis(System.currentTimeMillis())

        mBuffer.setLength(0)
        mBuffer.append("[")
        mBuffer.append(tag)
        mBuffer.append(" : ")
        mBuffer.append(mDate.get(Calendar.MONTH) + 1)
        mBuffer.append("-")
        mBuffer.append(mDate.get(Calendar.DATE))
        mBuffer.append(" ")
        mBuffer.append(mDate.get(Calendar.HOUR_OF_DAY))
        mBuffer.append(":")
        mBuffer.append(mDate.get(Calendar.MINUTE))
        mBuffer.append(":")
        mBuffer.append(mDate.get(Calendar.SECOND))
        mBuffer.append(":")
        mBuffer.append(mDate.get(Calendar.MILLISECOND))
        mBuffer.append("] ")
        mBuffer.append(msg)

        return mBuffer.toString()
    }

    private fun renameLogFile(): Boolean {
        synchronized(lockObj) {

            val file = File(getLogFolder(), LOG_TEMP_FILE)
            val destFile = File(getLogFolder(), LOG_LAST_FILE)
            if (destFile.exists()) {
                destFile.delete()
            }
            file.renameTo(destFile)
            return if (file.exists()) {
                file.delete()
            } else {
                true
            }
        }
    }

    private fun closeLogFileOutStream() {
        try {
            if (mLogStream != null) {
                mLogStream!!.close()
                mLogStream = null
                mFileSize = 0
            }
        } catch (e: Exception) {

            e.printStackTrace()
        }

    }

    private fun openLogFileOutStream(): OutputStream? {
        if (mLogStream == null && mContext != null) {
            try {

                val file = File(getLogFolder(), LOG_TEMP_FILE)
                if (file.exists()) {
                    mLogStream = FileOutputStream(file, true)
                    mFileSize = file.length()
                } else {

                    mLogStream = FileOutputStream(file)
                    mFileSize = 0
                }
            } catch (e: FileNotFoundException) {

                e.printStackTrace()
            }

        }
        return mLogStream
    }

    private fun getLogFolder(): File {

        var folder: File? = null

        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            folder = mContext!!.getExternalFilesDir(getLogFile())
            if (!folder!!.exists()) {
                folder.mkdirs()
            }
        } else {
            folder = mContext!!.getFilesDir()
            if (!folder!!.exists()) {
                folder.mkdirs()
            }
        }

        Log.d("LogUtil", folder.absolutePath)
        return folder
    }

    private fun getLogFile(): String {
        val packageName = mContext!!.packageName
        val list = packageName.split(".")
        val sb = StringBuilder()
        for (s in list) {
            sb.append("/").append(s)
        }
        return sb.toString()
    }

    fun log(tag: String?, msg: String?, outdest: Int, level: Int) {
        var tag = tag
        var msg = msg
        if (!DEBUG) {
            return
        }

        if (tag == null) {
            tag = "TAG_NULL"
        }
        if (msg == null) {
            msg = "MSG_NULL"
        }

        if (level >= LOG_LEVEL) {

            if (outdest and TO_SCREEN != 0) {
                logToScreen(tag, msg, level)
            }

            if (outdest and TO_FILE != 0) {
                logToFile(tag, msg, level)
            }

            if (outdest and FROM_LOGCAT != 0) {
//                if (mPaintLogThread == null) {
//                    mPaintLogThread = PaintLogThread()
//                    mPaintLogThread.start()
//                }
            }

            if (outdest and TO_CONSOLE != 0) {
                val max_str_length = 2001 - tag.length
                //大于4000时
                while (msg!!.length > max_str_length) {
                    logToConsole(tag, msg.substring(0, max_str_length), level)
                    msg = msg.substring(max_str_length)
                }
                //剩余部分
                logToConsole(tag, msg, level)
            }
        }
    }

    private fun logToConsole(tag: String, msg: String, level: Int) {
        when (level) {
            Log.DEBUG -> Log.d(tag, msg)
            Log.ERROR -> Log.e(tag, msg)
            Log.INFO -> Log.i(tag, msg)
            Log.VERBOSE -> Log.v(tag, msg)
            Log.WARN -> Log.w(tag, msg)
            else -> {
            }
        }
    }
}