package com.eyescroll.app.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import com.eyescroll.app.domain.model.FaceSignals
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max

class FaceLandmarkerHelper(
    context: Context,
    private val onSignals: (FaceSignals) -> Unit
) {
    private var landmarker: FaceLandmarker? = null
    private val appContext = context.applicationContext

    fun start() {
        if (landmarker != null) return
        landmarker = create(Delegate.GPU) ?: create(Delegate.CPU)
    }

    private fun create(delegate: Delegate): FaceLandmarker? {
        return runCatching {
            val base = BaseOptions.builder()
                .setModelAssetPath(MODEL_ASSET)
                .setDelegate(delegate)
                .build()
            val options = FaceLandmarker.FaceLandmarkerOptions.builder()
                .setBaseOptions(base)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setNumFaces(1)
                .setOutputFaceBlendshapes(true)
                .setOutputFacialTransformationMatrixes(true)
                .setResultListener { result, _ -> handleResult(result) }
                .setErrorListener { /* swallow; service restarts pipeline */ }
                .build()
            FaceLandmarker.createFromOptions(appContext, options)
        }.getOrNull()
    }

    fun clear() {
        landmarker?.close()
        landmarker = null
    }

    @androidx.camera.core.ExperimentalGetImage
    fun processImageProxy(imageProxy: ImageProxy, isFrontCamera: Boolean) {
        val marker = landmarker ?: run {
            imageProxy.close()
            return
        }
        val bitmap = imageProxy.toBitmapRotated(isFrontCamera) ?: run {
            imageProxy.close()
            return
        }
        val mpImage = BitmapImageBuilder(bitmap).build()
        val ts = imageProxy.imageInfo.timestamp / 1_000_000L
        runCatching {
            marker.detectAsync(mpImage, ts)
        }
        imageProxy.close()
    }

    private fun handleResult(result: FaceLandmarkerResult) {
        val now = System.currentTimeMillis()
        if (result.faceLandmarks().isEmpty()) {
            onSignals(
                FaceSignals(
                    0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f,
                    facePresent = false,
                    timestampMs = now
                )
            )
            return
        }

        val blends = result.faceBlendshapes().orElse(emptyList())
        val categories = blends.firstOrNull()?.categories().orEmpty()
        fun score(name: String): Float =
            categories.firstOrNull { it.categoryName() == name }?.score() ?: 0f

        val landmarks = result.faceLandmarks()[0]
        // Approx alar width using cheek/nose-ish indices (stable-ish on Face Mesh)
        val leftAlar = landmarks.getOrNull(98)
        val rightAlar = landmarks.getOrNull(327)
        val noseWidth = if (leftAlar != null && rightAlar != null) {
            hypot(
                (leftAlar.x() - rightAlar.x()).toDouble(),
                (leftAlar.y() - rightAlar.y()).toDouble()
            ).toFloat()
        } else {
            1f
        }

        var headYaw = 0f
        var headPitch = 0f
        val matrices = result.facialTransformationMatrixes().orElse(emptyList())
        if (matrices.isNotEmpty()) {
            val m = matrices[0]
            // Rough pose from rotation matrix elements when available (16 floats column-major)
            if (m.size >= 16) {
                headYaw = m[8]
                headPitch = m[9]
            }
        }

        onSignals(
            FaceSignals(
                eyeBlinkLeft = score("eyeBlinkLeft"),
                eyeBlinkRight = score("eyeBlinkRight"),
                browInnerUp = score("browInnerUp"),
                jawOpen = score("jawOpen"),
                noseSneerLeft = score("noseSneerLeft"),
                noseSneerRight = score("noseSneerRight"),
                noseWidthRatio = max(0.01f, noseWidth),
                headYaw = headYaw,
                headPitch = headPitch,
                facePresent = true,
                timestampMs = now
            )
        )
    }

    companion object {
        const val MODEL_ASSET = "face_landmarker.task"
    }
}

@androidx.camera.core.ExperimentalGetImage
private fun ImageProxy.toBitmapRotated(front: Boolean): Bitmap? {
    val image = image ?: return null
    val yBuffer = image.planes[0].buffer
    val uBuffer = image.planes[1].buffer
    val vBuffer = image.planes[2].buffer
    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()
    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuv = android.graphics.YuvImage(
        nv21,
        android.graphics.ImageFormat.NV21,
        width,
        height,
        null
    )
    val out = java.io.ByteArrayOutputStream()
    yuv.compressToJpeg(android.graphics.Rect(0, 0, width, height), 70, out)
    val bytes = out.toByteArray()
    var bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
    val matrix = Matrix()
    matrix.postRotate(imageInfo.rotationDegrees.toFloat())
    if (front) {
        matrix.postScale(-1f, 1f)
    }
    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    return bitmap
}

/** Unused helper kept for potential EAR fallback */
fun earMetric(opennessHint: Float): Float = abs(1f - opennessHint)
