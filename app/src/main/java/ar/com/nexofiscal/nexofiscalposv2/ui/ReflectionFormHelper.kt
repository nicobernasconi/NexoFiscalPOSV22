// ReflectionFormHelper.kt
package ar.com.nexofiscal.nexofiscalposv2.ui

import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

object ReflectionFormHelper {

    inline fun <reified T : Any> generateFieldsFrom(
        instance: T? = null
    ): List<FormField> {
        val kClass: KClass<T> = T::class
        return kClass.memberProperties.map { prop ->
            val name  = prop.name
            val label = name.replaceFirstChar { it.uppercase() }
            val value = instance?.let { prop.getter.call(it) }

            when (prop.returnType.classifier) {
                String::class -> FormField.Text(
                    fieldKey   = name,
                    fieldLabel = label,
                    initial    = value as? String ?: ""
                )

                Int::class, Double::class, Float::class, Long::class -> FormField.Number(
                    fieldKey   = name,
                    fieldLabel = label,
                    initial    = (value?.toString() ?: "")
                )

                Boolean::class -> FormField.Checkbox(
                    fieldKey   = name,
                    fieldLabel = label,
                    initial    = value as? Boolean ?: false
                )

                else -> {
                    // Si es un enum, lo tratamos como Dropdown
                    val clazz = prop.returnType.classifier
                    if (clazz is KClass<*> && clazz.java.isEnum) {
                        @Suppress("UNCHECKED_CAST")
                        val enumConstants = (clazz.java.enumConstants as Array<Any>)
                            .map { it.toString() }
                        FormField.Dropdown(
                            fieldKey   = name,
                            fieldLabel = label,
                            options    = enumConstants,
                            initial    = value?.toString()
                        )
                    } else {
                        // fallback a texto
                        FormField.Text(
                            fieldKey   = name,
                            fieldLabel = label,
                            initial    = value?.toString() ?: ""
                        )
                    }
                }
            }
        }
    }

    inline fun <reified T : Any> mapToEntity(
        values: Map<String, Any?>,
        noinline factory: (Map<String, Any?>) -> T
    ): T = factory(values)
}
