package com.eyescroll.app.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.eyescroll.app.domain.model.FaceSignals
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.Category
import com.google.mediapipe.tasks.components.containers.Classifications
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
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
                .setErrorListener { }
                .build()
            FaceLandmarker.createFromOptions(appContext, options)
        }.getOrNull()
    }

    fun clear() {
        landmarker?.close()
        landmarker = null
    }

    fun processImageProxy(imageProxy: ImageProxy, isFrontCamera: Boolean) {
        val marker = landmarker
        if (marker == null) {
            imageProxy.close()
            return
        }
        val bitmap = imageProxy.toRgbBitmap(isFrontCamera)
        if (bitmap == null) {
            imageProxy.close()
            return
        }
        val mpImage = BitmapImageBuilder(bitmap).build()
        val ts = System.currentTimeMillis()
        runCatching { marker.detectAsync(mpImage, ts) }
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

        val blendOptional = result.faceBlendshapes()
        @Suppress("UNCHECKED_CAST")
        val nestedLists: List<List<Category>> =
            if (blendOptional.isPresent) {
                blendOptional.get() as List<List<Category>>
            } else {
                emptyList()
            }
        val categories: List<Category> = nestedLists.firstOrNull() ?: emptyList()

        fun score(name: String): Float {
            for (c in categories) {
                if (c.categoryName() == name) return c.score()
            }
            return 0f
        }

        val landmarks = result.faceLandmarks()[0]
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
        val matrixOptional = result.facialTransformationMatrixes()
        if (matrixOptional.isPresent && matrixOptional.get().isNotEmpty()) {
            val m = matrixOptional.get()[0]
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
        const val OUTPUT_RGBA = ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
    }
}

private fun ImageProxy.toRgbBitmap(front: Boolean): Bitmap? {
    return runCatching {
        val plane = planes.firstOrNull() ?: return null
        val buffer = plane.buffer
        buffer.rewind()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        val matrix = Matrix()
        matrix.postRotate(imageInfo.rotationDegrees.toFloat())
        if (front) matrix.postScale(-1f, 1f)
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }.getOrNull()
}
