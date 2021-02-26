package com.test.worker

import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

fun View(): HTMLElement {
    val container = document.createElement("div")
        .unsafeCast<HTMLDivElement>()

    container.style.apply {
        width = "100%"
        height = "100%"
    }

    return container
}
