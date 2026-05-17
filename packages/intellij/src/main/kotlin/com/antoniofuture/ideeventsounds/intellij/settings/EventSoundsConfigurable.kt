package com.antoniofuture.ideeventsounds.intellij.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.JTextField
import javax.swing.border.EmptyBorder
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableColumn
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridLayout
import java.awt.Point
import java.io.File
import javax.swing.DefaultCellEditor

import com.antoniofuture.ideeventsounds.core.config.ConfigManager
import com.antoniofuture.ideeventsounds.core.config.SoundMapping
import com.antoniofuture.ideeventsounds.core.soundplayer.SoundPlayer

class EventSoundsConfigurable(private val project: Project) : Configurable {
    private var configPanel: EventSoundsConfigPanel? = null

    override fun getDisplayName(): String = "IDE Event Sounds"

    override fun createComponent(): JComponent {
        configPanel = EventSoundsConfigPanel(project)
        return configPanel!!.mainPanel
    }

    override fun isModified(): Boolean {
        return configPanel?.isModified() ?: false
    }

    override fun apply() {
        configPanel?.applyChanges()
    }

    override fun reset() {
        configPanel?.loadSettings()
    }

    override fun disposeUIResources() {
        configPanel = null
    }
}

class EventSoundsConfigPanel(val project: Project) {
    private val configManager = ConfigManager.instance
    private val soundPlayer = SoundPlayer()

    private var enableCheckbox = JCheckBox("启用插件 (Enable Plugin)")
    private var eventTable: JTable = JTable()
    private var tableModel = SoundMappingTableModel()

    private var originalEnable = true
    private var originalMappings = listOf<SoundMapping>()

    val mainPanel: JPanel = JPanel(BorderLayout())

    init {
        setupUI()
        loadSettings()
    }

    private fun setupUI() {
        val topPanel = JPanel(BorderLayout(5, 5))
        
        val addPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        val addButton = JButton("添加事件 (Add)")
        addButton.addActionListener { addMapping() }
        addPanel.add(addButton)
        
        topPanel.add(enableCheckbox, BorderLayout.NORTH)
        topPanel.add(addPanel, BorderLayout.SOUTH)

        eventTable.model = tableModel
        eventTable.columnModel.getColumn(0).preferredWidth = 60
        eventTable.columnModel.getColumn(1).preferredWidth = 150
        eventTable.columnModel.getColumn(2).preferredWidth = 120
        eventTable.columnModel.getColumn(3).preferredWidth = 200
        eventTable.columnModel.getColumn(4).preferredWidth = 250
        eventTable.columnModel.getColumn(5).preferredWidth = 150
        
        eventTable.columnModel.getColumn(0).minWidth = 50
        eventTable.columnModel.getColumn(1).minWidth = 100
        eventTable.columnModel.getColumn(2).minWidth = 80
        eventTable.columnModel.getColumn(3).minWidth = 100
        eventTable.columnModel.getColumn(4).minWidth = 100
        eventTable.columnModel.getColumn(5).minWidth = 150
        
        eventTable.rowHeight = 30
        eventTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION)
        eventTable.setDefaultRenderer(JPanel::class.java, ActionButtonRenderer())
        eventTable.autoResizeMode = javax.swing.JTable.AUTO_RESIZE_OFF
        
        eventTable.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseClicked(e: java.awt.event.MouseEvent) {
                val point = e.point
                val row = eventTable.rowAtPoint(point)
                val col = eventTable.columnAtPoint(point)
                
                if (row >= 0 && col == 5) {
                    val rect = eventTable.getCellRect(row, col, false)
                    val relativeX = e.x - rect.x
                    
                    if (relativeX < 50) {
                        editMapping(row)
                    } else if (relativeX < 100) {
                        testPlay(tableModel.getMapping(row).soundPath)
                    } else {
                        removeMapping(row)
                    }
                }
            }
        })

        val tableScrollPane = JScrollPane(eventTable)
        tableScrollPane.preferredSize = java.awt.Dimension(950, 400)
        tableScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED

        val infoLabel = JLabel("<html>提示：修改配置后需要点击【确定】或【应用】按钮保存，重启IDE可使所有更改完全生效</html>")
        infoLabel.border = EmptyBorder(5, 5, 5, 5)

        mainPanel.add(topPanel, BorderLayout.NORTH)
        mainPanel.add(tableScrollPane, BorderLayout.CENTER)
        mainPanel.add(infoLabel, BorderLayout.SOUTH)
    }

    fun loadSettings() {
        try {
            val config = configManager.loadConfig()
            originalEnable = config.enable
            enableCheckbox.isSelected = config.enable
            originalMappings = config.sounds.toList()
            tableModel.setMappings(config.sounds)
        } catch (e: Exception) {
            Messages.showMessageDialog(project, "加载配置失败: ${e.message}", "错误", Messages.getErrorIcon())
        }
    }

    fun applyChanges() {
        try {
            val config = configManager.loadConfig()
            config.enable = enableCheckbox.isSelected
            config.sounds = tableModel.getMappings()
            configManager.saveConfig(config)
            originalEnable = config.enable
            originalMappings = config.sounds.toList()
        } catch (e: Exception) {
            Messages.showMessageDialog(project, "保存配置失败: ${e.message}", "错误", Messages.getErrorIcon())
        }
    }

    fun isModified(): Boolean {
        return enableCheckbox.isSelected != originalEnable || 
               tableModel.getMappings() != originalMappings
    }

    private fun addMapping() {
        val dialog = EventMappingDialog(project, null)
        dialog.show()
        if (dialog.okClicked) {
            tableModel.addMapping(dialog.getMapping())
        }
    }

    private fun editMapping(row: Int) {
        val mapping = tableModel.getMapping(row)
        val dialog = EventMappingDialog(project, mapping)
        dialog.show()
        if (dialog.okClicked) {
            tableModel.updateMapping(row, dialog.getMapping())
        }
    }

    private fun removeMapping(row: Int) {
        val result = Messages.showYesNoDialog(
            project,
            "确定要删除这个事件配置吗？",
            "确认删除",
            Messages.getQuestionIcon()
        )
        if (result == Messages.YES) {
            tableModel.removeMapping(row)
        }
    }

    private fun testPlay(soundPath: String) {
        try {
            if (soundPath.startsWith("sounds/")) {
                val fileName = soundPath.substring("sounds/".length)
                val resourcePath = "/preset/$fileName"
                val inputStream = SoundPlayer::class.java.getResourceAsStream(resourcePath)
                if (inputStream != null) {
                    soundPlayer.playFromStream(inputStream)
                } else {
                    Messages.showMessageDialog(project, "找不到声音文件: $soundPath", "错误", Messages.getWarningIcon())
                }
            } else {
                val soundFile = File(soundPath)
                if (soundFile.exists()) {
                    soundPlayer.playFromFile(soundFile)
                } else {
                    Messages.showMessageDialog(project, "找不到声音文件: $soundPath", "错误", Messages.getWarningIcon())
                }
            }
        } catch (e: Exception) {
            Messages.showMessageDialog(project, "播放失败: ${e.message}", "错误", Messages.getErrorIcon())
        }
    }
}

class SoundMappingTableModel : AbstractTableModel() {
    private val columns = arrayOf("启用", "事件Key", "名称", "声音路径", "正则", "操作")
    private val mappings = mutableListOf<SoundMapping>()

    fun setMappings(newMappings: List<SoundMapping>) {
        mappings.clear()
        mappings.addAll(newMappings)
        fireTableDataChanged()
    }

    fun getMappings(): List<SoundMapping> = mappings.toList()

    fun getMapping(index: Int): SoundMapping = mappings[index]

    fun addMapping(mapping: SoundMapping) {
        mappings.add(mapping)
        fireTableRowsInserted(mappings.size - 1, mappings.size - 1)
    }

    fun updateMapping(index: Int, mapping: SoundMapping) {
        mappings[index] = mapping
        fireTableRowsUpdated(index, index)
    }

    fun removeMapping(index: Int) {
        mappings.removeAt(index)
        fireTableRowsDeleted(index, index)
    }

    override fun getColumnCount(): Int = columns.size

    override fun getColumnName(col: Int): String = columns[col]

    override fun getRowCount(): Int = mappings.size

    override fun getValueAt(row: Int, col: Int): Any {
        val mapping = mappings[row]
        return when (col) {
            0 -> mapping.isEnabled
            1 -> mapping.eventKey
            2 -> mapping.name
            3 -> mapping.soundPath
            4 -> mapping.regex
            5 -> mapping
            else -> ""
        }
    }

    override fun getColumnClass(col: Int): Class<*> {
        return when (col) {
            0 -> java.lang.Boolean::class.java
            5 -> JPanel::class.java
            else -> java.lang.String::class.java
        }
    }

    override fun isCellEditable(row: Int, col: Int): Boolean = col == 0 || col == 5

    override fun setValueAt(value: Any, row: Int, col: Int) {
        if (col == 0) {
            mappings[row] = mappings[row].copy(isEnabled = value as Boolean)
        }
    }
}

class ActionButtonRenderer : TableCellRenderer {
    private val panel: JPanel
    private val editButton = JButton("编辑")
    private val playButton = JButton("播放")
    private val deleteButton = JButton("删除")

    init {
        panel = JPanel(FlowLayout(FlowLayout.LEFT, 2, 0))
        panel.isOpaque = false
        
        editButton.preferredSize = Dimension(45, 22)
        playButton.preferredSize = Dimension(45, 22)
        deleteButton.preferredSize = Dimension(45, 22)
        
        editButton.font = java.awt.Font("SansSerif", java.awt.Font.PLAIN, 11)
        playButton.font = java.awt.Font("SansSerif", java.awt.Font.PLAIN, 11)
        deleteButton.font = java.awt.Font("SansSerif", java.awt.Font.PLAIN, 11)
        
        panel.add(editButton)
        panel.add(playButton)
        panel.add(deleteButton)
    }

    override fun getTableCellRendererComponent(
        table: JTable, value: Any, isSelected: Boolean, hasFocus: Boolean, row: Int, col: Int
    ): Component {
        return panel
    }
}

class EventMappingDialog(val project: Project, initialMapping: SoundMapping?) : DialogWrapper(true) {
    private val eventKeys = listOf(
        "build.success", "build.failed", "compile.finished", "compile.started",
        "test.passed", "test.failed", "test.started", "test.stopped",
        "run.start", "run.stop", "debug.started", "debug.stopped",
        "project.opened", "project.closed", "application.starting", "application.loaded",
        "file.created", "file.deleted", "file.moved", "file.renamed", "file.saved",
        "git.commit.success", "git.commit.failed", "git.push.success", "git.push.failed",
        "git.pull.success", "git.pull.failed", "indexing.started", "indexing.finished"
    )
    
    private var eventKeyCombo = JComboBox(eventKeys.toTypedArray())
    private var soundPathField = JTextField()
    private var nameField = JTextField()
    private var regexField = JTextField()
    private var enabledCheckbox = JCheckBox("启用")
    private val soundPlayer = SoundPlayer()

    var okClicked = false

    init {
        title = if (initialMapping == null) "添加事件" else "编辑事件"
        eventKeyCombo.isEditable = true

        initialMapping?.let {
            eventKeyCombo.editor.item = it.eventKey
            soundPathField.text = it.soundPath
            nameField.text = it.name
            regexField.text = it.regex
            enabledCheckbox.isSelected = it.isEnabled
        }

        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(GridLayout(5, 2, 10, 10))
        
        panel.add(JLabel("事件Key:"))
        panel.add(eventKeyCombo)
        
        panel.add(JLabel("声音路径:"))
        val soundPathPanel = JPanel(BorderLayout(5, 0))
        soundPathPanel.add(soundPathField, BorderLayout.CENTER)
        val testButton = JButton("测试")
        testButton.addActionListener {
            testPlay(soundPathField.text)
        }
        soundPathPanel.add(testButton, BorderLayout.EAST)
        panel.add(soundPathPanel)
        
        panel.add(JLabel("名称:"))
        panel.add(nameField)
        
        panel.add(JLabel("正则表达式:"))
        panel.add(regexField)
        
        panel.add(JLabel(""))
        panel.add(enabledCheckbox)

        return panel
    }

    private fun testPlay(soundPath: String) {
        try {
            if (soundPath.startsWith("sounds/")) {
                val fileName = soundPath.substring("sounds/".length)
                val resourcePath = "/preset/$fileName"
                val inputStream = SoundPlayer::class.java.getResourceAsStream(resourcePath)
                if (inputStream != null) {
                    soundPlayer.playFromStream(inputStream)
                } else {
                    Messages.showMessageDialog(project, "找不到声音文件: $soundPath", "错误", Messages.getWarningIcon())
                }
            } else {
                val soundFile = File(soundPath)
                if (soundFile.exists()) {
                    soundPlayer.playFromFile(soundFile)
                } else {
                    Messages.showMessageDialog(project, "找不到声音文件: $soundPath", "错误", Messages.getWarningIcon())
                }
            }
        } catch (e: Exception) {
            Messages.showMessageDialog(project, "播放失败: ${e.message}", "错误", Messages.getErrorIcon())
        }
    }

    fun getMapping(): SoundMapping {
        val eventKey = if (eventKeyCombo.isEditable) {
            eventKeyCombo.editor.item?.toString() ?: ""
        } else {
            eventKeyCombo.selectedItem as String
        }
        return SoundMapping(
            eventKey = eventKey.trim(),
            soundPath = soundPathField.text.trim(),
            name = nameField.text.trim(),
            regex = regexField.text.trim(),
            isEnabled = enabledCheckbox.isSelected
        )
    }

    override fun doOKAction() {
        okClicked = true
        super.doOKAction()
    }
}
