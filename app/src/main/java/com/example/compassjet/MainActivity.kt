package com.example.compassjet

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.compassjet.ui.theme.CompassJetTheme

class MainActivity : ComponentActivity() {


    private lateinit var  sensorManager : SensorManager
    private lateinit var  sensorAccelerometer : Sensor
    private lateinit var  sensorMagneticField : Sensor


    private var floatGravity = FloatArray(3)
    private var floatGeoMagnetic = FloatArray(3)

    private var floatOrientation = FloatArray(3)
    private var floatRotationMatrix = FloatArray(9)

    // View Model
    private val compassViewModel : CompassViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        Log.d("MainActivity", "SensorManager: $sensorManager")
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val sensorEventListenerAccelerometer = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    sensorChanged(event = it, true)
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // TODO("Not yet implemented")
            }
        }
        val sensorEventListenerMagneticField = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    sensorChanged(event = it, false)
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // TODO("Not yet implemented")
            }
        }

        sensorManager.registerListener(
            sensorEventListenerAccelerometer,
            sensorAccelerometer,
            SensorManager.SENSOR_DELAY_NORMAL)

        sensorManager.registerListener(
            sensorEventListenerMagneticField,
            sensorMagneticField,
            SensorManager.SENSOR_DELAY_NORMAL)

        setContent {
            CompassJetTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    CompassApp(compassViewModel = compassViewModel)

                }
            }
        }
    }


    fun sensorChanged(event: SensorEvent, accelerometer: Boolean): Float {
        if (accelerometer){ // Accelerometer sensor changed
            floatGravity = event.values
        }else{ // Magnetic sensor changed
            floatGeoMagnetic = event.values
        }


        SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic)
        SensorManager.getOrientation(floatRotationMatrix, floatOrientation)

        // Return the orientation the image should have
        val rotation = (-floatOrientation[0] * 180 / 3.14159).toFloat()
        compassViewModel.onRotationChange(rotation)
        return rotation
    }



}

class CompassViewModel : ViewModel(){

    private val _imageRotation = MutableLiveData(0f)
    val imageRotation : LiveData<Float> = _imageRotation

    fun onRotationChange(newRotation : Float){
        _imageRotation.value = newRotation
    }

}

@Composable
fun CompassApp(compassViewModel: CompassViewModel){
    val rotation : Float by compassViewModel.imageRotation.observeAsState(0f)
    HomeScreen(
        rotation = rotation,
        onResetListener = {
            compassViewModel.onRotationChange(0f)
        }
    )
}

@Composable
fun HomeScreen(rotation : Float, onResetListener : () -> Unit) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()

    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(text = "North")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("West")
                Image(
                    painter = painterResource(id = R.drawable.ic_pointer),
                    contentDescription = "Pointer",
                    modifier = Modifier
                        .height(128.dp)
                        .width(128.dp)
                        .rotate(rotation)
                    )
                Text(text = "East")
            }
            Text("South")
 

            Button(onClick = onResetListener) {
                Text(text = "Reset")
            }
        }

    }

}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CompassJetTheme {
        HomeScreen(rotation = 90f){

        }
    }
}