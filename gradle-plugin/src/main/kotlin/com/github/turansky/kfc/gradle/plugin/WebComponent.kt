package com.github.turansky.kfc.gradle.plugin

import com.github.turansky.kfc.gradle.plugin.WebComponent.Property.Type.*
import java.io.Serializable

private const val SOURCE = "this._source"

data class WebComponent(
    private val id: String,
    private val properties: List<Property>,
    private val events: List<String>,
    private val source: String
) : Serializable {
    // language=JavaScript
    fun toCode(sourceModuleName: String): String = """
        |import * as sourceModule from '$sourceModuleName'
        |
        |const SourceElement = sourceModule.$source
        |
        |export class $name extends HTMLElement {
        |   constructor() {
        |     super();
        |     
        |     $SOURCE = new SourceElement()      
        |     const shadow = this.attachShadow({ mode: 'open' })
        |     shadow.appendChild($SOURCE)
        |   }
        |   
        |   ${properties.joinToString("\n\n")}
        |}    
        |
        |customElements.define('$name', $name)
    """.trimMargin()

    private val name: String = source.substringAfterLast(".")
    val sourceRoot: String = source.substringBeforeLast(".")

    data class Property(
        private val name: String,
        private val type: Type
    ) {
        private fun toGetter(): String = """
            get $name() {
                return $SOURCE.$name
            }
        """.trimIndent()

        private fun toSetter(): String = """
            set $name(value) {
                $SOURCE.$name = value
            }
        """.trimIndent()

        override fun toString(): String =
            when (type) {
                RW -> toGetter() + "\n\n" + toSetter()
                RO -> toGetter()
                WO -> toSetter()
            }

        enum class Type {
            RW, RO, WO
        }
    }
}
