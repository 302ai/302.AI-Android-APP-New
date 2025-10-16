package com.newAi302.app.utils

import android.content.Context
import android.util.Log
//import com.air4.chinesetts.dispatcher.OnTtsStateListener
//import com.air4.chinesetts.dispatcher.TtsStateDispatcher
//import com.air4.chinesetts.tts.TtsManager


object TtsManagerUtils {

    fun initTts(context:Context){
//        TtsManager.getInstance().init(context)
//
//        TtsStateDispatcher.getInstance().addListener(object : OnTtsStateListener {
//            override fun onTtsReady() {
//                Log.e("ceshi","tts贮备好了")
//            }
//
//            override fun onTtsStart(text: String) {
//                // 空实现（根据需要添加逻辑）
//            }
//
//            override fun onTtsStop() {
//                // 空实现（根据需要添加逻辑）
//            }
//        })

    }

    fun TtsSpeak(inputText:String){
        //TtsManager.getInstance().speak(inputText, 1f, true)
    }

    fun TtsStop(){
        //TtsManager.getInstance().stopTts()
    }





}