package fr.miage.geotrouvetou.data.maps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.view.drawToBitmap
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.ui.components.atoms.MarkerIcon
import org.osmdroid.views.MapView
import java.util.concurrent.CountDownLatch

/**
 * Génère et met en cache les icônes (marqueur, cluster) de la carte.
 * Isole le rendu graphique du service de carte.
 */
class MarkerIconRenderer(private val context: Context) {

    private var cachedMarkerIcon: BitmapDrawable? = null
    private var cachedClusterIcon: Bitmap? = null

    /** Icône d'un marqueur : rendu Compose si possible, sinon icône dessinée à la main. */
    fun markerIcon(mapView: MapView): BitmapDrawable {
        cachedMarkerIcon?.let { return it }
        return createComposeMarkerIcon(mapView)?.also { cachedMarkerIcon = it }
            ?: createFallbackIcon()
    }

    fun clusterIcon(): Bitmap {
        cachedClusterIcon?.let { return it }
        val sizePx = dip(40f).toInt()
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val center = sizePx / 2f
        val stroke = dip(3f)
        val radius = center - stroke
        canvas.drawCircle(center, center, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = context.getColor(R.color.primary_400)
        })
        canvas.drawCircle(center, center, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.WHITE
            strokeWidth = stroke
        })
        return bitmap.also { cachedClusterIcon = it }
    }

    /** Rend l'icône via Compose ; renvoie null si la vue n'est pas encore prête. */
    private fun createComposeMarkerIcon(mapView: MapView): BitmapDrawable? {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            // Le rendu Compose doit s'exécuter sur le thread principal.
            val latch = CountDownLatch(1)
            var result: BitmapDrawable? = null
            Handler(Looper.getMainLooper()).post {
                try { result = createComposeMarkerIconInternal(mapView) } finally { latch.countDown() }
            }
            latch.await()
            return result
        }
        return createComposeMarkerIconInternal(mapView)
    }

    private fun createComposeMarkerIconInternal(mapView: MapView): BitmapDrawable? {
        val sizeDp = 40
        val sizePx = dip(sizeDp.toFloat()).toInt()

        if (!mapView.isAttachedToWindow) return null

        return try {
            val composeView = ComposeView(context).apply {
                setContent { MarkerIcon(size = sizeDp.dp, borderWidth = 3.dp) }
            }
            // Attaché temporairement à l'arbre de vues pour hériter des lifecycle owners de Compose.
            val parent = mapView.parent as? ViewGroup
            parent?.addView(composeView, ViewGroup.LayoutParams(sizePx, sizePx))

            val bitmap = try {
                val spec = View.MeasureSpec.makeMeasureSpec(sizePx, View.MeasureSpec.EXACTLY)
                composeView.measure(spec, spec)
                composeView.layout(0, 0, sizePx, sizePx)
                composeView.drawToBitmap(Bitmap.Config.ARGB_8888)
            } finally {
                parent?.removeView(composeView)
            }

            BitmapDrawable(context.resources, bitmap).also { it.setBounds(0, 0, bitmap.width, bitmap.height) }
        } catch (t: Throwable) {
            Log.w("MarkerIconRenderer", "Rendu Compose de l'icône échoué", t)
            null
        }
    }

    /** Icône de secours dessinée (cercle + drapeau) quand Compose n'est pas disponible. */
    private fun createFallbackIcon(): BitmapDrawable {
        val sizePx = dip(48f).toInt()
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val center = sizePx / 2f
        val stroke = dip(3f)
        val radius = center - stroke
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = context.getColor(R.color.primary_400)
        }
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.WHITE
            strokeWidth = stroke
        }
        val flagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = context.getColor(R.color.primary_600)
        }
        val flagStemPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.WHITE
            strokeWidth = stroke * 0.8f
            strokeCap = Paint.Cap.ROUND
        }
        val flagPath = Path().apply {
            moveTo(center - sizePx * 0.10f, center - sizePx * 0.18f)
            lineTo(center + sizePx * 0.12f, center - sizePx * 0.10f)
            lineTo(center - sizePx * 0.03f, center + sizePx * 0.02f)
            close()
        }
        canvas.drawCircle(center, center, radius, fill)
        canvas.drawPath(flagPath, flagPaint)
        canvas.drawLine(
            center - sizePx * 0.05f, center - sizePx * 0.20f,
            center - sizePx * 0.05f, center + sizePx * 0.12f,
            flagStemPaint,
        )
        canvas.drawCircle(center, center, radius, border)
        return BitmapDrawable(context.resources, bitmap).also { it.setBounds(0, 0, bitmap.width, bitmap.height) }
    }

    private fun dip(value: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.resources.displayMetrics)
}
