package net.geeksempire.keepnotes.Notes.Taking

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import net.geeksempire.keepnotes.Database.DataStructure.NotesDataStructure
import net.geeksempire.keepnotes.Database.GeneralEndpoints.DatabaseEndpoints
import net.geeksempire.keepnotes.Database.IO.PaintingIO
import net.geeksempire.keepnotes.KeepNoteApplication
import net.geeksempire.keepnotes.Notes.Painting.Adapter.RecentColorsAdapter
import net.geeksempire.keepnotes.Notes.Painting.PaintingCanvasView
import net.geeksempire.keepnotes.Notes.Taking.Extensions.setupPaintingActions
import net.geeksempire.keepnotes.Notes.Taking.Extensions.setupTakeNoteTheme
import net.geeksempire.keepnotes.Notes.Taking.Extensions.setupToggleKeyboardHandwriting
import net.geeksempire.keepnotes.Preferences.Theme.ThemePreferences
import net.geeksempire.keepnotes.R
import net.geeksempire.keepnotes.Utils.Network.NetworkConnectionListener
import net.geeksempire.keepnotes.Utils.Network.NetworkConnectionListenerInterface
import net.geeksempire.keepnotes.databinding.TakeNoteLayoutBinding
import javax.inject.Inject

class TakeNote : AppCompatActivity(), NetworkConnectionListenerInterface {

    val paintingCanvasView: PaintingCanvasView by lazy {
        PaintingCanvasView(applicationContext).also {
            it.setupPaintingPanel(
                getColor(R.color.default_color_light),
                3.7531f
            )
        }
    }

    val inputMethodManager: InputMethodManager by lazy {
        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    val themePreferences: ThemePreferences by lazy {
        ThemePreferences(applicationContext)
    }

    val paintingIO: PaintingIO by lazy {
        PaintingIO(applicationContext)
    }

    /**
     * True: Handwriting - False: Keyboard
     **/
    var toggleKeyboardHandwriting: Boolean = false

    val firebaseUser = Firebase.auth.currentUser

    val databaseEndpoints: DatabaseEndpoints = DatabaseEndpoints()

    val recentColorsAdapter: RecentColorsAdapter by lazy {
        RecentColorsAdapter(this@TakeNote, paintingCanvasView)
    }

    @Inject
    lateinit var networkConnectionListener: NetworkConnectionListener

    lateinit var takeNoteLayoutBinding: TakeNoteLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        takeNoteLayoutBinding = TakeNoteLayoutBinding.inflate(layoutInflater)
        setContentView(takeNoteLayoutBinding.root)

        val documentId = System.currentTimeMillis()

        (application as KeepNoteApplication)
            .dependencyGraph
            .subDependencyGraph()
            .create(this@TakeNote, takeNoteLayoutBinding.rootView)
            .inject(this@TakeNote)

        networkConnectionListener.networkConnectionListenerInterface = this@TakeNote

        setupTakeNoteTheme()

        setupToggleKeyboardHandwriting()

        setupPaintingActions()

        takeNoteLayoutBinding.savingView.setOnClickListener {

            firebaseUser?.let {

                val notesDataStructure = NotesDataStructure(
                    noteTile = takeNoteLayoutBinding.editTextTitleView.text.toString(),
                    noteTextContent = takeNoteLayoutBinding.editTextContentView.text.toString(),
                    noteHandwritingSnapshotLink = null
                )

                (application as KeepNoteApplication).firestoreDatabase
                    .document(databaseEndpoints.GeneralEndpoints(firebaseUser.uid) + "/" + "${documentId}")
                    .set(notesDataStructure)
                    .addOnSuccessListener {
                        Log.d(this@TakeNote.javaClass.simpleName, "Note Saved Successfully")

                        (application as KeepNoteApplication).firebaseStorage
                            .getReference(databaseEndpoints.GeneralEndpoints(firebaseUser.uid) + "/${documentId}.PNG")
                            .putBytes(paintingIO.takeScreenshot(paintingCanvasView))
                            .addOnSuccessListener { uploadTaskSnapshot ->
                                Log.d(this@TakeNote.javaClass.simpleName, "Paint Saved Successfully")

                                (application as KeepNoteApplication).firebaseStorage
                                    .getReference(databaseEndpoints.GeneralEndpoints(firebaseUser.uid) + "/${documentId}.PNG")
                                    .downloadUrl
                                    .addOnSuccessListener { downloadUrl ->

                                        (application as KeepNoteApplication).firestoreDatabase
                                            .document(databaseEndpoints.GeneralEndpoints(firebaseUser.uid) + "/" + documentId)
                                            .update(
                                                "noteHandwritingSnapshotLink", downloadUrl.toString(),
                                            ).addOnSuccessListener {
                                                Log.d(this@TakeNote.javaClass.simpleName, "Paint Link Saved Successfully")


                                            }.addOnFailureListener {
                                                Log.d(this@TakeNote.javaClass.simpleName, "Paint Link Did Not Saved")


                                            }

                                    }.addOnFailureListener {



                                    }

                            }.addOnFailureListener {
                                Log.d(this@TakeNote.javaClass.simpleName, "Paint Did Note Saved")


                            }

                    }.addOnFailureListener {
                        Log.d(this@TakeNote.javaClass.simpleName, "Note Did Note Saved")


                    }

            }

        }

    }

    override fun onResume() {
        super.onResume()



    }

    override fun onPause() {
        super.onPause()



    }

    override fun onBackPressed() {

        startActivity(Intent(Intent.ACTION_MAIN).apply {
            this.addCategory(Intent.CATEGORY_HOME)
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }, ActivityOptions.makeCustomAnimation(applicationContext, android.R.anim.fade_in, android.R.anim.fade_out).toBundle())

    }

    override fun networkAvailable() {



    }

    override fun networkLost() {



    }

}