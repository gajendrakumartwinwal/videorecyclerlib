package com.example.kotlin.recyclerview

import android.support.annotation.IntDef
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy


@IntDef(
    ViewTemplate.SIMPLE,
    ViewTemplate.VIDEO
)
@Retention(RetentionPolicy.SOURCE)
annotation class ViewTemplate {
    companion object {
        const val SIMPLE = 0
        const val VIDEO = 1
    }
}
