package com.antoniofuture.ideeventsounds.intellij.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.JBColor
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.JTextField
import javax.swing.border.EmptyBorder
import javax.swing.table.AbstractTableModel
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.GridLayout
import java.io.File
import javax.swing.Icon

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
    private val configManager = ConfigManager()
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
        val topPanel = JPanel(GridLayout(0, 1))
        topPanel.add(enableCheckbox)

        val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        val addButton = JButton("添加事件")
        val editButton = JButton("编辑")
        val removeButton = JButton("删除")
        val testButton = JButton("测试播放")

        addButton.addActionListener { addMapping() }
        editButton.addActionListener { editMapping() }
        removeButton.addActionListener { removeMapping() }
        testButton.addActionListener { testPlay() }

        buttonPanel.add(addButton)
        buttonPanel.add(editButton)
        buttonPanel.add(removeButton)
        buttonPanel.add(testButton)

        eventTable.model = tableModel
        eventTable.columnModel.getColumn(0).width = 50
        eventTable.columnModel.getColumn(1).width = 150
        eventTable.columnModel.getColumn(2).width = 100
        eventTable.columnModel.getColumn(3).width = 150
        eventTable.columnModel.getColumn(4).width = 200

        val tableScrollPane = JScrollPane(eventTable)

        val infoLabel = JLabel("<html>提示：修改配置后可能需要重启 IDE 才能完全生效</html>")
        infoLabel.border = EmptyBorder(5, 5, 5, 5)

        mainPanel.add(topPanel, BorderLayout.NORTH)
        mainPanel.add(tableScrollPane, BorderLayout.CENTER)
        mainPanel.add(buttonPanel, BorderLayout.SOUTH)
        mainPanel.add(infoLabel, BorderLayout.PAGE_END)
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
            Messages.showMessageDialog(project, "配置已保存", "成功", Messages.getInformationIcon())
        } catch (e: Exception) {
            Messages.showMessageDialog(project, "保存配置失败: ${e.message}", "错误", Messages.getErrorIcon())
        }
    }

    fun isModified(): Boolean {
        if (enableCheckbox.isSelected != originalEnable) return true
        return tableModel.getMappings() != originalMappings
    }

    private fun addMapping() {
        val dialog = MappingDialog(project, null)
        if (dialog.showAndGet()) {
            tableModel.addMapping(dialog.getMapping())
        }
    }

    private fun editMapping() {
        val row = eventTable.selectedRow
        if (row >= 0) {
            val mapping = tableModel.getMapping(row)
            val dialog = MappingDialog(project, mapping)
            if (dialog.showAndGet()) {
                tableModel.updateMapping(row, dialog.getMapping())
            }
        } else {
            Messages.showInfoMessage(project, "请先选择要编辑的事件", "提示")
        }
    }

    private fun removeMapping() {
        val row = eventTable.selectedRow
        if (row >= 0) {
            val result = Messages.showYesNoDialog(
                project,
                "确定要删除选中的事件吗？",
                "确认删除",
                Messages.getQuestionIcon()
            )
            if (result == 0) {
                tableModel.removeMapping(row)
            }
        } else {
            Messages.showInfoMessage(project, "请先选择要删除的事件", "提示")
        }
    }

    private fun testPlay() {
        val row = eventTable.selectedRow
        if (row >= 0) {
            val mapping = tableModel.getMapping(row)
            try {
                val resourceDir = System.getProperty("project.resources")
                val soundFile = if (resourceDir != null) {
                    File(resourceDir, mapping.soundPath)
                } else {
                    File(mapping.soundPath)
                }

                if (soundFile.exists()) {
                    soundPlayer.playFromFile(soundFile)
                    Messages.showInfoMessage(project, "正在播放: ${mapping.name}", "测试播放")
                } else {
                    Messages.showMessageDialog(project, "找不到声音文件: ${mapping.soundPath}", "错误", Messages.getWarningIcon())
                }
            } catch (e: Exception) {
                Messages.showMessageDialog(project, "播放失败: ${e.message}", "错误", Messages.getErrorIcon())
            }
        } else {
            Messages.showInfoMessage(project, "请先选择要测试的事件", "提示")
        }
    }
}

class SoundMappingTableModel : AbstractTableModel() {
    private val columns = arrayOf("启用", "事件Key", "名称", "正则表达式", "声音文件")
    private val mappings = mutableListOf<SoundMapping>()

    fun setMappings(list: List<SoundMapping>) {
        mappings.clear()
        mappings.addAll(list)
        fireTableDataChanged()
    }

    fun getMapping(row: Int) = mappings[row]

    fun addMapping(mapping: SoundMapping) {
        mappings.add(mapping)
        fireTableRowsInserted(mappings.size - 1, mappings.size - 1)
    }

    fun updateMapping(row: Int, mapping: SoundMapping) {
        mappings[row] = mapping
        fireTableRowsUpdated(row, row)
    }

    fun removeMapping(row: Int) {
        mappings.removeAt(row)
        fireTableRowsDeleted(row, row)
    }

    fun getMappings() = mappings.toList()

    override fun getColumnCount() = columns.size
    override fun getRowCount() = mappings.size
    override fun getColumnName(col: Int) = columns[col]

    override fun getValueAt(row: Int, col: Int): Any {
        val m = mappings[row]
        return when (col) {
            0 -> m.isEnabled
            1 -> m.eventKey
            2 -> m.name
            3 -> m.regex
            4 -> m.soundPath
            else -> ""
        }
    }

    override fun setValueAt(value: Any, row: Int, col: Int) {
        val m = mappings[row]
        mappings[row] = when (col) {
            0 -> m.copy(isEnabled = value as Boolean)
            1 -> m.copy(eventKey = value as String)
            2 -> m.copy(name = value as String)
            3 -> m.copy(regex = value as String)
            4 -> m.copy(soundPath = value as String)
            else -> m
        }
        fireTableRowsUpdated(row, row)
    }

    override fun isCellEditable(row: Int, col: Int) = col == 0

    override fun getColumnClass(col: Int): Class<*> {
        return when (col) {
            0 -> java.lang.Boolean::class.java
            else -> String::class.java
        }
    }
}

class MappingDialog(private val project: Project, private val initialMapping: SoundMapping?) :
    DialogWrapper(project) {

    private var eventKeyField = JTextField(20)
    private var nameField = JTextField(20)
    private var regexField = JTextField(20)
    private var soundPathField = JTextField(20)
    private var enabledCheckbox = JCheckBox("启用")
    private var browseButton = JButton("浏览...")

    private var resultMapping: SoundMapping? = null

    init {
        title = if (initialMapping == null) "添加事件" else "编辑事件"

        initialMapping?.let {
            eventKeyField.text = it.eventKey
            nameField.text = it.name
            regexField.text = it.regex
            soundPathField.text = it.soundPath
            enabledCheckbox.isSelected = it.isEnabled
        } ?: run {
            enabledCheckbox.isSelected = true
        }

        browseButton.addActionListener {
            val descriptor = com.intellij.openapi.fileChooser.FileChooserDescriptor(
                true, false, false, false, false, false
            )
            val file = com.intellij.openapi.fileChooser.FileChooser.chooseFile(descriptor, project, null)
            file?.let { soundPathField.text = it.path }
        }

        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(GridLayout(0, 2, 5, 5))
        panel.border = EmptyBorder(10, 10, 10, 10)

        panel.add(JLabel("事件Key:"))
        panel.add(eventKeyField)

        panel.add(JLabel("名称:"))
        panel.add(nameField)

        panel.add(JLabel("正则表达式:"))
        panel.add(regexField)

        panel.add(JLabel("声音文件:"))
        val soundPanel = JPanel(BorderLayout())
        soundPanel.add(soundPathField, BorderLayout.CENTER)
        soundPanel.add(browseButton, BorderLayout.EAST)
        panel.add(soundPanel)

        panel.add(JLabel(""))
        panel.add(enabledCheckbox)

        return panel
    }

    fun getMapping(): SoundMapping {
        return SoundMapping(
            eventKey = eventKeyField.text.trim(),
            soundPath = soundPathField.text.trim(),
            name = nameField.text.trim(),
            regex = regexField.text.trim(),
            isEnabled = enabledCheckbox.isSelected
        )
    }

    override fun doOKAction() {
        if (eventKeyField.text.isBlank()) {
            Messages.showMessageDialog(project, "事件Key不能为空", "验证错误", Messages.getWarningIcon())
            return
        }
        if (soundPathField.text.isBlank()) {
            Messages.showMessageDialog(project, "声音文件不能为空", "验证错误", Messages.getWarningIcon())
            return
        }
        resultMapping = getMapping()
        super.doOKAction()
    }

    override fun showAndGet(): Boolean {
        show()
        return resultMapping != null
    }
}