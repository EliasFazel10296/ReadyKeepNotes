package net.geeksempire.ready.keep.notes.Overview.UserInterface

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.abanabsalan.aban.magazine.Utils.System.doVibrate
import com.abanabsalan.aban.magazine.Utils.System.hideKeyboard
import com.abanabsalan.aban.magazine.Utils.System.showKeyboard
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import net.geeksempire.ready.keep.notes.Database.DataStructure.Notes
import net.geeksempire.ready.keep.notes.Database.GeneralEndpoints.DatabaseEndpoints
import net.geeksempire.ready.keep.notes.Database.IO.NotesIO
import net.geeksempire.ready.keep.notes.KeepNoteApplication
import net.geeksempire.ready.keep.notes.Notes.Taking.TakeNote
import net.geeksempire.ready.keep.notes.Overview.NotesLiveData.NotesOverviewViewModel
import net.geeksempire.ready.keep.notes.Overview.UserInterface.Adapter.OverviewAdapter
import net.geeksempire.ready.keep.notes.Overview.UserInterface.Extensions.loadUserAccountInformation
import net.geeksempire.ready.keep.notes.Overview.UserInterface.Extensions.setupActions
import net.geeksempire.ready.keep.notes.Overview.UserInterface.Extensions.setupColors
import net.geeksempire.ready.keep.notes.Overview.UserInterface.Extensions.startNetworkOperation
import net.geeksempire.ready.keep.notes.Preferences.Theme.ThemePreferences
import net.geeksempire.ready.keep.notes.R
import net.geeksempire.ready.keep.notes.Utils.Extensions.checkSpecialCharacters
import net.geeksempire.ready.keep.notes.Utils.InApplicationUpdate.InApplicationUpdateProcess
import net.geeksempire.ready.keep.notes.Utils.InApplicationUpdate.UpdateResponse
import net.geeksempire.ready.keep.notes.Utils.Network.NetworkConnectionListener
import net.geeksempire.ready.keep.notes.Utils.Network.NetworkConnectionListenerInterface
import net.geeksempire.ready.keep.notes.Utils.Security.Encryption.ContentEncryption
import net.geeksempire.ready.keep.notes.Utils.UI.Display.columnCount
import net.geeksempire.ready.keep.notes.Utils.UI.NotifyUser.SnackbarActionHandlerInterface
import net.geeksempire.ready.keep.notes.Utils.UI.NotifyUser.SnackbarBuilder
import net.geeksempire.ready.keep.notes.databinding.OverviewLayoutBinding
import javax.inject.Inject

class KeepNoteOverview : AppCompatActivity(), NetworkConnectionListenerInterface {

    val firebaseUser = Firebase.auth.currentUser

    val databaseEndpoints = DatabaseEndpoints()

    val notesIO: NotesIO by lazy {
        NotesIO(application as KeepNoteApplication)
    }

    val themePreferences: ThemePreferences by lazy {
        ThemePreferences(applicationContext)
    }

    val contentEncryption: ContentEncryption = ContentEncryption()

    var databaseListener: ListenerRegistration? = null

    val notesOverviewViewModel: NotesOverviewViewModel by lazy {
        ViewModelProvider(this@KeepNoteOverview).get(NotesOverviewViewModel::class.java)
    }

    private val itemTouchHelper: ItemTouchHelper by lazy {

        var initialPosition = -1
        var targetPosition = -1

        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP
                    or ItemTouchHelper.DOWN
                    or ItemTouchHelper.START
                    or ItemTouchHelper.END,
            0
        ) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {



                return true
            }

            override fun onMoved(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                fromPosition: Int,
                target: RecyclerView.ViewHolder,
                toPosition: Int,
                x: Int,
                y: Int
            ) {
                super.onMoved(recyclerView, viewHolder, fromPosition, target, toPosition, x, y)

                val overviewAdapter = (recyclerView.adapter as OverviewAdapter)

                if (initialPosition == -1) {

                    databaseListener?.remove()

                    initialPosition = fromPosition

                }
                targetPosition = toPosition

                overviewAdapter.notifyItemMoved(initialPosition, targetPosition)

            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {



            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                doVibrate(applicationContext, 157)

                when (actionState) {
                    ItemTouchHelper.ACTION_STATE_DRAG -> {


                    }
                    ItemTouchHelper.ACTION_STATE_SWIPE -> {


                    }
                    ItemTouchHelper.ACTION_STATE_IDLE -> {


                    }
                }

            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)

                doVibrate(applicationContext, 57)

                if (initialPosition != -1 && targetPosition != -1) {

                    val overviewAdapter = (recyclerView.adapter as OverviewAdapter)

                    val oldIndex = overviewAdapter.notesDataStructureList[initialPosition][Notes.NoteIndex].toString().toLong()
                    val newIndex = overviewAdapter.notesDataStructureList[targetPosition][Notes.NoteIndex].toString().toLong()

                    (application as KeepNoteApplication)
                        .firestoreDatabase.document(overviewAdapter.notesDataStructureList[initialPosition].reference.path)
                        .update(
                            Notes.NoteIndex, newIndex,
                        ).addOnSuccessListener {
                            Log.d(
                                this@KeepNoteOverview.javaClass.simpleName,
                                "Database Rearrange Process Completed Successfully | Initial Position"
                            )

                            (application as KeepNoteApplication)
                                .firestoreDatabase.document(overviewAdapter.notesDataStructureList[targetPosition].reference.path)
                                .update(
                                    Notes.NoteIndex, oldIndex,
                                ).addOnSuccessListener {
                                    Log.d(
                                        this@KeepNoteOverview.javaClass.simpleName,
                                        "Database Rearrange Process Completed Successfully | Target Positionb"
                                    )

                                    (application as KeepNoteApplication).firestoreConfiguration.justRegisterChangeListener = true
                                    startNetworkOperation()

                                    initialPosition = -1
                                    targetPosition = -1

                                }

                        }

                }

            }

        }

        ItemTouchHelper(simpleItemTouchCallback)
    }

    var autoEnterPlaced = false

    @Inject
    lateinit var networkConnectionListener: NetworkConnectionListener

    lateinit var overviewLayoutBinding: OverviewLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overviewLayoutBinding = OverviewLayoutBinding.inflate(layoutInflater)
        setContentView(overviewLayoutBinding.root)

        (application as KeepNoteApplication)
            .dependencyGraph
            .subDependencyGraph()
            .create(this@KeepNoteOverview, overviewLayoutBinding.rootView)
            .inject(this@KeepNoteOverview)

        networkConnectionListener.networkConnectionListenerInterface = this@KeepNoteOverview

        setupActions()

        overviewLayoutBinding.root.post {

            overviewLayoutBinding.quickTakeNote.post {

                overviewLayoutBinding.quickTakeNote.requestFocus()

                showKeyboard(applicationContext, overviewLayoutBinding.quickTakeNote)

                overviewLayoutBinding.quickTakeNote.addTextChangedListener(object : TextWatcher {

                    override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {

                    }

                    override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {

                    }

                    override fun afterTextChanged(editable: Editable?) {

                        editable?.let {

                            try {

                                if (editable[editable.length - 1] == '\n') {

                                    val allLines = editable.toString().split("\n")

                                    val lastLine = allLines[allLines.size - 2]

                                    val specialCharacterData = lastLine.substring(IntRange(0, 0)).checkSpecialCharacters()

                                    if (specialCharacterData.detected) {

                                        if (lastLine.length == 2) {

                                            autoEnterPlaced = true

                                            overviewLayoutBinding.quickTakeNote.editableText.replace(editable.length - 4, editable.length, "")
                                            overviewLayoutBinding.quickTakeNote.append("\n")

                                        } else {

                                            if (!autoEnterPlaced) {

                                                overviewLayoutBinding.quickTakeNote.append(specialCharacterData.specialCharacter)
                                                overviewLayoutBinding.quickTakeNote.setSelection(editable.length)

                                            }

                                            autoEnterPlaced = false

                                        }

                                    }

                                }

                            } catch (e: IndexOutOfBoundsException) {
                                e.printStackTrace()
                            }

                        }

                    }

                })

            }

            overviewLayoutBinding.overviewRecyclerView.layoutManager = GridLayoutManager(applicationContext, columnCount(applicationContext, 313), RecyclerView.VERTICAL, false)

            val overviewAdapter = OverviewAdapter(this@KeepNoteOverview)

            itemTouchHelper.attachToRecyclerView(overviewLayoutBinding.overviewRecyclerView)

            notesOverviewViewModel.notesQuerySnapshots.observe(this@KeepNoteOverview, Observer {

                if (it.isNotEmpty()) {


                    overviewLayoutBinding.overviewRecyclerView.visibility = View.VISIBLE

                    if (overviewAdapter.notesDataStructureList.isNotEmpty()) {

                        overviewAdapter.notesDataStructureList.clear()
                        overviewAdapter.notesDataStructureList.addAll(it)

                        overviewAdapter.notifyDataSetChanged()

                    } else {

                        overviewAdapter.notesDataStructureList.clear()
                        overviewAdapter.notesDataStructureList.addAll(it)

                        overviewLayoutBinding.overviewRecyclerView.adapter = overviewAdapter

                    }

                    overviewLayoutBinding.waitingViewDownload.visibility = View.INVISIBLE

                } else {

                    overviewAdapter.notesDataStructureList.clear()

                    overviewLayoutBinding.overviewRecyclerView.removeAllViews()

                    overviewLayoutBinding.waitingViewDownload.visibility = View.VISIBLE

                    SnackbarBuilder(applicationContext).show(
                        rootView = overviewLayoutBinding.rootView,
                        messageText = getString(R.string.emptyNotesCollection),
                        messageDuration = Snackbar.LENGTH_INDEFINITE,
                        actionButtonText = android.R.string.ok,
                        snackbarActionHandlerInterface = object : SnackbarActionHandlerInterface {

                            override fun onActionButtonClicked(snackbar: Snackbar) {
                                super.onActionButtonClicked(snackbar)

                                startActivity(
                                    Intent(applicationContext, TakeNote::class.java).apply {
                                        putExtra(
                                            TakeNote.NoteTakingWritingType.ExtraConfigurations,
                                            TakeNote.NoteTakingWritingType.Keyboard
                                        )
                                        putExtra(
                                            TakeNote.NoteTakingWritingType.ContentText,
                                            overviewLayoutBinding.quickTakeNote.text.toString()
                                        )
                                    }, ActivityOptions.makeCustomAnimation(
                                        applicationContext,
                                        R.anim.fade_in,
                                        0
                                    ).toBundle()
                                )

                            }

                        }
                    )

                }

            })

            /*Invoke In Application Update*/
            InApplicationUpdateProcess(this@KeepNoteOverview, overviewLayoutBinding.rootView)
                .initialize (object : UpdateResponse {

                    override fun latestVersionAlreadyInstalled() {
                        super.latestVersionAlreadyInstalled()

                        Toast.makeText(applicationContext, "Latest Version Already Installed", Toast.LENGTH_LONG).show()

                    }

                })

        }

    }

    override fun onResume() {
        super.onResume()

        setupColors()

        loadUserAccountInformation()

    }

    override fun onPause() {
        super.onPause()

        hideKeyboard(applicationContext, overviewLayoutBinding.quickTakeNote)

    }

    override fun onBackPressed() {

        startActivity(
            Intent(Intent.ACTION_MAIN).apply {
                this.addCategory(Intent.CATEGORY_HOME)
                this.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }, ActivityOptions.makeCustomAnimation(
                applicationContext,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            ).toBundle()
        )

    }

    override fun networkAvailable() {

        (application as KeepNoteApplication).firestoreConfiguration.justRegisterChangeListener = false

        startNetworkOperation()

    }

    override fun networkLost() {



    }

}