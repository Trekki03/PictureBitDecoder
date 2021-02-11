package de.tobias.datareciever.gui

import de.tobias.datareciever.gui.actionListener.ReadInputButtonActionListener
import de.tobias.datareciever.gui.actionListener.SelectInputButtonActionListener
import java.awt.Dimension
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext

class Window : JFrame()
{
    private lateinit var textPaneInput : JTextPane
    private lateinit var textAreaBitOutput : JTextArea
    private lateinit var textAreaTextOutput : JTextArea
    private lateinit var buttonInput : JButton
    private lateinit var buttonRead : JButton
    private val frameTitle : String = "Receiver"
    private val xSize : Int = 1060 // Window width
    private val ySize : Int = 700  // Window height
    private var gbc : GridBagConstraints

    //creating the window with basic setup
    init
    {
        title = frameTitle
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        setLocationRelativeTo(null)
        setSize(xSize, ySize)
        isResizable = false
        layout = GridBagLayout()
        gbc = GridBagConstraints()
        initComponents()
        addComponents()
        isVisible = true
    }

    //initialize all components
    private fun initComponents()
    {
        val centerStyle : StyleContext.NamedStyle = StyleContext.getDefaultStyleContext().NamedStyle()
        StyleConstants.setAlignment(centerStyle, StyleConstants.ALIGN_CENTER)

        textPaneInput = JTextPane()
        textPaneInput.text = ""
        textPaneInput.preferredSize = Dimension(550, 20)
        textPaneInput.logicalStyle = centerStyle

        textAreaBitOutput = JTextArea("")
        textAreaBitOutput.lineWrap = true
        textAreaBitOutput.isEditable = false
        textAreaBitOutput.font = Font("monospaced", Font.PLAIN, 12)
        textAreaBitOutput.preferredSize = Dimension(955, 200)

        textAreaTextOutput = JTextArea("")
        textAreaTextOutput.lineWrap = true
        textAreaTextOutput.preferredSize = Dimension(955, 200)

        buttonInput = JButton("Select Input Path")
        buttonInput.preferredSize = Dimension(200, 50)
        buttonInput.addActionListener(SelectInputButtonActionListener(textPaneInput))

        buttonRead = JButton("Read Input Data")
        buttonRead.preferredSize = Dimension(900, 50)
        buttonRead.addActionListener(ReadInputButtonActionListener(textPaneInput, textAreaTextOutput, textAreaBitOutput))
    }

    //Add all Components to the layout
    private fun addComponents()
    {
        addComponentToLayout(textPaneInput,      0, 0, 2, 1, 1.0, 1.0, MAXIMIZED_BOTH)
        addComponentToLayout(buttonInput,        2, 0, 1, 1, 1.0, 1.0, NORMAL)
        addComponentToLayout(buttonRead,         0, 1, 3, 1, 1.0, 1.0, MAXIMIZED_BOTH)
        addComponentToLayout(textAreaBitOutput,  0, 2, 3, 1, 1.0, 1.0, MAXIMIZED_BOTH)
        addComponentToLayout(textAreaTextOutput, 0, 3, 3, 1, 1.0, 1.0, MAXIMIZED_BOTH)
    }

    /**
     * Add Component to Layout
     * @author Tobias W.
     * @param component JComponent Object which should be added
     * @param xPos xPos of the Component in the Grid
     * @param yPos yPos of the Component in the Grid
     * @param width grid width of the component.
     * @param height grid height of the component.
     * @param xWeight xWeight of the Component (Ability to get space)
     * @param yWeight yWeight of the Component (Ability to get space)
     * @param fill fill type of the Component (e.g. MAXIMIZED_BOTH)
     */
    private fun addComponentToLayout(component : JComponent, xPos : Int, yPos : Int, width : Int, height : Int, xWeight : Double, yWeight : Double, fill : Int)
    {
        gbc.gridx = xPos
        gbc.gridy = yPos
        gbc.gridwidth = width
        gbc.gridheight = height
        gbc.weightx = xWeight
        gbc.weighty = yWeight
        gbc.fill = fill
        add(component, gbc)
    }
}