package com.example.food.util

import android.widget.Button
import androidx.core.content.ContextCompat

fun Button.configure(isEnabled: Boolean, enabledColor: Int, disabledColor: Int, textColor: Int) {
    this.isEnabled = isEnabled
    this.setBackgroundColor(ContextCompat.getColor(context, if (isEnabled) enabledColor else disabledColor))
    this.setTextColor(ContextCompat.getColor(context, textColor))
}
