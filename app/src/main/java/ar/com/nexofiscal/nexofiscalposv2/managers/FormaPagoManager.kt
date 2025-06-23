package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toFormaPagoEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.FormaPago
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object FormaPagoManager {
    private const val TAG = "FormaPagoManager"
    private const val ENDPOINT_FORMAS_PAGO = "/api/formas_pagos"

    fun obtenerFormasPago(
        context: Context,
        headers: MutableMap<String?, String?>?,
        callback: FormaPagoListCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listType = object : com.google.gson.reflect.TypeToken<MutableList<FormaPago?>?>() {}.type
                val allItems = PaginationManager.fetchAllPages<FormaPago>(ENDPOINT_FORMAS_PAGO, headers, listType)

                val dao = AppDatabase.getInstance(context.applicationContext).formaPagoDao()
                val entities = allItems.toFormaPagoEntityList()
                entities.forEach { entity -> dao.insert(entity) }
                Log.d(TAG, "${entities.size} formas de pago guardadas/actualizadas en la BD.")

                withContext(Dispatchers.Main) {
                    callback.onSuccess(allItems.toMutableList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener todas las páginas de formas de pago: ", e)
                withContext(Dispatchers.Main) {
                    callback.onError(e.message)
                }
            }
        }
    }

    interface FormaPagoListCallback {
        fun onSuccess(formasPago: MutableList<FormaPago?>?)
        fun onError(errorMessage: String?)
    }
}