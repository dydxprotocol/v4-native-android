package exchange.dydx.platformui.settings

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class PlatformUISettings(
    @SerializedName("sections")
    val sections: List<Section>
) {
    @Serializable
    data class Option(
        @SerializedName("text")
        val text: String?,
        @SerializedName("value")
        val value: String?
    )

    @Serializable
    data class Field(
        @SerializedName("field")
        val field: String?,
        @SerializedName("type")
        val type: String?,
        @SerializedName("optional")
        val optional: Boolean?,
        @SerializedName("options")
        val options: List<Option>?
    )

    @Serializable
    data class TextField(
        @SerializedName("text")
        val text: String
    )

    @Serializable
    data class SectionField(
        @SerializedName("title")
        val title: TextField?,
        @SerializedName("text")
        val text: TextField?,
        @SerializedName("link")
        val link: TextField?,
        @SerializedName("field")
        val field: Field?
    )

    @Serializable
    data class Section(
        @SerializedName("title")
        val title: String?,
        @SerializedName("input")
        val input: String?,
        @SerializedName("fields")
        val fields: List<SectionField>?
    )

    companion object {
        fun loadFromString(jsonString: String): PlatformUISettings {
            return PlatformUISettings(sections = Gson().fromJson(jsonString, Array<Section>::class.java).toList())
        }

        fun loadFromAssets(context: Context, fileName: String): PlatformUISettings {
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            return loadFromString(jsonString)
        }

        val preview = loadFromString(
            """
            [
                {
                    "title": "title",
                    "input": "input",
                    "fields": [
                        {
                            "title": {
                                "text": "title"
                            },
                            "text": {
                                "text": "text"
                            },
                            "link": {
                                "text": "https://dydx.exchange"
                            },
                            "field": {
                                "field": "field",
                                "type": "type",
                                "optional": true,
                                "options": [
                                    {
                                        "text": "text",
                                        "value": "value"
                                    }
                                ]
                            }
                        }
                    ]
                }
            ]
            """.trimIndent(),
        )
    }
}
