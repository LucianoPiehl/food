package com.example.food.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import com.example.food.R

class CustomTextWatcher(
    private val emailEditText: EditText,
    private val contraseniaEditText: EditText,
    private val button: Button,
    private val onTextChanged: (Boolean) -> Unit
) : TextWatcher {

    init {
        // Llamamos onTextChanged inicialmente para configurar el estado inicial del botón
        onTextChanged(false)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        val emailText = emailEditText.text.toString()
        val contraseniaText = contraseniaEditText.text.toString()
        val bothFieldsHaveText = emailText.isNotEmpty() && contraseniaText.isNotEmpty()

        // Llama a configure con el botón pasado como parámetro
        button.configure(bothFieldsHaveText, R.color.red, R.color.grey, R.color.white)

        // Llama a onTextChanged pasando el boton de regitro como parametro
    }


    override fun afterTextChanged(s: Editable?) {}

}
