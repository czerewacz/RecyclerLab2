/*
 * Copyright (c) 2017 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */


package com.raywenderlich.marsrovers

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.raywenderlich.marsrovers.models.Photo
import com.raywenderlich.marsrovers.models.PhotoList
import com.raywenderlich.marsrovers.models.PhotoRow
import com.raywenderlich.marsrovers.models.RowType
import com.raywenderlich.marsrovers.recyclerview.PhotoAdapter
import com.raywenderlich.marsrovers.service.NasaPhotos
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "MarsRover"

class MainActivity : AppCompatActivity() {

  private var currentRover = "curiosity"
  private var currentRoverPosition = 0
  private var currentCameraPosition = 0


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    recycler_view.visibility = View.GONE
    recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    recycler_view.layoutManager = LinearLayoutManager(this)

    setupSpinners()
    loadPhotos()


  }

  private fun setupSpinners() {
    setupRoverSpinner()
    setupCameraSpinner()
  }

  private fun setupCameraSpinner() {
    // Camera spinner
    val cameraStrings = resources.getStringArray(R.array.camera_values)
    val cameraAdapter = ArrayAdapter.createFromResource(this, R.array.camera_names, android.R.layout.simple_spinner_item)
    cameraAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    cameras.adapter = cameraAdapter
    cameras.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
      override fun onNothingSelected(parent: AdapterView<*>) {
      }

      override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        if (recycler_view.adapter != null && currentCameraPosition != position) {
          (recycler_view.adapter as PhotoAdapter).filterCamera(cameraStrings[position])
        }

        currentCameraPosition = position
      }
    }
  }

  private fun setupRoverSpinner() {
    // Setup the spinners for selecting different rovers and cameras
    val roverStrings = resources.getStringArray(R.array.rovers)
    val adapter = ArrayAdapter.createFromResource(this, R.array.rovers, android.R.layout.simple_spinner_item)
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    rovers.adapter = adapter
    rovers.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
      override fun onNothingSelected(parent: AdapterView<*>) {
      }

      override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        if (currentRoverPosition != position) {
          currentRover = roverStrings[position].toLowerCase()
          loadPhotos()
        }
        currentRoverPosition = position
      }
    }
  }

  private fun loadPhotos() {
    progress.visibility = View.VISIBLE
    recycler_view.visibility = View.GONE
    NasaPhotos.getPhotos(currentRover).enqueue(object : Callback<PhotoList> {
      override fun onFailure(call: Call<PhotoList>?, t: Throwable?) {
        Snackbar.make(recycler_view, R.string.api_error, Snackbar.LENGTH_LONG)
        Log.e(TAG, "Problems getting Photos with error: $t.msg")
      }

      override fun onResponse(call: Call<PhotoList>?, response: Response<PhotoList>?) {
        response?.let { photoResponse ->
          if (photoResponse.isSuccessful) {
            val body = photoResponse.body()
            body?.let {
              Log.d(TAG, "Received ${body.photos.size} photos")
              if (recycler_view.adapter == null) {
                val adapter = PhotoAdapter(sortPhotos(body))
                recycler_view.adapter = adapter
              } else {
                (recycler_view.adapter as PhotoAdapter).updatePhotos(sortPhotos(body))
              }
            }
            recycler_view.scrollToPosition(0)
            recycler_view.visibility = View.VISIBLE
            progress.visibility = View.GONE
          }
        }
      }
    })
  }

  fun sortPhotos(photoList: PhotoList) : ArrayList<PhotoRow> {
    val map = HashMap<String, ArrayList<Photo>>()
    for (photo in photoList.photos) {
      var photos = map[photo.camera.full_name]
      if (photos == null) {
        photos = ArrayList()
        map[photo.camera.full_name] = photos
      }
      photos.add(photo)
    }
    val newPhotos = ArrayList<PhotoRow>()
    for ((key, value) in map) {
      newPhotos.add(PhotoRow(RowType.HEADER, null, key))
      value.mapTo(newPhotos) { PhotoRow(RowType.PHOTO, it, null) }
    }
    return newPhotos
  }


}
