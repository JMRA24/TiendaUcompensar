package com.project.store.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.project.store.data.model.Order
import com.project.store.data.model.Payment
import com.project.store.data.model.Product
import com.project.store.data.model.User
import com.project.store.utils.Constants
import com.project.store.utils.SecurePreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

/**
 * FirebaseRepository - Repositorio central de datos con Firebase.
 *
 * Implementa el patron Repository para abstraer el acceso a:
 * - Firebase Authentication (autenticacion de usuarios)
 * - Cloud Firestore (base de datos en tiempo real)
 * - Codificacion Base64 para almacenamiento de imagenes
 *
 * Usa el patron Singleton para garantizar una unica instancia.
 * Todos los metodos de red son suspend functions para uso con coroutines.
 *
 * @author Julian
 * @version 2.0 - Actividad 4
 */
class FirebaseRepository private constructor() {

    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    val usersCollection by lazy { firestore.collection(Constants.COLLECTION_USERS) }
    val productsCollection by lazy { firestore.collection(Constants.COLLECTION_PRODUCTS) }
    val ordersCollection by lazy { firestore.collection(Constants.COLLECTION_ORDERS) }
    val paymentsCollection by lazy { firestore.collection(Constants.COLLECTION_PAYMENTS) }

    /**
     * Obtiene el usuario autenticado actual desde Firestore.
     *
     * @return User autenticado, o null si no hay sesion activa o no existe perfil.
     */
    suspend fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        val snapshot = usersCollection.document(firebaseUser.uid).get().await()

        return snapshot.toObject(User::class.java)?.let { user ->
            if (user.id.isBlank()) user.copy(id = snapshot.id) else user
        }
    }

    /**
     * Verifica si Firebase Auth mantiene una sesion activa.
     *
     * @return true si existe un usuario autenticado, false en caso contrario.
     */
    fun isLoggedIn(): Boolean = auth.currentUser != null

    /**
     * Obtiene la instancia nativa del usuario autenticado en Firebase Auth.
     *
     * @return FirebaseUser actual o null si no hay sesion activa.
     */
    fun getCurrentFirebaseUser(): FirebaseUser? = auth.currentUser

    /**
     * Autentica un usuario con email y contrasena en Firebase Auth.
     * Recupera el perfil completo desde Firestore despues del login.
     *
     * @param email Correo electronico registrado.
     * @param password Contrasena del usuario.
     * @return Result<User> con el usuario autenticado o el error.
     */
    suspend fun loginWithEmail(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Usuario no encontrado"))
            android.util.Log.d("LOGIN_DEBUG", "uid: ${firebaseUser.uid}")
            val userDoc = usersCollection.document(firebaseUser.uid).get().await()
            val user = userDoc.toObject(User::class.java) ?: User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                name = firebaseUser.displayName ?: "",
                role = Constants.ROLE_BUYER
            )
            android.util.Log.d("LOGIN_DEBUG", "role desde Firestore: ${user.role}")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Autentica un usuario usando un token de Google Sign-In.
     * Si el perfil no existe en Firestore, lo crea con rol comprador.
     *
     * @param idToken Token ID emitido por Google Sign-In.
     * @return Result<User> con el usuario autenticado o el error.
     */
    suspend fun loginWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Error de Google"))
            val userDoc = usersCollection.document(firebaseUser.uid).get().await()
            val user = if (userDoc.exists()) {
                userDoc.toObject(User::class.java)!!
            } else {
                val newUser = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    name = firebaseUser.displayName ?: "",
                    role = Constants.ROLE_BUYER,
                    photoUrl = firebaseUser.photoUrl?.toString() ?: ""
                )
                usersCollection.document(firebaseUser.uid).set(newUser).await()
                newUser
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Registra un usuario con email y contrasena en Firebase Auth.
     * Crea el perfil asociado en la coleccion de usuarios de Firestore.
     *
     * @param email Correo electronico del nuevo usuario.
     * @param password Contrasena inicial del usuario.
     * @param name Nombre completo o visible del usuario.
     * @param role Rol asignado: buyer, seller o admin.
     * @return Result<User> con el usuario creado o el error.
     */
    suspend fun registerUser(email: String, password: String, name: String, role: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Error al crear usuario"))
            val user = User(
                id = firebaseUser.uid,
                email = email,
                name = name,
                role = role
            )
            usersCollection.document(firebaseUser.uid).set(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cierra la sesion actual en Firebase Auth.
     *
     * @return Unit cuando la sesion local fue cerrada.
     */
    suspend fun logout(context: Context? = null) {
        context?.let { SecurePreferences.setJustLoggedOut(it, true) }
        auth.signOut()
    }

    // ==================== PRODUCTOS ====================

    /**
     * Obtiene todos los productos del catalogo desde Firestore.
     *
     * @return Result<List<Product>> lista completa o error de red.
     */
    suspend fun getProducts(): Result<List<Product>> {
        return try {
            val snapshot = productsCollection.get().await()
            val products = snapshot.toObjects(Product::class.java)
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene los productos publicados por un vendedor especifico.
     *
     * @param sellerId UID del vendedor propietario de los productos.
     * @return Result<List<Product>> con los productos del vendedor o el error.
     */
    suspend fun getProductsBySeller(sellerId: String): Result<List<Product>> {
        return try {
            val snapshot = productsCollection
                .whereEqualTo("sellerId", sellerId)
                .get().await()
            Result.success(snapshot.toObjects(Product::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Guarda un producto nuevo o reutiliza el ID si el producto ya lo trae.
     *
     * @param product Producto que sera persistido en Firestore.
     * @return Result<String> con el ID final del producto o el error.
     */
    suspend fun saveProduct(product: Product): Result<String> {
        return try {
            val id = if (product.id.isEmpty())
                productsCollection.document().id else product.id
            val productWithId = product.copy(id = id)
            productsCollection.document(id).set(productWithId).await()
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza completamente un producto existente en Firestore.
     *
     * @param product Producto con ID existente y datos actualizados.
     * @return Result<Unit> indicando exito o error.
     */
    suspend fun updateProduct(product: Product): Result<Unit> {
        return try {
            productsCollection.document(product.id).set(product).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina un producto del catalogo por su identificador.
     *
     * @param productId ID del producto a eliminar.
     * @return Result<Unit> indicando exito o error.
     */
    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            productsCollection.document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== ORDENES ====================

    /**
     * Crea una nueva orden de compra en Firestore.
     *
     * @param order Objeto Order con los datos de la compra.
     * @return Result<String> con el ID generado de la orden.
     */
    suspend fun createOrder(order: Order): Result<String> {
        return try {
            val id = ordersCollection.document().id
            val orderWithId = order.copy(id = id)
            ordersCollection.document(id).set(orderWithId).await()
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene las ordenes realizadas por un comprador.
     *
     * @param buyerId UID del comprador.
     * @return Result<List<Order>> con las ordenes ordenadas por fecha descendente.
     */
    suspend fun getOrdersByBuyer(buyerId: String): Result<List<Order>> {
        return try {
            val snapshot = ordersCollection
                .whereEqualTo("buyerId", buyerId)
                .get().await()
            val orders = snapshot.toObjects(Order::class.java)
                .sortedByDescending { it.createdAt }
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene las ordenes recibidas por un vendedor.
     *
     * @param sellerId UID del vendedor.
     * @return Result<List<Order>> con las ordenes ordenadas por fecha descendente.
     */
    suspend fun getOrdersBySeller(sellerId: String): Result<List<Order>> {
        return try {
            val snapshot = ordersCollection
                .whereEqualTo("sellerId", sellerId)
                .get().await()
            val orders = snapshot.toObjects(Order::class.java)
                .sortedByDescending { it.createdAt }
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene todas las ordenes registradas en la plataforma.
     *
     * @return Result<List<Order>> con todas las ordenes ordenadas por fecha descendente.
     */
    suspend fun getAllOrders(): Result<List<Order>> {
        return try {
            val snapshot = ordersCollection
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get().await()
            Result.success(snapshot.toObjects(Order::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza el estado operativo de una orden.
     *
     * @param orderId ID de la orden a actualizar.
     * @param status Nuevo estado de la orden.
     * @return Result<Unit> indicando exito o error.
     */
    suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit> {
        return try {
            ordersCollection.document(orderId)
                .update("status", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== USUARIOS ====================

    /**
     * Obtiene el perfil de Firestore para el usuario autenticado actual.
     *
     * @return Result<User> con el perfil actual o el error.
     */
    suspend fun getCurrentUserFromFirestore(): Result<User> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("No hay sesion activa"))
            val doc = usersCollection.document(uid).get().await()
            val user = doc.toObject(User::class.java)
                ?: return Result.failure(Exception("Usuario no encontrado"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene todos los usuarios registrados en Firestore.
     *
     * @return Result<List<User>> con la lista de usuarios o el error.
     */
    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = usersCollection.get().await()
            Result.success(snapshot.toObjects(User::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza el rol de un usuario especifico.
     *
     * @param userId UID del usuario a modificar.
     * @param role Nuevo rol: buyer, seller o admin.
     * @return Result<Unit> indicando exito o error.
     */
    suspend fun updateUserRole(userId: String, role: String): Result<Unit> {
        return try {
            usersCollection.document(userId)
                .update("role", role).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza el perfil completo de un usuario en Firestore.
     *
     * @param user Usuario con los datos actualizados.
     * @return Result<Unit> indicando exito o error.
     */
    suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== PAGOS ====================

    /**
     * Procesa un pago simulado y registra el resultado en Firestore.
     * Si el pago es aprobado, marca la orden asociada como pagada.
     *
     * @param payment Datos base del pago a procesar.
     * @return Result<Payment> con el pago final y su transaccion, o el error.
     */
    suspend fun processPayment(payment: Payment): Result<Payment> {
        return try {
            delay(2000)
            val transactionId = UUID.randomUUID().toString().take(8).uppercase()
            val isApproved = (1..10).random() <= Constants.PAYMENT_SUCCESS_RATE
            val status = if (isApproved) Constants.PAYMENT_APPROVED else Constants.PAYMENT_REJECTED
            val finalPayment = payment.copy(
                id = paymentsCollection.document().id,
                transactionId = transactionId,
                status = status
            )
            paymentsCollection.document(finalPayment.id).set(finalPayment).await()
            if (isApproved) {
                ordersCollection.document(payment.orderId)
                    .update("status", Constants.ORDER_PAID).await()
            }
            Result.success(finalPayment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Codifica una imagen local en Base64 para almacenarla en Firestore.
     * Reduce el bitmap antes de comprimirlo para mantenerse bajo el limite de documento.
     *
     * @param uri Uri local de la imagen seleccionada.
     * @param context Context requerido para abrir el ContentResolver.
     * @return String con prefijo data:image/jpeg;base64, o null si falla la conversion.
     */
    fun encodeImageToBase64(uri: Uri, context: Context): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val scaled = scaleBitmap(bitmap, Constants.IMAGE_MAX_SIZE)
            val outputStream = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, Constants.IMAGE_QUALITY, outputStream)
            val bytes = outputStream.toByteArray()
            "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    private fun scaleBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val scale = minOf(maxSize.toFloat() / width, maxSize.toFloat() / height)
        if (scale >= 1f) return bitmap
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    // TODO: Implement auth methods.
    // TODO: Implement user CRUD methods.
    // TODO: Implement product CRUD methods.
    // TODO: Implement order CRUD methods.
    // TODO: Implement storage upload/delete helpers.

    companion object {
        @Volatile
        private var instance: FirebaseRepository? = null

        /**
         * Devuelve la instancia unica de FirebaseRepository.
         *
         * @return Instancia singleton compartida por toda la aplicacion.
         */
        fun getInstance(): FirebaseRepository {
            return instance ?: synchronized(this) {
                instance ?: FirebaseRepository().also { instance = it }
            }
        }
    }
}
