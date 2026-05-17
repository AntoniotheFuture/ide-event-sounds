package com.antoniofuture.ideeventsounds.intellij.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.ToolbarDecorator
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.border.EmptyBorder
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableColumn
import java.awt.BorderLayout
import java.awt.GridLayout
import java.io.File
import javax.swing.DefaultCellEditor

import com.antoniofuture.ideeventsounds.core.config.ConfigManager
import com.antoniofuture.ideeventsounds.core.config.SoundMapping
import com.antoniofuture.ideeventsounds.core.soundplayer.SoundPlayer
import com.antoniofuture.ideeventsounds.intellij.plugin.EventSoundsPluginService
import com.intellij.openapi.components.service
import java.awt.Desktop

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
        eventTable.model = tableModel
        eventTable.columnModel.getColumn(0).preferredWidth = 60
        eventTable.columnModel.getColumn(1).preferredWidth = 150
        eventTable.columnModel.getColumn(2).preferredWidth = 120
        eventTable.columnModel.getColumn(3).preferredWidth = 250
        eventTable.columnModel.getColumn(4).preferredWidth = 150
        
        eventTable.columnModel.getColumn(0).minWidth = 50
        eventTable.columnModel.getColumn(1).minWidth = 100
        eventTable.columnModel.getColumn(2).minWidth = 80
        eventTable.columnModel.getColumn(3).minWidth = 120
        eventTable.columnModel.getColumn(4).minWidth = 80
        
        eventTable.rowHeight = 30
        eventTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION)
        eventTable.autoResizeMode = javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS
        
        eventTable.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseClicked(e: java.awt.event.MouseEvent) {
                val point = e.point
                val row = eventTable.rowAtPoint(point)
                val col = eventTable.columnAtPoint(point)
                
                if (row >= 0 && col == 3) {
                    testPlay(tableModel.getMapping(row).soundPath)
                }
            }
        })

        val tableWithToolbar = ToolbarDecorator.createDecorator(eventTable)
            .setAddAction { addMapping() }
            .setEditAction { 
                val selectedRow = eventTable.selectedRow
                if (selectedRow >= 0) {
                    editMapping(selectedRow)
                }
            }
            .setRemoveAction { 
                val selectedRow = eventTable.selectedRow
                if (selectedRow >= 0) {
                    removeMapping(selectedRow)
                }
            }
            .createPanel()

        val infoLabel = JLabel("<html>提示：修改配置后需要点击【确定】或【应用】按钮保存，配置会立即生效</html>")
        infoLabel.border = EmptyBorder(5, 5, 5, 5)

        val testNotificationBtn = JButton("发送测试通知")
        testNotificationBtn.addActionListener {
            val service = project.service<EventSoundsPluginService>()
            service.triggerNotification("测试通知", "这是一条测试消息内容")
        }

        val githubLink = JLabel("<html><a href='https://github.com/antoniothefuture/ide-event-sounds'>GitHub 仓库</a></html>")
        githubLink.cursor = java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR)
        githubLink.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseClicked(e: java.awt.event.MouseEvent) {
                try {
                    Desktop.getDesktop().browse(java.net.URI("https://github.com/antoniothefuture/ide-event-sounds"))
                } catch (ex: Exception) {
                    Messages.showMessageDialog(project, "无法打开链接: ${ex.message}", "错误", Messages.getErrorIcon())
                }
            }
        })

        val southPanel = JPanel(BorderLayout())
        southPanel.add(infoLabel, BorderLayout.CENTER)
        val rightPanel = JPanel(BorderLayout())
        rightPanel.add(testNotificationBtn, BorderLayout.WEST)
        rightPanel.add(githubLink, BorderLayout.EAST)
        southPanel.add(rightPanel, BorderLayout.EAST)

        mainPanel.add(enableCheckbox, BorderLayout.NORTH)
        mainPanel.add(tableWithToolbar, BorderLayout.CENTER)
        mainPanel.add(southPanel, BorderLayout.SOUTH)
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
            } else if (soundPath.startsWith("preset/")) {
                val fileName = soundPath.substring("preset/".length)
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
    private val columns = arrayOf("启用", "事件Key", "名称", "声音路径", "正则")
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
            3 -> "🔊 " + mapping.soundPath
            4 -> mapping.regex
            else -> ""
        }
    }
    
    override fun getColumnClass(col: Int): Class<*> {
        return when (col) {
            0 -> java.lang.Boolean::class.java
            else -> java.lang.String::class.java
        }
    }
    
    override fun isCellEditable(row: Int, col: Int): Boolean = col == 0

    override fun setValueAt(value: Any, row: Int, col: Int) {
        if (col == 0) {
            mappings[row] = mappings[row].copy(isEnabled = value as Boolean)
        }
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
        "git.pull.success", "git.pull.failed", "indexing.started", "indexing.finished",
        "notification"
    )
    
    private var eventKeyCombo = JComboBox(eventKeys.toTypedArray())
    private var soundPathField = TextFieldWithBrowseButton()
    private var nameField = javax.swing.JTextField()
    private var regexField = javax.swing.JTextField()
    private var enabledCheckbox = JCheckBox("启用")
    private val soundPlayer = SoundPlayer()

    var okClicked = false

    init {
        title = if (initialMapping == null) "添加事件" else "编辑事件"
        eventKeyCombo.isEditable = true
        
        soundPathField.addBrowseFolderListener(
            "选择声音文件",
            "请选择一个声音文件",
            project,
            FileChooserDescriptorFactory.createSingleFileDescriptor()
        )

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
        val testButton = JButton("🔊")
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
            } else if (soundPath.startsWith("preset/")) {
                val fileName = soundPath.substring("preset/".length)
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
