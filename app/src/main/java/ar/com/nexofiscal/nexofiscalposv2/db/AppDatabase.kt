// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/AppDatabase.kt
package ar.com.nexofiscal.nexofiscalposv2.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.TypeConverters
import ar.com.nexofiscal.nexofiscalposv2.db.dao.AgrupacionDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.CategoriaDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.CierreCajaDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.ClienteDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.CombinacionDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.ComprobanteDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.ComprobantePagoDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.ComprobantePromocionDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.FamiliaDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.FormaPagoDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.LocalidadDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.MonedaDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.PaisDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.ProductoDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.PromocionDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.ProveedorDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.ProvinciaDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.RenglonComprobanteDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.RolDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.StockProductoDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.SubcategoriaDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.SucursalDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.TasaIvaDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.TipoComprobanteDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.TipoDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.TipoDocumentoDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.TipoFormaPagoDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.TipoIvaDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.UnidadDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.UsuarioDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.VendedorDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.AgrupacionEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CategoriaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CierreCajaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ClienteEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CombinacionEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ComprobanteEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ComprobantePagoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ComprobantePromocionEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.FamiliaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.FormaPagoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.LocalidadEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.MonedaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.PaisEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProductoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.PromocionEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProveedorEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProvinciaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.RenglonComprobanteEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.RolEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.StockProductoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SubcategoriaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SucursalEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TasaIvaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoComprobanteEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoDocumentoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoFormaPagoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoIvaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.UnidadEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.UsuarioEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.VendedorEntity

@Database(
    entities = [
        AgrupacionEntity::class,
        CategoriaEntity::class,
        CierreCajaEntity::class,
        ClienteEntity::class,
        CombinacionEntity::class,
        FamiliaEntity::class,
        FormaPagoEntity::class,
        LocalidadEntity::class,
        MonedaEntity::class,
        PaisEntity::class,
        ProductoEntity::class,
        PromocionEntity::class,
        ProveedorEntity::class,
        ProvinciaEntity::class,
        RolEntity::class,
        StockProductoEntity::class,
        SubcategoriaEntity::class,
        SucursalEntity::class,
        TasaIvaEntity::class,
        TipoEntity::class,
        TipoComprobanteEntity::class,
        TipoDocumentoEntity::class,
        TipoFormaPagoEntity::class,
        TipoIvaEntity::class,
        UnidadEntity::class,
        UsuarioEntity::class,
        VendedorEntity::class,
        ComprobanteEntity::class,
        RenglonComprobanteEntity::class,
        ComprobantePagoEntity::class,      // Se añade la nueva entidad
        ComprobantePromocionEntity::class


    ],
    version = 6,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {

    abstract fun agrupacionDao(): AgrupacionDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun cierreCajaDao(): CierreCajaDao
    abstract fun clienteDao(): ClienteDao
    abstract fun combinacionDao(): CombinacionDao
    abstract fun familiaDao(): FamiliaDao
    abstract fun formaPagoDao(): FormaPagoDao
    abstract fun localidadDao(): LocalidadDao
    abstract fun monedaDao(): MonedaDao
    abstract fun paisDao(): PaisDao
    abstract fun productoDao(): ProductoDao
    abstract fun promocionDao(): PromocionDao
    abstract fun proveedorDao(): ProveedorDao
    abstract fun provinciaDao(): ProvinciaDao
    abstract fun rolDao(): RolDao
    abstract fun stockProductoDao(): StockProductoDao
    abstract fun subcategoriaDao(): SubcategoriaDao
    abstract fun sucursalDao(): SucursalDao
    abstract fun tasaIvaDao(): TasaIvaDao
    abstract fun tipoDao(): TipoDao
    abstract fun tipoComprobanteDao(): TipoComprobanteDao
    abstract fun tipoDocumentoDao(): TipoDocumentoDao
    abstract fun tipoFormaPagoDao(): TipoFormaPagoDao
    abstract fun tipoIvaDao(): TipoIvaDao
    abstract fun unidadDao(): UnidadDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun vendedorDao(): VendedorDao
    abstract fun comprobanteDao(): ComprobanteDao
    abstract fun renglonComprobanteDao(): RenglonComprobanteDao
    abstract fun comprobantePagoDao(): ComprobantePagoDao // Añadido para manejar pagos
    abstract fun comprobantePromocionDao(): ComprobantePromocionDao // Añadido para manejar promociones en comprobantes




    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nexofiscal.db"
                ).fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}
