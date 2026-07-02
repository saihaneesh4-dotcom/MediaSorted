package com.example.mediasorted

import androidx.documentfile.provider.DocumentFile

object MediaMatcher {
    fun findFile(root : DocumentFile,
                 fileName : String
    ): DocumentFile? {
        for(child in root.listFiles()){
            if(child.isFile){
                if(child.name == fileName){
                    return child
                }else{
                    findFile(child,fileName)
                }
            }
        }
    }
}