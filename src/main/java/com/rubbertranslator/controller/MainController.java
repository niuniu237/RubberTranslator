package com.rubbertranslator.controller;

import com.rubbertranslator.App;
import com.rubbertranslator.modules.TranslatorFacade;
import com.rubbertranslator.modules.system.SystemConfiguration;
import com.rubbertranslator.modules.system.SystemResourceManager;
import com.rubbertranslator.modules.textinput.TextInputListener;
import com.rubbertranslator.modules.textinput.ocr.OCRUtils;
import com.rubbertranslator.modules.translate.Language;
import com.rubbertranslator.modules.translate.TranslatorType;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;

import java.awt.*;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author Raven
 * @version 1.0
 * @date 2020/5/6 20:49
 */
public class MainController implements TranslatorFacade.TranslatorFacadeListener, TextInputListener {

    // 主功能区
    @FXML
    private TextArea originTextArea;
    @FXML
    private TextArea translatedTextAre;

    // menuBar区
    // " 功能开关 "
    @FXML   // 翻译引擎类型
    private ToggleGroup translatorGroup;
    @FXML
    private RadioMenuItem googleTranslator;
    @FXML
    private RadioMenuItem baiduTranslator;
    @FXML
    private RadioMenuItem youdaoTranslator;

    @FXML // 源语言类型
    private ToggleGroup sourceLanguageGroup;
    @FXML
    private RadioMenuItem srcSimpleChinese;
    @FXML
    private RadioMenuItem srcTraditionalChinese;
    @FXML
    private RadioMenuItem srcEnglish;
    @FXML
    private RadioMenuItem srcJapanese;
    @FXML
    private RadioMenuItem srcFrench;


    @FXML // 目标语言
    private ToggleGroup destLanguageGroup;
    @FXML
    private RadioMenuItem destSimpleChinese;
    @FXML
    private RadioMenuItem destTraditionalChinese;
    @FXML
    private RadioMenuItem destEnglish;
    @FXML
    private RadioMenuItem destJapanese;
    @FXML
    private RadioMenuItem destFrench;


    @FXML // 监听剪切板
    private RadioMenuItem clipboardListenerMenu;
    @FXML // 拖拽复制
    private RadioMenuItem dragCopyMenu;
    @FXML // 增量复制
    private RadioMenuItem incrementalCopyMenu;
    @FXML // 保持段落格式
    private RadioMenuItem keepParagraphMenu;
    @FXML // 置顶
    private RadioMenuItem keepTopMenu;

    // 专注模式
    @FXML
    private Menu focusMenu;

    /**
     * 组件初始化完成后，会调用这个方法
     */
    @FXML
    public void initialize() {
        initListeners();
        initViews();
    }

    private void initListeners() {
        // 注册文本变化监听
        SystemResourceManager.getClipBoardListenerThread().setTextInputListener(this);
        // 注册翻译完成监听
        SystemResourceManager.getFacade().setFacadeListener(this);
    }

    private void initViews() {
        // 加载配置
        SystemConfiguration configuration = SystemResourceManager.getConfiguration();
        initFeatureSwitcherMenu(configuration);
    }

    private void initFeatureSwitcherMenu(SystemConfiguration configuration) {
        initTranslatorType(configuration.getTranslatorConfig().getCurrentTranslator());
        initSrcDestLanguage(configuration.getTranslatorConfig().getSourceLanguage(), configuration.getTranslatorConfig().getDestLanguage());
        initFeatureSwitcherOthers(configuration);
        Logger.getLogger(this.getClass().getName()).info("初始化功能开关成功");
    }

    private void initFeatureSwitcherOthers(SystemConfiguration configuration) {
        // 设置onActionListener
        clipboardListenerMenu.setOnAction((actionEvent -> {
            SystemResourceManager.getClipBoardListenerThread().setRun(clipboardListenerMenu.isSelected());
        }));
        dragCopyMenu.setOnAction((actionEvent -> {
            SystemResourceManager.getDragCopyThread().setRun(dragCopyMenu.isSelected());
        }));
        incrementalCopyMenu.setOnAction((actionEvent -> {
            Logger.getLogger(this.getClass().getName()).warning("暂不支持增量复制");
        }));
        keepParagraphMenu.setOnAction((actionEvent -> {
            SystemResourceManager.getFacade().getTextPreProcessor().setTryToKeepParagraph(keepParagraphMenu.isSelected());
        }));
        keepTopMenu.setOnAction((actionEvent -> {
            App.setKeepTop(keepTopMenu.isSelected());
        }));


        // 监听剪切板
        clipboardListenerMenu.setSelected(configuration.getTextInputConfig().isOpenClipboardListener());
        clipboardListenerMenu.fire();
        // 拖拽复制
        dragCopyMenu.setSelected(configuration.getTextInputConfig().isDragCopy());
        dragCopyMenu.fire();
        // 增量复制 TODO:暂不支持增量复制
        incrementalCopyMenu.setSelected(false);
        incrementalCopyMenu.fire();
        // 保持段落格式
        keepParagraphMenu.setSelected(configuration.getUiConfig().getKeepTop());
        keepParagraphMenu.fire();
        keepTopMenu.setSelected(configuration.getUiConfig().getKeepTop());
        keepTopMenu.fire();
    }

    private void initTranslatorType(TranslatorType type) {
        // view
        switch (type) {
            case GOOGLE:
                googleTranslator.setSelected(true);
                break;
            case BAIDU:
                baiduTranslator.setSelected(true);
                break;
            case YOUDAO:
                youdaoTranslator.setSelected(true);
                break;
        }
        // 监听
        translatorGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            // TODO: fxml中如何引用枚举？ 这里暂时采用硬编码
            SystemConfiguration.TranslatorConfig translatorConfig = SystemResourceManager.getConfiguration().getTranslatorConfig();
            if (newValue == googleTranslator) {
                translatorConfig.setCurrentTranslator(TranslatorType.GOOGLE);
            } else if (newValue == baiduTranslator) {
                translatorConfig.setCurrentTranslator(TranslatorType.BAIDU);
            } else if (newValue == youdaoTranslator) {
                translatorConfig.setCurrentTranslator(TranslatorType.YOUDAO);
            }
        });
    }

    private void initSrcDestLanguage(Language src, Language dest) {
        // src
        initLanguage(src, srcSimpleChinese, srcTraditionalChinese, srcEnglish, srcFrench, srcJapanese);
        // dest
        initLanguage(dest, destSimpleChinese, destTraditionalChinese, destEnglish, destFrench, destJapanese);
    }

    private void initLanguage(Language type, RadioMenuItem simpleChinese, RadioMenuItem traditional,
                              RadioMenuItem english, RadioMenuItem french, RadioMenuItem japanese) {
        switch (type) {
            case CHINESE_SIMPLIFIED:
                simpleChinese.setSelected(true);
                break;
            case CHINESE_TRADITIONAL:
                traditional.setSelected(true);
                break;
            case ENGLISH:
                english.setSelected(true);
                break;
            case FRENCH:
                french.setSelected(true);
                break;
            case JAPANESE:
                japanese.setSelected(true);
                break;
        }
    }


    @FXML
    public void onBtnTranslateClick(ActionEvent actionEvent) {
        String originText = originTextArea.getText();
        processTranslate(originText);
    }

    public static void switchToFocusMode(MouseEvent event) {
        try {
            App.setRoot(ControllerConstant.FOCUS_CONTROLLER_FXML);
        } catch (IOException e) {
            e.printStackTrace();
            Logger.getLogger(MainController.class.getName()).warning(e.getMessage());
        }
    }


    private void processTranslate(String text) {
        SystemResourceManager.getFacade().process(text);
    }

    @Override
    public void onTextInput(String text) {
        Platform.runLater(() -> originTextArea.setText(text));
        // 翻译
        processTranslate(text);
    }

    @Override
    public void onImageInput(Image image) {
        try {
            String text = OCRUtils.ocr(image);
            if (text != null) {
                onTextInput(text);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Logger.getLogger(this.getClass().getName()).warning(e.getMessage());
        }
    }

    @Override
    public void onComplete(String text) {
        // 不管从哪里会回调，回到UI线程
        Platform.runLater(() -> translatedTextAre.setText(text));
    }

}
