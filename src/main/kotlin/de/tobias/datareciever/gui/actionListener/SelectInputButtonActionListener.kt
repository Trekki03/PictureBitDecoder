package de.tobias.datareciever.gui.actionListener

import de.tobias.datareciever.util.FileContext
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JTextPane

class SelectInputButtonActionListener(inputTP : JTextPane) : ActionListener {

    private var inputPathTextPane : JTextPane = inputTP
    private var fileContext : FileContext = FileContext()

    override fun actionPerformed(e: ActionEvent?) {
        inputPathTextPane.text = fileContext.openFileDialog()
    }
}