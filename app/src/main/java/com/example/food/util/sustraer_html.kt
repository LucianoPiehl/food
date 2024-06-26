package com.example.food.util

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class sustraer_html {
    fun removeIngredientGrid(html: String): String {
        val doc = Jsoup.parse(html)
        val gridElements = doc.select("#spoonacular-ingredient-vis-grid").first()
        gridElements?.children()?.forEach { setMargin(it) }
        centerElement(gridElements)
        gridElements?.remove()
        return gridElements?.outerHtml() ?: ""
    }

    fun removeIngredientList(html: String): String {
        val doc = Jsoup.parse(html)
        val listElements = doc.select("#spoonacular-ingredient-vis-list").first()
        listElements?.children()?.forEach { setMargin(it) }
        centerElement(listElements)
        listElements?.remove()
        return listElements?.outerHtml() ?: ""
    }

    private fun setMargin(element: Element) {
        element.attr("style", element.attr("style") + "; margin: 10px;")
    }

    private fun centerElement(element: Element?) {
        element?.attr("style", element.attr("style") + "; display: flex; justify-content: center;")
    }
}
