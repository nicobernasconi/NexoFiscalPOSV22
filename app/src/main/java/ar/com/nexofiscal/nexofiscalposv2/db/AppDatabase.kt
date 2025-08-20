// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/AppDatabase.kt
package ar.com.nexofiscal.nexofiscalposv2.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.TypeConverters
import ar.com.nexofiscal.nexofiscalposv2.db.converter.DateConverter
import ar.com.nexofiscal.nexofiscalposv2.db.converter.SyncStatusConverter
import ar.com.nexofiscal.nexofiscalposv2.db.dao.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.*

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
        StockActualizacionEntity::class,
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
        ComprobantePagoEntity::class,
        ComprobantePromocionEntity::class,
        NotificacionEntity::class,
        GastoEntity::class,
        TipoGastoEntity::class
    ],
    views = [CierreCajaResumenView::class, CierreCajaInformeView::class],
    version = 21,
    exportSchema = false
)
@TypeConverters(DateConverter::class, SyncStatusConverter::class)
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
    abstract fun stockActualizacionDao(): StockActualizacionDao
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
    abstract fun comprobantePagoDao(): ComprobantePagoDao
    abstract fun comprobantePromocionDao(): ComprobantePromocionDao
    abstract fun notificacionDao(): NotificacionDao
    abstract fun gastoDao(): GastoDao
    abstract fun tipoGastoDao(): TipoGastoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nexofiscal_v21.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-poblar tipos_gastos
                            val inserts = listOf(
                                // Recursos Humanos y Personal (100s)
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (101, 'Sueldos y Salarios')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (102, 'Cargas Sociales y Aportes Patronales')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (103, 'Beneficios y Compensaciones')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (104, 'Bonos y Premios')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (105, 'Capacitación y Formación')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (106, 'Viáticos y Movilidad de Personal')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (107, 'Seguros de Vida y ART')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (108, 'Indumentaria y Equipo de Trabajo')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (109, 'Honorarios por Búsqueda y Selección')",
                                // Operativos e Infraestructura (200s)
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (201, 'Alquiler de Oficina o Local')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (202, 'Expensas y Gastos Comunes')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (203, 'Servicios Públicos (Luz, Agua, Gas)')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (204, 'Internet y Telefonía')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (205, 'Limpieza y Mantenimiento')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (206, 'Seguridad y Vigilancia')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (207, 'Insumos de Producción / Materia Prima')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (208, 'Fletes, Envíos y Logística')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (209, 'Reparación de Maquinaria y Equipos')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (210, 'Seguros Generales (incendio, robo)')",
                                // Administrativos (300s)
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (301, 'Útiles de Oficina y Papelería')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (302, 'Software, Licencias y Suscripciones')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (303, 'Hosting y Servicios en la Nube')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (304, 'Gastos Legales y Notariales')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (305, 'Honorarios Contables y de Auditoría')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (306, 'Gastos de Representación')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (307, 'Correo y Mensajería')",
                                // Marketing y Ventas (400s)
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (401, 'Publicidad y Promoción')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (402, 'Comisiones sobre Ventas')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (403, 'Eventos, Ferias y Exposiciones')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (404, 'Material Promocional y Merchandising')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (405, 'Viajes y Viáticos Comerciales')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (406, 'Herramientas de Marketing y CRM')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (407, 'Diseño Gráfico y Contenido')",
                                // Financieros e Impuestos (500s)
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (501, 'Comisiones y Gastos Bancarios')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (502, 'Intereses sobre Préstamos')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (503, 'Impuestos Nacionales')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (504, 'Impuestos Provinciales')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (505, 'Tasas Municipales')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (506, 'Diferencias de Cambio')",
                                // Movilidad y Transporte (600s)
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (601, 'Combustible')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (602, 'Mantenimiento y Reparación de Vehículos')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (603, 'Patentes y Seguros de Vehículos')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (604, 'Peajes y Estacionamiento')",
                                // Otros Gastos (900s)
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (901, 'Depreciación de Activos Fijos')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (902, 'Amortización de Intangibles')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (903, 'Donaciones y Responsabilidad Social')",
                                "INSERT INTO tipos_gastos (id, nombre) VALUES (999, 'Gastos Varios / Sin Categorizar')"
                            )
                            inserts.forEach { sql -> db.execSQL(sql) }
                        }
                    })
                    .build().also { INSTANCE = it }
            }
    }
}
